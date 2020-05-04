package io.hsar.wh40k.combatsimulator.model.unit

enum class EquipmentType {
    ARMOUR,
    WEAPON,
    CYBERNETIC
}

sealed class EquipmentInfo {
    abstract val modifiesAttributes: Map<Attribute, AttributeValue>
}

/**
 * Represents equipment that has no state, only whether it is equipped or not (gear, armour etc.)
 */
data class SimpleEquipmentInfo(
        val itemRef: String,
        override val modifiesAttributes: Map<Attribute, AttributeValue>
) : EquipmentInfo()

/**
 * Represents weapons that have their own turn by turn state (ammo, fire selector, jammed, misfire etc.)
 */
data class WeaponInfo(
        val itemRef: String,
        override val modifiesAttributes: Map<Attribute, AttributeValue>
        // #TODO: How does this deserialize from file?
) : EquipmentInfo()