package com.jupiter.game.player.content;

import java.util.Optional;

import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.combat.npc.NPC;
import com.jupiter.game.map.TileAttributes;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.LinkedTaskSequence;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.ForceTalk;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.utils.Utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A new way of handling Emotes & Special Emotes such as Linked Queue emotes &
 * Skillcapes.
 * 
 * @author Dennis
 *
 */
public class Emotes {

	/**
	 * A list of Emotes for the Player to perform from the Emotes tab
	 * 
	 * @author Dennis
	 *
	 */
	@AllArgsConstructor
	public enum Emote {
		YES((byte) 0, Optional.of(new Animation(855)), Optional.empty(), Optional.empty()),
		NO((byte) 1, Optional.of(new Animation(856)), Optional.empty(), Optional.empty()),
		BOW((byte) 2, Optional.of(new Animation(858)), Optional.empty(), Optional.empty()),
		ANGRY((byte) 3, Optional.of(new Animation(859)), Optional.empty(), Optional.empty()),
		THINKING((byte) 4, Optional.of(new Animation(857)), Optional.empty(), Optional.empty()),
		WAVE((byte) 5, Optional.of(new Animation(863)), Optional.empty(), Optional.empty()),
		SHRUG((byte) 6, Optional.of(new Animation(2113)), Optional.empty(), Optional.empty()),
		CHEER((byte) 7, Optional.of(new Animation(862)), Optional.empty(), Optional.empty()),
		BECKON((byte) 8, Optional.of(new Animation(864)), Optional.empty(), Optional.empty()),
		LAUGH((byte) 9, Optional.of(new Animation(861)), Optional.empty(), Optional.empty()),
		JUMP_FOR_JOY((byte) 10, Optional.of(new Animation(2109)), Optional.empty(), Optional.empty()),
		YAWN((byte) 11, Optional.of(new Animation(2111)), Optional.empty(), Optional.empty()),
		DANCE((byte) 12, Optional.of(new Animation(866)), Optional.empty(), Optional.empty()),
		JIG((byte) 13, Optional.of(new Animation(2106)), Optional.empty(), Optional.empty()),
		TWIRL((byte) 14, Optional.of(new Animation(2107)), Optional.empty(), Optional.empty()),
		HEADBANG((byte) 15, Optional.of(new Animation(2108)), Optional.empty(), Optional.empty()),
		CRY((byte) 16, Optional.of(new Animation(860)), Optional.empty(), Optional.empty()),
		BLOW_KISS((byte) 17, Optional.of(new Animation(1374)), Optional.of(new Graphics(1702)), Optional.empty()),
		PANIC((byte) 18, Optional.of(new Animation(2105)), Optional.empty(), Optional.empty()),
		RASPBERRY((byte) 19, Optional.of(new Animation(2110)), Optional.empty(), Optional.empty()),
		CLAP((byte) 20, Optional.of(new Animation(865)), Optional.empty(), Optional.empty()),
		SALUTE((byte) 21, Optional.of(new Animation(2112)), Optional.empty(), Optional.empty()),
		GOBLIN_BOW((byte) 22, Optional.of(new Animation(0x84F)), Optional.empty(), Optional.empty()),
		GOBLIN_SALUTE((byte) 23, Optional.of(new Animation(0x850)), Optional.empty(), Optional.empty()),
		GLASS_BOX((byte) 24, Optional.of(new Animation(1131)), Optional.empty(), Optional.empty()),
		CLIMB_ROPE((byte) 25, Optional.of(new Animation(1130)), Optional.empty(), Optional.empty()),
		LEAN((byte) 26, Optional.of(new Animation(1129)), Optional.empty(), Optional.empty()),
		GLASS_WALL((byte) 27, Optional.of(new Animation(1128)), Optional.empty(), Optional.empty()),
		IDEA((byte) 28, Optional.of(new Animation(4275)), Optional.empty(), Optional.empty()),
		STOMP((byte) 29, Optional.of(new Animation(1745)), Optional.empty(), Optional.empty()),
		FLAP((byte) 30, Optional.of(new Animation(4280)), Optional.empty(), Optional.empty()),
		SLAP_HEAD((byte) 31, Optional.of(new Animation(4276)), Optional.empty(), Optional.empty()),
		ZOMBIE_WALK((byte) 32, Optional.of(new Animation(3544)), Optional.empty(), Optional.empty()),
		ZOMBIE_DANCE((byte) 33, Optional.of(new Animation(3543)), Optional.empty(), Optional.empty()),
		ZOMBIE_HAND((byte) 34, Optional.of(new Animation(7272)), Optional.of(new Graphics(1244)), Optional.empty()),
		SCARED((byte) 35, Optional.of(new Animation(2836)), Optional.empty(), Optional.empty()),
		BUNNY_HOP((byte) 36, Optional.of(new Animation(6111)), Optional.empty(), Optional.empty()),
		// skillcape is 37
		SNOWMAN_DANCE((byte) 38, Optional.of(new Animation(7531)), Optional.empty(), Optional.empty()),
		AIR_GUITAR((byte) 39, Optional.of(new Animation(2414)), Optional.of(new Graphics(1537)),
				Optional.of(SpecialEmote.AIR_GUITAR)),
		SAFETY_FIRST((byte) 40, Optional.of(new Animation(8770)), Optional.of(new Graphics(1553)), Optional.empty()),
		EXPLORE((byte) 41, Optional.of(new Animation(9990)), Optional.of(new Graphics(1734)), Optional.empty()),
		TRICK((byte) 42, Optional.of(new Animation(10530)), Optional.of(new Graphics(1864)), Optional.empty()),
		FREEZE((byte) 43, Optional.of(new Animation(11044)), Optional.of(new Graphics(1973)), Optional.empty()),
		TURKEY((byte) 44, Optional.empty(), Optional.empty(), Optional.of(SpecialEmote.TURKEY)),
		AROUND_THE_WORLD_IN_EGGTY_DAYS((byte) 45, Optional.of(new Animation(11542)), Optional.of(new Graphics(2037)),
				Optional.empty()),
		DRAMATIC_POINT((byte) 46, Optional.of(new Animation(12658)), Optional.empty(), Optional.empty()),
		FAINT((byte) 47, Optional.of(new Animation(14165)), Optional.empty(), Optional.empty()),
		PUPPET_MASTER((byte) 48, Optional.of(new Animation(14869)), Optional.of(new Graphics(2837)), Optional.empty()),
		TASK_MASTER((byte) 49, Optional.of(new Animation(15033)), Optional.of(new Graphics(2930)), Optional.empty()),
		SEAL_OF_APPROVAL((byte) 50, Optional.empty(), Optional.empty(), Optional.of(SpecialEmote.SEAL_OF_APPROVAL)),
		CAT_FIGHT((byte) 51, Optional.of(new Animation(2252)), Optional.empty(), Optional.empty()),
		TALK_TO_THE_HAND((byte) 52, Optional.of(new Animation(2416)), Optional.empty(), Optional.empty()),
		SHAKE_HANDS((byte) 53, Optional.of(new Animation(2303)), Optional.empty(), Optional.empty()),
		HIGH_FIVE((byte) 54, Optional.of(new Animation(2312)), Optional.empty(), Optional.empty()),
		FACE_PALM((byte) 55, Optional.of(new Animation(2254)), Optional.empty(), Optional.empty()),
		SURRENDER((byte) 56, Optional.of(new Animation(2360)), Optional.empty(), Optional.empty()),
		LEVITATE((byte) 57, Optional.of(new Animation(2327)), Optional.empty(), Optional.empty()),
		MUSCLE_MAN_POSE((byte) 58, Optional.of(new Animation(2566)), Optional.empty(), Optional.empty()),
		ROFL((byte) 59, Optional.of(new Animation(2347)), Optional.empty(), Optional.empty()),
		BREATHE_FIRE((byte) 60, Optional.of(new Animation(2238)), Optional.of(new Graphics(358)), Optional.empty()),
		STORM((byte) 61, Optional.of(new Animation(2563)), Optional.of(new Graphics(365)), Optional.empty()),
		SNOW((byte) 62, Optional.of(new Animation(2417)), Optional.of(new Graphics(364)), Optional.empty()),
		INVOKE_SPRING((byte) 63, Optional.of(new Animation(15357)), Optional.of(new Graphics(1391)), Optional.empty()),
		HEAD_IN_SAND((byte) 64, Optional.of(new Animation(12926)), Optional.of(new Graphics(1761)), Optional.empty()),
		HULA_HOOP((byte) 65, Optional.of(new Animation(12928)), Optional.empty(), Optional.empty()),
		DISAPPEAR((byte) 66, Optional.of(new Animation(12929)), Optional.of(new Graphics(1760)), Optional.empty()),
		GHOST((byte) 67, Optional.of(new Animation(12932)), Optional.of(new Graphics(1762)), Optional.empty()),
		BRING_IT((byte) 68, Optional.of(new Animation(12934)), Optional.empty(), Optional.empty()),
		PALM_FIST((byte) 69, Optional.of(new Animation(12931)), Optional.empty(), Optional.empty()),
		LIVIMG_ON_BORROWED_TIME((byte) 93, Optional.empty(), Optional.empty(), Optional.of(SpecialEmote.BORROWED_TIME)),
		TROUBADOUR_DANCE((byte) 94, Optional.of(new Animation(15399)), Optional.empty(), Optional.empty()),
		EVIL_LAUGH((byte) 95, Optional.of(new Animation(15535)), Optional.empty(), Optional.empty()), // female: 15536
		GOLF_CLAP((byte) 96, Optional.of(new Animation(15520)), Optional.empty(), Optional.empty()),
		LOL_CANO((byte) 97, Optional.of(new Animation(15532)), Optional.of(new Graphics(2191)), Optional.empty()),
		INFERNAL_POWER((byte) 98, Optional.of(new Animation(15529)), Optional.of(new Graphics(2197)), Optional.empty()),
		DIVINE_POWER((byte) 99, Optional.of(new Animation(15524)), Optional.of(new Graphics(2195)), Optional.empty()),
		YOUR_DEAD((byte) 100, Optional.of(new Animation(14195)), Optional.empty(), Optional.empty()),
		SCREAM((byte) 101, Optional.of(new Animation(15526)), Optional.empty(), Optional.empty()), // female 15527
		TORNADO((byte) 102, Optional.of(new Animation(15530)), Optional.of(new Graphics(2196)), Optional.empty()),
		CHAOTIC_COOKERY((byte) 103, Optional.of(new Animation(15604)), Optional.of(new Graphics(2239)),
				Optional.empty()),
		ROFL_COPTER((byte) 104, Optional.of(new Animation(16373)), Optional.of(new Graphics(3009)), Optional.empty()), // female
																														// 163734
		NATURES_MIGHT((byte) 105, Optional.of(new Animation(16376)), Optional.of(new Graphics(3011)), Optional.empty()),
		INNER_POWER((byte) 106, Optional.of(new Animation(16382)), Optional.of(new Graphics(3014)), Optional.empty()),
		WEREWOLF((byte) 107, Optional.empty(), Optional.empty(), Optional.of(SpecialEmote.WEREWOLF)),
		CELEBRATE((byte) 108, Optional.of(new Animation(16913)), Optional.of(new Graphics(3175)), Optional.empty()),
		BREAKDANCE((byte) 109, Optional.of(new Animation(17079)), Optional.empty(), Optional.empty()),
		MAHJARRAT_TRANSFORMATION((byte) 110, Optional.of(new Animation(17103)), Optional.of(new Graphics(3222)),
				Optional.empty()),
		BREAK_WIND((byte) 111, Optional.of(new Animation(17076)), Optional.of(new Graphics(3226)), Optional.empty()),
		BACKFLIP((byte) 112, Optional.of(new Animation(17101)), Optional.of(new Graphics(3221)), Optional.empty()),
		GRAVE_DIGGER((byte) 113, Optional.of(new Animation(17077)), Optional.of(new Graphics(3219)), Optional.empty()),
		FROG_TRANSFORMATION((byte) 114, Optional.of(new Animation(17080)), Optional.of(new Graphics(3220)),
				Optional.empty()),
		MEXICAN_WAVE((byte) 115, Optional.of(new Animation(17163)), Optional.empty(), Optional.empty()),
		SPORTSMAN((byte) 116, Optional.of(new Animation(17166)), Optional.empty(), Optional.empty()),
		SUNBATH((byte) 117, Optional.of(new Animation(17213)), Optional.of(new Graphics(3257)), Optional.empty()),
		KICK_SAND((byte) 118, Optional.of(new Animation(17186)), Optional.of(new Graphics(3252)), Optional.empty()),;

