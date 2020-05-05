import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.*
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWorld {

    @Test
    fun testDistanceApart() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1,6), MapPosition(10, 4))
        assertEquals(9,world.distanceApart(world.friendlyForces[0], world.enemyForces[0]))
        // TODO if I was a better kotlin dev I would patch the internal call to DistanceToPosition
    }

    @Test
    fun testMapPositionDistancetoPosition() {
        val first: MapPosition = MapPosition(1, 6)
        val second: MapPosition = MapPosition(10, 4)
        assertEquals(9, first.distanceToPosition(second))
    }

    @Test()
    fun testMapPositionOperatorMinus() {
        val first: MapPosition = MapPosition(1, 6)
        val second: MapPosition = MapPosition(10, 4)
        assertEquals(9, first - second)
    }
}
