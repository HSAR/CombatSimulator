import io.hsar.wh40k.combatsimulator.model.HalfMoveAction
import io.hsar.wh40k.combatsimulator.model.MapPosition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class TestWorld {

    @Test
    fun testCanMoveToUnitInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1,6), MapPosition(5, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMoveAction), equalTo(true))
    }

    @Test
    fun testCanMoveToUnitOutOfRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1,6), MapPosition(6, 4))
        assertThat(world.canMoveToUnit(world.friendlyForces[0], world.enemyForces[0], HalfMoveAction), equalTo(false))
    }

    @Test
    fun testDistanceApart() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1,6), MapPosition(10, 4))
        assertThat(world.distanceApart(world.friendlyForces[0], world.enemyForces[0]), equalTo(9))
    }

    @Test
    fun testMapPositionDistancetoPosition() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first.distanceToPosition(second), equalTo(9))
    }

    @Test()
    fun testMapPositionOperatorMinus() {
        val first = MapPosition(1, 6)
        val second = MapPosition(10, 4)
        assertThat(first - second, equalTo(9))
    }
}
