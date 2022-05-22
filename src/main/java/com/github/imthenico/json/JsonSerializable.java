package com.github.imthenico.json;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

public interface JsonSerializable {

    @NotNull JsonElement serialize();

}