package io.hsar.wh40k.combatsimulator.content

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.hsar.wh40k.combatsimulator.model.unit.AttributeValue
import io.hsar.wh40k.combatsimulator.model.unit.Effect
import io.hsar.wh40k.combatsimulator.model.unit.EffectValue
import io.hsar.wh40k.combatsimulator.model.unit.NumericValue
import io.hsar.wh40k.combatsimulator.model.unit.WeaponType
import io.hsar.wh40k.combatsimulator.model.unit.WeaponTypeValue

class AttributeValueDeserialiser(vc: Class<*>? = null) : StdDeserializer<AttributeValue>(vc) {

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): AttributeValue {
        return jsonParser.codec.readTree<JsonNode>(jsonParser)
                .let { jsonNode ->
                    val localParser = jsonNode.traverse()
                            .also { it.setCodec(jsonParser.codec) }

                    when {
                        jsonNode.isNumber -> NumericValue(jsonNode.asInt())
                        jsonNode.isWeaponType() -> WeaponTypeValue(WeaponType.valueOf(jsonNode.asText()))
                        jsonNode.isArray -> {
                            val firstItem = jsonNode.first()
                            when {
                                firstItem.isEffect() -> {
                                    localParser
                                            .readValueAs(List::class.java)
                                            .let { effects ->
                                                EffectValue(effects as List<Effect>)
                                            }
                                }
                                firstItem.isAction() -> {

                                }
                                else -> throw JsonParseException(localParser, "Unknown attribute value found: $jsonNode")
                            }
                        }
                    }
                    else -> throw JsonParseException(localParser, "Unknown attribute value found: $jsonNode")
                }
    }

    private fun JsonNode.isWeaponType(): Boolean {
        try {
            WeaponType.valueOf(this.asText())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun JsonNode.isEffect(): Boolean {
        try {
            Effect.valueOf(this.asText())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

}