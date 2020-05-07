import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.*

object TestUtils {
    fun getGenericUnitInstance(): UnitInstance {
        val stats = mapOf<BaseStat, Short>(BaseStat.AGILITY to 32)
        val derivedStats = mapOf<DerivedStats, Short>()
        val unit = Unit("bob", "bob the guy", Stats(stats, derivedStats))
        val equipment = mapOf<EquipmentType, EquipmentInfo>()
        val attributes = mapOf<Attribute, AttributeValue>()
        return UnitInstance("bob", "a guy", unit, equipment, attributes)
    }

    fun getGenericTwoUnitWorld(firstPosition: MapPosition, secondPosition: MapPosition): World {
        val unitInstance = getGenericUnitInstance()
        val otherUnitInstance = getGenericUnitInstance()
        val unitPositions = mutableMapOf(unitInstance to firstPosition, otherUnitInstance to secondPosition)
        return World(
                mutableListOf(unitInstance),
                mutableListOf(otherUnitInstance),
                unitPositions
        )
    }
}