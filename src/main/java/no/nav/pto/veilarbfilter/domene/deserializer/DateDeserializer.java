package no.nav.pto.veilarbfilter.domene.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.util.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class DateDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return LocalDateTime.parse(jsonParser.getText(), DateUtils.getFormat());
    }
}
