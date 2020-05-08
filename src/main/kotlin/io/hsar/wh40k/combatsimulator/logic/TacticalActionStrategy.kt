package io.hsar.wh40k.combatsimulator.logic

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.BaseStat
import io.hsar.wh40k.combatsimulator.random.AverageDice

object TacticalActionStrategy : ActionStrategy {

    override fun decideTurnActions(world: World, thisUnit: UnitInstance, possibleActionOptions: Collection<ActionOption>): List<TurnAction> {
        // Shit implementation - units will only ever aim and fire their max damage attack
        val aimAction = possibleActionOptions
                .find { action ->
                    action is ActionOption.HalfAim
                }
                ?.let {
                    action -> AimAction(action)
                }

        val maxDamageAttackAction = possibleActionOptions
                .filter { action ->
                    action is DamageCausingAction
                }
                .map {
                    eachDamageCausingAction ->
                     (eachDamageCausingAction as DamageCausingAction)
                }
                .let { damageCausingActions ->
                    performTargeting(world, thisUnit, damageCausingActions)
                }
                .map { eachTargetedAction ->
                    // Associate each action with an estimate of its damage
                    when (eachTargetedAction.action) {
                        is DamageCausingAction -> eachTargetedAction.action.damage
                                .let { damageString ->
                                    eachTargetedAction to AverageDice.roll(damageString)
                                }
                        else -> eachTargetedAction to 0
                    }
                }
                .maxBy { (_, expectedDamage) -> expectedDamage }
                ?.first


        return listOfNotNull(aimAction, maxDamageAttackAction)
    }

    fun performTargeting(world: World, thisUnit: UnitInstance, possibleAttacks: List<DamageCausingAction>): List<TargetedAction> {
        val targetedActions: List<TargetedAction> = listOf()
        var wip = targetedActions.toMutableList()

        // for each possible attack, work out which targets in range and generate an
        for(attack in possibleAttacks) {
            if(attack is RangedAttackAction) {
                for(adversary in world.getAdversaries(thisUnit)) {
                    if(attack.range >= world.distanceApart(thisUnit, adversary)) {
                        wip.add(wip.size - 1, TargetedAction(attack as ActionOption, adversary))
                    }
                }

            } else { // for melee attacks
                // check melee attack range based on attack type
                for(adversary in world.getAdversaries(thisUnit)) {
                    if(attack is ActionOption.MeleeAttack && world.distanceApart(thisUnit, adversary) == 1) {
                        wip.add(wip.size - 1, TargetedAction(attack, adversary))
                    } else if(attack is ActionOption.ChargeAttack) {
                        if(attack.isValidMovementPath(world.getPosition(thisUnit), world.getPosition(adversary))
                                && attack.getMovementRange(thisUnit.unit.stats.baseStats.getValue(BaseStat.AGILITY)) >
                                world.distanceApart(thisUnit, adversary)) {
                            wip.add(wip.size - 1, TargetedAction(attack, adversary))
                        }
                    }
                }
            }
        }
        return wip

    }

    //TODO add collision detection
}