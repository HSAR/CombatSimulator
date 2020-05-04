package io.hsar.wh40k.combatsimulator.model

import io.hsar.wh40k.combatsimulator.model.unit.Unit

// #TODO May have to introduce an intermediate class (UnitPosition?) to represent cover and/or co-ordinate positions
data class World(val friendlyForces: List<UnitInstance>, val enemyForces: List<UnitInstance>)