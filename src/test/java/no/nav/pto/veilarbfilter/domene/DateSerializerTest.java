package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

public class DateSerializerTest {
    private static ObjectMapper mapper;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        mapper.registerModule(module);
    }

    @Test
    public void whenDateIsValidThenDeserializeIt() throws IOException {
        String json = "{\"opprettetDato\": \"2020-03-05 23:05:45\"}";

        MineLagredeFilterModel deserializedData = mapper.readValue(json, MineLagredeFilterModel.class);

        LocalDateTime deserializedDate = deserializedData.getOpprettetDato();
        Assertions.assertNotNull(deserializedDate);
        Assertions.assertEquals(2020, deserializedDate.getYear());
        Assertions.assertEquals(3, deserializedDate.getMonthValue());
        Assertions.assertEquals(5, deserializedDate.getDayOfMonth());
        Assertions.assertEquals(deserializedDate.getHour(), 23);
        Assertions.assertEquals(deserializedDate.getMinute(), 5);
        Assertions.assertEquals(deserializedDate.getSecond(), 45);
    }

    @Test
    public void whenDateIsInvalidThenReturnNull() {
        MineLagredeFilterModel deserializedData = new MineLagredeFilterModel();
        try {
            String json = "{\"opprettetDato\": \"20220-03-05T23:05:45.224\"}";

            deserializedData = mapper.readValue(json, MineLagredeFilterModel.class);
            Assertions.fail();
        } catch (JsonProcessingException e) {
            Assertions.assertNull(deserializedData.getOpprettetDato());
        }
    }

    @Test
    public void whenDateIsValidTestSerialization() throws IOException {
        String jsonValue = mapper.writeValueAsString(LocalDateTime.of(2022, Month.JANUARY, 1, 0, 0));

        Assertions.assertNotNull(jsonValue);
        Assertions.assertTrue(jsonValue.length() > 10);
    }
}
