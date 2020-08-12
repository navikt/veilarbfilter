package no.nav.pto.veilarbfilter.model

import com.google.gson.JsonPrimitive
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

class DateSerializerTest {

    @Test
    fun whenDateIsValidThenDeserializeIt() {

        var deserializedDate =
            DateSerializer().deserialize(JsonPrimitive("2020-03-05T23:05:45.224"), null, null)
        Assert.assertTrue(deserializedDate != null)
        Assert.assertTrue(deserializedDate?.year == 2020)
        Assert.assertTrue(deserializedDate?.monthValue == 3)
        Assert.assertTrue(deserializedDate?.dayOfMonth == 5)
        Assert.assertTrue(deserializedDate?.hour == 23)
        Assert.assertTrue(deserializedDate?.minute == 5)
        Assert.assertTrue(deserializedDate?.second == 45)

        deserializedDate =
            DateSerializer().deserialize(JsonPrimitive("2020-03-05T23:05:45.24"), null, null)
        Assert.assertTrue(deserializedDate != null)
        Assert.assertTrue(deserializedDate?.year == 2020)
        Assert.assertTrue(deserializedDate?.monthValue == 3)
        Assert.assertTrue(deserializedDate?.dayOfMonth == 5)
        Assert.assertTrue(deserializedDate?.hour == 23)
        Assert.assertTrue(deserializedDate?.minute == 5)
        Assert.assertTrue(deserializedDate?.second == 45)
    }

    @Test
    fun whenDateIsInvalidThenReturnNull() {
        var deserializedDate =
            DateSerializer().deserialize(JsonPrimitive("20220-03-05T23:05:45.224"), null, null)
        Assert.assertNull(deserializedDate)
    }

    @Test
    fun whenDateIsValidTestSerialization() {
        var date = LocalDateTime.now()
        val serializedDate = DateSerializer().serialize(date, null, null)
        Assert.assertTrue(serializedDate != null)
        Assert.assertTrue(serializedDate.asString.length > 10)
    }

}