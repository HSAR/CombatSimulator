package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.cli.Loggable
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.logic.FullAim
import io.hsar.wh40k.combatsimulator.logic.HalfAim
import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.CURRENT_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.EFFECTS
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.RollResult
import io.hsar.wh40k.combatsimulator.utils.mergeWithAddition
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
        val startingAttributes: Map<Attribute, AttributeValue> = createInitialAttributeMap(unit, equipment),
        val tacticalActionStrategy: TacticalActionStrategy = TacticalActionStrategy,
        val currentAttributes: MutableMap<Attribute, AttributeValue> = startingAttributes.toMutableMap()
) {
    val availableActionOptions: List<ActionOption>
        get() = (startingAttributes.getValue(ACTIONS) as? ActionValue
                ?: throw IllegalStateException("Unit ${name} ACTION attribute should have actions but instead was: ${startingAttributes.getValue(ACTIONS)}"))
                .value

    fun rollBaseStat(stat: BaseStat, bonus: Int): RollResult {
        return RandomDice.roll(unit.stats.baseStats.getValue(stat) + bonus)
    }

    fun getBaseStatSuccessChance(stat: BaseStat, bonus: Int): Float {
        return minOf(unit.stats.baseStats.getValue(stat) + bonus, 100) / 100f
    }

    fun setEffect(effect: Effect) {
        when (val effects = currentAttributes[EFFECTS] ?: EffectValue(listOf())) {
            is EffectValue -> {
                if (effect !in effects.value) {
                    currentAttributes[EFFECTS] = effects + EffectValue(listOf(effect))
                }
            }
            else -> return
        }
    }

    fun getBaseStatBonus(stat: BaseStat): Int {
        return unit.stats.baseStats.getValue(stat) / 10  // integer division
    }

    fun getAimBonus(): Int {
        return when (val effects = currentAttributes[Attribute.EFFECTS] ?: EffectValue(listOf())) {
            is EffectValue -> {
                when {
                    Effect.AIMED_FULL in effects.value -> 20
                    Effect.AIMED_HALF in effects.value -> 10
                    else -> 0
                }
            }
            else -> throw IllegalStateException("Effects must be of type EffectValue")
        }
    }

    fun receiveDamage(damage: Int) {
        when (val health = currentAttributes.getValue(CURRENT_HEALTH)) {
            is NumericValue -> currentAttributes[CURRENT_HEALTH] = NumericValue(health.value - damage)
            else -> throw IllegalStateException("Current health ought to be a NumericValue")
        }
    }

    fun createCopy(): UnitInstance = UnitInstance(
            name = name,
            description = description,
            unit = unit,
            equipment = equipment,
            startingAttributes = startingAttributes,
            tacticalActionStrategy = tacticalActionStrategy,
            currentAttributes = currentAttributes
                    .mapValues { attribute ->
                        attribute.value.copy()
                    }
                    .toMutableMap()
    )

    companion object : Loggable {
        val DEFAULT_ACTIONS = ActionValue(listOf(
                HalfAim(),
                FullAim()
        ))
        val DEFAULT_ATTRIBUTES = mapOf(ACTIONS to DEFAULT_ACTIONS, EFFECTS to EffectValue(emptyList()))

        fun createInitialAttributeMap(unit: Unit, equipment: List<EquipmentItem>): Map<Attribute, AttributeValue> {
            val equipmentAttributes = equipment.map { it.modifiesAttributes }.sum()

            /* val ammoMap = equipment.firstOrNull() { it.itemType == WEAPON } // #TODO: Handle this better than just "the first weapon to hand"
                     ?.modifiesAttributes
                     ?.let { weaponAttributes ->
                         if (weaponAttributes.getValue(WEAPON_TYPE) == WeaponTypeValue(MELEE)) {
                             // Melee weapons have no need for ammunition
                             null
                         } else {
                             // Ranged weapons take their clip size from the ammo given from a reload
                             mapOf(Attribute.WEAPON_AMMUNITION to NumericValue((weaponAttributes.getValue(ACTIONS) as ActionValue)
                                     .value
                                     .filterIsInstance<WeaponReload>()
                                     .first().setsAmmunitionTo)
                             )
                         }
                     } ?: emptyMap()*/

            val dynamicAttributes = mapOf(
                    CURRENT_HEALTH to NumericValue(unit.stats.baseStats.getValue(BaseStat.MAX_HEALTH))
            ) //+ ammoMap

            return DEFAULT_ATTRIBUTES
                    .mergeWithAddition(equipmentAttributes)
                    .mergeWithAddition(dynamicAttributes)
        }

        fun randomBodyPart(): BodyPart {

            return RandomDice.roll("1d100")
                    .let { diceRoll ->
                        when (diceRoll) {
                            in 0..10 -> BodyPart.HEAD
                            in 11..20 -> BodyPart.RIGHT_ARM
                            in 21..30 -> BodyPart.LEFT_ARM
                            in 31..70 -> BodyPart.BODY
                            in 71..85 -> BodyPart.RIGHT_LEG
                            in 86..100 -> BodyPart.LEFT_LEG
                            else -> throw RuntimeException("d100 roll outside of 1-100")
                        }
                    }

        }
    }
}