		/**
		 * The button Id (slot id).
		 */
		@Getter
		private final byte buttonId;

		/**
		 * The Animation being performed.
		 */
		@Getter
		private final Optional<Animation> animation;

		/**
		 * The Graphics being performed.
		 */
		@Getter
		private final Optional<Graphics> graphics;

		/**
		 * The Special Emote being performed.
		 */
		@Getter
		private final Optional<SpecialEmote> specialEmote;

		public static void executeSpecifiedEmote(Player player, Emote emote) {
			if (isDoingEmote(player))
				return;
			emote.getSpecialEmote().ifPresent(user -> user.handleSpecialEmote(player));
			emote.getAnimation().ifPresent(player::setNextAnimation);
			emote.getGraphics().ifPresent(player::setNextGraphics);
			if (!isDoingEmote(player))
				setNextEmoteEnd(player);
		}
		
		public static void executeNPCEmote(NPC npc, Emote emote) {
			emote.getAnimation().ifPresent(npc::setNextAnimation);
			emote.getGraphics().ifPresent(npc::setNextGraphics);
		}

		/**
		 * Executes the Emote.
		 * 
		 * @param player
		 * @param buttonId
		 */
		public static void executeEmote(Player player, int buttonId) {
			if (isDoingEmote(player))
				return;
			for (Emote emote : Emote.values()) {
				if (buttonId == emote.getButtonId()) {
					emote.getSpecialEmote().ifPresent(user -> user.handleSpecialEmote(player));

					emote.getAnimation().ifPresent(player::setNextAnimation);
					emote.getGraphics().ifPresent(player::setNextGraphics);
					if (!isDoingEmote(player))
						setNextEmoteEnd(player);
				}
				if (buttonId == 37) {
					instance.useBookEmote(player, buttonId);
				}
			}
		}

