package com.runescape.game.world.entity.player;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.ItemsEquipIds;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.item.Item;
import com.runescape.network.stream.OutputStream;
import com.runescape.utility.Utils;

import java.io.Serializable;
import java.util.Arrays;

public class Appearence implements Serializable {

	private static final long serialVersionUID = 7655608569741626586L;

	private int title;

	private int[] look;

	private byte[] colour;

	private boolean male;

	private transient int renderEmote;

	private transient boolean glowRed;

	private transient byte[] appeareanceData;

	private transient byte[] md5AppeareanceDataHash;

	private transient short transformedNpcId;

	private boolean hidePlayer;

	private transient Player player;

	public Appearence() { }

	public Appearence setAppearance() {
		male = true;
		renderEmote = -1;
		title = -1;
		resetAppearence();
		return this;
	}

	public void resetAppearence() {
		look = new int[7];
		colour = new byte[10];
		male();
	}

	public void male() {
		look[0] = 3; // Hair
		look[1] = 14; // Beard
		look[2] = 18; // Torso
		look[3] = 26; // Arms
		look[4] = 34; // Bracelets
		look[5] = 38; // Legs
		look[6] = 42; // Shoes~
		colour[2] = 16;
		colour[1] = 16;
		colour[0] = 3;
		male = true;
	}

	public Appearence setDefaultAppearance() {
		getLook()[0] = 4;
		getLook()[1] = 98;
		getLook()[2] = 24;
		getLook()[3] = 26;
		getLook()[4] = 34;
		getLook()[5] = 625;
		getLook()[6] = 42;

		getColour()[0] = 1;
		getColour()[1] = -105;
		getColour()[2] = -50;

		male = true;
		return this;
	}

	public void setGlowRed(boolean glowRed) {
		this.glowRed = glowRed;
		generateAppearenceData();
	}

