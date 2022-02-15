package no.nav.pto.veilarbfilter.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.pto.veilarbfilter.domene.MineLagredeFilterModel;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

public class DateSerializerTest {
    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        mapper.registerModule(module);
    }

    @Test
    public void whenDateIsValidThenDeserializeIt() throws IOException {
        String json = "{\"opprettetDato\": \"2020-03-05T23:05:45.224\"}";

        MineLagredeFilterModel deserializedData = mapper.readValue(json, MineLagredeFilterModel.class);

        LocalDateTime deserializedDate = deserializedData.getOpprettetDato();
        Assert.assertTrue(deserializedDate != null);
        Assert.assertTrue(deserializedDate.getYear() == 2020);
        Assert.assertTrue(deserializedDate.getMonthValue() == 3);
        Assert.assertTrue(deserializedDate.getDayOfMonth() == 5);
        Assert.assertTrue(deserializedDate.getHour() == 23);
        Assert.assertTrue(deserializedDate.getMinute() == 5);
        Assert.assertTrue(deserializedDate.getSecond() == 45);
    }

    @Test
    public void whenDateIsInvalidThenReturnNull() {
        MineLagredeFilterModel deserializedData = new MineLagredeFilterModel();
        try {
            String json = "{\"opprettetDato\": \"20220-03-05T23:05:45.224\"}";

            deserializedData = mapper.readValue(json, MineLagredeFilterModel.class);
            Assert.fail();
        } catch (JsonProcessingException e) {
            Assert.assertNull(deserializedData.getOpprettetDato());
        }
    }

    @Test
    public void whenDateIsValidTestSerialization() throws IOException {
        String jsonValue = mapper.writeValueAsString(LocalDateTime.of(2022, Month.JANUARY, 1, 0, 0));

        Assert.assertNotNull(jsonValue);
        Assert.assertTrue(jsonValue.length() > 10);
    }
}
