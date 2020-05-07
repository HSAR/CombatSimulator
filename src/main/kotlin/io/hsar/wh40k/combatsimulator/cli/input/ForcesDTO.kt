package io.hsar.wh40k.combatsimulator.cli.input

data class ForcesDTO(
        val units: List<UnitDTO>,
        val unitsToSpawn: Map<String, List<String>>
        // #TODO Possible manual positioning specified here?
)