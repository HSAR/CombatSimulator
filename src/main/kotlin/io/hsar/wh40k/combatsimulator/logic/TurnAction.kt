package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION

sealed class TurnAction {
    abstract val actionCost: ActionCost

    data class ATTACK_MELEE(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class ATTACK_RANGED_SINGLE(override val damage: String) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val numberOfAttacks = 1
    }

    data class ATTACK_RANGED_SEMI_AUTO_BURST(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class ATTACK_RANGED_FULL_AUTO_BURST(override val damage: String, override val numberOfAttacks: Int) : DamageCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
    }

    data class RELOAD_WEAPON(override val actionCost: ActionCost) : TurnAction()
}

interface DamageCausingAction {
    val damage: String
    val numberOfAttacks: Int
}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}