	public void generateAppearenceData() {
		OutputStream stream = new OutputStream();
		int flag = 0;
		if (!male) {
			flag |= 0x1;
		}
		if (transformedNpcId >= 0 && NPCDefinitions.getNPCDefinitions(transformedNpcId).aBoolean3190) {
			flag |= 0x2;
		}
		stream.writeByte(flag);
		stream.writeByte(title); // mobi arms titles
		stream.writeByte(player.hasSkull() ? player.getSkullId() : -1);
		stream.writeByte(player.getPrayer().getPrayerHeadIcon()); // prayer icon
		stream.writeByte(hidePlayer ? 1 : 0);
		if (transformedNpcId >= 0) {
			stream.writeShort(-1); // 65535 tells it a npc
			stream.writeShort(transformedNpcId);
			stream.writeByte(0);
		} else {
			for (int index = 0; index < 4; index++) {
				Item item = player.getEquipment().getItems().get(index);
				if (glowRed) {
					if (index == 0) {
						stream.writeShort(32768 + ItemsEquipIds.getEquipId(2910));
						continue;
					}
					if (index == 1) {
						stream.writeShort(32768 + ItemsEquipIds.getEquipId(14641));
						continue;
					}
				}
				if (item == null) {
					stream.writeByte(0);
				} else {
					stream.writeShort(32768 + item.getEquipId());
				}
			}
			Item item = player.getEquipment().getItems().get(Equipment.SLOT_CHEST);
			stream.writeShort(item == null ? 0x100 + look[2] : 32768 + item.getEquipId());
			item = player.getEquipment().getItems().get(Equipment.SLOT_SHIELD);
			if (item == null) {
				stream.writeByte(0);
			} else {
				stream.writeShort(32768 + item.getEquipId());
			}
			item = player.getEquipment().getItems().get(Equipment.SLOT_CHEST);
			if (item == null || !Equipment.hideArms(item)) {
				stream.writeShort(0x100 + look[3]);
			} else {
				stream.writeByte(0);
			}
			item = player.getEquipment().getItems().get(Equipment.SLOT_LEGS);
			stream.writeShort(glowRed ? 32768 + ItemsEquipIds.getEquipId(2908) : item == null ? 0x100 + look[5] : 32768 + item.getEquipId());
			item = player.getEquipment().getItems().get(Equipment.SLOT_HAT);
			if (!glowRed && (item == null || !Equipment.hideHair(item))) {
				stream.writeShort(0x100 + look[0]);
			} else {
				stream.writeByte(0);
			}
			item = player.getEquipment().getItems().get(Equipment.SLOT_HANDS);
			stream.writeShort(glowRed ? 32768 + ItemsEquipIds.getEquipId(2912) : item == null ? 0x100 + look[4] : 32768 + item.getEquipId());
			item = player.getEquipment().getItems().get(Equipment.SLOT_FEET);
			stream.writeShort(glowRed ? 32768 + ItemsEquipIds.getEquipId(2904) : item == null ? 0x100 + look[6] : 32768 + item.getEquipId());
			item = player.getEquipment().getItems().get(male ? Equipment.SLOT_HAT : Equipment.SLOT_CHEST);
			if (item == null || (male && Equipment.showBeard(item))) {
				stream.writeShort(0x100 + look[1]);
			} else {
				stream.writeByte(0);
			}
			item = player.getEquipment().getItems().get(Equipment.SLOT_AURA);
			if (item == null) {
				stream.writeByte(0);
			} else {
				stream.writeShort(32768 + item.getEquipId());
			}
			int pos = stream.getOffset();
			stream.writeShort(0);
			int hash = 0;
			int slotFlag = -1;
			for (int slotId = 0; slotId < player.getEquipment().getItems().getSize(); slotId++) {
				if (Equipment.DISABLED_SLOTS[slotId] != 0) {
					continue;
				}
				slotFlag++;
				if (slotId == Equipment.SLOT_HAT) {
					int hatId = player.getEquipment().getHatId();
					if (hatId == 20768 || hatId == 20770 || hatId == 20772) {
						ItemDefinitions defs = ItemDefinitions.getItemDefinitions(hatId - 1);
						if ((hatId == 20768 && Arrays.equals(player.getMaxedCapeCustomized(), defs.originalModelColors) || ((hatId == 20770 || hatId == 20772) && Arrays.equals(player.getCompletionistCapeCustomized(), defs.originalModelColors)))) {
							continue;
						}
						hash |= 1 << slotFlag;
						stream.writeByte(0x4); // modify 4 model colors
						int[] hat = hatId == 20768 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
						int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
						stream.writeShort(slots);
						for (int i = 0; i < 4; i++) {
							stream.writeShort(hat[i]);
						}
					}
				} else if (slotId == Equipment.SLOT_CAPE) {
					int capeId = player.getEquipment().getCapeId();
					if (capeId == 20767 || capeId == 20769 || capeId == 20771) {
						ItemDefinitions defs = ItemDefinitions.getItemDefinitions(capeId);
						if ((capeId == 20767 && Arrays.equals(player.getMaxedCapeCustomized(), defs.originalModelColors) || ((capeId == 20769 || capeId == 20771) && Arrays.equals(player.getCompletionistCapeCustomized(), defs.originalModelColors)))) {
							continue;
						}
						hash |= 1 << slotFlag;
						stream.writeByte(0x4); // modify 4 model colors
						int[] cape = capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
						int slots = 0 | 1 << 4 | 2 << 8 | 3 << 12;
						stream.writeShort(slots);
						for (int i = 0; i < 4; i++) {
							if (cape[i] == -1) {
								continue;
							}
							stream.writeShort(cape[i]);
						}
					}
				} else if (slotId == Equipment.SLOT_AURA) {
					int auraId = player.getEquipment().getAuraId();
					if (auraId == -1 || !player.getAuraManager().isActivated()) {
						continue;
					}
					ItemDefinitions auraDefs = ItemDefinitions.getItemDefinitions(auraId);
					if (auraDefs.getMaleWornModelId1() == -1 || auraDefs.getFemaleWornModelId1() == -1) {
						continue;
					}
					hash |= 1 << slotFlag;
					stream.writeByte(0x1); // modify model ids
					int modelId = player.getAuraManager().getAuraModelId();
					stream.writeBigSmart(modelId); // male modelid1
					stream.writeBigSmart(modelId); // female modelid1
					if (auraDefs.getMaleWornModelId2() != -1 || auraDefs.getFemaleWornModelId2() != -1) {
						int modelId2 = player.getAuraManager().getAuraModelId();
						stream.writeBigSmart(modelId2);
						stream.writeBigSmart(modelId2);
					}
				}
			}
			int pos2 = stream.getOffset();
			stream.setOffset(pos);
			stream.writeShort(hash);
			stream.setOffset(pos2);
		}

		for (byte element : colour) {
			stream.writeByte(element);
		}
		stream.writeShort(getRenderEmote());
		stream.writeString(player.getDisplayName());
		boolean pvpArea = player.isCanPvp() && player.getControllerManager().getController() instanceof Wilderness && player.getFamiliar() == null;
		stream.writeByte(pvpArea ? player.getSkills().getCombatLevel() : player.getSkills().getCombatLevelWithSummoning());
		stream.writeByte(pvpArea ? player.getSkills().getCombatLevelWithSummoning() : 0);
		stream.writeByte(-1); // higher level acc name appears in front :P
		stream.writeByte(transformedNpcId >= 0 ? 1 : 0); // to end here else id
		// need to send more
		// data
		if (transformedNpcId >= 0) {
			NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(transformedNpcId);
			stream.writeShort(defs.anInt876);
			stream.writeShort(defs.anInt842);
			stream.writeShort(defs.anInt884);
			stream.writeShort(defs.anInt875);
			stream.writeByte(defs.anInt875);
		}
		byte[] appeareanceData = new byte[stream.getOffset()];
		System.arraycopy(stream.getBuffer(), 0, appeareanceData, 0, appeareanceData.length);
		byte[] md5Hash = Utils.encryptUsingMD5(appeareanceData);
		this.appeareanceData = appeareanceData;
		md5AppeareanceDataHash = md5Hash;
	}

