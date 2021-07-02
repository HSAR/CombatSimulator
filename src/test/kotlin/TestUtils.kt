
import io.hsar.wh40k.combatsimulator.logic.actionoptions.ActionCost
import io.hsar.wh40k.combatsimulator.logic.actionoptions.ActionOption
import io.hsar.wh40k.combatsimulator.logic.actionoptions.FullAim
import io.hsar.wh40k.combatsimulator.logic.actionoptions.FullMove
import io.hsar.wh40k.combatsimulator.logic.actionoptions.HalfAim
import io.hsar.wh40k.combatsimulator.logic.actionoptions.HalfMove
import io.hsar.wh40k.combatsimulator.logic.actionoptions.SingleRangedAttack
import io.hsar.wh40k.combatsimulator.logic.actionoptions.TargetType
import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.Stats
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.any

object TestUtils {
    fun getGenericUnitInstance(): UnitInstance {
        val stats = mapOf(
                BaseStat.AGILITY to 32,
                BaseStat.WEAPON_SKILL to 35,
                BaseStat.BALLISTIC_SKILL to 40,
                BaseStat.MAX_HEALTH to 10)
        val derivedStats = emptyMap<DerivedStats, Int>()
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

    fun getMockedBasicActions(): Map<String, ActionOption> {
        val halfAimMock = mock(HalfAim::class.java)
        `when`(halfAimMock.actionCost).thenReturn(ActionCost.HALF_ACTION)
        `when`(halfAimMock.targetType).thenReturn(TargetType.SELF_TARGET)
        `when`(halfAimMock.isLegal(any(), any(), any()))
                .thenReturn(true)
        `when`(halfAimMock.expectedValue(any(), any(), any()))
                .thenReturn(5f)
        `when`(halfAimMock.apply(any(), any(), any()))
                .then {
                    (it.arguments[1] as UnitInstance).setEffect(Effect.AIMED_HALF)
                }


        val halfMoveMock = mock(HalfMove::class.java)
        `when`(halfMoveMock.actionCost).thenReturn(ActionCost.HALF_ACTION)
        `when`(halfMoveMock.targetType).thenReturn(TargetType.ANY_TARGET)
        `when`(halfMoveMock.isLegal(any(), any(), any()))
                .thenReturn(true)
        `when`(halfMoveMock.expectedValue(any(), any(), any()))
                .thenReturn(4f)
        `when`(halfMoveMock.apply(any(), any(), any()))
                .then {
                    // move map position so that we can subsequently test it is not moved in real world
                    (it.arguments[0] as World).unitPositions.set((it.arguments[1] as UnitInstance), MapPosition(99,99))
                }



        val singleRangedAttackMock = mock(SingleRangedAttack::class.java)
        `when`(singleRangedAttackMock.actionCost).thenReturn(ActionCost.HALF_ACTION)
        `when`(singleRangedAttackMock.targetType).thenReturn(TargetType.ADVERSARY_TARGET)
        `when`(singleRangedAttackMock.isLegal(any(), any(), any()))
                .thenReturn(true)
        `when`(singleRangedAttackMock.expectedValue(any(), any(), any()))
                .then {
                    if((it.arguments[1] as UnitInstance).getAimBonus() > 0) {
                        10f  // increase EV if aimed
                    } else {
                        6f
                    }
                }
        `when`(singleRangedAttackMock.apply(any(), any(), any()))
                .then {
                    (it.arguments[2] as UnitInstance).receiveDamage(1000);
                }
        return mapOf("HalfAim" to halfAimMock, "HalfMove" to halfMoveMock, "SingleRangedAttack" to singleRangedAttackMock)
    }

    fun getMockedIllegalAction(): ActionOption {
        val mockedIllegalHalfAim = mock(HalfAim::class.java)
        `when`(mockedIllegalHalfAim.actionCost).thenReturn(ActionCost.HALF_ACTION)
        `when`(mockedIllegalHalfAim.targetType).thenReturn(TargetType.SELF_TARGET)
        `when`(mockedIllegalHalfAim.isLegal(any(), any(), any()))
                .thenReturn(false)
        `when`(mockedIllegalHalfAim.expectedValue(any(), any(), any()))
                .thenReturn(100f)
        return mockedIllegalHalfAim
    }

    fun getMockedRawFullAction(): ActionOption {
        val mockedRawFullAction = mock(ActionOption::class.java)
        `when`(mockedRawFullAction.actionCost).thenReturn(ActionCost.FULL_ACTION)
        `when`(mockedRawFullAction.targetType).thenReturn(TargetType.ANY_TARGET)
        `when`(mockedRawFullAction.isLegal(any(), any(), any()))
                .thenReturn(true)
        return mockedRawFullAction

    }

    fun getMockedHighValueFullAction(): ActionOption {
        val mockedFullAction = getMockedRawFullAction()
        `when`(mockedFullAction.expectedValue(any(), any(), any()))
                .thenReturn(100f)
        return mockedFullAction
    }

    fun getMockedLowValueFullAction(): ActionOption {
        val mockedFullAction = getMockedRawFullAction()
        `when`(mockedFullAction.expectedValue(any(), any(), any()))
                .thenReturn(1f)
        return mockedFullAction
    }

    fun getMockedPositionDependentAction(): ActionOption {
        val mockedFullAction = getMockedRawFullAction()
        `when`(mockedFullAction.expectedValue(any(), any(), any()))
                .then {
                    if((it.arguments[0] as World).unitPositions.getValue((it.arguments[2] as UnitInstance))
                    == MapPosition(99,99)) {  // if target is at position 99,99
                        100f
                    } else {
                        1f
                    }
                }
        return mockedFullAction
    }

    fun getDefaultActions(): List<ActionOption> {
        return listOf(HalfAim(), FullAim(), HalfMove(), FullMove(), SingleRangedAttack("1d10+3", 30))
    }
}
