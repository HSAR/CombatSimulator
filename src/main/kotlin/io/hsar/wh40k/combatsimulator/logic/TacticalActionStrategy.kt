package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TargetedAction> {
        if (possibleActionOptions.isEmpty()) {
            throw IllegalArgumentException("Cannot decide turn actions with no possibilities!")
        }

        // Shit implementation - units will only ever aim and fire their max damage attack

        // for each Action option, map to list of legal targets

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
                //TODO test this thoroughly
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

    private fun getExpectedValue(world: World, thisUnit: UnitInstance, actionCombo: List<TargetedAction>): Float {
        // set up clone of world

        // TODO need to set up unit and target to use the tempworld versions

        return world.createCopy()
                .let { tempWorld ->
                    tempWorld.replaceUnitInstanceWithCopy(thisUnit)
                            .let { tempUser ->
                                actionCombo.map { targetedAction ->
                                    tempWorld.replaceUnitInstanceWithCopy(targetedAction.target)
                                            // TODO not a massive issue but if both actipns of a combo have the same target then any changes to the target won't carry over
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

