package com.jupiter.network.encoders.other;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jupiter.Settings;
import com.jupiter.cache.io.OutputStream;
import com.jupiter.combat.npc.NPC;
import com.jupiter.game.HitBar;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;

public final class LocalNPCUpdate {

	private transient Player player;
	private LinkedList<NPC> localNPCs;

	public void reset() {
		localNPCs.clear();
	}

	public LocalNPCUpdate(Player player) {
		this.player = player;
		localNPCs = new LinkedList<NPC>();
	}

	public OutputStream createPacketAndProcess() {
		boolean largeSceneView = player.isLargeSceneView();
		OutputStream stream = new OutputStream();
		OutputStream updateBlockData = new OutputStream();
		stream.writePacketVarShort(player, largeSceneView ? 47 : 6);
		processLocalNPCsInform(stream, updateBlockData, largeSceneView);
		stream.writeBytes(updateBlockData.getBuffer(), 0, updateBlockData.getOffset());
		stream.endPacketVarShort();
		return stream;
	}

	private void processLocalNPCsInform(OutputStream stream, OutputStream updateBlockData, boolean largeSceneView) {
		stream.initBitAccess();
		processInScreenNPCs(stream, updateBlockData, largeSceneView);
		addInScreenNPCs(stream, updateBlockData, largeSceneView);
		if (updateBlockData.getOffset() > 0)
			stream.writeBits(15, 32767);
		stream.finishBitAccess();
	}

	private void processInScreenNPCs(OutputStream stream, OutputStream updateBlockData, boolean largeSceneView) {
		stream.writeBits(8, localNPCs.size());
		// for (NPC n : localNPCs.toArray(new NPC[localNPCs.size()])) {
		for (Iterator<NPC> it = localNPCs.iterator(); it.hasNext();) {
			NPC n = it.next();
			if (n.hasFinished() || !n.withinDistance(player, largeSceneView ? 126 : 14) || n.hasTeleported()) {
				stream.writeBits(1, 1);
				stream.writeBits(2, 3);
				it.remove();
				continue;
			}
			boolean needUpdate = n.needMasksUpdate();
			boolean walkUpdate = n.getNextWalkDirection() != null;
			stream.writeBits(1, (needUpdate || walkUpdate) ? 1 : 0);
			if (walkUpdate) {
				stream.writeBits(2, n.getNextRunDirection() == null? 1 : 2);
				if (n.getNextRunDirection() != null)
					stream.writeBits(1, 1);
				stream.writeBits(3, n.getNextWalkDirection().getId());
				if (n.getNextRunDirection() != null)
					stream.writeBits(3, n.getNextWalkDirection().getId());
				stream.writeBits(1, needUpdate ? 1 : 0);
			} else if (needUpdate)
				stream.writeBits(2, 0);
			if (needUpdate)
				appendUpdateBlock(n, updateBlockData, false);
			
		}
	}

	private void addInScreenNPCs(OutputStream stream, OutputStream updateBlockData, boolean largeSceneView) {
		for (int regionId : player.getMapRegionsIds()) {
			List<Integer> indexes = World.getRegion(regionId).getNPCsIndexes();
			if (indexes == null)
				continue;
			for (int npcIndex : indexes) {
				if (localNPCs.size() == Settings.LOCAL_NPCS_LIMIT)
					break;
				NPC n = World.getNPCs().get(npcIndex);
				if (n == null || n.hasFinished() || localNPCs.contains(n) || !n.withinDistance(player, largeSceneView ? 126 : 14) || n.isDead())
					continue;

				stream.writeBits(15, n.getIndex());
				boolean needUpdate = n.needMasksUpdate() || n.getLastFaceEntity() != -1;
				int x = n.getX() - player.getX();
				int y = n.getY() - player.getY();
				if (largeSceneView) {
					if (x < 127)
						x += 256;
					if (y < 127)
						y += 256;
				} else {
					if (x < 15)
						x += 32;
					if (y < 15)
						y += 32;
				}
				stream.writeBits(1, needUpdate ? 1 : 0);
				stream.writeBits(largeSceneView ? 8 : 5, y);
				stream.writeBits(3, (n.direction >> 11) - 4);
				stream.writeBits(15, n.getId());
				stream.writeBits(largeSceneView ? 8 : 5, x);
				stream.writeBits(1, n.hasTeleported() ? 1 : 0);
				stream.writeBits(2, n.getPlane());
				localNPCs.add(n);
				if (needUpdate)
					appendUpdateBlock(n, updateBlockData, true);
			}
		}
	}

