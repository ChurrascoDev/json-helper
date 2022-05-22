package com.github.imthenico.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public interface JsonReader {

    String getJsonLabel();

    @NotNull JsonElement get(String key) throws IllegalArgumentException;

    <T> @NotNull T mapTo(Type type);

    @NotNull String readString(String key) throws IllegalArgumentException;

    boolean getAsBoolean(String key);

    int readInt(String key);

    byte readByte(String key);

    double readDouble(String key);

    long getLong(String key);

    float getFloat(String key);

    @Nullable
    Number getAsNumber(String key);

    @Nullable
    <T> T readModel(String key, Type type);

    @NotNull
    <E> List<E> listOf(String key, Type type);

    @Nullable
    Map<String, Object> getMap(String key);

    @Nullable
    <T> T manualMapping(
            String key,
            Function<JsonReader, T> mappingFunction
    );

    @Nullable
    <T> T manualElementMapping(
            String key,
            Function<JsonElement, T> mappingFunction
    );

    @NotNull
    <K, V> Map<K, V> treeObjectsMapping(
            String key,
            Function<String, K> keyMapping,
            Function<JsonReader, V> valueMapping,
            String... targets
    );

    @NotNull
    <K, V> Map<K, V> treeValuesMapping(
            String key,
            Function<String, K> keyMapping,
            Function<JsonElement, V> valueMapping,
            String... targets
    );

    @NotNull
    <E> List<E> arrayMapping(
            String arrayKey,
            Function<JsonElement, E> mappingFunction
    );

    @NotNull
    <E> List<E> treeArrayMapping(
            String arrayKey,
            Function<JsonReader, E> mappingFunction
    );

    @NotNull
    <K, V> Map<K, List<V>> treeArraysMapping(
            String sectionKey,
            Function<String, K> keyFunction,
            Function<JsonReader, V> mappingFunction
    );

    @NotNull
    <K, V> Map<K, List<V>> treeArraysMapping(
            String sectionKey,
            Function<String, K> keyFunction,
            Function<JsonReader, V> mappingFunction,
            String... targets
    );

    @NotNull
    Set<String> keys();

    @Nullable
    JsonReader getChildren(String key);

    @NotNull JsonObject getObject(String name);

    @NotNull
    JsonReader copy(Gson gson);

    static JsonReader create(JsonObject jsonObject, Gson gson, String label) {
        return new JsonReaderImpl(
                Objects.requireNonNull(jsonObject, "jsonObject"),
                Objects.requireNonNull(gson, "gson"),
                Objects.requireNonNull(label, "label")
        );
    }
}