package no.nav.pto.veilarbfilter.domene.deserializer;

import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbfilter.util.DateUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import java.time.LocalDateTime;

@Slf4j
public class DateSerializer extends StdScalarSerializer<LocalDateTime> {

    public DateSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeString(value.format(DateUtils.getFormat()));
    }
}
