package no.nav.pto.veilarbfilter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.domene.PortefoljeFilter;
import no.nav.pto.veilarbfilter.domene.deserializer.DateDeserializer;
import no.nav.pto.veilarbfilter.domene.deserializer.DateSerializer;

import java.time.LocalDateTime;

@Slf4j
public class JsonUtils {

    public static PortefoljeFilter deserializeFilterValg(String json) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        mapper.registerModule(module);

        try {
            return mapper.readValue(json, PortefoljeFilter.class);
        } catch (JsonProcessingException e) {
            log.error("Can't unserialize filter valg", e);
            return null;
        }
    }

    public static String serializeFilterValg(PortefoljeFilter filter) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new DateDeserializer());
        module.addSerializer(LocalDateTime.class, new DateSerializer());
        mapper.registerModule(module);
        try {
            return mapper.writeValueAsString(filter);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize filter valg", e);
            return null;
        }
    }
}
