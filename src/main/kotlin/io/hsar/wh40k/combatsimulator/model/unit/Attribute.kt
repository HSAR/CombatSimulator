package io.hsar.wh40k.combatsimulator.model.unit

import io.hsar.wh40k.combatsimulator.logic.TurnAction

/**
 * Attributes are dynamic information that can change turn by turn.
 */
enum class Attribute {
    CURRENT_HEALTH,
    POSITION,
    DAMAGE_REDUCTION_HEAD,
    DAMAGE_REDUCTION_ARM_L,
    DAMAGE_REDUCTION_ARM_R,
    DAMAGE_REDUCTION_TORSO,
    DAMAGE_REDUCTION_LEG_L,
    DAMAGE_REDUCTION_LEG_R,
    DAMAGE_OUTPUT,
    DAMAGE_TYPES,
    WEAPON_TYPE,
    ACTIONS,
    EFFECTS,
    IN_MELEE_COMBAT
}

/**
 * Attribute values, whatever they are, must be capable of being added to one another in order to combine effects.
 */
sealed class AttributeValue

data class NumericValue(val value: Int) : AttributeValue() {
    operator fun plus(other: NumericValue): NumericValue {
        return (this.value + other.value)
                .let { newValue ->
                    NumericValue(newValue)
                }
    }
}

enum class WeaponType {
    MELEE,
    PISTOL,
    BASIC,
    HEAVY
    // #TODO Can optimise this: https://stackoverflow.com/a/19277247/2756877
}

data class HighestValue(val value: WeaponType) : AttributeValue() {
    operator fun plus(other: HighestValue): HighestValue {
        return this.value.ordinal.coerceAtLeast(other.value.ordinal)
                .let { largerOrdinal ->
                    WeaponType.values()[largerOrdinal]
                }
                .let { newValue ->
                    HighestValue(newValue)
                }
    }
}

/**
 * ActionValue adds items to the end of a list.
 */
data class ActionValue(val value: List<TurnAction>) : AttributeValue() {
    operator fun plus(other: ActionValue): ActionValue {
        return (this.value + other.value)
                .let { newValue ->
                    ActionValue(newValue)
                }
    }
}

/**
 * EffectValue adds items to the end of the list.
 */
data class EffectValue(val value: List<Effects>) : AttributeValue() {
    operator fun plus(other: EffectValue): EffectValue {
        return (this.value + other.value)
                .let { newValue ->
                    EffectValue(newValue)
                }
    }
}