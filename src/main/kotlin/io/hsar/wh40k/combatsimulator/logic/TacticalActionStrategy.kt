package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.random.AverageDice

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActions: Collection<TurnAction>): List<TurnAction> {
        return possibleActions
                .filter { action ->
                    action is DamageCausingAction
                }
                .map { eachDamageCausingAction ->
                    // Estimate average damage
                    (eachDamageCausingAction as DamageCausingAction).damage
                            .let { damageString ->
                                AverageDice.roll(damageString)
                            } to eachDamageCausingAction
                }
                .maxBy { (expectedDamage, _) -> expectedDamage }
                ?.second
                .let { maxDamageAttack ->
                    // Shit implementation - units fire their maximum damage attack and do nothing else
                    listOf(maxDamageAttack).filterNotNull()
                }
    }
    
}