		private static void setNextEmoteEnd(Player player) {
			player.setNextEmoteEnd(player.getLastAnimationEnd() - 600);
		}

		public void setNextEmoteEnd(Player player, long delay) {
			player.setNextEmoteEnd(Utils.currentTimeMillis() + delay);
		}

		public static boolean isDoingEmote(Player player) {
			return player.getNextEmoteEnd() >= Utils.currentTimeMillis();
		}
	}

	/**
	 * A list of Special Emote events to take place (Skillcapes, Linked Queue
	 * events, etc..).
	 * 
	 * @author Dennis
	 *
	 */
	private enum SpecialEmote {
		AIR_GUITAR {
			@Override
			protected boolean handleSpecialEmote(Player player) {
				player.getPackets().sendMusicEffect(302);
				return true;
			}
		},
		TURKEY {
			@Override
			protected boolean handleSpecialEmote(Player player) {
				LinkedTaskSequence turkeySeq = new LinkedTaskSequence();
				turkeySeq.connect(1, () -> {
					player.setNextAnimation(new Animation(10994));
					player.setNextGraphics(new Graphics(86));
				});
				turkeySeq.connect(2, () -> {
					player.setNextAnimation(new Animation(10996));
					player.getAppearence().transformIntoNPC(8499);
				});
				turkeySeq.connect(6, () -> {
					player.setNextAnimation(new Animation(10995));
					player.setNextGraphics(new Graphics(86));
					player.getAppearence().transformIntoNPC(-1);
				});
				turkeySeq.start();
				return true;
			}
		},
		SEAL_OF_APPROVAL {
			@Override
			protected boolean handleSpecialEmote(Player player) {
				LinkedTaskSequence soaSeq = new LinkedTaskSequence();
				soaSeq.connect(1, () -> {
					player.setNextAnimation(new Animation(15104));
					player.setNextGraphics(new Graphics(1287));
				});
				soaSeq.connect(2, () -> {
					int random = (int) (Math.random() * (2 + 1));
					player.setNextAnimation(new Animation(15106));
					player.getAppearence().transformIntoNPC(random == 0 ? 13255 : (random == 1 ? 13256 : 13257));
				});
				soaSeq.connect(3, () -> player.setNextAnimation(new Animation(15108)));
				soaSeq.connect(4, () -> {
					player.setNextAnimation(new Animation(15105));
					player.setNextGraphics(new Graphics(1287));
					player.getAppearence().transformIntoNPC(-1);
				});
				soaSeq.start();
				return true;
			}
		},
		WEREWOLF {
			@Override
			protected boolean handleSpecialEmote(Player player) {
				player.setNextAnimation(new Animation(16380));
				player.setNextGraphics(new Graphics(3013));
				player.setNextGraphics(new Graphics(3016));
				return true;
			}
		},
		BORROWED_TIME {
			@Override
			protected boolean handleSpecialEmote(Player player) {
				if (!TileAttributes.isTileFree(player.getPlane(), player.getX(), player.getY(), 3)) {
					player.getPackets().sendGameMessage("You need clear space in order to perform this emote.", true);
					return false;
				} else if (player.getCurrentActivity().isPresent()) {
					player.getPackets().sendGameMessage("You can't do this here.", true);
					return false;
				}
				final NPC reaper = new NPC(14388, new WorldTile(player.getX(), player.getY() - 2, player.getPlane()),
						false);
				reaper.setLocation(reaper);
				reaper.setNextFaceEntity(player);
				player.setNextFaceEntity(reaper);

				LinkedTaskSequence timeSeq = new LinkedTaskSequence();
				timeSeq.connect(1, () -> {
					reaper.setNextAnimation(new Animation(13964));
					player.setNextGraphics(new Graphics(1766));
					player.setNextAnimation(new Animation(13965));
				});
				timeSeq.connect(10, () -> {
					reaper.setFinished(true);
					World.removeNPC(reaper);
					reaper.setNextFaceEntity(null);
				});
				timeSeq.connect(11, () -> {
					player.setNextForceTalk(new ForceTalk("Phew! Close call."));
					player.setNextFaceEntity(null);
				});
				timeSeq.start();
				return true;
			}
		};

