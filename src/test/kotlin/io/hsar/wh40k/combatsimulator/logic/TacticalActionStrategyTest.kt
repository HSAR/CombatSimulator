package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class TacticalActionStrategyTest {
    private val meleeAttack = ActionOption.MeleeAttack("1d10+4")
    private val singleRangedAttack = ActionOption.SingleRangedAttack("1d10+2", 30)
    private val chargeAttack = ActionOption.ChargeAttack("1d10+5")
    private val aimAction = ActionOption.HalfAim

    @Test
    fun decideTurnActions() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(10,5))
        val unit = world.friendlyForces[0]
        val feasibleActions: List<TurnAction> = TacticalActionStrategy.decideTurnActions(world, unit, listOf(meleeAttack, singleRangedAttack, chargeAttack, aimAction))
        assertThat(feasibleActions.size, equalTo(2))
        assertThat(feasibleActions[0] is AimAction, equalTo(true))
        assertThat(feasibleActions[0].action as ActionOption.HalfAim, equalTo(aimAction))
        assertThat(feasibleActions[1].action is ActionOption.ChargeAttack, equalTo(true))
        assertThat(feasibleActions[1].action as ActionOption.ChargeAttack, equalTo(chargeAttack))
    }

    @Test
    fun decideTurnActionsMultipleTargets() {
        // The logic should dictate that we will charge the extra enemy, as it will do more damage
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(4,5))
        val unit = world.friendlyForces[0]
        val extraEnemy = TestUtils.getGenericUnitInstance()
        world.enemyForces.add(extraEnemy)
        world.unitPositions[extraEnemy] = MapPosition(10,4)

        val feasibleActions: List<TurnAction> = TacticalActionStrategy.decideTurnActions(world, unit, listOf(meleeAttack, singleRangedAttack, chargeAttack, aimAction))
        assertThat(feasibleActions.size, equalTo(2))
        assertThat(feasibleActions[0] is AimAction, equalTo(true))
        assertThat(feasibleActions[1].action is ActionOption.ChargeAttack, equalTo(true))
        assertThat((feasibleActions[1] as TargetedAction).target, equalTo(extraEnemy))
    }

    @Test
    fun performTargeting() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(4,5))
        val unit = world.friendlyForces[0]
        val feasibleActions: List<TurnAction> = TacticalActionStrategy.getPossibleTargetedActions(world, unit, listOf(meleeAttack, singleRangedAttack, chargeAttack))
        assertThat(feasibleActions.size, equalTo(1))
        assertThat(feasibleActions[0].action is ActionOption.SingleRangedAttack, equalTo(true))
    }

}