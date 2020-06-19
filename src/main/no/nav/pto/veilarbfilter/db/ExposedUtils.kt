package no.nav.pto.veilarbfilter.db

import no.nav.pto.veilarbfilter.ObjectMapperProvider.Companion.objectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

fun <T : Any> Table.jsonb(name: String, klass: Class<T>): Column<T>
        = registerColumn(name, Json(klass))

private class Json<out T : Any>(private val klass: Class<T>) : ColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any {
        value as PGobject
        return try {
            objectMapper.readValue(value.value, klass)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }


    override fun notNullValueToDB(value: Any): Any = objectMapper.writeValueAsString(value)
    override fun nonNullValueToString(value: Any): String = "'${objectMapper.writeValueAsString(value)}'"
}