		/**
		 * The execution method for the Special Emote.
		 * 
		 * @param player
		 * @return state
		 */
		protected boolean handleSpecialEmote(Player player) {
			return false;
		}
	}

	public static Emotes instance = new Emotes();
	
	public void useBookEmote(Player player, int id) {
		player.getAttributes().stopAll(player, false);
		if (id == 37) {
			// TODO skillCape
			final int capeId = player.getEquipment().getCapeId();
			switch (capeId) {
			case 9747:
			case 9748:
			case 10639: // Attack cape
				player.setNextAnimation(new Animation(4959));
				player.setNextGraphics(new Graphics(823));
				break;
			case 9753:
			case 9754:
			case 10641: // Defence cape
				player.setNextAnimation(new Animation(4961));
				player.setNextGraphics(new Graphics(824));
				break;
			case 9750:
			case 9751:
			case 10640: // Strength cape
				player.setNextAnimation(new Animation(4981));
				player.setNextGraphics(new Graphics(828));
				break;
			case 9768:
			case 9769:
			case 10647: // Hitpoints cape
				player.setNextAnimation(new Animation(14242));
				player.setNextGraphics(new Graphics(2745));
				break;
			case 9756:
			case 9757:
			case 10642: // Ranged cape
				player.setNextAnimation(new Animation(4973));
				player.setNextGraphics(new Graphics(832));
				break;
			case 9762:
			case 9763:
			case 10644: // Magic cape
				player.setNextAnimation(new Animation(4939));
				player.setNextGraphics(new Graphics(813));
				break;
			case 9759:
			case 9760:
			case 10643: // Prayer cape
				player.setNextAnimation(new Animation(4979));
				player.setNextGraphics(new Graphics(829));
				break;
			case 9801:
			case 9802:
			case 10658: // Cooking cape
				player.setNextAnimation(new Animation(4955));
				player.setNextGraphics(new Graphics(821));
				break;
			case 9807:
			case 9808:
			case 10660: // Woodcutting cape
				player.setNextAnimation(new Animation(4957));
				player.setNextGraphics(new Graphics(822));
				break;
			case 9783:
			case 9784:
			case 10652: // Fletching cape
				player.setNextAnimation(new Animation(4937));
				player.setNextGraphics(new Graphics(812));
				break;
			case 9798:
			case 9799:
			case 10657: // Fishing cape
				player.setNextAnimation(new Animation(4951));
				player.setNextGraphics(new Graphics(819));
				break;
			case 9804:
			case 9805:
			case 10659: // Firemaking cape
				player.setNextAnimation(new Animation(4975));
				player.setNextGraphics(new Graphics(831));
				break;
			case 9780:
			case 9781:
			case 10651: // Crafting cape
				player.setNextAnimation(new Animation(4949));
				player.setNextGraphics(new Graphics(818));
				break;
			case 9795:
			case 9796:
			case 10656: // Smithing cape
				player.setNextAnimation(new Animation(4943));
				player.setNextGraphics(new Graphics(815));
				break;
			case 9792:
			case 9793:
			case 10655: // Mining cape
				player.setNextAnimation(new Animation(4941));
				player.setNextGraphics(new Graphics(814));
				break;
			case 9774:
			case 9775:
			case 10649: // Herblore cape
				player.setNextAnimation(new Animation(4969));
				player.setNextGraphics(new Graphics(835));
				break;
			case 9771:
			case 9772:
			case 10648: // Agility cape
				player.setNextAnimation(new Animation(4977));
				player.setNextGraphics(new Graphics(830));
				break;
			case 9777:
			case 9778:
			case 10650: // Thieving cape
				player.setNextAnimation(new Animation(4965));
				player.setNextGraphics(new Graphics(826));
				break;
			case 9786:
			case 9787:
			case 10653: // Slayer cape
				player.setNextAnimation(new Animation(4967));
				player.setNextGraphics(new Graphics(1656));
				break;
			case 9810:
			case 9811:
			case 10611: // Farming cape
				player.setNextAnimation(new Animation(4963));
				player.setNextGraphics(new Graphics(825));
				break;
			case 9765:
			case 9766:
			case 10645: // Runecrafting cape
				player.setNextAnimation(new Animation(4947));
				player.setNextGraphics(new Graphics(817));
				break;
			case 9789:
			case 9790:
			case 10654: // Construction cape
				player.setNextAnimation(new Animation(4953));
				player.setNextGraphics(new Graphics(820));
				break;
			case 12169:
			case 12170:
			case 12524: // Summoning cape
				player.setNextAnimation(new Animation(8525));
				player.setNextGraphics(new Graphics(1515));
				break;
			case 9948:
			case 9949:
			case 10646: // Hunter cape
				player.setNextAnimation(new Animation(5158));
				player.setNextGraphics(new Graphics(907));
				break;
			case 9813:
			case 10662: // Quest cape
				player.setNextAnimation(new Animation(4945));
				player.setNextGraphics(new Graphics(816));
				break;
			case 18508:
			case 18509: // Dungeoneering cape
				final int rand = (int) (Math.random() * (2 + 1));
				player.setNextAnimation(new Animation(13190));
				player.setNextGraphics(new Graphics(2442));

				LinkedTaskSequence seq = new LinkedTaskSequence();
				seq.connect(1, () -> {
					player.setNextAnimation(new Animation(((rand > 0 ? 13192 : (rand == 2 ? 13193 : 13194)))));
				}).connect(2, () -> {
					player.getAppearence().transformIntoNPC((rand == 0 ? 11229 : (rand == 2 ? 11228 : 11227)));
				}).connect(3, () -> {
					player.getAppearence().transformIntoNPC(-1);
				}).start();
				break;
			case 19709:
			case 19710: // Master dungeoneering cape
				/*
				 * WorldTasksManager.schedule(new WorldTask() { int step; private NPC dung1,
				 * dung2, dung3, dung4;
				 * 
				 * @Override public void run() { if (step == 1) {
				 * player.getAppearence().transformIntoNPC(11229); player.setNextAnimation(new
				 * Animation(14608)); dung1 = new NPC(-1, new WorldTile(player.getX(),
				 * player.getY() -1, player.getPlane()), -1, true);
				 * player.setNextFaceEntity(dung1); dung1.setLocation(dung1);
				 * dung1.setNextGraphics(new Graphics(2777)); dung2 = new NPC(-1, new
				 * WorldTile(player.getX()+1, player.getY()-1, player.getPlane()), -1, true); }
				 * if (step == 2) { player.setNextFaceEntity(null); dung1.finish();
				 * player.getAppearence().transformIntoNPC(11228); dung2.setLocation(dung2);
				 * player.setNextAnimation(new Animation(14609)); player.setNextGraphics(new
				 * Graphics(2782)); dung2.setNextGraphics(new Graphics(2778)); dung3 = new
				 * NPC(-1, new WorldTile(player.getX(), player.getY()-1, player.getPlane()), -1,
				 * true); dung4 = new NPC(-1, new WorldTile(player.getX(), player.getY()+1,
				 * player.getPlane()), -1, true); } if (step == 3) { dung2.finish();
				 * player.getAppearence().transformIntoNPC(11227); dung3.setLocation(dung3);
				 * dung4.setLocation(dung4); dung4.setNextFaceEntity(player);
				 * player.setNextAnimation(new Animation(14610)); dung3.setNextGraphics(new
				 * Graphics(2779)); dung4.setNextGraphics(new Graphics(2780)); } if (step > 4) {
				 * dung4.setNextFaceEntity(null); player.getAppearence().transformIntoNPC(-1);
				 * dung3.finish(); dung4.finish(); stop(); } step++; } }, 0, 1);
				 */
				break;
			case 20763: // Veteran cape
				if (player.getCurrentActivity().isPresent()) {
					player.getPackets().sendGameMessage("You cannot do this here!");
					return;
				}
				player.setNextAnimation(new Animation(352));
				player.setNextGraphics(new Graphics(1446));
				break;
			case 20765: // Classic cape
				if (player.getCurrentActivity().isPresent()) {
					player.getPackets().sendGameMessage("You cannot do this here!");
					return;
				}
				int random = Utils.getRandom(2);
				player.setNextAnimation(new Animation(122));
				player.setNextGraphics(new Graphics(random == 0 ? 1471 : 1466));
				break;
			case 20767: // Max cape
				 NPC npc;
				if (player.getCurrentActivity().isPresent()) {
					player.getPackets().sendGameMessage("You can't do this here.");
					return;
				}
				int size = NPCDefinitions.getNPCDefinitions(1224).size;
				WorldTile spawnTile = new WorldTile(new WorldTile(player.getX() + 1, player.getY(), player.getPlane()));
				if (!TileAttributes.floorAndWallsFree(spawnTile, player.getSize())) {
					spawnTile = null;
					int[][] dirs = Utils.getCoordOffsetsNear(size);
					for (int dir = 0; dir < dirs[0].length; dir++) {
						final WorldTile tile = new WorldTile(new WorldTile(player.getX() + dirs[0][dir],
								player.getY() + dirs[1][dir], player.getPlane()));
						if (TileAttributes.floorAndWallsFree(spawnTile, player.getSize())) {
							spawnTile = tile;
							break;
						}
					}
				}
				if (spawnTile == null) {
					player.getPackets().sendGameMessage("Need more space to perform this skillcape emote.");
					return;
				}
				player.setNextEmoteEnd(Utils.currentTimeMillis() + (25 * 600));
				final WorldTile npcTile = spawnTile;
				LinkedTaskSequence maxCapeSeq = new LinkedTaskSequence();
				npc = new NPC(1224, npcTile, false);
				maxCapeSeq.connect(1, () -> {
					npc.setNextAnimation(new Animation(1434));
					npc.setNextGraphics(new Graphics(1482));
					player.setNextAnimation(new Animation(1179));
					npc.setNextFaceEntity(player);
					player.setNextFaceEntity(npc);
				});
				maxCapeSeq.connect(2, () -> {
					npc.setNextAnimation(new Animation(1436));
					npc.setNextGraphics(new Graphics(1486));
					player.setNextAnimation(new Animation(1180));
				});
				maxCapeSeq.connect(3, () -> {
					npc.setNextGraphics(new Graphics(1498));
					player.setNextAnimation(new Animation(1181));
				});
				maxCapeSeq.connect(4, () -> player.setNextAnimation(new Animation(1182)));
				maxCapeSeq.connect(5, () -> {
					npc.setNextAnimation(new Animation(1448));
					player.setNextAnimation(new Animation(1250));
				});
				maxCapeSeq.connect(6, () -> {
					player.setNextAnimation(new Animation(1251));
					player.setNextGraphics(new Graphics(1499));
					npc.setNextAnimation(new Animation(1454));
					npc.setNextGraphics(new Graphics(1504));
				});
				maxCapeSeq.connect(11, () -> {
					player.setNextAnimation(new Animation(1291));
					player.setNextGraphics(new Graphics(1686));
					player.setNextGraphics(new Graphics(1598));
					npc.setNextAnimation(new Animation(1440));
				});
				maxCapeSeq.connect(12, () -> npc.finish());
				maxCapeSeq.start();
				break;
			case 20769:
			case 20771:
				if (!TileAttributes.floorAndWallsFree(player, 3)) {
					player.getPackets().sendGameMessage("Need more space to perform this skillcape emote.");
					return;
				} else if (player.getCurrentActivity().isPresent()) {
					player.getPackets().sendGameMessage("You can't do this here.");
					return;
				}
				player.setNextEmoteEnd(Utils.currentTimeMillis() + (20 * 600));

				LinkedTaskSequence compCapeSeq = new LinkedTaskSequence();

				compCapeSeq.connect(1, () -> {
					player.setNextAnimation(new Animation(356));
					player.setNextGraphics(new Graphics(307));
				});
				compCapeSeq.connect(2, () -> {
					player.getAppearence().transformIntoNPC(capeId == 20769 ? 1830 : 3372);
					player.setNextAnimation(new Animation(1174));
					player.setNextGraphics(new Graphics(1443));
				});
				compCapeSeq.connect(4, () -> player.getPackets().sendCameraShake(3, 25, 50, 25, 50));
				compCapeSeq.connect(5, () -> player.getPackets().sendStopCameraShake());
				compCapeSeq.connect(6, () -> {
					player.getAppearence().transformIntoNPC(-1);
					player.setNextAnimation(new Animation(1175));
				});
				compCapeSeq.start();
				break;
			}
			return;
		}
		if (!Emote.isDoingEmote(player))
			Emote.setNextEmoteEnd(player);
	}
	public static void unlockEmotesBook(Player player) {
		player.getPackets().sendAccessMask(590, 8, 0, 121, 0, 1);
	}

