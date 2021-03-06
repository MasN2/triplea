package games.strategy.triplea.delegate.power.calculator;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.Unit;
import games.strategy.triplea.Properties;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.triplea.java.collections.IntegerMap;

/**
 * Computes the total power, total strength, and total roll for a collection of units
 *
 * <p>The strength is either attack or defense plus any support. The rolls is either attackRolls or
 * defenseRolls plus any support. The power is the strength * rolls unless either LHTR Heavy Bombers
 * or chooseBestRoll is active.
 */
@Value
@Getter(AccessLevel.NONE)
public class PowerStrengthAndRolls implements TotalPowerAndTotalRolls {

  CombatValue calculator;
  GameData gameData;

  @Getter(AccessLevel.PUBLIC)
  Map<Unit, UnitPowerStrengthAndRolls> totalStrengthAndTotalRollsByUnit = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  Map<Unit, IntegerMap<Unit>> unitSupportPowerMap = new HashMap<>();

  @Getter(AccessLevel.PUBLIC)
  Map<Unit, IntegerMap<Unit>> unitSupportRollsMap = new HashMap<>();

  private PowerStrengthAndRolls(
      final GameData gameData, final Collection<Unit> units, final CombatValue calculator) {
    this.gameData = gameData;
    this.calculator = calculator;
    addUnits(units);
  }

  /**
   * Returns the power (strength) and rolls for each of the specified units.
   *
   * @param unitsGettingPowerFor should be sorted from weakest to strongest, before the method is
   *     called, for the actual battle.
   */
  public static PowerStrengthAndRolls build(
      final Collection<Unit> unitsGettingPowerFor, final CombatValue calculator) {

    if (unitsGettingPowerFor == null || unitsGettingPowerFor.isEmpty()) {
      return new PowerStrengthAndRolls(calculator.getGameData(), List.of(), calculator);
    }

    return new PowerStrengthAndRolls(calculator.getGameData(), unitsGettingPowerFor, calculator);
  }

  private void addUnits(final Collection<Unit> units) {
    final boolean lhtrBombers = Properties.getLhtrHeavyBombers(gameData);

    final StrengthCalculator strengthCalculator = calculator.getStrength();
    final RollCalculator rollCalculator = calculator.getRoll();
    for (final Unit unit : units) {
      int strength = strengthCalculator.getStrength(unit).getValue();
      int rolls = rollCalculator.getRoll(unit).getValue();
      if (rolls == 0 || strength == 0) {
        strength = 0;
        rolls = 0;
      }
      totalStrengthAndTotalRollsByUnit.put(
          unit,
          UnitPowerStrengthAndRolls.builder()
              .strength(strength)
              .rolls(rolls)
              .diceSides(calculator.getDiceSides(unit))
              .chooseBestRoll(lhtrBombers || unit.getUnitAttachment().getChooseBestRoll())
              .build());
    }

    strengthCalculator
        .getSupportGiven()
        .forEach(
            (supporter, supportedUnits) ->
                unitSupportPowerMap
                    .computeIfAbsent(supporter, (newSupport) -> new IntegerMap<>())
                    .add(supportedUnits));
    rollCalculator
        .getSupportGiven()
        .forEach(
            (supporter, supportedUnits) ->
                unitSupportRollsMap
                    .computeIfAbsent(supporter, (newSupport) -> new IntegerMap<>())
                    .add(supportedUnits));
  }

  @Override
  public int calculateTotalPower() {
    return totalStrengthAndTotalRollsByUnit.values().stream()
        .mapToInt(UnitPowerStrengthAndRolls::calculatePower)
        .sum();
  }

  @Override
  public int calculateTotalRolls() {
    return totalStrengthAndTotalRollsByUnit.values().stream()
        .mapToInt(UnitPowerStrengthAndRolls::getRolls)
        .sum();
  }

  @Override
  public boolean hasStrengthOrRolls() {
    return calculateTotalRolls() != 0 && calculateTotalPower() != 0;
  }

  @Override
  public int getStrength(final Unit unit) {
    return totalStrengthAndTotalRollsByUnit.get(unit).getStrength();
  }

  @Override
  public int getRolls(final Unit unit) {
    return totalStrengthAndTotalRollsByUnit.get(unit).getRolls();
  }
}
