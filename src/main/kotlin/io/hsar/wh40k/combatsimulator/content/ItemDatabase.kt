package io.hsar.wh40k.combatsimulator.content

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import java.io.File

/**
 * Deserialises item database and makes it available for general access.
 */
object ItemDatabase {
    val itemsByItemRef = File(this::class.java.classLoader.getResource("data/items.json")!!.file)
            .readText()
            .let { itemsString ->
                jacksonObjectMapper().readValue<List<EquipmentItem>>(itemsString)
            }
            .associateBy { equipmentItem ->
                equipmentItem.itemRef
            }
}