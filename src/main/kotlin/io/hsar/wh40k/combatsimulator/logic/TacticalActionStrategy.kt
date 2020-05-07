package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.random.AverageDice

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActions: Collection<TurnAction>): List<TurnAction> {
        // Shit implementation - units will only ever aim and fire their max damage attack
        val aimAction = possibleActions
                .find { action ->
                    action is TurnAction.HalfAim
                }

        val maxDamageAttackAction = possibleActions
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


        return listOfNotNull(aimAction, maxDamageAttackAction)
    }

    //TODO add collision detection
}