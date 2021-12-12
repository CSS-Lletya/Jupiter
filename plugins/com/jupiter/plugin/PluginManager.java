package com.jupiter.plugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jupiter.plugin.annotations.PluginEventHandler;
import com.jupiter.plugin.annotations.ServerStartupEvent;
import com.jupiter.plugin.events.PluginEvent;
import com.jupiter.plugin.handlers.PluginHandler;
import com.jupiter.utils.LogUtility;
import com.jupiter.utils.LogUtility.Type;
import com.jupiter.utils.Utils;

public class PluginManager {
	
	private static Map<String, HashSet<PluginHandler<PluginEvent>>> UNMAPPED_HANDLERS = new HashMap<>();
	private static List<Method> STARTUP_HOOKS = new ArrayList<>();
	private static PluginMethodRepository REPOSITORY = new PluginMethodRepository();
	
	private static void addUnmappedHandler(String type, PluginHandler<PluginEvent> method) {
		HashSet<PluginHandler<PluginEvent>> methods = UNMAPPED_HANDLERS.get(type);
		if (methods == null) {
			methods = new HashSet<>();
		}
		methods.add(method);
		UNMAPPED_HANDLERS.put(type, methods);
	}

	@SuppressWarnings("unchecked")
	public static void loadPlugins() {
		try {
			long start = System.currentTimeMillis();
			LogUtility.log(Type.INFO, "Plugin Manager", "Loading new plugins...");
			ArrayList<Class<?>> eventTypes = Utils.getClassesArray("com.jupiter.plugin.events");
			ArrayList<Class<?>> classes = Utils.getClassesWithAnnotation("com.jupiter", PluginEventHandler.class);
			Set<Method> visitedMethods = new HashSet<>();
			Set<Field> visitedFields = new HashSet<>();
			LogUtility.log(Type.INFO, "Plugin Manager", "Loading " + eventTypes.size() + " event types and " + classes.size() + " plugin enabled classes.");
			int handlers = 0;
			for (Class<?> clazz : classes) {
				for (Method method : clazz.getMethods()) {
					if (!Modifier.isStatic(method.getModifiers()))
						continue;
					if (visitedMethods.contains(method))
						continue;
					visitedMethods.add(method);
					switch(method.getParameterCount()) {
					case 0:
						if (method.isAnnotationPresent(ServerStartupEvent.class)) {
							STARTUP_HOOKS.add(method);
							handlers++;
						}
						break;
					}
				}
				for (Field field : clazz.getFields()) {
					if (!Modifier.isStatic(field.getModifiers()))
						continue;
					if (visitedFields.contains(field))
						continue;
					visitedFields.add(field);
					
					for (Class<?> eventType : eventTypes) {
						if (field.getType() == PluginHandler.class) {
							Class<?> type = ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
							if (type != eventType)
								continue;
						} else if (field.getType().getSuperclass() == PluginHandler.class) {
							Class<?> type = ((Class<?>) ((ParameterizedType) field.getType().getGenericSuperclass()).getActualTypeArguments()[0]);
							if (type != eventType)
								continue;
						} else
							continue;
						field.setAccessible(true);
						PluginHandler<PluginEvent> handler = (PluginHandler<PluginEvent>) field.get(null);
						if (handler.keys() == null || handler.keys().length <= 0) {
							addUnmappedHandler(eventType.getName(), handler);
							handlers++;
						} else {
							REPOSITORY.addMappedHandler(eventType, handler);
							handlers++;
						}
					}
				}
			}
			LogUtility.log(Type.INFO, "Plugin Manager", "Loaded " + handlers + " plugin event handlers in " + (System.currentTimeMillis()-start) + "ms.");
		} catch (ClassNotFoundException | IOException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	

	public static void executeStartupHooks() {
		for (Method m : STARTUP_HOOKS) {
			long start = System.currentTimeMillis();
			try {
				m.invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Error executing startup hook: " + m.getDeclaringClass().getName() + "." + m.getName());
				e.printStackTrace();
			}
			long time = System.currentTimeMillis() - start;
			if (time > 100L)
				LogUtility.log(Type.INFO, "Plugin Manager", "Executed " + m.getName() + " in " + time + "ms...");
		}
	}
	
	public static boolean handle(PluginEvent event) {
		if (REPOSITORY.handle(event))
			return true;
		HashSet<PluginHandler<PluginEvent>> methods = UNMAPPED_HANDLERS.get(event.getClass().getName());
		if (methods == null)
			return false;
		boolean usedPlugins = false;
		for (PluginHandler<PluginEvent> method : methods) {
			if (method.handleGlobal(event))
				usedPlugins = true;
		}
		return usedPlugins;
	}
	
	public static Object getObj(PluginEvent event) {
		Object obj = REPOSITORY.getObj(event);
		if (obj != null)
			return obj;
		HashSet<PluginHandler<PluginEvent>> methods = UNMAPPED_HANDLERS.get(event.getClass().getName());
		if (methods == null)
			return null;
		for (PluginHandler<PluginEvent> method : methods) {
			obj = method.getObj(event);
			if (obj != null)
				return obj;
		}
		return null;
	}
	
	public static boolean handle(Method method, Object event) {
		try {
			return (boolean) method.invoke(null, event);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LogUtility.log(Type.ERROR, "Plugin Manager", e.getMessage());
		}
		return false;
	}
}
