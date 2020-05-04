package io.hsar.wh40k.combatsimulator.cli.model

import io.hsar.wh40k.combatsimulator.model.unit.DerivedStats
import io.hsar.wh40k.combatsimulator.model.unit.Stats

/**
 * User-defined unit attributes from which to derive required simulation information
 */
// TODO Templated units for large battles
data class UnitDefinition(
        val unitName: String,
        val stats: Stats,
        val training: Map<DerivedStats, Short>
)