package io.hsar.wh40k.combatsimulator.model.unit

enum class ItemType {
    ARMOUR,
    WEAPON,
    CYBERNETIC
}

/**
 * Items should store their state (jammed, misfire, ammo status, braced etc.) on the attribute map.
 */
data class EquipmentItem(
        val itemRef: String,
        val itemName: String,
        val itemType: ItemType,
        val modifiesAttributes: Map<Attribute, AttributeValue>
)