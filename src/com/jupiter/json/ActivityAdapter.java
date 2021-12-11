package com.jupiter.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.SneakyThrows;

public class ActivityAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

	@Override
	public final JsonElement serialize(final T object, final Type interfaceType, final JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.addProperty("type", object.getClass().getName());
		member.add("activity", context.serialize(object));
		return member;
	}

	@Override
	public final T deserialize(final JsonElement elem, final Type interfaceType, final JsonDeserializationContext context) throws JsonParseException {
		final JsonObject member = (JsonObject) elem;
		final JsonElement typeString = get(member, "type");
		final JsonElement data = get(member, "activity");
		final Type actualType = typeForName(typeString);
		return context.deserialize(data, actualType);
	}

	@SneakyThrows(ClassNotFoundException.class)
	private Type typeForName(final JsonElement typeElem) {
		return Class.forName(typeElem.getAsString());
	}

	private JsonElement get(final JsonObject wrapper, final String memberName) {
		if (wrapper.get(memberName) == null) {
			throw new JsonParseException("no '" + memberName + "' member found in json file.");
		}
		return wrapper.get(memberName);
	}
}