	public void init(Player player) {
		refreshListConfigs(player);
	}

	public static void refreshListConfigs(Player player) {
		player.getPackets().sendConfig(465, 7); // goblin quest emotes
		int value1 = 0;
		value1 += 1;
		value1 += 2;
		value1 += 4;
		value1 += 8;
		if (value1 > 0)
			player.getPackets().sendConfig(802, value1); // stronghold of
		// security emotes player.getPackets().sendConfig(1085, 249852); // hallowen
		// hand
		// emote
		int value2 = 0;
		value2 += 1;
		value2 += 2;
		value2 += 4;
		value2 += 8;
		value2 += 16;
		value2 += 32;
		value2 += 64;
		value2 += 128;
		value2 += 256;
		value2 += 512;
		value2 += 1024;
		value2 += 2048;
		value2 += 4096;
		value2 += 8192;
		value2 += 16384;
		value2 += 32768;
		if (value2 > 0)
			player.getPackets().sendConfig(313, value2); //
		player.getPackets().sendConfig(313, 1);
		player.getPackets().sendConfig(818, 1);
		player.getPackets().sendConfig(465, 7);
		player.getPackets().sendConfig(802, -1);
		player.getPackets().sendConfig(1085, 249852);
		player.getPackets().sendConfig(313, -1);
		player.getPackets().sendConfig(2033, 1043648799);
		player.getPackets().sendConfig(2032, 7341);
		player.getPackets().sendConfig(1921, -893736236);
		player.getPackets().sendConfig(1404, 123728213);
		player.getPackets().sendConfig(2169, -1);
		player.getPackets().sendConfig(2230, -1);
		player.getPackets().sendConfig(1597, -1);
		player.getPackets().sendConfig(1842, -1);
		player.getPackets().sendConfig(2432, -1);
		player.getPackets().sendConfig(1958, 534);
		player.getPackets().sendConfig(2405, -1);
		player.getPackets().sendConfig(2458, -1);
//		for (int i = 0; i < 17; i++)
//			player.getVarsManager().sendVarBit(1171 + i, 1);
//		for (int i = 0; i < 31; i++)
//			player.getVarsManager().sendVarBit(20214 + i, 1);
//		for (int i = 0; i < 4; i++)
//			player.getVarsManager().sendVarBit(25838 + i, 1);
	}
	
	public static void setNextEmoteEnd(Player player) {
		player.setNextEmoteEnd(player.getLastAnimationEnd() - 600);
	}
}