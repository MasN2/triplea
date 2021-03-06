package games.strategy.triplea.delegate.power.calculator;

import static games.strategy.triplea.delegate.battle.steps.MockGameData.givenGameData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.GamePlayer;
import games.strategy.engine.data.Unit;
import games.strategy.engine.data.UnitType;
import games.strategy.engine.data.gameparser.GameParseException;
import games.strategy.triplea.attachments.TechAttachment;
import games.strategy.triplea.attachments.UnitSupportAttachment;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.triplea.java.collections.IntegerMap;

class AvailableSupportsTest {

  @Test
  void ruleIsAddedToTheSupportCalculator() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule), false, false));
    assertThat("There is only one bonus type", tracker.supportRules.size(), is(1));
    assertThat("The rule only has one support available", tracker.getSupportLeft(rule), is(1));
  }

  @Test
  void ruleIsIgnoredIfNoPlayers() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of())
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));
    assertThat("Rule with players is added", tracker.getSupportLeft(rule), is(1));
    assertThat("Rule without players is not added", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void ruleIsIgnoredIfNullUnitTypes() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(null)
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));
    assertThat("Rule with unit types is added", tracker.getSupportLeft(rule), is(1));
    assertThat("Rule without unit types is not added", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void ruleIsIgnoredIfEmptyUnitTypes() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of())
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));
    assertThat("Rule with unit types is added", tracker.getSupportLeft(rule), is(1));
    assertThat("Rule without unit types is not added", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void defenceRuleIsIgnoredIfOffenceIsWanted() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("defence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));
    assertThat("Offence rule has support", tracker.getSupportLeft(rule), is(1));
    assertThat("Defence rule is ignored", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void offenceRuleIsIgnoredIfDefenceIsWanted() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("defence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), true, false));
    assertThat("Defence rule has support", tracker.getSupportLeft(rule), is(1));
    assertThat("Offence rule is ignored", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void alliedRuleIsIgnoredIfEnemyIsWanted() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));
    assertThat("Enemy rule has support", tracker.getSupportLeft(rule), is(1));
    assertThat("Allied rule is ignored", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void enemyRuleIsIgnoredIfAlliedIsWanted() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, true));
    assertThat("Allied rule has support", tracker.getSupportLeft(rule), is(1));
    assertThat("Enemy rule is ignored", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void ruleWithNoMatchingSupportersIsIgnored() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setNumber(1);

    final UnitType unusedUnitType = new UnitType("unit2", gameData);
    final UnitSupportAttachment rule2 =
        new UnitSupportAttachment("rule2", unusedUnitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, true));
    assertThat("Rule with a supporter is added", tracker.getSupportLeft(rule), is(1));
    assertThat("Rule without a supporter is ignored", tracker.getSupportLeft(rule2), is(0));
  }

  @Test
  void improvedArtilleryTechnologyDoublesTheSupport() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);
    final TechAttachment techAttachment = mock(TechAttachment.class);
    when(owner.getTechAttachment()).thenReturn(techAttachment);
    when(techAttachment.getImprovedArtillerySupport()).thenReturn(true);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setImpArtTech(true)
        .setBonusType("bonus")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule), false, true));
    assertThat(
        "Rule for improved artillery gives double the support "
            + "when the unit has improved artillery",
        tracker.getSupportLeft(rule),
        is(2));
  }

  @Test
  void ruleMustWantImprovedArtilleryTechnologyToGetTheBonus() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);
    final TechAttachment techAttachment = mock(TechAttachment.class);
    when(owner.getTechAttachment()).thenReturn(techAttachment);
    when(techAttachment.getImprovedArtillerySupport()).thenReturn(true);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setImpArtTech(false)
        .setBonusType("bonus")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule), false, true));
    assertThat(
        "Rule that doesn't use improved artillery doesn't give double the support "
            + "when the unit has improved artillery",
        tracker.getSupportLeft(rule),
        is(1));
  }

  @Test
  void unitWithoutImprovedArtilleryDoesNotGetBonusWhenRuleUsesTech() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);
    final TechAttachment techAttachment = mock(TechAttachment.class);
    when(owner.getTechAttachment()).thenReturn(techAttachment);
    when(techAttachment.getImprovedArtillerySupport()).thenReturn(true);
    final GamePlayer ownerWithoutImprovedTechnology = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);
    final Unit unitWithoutImprovedTechnology =
        unitType.create(1, ownerWithoutImprovedTechnology, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("allied")
        .setPlayers(List.of(owner, ownerWithoutImprovedTechnology))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setImpArtTech(true)
        .setBonusType("bonus")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(
                List.of(unit, unitWithoutImprovedTechnology), List.of(rule), false, true));
    assertThat(
        "Unit with improved technology has a value of 2 while the unit without has a value of 1",
        tracker.getSupportLeft(rule),
        is(3));
  }

  @Test
  void ruleThatMatchesFilterIsCopiedToNewCalculator() throws GameParseException {
    final GameData gameData = givenGameData().build();

    final GamePlayer owner = mock(GamePlayer.class);

    final UnitType unitType = new UnitType("unit", gameData);
    final Unit unit = unitType.create(1, owner, true).get(0);

    final UnitSupportAttachment rule = new UnitSupportAttachment("rule", unitType, gameData);
    rule.setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus")
        .setDice("roll")
        .setNumber(1);

    final UnitSupportAttachment rule2 = new UnitSupportAttachment("rule2", unitType, gameData);
    rule2
        .setSide("offence")
        .setFaction("enemy")
        .setPlayers(List.of(owner))
        .setUnitType(Set.of(mock(UnitType.class)))
        .setBonusType("bonus2")
        .setDice("strength")
        .setNumber(1);

    final AvailableSupports tracker =
        AvailableSupports.getSupport(
            new SupportCalculator(List.of(unit), List.of(rule, rule2), false, false));

    final AvailableSupports filtered = tracker.filter(UnitSupportAttachment::getRoll);
    assertThat(
        "The roll rule is copied to the new calculator", filtered.getSupportLeft(rule), is(1));
    assertThat(
        "The strength rule is not copied to the new calculator",
        filtered.getSupportLeft(rule2),
        is(0));
    assertThat(
        "Only one bonus (the roll one) is in the new calculator",
        filtered.supportRules.keySet(),
        hasSize(1));
  }

  @Nested
  class UseSupport {

    @Test
    void supportToOneUnit() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(List.of(supportUnit), List.of(rule), false, false));

      assertThat("Support unit can give one", tracker.giveSupportToUnit(unit), is(1));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The support unit gave one support",
          tracker.getUnitsGivingSupport(),
          is(Map.of(supportUnit, IntegerMap.of(Map.of(unit, 1)))));
    }

    @Test
    void supportWithValueOfTwoForOneUnit() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(2)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(List.of(supportUnit), List.of(rule), false, false));

      assertThat("Support unit can give 2", tracker.giveSupportToUnit(unit), is(2));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The support unit gave one support of 2",
          tracker.getUnitsGivingSupport(),
          is(Map.of(supportUnit, IntegerMap.of(Map.of(unit, 2)))));
    }

    @Test
    void supportToTwoUnitsButOnlyEnoughForOne() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);
      final Unit unit2 = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(List.of(supportUnit), List.of(rule), false, false));

      // give the support to the first unit
      assertThat("Support unit can give 1", tracker.giveSupportToUnit(unit), is(1));
      // attempt to give the support to the second unit
      assertThat("Support unit has no more to give", tracker.giveSupportToUnit(unit2), is(0));

      assertThat(
          "The second unit should get no support as it was all used up",
          tracker.getUnitsGivingSupport(),
          is(Map.of(supportUnit, IntegerMap.of(Map.of(unit, 1)))));
    }

    @Test
    void supportForTwoUnits() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);
      final Unit unit2 = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(2);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(List.of(supportUnit), List.of(rule), false, false));

      assertThat("Support can give 1", tracker.giveSupportToUnit(unit), is(1));
      assertThat("Support can still give 1 more", tracker.giveSupportToUnit(unit2), is(1));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The support unit gave one support to both",
          tracker.getUnitsGivingSupport(),
          is(Map.of(supportUnit, IntegerMap.of(Map.of(unit, 1, unit2, 1)))));
    }

    @Test
    void twoSupportersGiveSupportToTwoUnits() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);
      final Unit unit2 = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);
      final Unit supportUnit2 = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule), false, false));

      assertThat("First support unit can support", tracker.giveSupportToUnit(unit), is(1));
      assertThat("Second support unit can support", tracker.giveSupportToUnit(unit2), is(1));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The first support unit supports the first unit and "
              + "the second support unit supports the second unit",
          tracker.getUnitsGivingSupport(),
          is(
              Map.of(
                  supportUnit,
                  IntegerMap.of(Map.of(unit, 1)),
                  supportUnit2,
                  IntegerMap.of(Map.of(unit2, 1)))));
    }

    @Test
    void twoSupportersInAStackGiveSupportToOneUnit() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);
      final Unit unit2 = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);
      final Unit supportUnit2 = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          // allow this support to stack up to 2 times
          .setBonusType("2:bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule), false, false));

      assertThat(
          "All support is given because of stacking", tracker.giveSupportToUnit(unit), is(2));
      assertThat("No support left because of stacking", tracker.giveSupportToUnit(unit2), is(0));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The first unit gets all the support because of stack of 2",
          tracker.getUnitsGivingSupport(),
          is(
              Map.of(
                  supportUnit,
                  IntegerMap.of(Map.of(unit, 1)),
                  supportUnit2,
                  IntegerMap.of(Map.of(unit, 1)))));
    }

    @Test
    void twoSupportersInAStackWithSupportNumberOfTwoGiveSupportToTwoUnits()
        throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);
      final Unit unit2 = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);
      final Unit supportUnit2 = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          // allow this support to stack up to 2 times
          .setBonusType("2:bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(2);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule), false, false));

      assertThat(
          "First support unit can fill the entire stack", tracker.giveSupportToUnit(unit), is(2));
      assertThat(
          "Second support unit can give its support", tracker.giveSupportToUnit(unit2), is(2));

      assertThat("All the support was used for the rule", tracker.getSupportLeft(rule), is(0));
      assertThat(
          "The first support unit can give 2 supports and the bonus stacks up to two "
              + "so the first unit gets all of its support. "
              + "The second support unit can give 2 supports and the bonus stacks up to two "
              + "so the second unit gets all of its support.",
          tracker.getUnitsGivingSupport(),
          is(
              Map.of(
                  supportUnit,
                  IntegerMap.of(Map.of(unit, 2)),
                  supportUnit2,
                  IntegerMap.of(Map.of(unit2, 2)))));
    }

    @Test
    void twoRulesOfDifferentBonusAlwaysStack() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final UnitType supportUnitType2 = new UnitType("support2", gameData);
      final Unit supportUnit2 = supportUnitType2.create(1, owner, true).get(0);

      final UnitSupportAttachment rule2 =
          new UnitSupportAttachment("rule2", supportUnitType2, gameData);
      rule2
          .setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus2")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule, rule2), false, false));

      assertThat("Both support units gave support", tracker.giveSupportToUnit(unit), is(2));

      assertThat(
          "Both support units gave their support",
          tracker.getUnitsGivingSupport(),
          is(
              Map.of(
                  supportUnit,
                  IntegerMap.of(Map.of(unit, 1)),
                  supportUnit2,
                  IntegerMap.of(Map.of(unit, 1)))));
      assertThat("First rule should have no support left", tracker.getSupportLeft(rule), is(0));
      assertThat("Second rule should have no support left", tracker.getSupportLeft(rule2), is(0));
    }

    @Test
    void twoRulesOfSameBonusWithStackOf1() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final UnitType supportUnitType2 = new UnitType("support2", gameData);
      final Unit supportUnit2 = supportUnitType2.create(1, owner, true).get(0);

      final UnitSupportAttachment rule2 =
          new UnitSupportAttachment("rule2", supportUnitType2, gameData);
      rule2
          .setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule, rule2), false, false));

      assertThat(
          "Only the first rule can give its support", tracker.giveSupportToUnit(unit), is(1));

      assertThat(
          "Only the first rule gives support because the stack size is 1",
          tracker.getUnitsGivingSupport(),
          is(Map.of(supportUnit, IntegerMap.of(Map.of(unit, 1)))));
      assertThat("First rule should have no support left", tracker.getSupportLeft(rule), is(0));
      assertThat("Second rule should have support left", tracker.getSupportLeft(rule2), is(1));
    }

    @Test
    void twoRulesOfSameBonusWithStackOf2() throws GameParseException {
      final GameData gameData = givenGameData().build();

      final GamePlayer owner = mock(GamePlayer.class);

      final UnitType unitType = new UnitType("unit", gameData);
      final Unit unit = unitType.create(1, owner, true).get(0);

      final UnitType supportUnitType = new UnitType("support", gameData);
      final Unit supportUnit = supportUnitType.create(1, owner, true).get(0);

      final UnitSupportAttachment rule =
          new UnitSupportAttachment("rule", supportUnitType, gameData);
      rule.setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("2:bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final UnitType supportUnitType2 = new UnitType("support2", gameData);
      final Unit supportUnit2 = supportUnitType2.create(1, owner, true).get(0);

      final UnitSupportAttachment rule2 =
          new UnitSupportAttachment("rule2", supportUnitType2, gameData);
      rule2
          .setSide("offence")
          .setFaction("enemy")
          .setPlayers(List.of(owner))
          .setBonusType("2:bonus")
          .setUnitType(Set.of(unitType))
          .setBonus(1)
          .setNumber(1);

      final AvailableSupports tracker =
          AvailableSupports.getSupport(
              new SupportCalculator(
                  List.of(supportUnit, supportUnit2), List.of(rule, rule2), false, false));

      assertThat("All support can be given", tracker.giveSupportToUnit(unit), is(2));

      assertThat(
          "Both support units gave their support",
          tracker.getUnitsGivingSupport(),
          is(
              Map.of(
                  supportUnit,
                  IntegerMap.of(Map.of(unit, 1)),
                  supportUnit2,
                  IntegerMap.of(Map.of(unit, 1)))));
      assertThat("First rule should have no support left", tracker.getSupportLeft(rule), is(0));
      assertThat("Second rule should have no support left", tracker.getSupportLeft(rule2), is(0));
    }
  }
}
