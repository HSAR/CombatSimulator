package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.cli.Loggable
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.logic.DamageCausingAction
import io.hsar.wh40k.combatsimulator.logic.EffectCausingAction
import io.hsar.wh40k.combatsimulator.logic.FullAim
import io.hsar.wh40k.combatsimulator.logic.HalfAim
import io.hsar.wh40k.combatsimulator.logic.TacticalActionStrategy
import io.hsar.wh40k.combatsimulator.logic.TargetedAction
import io.hsar.wh40k.combatsimulator.logic.TurnAction
import io.hsar.wh40k.combatsimulator.logic.WeaponReload
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
import io.hsar.wh40k.combatsimulator.model.unit.Attribute
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.ACTIONS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.CURRENT_HEALTH
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_ARM_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_HEAD
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_L
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_LEG_R
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.DAMAGE_REDUCTION_TORSO
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.EFFECTS
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_AMMUNITION
import io.hsar.wh40k.combatsimulator.model.unit.Attribute.WEAPON_TYPE
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.EquipmentItem
import io.hsar.wh40k.combatsimulator.model.unit.ItemType.WEAPON
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import io.hsar.wh40k.combatsimulator.model.unit.WeaponType.MELEE
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue
import io.hsar.wh40k.combatsimulator.random.RandomDice
import io.hsar.wh40k.combatsimulator.random.RollResult
import io.hsar.wh40k.combatsimulator.utils.mergeWithAddition
import io.hsar.wh40k.combatsimulator.utils.sum


/**
 * A single combatant.
 * This class may contain dynamic information that changes as combat progresses.
 */
class UnitInstance(
        val name: String,
        val description: String,
        val unit: Unit,
        val equipment: List<EquipmentItem>,
        val attackExecutor: AttackExecutor = AttackExecutor(), // open for test
        val startingAttributes: Map<Attribute, AttributeValue> = createInitialAttributeMap(unit, equipment),
        val tacticalActionStrategy: TacticalActionStrategy = TacticalActionStrategy,
        val currentAttributes: MutableMap<Attribute, AttributeValue> = startingAttributes.toMutableMap()
) {
    val availableActionOptions: List<ActionOption>
        get() = (startingAttributes.getValue(ACTIONS) as? ActionValue
                ?: throw IllegalStateException("Unit ${name} ACTION attribute should have actions but instead was: ${startingAttributes.getValue(ACTIONS)}"))
                .value

    fun executeActions(actionsToExecute: List<TurnAction>) {
        actionsToExecute
                .map { actionToExecute ->
                    print("        $name does ${actionToExecute.action}")
                    when (actionToExecute) {
                        is TargetedAction -> {
                            when (actionToExecute.action) {
                                is DamageCausingAction -> {
                                    attackExecutor
                                            .rollHits(
                                                    attacker = this,
                                                    target = actionToExecute.target,
                                                    action = actionToExecute.action as DamageCausingAction
                                                    // forced to hard cast to avoid compiler error
                                            )
                                            .let { numberOfHits ->
                                                when {
                                                    (numberOfHits < 1) -> println(" but misses.")
                                                    (numberOfHits == 1) -> print(", hitting ")
                                                    else -> print(", making $numberOfHits hits")
                                                }
                                                repeat(numberOfHits) {
                                                    attackExecutor
                                                            .calcDamage(
                                                                    attacker = this,
                                                                    target = actionToExecute.target,
                                                                    action = actionToExecute.action as DamageCausingAction)
                                                            .let { damage ->
                                                                println("${actionToExecute.target.name} for $damage damage.")
                                                                actionToExecute.target.receiveDamage(damage)
                                                            }
                                                }
                                            }
                                }
                                else -> TODO()
                            }
                        }
                        else -> {  // deal with non-targeted actions, eg aiming
                            when (actionToExecute.action) {
                                is EffectCausingAction -> {
                                    when (val existingEffects = this.currentAttributes[EFFECTS]) {
                                        is EffectValue -> {
                                            this.currentAttributes[EFFECTS] = EffectValue(listOf(existingEffects.value,
                                                    (actionToExecute.action as EffectCausingAction).appliesEffects).flatten())
                                        }
                                        else -> throw IllegalStateException("Effects value should be of type EffectValue)")
                                    }

                                }
                                else -> TODO()
                            }
                            println(".")
                        }
                    }
                }
    }

    fun rollBaseStat(stat: BaseStat, bonus: Int): RollResult {
        return RandomDice.roll(unit.stats.baseStats.getValue(stat) + bonus)
    }

    private fun receiveDamage(damage: Int) {
        when (val health = currentAttributes.getValue(CURRENT_HEALTH)) {
            is NumericValue -> currentAttributes[CURRENT_HEALTH] = NumericValue(health.value - damage)
            else -> throw IllegalStateException("Current health ought to be a NumericValue")
        }
    }

    companion object : Loggable {
        val DEFAULT_ACTIONS = ActionValue(listOf(
                HalfAim,
                FullAim
        ))
        val DEFAULT_ATTRIBUTES = mapOf(ACTIONS to DEFAULT_ACTIONS, EFFECTS to EffectValue(emptyList()))

        fun createInitialAttributeMap(unit: Unit, equipment: List<EquipmentItem>): Map<Attribute, AttributeValue> {
            val equipmentAttributes = equipment.map { it.modifiesAttributes }.sum()

            val ammoMap = equipment
                    .firstOrNull { it.itemType == WEAPON } // #TODO: Handle this better than just "the first weapon to hand"
                    ?.modifiesAttributes
                    ?.let { weaponAttributes ->
                        if (weaponAttributes.getValue(WEAPON_TYPE) == WeaponTypeValue(MELEE)) {
                            // Melee weapons have no need for ammunition
                            null
                        } else {
                            // Ranged weapons take their clip size from the ammo given from a reload
                            mapOf(WEAPON_AMMUNITION to NumericValue((weaponAttributes.getValue(ACTIONS) as ActionValue)
                                    .value
                                    .filterIsInstance<WeaponReload>()
                                    .first().setsAmmunitionTo)
                            )
                        }
                    } ?: emptyMap()

            val toughnessMap = ((unit.stats.baseStats[BaseStat.TOUGHNESS] ?: 0) / 10)
                    .let { toughnessBonus ->
                        listOf(
                                DAMAGE_REDUCTION_HEAD,
                                DAMAGE_REDUCTION_ARM_L,
                                DAMAGE_REDUCTION_ARM_R,
                                DAMAGE_REDUCTION_TORSO,
                                DAMAGE_REDUCTION_LEG_L,
                                DAMAGE_REDUCTION_LEG_R
                        ).map { damageReductionLocation ->
                            damageReductionLocation to NumericValue(toughnessBonus)
                        }.toMap()
                    }

            val dynamicAttributes = mapOf(
                    CURRENT_HEALTH to NumericValue(unit.stats.baseStats.getValue(BaseStat.MAX_HEALTH))
            )
                    .mergeWithAddition(ammoMap)
                    .mergeWithAddition(toughnessMap)

            return DEFAULT_ATTRIBUTES
                    .mergeWithAddition(equipmentAttributes)
                    .mergeWithAddition(dynamicAttributes)
        }
    }
}
