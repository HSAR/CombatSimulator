package io.hsar.wh40k.combatsimulator.cli

import io.hsar.wh40k.combatsimulator.model.UnitInstance
import io.hsar.wh40k.combatsimulator.model.World
import io.hsar.wh40k.combatsimulator.model.unit.Unit
import io.hsar.wh40k.combatsimulator.utils.UnitPositionUtils

/**
 * Given deserialised DTO data classes, constructs a World.
 */
object WorldCreator {

    fun createWorld(forces: Collection<ForcesDTO>): World {
        return forces
                .map { forceDTO ->
                    // Create Unit from UnitDTO by looking up itemRefs
                    val unitsByUnitName = forceDTO.units
                            .map { unitDTO ->
                                Unit(
                                        unitRef = unitDTO.unitRef,
                                        description = unitDTO.description,
                                        stats = unitDTO.stats,
                                        initialEquipment = unitDTO.equipmentRefs
                                                .map { equipmentRef ->
                                                    ItemDatabase.itemsByItemRef[equipmentRef]
                                                            ?: throw IllegalArgumentException("Attempting to equip unitRef '${unitDTO.unitRef}' with itemRef '$equipmentRef' that was not found.")
                                                }
                                )
                            }
                            .associateBy { unit -> unit.unitRef }

                    // Spawn requested units by creating UnitInstances from Units for them
                    forceDTO.unitsToSpawn
                            .map { (unitName, spawnList) ->
                                val unitToSpawnFrom = unitsByUnitName[unitName]
                                        ?: throw IllegalArgumentException("Attempting to spawn unit named '$unitName' but only these units are available: ${unitsByUnitName.values}")
                                spawnList.map { spawnInstanceName ->
                                    UnitInstance(
                                            name = spawnInstanceName,
                                            description = unitToSpawnFrom.description, // #TODO: Fix this to be specified, or remove
                                            unit = unitToSpawnFrom,
                                            equipment = unitToSpawnFrom.initialEquipment
                                    )
                                }
                            }
                            .flatten()
                            .toMutableList()
                }
                .let { (friendlyForces, enemyForces) ->
                    World(
                            friendlyForces = friendlyForces,
                            enemyForces = enemyForces,
                            unitPositions = UnitPositionUtils.generateStartPositions(listOf(friendlyForces, enemyForces)).toMutableMap()
                    )
                }
    }
}