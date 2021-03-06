package io.hsar.wh40k.combatsimulator.model.unit

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.hsar.wh40k.combatsimulator.content.AttributeValueDeserialiser
import io.hsar.wh40k.combatsimulator.logic.actionoptions.ActionOption

/**
 * Attributes are dynamic information that can change turn by turn.
 */
enum class Attribute {
    /**
     * This has no effect and is used, as in code, to put human-readable machine-ignored text into the file.
     */
    COMMENT,

    CURRENT_HEALTH,
    POSITION,
    DAMAGE_REDUCTION_HEAD,
    DAMAGE_REDUCTION_ARM_L,
    DAMAGE_REDUCTION_ARM_R,
    DAMAGE_REDUCTION_TORSO,
    DAMAGE_REDUCTION_LEG_L,
    DAMAGE_REDUCTION_LEG_R,
    DAMAGE_OUTPUT,

    // DAMAGE_TYPES, // #TODO: Implement damage types later
    WEAPON_TYPE,
    WEAPON_AMMUNITION,
    ACTIONS,
    EFFECTS,
    IN_MELEE_COMBAT
}

enum class WeaponType {
    MELEE,
    PISTOL,
    BASIC,
    HEAVY
    // #TODO Can optimise this: https://stackoverflow.com/a/19277247/2756877
}

/**
 * Attribute values, whatever they are, must be capable of being added to one another in order to combine effects.
 */
@JsonDeserialize(using = AttributeValueDeserialiser::class)
sealed class AttributeValue {
    abstract fun copy(): AttributeValue
}

@JsonDeserialize
data class StringValue(val value: String) : AttributeValue() {
    override fun copy(): AttributeValue = StringValue(this.value)
}

@JsonDeserialize
data class NumericValue(val value: Int) : AttributeValue() {
    operator fun plus(other: NumericValue): NumericValue {
        return NumericValue(this.value + other.value)
    }

    override fun copy(): NumericValue {
        return NumericValue(value)
    }
}

@JsonDeserialize
data class WeaponTypeValue(val value: WeaponType) : AttributeValue() {
    operator fun plus(other: WeaponTypeValue): WeaponTypeValue {
        return this.value.ordinal.coerceAtLeast(other.value.ordinal)
                .let { largerOrdinal ->
                    WeaponType.values()[largerOrdinal]
                }
                .let { newValue ->
                    WeaponTypeValue(newValue)
                }
    }

    override fun copy(): AttributeValue {
        return WeaponTypeValue(value)
    }
}

/**
 * ActionValue adds items to the end of a list.
 */
@JsonDeserialize
data class ActionValue(val value: List<ActionOption>) : AttributeValue() {
    operator fun plus(other: ActionValue): ActionValue {
        return (this.value + other.value)
                .let { newValue ->
                    ActionValue(newValue)
                }
    }

    override fun copy(): AttributeValue {
        return ActionValue(value)
    }
}

/**
 * EffectValue adds items to the end of the list.
 */
@JsonDeserialize
data class EffectValue(val value: List<Effect>) : AttributeValue() {
    operator fun plus(other: EffectValue): EffectValue {
        return EffectValue(this.value + other.value)
    }
    override fun copy(): AttributeValue {
        return EffectValue(value.toList()) // explicit toList call to copy the list items by value
    }
}