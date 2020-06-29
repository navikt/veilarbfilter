package no.nav.pto.veilarbfilter.model

import com.google.gson.*
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class DateSerializer : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
    private val log = LoggerFactory.getLogger("DateDeserializer")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    @Throws(JsonParseException::class)
    override fun deserialize(element: JsonElement, typeOf: Type?, arg2: JsonDeserializationContext?): LocalDateTime? {
        val date = element.asString
        return try {
            LocalDateTime.parse(date, formatter);
        } catch (e: DateTimeParseException) {
            log.warn("Can't parse date:$date, error: ", e)
            null
        }
    }

    override fun serialize(date: LocalDateTime?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(formatter.format(date))
    }
}