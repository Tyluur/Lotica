package com.runescape.game.content.skills;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

public final class Combat {

	public static boolean hasAntiDragProtection(Entity target) {
		if (target instanceof NPC) {
			return false;
		}
		Player p2 = (Player) target;
		int shieldId = p2.getEquipment().getShieldId();
		return shieldId == 1540 || shieldId == 11283 || shieldId == 11284;
	}

	public static int getDefenceEmote(Entity target) {
		if (target instanceof NPC) {
			NPC n = (NPC) target;
			return n.getCombatDefinitions().getDefenceEmote();
		} else {
			Player p = (Player) target;
			int shieldId = p.getEquipment().getShieldId();
			String shieldName = shieldId == -1 ? null : ItemDefinitions.forId(shieldId).getName().toLowerCase();
			if (shieldId == -1 || (shieldName.contains("book") && shieldId != 18346)) {
				int weaponId = p.getEquipment().getWeaponId();
				if (weaponId == -1) {
					return 424;
				}
				String weaponName = ItemDefinitions.forId(weaponId).getName().toLowerCase();
				if (!weaponName.equals("null")) {
					if (weaponName.contains("scimitar") || weaponName.contains("korasi sword")) {
						return 15074;
					}
					if (weaponName.contains("whip")) {
						return 11974;
					}
					if (weaponName.contains("staff of light")) {
						return 12806;
					}
					if (weaponName.contains("longsword") || weaponName.contains("darklight") || weaponName.contains("silverlight") || weaponName.contains("excalibur")) {
						return 388;
					}
					if (weaponName.contains("dagger")) {
						return 378;
					}
					if (weaponName.contains("rapier")) {
						return 13038;
					}
					if (weaponName.contains("pickaxe")) {
						return 397;
					}
					if (weaponName.contains("mace")) {
						return 403;
					}
					if (weaponName.contains("claws")) {
						return 4177;
					}
					if (weaponName.contains("hatchet") || weaponName.contains("battleaxe")) {
						return 397;
					}
					if (weaponName.contains("greataxe")) {
						return 12004;
					}
					if (weaponName.contains("wand")) {
						return 415;
					}
					if (weaponName.contains("chaotic staff")) {
						return 13046;
					}
					if (weaponName.contains("staff")) {
						return 420;
					}
					if (weaponName.contains("warhammer") || weaponName.contains("tzhaar-ket-em")) {
						return 403;
					}
					if (weaponName.contains("maul") || weaponName.contains("tzhaar-ket-om")) {
						return 1666;
					}
					if (weaponName.contains("zamorakian spear")) {
						return 12008;
					}
					if (weaponName.contains("spear") || weaponName.contains("halberd") || weaponName.contains("hasta")) {
						return 430;
					}
					if (weaponName.contains("2h sword") || weaponName.contains("godsword") || weaponName.equals("saradomin sword")) {
						return 7050;
					}
				}
				return 424;
			}
			if (shieldName != null) {
				if (shieldName.contains("shield") || shieldName.contains("toktz-ket-xil")) {
					return 1156;
				}
				if (shieldName.contains("defender")) {
					return 4177;
				}
			}
			switch (shieldId) {
				case -1:
				default:
					return 424;
			}
		}
	}

	private Combat() {
	}

	public static boolean rollHit(double attack, double defence) {
		double chance;
		if (attack < defence) {
			chance = (attack - 1) / (defence * 2);
		} else {
			chance = 1 - ((defence + 1) / (attack * 2));
		}
		double ratio = chance * 100;
		double accuracy = Math.floor(ratio);
		double block = Math.floor(101 - ratio);
		double acc = Math.random() * accuracy;
		double def = Math.random() * block;
//		System.out.println((acc > def) + " attack=" + attack + ", defence=" + defence + ", ratio=" + ratio + ", acc=" + acc + ", def=" + def);
		return acc > def;
	//	return attack >= 0 && (defence < 0 || Utils.random((int) (attack + defence)) > defence);
	}
}