	private void appendUpdateBlock(NPC n, OutputStream data, boolean added) {
		int maskData = 0;

		if (n.getNextAnimation() != null) {
			maskData |= 0x10;
		}
		if (n.getNextForceMovement() != null) {
			maskData |= 0x400;
		}
		if (n.getNextGraphics4() != null) {
			maskData |= 0x1000000;
		}
		if (n.getNextFaceEntity() != -2 || (added && n.getLastFaceEntity() != -1)) {
			maskData |= 0x80;
		}
		if (n.getNextGraphics2() != null) {
			maskData |= 0x800;
		}
		if (!n.getNextHits().isEmpty()) {
			maskData |= 0x1;
		}
		if (n.getNextTransformation() != null) {
			maskData |= 0x8;
		}
		if (n.getNextGraphics3() != null) {
			maskData |= 0x2000000;
		}
		if (n.isChangedCombatLevel() || (added && n.getCustomCombatLevel() >= 0)) {
			maskData |= 0x10000;
		}
		if (n.getNextFaceWorldTile() != null && n.getNextRunDirection() == null && n.getNextWalkDirection() == null) {
			maskData |= 0x4;
		}
		if (n.isChangedName() || (added && n.getCustomName() != null)) {
			maskData |= 0x800000;
		}
		if (n.getNextForceTalk() != null) {
			maskData |= 0x2;
		}
		if (n.getNextGraphics1() != null) {
			maskData |= 0x20;
		}

		if (maskData > 0xff)
			maskData |= 0x40;
		if (maskData > 0xffff)
			maskData |= 0x2000;
		if (maskData > 0xffffff)
			maskData |= 0x100000;

		data.writeByte(maskData);

		if (maskData > 0xff)
			data.writeByte(maskData >> 8);
		if (maskData > 0xffff)
			data.writeByte(maskData >> 16);
		if (maskData > 0xffffff)
			data.writeByte(maskData >> 24);

		if (n.getNextAnimation() != null) {
			applyAnimationMask(n, data);
		}
		if (n.getNextForceMovement() != null) {
			applyForceMovementMask(n, data);
		}
		if (n.getNextGraphics4() != null) {
			applyGraphicsMask4(n, data);
		}
		if (n.getNextFaceEntity() != -2 || (added && n.getLastFaceEntity() != -1)) {
			applyFaceEntityMask(n, data);
		}
		if (n.getNextGraphics2() != null) {
			applyGraphicsMask2(n, data);
		}
		if (!n.getNextHits().isEmpty()) {
			applyHitMask(n, data);
		}
		if (n.getNextTransformation() != null) {
			applyTransformationMask(n, data);
		}
		if (n.getNextGraphics3() != null) {
			applyGraphicsMask3(n, data);
		}
		if (n.isChangedCombatLevel() || (added && n.getCustomCombatLevel() >= 0)) {
			applyChangeLevelMask(n, data);
		}
		if (n.getNextFaceWorldTile() != null) {
			applyFaceWorldTileMask(n, data);
		}
		if (n.isChangedName() || (added && n.getCustomName() != null)) {
			applyNameChangeMask(n, data);
		}
		if (n.getNextForceTalk() != null) {
			applyForceTalkMask(n, data);
		}
		if (n.getNextGraphics1() != null) {
			applyGraphicsMask1(n, data);
		}
	}

	private void applyChangeLevelMask(NPC n, OutputStream data) {
		data.writeShort128(n.getCombatLevel());
	}

	private void applyNameChangeMask(NPC npc, OutputStream data) {
		data.writeString(npc.getName());
	}

	private void applyTransformationMask(NPC n, OutputStream data) {
		data.writeBigSmart(n.getNextTransformation().getToNPCId());
	}

	private void applyForceTalkMask(NPC n, OutputStream data) {
		data.writeString(n.getNextForceTalk().getText());
	}

