import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.*
import org.mockito.Mockito

object TestUtils {
    fun getGenericUnitInstance(): UnitInstance {
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
                equipment = unit.initialEquipment
        )
    }

    fun getGenericTwoUnitWorld(firstPosition: MapPosition, secondPosition: MapPosition): World {
        val unitInstance = getGenericUnitInstance()
        val otherUnitInstance = getGenericUnitInstance()
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
