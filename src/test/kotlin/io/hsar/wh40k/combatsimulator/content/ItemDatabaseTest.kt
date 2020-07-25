package io.hsar.wh40k.combatsimulator.content

import io.hsar.wh40k.combatsimulator.logic.actionoptions.SingleRangedAttack
// import io.hsar.wh40k.combatsimulator.logic.WeaponReload
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_TYPE
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.ItemType
import io.hsar.wh40k.combatsimulator.model.unit.WeaponType.PISTOL
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class ItemDatabaseTest {

    @Test
    fun `retrieves items as expected`() {
        // Arrange
        val objectUnderTest = ItemDatabase
        val expectedItem = EquipmentItem(
                itemRef = "wpn_pistol_autopistol",
                itemName = "Autopistol",
                itemType = ItemType.WEAPON,
                modifiesAttributes = mapOf(
                        WEAPON_TYPE to WeaponTypeValue(PISTOL),
                        ACTIONS to ActionValue(listOf(
                                SingleRangedAttack(damage = "1d10+3", range = 30)
                        ))
                )
        )
        val expectedWeaponRef = expectedItem.itemRef

        // Act
        val result = objectUnderTest.itemsByItemRef.getValue(expectedWeaponRef)

        // Assert
        assertThat(result.itemRef, equalTo(expectedItem.itemRef))
        assertThat(result.itemName, equalTo(expectedItem.itemName))
        assertThat(result.itemType, equalTo(expectedItem.itemType))
        assertThat(result.modifiesAttributes[WEAPON_TYPE], equalTo(expectedItem.modifiesAttributes[WEAPON_TYPE]))

        // Check both expected and actual have one action available
        assertThat(
                (result.modifiesAttributes[ACTIONS] as ActionValue).value.size,
                equalTo((expectedItem.modifiesAttributes[ACTIONS] as ActionValue).value.size))

        // and that both expected and actual have the action as the same class of ActionOption
        assertThat(
                (result.modifiesAttributes[ACTIONS] as ActionValue).value[0]::class,
                equalTo((expectedItem.modifiesAttributes[ACTIONS] as ActionValue).value[0]::class))


        /*
        Checks that the associated ActionOption has the same qualities. Fairly verbose, but the alternate is to
        implement operator == for all subclasses of ActionOption
         */
        assertThat(
               ((result.modifiesAttributes[ACTIONS] as ActionValue).value[0] as SingleRangedAttack).damage,
                equalTo(((expectedItem.modifiesAttributes[ACTIONS] as ActionValue).value[0] as SingleRangedAttack).damage))

        assertThat(
                ((result.modifiesAttributes[ACTIONS] as ActionValue).value[0] as SingleRangedAttack).range,
                equalTo(((expectedItem.modifiesAttributes[ACTIONS] as ActionValue).value[0] as SingleRangedAttack).range))
    }

}