package io.hsar.wh40k.combatsimulator.utils

import io.hsar.wh40k.combatsimulator.model.MapPosition
import io.hsar.wh40k.combatsimulator.model.UnitInstance

object UnitPositionUtils {

    /**
     * Places forces 2 squares apart, facing each other in a line abreast.
     */
    fun generateStartPositions(forcesUnits: Collection<Collection<UnitInstance>>): Map<UnitInstance, MapPosition> {
        return forcesUnits
                .mapIndexed { forceIndex, force ->
                    force
                            .shuffled() // Inject randomness to avoid biasing some units to the middle
                            .mapIndexed { unitIndex, unit ->
                                unit to MapPosition(unitIndex, forceIndex * 2)
                            }
                }
                .flatten()
                .toMap()
    }
}