package io.hsar.wh40k.combatsimulator.cli

import io.hsar.wh40k.combatsimulator.model.unit.Unit

typealias UnitName = String
typealias UnitInstanceName = String

data class ForcesInput(
        val units: List<UnitDTO>,
        val unitsToSpawn: Map<UnitName, List<UnitInstanceName>>
        // #TODO Possible manual positioning specified here?
)