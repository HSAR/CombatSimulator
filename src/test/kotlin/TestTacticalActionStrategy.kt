import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTacticalActionStrategy {

    @Test
    fun testCanHalfMoveToEnemyInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 4), MapPosition(5, 8))

        // This will break if someone changes the utils code for our generic unitinstance
        assertEquals(32, world.friendlyForces[0].unit.stats.baseStats.getValue(BaseStat.AGILITY))

        assertEquals(true, TacticalActionStrategy.canHalfMoveToEnemy(world, world.friendlyForces[0], world.enemyForces[0]))
    }

    @Test
    fun testCanHalfMoveToEnemyNotInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 4), MapPosition(6, 8))

        // This will break if someone changes the utils code for our generic unitinstance
        assertEquals(32, world.friendlyForces[0].unit.stats.baseStats.getValue(BaseStat.AGILITY))

        assertEquals(false, TacticalActionStrategy.canHalfMoveToEnemy(world, world.friendlyForces[0], world.enemyForces[0]))
    }

    @Test
    fun testCanChargeToEnemyInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 4), MapPosition(11, 4))

        // This will break if someone changes the utils code for our generic unitinstance
        assertEquals(32, world.friendlyForces[0].unit.stats.baseStats.getValue(BaseStat.AGILITY))

        assertEquals(true, TacticalActionStrategy.canChargeToEnemy(world, world.friendlyForces[0], world.enemyForces[0]))
    }

    @Test
    fun testCanChargeToEnemyNotInRange() {
        val world = TestUtils.getGenericTwoUnitWorld(MapPosition(1, 4), MapPosition(12, 4))

        // This will break if someone changes the utils code for our generic unitinstance
        assertEquals(32, world.friendlyForces[0].unit.stats.baseStats.getValue(BaseStat.AGILITY))

        assertEquals(false, TacticalActionStrategy.canChargeToEnemy(world, world.friendlyForces[0], world.enemyForces[0]))
    }
}