	private void applyForceMovementMask(NPC n, OutputStream data) {
		data.write128Byte(n.getNextForceMovement().getToFirstTile().getX() - n.getX());
		data.writeByte128(n.getNextForceMovement().getToFirstTile().getY() - n.getY());
		data.writeByteC(n.getNextForceMovement().getToSecondTile() == null ? 0 : n.getNextForceMovement().getToSecondTile().getX() - n.getX());
		data.writeByte128(n.getNextForceMovement().getToSecondTile() == null ? 0 : n.getNextForceMovement().getToSecondTile().getY() - n.getY());
		data.writeShortLE((n.getNextForceMovement().getFirstTileTicketDelay() * 600) / 20);
		data.writeShortLE(n.getNextForceMovement().getToSecondTile() == null ? 0 : ((n.getNextForceMovement().getSecondTileTicketDelay() * 600) / 20));
		data.writeShortLE128(n.getNextForceMovement().getDirection());
	}

	private void applyFaceWorldTileMask(NPC n, OutputStream data) {
		data.writeShortLE128((n.getNextFaceWorldTile().getX() << 1) + 1);
		data.writeShortLE((n.getNextFaceWorldTile().getY() << 1) + 1);
	}

	private void applyHitMask(NPC n, OutputStream data) {
		data.writeByte128(n.getNextHits().size());
		for (Hit hit : n.getNextHits().toArray(new Hit[n.getNextHits().size()])) {
			boolean interactingWith = hit.interactingWith(player, n);
			if (hit.missed() && !interactingWith) {
				data.writeSmart(32766);
				data.writeByte(hit.getDamage());
			} else {
				if (hit.getSoaking() != null) {
					data.writeSmart(32767);
					data.writeSmart(hit.getMark(player, n));
					data.writeSmart(hit.getDamage());
					data.writeSmart(hit.getSoaking().getMark(player, n));
					data.writeSmart(hit.getSoaking().getDamage());
				} else {
					data.writeSmart(hit.getMark(player, n));
					data.writeSmart(hit.getDamage());
				}
			}
			data.writeSmart(hit.getDelay());
		}
		data.writeByte128(n.getNextBars().size());
		for (HitBar bar : n.getNextBars()) {
			data.writeSmart(bar.getType());
			int perc = bar.getPercentage();
			int toPerc = bar.getToPercentage();
			boolean display = bar.display(player);
			data.writeSmart(display ? perc != toPerc ? bar.getTimer() : 0 : 32767);
			if (display) {
				data.writeSmart(bar.getDelay());
				data.write128Byte(perc);
				if (toPerc != perc)
					data.writeByte128(toPerc);
			}
		}
	}

	private void applyFaceEntityMask(NPC n, OutputStream data) {
		data.writeShortLE(n.getNextFaceEntity() == -2 ? n.getLastFaceEntity() : n.getNextFaceEntity());
	}

	private void applyAnimationMask(NPC n, OutputStream data) {
		for (int id : n.getNextAnimation().getIds())
			data.writeBigSmart(id);
		data.writeByte(n.getNextAnimation().getSpeed());
	}

	private void applyGraphicsMask4(NPC n, OutputStream data) {
		data.writeShort128(n.getNextGraphics4().getId());
		data.writeIntV2(n.getNextGraphics4().getSettingsHash());
		data.writeByte128(n.getNextGraphics4().getSettings2Hash());
	}

	private void applyGraphicsMask3(NPC n, OutputStream data) {
		data.writeShortLE(n.getNextGraphics3().getId());
		data.writeIntLE(n.getNextGraphics3().getSettingsHash());
		data.writeByte(n.getNextGraphics3().getSettings2Hash());
	}

	private void applyGraphicsMask2(NPC n, OutputStream data) {
		data.writeShortLE(n.getNextGraphics2().getId());
		data.writeIntLE(n.getNextGraphics2().getSettingsHash());
		data.writeByte128(n.getNextGraphics2().getSettings2Hash());
	}

	private void applyGraphicsMask1(NPC n, OutputStream data) {
		data.writeShort(n.getNextGraphics1().getId());
		data.writeIntV2(n.getNextGraphics1().getSettingsHash());
		data.writeByteC(n.getNextGraphics1().getSettings2Hash());
	}

}
