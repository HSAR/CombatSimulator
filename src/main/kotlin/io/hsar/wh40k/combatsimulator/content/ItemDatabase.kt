package io.hsar.wh40k.combatsimulator.content

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import java.io.File

/**
 * Deserialises item database and makes it available for general access.
 */
object ItemDatabase {

    private val ITEM_FILES = listOf(
            "data/armour.json",
            "data/meleeWeapons.json",
            "data/rangedWeapons.json"
    )

    val itemsByItemRef = ITEM_FILES
            .map { itemFilePath ->
                File(this::class.java.classLoader.getResource(itemFilePath)!!.file)
                        .readText()
                        .let { itemsString ->
                            jacksonObjectMapper().readValue<List<EquipmentItem>>(itemsString)
                        }
            }
            .flatten()
            .associateBy { equipmentItem ->
                equipmentItem.itemRef
            }
}