package com.jupiter.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.Activity;
import com.jupiter.json.impl.NPCSpawns;
import com.jupiter.utils.LogUtility;

import io.vavr.control.Try;

/**
 * @author Melvin 27 jan. 2020
 * @project Game
 * 
 */

@SuppressWarnings("all")
public class GsonLoader {

	private static Gson GSON;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().disableInnerClassSerialization().enableComplexMapKeySerialization()
				.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
				.registerTypeAdapter(Activity.class, new ActivityAdapter<Activity>())
				.registerTypeAdapter(EnumMap.class, new InstanceCreator<EnumMap>() {
					@Override
					public EnumMap createInstance(Type type) {
						Type[] types = (((ParameterizedType) type).getActualTypeArguments());
						return new EnumMap((Class<?>) types[0]);
					}
				}).create();
		
		Try.run(() -> NPCSpawns.init());
	}

	public static Player load(String dir, Type type) {
		try (Reader reader = Files.newBufferedReader(Paths.get(dir))) {
			return GSON.fromJson(reader, type);
		} catch (IOException e) {
			e.printStackTrace();
			LogUtility.log(LogUtility.Type.ERROR, "Gson Loader", e.getMessage());
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

	public static <T> T loadJsonFile(File f, Type clazz) throws JsonIOException, IOException {
		if (!f.exists())
			return null;
		JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		T obj = GSON.fromJson(reader, clazz);
		reader.close();
		return obj;
	}

	public static <T> T fromJSONString(String json, Type clazz) throws JsonIOException, IOException {
		T obj = GSON.fromJson(json, clazz);
		return obj;
	}

	public static final void saveJsonFile(Object o, File f) throws JsonIOException, IOException {
		File dir = new File(f.getParent());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
		writer.setIndent("  ");
		GSON.toJson(o, o.getClass(), writer);
		writer.flush();
		writer.close();
	}

	public static String toJson(Object o) {
		return GSON.toJson(o);
	}
}