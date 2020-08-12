package io.hsar.wh40k.combatsimulator.model

import TestUtils
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class WorldTest {

    @Test
    fun testDistanceApart() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(10, 4))
        assertThat(world.distanceApart(world.friendlyForces[0], world.enemyForces[0]), equalTo(9))
    }

    @Test
    fun `moveTowards updates caller's position correctly`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(12,10), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]

        world.moveTowards(unit, enemy, 6)
        //unit ought to move diagonally towards enemy as far as it can
        assertThat(world.getPosition(unit), equalTo(MapPosition(6,4)))
    }

    @Test
    fun `moveTowards stops unit adjacent to target even if movement left`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(4,0), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]

        world.moveTowards(unit, enemy, 6)
        //unit ought to move diagonally towards enemy as far as it can
        assertThat(world.getPosition(unit), equalTo(MapPosition(1,0)))
    }

    @Test
    fun `moveTowards calculates moving through allies correctly`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(10,10), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]
        val ally1 = TestUtils.getGenericUnitInstance()
        val ally2 = TestUtils.getGenericUnitInstance()
        world.friendlyForces += ally1
        world.friendlyForces += ally2
        world.unitPositions[ally1] = MapPosition(9,9)
        world.unitPositions[ally2] = MapPosition(8,8)
        world.moveTowards(unit, enemy, 6)
        assertThat(world.getPosition(unit), equalTo(MapPosition(6,6)))  // lost 2 squares to moving through allies
    }

    @Test
    fun `moveTowards will not stop unit on top of ally`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(10,10), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]
        val ally1 = TestUtils.getGenericUnitInstance()
        world.friendlyForces += ally1
        world.unitPositions[ally1] = MapPosition(9,9)
        world.moveTowards(unit, enemy, 2)
        // will not stop on top of ally
        assertThat(world.getPosition(unit), equalTo(MapPosition(10,10)))
    }

    @Test
    fun `getAllies returns caller's allies`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(10,10), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val allies = world.getAllies(unit)
        assertThat(allies.containsAll(world.friendlyForces), equalTo(true))
    }

    @Test
    fun `getAdversaries returns caller's adverdaries`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(10,10), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        val adversaries = world.getAdversaries(unit)
        assertThat(adversaries.containsAll(world.enemyForces), equalTo(true))
    }

    @Test
    fun `getPosition retreives correct position`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,11), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        assertThat(world.getPosition(unit), equalTo(MapPosition(2,11)))
    }

    @Test
    fun `setPosition sets unit's position correctly`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,11), MapPosition(0,0))
        val unit = world.friendlyForces[0]
        world.setPosition(unit, MapPosition(13,14))
        assertThat(world.getPosition(unit), equalTo(MapPosition(13,14)))
    }

    @Test
    fun testMapPositionDistancetoPosition() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first.distanceToPosition(second), equalTo(9))
    }

    @Test
    fun testMapPositionOperatorMinus() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first - second, equalTo(9))
    }

    @Test
    fun `findDead returns exclusively dead units`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
        world.enemyForces[0].currentAttributes[Attribute.CURRENT_HEALTH] = NumericValue(-1)
        val deadUnits = world.findDead()
        assertThat(deadUnits.size, equalTo(1))
        assertThat(deadUnits[0], equalTo(world.enemyForces[0]))
    }

    @Test
    fun `removeUnits exclusively removes dead units`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 6), MapPosition(5, 4))
        world.friendlyForces[0].currentAttributes[Attribute.CURRENT_HEALTH] = NumericValue(-1)
        world.removeUnits(world.findDead())
        assertThat(world.enemyForces.size, equalTo(1))
        assertThat(world.friendlyForces.size, equalTo(0))
    }

    @Test
    fun `isInMeleeRange returns true when units adjacent`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(3,4))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]
        assertThat(world.isInMeleeRange(unit, enemy), equalTo(true))
    }

    @Test
    fun `isInMeleeRange returns false when units not adjacent`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(3,5))
        val unit = world.friendlyForces[0]
        val enemy = world.enemyForces[0]
        assertThat(world.isInMeleeRange(unit, enemy), equalTo(false))
    }

    @Test
    fun `createCopy deep copies unit positions correctly`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(3,4))
        val tempWorld = world.createCopy()

        assertThat("copied unitPositions same length as original",
        world.unitPositions.size, equalTo(tempWorld.unitPositions.size))

        assertThat("friendly forces list preserved across copy",
        tempWorld.friendlyForces, equalTo(world.friendlyForces))

        assertThat("Enemy forces list preserved across copy",
        tempWorld.enemyForces, equalTo(world.enemyForces))

        for(index in 0..world.unitPositions.size - 1) {
            val originalUnitPosition = world.unitPositions.toList()[index]
            val copiedUnitPosition = tempWorld.unitPositions.toList()[index]
            assertThat("Object ref for UnitInstance shared between entry and copy",
            originalUnitPosition.first === copiedUnitPosition.first, equalTo(true))
            assertThat("Object ref for MapPosition not shared between unitPosition entry and copy",
                    originalUnitPosition.second === copiedUnitPosition.second, equalTo(false))
            assertThat("Values of MapPosition entry same between entry and copy",
            originalUnitPosition.second == copiedUnitPosition.second, equalTo(true))
        }

    }

    @Test
    fun `replaceUnitInstanceWithCopy updates world correctly`() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(2,3), MapPosition(3,4))
        val unit = world.friendlyForces[0]
        val tempUnit = world.replaceUnitInstanceWithCopy(unit)

        assertThat("Copied unit has different ref to the original",
        tempUnit != unit, equalTo(true))

        assertThat("Unit has been replaced in world by copy",
        world.friendlyForces[0] == tempUnit, equalTo(true))

        assertThat("Unit has been removed from world after copy was added",
        world.friendlyForces.size, equalTo(1))

        assertThat("Temp unit's position is same as the unit it replaced",
        world.getPosition(tempUnit), equalTo(MapPosition(2,3)))
    }
}
