package io.hsar.wh40k.combatsimulator.cli

import io.hsar.wh40k.combatsimulator.model.unit.Stats

/**
 * Input file that will be turned into a Unit data class later.
 */
data class UnitDTO(
        val unitRef: String,
        val description: String,
        val stats: Stats,
        val equipmentRefs: List<String>
)