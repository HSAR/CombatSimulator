package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.*
import io.hsar.wh40k.combatsimulator.model.unit.*
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.CURRENT_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.RollResult
import io.hsar.wh40k.combatsimulator.utils.sum

/**
 * A single combatant.
 * This class may contain dynamic information that changes as combat progresses.
 */
class UnitInstance(
        val name: String,
        val description: String,
        val unit: Unit,
        val equipment: List<EquipmentItem>,
        val attackExecutor: AttackExecutor = AttackExecutor(), // open for test
        val startingAttributes: Map<Attribute, AttributeValue> =  // #TODO: Figure out whether this is good long-term solution
                DEFAULT_ATTRIBUTES + equipment.map { it.modifiesAttributes }.sum(),
        val tacticalActionStrategy: TacticalActionStrategy = TacticalActionStrategy,
        val currentAttributes: MutableMap<Attribute, AttributeValue> = startingAttributes.toMutableMap()

) {

    val availableActionOptions: List<ActionOption>
        get() = (startingAttributes.getValue(ACTIONS) as? ActionValue
                ?: throw IllegalStateException("Unit ${name} ACTION attribute should have actions but instead was: ${startingAttributes.getValue(ACTIONS)}"))
                .value

    fun executeActions(actionsToExecute: List<TurnAction>) {
        actionsToExecute
                .map { actionToExecute ->
                    when (actionToExecute) {
                        is TargetedAction -> {
                            when (actionToExecute.action) {
                                is DamageCausingAction -> {
                                    attackExecutor.rollHits(
                                            attacker = this,
                                            target = actionToExecute.target,
                                            action = actionToExecute.action as DamageCausingAction
                                            // forced to hard cast to avoid compiler error
                                    ).let { numberOfHits ->
                                        repeat(numberOfHits) {
                                            actionToExecute.target.receiveDamage(
                                                    attackExecutor.calcDamage(
                                                            attacker = this,
                                                            target = actionToExecute.target,
                                                            action = actionToExecute.action as DamageCausingAction))
                                        }
                                    }
                                }
                                else -> TODO()
                            }
                        }
                        else -> {  // deal with non-targeted actions, eg aiming
                            when(actionToExecute.action) {
                                is EffectCausingAction ->  {
                                    when (val existingEffects = this.currentAttributes[Attribute.EFFECTS]) {
                                        is EffectValue -> {
                                            this.currentAttributes[Attribute.EFFECTS] = EffectValue(listOf(existingEffects.value,
                                                    (actionToExecute.action as EffectCausingAction).appliesEffects).flatten())
                                        }
                                        else -> throw IllegalStateException("Effects value should be of type EffectValue)")
                                    }

                                }
                                else -> TODO()
                            }
                        }
                    }
                }
    }

    fun rollBaseStat(stat: BaseStat, bonus: Int): RollResult {
        return RandomDice.roll(unit.stats.baseStats.getValue(stat) + bonus)
    }

    private fun receiveDamage(damage: Int) {
        when(val health = currentAttributes.getValue(CURRENT_HEALTH)) {
            is NumericValue -> currentAttributes[CURRENT_HEALTH] = NumericValue(health.value - damage)
            else -> throw IllegalStateException("Current health ought to be a NumericValue")
        }
    }

    companion object {
        val DEFAULT_ACTIONS = ActionValue(listOf(
                HalfAim,
                FullAim
        ))
        val DEFAULT_ATTRIBUTES = mapOf(ACTIONS to DEFAULT_ACTIONS, Attribute.EFFECTS to EffectValue(listOf<Effect>()))
    }
}
