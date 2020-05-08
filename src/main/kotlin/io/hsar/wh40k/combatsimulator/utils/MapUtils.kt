package io.hsar.wh40k.combatsimulator.utils

import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue

fun <K> Map<K, AttributeValue>.mergeWithAddition(otherMap: Map<K, AttributeValue>): Map<K, AttributeValue> {
    return this.toMutableMap()
            .also { tempMutableMap ->
                otherMap.forEach { (counterIdentifier, counterValue) ->
                    tempMutableMap.merge(counterIdentifier, counterValue) { counterValueA, counterValueB ->
                        when {
                            (counterValueA is NumericValue && counterValueB is NumericValue) ->
                                counterValueA + counterValueB
                            (counterValueA is WeaponTypeValue && counterValueB is WeaponTypeValue) ->
                                counterValueA + counterValueB
                            // Not working yet
                            //  (counterValueA is StackingValue<*> && counterValueB is StackingValue<*>) ->
                            //      counterValueA + counterValueB
                            else ->
                                throw UnsupportedOperationException("Cannot add ${counterValueA.javaClass.kotlin.qualifiedName} and ${counterValueB.javaClass.kotlin.qualifiedName}.")
                        }
                    }
                }
            }
}

fun <K> List<Map<K, AttributeValue>>.sum(): Map<K, AttributeValue> {
    return this.fold( // Merge everything together
            initial = emptyMap()
    ) { totalValueLists, newValues ->
        totalValueLists.mergeWithAddition(newValues)
    }
}