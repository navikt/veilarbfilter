package no.nav.pto.veilarbfilter.domene.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.util.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class DateSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(localDateTime.format(DateUtils.getFormat()));
    }
}
