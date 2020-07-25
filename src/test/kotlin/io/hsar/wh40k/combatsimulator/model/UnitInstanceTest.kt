package io.hsar.wh40k.combatsimulator.model

import TestUtils

import io.hsar.wh40k.combatsimulator.model.unit.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitInstanceTest {

    @Test
    fun `getBaseStatSuccessChance returns the correct value`() {
        val unit = TestUtils.getGenericUnitInstance()  // Ws of 35
        assertThat("getBaseStatSuccess chance returns correct value",
                unit.getBaseStatSuccessChance(BaseStat.WEAPON_SKILL,10), equalTo(0.45f))
    }

    @Test
    fun `getBaseStatSuccessChance is bounded at 1`() {
        val unit = TestUtils.getGenericUnitInstance()
        assertThat("getBaseStatSuccess bounded at max 1",
                unit.getBaseStatSuccessChance(BaseStat.WEAPON_SKILL,100), equalTo(1f))
    }

    @Test
    fun `setEffect adds effect if not present`() {
        val unit = TestUtils.getGenericUnitInstance()
        unit.setEffect(Effect.AIMED_HALF)
        val isAimed = (unit.currentAttributes.getValue(Attribute.EFFECTS) as EffectValue).value.contains(Effect.AIMED_HALF)
        assertThat("HalfAim effect added to unit",
            isAimed, equalTo(true))
        assertThat("No other effects added",
                (unit.currentAttributes.getValue(Attribute.EFFECTS) as EffectValue).value.size, equalTo(1))
    }

    @Test
    fun `setEffect doesn't add effect if already present`() {
        val unit = TestUtils.getGenericUnitInstance()
        unit.setEffect(Effect.AIMED_HALF)
        unit.setEffect(Effect.AIMED_HALF)
        assertThat("Effect not added twice",
                (unit.currentAttributes.getValue(Attribute.EFFECTS) as EffectValue).value.size, equalTo(1))
    }

    @Test
    fun `getBaseStatBonus returns correct value`() {
        val unit = TestUtils.getGenericUnitInstance()
        assertThat("getBaseStatBonus gets correct value",
                unit.getBaseStatBonus(BaseStat.WEAPON_SKILL), equalTo(3))
    }

    @Test
    fun `getAimBonus returns correct value`() {
        val unit = TestUtils.getGenericUnitInstance()
        assertThat(unit.getAimBonus(), equalTo(0))
        unit.setEffect(Effect.AIMED_HALF)
        assertThat(unit.getAimBonus(), equalTo(10))
        unit.setEffect(Effect.AIMED_FULL)
        assertThat(unit.getAimBonus(), equalTo(20))
    }

    @Test
    fun `receiveDamage modifies health correctly`() {
        val unit = TestUtils.getGenericUnitInstance()
        unit.currentAttributes[Attribute.CURRENT_HEALTH] = NumericValue(10)
        unit.receiveDamage(6)
        val health = (unit.currentAttributes[Attribute.CURRENT_HEALTH] as NumericValue).value
        assertThat(health, equalTo(4))
    }

    @Test
    fun `createCopy deep copies attributes correctly but changes object refs`() {
        val unit = TestUtils.getGenericUnitInstance()
        val copy = unit.createCopy()
        unit.currentAttributes.forEach {
            assertThat(it.value === copy.currentAttributes[it.key], equalTo(false))
            when(it.value) {
                is NumericValue -> {
                    assertThat((it.value as NumericValue).value,
                            equalTo((copy.currentAttributes[it.key] as NumericValue).value))
                    assertThat(it.value === copy.currentAttributes[it.key], equalTo(false))
                }
                is EffectValue -> {
                    assertThat((it.value as EffectValue).value,
                            equalTo((copy.currentAttributes[it.key] as EffectValue).value))
                }
                is ActionValue -> {
                    assertThat((it.value as ActionValue).value,
                            equalTo((copy.currentAttributes[it.key] as ActionValue).value))
                }
                is WeaponTypeValue -> {
                    assertThat((it.value as WeaponTypeValue).value,
                            equalTo((copy.currentAttributes[it.key] as WeaponTypeValue).value))
                }
            }
        }
    }

    @Test
    fun `createInitialAttributeMap creates correct attributes`() {
        val unit = TestUtils.getGenericUnitInstance()
        val equipmentModifiers = mapOf(Attribute.DAMAGE_REDUCTION_HEAD to NumericValue(1))
        val equipment = EquipmentItem(
                "ref",
                "name",
                ItemType.WEAPON,
                equipmentModifiers
        )
        val attributeMap = UnitInstance.createInitialAttributeMap(unit.unit, listOf(equipment))
        assertThat((attributeMap[Attribute.CURRENT_HEALTH] as NumericValue).value, equalTo(10))
        //assertThat(UnitInstance.DEFAULT_ATTRIBUTES)
        assertThat(attributeMap.entries.containsAll(UnitInstance.DEFAULT_ATTRIBUTES.entries), equalTo(true))
        assertThat(attributeMap.entries.containsAll(equipmentModifiers.entries), equalTo(true))
    }








}