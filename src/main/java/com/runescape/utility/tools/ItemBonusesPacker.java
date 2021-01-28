package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.utility.Utils;

import java.io.*;

public class ItemBonusesPacker {

	public static final void main(String[] args) throws IOException {
		Cache.init();
		DataOutputStream out = new DataOutputStream(new FileOutputStream(
				"data/items/bonuses.ib"));
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			File file = new File("bonuses/" + itemId + ".txt");
			if (file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				out.writeShort(itemId);
				reader.readLine();
				// att bonuses
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				reader.readLine();
				// def bonuses
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				reader.readLine();
				// Damage absorption
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				reader.readLine();
				// Other bonuses
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				out.writeShort(Integer.valueOf(reader.readLine()));
				if (reader.readLine() != null)
					throw new RuntimeException("Should be null line" + itemId);
			}
		}
		out.flush();
		out.close();
	}

}
