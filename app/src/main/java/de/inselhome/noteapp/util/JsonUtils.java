package de.inselhome.noteapp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

public final class JsonUtils {

    public static <T> T read(final Class<T> type, final String json) throws IOException {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        final Gson gson = builder.create();
        return gson.fromJson(json, type);
    }

    public static String toJson(final Object data) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
                return new JsonPrimitive(date.getTime());
            }
        });

        final Gson gson = builder.create();
        return gson.toJson(data);
    }
}