	public int getRenderEmote() {
		if (renderEmote >= 0) {
			return renderEmote;
		}
		if (transformedNpcId >= 0) {
			return NPCDefinitions.getNPCDefinitions(transformedNpcId).renderEmote;
		}
		return player.getEquipment().getWeaponRenderEmote();
	}

	public void setRenderEmote(int id) {
		this.renderEmote = id;
		generateAppearenceData();
	}

	public void setPlayer(Player player) {
		this.player = player;
		transformedNpcId = -1;
		renderEmote = -1;
		if (look == null) {
			resetAppearence();
		}
	}

	public void transformIntoNPC(int id) {
		transformedNpcId = (short) id;
		generateAppearenceData();
	}

	public void switchHidden() {
		hidePlayer = !hidePlayer;
		generateAppearenceData();
	}

	public boolean isHidden() {
		return hidePlayer;
	}

	public void setHidden(boolean hide) {
		hidePlayer = hide;
		generateAppearenceData();
	}

	public int getSize() {
		if (transformedNpcId >= 0) {
			return NPCDefinitions.getNPCDefinitions(transformedNpcId).size;
		}
		return 1;
	}

	public void female() {
		look[0] = 48; // Hair
		look[1] = 57; // Beard
		look[2] = 57; // Torso
		look[3] = 65; // Arms
		look[4] = 68; // Bracelets
		look[5] = 77; // Legs
		look[6] = 80; // Shoes

		colour[2] = 16;
		colour[1] = 16;
		colour[0] = 3;
		male = false;
	}

	public byte[] getAppeareanceData() {
		return appeareanceData;
	}

	public byte[] getMD5AppeareanceDataHash() {
		return md5AppeareanceDataHash;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public void setLook(int i, int i2) {
		look[i] = i2;
	}

	public void setColor(int i, int i2) {
		colour[i] = (byte) i2;
	}

	public int getTopStyle() {
		return look[2];
	}

	public void setTopStyle(int i) {
		look[2] = i;
	}

	public void setArmsStyle(int i) {
		look[3] = i;
	}

	public void setWristsStyle(int i) {
		look[4] = i;
	}

	public void setLegsStyle(int i) {
		look[5] = i;
	}

	public void setBootsStyle(int i) {
		look[6] = i;
	}

	public int getHairStyle() {
		return look[0];
	}

	public void setHairStyle(int i) {
		look[0] = i;
	}

	public int getBeardStyle() {
		return look[1];
	}

	public void setBeardStyle(int i) {
		look[1] = i;
	}

	public int getFacialHair() {
		return look[1];
	}

	public void setFacialHair(int i) {
		look[1] = i;
	}

	public void setBootsColor(int color) {
		colour[3] = (byte) color;
	}

	public int getSkinColor() {
		return colour[4];
	}

	public void setSkinColor(int color) {
		colour[4] = (byte) color;
	}

	public void setTopColor(int color) {
		colour[1] = (byte) color;
	}

	public void setLegsColor(int color) {
		colour[2] = (byte) color;
	}

	public int getHairColor() {
		return colour[0];
	}

	public void setHairColor(int color) {
		colour[0] = (byte) color;
	}

	public void setTitle(int title) {
		this.title = title;
		generateAppearenceData();
	}

	public boolean isNPC() {
		return transformedNpcId != -1;
	}

	public void setLooks(short[] look) {
		for (byte i = 0; i < this.look.length; i = (byte) (i + 1)) {
			if (look[i] != -1) {
				this.look[i] = look[i];
			}
		}
	}

	public void copyColors(short[] colors) {
		for (byte i = 0; i < this.colour.length; i = (byte) (i + 1)) {
			if (colors[i] != -1) {
				this.colour[i] = (byte) colors[i];
			}
		}
	}

    public int[] getLook() {
        return this.look;
    }

    public byte[] getColour() {
        return this.colour;
    }

    public short getTransformedNpcId() {
        return this.transformedNpcId;
    }
}