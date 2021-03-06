package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.cli.Loggable
import io.hsar.wh40k.combatsimulator.dice.RandomDice
import io.hsar.wh40k.combatsimulator.dice.RollResult
import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.logic.actionoptions.ActionOption
import io.hsar.wh40k.combatsimulator.logic.actionoptions.FullAim
import io.hsar.wh40k.combatsimulator.logic.actionoptions.HalfAim
import io.hsar.wh40k.combatsimulator.logic.actionoptions.WeaponReload
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.CURRENT_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_HEAD
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_TORSO
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.EFFECTS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_AMMUNITION
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_TYPE
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.BodyPart
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.ItemType.WEAPON
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import io.hsar.wh40k.combatsimulator.model.unit.WeaponType.MELEE
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue
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
        return when (val effects = currentAttributes[EFFECTS] ?: EffectValue(listOf())) {
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

    /*
    This creates a copy that has deep copies of the current attributes, allowing these to be modified eg
    when calculating optimum combat actions without affecting the original
     */
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
            val equipmentAttributes = equipment
                    .map { equipmentItem -> equipmentItem.modifiesAttributes }
                    .sum()

            val ammoMap = equipment
                    .firstOrNull { it.itemType == WEAPON } // #TODO: Handle this better than just "the first weapon to hand"
                    ?.modifiesAttributes
                    ?.let { weaponAttributes ->
                        if (weaponAttributes.getValue(WEAPON_TYPE) == WeaponTypeValue(MELEE)) {
                            // Melee weapons have no need for ammunition
                            null
                        } else {
                            // Ranged weapons take their clip size from the ammo given from a reload
                            mapOf(WEAPON_AMMUNITION to NumericValue((weaponAttributes.getValue(ACTIONS) as ActionValue)
                                    .value
                                    .filterIsInstance<WeaponReload>()
                                    .first().setsAmmunitionTo)
                            )
                        }
                    } ?: emptyMap()

            val toughnessMap = ((unit.stats.baseStats[BaseStat.TOUGHNESS] ?: 0) / 10)
                    .let { toughnessBonus ->
                        listOf(
                                DAMAGE_REDUCTION_HEAD,
                                DAMAGE_REDUCTION_ARM_L,
                                DAMAGE_REDUCTION_ARM_R,
                                DAMAGE_REDUCTION_TORSO,
                                DAMAGE_REDUCTION_LEG_L,
                                DAMAGE_REDUCTION_LEG_R
                        ).map { damageReductionLocation ->
                            damageReductionLocation to NumericValue(toughnessBonus)
                        }.toMap()
                    }

            val dynamicAttributes = mapOf(
                    CURRENT_HEALTH to NumericValue(unit.stats.baseStats.getValue(BaseStat.MAX_HEALTH))
            )
                    .mergeWithAddition(ammoMap)
                    .mergeWithAddition(toughnessMap)

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
