import io.hsar.wh40k.combatsimulator.model.AttackExecutor
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.Stats
import io.hsar.wh40k.combatsimulator.model.unit.Unit

object TestUtils {
    fun getGenericUnitInstance(attackExecutor: AttackExecutor = AttackExecutor()): UnitInstance {
        val stats = mapOf<BaseStat, Int>(
                BaseStat.AGILITY to 32,
                BaseStat.BALLISTIC_SKILL to 40)
        val derivedStats = mapOf<DerivedStats, Int>()
        val unit = Unit(
                unitRef = "bob",
                description = "bob the guy",
                stats = Stats(stats, derivedStats),
                initialEquipment = emptyList()
        )
        return UnitInstance(
                name = "bob",
                description = "a guy",
                unit = unit,
                equipment = unit.initialEquipment,
                attackExecutor = attackExecutor
        )
    }

    fun getGenericTwoUnitWorld(firstPosition: MapPosition, secondPosition: MapPosition, attackExecutor: AttackExecutor = AttackExecutor()): World {
        val unitInstance = getGenericUnitInstance(attackExecutor)
        val otherUnitInstance = getGenericUnitInstance(attackExecutor)
        unitInstance.currentAttributes[Attribute.CURRENT_HEALTH] = NumericValue(15)
        otherUnitInstance.currentAttributes[Attribute.CURRENT_HEALTH] = NumericValue(10)
        val unitPositions = mutableMapOf(unitInstance to firstPosition, otherUnitInstance to secondPosition)
        return World(
                mutableListOf(unitInstance),
                mutableListOf(otherUnitInstance),
                unitPositions
        )
    }
}
