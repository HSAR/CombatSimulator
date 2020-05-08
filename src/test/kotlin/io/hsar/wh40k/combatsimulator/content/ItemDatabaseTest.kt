package io.hsar.wh40k.combatsimulator.content

import io.hsar.wh40k.combatsimulator.logic.ActionCost.TWO_FULL_ACTIONS
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_TYPE
import io.hsar.wh40k.combatsimulator.model.unit.Effect.PRIMITIVE
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue
import io.hsar.wh40k.combatsimulator.model.unit.ItemType
import io.hsar.wh40k.combatsimulator.model.unit.WeaponType.PISTOL
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class ItemDatabaseTest {

    @Test
    fun `retrieves items as expected`() {
        // Arrange
        val objectUnderTest = ItemDatabase
        val expectedItem = EquipmentItem(
                itemRef = "wpn_pistol_stubRevolver",
                itemName = "Stub Revolver",
                itemType = ItemType.WEAPON,
                modifiesAttributes = mapOf(
                        WEAPON_TYPE to WeaponTypeValue(PISTOL),
                        ACTIONS to ActionValue(listOf(
                                TurnAction.SingleRangedAttack(damage = "1d10+3"),
                                TurnAction.MeleeAttack(damage = "1d10-1", appliesEffects = listOf(PRIMITIVE)),
                                TurnAction.WeaponReload(actionCost = TWO_FULL_ACTIONS)
                        ))
                )
        )
        val expectedWeaponRef = expectedItem.itemRef

        // Act
        val result = objectUnderTest.itemsByItemRef.getValue(expectedWeaponRef)

        // Assert
        assertThat(result, equalTo(expectedItem))
    }
}