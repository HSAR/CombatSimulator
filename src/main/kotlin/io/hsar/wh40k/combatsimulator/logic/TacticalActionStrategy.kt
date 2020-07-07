package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TurnAction> {
        // Shit implementation - units will only ever aim and fire their max damage attack
        val aimAction = possibleActionOptions
                .find { action ->
                    action is HalfAim
                }
                ?.let { action ->
                    AimAction(action)
                }

        val maxDamageAttackAction = possibleActionOptions
                .filter { action ->
                    action is DamageCausingAction
                }
                .filterIsInstance<DamageCausingAction>()
                .let { damageCausingActions ->
                    getPossibleTargetedActions(world, thisUnit, damageCausingActions)
                    // this will produce a list of physically possible targeted actions
                }
                .map { eachTargetedAction ->
                    // Associate each action with an estimate of its damage
                    eachTargetedAction to ExpectedDamageCalculator.calculate(eachTargetedAction)
                }
                .maxBy { (_, expectedDamage) -> expectedDamage }
                ?.first

        return listOfNotNull(aimAction, maxDamageAttackAction)
    }

    fun getPossibleTargetedActions(world: World, thisUnit: UnitInstance, possibleAttacks: List<DamageCausingAction>): List<TargetedAction> {

        // This will create a list containing a TargetedAction for each feasible action/target pair
        return possibleAttacks.map { possibleAttack ->
            world.getAdversaries(thisUnit).map { adversary ->
                TargetedAction(possibleAttack as ActionOption, adversary)
            }
        }.flatten()
        .filter { targetedAction -> // filter out options that are not possible due to range etc
            when (targetedAction.action) {
                is RangedAttackAction -> targetedAction.action.range >= world.distanceApart(thisUnit, targetedAction.target)
                is ChargeAttack ->
                    targetedAction.action.isValidMovementPath(world.getPosition(thisUnit), world.getPosition(targetedAction.target))
                            && targetedAction.action.getMovementRange(thisUnit.unit.stats.baseStats.getValue(BaseStat.AGILITY)) >
                            world.distanceApart(thisUnit, targetedAction.target)
                is MeleeAttack -> world.distanceApart(thisUnit, targetedAction.target) == 1
                else -> false
            }

        }
    }

}