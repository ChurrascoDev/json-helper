package com.github.imthenico.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Objects;

public interface JsonTreeBuilder {

    JsonTreeBuilder add(String name, String... str);

    JsonTreeBuilder add(String name, Number... number);

    JsonTreeBuilder add(String name, Boolean... bool);

    JsonTreeBuilder add(String name, Character... character);

    JsonTreeBuilder add(String name, JsonElement element);

    JsonTreeBuilder add(String name, JsonSerializable serializable);

    JsonTreeBuilder add(String name, JsonTreeBuilder jsonTreeBuilder);

    JsonTreeBuilder addTree(JsonObject jsonObject);

    JsonTreeBuilder addAll(String name, Collection<? extends JsonSerializable> jsonSerializableList);

    JsonObject build();

    static JsonTreeBuilder create() {
        return new JsonTreeBuilderImpl();
    }

    static JsonTreeBuilder create(JsonObject jsonObject) {
        Objects.requireNonNull(jsonObject, "jsonObject");

        return create().addTree(jsonObject);
    }
}