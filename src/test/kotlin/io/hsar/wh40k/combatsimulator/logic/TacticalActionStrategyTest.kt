package io.hsar.wh40k.combatsimulator.logic

import TestUtils
import io.hsar.wh40k.combatsimulator.logic.actionoptions.*
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class TacticalActionStrategyTest {



    @Test
    fun `decideTurnActions returns legal combo with highest EV (and rules out two attacks)`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val mockActions = TestUtils.getMockedBasicActions()

        val suggestedActions  = unit.tacticalActionStrategy.decideTurnActions(world, unit, mockActions.values)

        assertThat("Suggested actions starts with aim to yield highest EV when subsequently shooting",
                suggestedActions[0].action, equalTo(mockActions["HalfAim"]))
        assertThat("Aim action is targeted on user",
                suggestedActions[0].target, equalTo(unit))

        assertThat("Suggested actions shoots after aiming to maximise EV",
                suggestedActions[1].action, equalTo(mockActions["SingleRangedAttack"]))
        assertThat("Single ranged attack action is targeted on enemy",
                suggestedActions[1].target, equalTo(world.enemyForces[0]))
    }

    @Test
    fun `decideTurnActions returns same suggestion regardless of order of ActionOptions input`(){
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val mockActions = TestUtils.getMockedBasicActions()
        val actionOptions = mockActions.values.toList()

        val suggestedActions1  = unit.tacticalActionStrategy.decideTurnActions(world, unit,
            listOf(actionOptions[0], actionOptions[1], actionOptions[2]))
        val suggestedActions2  = unit.tacticalActionStrategy.decideTurnActions(world, unit,
            listOf(actionOptions[2], actionOptions[0], actionOptions[1]))

        assertThat("ActionOptions order does not affect suggested actions output",
            suggestedActions1[0].target, equalTo(suggestedActions2[0].target))
        assertThat("ActionOptions order does not affect suggested actions output",
                suggestedActions1[0].action, equalTo(suggestedActions2[0].action))
        assertThat("ActionOptions order does not affect suggested actions output",
                suggestedActions1[1].target, equalTo(suggestedActions2[1].target))
        assertThat("ActionOptions order does not affect suggested actions output",
                suggestedActions1[1].action, equalTo(suggestedActions2[1].action))
    }

    @Test
    fun `decideTurnActions filters out illegal actions`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val mockActions = TestUtils.getMockedBasicActions()
        val mockedIllegalAim= TestUtils.getMockedIllegalAction()
        val actionOptions = mockActions.values.toList<ActionOption>() + mockedIllegalAim

        val suggestedActions  = unit.tacticalActionStrategy.decideTurnActions(world, unit,
                actionOptions)

        assertThat("Illegal action not in suggested actions",
            suggestedActions[0].action == mockedIllegalAim || suggestedActions[1].action == mockedIllegalAim,
            equalTo(false))
    }

    @Test
    fun `decideTurnActions deals with full action when most valuable option`(){
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val mockActions = TestUtils.getMockedBasicActions()

        val mockedHighValueFullAction = TestUtils.getMockedHighValueFullAction()
        val actionOptions = mockActions.values.toList<ActionOption>() + mockedHighValueFullAction

        val suggestedActions  = unit.tacticalActionStrategy.decideTurnActions(world, unit,
                actionOptions)
        assertThat("Full action provided as suggested action due to highest value",
                suggestedActions.size, equalTo(1))
        assertThat("Full action provided as suggested action due to highest value",
                suggestedActions[0].action, equalTo(mockedHighValueFullAction))
    }

    @Test
    fun `decideTurnActions deals with full action when not most valuable option`(){
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val mockActions = TestUtils.getMockedBasicActions()

        val mockedLowValueFullAction = TestUtils.getMockedLowValueFullAction()
        val actionOptions = mockActions.values.toList<ActionOption>() + mockedLowValueFullAction

        val suggestedActions  = unit.tacticalActionStrategy.decideTurnActions(world, unit,
                actionOptions)
        assertThat("Half actions chosen over full actions due to higher value",
                suggestedActions.size, equalTo(2))
        assertThat("Full action provided as suggested action due to highest value",
                suggestedActions[0].action == mockedLowValueFullAction ||
                suggestedActions[1].action == mockedLowValueFullAction, equalTo(false))
    }

    @Test
    fun `decideTurnactions does not affect actual world while applying actions`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]

        val startingUnitHealth = unit.currentAttributes[Attribute.CURRENT_HEALTH]
        val startingUnitPosition = world.getPosition(unit)

        val startingEnemyHealth = world.enemyForces[0].currentAttributes[Attribute.CURRENT_HEALTH]
        val startingEnemyPosition = world.getPosition(world.enemyForces[0])

        val mockActions = TestUtils.getMockedBasicActions()

        val suggestedActions = unit.tacticalActionStrategy.decideTurnActions(world, unit,
                mockActions.values)

        assertThat("User health has not changed from calculating best action",
        unit.currentAttributes[Attribute.CURRENT_HEALTH], equalTo(startingUnitHealth))
        assertThat("User position has not changed from calculating best action",
                world.getPosition(unit), equalTo(startingUnitPosition))
        assertThat("Enemy health has not changed from calculating best action",
                world.enemyForces[0].currentAttributes[Attribute.CURRENT_HEALTH], equalTo(startingEnemyHealth))
        assertThat("Enemy position has not changed from calculating best action",
                world.getPosition(world.enemyForces[0]), equalTo(startingEnemyPosition))

    }

    @Test
    fun `decideTurnActions chooses best target when multiple available`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2, 3), MapPosition(10, 5))
        val unit = world.friendlyForces[0]
        val otherEnemy = TestUtils.getGenericUnitInstance()
        world.enemyForces += otherEnemy
        world.unitPositions[otherEnemy] = MapPosition(99,99)

        val mockActions = TestUtils.getMockedBasicActions()
        val mockedPositionDependentAction = TestUtils.getMockedPositionDependentAction()
        val actionOptions = mockActions.values.toList() + mockedPositionDependentAction

        val suggestedActions = unit.tacticalActionStrategy.decideTurnActions(world, unit,
                actionOptions)

        assertThat("Target yielding highest expected value is suggested",
            suggestedActions[0].target, equalTo(otherEnemy))



    }
}
