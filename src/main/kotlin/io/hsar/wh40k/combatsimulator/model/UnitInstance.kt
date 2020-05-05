package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentInfo
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentType
import io.hsar.wh40k.combatsimulator.model.unit.Unit

/**
 * A single combatant.
 * This class may contain dynamic information that changes as combat progresses.
 */
class UnitInstance(
        val name: String,
        val description: String,
        val unit: Unit,
        val equipment: Map<EquipmentType, EquipmentInfo>,
        val attributes: Map<Attribute, AttributeValue>, // #TODO: Figure out whether this is good long-term solution
        val tacticalActionStrategy: TacticalActionStrategy = TacticalActionStrategy
) {

    val availableActions: List<TurnAction>
        get() = (attributes.getValue(Attribute.ACTIONS) as? ActionValue
                ?: throw IllegalStateException("Unit ${name} ACTION attribute should have actions but instead was: ${attributes.getValue(Attribute.ACTIONS)}"))
                .value

    fun executeTurnAction(turnAction: TurnAction) {
        TODO("Not yet implemented")
    }
}