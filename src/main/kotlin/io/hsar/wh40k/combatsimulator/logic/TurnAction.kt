package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.logic.ActionCost.HALF_ACTION
import io.hsar.wh40k.combatsimulator.model.unit.Bonus
import io.hsar.wh40k.combatsimulator.model.unit.Bonus.*

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

    object AIM_HALF: BonusCausingAction, TurnAction() {
        override val actionCost =  HALF_ACTION
        override val appliesBonus = BONUS_AIM_HALF
    }

    object AIM_FULL: BonusCausingAction, TurnAction() {
        override val actionCost: ActionCost = HALF_ACTION
        override val appliesBonus = BONUS_AIM_FULL
    }
}

interface DamageCausingAction {
    val damage: String
    val numberOfAttacks: Int
}

interface BonusCausingAction {
    val appliesBonus: Bonus
}

enum class ActionCost {
    FREE_ACTION,
    REACTION,
    HALF_ACTION,
    FULL_ACTION,
    TWO_FULL_ACTIONS // #TODO: Implement
}