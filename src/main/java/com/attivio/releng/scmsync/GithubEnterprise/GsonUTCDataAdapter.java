package com.attivio.releng.scmsync.GithubEnterprise;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Custom Adapter to translate date to utc using JodaTime which can handle
 * timezone offset including ':' which SimpleDateFormatter will not handle
 *
 * see http://stackoverflow.com/questions/26044881/java-date-to-utc-using-gson
 * Created by userb on 10/26/16.
 */
class GsonUTCDataAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
  private final DateTimeFormatter dateFormatter;

  GsonUTCDataAdapter() {
    dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
  }

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      DateTime dateTime = dateFormatter.parseDateTime(json.getAsString());

      // Generate Date object in UTC
      return dateTime.toDateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
  }

  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(dateFormatter.print(new DateTime(src)));
  }
}
