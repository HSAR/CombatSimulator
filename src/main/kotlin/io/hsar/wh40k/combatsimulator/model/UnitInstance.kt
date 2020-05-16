package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.logic.FullAim
import io.hsar.wh40k.combatsimulator.logic.HalfAim
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
        val startingAttributes: Map<Attribute, AttributeValue> =  // #TODO: Figure out whether this is good long-term solution
                DEFAULT_ATTRIBUTES + equipment.map { it.modifiesAttributes }.sum(),
        val tacticalActionStrategy: TacticalActionStrategy = TacticalActionStrategy,
        val currentAttributes: MutableMap<Attribute, AttributeValue> = startingAttributes.toMutableMap()
) {

    val availableActionOptions: List<ActionOption>
        get() = (startingAttributes.getValue(ACTIONS) as? ActionValue
                ?: throw IllegalStateException("Unit ${name} ACTION attribute should have actions but instead was: ${startingAttributes.getValue(ACTIONS)}"))
                .value

    fun executeTurnAction(actionOption: ActionOption) {
        TODO("Not yet implemented")
    }

    fun rollBaseStat(stat: BaseStat, bonus: Int): RollResult {
        return RandomDice.roll(unit.stats.baseStats.getValue(stat) + bonus)
    }

    fun receiveDamage(damage: Int) {
        //TODO
        when(val health = currentAttributes.getValue(CURRENT_HEALTH)) {
            is NumericValue -> currentAttributes[CURRENT_HEALTH] = NumericValue(health.value - damage)
            else -> throw RuntimeException("Current health ought to be a NumericValue")
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

enum class  BodyPart {
    HEAD,
    BODY,
    RIGHT_ARM,
    LEFT_ARM,
    RIGHT_LEG,
    LEFT_LEG
}