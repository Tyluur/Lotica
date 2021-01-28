package com.runescape.game.content.skills.farming;

import com.runescape.cache.loaders.ObjectDefinitions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public enum SpotInfo implements FarmingConstants {

	Talvery_Tree(8388, TREES),
	Falador_Garden_Tree(8389, TREES),
	Varrock_Tree(8390, TREES),
	Lumbridge_Tree(8391, TREES),
	Gnome_Tree(19147, TREES),

	Gnome_Strong_Fruit_Tree(7962, FRUIT_TREES),
	Gnome_Fruit_Tree(7963, FRUIT_TREES),
	Brimhaven_Fruit_Tree(7964, FRUIT_TREES),
	Catherby_Fruit_Tree(7965, FRUIT_TREES),
	Lletya_Fruit_Tree(28919, FRUIT_TREES),

	Falador_Allotment_North(8550, ALLOTMENT),
	Falador_Allotment_South(8551, ALLOTMENT),
	Catherby_Allotment_North(8552, ALLOTMENT),
	Catherby_Allotment_South(8553, ALLOTMENT),
	Ardougne_Allotment_North(8554, ALLOTMENT),
	Ardougne_Allotment_South(8555, ALLOTMENT),
	Canfis_Allotment_North(8556, ALLOTMENT),
	Canfis_Allotment_South(8557, ALLOTMENT),

	Yannile_Hops_Patch(8173, HOPS),
	Talvery_Hops_Patch(8174, HOPS),
	Lumbridge_Hops_Patch(8175, HOPS),
	McGrubor_Hops_Patch(8176, HOPS),

	Falador_Flower(7847, FLOWERS),
	Catherby_Flower(7848, FLOWERS),
	Ardougne_Flower(7849, FLOWERS),
	Canfis_Flower(7850, FLOWERS),

	Champions_Bush(7577, BUSHES),
	Rimmington_Bush(7578, BUSHES),
	Etceteria_Bush(7579, BUSHES),
	South_Arddougne_Bush(7580, BUSHES),

	Falador_Herb_Patch(8150, HERBS),
	Catherby_Herb_Patch(8151, HERBS),
	Ardougne_Herb_Patch(8152, HERBS),
	Canfis_Herb_Patch(8153, HERBS),

	Falador_Compost_Bin(7836, COMPOST),
	Catherby_Bin(7837, COMPOST),
	Port_Phasymatis_Bin(7838, COMPOST),
	Ardougn_Bin(7839, COMPOST),
	Taverly_Bin(66577, COMPOST),

	Mushroom_Special(8337, MUSHROOMS),

	Belladonna(7572, BELLADONNA);

	private static Map<Short, SpotInfo> informations = new HashMap<>();

	static {
		for (SpotInfo information : SpotInfo.values()) {
			informations.put((short) information.objectId, information);
		}
	}

	private int objectId;

	private int configFileId;

	private int type;

	SpotInfo(int objectId, int type) {
		this.objectId = objectId;
		this.configFileId = ObjectDefinitions.getObjectDefinitions(objectId).getConfigFileId();
		this.type = type;
	}

	public static SpotInfo getInfo(int objectId) {
		return informations.get((short) objectId);
	}

    public int getObjectId() {
        return this.objectId;
    }

    public int getConfigFileId() {
        return this.configFileId;
    }

    public int getType() {
        return this.type;
    }
}

