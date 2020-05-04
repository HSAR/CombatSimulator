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
    IN_MELEE_COMBAT
}

/**
 * Attribute values, whatever they are, must be capable of being added to one another in order to combine effects.
 */
sealed class AttributeValue

data class NumericValue(val value: Int): AttributeValue() {
    operator fun plus(other: NumericValue): NumericValue = NumericValue(this.value + other.value)
}