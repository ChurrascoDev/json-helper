package com.github.imthenico.json;

import com.google.gson.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.*;

public class JsonTreeBuilderImpl implements JsonTreeBuilder {

    private final static Gson GSON = new Gson();

    private final Map<String, JsonElement> properties;

    public JsonTreeBuilderImpl() {
        this.properties = new LinkedHashMap<>();
    }

    @Override
    public JsonTreeBuilder add(String name, String... strings) {
        JsonElement jsonElement;

        if (strings.length == 0)
            throw new IllegalArgumentException("length == 0");

        if (strings.length > 1) {
            JsonArray jsonArray = new JsonArray();
            jsonElement = jsonArray;

            for (String str : strings) {
                jsonArray.add(str);
            }
        } else {
            jsonElement = new JsonPrimitive(strings[0]);
        }

        return addProperty(name, jsonElement);
    }

    @Override
    public JsonTreeBuilder add(String name, Number... numbers) {
        JsonElement jsonElement;

        if (numbers.length == 0)
            throw new IllegalArgumentException("length == 0");

        if (numbers.length > 1) {
            JsonArray jsonArray = new JsonArray();
            jsonElement = jsonArray;

            for (Number number : numbers) {
                jsonArray.add(number);
            }
        } else {
            jsonElement = new JsonPrimitive(numbers[0]);
        }

        return addProperty(name, jsonElement);
    }

    @Override
    public JsonTreeBuilder add(String name, Boolean... booleans) {
        JsonElement jsonElement;

        if (booleans.length == 0)
            throw new IllegalArgumentException("length == 0");

        if (booleans.length > 1) {
            JsonArray jsonArray = new JsonArray();
            jsonElement = jsonArray;

            for (Boolean bool : booleans) {
                jsonArray.add(bool);
            }
        } else {
            jsonElement = new JsonPrimitive(booleans[0]);
        }

        return addProperty(name, jsonElement);
    }

    @Override
    public JsonTreeBuilder add(String name, Character... characters) {
        JsonElement jsonElement;

        if (characters.length == 0)
            throw new IllegalArgumentException("length == 0");

        if (characters.length > 1) {
            JsonArray jsonArray = new JsonArray();
            jsonElement = jsonArray;

            for (Character character : characters) {
                jsonArray.add(character);
            }
        } else {
            jsonElement = new JsonPrimitive(characters[0]);
        }

        return addProperty(name, jsonElement);
    }

    @Override
    public JsonTreeBuilder add(String name, JsonElement element) {
        return addProperty(name, element.deepCopy());
    }

    @Override
    public JsonTreeBuilder add(String name, JsonTreeBuilder jsonTreeBuilder) {
        return addProperty(name, jsonTreeBuilder.build());
    }

    @Override
    public JsonTreeBuilder addAll(String name, Collection<? extends JsonSerializable> collection) {
        JsonArray arrayValue = new JsonArray(collection.size());

        for (JsonSerializable jsonSerializable : collection) {
            JsonElement serialized = jsonSerializable.serialize();

            requireNonNull(serialized);

            arrayValue.add(serialized);
        }

        return addProperty(name, arrayValue);
    }

    @Override
    public JsonTreeBuilder add(String name, JsonSerializable serializable) {
        return addProperty(name, serializable.serialize());
    }

    @Override
    public JsonTreeBuilder addTree(JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            addProperty(entry.getKey(), entry.getValue().deepCopy());
        }

        return this;
    }

    @Override
    public JsonObject build() {
        return GSON.toJsonTree(properties)
                .getAsJsonObject();
    }

    private JsonTreeBuilderImpl addProperty(String name, JsonElement object) {
        properties.put(name, object);
        return this;
    }
}