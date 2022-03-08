package no.nav.pto.veilarbfilter.domene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;
import org.junit.jupiter.api.BeforeAll;

import java.time.LocalDateTime;

public class MineLagredeFilterModelTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        objectMapper.registerModule(module);
    }
    

}