package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

/**
 * This class is responsible for working out the best actions for a unit to take on its turn, given the options
 * available to it and the context of the world around it.
 *
 * It has one public function, which is relied on by UnitInstances every turn to calculate the optimum actions
 * to take based on the state of combat at that time.
 */
object TacticalActionStrategy : ActionStrategy {

    /**
     * Given a list of ActionOptions that the unit can carry out, returns a list of TargetedActions for that turn
     * with the highest expected value
     */
    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TargetedAction> {
        if (possibleActionOptions.isEmpty()) {
            throw IllegalArgumentException("Cannot decide turn actions with no possibilities!")
        }

        // cache some lists of units to avoid re-calling methods
        val allies = world.getAllies(thisUnit)
        val adversaries = world.getAdversaries(thisUnit)
        val allUnits = allies + adversaries
        val possibleTargetedActions = possibleActionOptions
                .map { actionOption ->
                    when (actionOption.targetType) {
                        TargetType.SELF_TARGET -> listOf(TargetedAction(actionOption, thisUnit))
                        TargetType.ADVERSARY_TARGET -> adversaries.map { adversary ->
                            TargetedAction(actionOption, adversary)
                        }
                        TargetType.ALLY_TARGET -> allies.map { ally ->
                            TargetedAction(actionOption, ally)
                        }
                        TargetType.ANY_TARGET -> allUnits.map { anyUnit ->
                            TargetedAction(actionOption, anyUnit)
                        }
                    }
                }
                .flatten()

        val fullActionTargetedActions = possibleTargetedActions
                .filter { targetedAction ->
                    targetedAction.action.actionCost == ActionCost.FULL_ACTION
                }
                .map { fullActionTargetedAction ->
                    listOf(fullActionTargetedAction) // wrap each in list so that can merge with legal half action combos
                }

        val allLegalHalfActionCombos = possibleTargetedActions
                .filter { targetedAction ->
                    targetedAction.action.actionCost == ActionCost.HALF_ACTION
                }

                .let { halfActionTargetedActions ->
                    halfActionTargetedActions
                            .map { targetedAction ->
                                // Create a list of all possible pairings of half actions
                                halfActionTargetedActions.map { anotherAction ->
                                    listOf(targetedAction, anotherAction)
                                }
                            }
                            .flatten()
                }
                // Remove all half action combos where both actions are the same
                .filterNot { (firstAction, secondAction) ->
                    firstAction == secondAction
                }
                // Remove illegal combinations
                .filter { halfActionCombo ->
                    isLegalActionPair(halfActionCombo)
                }

        return (fullActionTargetedActions + allLegalHalfActionCombos)
                .map { eachLegalActionCombo ->
                    getExpectedValue(world, thisUnit, eachLegalActionCombo) to eachLegalActionCombo
                }
                .maxBy { (expectedValue, _) -> expectedValue }!!
                .second
    }

    fun getExpectedValue(world: World, thisUnit: UnitInstance, actionCombo: List<TargetedAction>): Float {
        return world.createCopy()
                .let { tempWorld ->
                    tempWorld.replaceUnitInstanceWithCopy(thisUnit)
                            .let { tempUser ->
                                actionCombo.map { targetedAction ->
                                    tempWorld.replaceUnitInstanceWithCopy(targetedAction.target)
                                            // TODO not a massive issue but if both actions of a combo have the same target then any changes to the target won't carry over
                                            .let { tempTarget ->
                                                if (targetedAction.action.isLegal(tempWorld, tempUser, tempTarget)) {
                                                    targetedAction.action.expectedValue(tempWorld, tempUser, tempTarget)
                                                            .also {
                                                                targetedAction.action.apply(tempWorld, tempUser, tempTarget)
                                                            }
                                                } else {
                                                    0f
                                                }
                                            }
                                }.sum()
                            }
                }
    }

    fun isLegalActionPair(actionPair: List<TargetedAction>): Boolean {
        return !isTwoAttacks(actionPair)
        // can expand this in future to check for other potentially illegal scenarios
    }

    fun isTwoAttacks(actionPair: List<TargetedAction>): Boolean {
        return (actionPair[0].action is AttackActionOption && actionPair[1].action is AttackActionOption)
    }
}

