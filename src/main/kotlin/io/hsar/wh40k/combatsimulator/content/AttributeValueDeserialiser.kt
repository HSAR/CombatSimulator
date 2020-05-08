package io.hsar.wh40k.combatsimulator.content

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.hsar.wh40k.combatsimulator.logic.ActionOption
import io.hsar.wh40k.combatsimulator.model.unit.ActionValue
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
                    val codec = jsonParser.codec
                    val localParser = jsonNode.getParser(codec)

                    when {
                        jsonNode.isNumber -> NumericValue(jsonNode.asInt())
                        jsonNode.isWeaponType() -> WeaponTypeValue(WeaponType.valueOf(jsonNode.asText()))
                        jsonNode.isArray -> {
                            val firstItem = jsonNode.first()
                            when {
                                firstItem.isEffect() -> {
                                    EffectValue(
                                            jsonNode.map { eachNode ->
                                                eachNode.getParser(codec)
                                                        .readValueAs(Effect::class.java)
                                            }
                                    )
                                }
                                firstItem.isAction() -> {
                                    ActionValue(
                                            jsonNode.map { eachNode ->
                                                eachNode.getParser(codec)
                                                        .readValueAs(ActionOption::class.java)
                                            }
                                    )
                                }
                                else -> throw JsonParseException(localParser, "Unknown attribute value found: $jsonNode")
                            }
                        }
                        else -> throw JsonParseException(localParser, "Unknown attribute value found: $jsonNode")
                    }
                }
    }

    private fun JsonNode.getParser(objectCodec: ObjectCodec) = this
            .traverse()
            .also { it.setCodec(objectCodec) }

    private fun JsonNode.isWeaponType(): Boolean {
        return try {
            WeaponType.valueOf(this.asText())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun JsonNode.isEffect(): Boolean {
        return try {
            Effect.valueOf(this.asText())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun JsonNode.isAction(): Boolean = this["actionType"] != null

}