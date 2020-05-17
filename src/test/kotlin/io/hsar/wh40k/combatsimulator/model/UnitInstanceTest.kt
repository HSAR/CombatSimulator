package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.*
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitInstanceTest {

    private lateinit var world: World
    private lateinit var mockAttackExecutor: AttackExecutor
    private lateinit var  targetedAction: TargetedAction
    private lateinit var actionsToExecute: List<TurnAction>
    private val singleRangedAttack = SingleRangedAttack(30, "1d10+4")
    private val aimAction = AimAction(HalfAim)

    private fun setUp() {
        mockAttackExecutor = Mockito.mock(AttackExecutor::class.java)
        world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4), mockAttackExecutor)
        targetedAction = TargetedAction(singleRangedAttack, world.enemyForces[0])
        actionsToExecute = listOf(
                aimAction, targetedAction
        )
        Mockito.`when`(mockAttackExecutor.rollHits(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenReturn(1)
        Mockito.`when`(mockAttackExecutor.calcDamage(world.friendlyForces[0], world.enemyForces[0], singleRangedAttack)).thenReturn(5)
    }

    @Test
    fun `executeActions handles damage application correctly`() {
        setUp()  //  manual call as BeforeEach decorator not working
        world.friendlyForces[0].executeActions(actionsToExecute)

        val targetHealth = (world.enemyForces[0].currentAttributes[Attribute.CURRENT_HEALTH] as NumericValue).value
        val expectedHealth = 5
        assertThat(targetHealth, equalTo(expectedHealth))
    }

    @Test
    fun `executeActions applies aim effects correctly`() {
        setUp()
        world.friendlyForces[0].executeActions(actionsToExecute)
        val attackerEffects = (world.friendlyForces[0].currentAttributes[Attribute.EFFECTS] as EffectValue)
        assertThat(Effect.AIMED_HALF in attackerEffects.value, equalTo(true))
    }

}