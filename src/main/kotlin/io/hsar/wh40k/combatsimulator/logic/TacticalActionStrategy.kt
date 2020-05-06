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
                    // Associate each action with an estimate of its damage
                    eachDamageCausingAction to (eachDamageCausingAction as DamageCausingAction).damage
                            .let { damageString ->
                                AverageDice.roll(damageString)
                            }
                }
                .maxBy { (_, expectedDamage) -> expectedDamage }
                ?.first
                .let { maxDamageAttack ->
                    // Shit implementation - units fire their maximum damage attack and do nothing else
                    listOfNotNull(maxDamageAttack)
                }
    }
    
}