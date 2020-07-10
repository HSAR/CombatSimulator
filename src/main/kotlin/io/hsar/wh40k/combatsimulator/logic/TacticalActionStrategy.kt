package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.random.AverageDice

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TurnAction> {
        // Shit implementation - units will only ever aim and fire their max damage attack

        /*

         */

        // for each Action option, map to list of legal targets

        // cache some lists of units to avoid re-calling methods
        val allies = world.getAllies(thisUnit)
        val adversaries = world.getAdversaries(thisUnit)
        val allUnits = allies + adversaries
        val possibleTargetedActions = possibleActionOptions.map {actionOption ->
            when(actionOption.targetType) {
                TargetType.SELF_TARGET -> listOf<TargetedAction>(TargetedAction(actionOption, thisUnit))
                TargetType.ADVERSARY_TARGET -> adversaries.map{ adversary ->
                    TargetedAction(actionOption,adversary)
                }
                TargetType.ALLY_TARGET -> allies.map{ally ->
                    TargetedAction(actionOption,ally)
                }
                TargetType.ANY_TARGET -> allUnits.map{ anyUnit ->
                    TargetedAction(actionOption, anyUnit)
                }
            }
        }.flatten()

        val fullActionTargetedActions = possibleTargetedActions.filter { targetedAction ->
            targetedAction.action.actionCost == ActionCost.FULL_ACTION
        }.map { fullActionTargetedAction ->
            listOf(fullActionTargetedAction) // wrap each in list so that can merge with legal half action combos
        }

        val halfActionTargetedActions = possibleTargetedActions.filter { targetedAction ->
            targetedAction !in fullActionTargetedActions.flatten()
        }

        val allHalfActionCombos = halfActionTargetedActions.map { targetedAction ->
            (halfActionTargetedActions - targetedAction).map {otherAction ->
                listOf<TargetedAction>(targetedAction, otherAction)
            }
        }.flatten()
        //TODO test this thoroughly

        val allLegalHalfActionCombos = allHalfActionCombos.filter{ halfActionCombo ->
            isLegalActionPair(halfActionCombo)
        }

        val allLegalActionCombos = fullActionTargetedActions + allLegalHalfActionCombos

        // now, work out EV for each combo

        val maxDamageAttackAction = getMaxDamageAttackAction(possibleActionOptions, world, thisUnit)

        if(maxDamageAttackAction == null) {
            // try to work out if moving closer would help us
            // could we just run get possible targeted actions once and get a list containing range values
            //ie work out the difference between attack range and current distance


        }

        return listOfNotNull(aimAction, maxDamageAttackAction)
    }

    fun getExpectedValue(world: World, thisUnit: UnitInstance, actionCombo: List<TargetedAction>) {
        // set up clone of world
        val tempWorld = world.createCopy()
        var valueCount = 0f
        actionCombo.forEach { targetedAction->
            if(targetedAction.action.isLegal(tempWorld, thisUnit, targetedAction.target)) {
                valueCount += targetedAction.action.expectedValue(tempWorld, thisUnit, targetedAction.target)
                targetedAction.action.apply(tempWorld, thisUnit, targetedAction.target)
            }
        }
    }

    fun isLegalActionPair(actionPair: List<TargetedAction>): Boolean {
        return isTwoAttacks()
        // can expand this in future to check for other potentially illegal scenarios
    }

    fun isTwoAttacks(actionPair: List<TargetedAction>): Boolean {
        return when(actionPair[0].action) {
            is DamageCausingAction -> actionPair[1].action !is DamageCausingAction
            else -> true
        }
    }


    private fun getMaxDamageAttackAction(actionCombos: List<TargetedAction>, world: World, thisUnit: UnitInstance): TargetedAction? {
        return actionCombos

                //TODO clean this up and rethink

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
                    when (eachTargetedAction.action) {
                        is DamageCausingAction -> eachTargetedAction.action.damage
                                .let { damageString ->
                                    eachTargetedAction to AverageDice.roll(damageString)
                                }
                        else -> eachTargetedAction to 0
                    }
                }
                .maxBy { (_, expectedDamage) -> expectedDamage }
                ?.first
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

