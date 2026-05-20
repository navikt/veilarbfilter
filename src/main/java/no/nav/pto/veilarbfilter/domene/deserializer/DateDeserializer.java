package no.nav.pto.veilarbfilter.domene.deserializer;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.FromStringDeserializer;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.util.DateUtils;
import java.time.LocalDateTime;

@Slf4j
public class DateDeserializer extends FromStringDeserializer<LocalDateTime> {

    public DateDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    protected LocalDateTime _deserialize(String value, DeserializationContext ctx) throws JacksonException {
        return LocalDateTime.parse(value, DateUtils.getFormat());
    }
}
