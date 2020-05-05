package io.hsar.wh40k.combatsimulator.model.unit

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
        return Math.max(this.value.ordinal, other.value.ordinal)
                .let { largerOrdinal ->
                    WeaponType.values()[largerOrdinal]
                }
                .let { newValue ->
                    HighestValue(newValue)
                }
    }
}

/**
 * StackingValue adds attributes are to the end of the list.
 */
data class StackingValue<T>(val value: List<T>) : AttributeValue() {
    operator fun plus(other: StackingValue<T>): StackingValue<T> {
        return (this.value + other.value)
                .let { newValue ->
                    StackingValue(newValue)
                }
    }
}