package io.hsar.wh40k.combatsimulator.model

import TestUtils

import io.hsar.wh40k.combatsimulator.logic.HalfAim
import io.hsar.wh40k.combatsimulator.logic.SingleRangedAttack
import io.hsar.wh40k.combatsimulator.logic.TargetedAction

import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitInstanceTest {

    /*private val aimAction = SelfAction(HalfAim)
    private val singleRangedAttack = SingleRangedAttack(30, "1d10+4")


    private val world: World = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
            .also { world ->
                `when`(mockAttackExecutor.rollHits(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenReturn(1)
                `when`(mockAttackExecutor.calcDamage(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenReturn(5)
            }

    private val targetedAction: TargetedAction = TargetedAction(singleRangedAttack, world.enemyForces[0])*/





}