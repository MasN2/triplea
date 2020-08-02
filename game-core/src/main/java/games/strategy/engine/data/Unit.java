package games.strategy.engine.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import games.strategy.triplea.Properties;
import games.strategy.triplea.attachments.TerritoryAttachment;
import games.strategy.triplea.attachments.UnitAttachment;
import games.strategy.triplea.delegate.Matches;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.triplea.java.collections.CollectionUtils;

/**
 * Extended unit for triplea games.
 *
 * <p>As with all game data components, changes made to this unit must be made through a Change
 * instance. Calling setters on this directly will not serialize the changes across the network.
 */
@Log
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Unit extends GameDataComponent implements DynamicallyModifiable {
  public static final String TRANSPORTED_BY = "transportedBy";
  public static final String UNLOADED = "unloaded";
  public static final String LOADED_THIS_TURN = "wasLoadedThisTurn";
  public static final String UNLOADED_TO = "unloadedTo";
  public static final String UNLOADED_IN_COMBAT_PHASE = "wasUnloadedInCombatPhase";
  public static final String ALREADY_MOVED = "alreadyMoved";
  public static final String BONUS_MOVEMENT = "bonusMovement";
  public static final String SUBMERGED = "submerged";
  public static final String WAS_IN_COMBAT = "wasInCombat";
  public static final String LOADED_AFTER_COMBAT = "wasLoadedAfterCombat";
  public static final String UNLOADED_AMPHIBIOUS = "wasAmphibious";
  public static final String ORIGINATED_FROM = "originatedFrom";
  public static final String WAS_SCRAMBLED = "wasScrambled";
  public static final String MAX_SCRAMBLE_COUNT = "maxScrambleCount";
  public static final String WAS_IN_AIR_BATTLE = "wasInAirBattle";
  public static final String LAUNCHED = "launched";
  public static final String AIRBORNE = "airborne";
  public static final String CHARGED_FLAT_FUEL_COST = "chargedFlatFuelCost";

  private static final long serialVersionUID = -79061939642779999L;

  private GamePlayer owner;
  private final UUID id;
  @Setter private int hits = 0;
  private final UnitType type;

  // the transport that is currently transporting us
  private Unit transportedBy = null;
  // the units we have unloaded this turn
  private List<Unit> unloaded = List.of();
  // was this unit loaded this turn?
  private boolean wasLoadedThisTurn = false;
  // the territory this unit was unloaded to this turn
  private Territory unloadedTo = null;
  // was this unit unloaded in combat phase this turn?
  private boolean wasUnloadedInCombatPhase = false;
  // movement used this turn
  private BigDecimal alreadyMoved = BigDecimal.ZERO;
  // movement used this turn
  private int bonusMovement = 0;
  // amount of damage unit has sustained
  private int unitDamage = 0;
  // is this submarine submerged
  private boolean submerged = false;
  // original owner of this unit
  private GamePlayer originalOwner = null;
  // Was this unit in combat
  private boolean wasInCombat = false;
  private boolean wasLoadedAfterCombat = false;
  private boolean wasAmphibious = false;
  // the territory this unit started in (for use with scrambling)
  private Territory originatedFrom = null;
  private boolean wasScrambled = false;
  private int maxScrambleCount = -1;
  private boolean wasInAirBattle = false;
  private boolean disabled = false;
  // the number of airborne units launched by this unit this turn
  private int launched = 0;
  // was this unit airborne and launched this turn
  private boolean airborne = false;
  // was charged flat fuel cost already this turn
  private boolean chargedFlatFuelCost = false;

  /** Creates new Unit. Owner can be null. */
  public Unit(final UnitType type, @Nullable final GamePlayer owner, final GameData data) {
    super(data);
    this.type = checkNotNull(type);
    this.id = UUID.randomUUID();

    setOwner(owner);
  }

  public UnitAttachment getUnitAttachment() {
    return (UnitAttachment) type.getAttachment("unitAttachment");
  }

  public void setOwner(final @Nullable GamePlayer player) {
    owner = Optional.ofNullable(player).orElse(GamePlayer.NULL_PLAYERID);
  }

  public boolean isEquivalent(final Unit unit) {
    return type != null
        && type.equals(unit.getType())
        && owner != null
        && owner.equals(unit.getOwner())
        && hits == unit.getHits();
  }

  public int getHowMuchCanThisUnitBeRepaired(final Territory t) {
    return Math.max(
        0, (getHowMuchDamageCanThisUnitTakeTotal(t) - getHowMuchMoreDamageCanThisUnitTake(t)));
  }

  /**
   * How much more damage can this unit take? Will return 0 if the unit cannot be damaged, or is at
   * max damage.
   */
  public int getHowMuchMoreDamageCanThisUnitTake(final Territory t) {
    if (!Matches.unitCanBeDamaged().test(this)) {
      return 0;
    }
    return Properties.getDamageFromBombingDoneToUnitsInsteadOfTerritories(getData())
        ? Math.max(0, getHowMuchDamageCanThisUnitTakeTotal(t) - getUnitDamage())
        : Integer.MAX_VALUE;
  }

  /**
   * How much damage is the max this unit can take, accounting for territory, etc. Will return -1 if
   * the unit is of the type that cannot be damaged
   */
  public int getHowMuchDamageCanThisUnitTakeTotal(final Territory t) {
    if (!Matches.unitCanBeDamaged().test(this)) {
      return -1;
    }
    final UnitAttachment ua = UnitAttachment.get(getType());
    final int territoryUnitProduction = TerritoryAttachment.getUnitProduction(t);
    if (Properties.getDamageFromBombingDoneToUnitsInsteadOfTerritories(getData())) {
      if (ua.getMaxDamage() <= 0) {
        // factories may or may not have max damage set, so we must still determine here
        // assume that if maxDamage <= 0, then the max damage must be based on the territory value
        // can use "production" or "unitProduction"
        return territoryUnitProduction * 2;
      }

      if (Matches.unitCanProduceUnits().test(this)) {
        // can use "production" or "unitProduction"
        return (ua.getCanProduceXUnits() < 0)
            ? territoryUnitProduction * ua.getMaxDamage()
            : ua.getMaxDamage();
      }

      return ua.getMaxDamage();
    }

    return Integer.MAX_VALUE;
  }

  @Override
  public String toString() {
    // TODO: none of these should happen,... except that they did a couple times.
    if (type == null || owner == null || id == null || this.getData() == null) {
      final String text =
          "Unit.toString() -> Possible java de-serialization error: "
              + (type == null ? "Unit of UNKNOWN TYPE" : type.getName())
              + " owned by "
              + (owner == null ? "UNKNOWN OWNER" : owner.getName())
              + " with id: "
              + getId();
      UnitDeserializationErrorLazyMessage.printError(text);
      return text;
    }
    return type.getName() + " owned by " + owner.getName();
  }

  public String toStringNoOwner() {
    return type.getName();
  }

  /**
   * Until this error gets fixed, lets not scare the crap out of our users, as the problem doesn't
   * seem to be causing any serious issues. TODO: fix the root cause of this deserialization issue
   * (probably a circular dependency somewhere)
   */
  public static final class UnitDeserializationErrorLazyMessage {
    private static boolean shownError = false;

    private UnitDeserializationErrorLazyMessage() {}

    private static void printError(final String errorMessage) {
      if (!shownError) {
        shownError = true;
        log.severe(errorMessage);
      }
    }
  }

  @Override
  public Map<String, MutableProperty<?>> getPropertyMap() {
    return ImmutableMap.<String, MutableProperty<?>>builder()
        .put("owner", MutableProperty.ofSimple(this::setOwner, this::getOwner))
        .put("uid", MutableProperty.ofReadOnlySimple(this::getId))
        .put("hits", MutableProperty.ofSimple(this::setHits, this::getHits))
        .put("type", MutableProperty.ofReadOnlySimple(this::getType))
        .put(
            "transportedBy",
            MutableProperty.ofSimple(this::setTransportedBy, this::getTransportedBy))
        .put("unloaded", MutableProperty.ofSimple(this::setUnloaded, this::getUnloaded))
        .put(
            "wasLoadedThisTurn",
            MutableProperty.ofSimple(this::setWasLoadedThisTurn, this::getWasLoadedThisTurn))
        .put("unloadedTo", MutableProperty.ofSimple(this::setUnloadedTo, this::getUnloadedTo))
        .put(
            "wasUnloadedInCombatPhase",
            MutableProperty.ofSimple(
                this::setWasUnloadedInCombatPhase, this::getWasUnloadedInCombatPhase))
        .put("alreadyMoved", MutableProperty.ofSimple(this::setAlreadyMoved, this::getAlreadyMoved))
        .put(
            "bonusMovement",
            MutableProperty.ofSimple(this::setBonusMovement, this::getBonusMovement))
        .put("unitDamage", MutableProperty.ofSimple(this::setUnitDamage, this::getUnitDamage))
        .put("submerged", MutableProperty.ofSimple(this::setSubmerged, this::getSubmerged))
        .put(
            "originalOwner",
            MutableProperty.ofSimple(this::setOriginalOwner, this::getOriginalOwner))
        .put("wasInCombat", MutableProperty.ofSimple(this::setWasInCombat, this::getWasInCombat))
        .put(
            "wasLoadedAfterCombat",
            MutableProperty.ofSimple(this::setWasLoadedAfterCombat, this::getWasLoadedAfterCombat))
        .put(
            "wasAmphibious",
            MutableProperty.ofSimple(this::setWasAmphibious, this::getWasAmphibious))
        .put(
            "originatedFrom",
            MutableProperty.ofSimple(this::setOriginatedFrom, this::getOriginatedFrom))
        .put("wasScrambled", MutableProperty.ofSimple(this::setWasScrambled, this::getWasScrambled))
        .put(
            "maxScrambleCount",
            MutableProperty.ofSimple(this::setMaxScrambleCount, this::getMaxScrambleCount))
        .put(
            "wasInAirBattle",
            MutableProperty.ofSimple(this::setWasInAirBattle, this::getWasInAirBattle))
        .put("disabled", MutableProperty.ofSimple(this::setDisabled, this::getDisabled))
        .put("launched", MutableProperty.ofSimple(this::setLaunched, this::getLaunched))
        .put("airborne", MutableProperty.ofSimple(this::setAirborne, this::getAirborne))
        .put(
            "chargedFlatFuelCost",
            MutableProperty.ofSimple(this::setChargedFlatFuelCost, this::getChargedFlatFuelCost))
        .build();
  }

  public int getUnitDamage() {
    return unitDamage;
  }

  public void setUnitDamage(final int unitDamage) {
    this.unitDamage = unitDamage;
  }

  public boolean getSubmerged() {
    return submerged;
  }

  public void setSubmerged(final boolean submerged) {
    this.submerged = submerged;
  }

  public GamePlayer getOriginalOwner() {
    return originalOwner;
  }

  private void setOriginalOwner(final GamePlayer originalOwner) {
    this.originalOwner = originalOwner;
  }

  public boolean getWasInCombat() {
    return wasInCombat;
  }

  private void setWasInCombat(final boolean value) {
    wasInCombat = value;
  }

  public boolean getWasScrambled() {
    return wasScrambled;
  }

  private void setWasScrambled(final boolean value) {
    wasScrambled = value;
  }

  public int getMaxScrambleCount() {
    return maxScrambleCount;
  }

  private void setMaxScrambleCount(final int value) {
    maxScrambleCount = value;
  }

  public int getLaunched() {
    return launched;
  }

  private void setLaunched(final int value) {
    launched = value;
  }

  public boolean getAirborne() {
    return airborne;
  }

  private void setAirborne(final boolean value) {
    airborne = value;
  }

  public boolean getChargedFlatFuelCost() {
    return chargedFlatFuelCost;
  }

  private void setChargedFlatFuelCost(final boolean value) {
    chargedFlatFuelCost = value;
  }

  private void setWasInAirBattle(final boolean value) {
    wasInAirBattle = value;
  }

  public boolean getWasInAirBattle() {
    return wasInAirBattle;
  }

  public boolean getWasLoadedAfterCombat() {
    return wasLoadedAfterCombat;
  }

  private void setWasLoadedAfterCombat(final boolean value) {
    wasLoadedAfterCombat = value;
  }

  public boolean getWasAmphibious() {
    return wasAmphibious;
  }

  private void setWasAmphibious(final boolean value) {
    wasAmphibious = value;
  }

  public boolean getDisabled() {
    return disabled;
  }

  private void setDisabled(final boolean value) {
    disabled = value;
  }

  private void setTransportedBy(final Unit transportedBy) {
    this.transportedBy = transportedBy;
  }

  /**
   * This is a very slow method because it checks all territories on the map. Try not to use this
   * method if possible.
   *
   * @return Unmodifiable collection of units that the given transport is transporting.
   */
  public List<Unit> getTransporting() {
    if (Matches.unitCanTransport().test(this) || Matches.unitIsCarrier().test(this)) {
      // we don't store the units we are transporting
      // rather we look at the transported by property of units
      for (final Territory t : getData().getMap()) {
        // find the territory this transport is in
        if (t.getUnitCollection().contains(this)) {
          return getTransporting(t.getUnitCollection());
        }
      }
    }
    return List.of();
  }

  /** @return Unmodifiable collection of units that the given transport is transporting. */
  public List<Unit> getTransporting(final Collection<Unit> transportedUnitsPossible) {
    // we don't store the units we are transporting
    // rather we look at the transported by property of units
    return Collections.unmodifiableList(
        CollectionUtils.getMatches(transportedUnitsPossible, o -> equals(o.getTransportedBy())));
  }

  public List<Unit> getUnloaded() {
    return unloaded;
  }

  private void setUnloaded(final List<Unit> unloaded) {
    if (unloaded == null || unloaded.isEmpty()) {
      this.unloaded = List.of();
    } else {
      this.unloaded = new ArrayList<>(unloaded);
    }
  }

  public boolean getWasLoadedThisTurn() {
    return wasLoadedThisTurn;
  }

  private void setWasLoadedThisTurn(final boolean value) {
    wasLoadedThisTurn = value;
  }

  public Territory getUnloadedTo() {
    return unloadedTo;
  }

  private void setUnloadedTo(final Territory unloadedTo) {
    this.unloadedTo = unloadedTo;
  }

  public Territory getOriginatedFrom() {
    return originatedFrom;
  }

  private void setOriginatedFrom(final Territory t) {
    originatedFrom = t;
  }

  public boolean getWasUnloadedInCombatPhase() {
    return wasUnloadedInCombatPhase;
  }

  private void setWasUnloadedInCombatPhase(final boolean value) {
    wasUnloadedInCombatPhase = value;
  }

  public BigDecimal getAlreadyMoved() {
    return alreadyMoved;
  }

  public void setAlreadyMoved(final BigDecimal alreadyMoved) {
    this.alreadyMoved = alreadyMoved;
  }

  private void setBonusMovement(final int bonusMovement) {
    this.bonusMovement = bonusMovement;
  }

  public int getBonusMovement() {
    return bonusMovement;
  }

  /** Does not account for any movement already made. Generally equal to UnitType movement */
  public int getMaxMovementAllowed() {
    return Math.max(0, bonusMovement + UnitAttachment.get(getType()).getMovement(getOwner()));
  }

  public BigDecimal getMovementLeft() {
    return new BigDecimal(UnitAttachment.get(getType()).getMovement(getOwner()))
        .add(new BigDecimal(bonusMovement))
        .subtract(alreadyMoved);
  }

  public boolean hasMovementLeft() {
    return getMovementLeft().compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isDamaged() {
    return unitDamage > 0 && hits > 0;
  }

  public boolean hasMoved() {
    return alreadyMoved.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Avoid calling this method, it checks every territory on the map. To undeprecate we should
   * optimize this to halt on the first territory we have found with a transporting unit, or
   * otherwise optimize this to not check every territory.
   *
   * @deprecated Avoid callling this method, it is slow, needs optimization.
   */
  @Deprecated
  public boolean isTransporting() {
    return !getTransporting().isEmpty();
  }
}
