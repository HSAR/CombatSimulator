package io.hsar.wh40k.combatsimulator.logic.actionoptions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "actionType",
        visible = true)
@JsonIgnoreProperties(value = ["actionType"])
@JsonSubTypes( // NB: No elegant way to do this without specifying class names, see https://github.com/FasterXML/jackson-databind/issues/374
        JsonSubTypes.Type(value = StandardMeleeAttack::class, name = "StandardMeleeAttack"),
        JsonSubTypes.Type(value = SingleRangedAttack::class, name = "SingleRangedAttack"),
        JsonSubTypes.Type(value = SemiAutoBurstRangedAttack::class, name = "SemiAutoBurstRangedAttack"),
        JsonSubTypes.Type(value = FullAutoBurstRangedAttack::class, name = "FullAutoBurstRangedAttack"),
        JsonSubTypes.Type(value = WeaponReload::class, name = "WeaponReload")
)
abstract class ActionOption {
    abstract val actionCost: ActionCost
    abstract val targetType: TargetType
    abstract fun isLegal(world: World, user: UnitInstance, target: UnitInstance): Boolean
    abstract fun expectedValue(world: World, user: UnitInstance, target: UnitInstance): Float
    abstract fun apply(world: World, user: UnitInstance, target: UnitInstance): Unit

    companion object {
        // put all the non-dynamic expected action values in one place for easy balancing
        val HALF_AIMING_INHERENT_VALUE = 0.5f
        val FULL_AIMING_INHERENT_VALUE = 0.8f
        val MOVING_INHERENT_VALUE = 1.0f

    }
}

class TargetedAction(val action: ActionOption, val target: UnitInstance)

//TODO maybe nest these enums inside ActionOption to avoid namespace pollution
enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}

enum class TargetType {
    SELF_TARGET,
    ADVERSARY_TARGET,
    ALLY_TARGET,
    ANY_TARGET
}
