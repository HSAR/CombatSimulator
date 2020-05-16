package io.hsar.wh40k.combatsimulator.model

import TestUtils
import io.hsar.wh40k.combatsimulator.logic.*
import io.hsar.wh40k.combatsimulator.model.AttackExecution.calcDamage
import io.hsar.wh40k.combatsimulator.model.AttackExecution.rollHits
import io.hsar.wh40k.combatsimulator.random.RandomDice
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.mock


class WorldTest {

    @Test
    fun testExecuteActions() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
        val aimAcion = AimAction(HalfAim)
        val singleRangedAttack = SingleRangedAttack(30, "1d10+4")
        val targetedAction = TargetedAction(singleRangedAttack, world.enemyForces[0])
        val actionsToExecute = listOf<TurnAction>(
                aimAcion, targetedAction
        )
        val mockAttackExecution = mock(AttackExecution.javaClass)
        Mockito.`when`(mockAttackExecution.rollHits(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenAnswer {
            return@thenAnswer 1
        }
        Mockito.`when`(mockAttackExecution.calcDamage(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenAnswer {
            return@thenAnswer 5
        }
        world.executeActions(world.friendlyForces[0], actionsToExecute)
    }

    @Test
    fun testCanMoveToUnitInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMove), equalTo(true))
    }

    @Test
    fun testCanMoveToUnitOutOfRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(6, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMove), equalTo(false))
    }

    @Test
    fun testDistanceApart() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(10, 4))
        assertThat(world.distanceApart(world.friendlyForces[0], world.enemyForces[0]), equalTo(9))
    }

    @Test
    fun testMapPositionDistancetoPosition() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first.distanceToPosition(second), equalTo(9))
    }

    @Test()
    fun testMapPositionOperatorMinus() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first - second, equalTo(9))
    }
}
