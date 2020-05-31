package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.cli.Loggable
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.EffectCausingAction
import io.hsar.wh40k.combatsimulator.logic.FullAim
import io.hsar.wh40k.combatsimulator.logic.HalfAim
import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.logic.TargetedAction
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.CURRENT_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
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
                DEFAULT_ATTRIBUTES + equipment.map { it.modifiesAttributes }.sum()
                        + (CURRENT_HEALTH to NumericValue(unit.stats.baseStats.getValue(BaseStat.MAX_HEALTH))),
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
                    print("        $name does ${actionToExecute.action}")
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
                                        when {
                                            (numberOfHits < 1) -> println(" but misses.")
                                            (numberOfHits == 1) -> print(", hitting ")
                                            else -> print(", making $numberOfHits hits")
                                        }
                                        repeat(numberOfHits) {
                                            attackExecutor
                                                    .calcDamage(
                                                            attacker = this,
                                                            target = actionToExecute.target,
                                                            action = actionToExecute.action as DamageCausingAction)
                                                    .let { damage ->
                                                        println("${actionToExecute.target.name} for $damage damage.")
                                                        actionToExecute.target.receiveDamage(damage)
                                                    }
                                        }
                                    }
                                }
                                else -> TODO()
                            }
                        }
                        else -> {  // deal with non-targeted actions, eg aiming
                            when (actionToExecute.action) {
                                is EffectCausingAction -> {
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
        when (val health = currentAttributes.getValue(CURRENT_HEALTH)) {
            is NumericValue -> currentAttributes[CURRENT_HEALTH] = NumericValue(health.value - damage)
            else -> throw IllegalStateException("Current health ought to be a NumericValue")
        }
    }

    companion object : Loggable {
        val log = logger()
        val DEFAULT_ACTIONS = ActionValue(listOf(
                HalfAim,
                FullAim
        ))
        val DEFAULT_ATTRIBUTES = mapOf(ACTIONS to DEFAULT_ACTIONS, Attribute.EFFECTS to EffectValue(listOf<Effect>()))
    }
}
