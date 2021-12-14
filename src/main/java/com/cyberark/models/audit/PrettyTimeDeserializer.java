package com.cyberark.models.audit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PrettyTimeDeserializer extends StdDeserializer<String> {
  public PrettyTimeDeserializer() {
    super((Class<?> )null);
  }

  public PrettyTimeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public String deserialize(JsonParser jp,
                          DeserializationContext deserializationContext) throws IOException {
    PrettyTime p = new PrettyTime();
    JsonNode node = jp.getCodec().readTree(jp);

    try {
      return p.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X").parse(node.textValue()));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return node.textValue();
  }
}
