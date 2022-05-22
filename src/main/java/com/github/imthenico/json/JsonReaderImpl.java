package com.github.imthenico.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JsonReaderImpl implements JsonReader {

    private final JsonObject jsonObject;
    private final Gson gson;
    private final String label;

    public JsonReaderImpl(
            JsonObject jsonObject,
            Gson gson,
            String label
    ) {
        this.jsonObject = jsonObject;
        this.gson = gson;
        this.label = label;
    }

    @Override
    public String getJsonLabel() {
        return label;
    }

    @Override
    public @NotNull JsonElement get(String name) throws IllegalArgumentException {
        JsonElement type = jsonObject.get(name);

        if (type == null)
            throw new IllegalArgumentException("No value found with name '" + name + "'");

        return jsonObject.get(name);
    }

    @Override
    public <T> @NotNull T mapTo(Type type) {
        return gson.fromJson(jsonObject, type);
    }

    @Override
    public @NotNull String readString(String key) throws IllegalArgumentException {
        JsonElement element = get(key);

        return element.getAsJsonPrimitive().getAsString();
    }

    @Override
    public boolean getAsBoolean(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return false;

        return element.getAsJsonPrimitive().getAsBoolean();
    }

    @Override
    public int readInt(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsInt();
    }

    @Override
    public byte readByte(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsByte();
    }

    @Override
    public double readDouble(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsDouble();
    }

    @Override
    public long getLong(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsLong();
    }

    @Override
    public float getFloat(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsFloat();
    }

    @Override
    public @Nullable Number getAsNumber(String key) {
        JsonElement element = get(key);

        if (!element.isJsonPrimitive())
            return 0;

        return element.getAsJsonPrimitive().getAsNumber();
    }

    @Override
    public <T> T readModel(String key, Type type) {
        JsonElement element = get(key);

        return gson.fromJson(element, type);
    }

    @Override
    public @NotNull <E> List<E> listOf(String key, Type type) {
        return treeArrayMapping(key, reader -> reader.mapTo(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Map<String, Object> getMap(String key) {
        JsonElement element = get(key);

        return gson.fromJson(element, Map.class);
    }

    @Override
    public @Nullable <T> T manualMapping(String key, Function<JsonReader, T> mappingFunction) {
        JsonReader reader = getChildren(key);

        if (reader == null)
            return null;

        return mappingFunction.apply(reader);
    }

    @Override
    public <T> @Nullable T manualElementMapping(
            String key,
            Function<JsonElement, T> mappingFunction
    ) {
        JsonElement jsonElement = jsonObject.get(key);

        if (jsonElement == null) {
            throw new NullPointerException("no element found with key '" + key + "'");
        }

        return mappingFunction.apply(jsonElement);
    }

    @Override
    public @NotNull <K, V> Map<K, V> treeObjectsMapping(
            String key,
            Function<String, K> keyMapping,
            Function<JsonReader, V> valueMapping,
            String... targets
    ) {
        Map<K, V> map = new LinkedHashMap<>();

        iterateInsideTree(key, (label, value) -> {
            if (!value.isJsonPrimitive())
                return;

            K mappedKey = keyMapping.apply(label);
            V mappedValue = valueMapping.apply(new JsonReaderImpl(
                    value.getAsJsonObject(),
                    gson,
                    label
            ));

            if (mappedKey == null || mappedValue == null)
                return;

            map.put(mappedKey, mappedValue);
        }, targets);

        return map;
    }

    @Override
    public @NotNull <K, V> Map<K, V> treeValuesMapping(
            String key,
            Function<String, K> keyMapping,
            Function<JsonElement, V> valueMapping,
            String... targets
    ) {
        Map<K, V> map = new LinkedHashMap<>();

        iterateInsideTree(key, (label, element) -> {
            K mappedKey = keyMapping.apply(key);
            V mappedValue = valueMapping.apply(element);

            if (mappedKey == null || mappedValue == null)
                return;

            map.put(mappedKey, mappedValue);
        }, targets);

        return map;
    }

    @Override
    public @NotNull <E> List<E> arrayMapping(
            String arrayKey, Function<JsonElement, E> mappingFunction
    ) {
        List<E> list = new LinkedList<>();

        try {
            JsonArray array = jsonObject.getAsJsonArray(arrayKey);

            if (array == null)
                return list;

            for (JsonElement element : array) {
                if (element == null)
                    continue;

                E mapped = mappingFunction.apply(element);

                if (mapped == null)
                    continue;

                list.add(mapped);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public @NotNull <E> List<E> treeArrayMapping(
            String arrayKey,
            Function<JsonReader, E> mappingFunction
    ) {
        return arrayMapping(arrayKey, value -> {
            if (!value.isJsonObject())
                return null;

            return mappingFunction.apply(new JsonReaderImpl(
                    value.getAsJsonObject(),
                    gson,
                    null
            ));
        });
    }

    @Override
    public @NotNull <K, V> Map<K, List<V>> treeArraysMapping(
            String sectionKey,
            Function<String, K> keyFunction,
            Function<JsonReader, V> mappingFunction
    ) {
        return treeArraysMapping(sectionKey, keyFunction, mappingFunction, new String[0]);
    }

    @Override
    public @NotNull <K, V> Map<K, List<V>> treeArraysMapping(
            String sectionKey,
            Function<String, K> keyFunction,
            Function<JsonReader, V> mappingFunction,
            String... targets
    ) {
        Map<K, List<V>> map = new LinkedHashMap<>();

        JsonObject section = sectionKey != null ? getObject(sectionKey) : this.jsonObject;

        if (section == null)
            return map;

        List<String> targetList = new LinkedList<>();

        if (targets == null || targets.length == 0) {
            targetList.addAll(section.keySet());
        } else {
            targetList.addAll(Arrays.asList(targets));
        }

        for (String entryKey : targetList) {
            JsonArray array = getArray(section, entryKey);
            K key = keyFunction.apply(entryKey);

            List<V> list = new LinkedList<>();

            for (JsonElement value : array) {
                if (!value.isJsonObject())
                    continue;

                V mapped = mappingFunction.apply(new JsonReaderImpl(
                        value.getAsJsonObject(),
                        gson,
                        entryKey
                ));

                if (mapped == null)
                    continue;

                list.add(mapped);
            }

            map.put(key, list);
        }

        return map;
    }

    private JsonArray getArray(JsonObject object, String name) {
        JsonArray jsonArray = object.getAsJsonArray(name);

        if (jsonArray == null)
            throw new IllegalArgumentException("No array found with name '" + name + "'");

        return jsonArray;
    }

    @Override
    public @NotNull Set<String> keys() {
        return new HashSet<>(jsonObject.keySet());
    }

    @Override
    public @Nullable JsonReader getChildren(String key) {
        JsonObject element = getObject(key);

        return new JsonReaderImpl(element, gson, key);
    }

    @Override
    public @NotNull JsonReader copy(Gson gson) {
        return new JsonReaderImpl(
                jsonObject.deepCopy(),
                Objects.requireNonNull(gson),
                label
        );
    }

    private void iterateInsideTree(String name, BiConsumer<String, JsonObject> action, String... targets) {
        JsonObject jsonObject = getObject(name);

        for (String target : targets) {
            if (!jsonObject.has(target))
                throw new IllegalArgumentException("No target found with name '" + target + "'");

            action.accept(target, getObject(target));
        }
    }

    @Override
    public @NotNull JsonObject getObject(String name) {
        JsonObject tree = this.jsonObject.getAsJsonObject(name);

        if (tree == null) {
            throw new IllegalArgumentException("No object found with name '" + name + "'");
        }

        return tree;
    }
}