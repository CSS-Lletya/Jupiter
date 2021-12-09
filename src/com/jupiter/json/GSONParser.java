package com.jupiter.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.EnumMap;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.jupiter.game.player.Player;
import com.jupiter.utils.Logger;

/**
 * @author Melvin 27 jan. 2020
 * @project Game
 * 
 */

@SuppressWarnings("rawtypes")
public class GSONParser {

	private static Gson GSON;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().disableInnerClassSerialization().enableComplexMapKeySerialization()
				.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).registerTypeAdapter(EnumMap.class, new InstanceCreator<EnumMap>() {
                    @SuppressWarnings({ "unchecked" })
					@Override
                    public EnumMap createInstance(Type type) {
                        Type[] types = (((ParameterizedType) type).getActualTypeArguments());
                        return new EnumMap((Class<?>) types[0]);
                    }
                }).create();
	}

	public static Player load(String dir, Type type) {
		try (Reader reader = Files.newBufferedReader(Paths.get(dir))) {
			return GSON.fromJson(reader, type);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.log("Load", e);
		}
		return null;
	}

	public static void save(Object src, String dir, Type type) {
		try (Writer writer = Files.newBufferedWriter(Paths.get(dir))) {
			writer.write(GSON.toJson(src, type));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}