package io.hsar.wh40k.combatsimulator.logic.actionoptions

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class RangedAttackTest {

    val rangedAttack = VanillaRangedAttack()

    @Test
    fun `rollToHit passes correct values to rollBaseStat`() {
        val mockUser = Mockito.mock(UnitInstance::class.java)
        Mockito.`when`(mockUser.getAimBonus()).thenReturn(10)

        rangedAttack.rollToHit(mockUser)

        // check correct stat is used and bonus is aim bonus plus inherent bonus from attack
        Mockito.verify(mockUser, Mockito.times(1)).rollBaseStat(BaseStat.BALLISTIC_SKILL, 25)

        // we will only hit the below assertion if the verify call came back correct
        MatcherAssert.assertThat(true, CoreMatchers.equalTo(true))
    }

    @Test
    fun `getHitChance passes the correct values to getBaseStatSuccessChance`() {
        val mockUser = Mockito.mock(UnitInstance::class.java)
        Mockito.`when`(mockUser.getAimBonus()).thenReturn(10)

        rangedAttack.getHitChance(mockUser)

        // check correct stat is used and bonus is aim bonus plus inherent bonus from attack
        Mockito.verify(mockUser, Mockito.times(1)).getBaseStatSuccessChance(BaseStat.BALLISTIC_SKILL, 25)

        // we will only hit the below assertion if the verify call came back correct
        MatcherAssert.assertThat(true, CoreMatchers.equalTo(true))
    }
}

class VanillaRangedAttack : RangedAttack(30) {
    override val actionCost = ActionCost.FULL_ACTION
    override val hitModifier = 15
    override val damage = "1d10"
    override fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean {
        return true
    }

    override fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float {
        return 1f
    }

    override fun apply(world: World, user: UnitInstance, target: UnitInstance) {

    }
}