package com.jupiter;

import java.math.BigInteger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jupiter.game.item.Item;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Rights;

/**
 * Server Settings
 * @author Dennis
 *
 */
public final class Settings {
	
	/**
	 * The Server Name
	 */
	public static final String SERVER_NAME = "Jupiter";

	/**
	 * The Network Port ID
	 */
	public static final int PORT_ID = 43594;
	
	/**
	 * The latest update (this is a message sent on login)
	 */
	public static final String LASTEST_UPDATE = "<col=7E2217>We're proud to be open sourced! Join the Discord & Development.";
	
	/**
	 * The size limit of the received data payload
	 */
	public static final int RECEIVE_DATA_LIMIT = 7500;
	
	/**
	 * The size limit of the Packet payload
	 */
	public static final int PACKET_SIZE_LIMIT = 7500;
	
	/**
	 * The client's revision
	 */
	public static final short CLIENT_BUILD = 727;
	
	/**
	 * The clients custom build number (this usually never changes)
	 */
	public static final byte CUSTOM_CLIENT_BUILD = 1;

	/**
	 * Launching settings
	 * (Startup)
	 */
	public static boolean DEBUG = true;

	/**
	 * Player settings
	 */
	public static final WorldTile START_PLAYER_LOCATION = new WorldTile(3094, 3502, 0);
	public static final WorldTile RESPAWN_PLAYER_LOCATION = new WorldTile(3094, 3502, 0);

	/**
	 * The Max ping delay for packs
	 */
	public static final long MAX_PACKETS_DECODER_PING_DELAY = 30000; // 30seconds
	
	/**
	 * The Server's overall Experience rates
	 */
	public static final byte XP_RATE = 1;

	/**
	 * World settings
	 */
	public static final long WORLD_CYCLE_NS = 600000000L;
	public static final long WORLD_CYCLE_MS = WORLD_CYCLE_NS / 1000000L;

	/**
	 * Memory settings
	 */
	public static final short LOCAL_PLAYERS_LIMIT = 250;
	public static final short PLAYERS_LIMIT = 2048;
	public static final short NPCS_LIMIT = Short.MAX_VALUE;
	public static final byte LOCAL_NPCS_LIMIT = 127;
	public static final int MIN_FREE_MEM_ALLOWED = 30000000; // 30mb

	/**
	 * Map Sizes
	 */
	public static final short[] MAP_SIZES = { 104, 120, 136, 168, 72 };

	public static final String GRAB_SERVER_TOKEN = "ev9+VAp5/tMKeNR/7MOuH6lKWS+rGkHK";
	public static final int[] GRAB_SERVER_KEYS = { 1441, 78700, 44880, 39771, 363186, 44375, 0, 16140, 7316, 271148,
			810710, 216189, 379672, 454149, 933950, 21006, 25367, 17247, 1244, 1, 14856, 1494, 119, 882901, 1818764,
			3963, 3618 };

	// an exeption(grab server has his own keyset unlike rest of client)
	public static final BigInteger GRAB_SERVER_PRIVATE_EXPONENT = new BigInteger(
			"95776340111155337321344029627634178888626101791582245228586750697996713454019354716577077577558156976177994479837760989691356438974879647293064177555518187567327659793331431421153203931914933858526857396428052266926507860603166705084302845740310178306001400777670591958466653637275131498866778592148380588481");
	public static final BigInteger GRAB_SERVER_MODULUS = new BigInteger(
			"119555331260995530494627322191654816613155476612603817103079689925995402263457895890829148093414135342420807287820032417458428763496565605970163936696811485500553506743979521465489801746973392901885588777462023165252483988431877411021816445058706597607453280166045122971960003629860919338852061972113876035333");

	public static final BigInteger PRIVATE_EXPONENT = new BigInteger(
			"95776340111155337321344029627634178888626101791582245228586750697996713454019354716577077577558156976177994479837760989691356438974879647293064177555518187567327659793331431421153203931914933858526857396428052266926507860603166705084302845740310178306001400777670591958466653637275131498866778592148380588481");
	public static final BigInteger MODULUS = new BigInteger(
			"119555331260995530494627322191654816613155476612603817103079689925995402263457895890829148093414135342420807287820032417458428763496565605970163936696811485500553506743979521465489801746973392901885588777462023165252483988431877411021816445058706597607453280166045122971960003629860919338852061972113876035333");

	/**
	 * The maximum amount of players that can be logged in on a single game
	 * sequence.
	 */
	public static final byte LOGOUT_THRESHOLD = 30;

	/**
	 * The maximum amount of drops that can be rolled from the dynamic drop table.
	 */
	public static final byte DROP_THRESHOLD = 2;

	/**
	 * The attempted looped of randomized rare mob drops.
	 */
	public static final byte DROP_RARE_ATTEMPTS = 5;

	public static String[] PROTECT_ON_DEATH = { "chaotic", "stream", "defender", "swift", "spellcaster", "goliath",
			"fire cape", "max cape", "max hood", "completionist cape", "completionist hood", "farseer kiteshield",
			"eagle-eye kiteshield", "gravite" };

	/**
	 * An immutable map of Staff members
	 */
	public static final ImmutableMap<String, Rights> STAFF = ImmutableMap.of(
			"Zed", Rights.ADMINISTRATOR
	);

	/**
	 * An immutable list of Items to give to a Player (Typically on new account creation)
	 */
	public static ImmutableSet<Item> STATER_KIT = ImmutableSet.of(new Item(995, 5000000),
			new Item(4069, 1), new Item(4070, 1), new Item(4071, 1), new Item(4068, 1), new Item(4072, 1),
			new Item(1063, 1), new Item(1095, 1), new Item(1129, 1), new Item(577, 1), new Item(1011, 1),
			new Item(ItemNames.SHORTBOW, 1), new Item(ItemNames.BRONZE_ARROW, 200),
			new Item(1381, 1), new Item(558, 500), new Item(554, 500), new Item(555, 500), new Item(557, 500),
			new Item(11640, 1), new Item(362, 50), new Item(29941));

}