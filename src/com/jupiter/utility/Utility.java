package com.jupiter.utility;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.reflect.ClassPath;
import com.jupiter.game.Entity;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.skills.Skills;
import com.jupiter.utility.LogUtility.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public final class Utility {

	private static final Object ALGORITHM_LOCK = new Object();

	private static long timeCorrection;
	private static long lastTimeUpdate;

	public static synchronized long currentTimeMillis() {
		long l = System.currentTimeMillis();
		if (l < lastTimeUpdate)
			timeCorrection += lastTimeUpdate - l;
		lastTimeUpdate = l;
		return l + timeCorrection;
	}

	public static byte[] cryptRSA(byte[] data, BigInteger exponent, BigInteger modulus) {
		return new BigInteger(data).modPow(exponent, modulus).toByteArray();
	}

	/**
	 * Gets all of the classes in a directory
	 * @param directory The directory to iterate through
	 * @return The list of classes
	 */
	public static ObjectList<Object> getClassesInDirectory(String directory) {
		ObjectList<Object> classes = new ObjectArrayList<>();
		for(File file : new File("./bin/main/" + directory.replace(".", "/")).listFiles()) {
			if(file.getName().contains("$")) {
				continue;
			}
			try {
				Object objectEvent = (Class.forName(directory + "." + file.getName().replace(".class", "")).newInstance());
				classes.add(objectEvent);
			} catch(InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//				e.printStackTrace();
			}
		}
		return classes;
	}
	
	public static final byte[] encryptUsingMD5(byte[] buffer) {
		// prevents concurrency problems with the algorithm
		synchronized (ALGORITHM_LOCK) {
			try {
				MessageDigest algorithm = MessageDigest.getInstance("MD5");
				algorithm.update(buffer);
				byte[] digest = algorithm.digest();
				algorithm.reset();
				return digest;
			} catch (Throwable e) {
				LogUtility.log(Type.ERROR, "Utils", e.getMessage());
			}
			return null;
		}
	}

	public static boolean inCircle(WorldTile location, WorldTile center, int radius) {
		return getDistance(center, location) < radius;
	}

	public static final int getDistance(WorldTile t1, WorldTile t2) {
		return getDistance(t1.getX(), t1.getY(), t2.getX(), t2.getY());
	}

	public static final int getDistance(int coordX1, int coordY1, int coordX2, int coordY2) {
		int deltaX = coordX2 - coordX1;
		int deltaY = coordY2 - coordY1;
		return ((int) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
	}

	public static final byte[] DIRECTION_DELTA_X = new byte[] { -1, 0, 1, -1, 1, -1, 0, 1 };
	public static final byte[] DIRECTION_DELTA_Y = new byte[] { 1, 1, 1, 0, 0, -1, -1, -1 };

	public static int getNpcMoveDirection(int dd) {
		if (dd < 0)
			return -1;
		return getNpcMoveDirection(DIRECTION_DELTA_X[dd], DIRECTION_DELTA_Y[dd]);
	}

	public static int getNpcMoveDirection(int dx, int dy) {
		if (dx == 0 && dy > 0)
			return 0;
		if (dx > 0 && dy > 0)
			return 1;
		if (dx > 0 && dy == 0)
			return 2;
		if (dx > 0 && dy < 0)
			return 3;
		if (dx == 0 && dy < 0)
			return 4;
		if (dx < 0 && dy < 0)
			return 5;
		if (dx < 0 && dy == 0)
			return 6;
		if (dx < 0 && dy > 0)
			return 7;
		return -1;
	}

	public static final int[][] getCoordOffsetsNear(int size) {
		int[] xs = new int[4 + (4 * size)];
		int[] xy = new int[xs.length];
		xs[0] = -size;
		xy[0] = 1;
		xs[1] = 1;
		xy[1] = 1;
		xs[2] = -size;
		xy[2] = -size;
		xs[3] = 1;
		xy[2] = -size;
		for (int fakeSize = size; fakeSize > 0; fakeSize--) {
			xs[(4 + ((size - fakeSize) * 4))] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4))] = 1;
			xs[(4 + ((size - fakeSize) * 4)) + 1] = -size;
			xy[(4 + ((size - fakeSize) * 4)) + 1] = -fakeSize + 1;
			xs[(4 + ((size - fakeSize) * 4)) + 2] = 1;
			xy[(4 + ((size - fakeSize) * 4)) + 2] = -fakeSize + 1;
			xs[(4 + ((size - fakeSize) * 4)) + 3] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4)) + 3] = -size;
		}
		return new int[][] { xs, xy };
	}

	public static final int getFaceDirection(int xOffset, int yOffset) {
		return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
	}

	public static final int getMoveDirection(int xOffset, int yOffset) {
		if (xOffset < 0) {
			if (yOffset < 0)
				return 5;
			else if (yOffset > 0)
				return 0;
			else
				return 3;
		} else if (xOffset > 0) {
			if (yOffset < 0)
				return 7;
			else if (yOffset > 0)
				return 2;
			else
				return 4;
		} else {
			if (yOffset < 0)
				return 6;
			else if (yOffset > 0)
				return 1;
			else
				return -1;
		}
	}

	public static String[] ALPHABET = { "a", "b", "c", "d", "e",
			"f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
			"s", "t", "u", "v", "w", "x", "y", "z" };

	public static String getCharacterFromByte(int byte0){
		if (byte0 >= 97 && byte0 <= 122)
			return ALPHABET[byte0 - 97];
		else if (byte0 >= 48 && byte0 <= 57)
			return String.valueOf(byte0 - 48);
		return "a";
				
	}
	
	public static int[] KEY_PRESS_ORDINALITY = { 12288, 17408, 16896, 12800, 
			8704, 13056, 13312, 13568, 9984, 13824, 14080, 14336, 17920, 17664, 
			10240, 10496, 8192, 8960, 12544, 9216, 9728, 17152, 8448, 16640, 9472, 
			16384, 6400, 4096, 4352, 4608, 4864, 5120, 5376, 5632, 5888, 6144 };
	
	public static String getKeyPressedFromListenerByte(int short0){
		switch (short0){ //shortcutting
		case 3328:
			return "esc";
		case 21248:
			return "space_bar";
		}
		int keyIndex = -1;
		for (int index = 0; index < KEY_PRESS_ORDINALITY.length; index++){
			if (KEY_PRESS_ORDINALITY[index] == short0){
				keyIndex = index;
				break;
			}
		}
		if (keyIndex >= 0 && keyIndex <= ALPHABET.length - 1)
			return ALPHABET[keyIndex];
		else if (keyIndex >= ALPHABET.length && keyIndex <= ALPHABET.length + 9)
			return String.valueOf(keyIndex - ALPHABET.length);
		return "-a";
	}
	
	public static String formatPlayerNameForProtocol(String name) {
		if (name == null)
			return "";
		name = name.replaceAll(" ", "_");
		name = name.toLowerCase();
		return name;
	}

	public static String formatPlayerNameForDisplay(String name) {
		if (name == null)
			return "";
		name = name.replaceAll("_", " ");
		name = name.toLowerCase();
		StringBuilder newName = new StringBuilder();
		boolean wasSpace = true;
		for (int i = 0; i < name.length(); i++) {
			if (wasSpace) {
				newName.append(("" + name.charAt(i)).toUpperCase());
				wasSpace = false;
			} else {
				newName.append(name.charAt(i));
			}
			if (name.charAt(i) == ' ') {
				wasSpace = true;
			}
		}
		return newName.toString();
	}
	
	public static final String longToString(long l) {
		if (l <= 0L || l >= 0x5b5b57f8a98a5dd1L)
			return null;
		if (l % 37L == 0L)
			return null;
		int i = 0;
		char ac[] = new char[12];
		while (l != 0L) {
			long l1 = l;
			l /= 37L;
			ac[11 - i++] = VALID_CHARS[(int) (l1 - l * 37L)];
		}
		return new String(ac, 12 - i, i);
	}

	public static final char[] VALID_CHARS = { '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9' };

	public static boolean invalidAccountName(String name) {
		return name.length() < 2 || name.length() > 12 || name.startsWith("_") || name.endsWith("_")
				|| name.contains("__") || containsInvalidCharacter(name);
	}

	public static boolean invalidAuthId(String auth) {
		return auth.length() != 10 || auth.contains("_") || containsInvalidCharacter(auth);
	}

	public static boolean containsInvalidCharacter(char c) {
		for (char vc : VALID_CHARS) {
			if (vc == c)
				return false;
		}
		return true;
	}

	public static boolean containsInvalidCharacter(String name) {
		for (char c : name.toCharArray()) {
			if (containsInvalidCharacter(c))
				return true;
		}
		return false;
	}

	public static final long stringToLong(String s) {
		long l = 0L;
		for (int i = 0; i < s.length() && i < 12; i++) {
			char c = s.charAt(i);
			l *= 37L;
			if (c >= 'A' && c <= 'Z')
				l += (1 + c) - 65;
			else if (c >= 'a' && c <= 'z')
				l += (1 + c) - 97;
			else if (c >= '0' && c <= '9')
				l += (27 + c) - 48;
		}
		while (l % 37L == 0L && l != 0L) {
			l /= 37L;
		}
		return l;
	}

	/**
	 * Walk dirs 0 - South-West 1 - South 2 - South-East 3 - West 4 - East 5 - North-West 6 - North 7 - North-East
	 */
	public static int getPlayerWalkingDirection(int dx, int dy) {
		if (dx == -1 && dy == -1) {
			return 0;
		}
		if (dx == 0 && dy == -1) {
			return 1;
		}
		if (dx == 1 && dy == -1) {
			return 2;
		}
		if (dx == -1 && dy == 0) {
			return 3;
		}
		if (dx == 1 && dy == 0) {
			return 4;
		}
		if (dx == -1 && dy == 1) {
			return 5;
		}
		if (dx == 0 && dy == 1) {
			return 6;
		}
		if (dx == 1 && dy == 1) {
			return 7;
		}
		return -1;
	}

	public static int getPlayerRunningDirection(int dx, int dy) {
		if (dx == -2 && dy == -2)
			return 0;
		if (dx == -1 && dy == -2)
			return 1;
		if (dx == 0 && dy == -2)
			return 2;
		if (dx == 1 && dy == -2)
			return 3;
		if (dx == 2 && dy == -2)
			return 4;
		if (dx == -2 && dy == -1)
			return 5;
		if (dx == 2 && dy == -1)
			return 6;
		if (dx == -2 && dy == 0)
			return 7;
		if (dx == 2 && dy == 0)
			return 8;
		if (dx == -2 && dy == 1)
			return 9;
		if (dx == 2 && dy == 1)
			return 10;
		if (dx == -2 && dy == 2)
			return 11;
		if (dx == -1 && dy == 2)
			return 12;
		if (dx == 0 && dy == 2)
			return 13;
		if (dx == 1 && dy == 2)
			return 14;
		if (dx == 2 && dy == 2)
			return 15;
		return -1;
	}

	public static byte[] completeQuickMessage(Player player, int fileId, byte[] data) {
		if (fileId == 1)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.AGILITY) };
		else if (fileId == 8)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.ATTACK) };
		else if (fileId == 13)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.CONSTRUCTION) };
		else if (fileId == 16)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.COOKING) };
		else if (fileId == 23)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.CRAFTING) };
		else if (fileId == 30)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.DEFENCE) };
		else if (fileId == 34)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FARMING) };
		else if (fileId == 41)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FIREMAKING) };
		else if (fileId == 47)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FISHING) };
		else if (fileId == 55)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FLETCHING) };
		else if (fileId == 62)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HERBLORE) };
		else if (fileId == 70)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HITPOINTS) };
		else if (fileId == 74)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HUNTER) };
		else if (fileId == 135)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.MAGIC) };
		else if (fileId == 127)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.MINING) };
		else if (fileId == 120)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.PRAYER) };
		else if (fileId == 116)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.RANGE) };
		else if (fileId == 111)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.RUNECRAFTING) };
		else if (fileId == 103)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SLAYER) };
		else if (fileId == 96)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SMITHING) };
		else if (fileId == 92)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.STRENGTH) };
		else if (fileId == 85)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SUMMONING) };
		else if (fileId == 79)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.THIEVING) };
		else if (fileId == 142)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.WOODCUTTING) };
		else if (fileId == 990)
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.DUNGEONEERING) };
		else if (fileId == 965) {
			int value = player.getHitpoints();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		}
		LogUtility.log(Type.INFO, "Utils", "qc: " + fileId + ", " + (data == null ? 0 : data.length));
		return data;
	}

	public static String fixChatMessage(String message) {
		StringBuilder newText = new StringBuilder();
		boolean wasSpace = true;
		boolean exception = false;
		for (int i = 0; i < message.length(); i++) {
			if (!exception) {
				if (wasSpace) {
					newText.append(("" + message.charAt(i)).toUpperCase());
					if (!String.valueOf(message.charAt(i)).equals(" "))
						wasSpace = false;
				} else {
					newText.append(("" + message.charAt(i)).toLowerCase());
				}
			} else {
				newText.append(("" + message.charAt(i)));
			}
			if (String.valueOf(message.charAt(i)).contains(":"))
				exception = true;
			else if (String.valueOf(message.charAt(i)).contains(".") || String.valueOf(message.charAt(i)).contains("!")
					|| String.valueOf(message.charAt(i)).contains("?"))
				wasSpace = true;
		}
		return newText.toString();
	}

	/**
	 * Appends the determined plural check to {@code thing}.
	 * @param thing the thing to append.
	 * @return the {@code thing} after the plural check has been appended.
	 */
	public static String appendPluralCheck(String thing) {
		return thing.concat(determinePluralCheck(thing));
	}
	
	/**
	 * Determines the plural check of {@code thing}.
	 * @param thing the thing to determine for.
	 * @return the plural check.
	 */
	public static String determinePluralCheck(String thing) {
		boolean needsPlural = !thing.endsWith("s") && !thing.endsWith(")");
		return needsPlural ? "s" : "";
	}
	
	public static String format(long number) {
		return NumberFormat.getIntegerInstance().format(number);
	}

	public static int clampI(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	public static ArrayList<Class<?>> getClassesWithAnnotation(String packageName,
			Class<? extends Annotation> annotation) throws ClassNotFoundException, IOException {
		ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (ClassPath.ClassInfo info : cp.getTopLevelClassesRecursive(packageName)) {
			if (!Class.forName(info.getName()).isAnnotationPresent(annotation))
				continue;
			classes.add(Class.forName(info.getName()));
		}
		return classes;
	}

	public static ArrayList<Class<?>> getClassesArray(String packageName) throws ClassNotFoundException, IOException {
		ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (ClassPath.ClassInfo info : cp.getTopLevelClassesRecursive(packageName)) {
			classes.add(Class.forName(info.getName()));
		}
		return classes;
	}

	public static Map<String, Object> cloneMap(Map<String, Object> from) {
		if (from == null)
			return null;
		Map<String, Object> newMap = new HashMap<String, Object>();
		from.entrySet().forEach(entry -> newMap.put(entry.getKey(), entry.getValue()));
		return newMap;
	}

	public static final int getAngleTo(WorldTile fromTile, WorldTile toTile) {
		return getAngleTo(toTile.getX() - fromTile.getX(), toTile.getY() - fromTile.getY());
	}

	public static final int getAngleTo(int xOffset, int yOffset) {
		return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
	}

	public static boolean isInRange(int x1, int y1, int size1, int x2, int y2, int size2, int maxDistance) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		if (distanceX > size2 + maxDistance || distanceX < -size1 - maxDistance || distanceY > size2 + maxDistance || distanceY < -size1 - maxDistance)
			return false;
		return true;
	}
		
	public static boolean collides(Entity entity, Entity target) {
		return entity.getPlane() == target.getPlane() && collides(entity.getX(), entity.getY(), entity.getSize(), target.getX(), target.getY(), target.getSize());
	}
	
	public static boolean collides(WorldTile entity, WorldTile target) {
		return entity.getPlane() == target.getPlane() && collides(entity.getX(), entity.getY(), entity instanceof Entity ? ((Entity) entity).getSize() : 1, target.getX(), target.getY(), target instanceof Entity ? ((Entity) target).getSize() : 1);
	}

	public static boolean collides(WorldTile entity, WorldTile target, int s1, int s2) {
		return entity.getPlane() == target.getPlane() && collides(entity.getX(), entity.getY(), s1, target.getX(), target.getY(), s2);
	}
	
	public static boolean isInRange(WorldTile entity, WorldTile target, int rangeRatio) {
		return entity.getPlane() == target.getPlane() && isInRange(entity.getX(), entity.getY(), entity instanceof Entity ? ((Entity) entity).getSize() : 1, target.getX(), target.getY(), target instanceof Entity ? ((Entity) target).getSize() : 1, rangeRatio);
	}
	
	public static boolean isInRange(Entity entity, Entity target, int rangeRatio) {
		return entity.getPlane() == target.getPlane() && isInRange(entity.getX(), entity.getY(), entity.getSize(), target.getX(), target.getY(), target.getSize(), rangeRatio);
	}

	public static boolean isInRange(WorldTile entity, WorldTile target, int rangeRatio, int s1, int s2) {
		return entity.getPlane() == target.getPlane() && isInRange(entity.getX(), entity.getY(), s1, target.getX(), target.getY(), s2, rangeRatio);
	}
	
	public static boolean collides(int x1, int y1, int size1, int x2, int y2, int size2) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return distanceX < size2 && distanceX > -size1 && distanceY < size2 && distanceY > -size1;
	}
}