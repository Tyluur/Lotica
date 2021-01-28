package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class DebugCommand extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "dbg" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		/*for (int animId = 1836; animId < 1837; animId++) {
			for (int i = 0; i < 10000; i++) {
				RenderAnimDefinitions defs = RenderAnimDefinitions.getRenderAnimDefinitions(i);
				if (defs == null) { continue; }
				if (defs.anInt972 == animId || defs.anInt963 == animId) {
					System.out.println(animId + ", " + i);
				}
			}
		}*/
/*		String file = System.getProperty("user.home") + "/Desktop/potions_list.txt";
		List<String> fullPotions = Utils.getFileText(file);
		List<String> lessPotions = new ArrayList<>();
		List<String> pricesList = new ArrayList<>();
		String[] suffixes = { "(3)", "(2)", "(1)" };
		for (String line : fullPotions) {
			int openIndex = line.indexOf("(");
			String potionName = line.substring(0, openIndex).trim();
			String fullPotionName = line.substring(0, line.indexOf(")") + 1).trim();
			ItemDefinitions potionDef = ItemDefinitions.getLowestDefinitionForName(fullPotionName);
			if (potionDef == null) {
				System.out.println("Couldn't find definition for " + fullPotionName);
				continue;
			}
			Integer price = ExchangePriceLoader.getUnlimitedPrice(potionDef.getId());
			if (price == null) {
				System.out.println("No unlimited price for " + potionName);
				continue;
			}
			for (int i = 0; i < suffixes.length; i++) {
				String suffix = suffixes[i];
				String newPotion = potionName + " " + suffix;
				ItemDefinitions def = ItemDefinitions.getLowestDefinitionForName(newPotion);
				if (def == null) {
					System.out.println("Couldn't find definitions for " + newPotion);
					continue;
				}
				pricesList.add("#" + newPotion);
				pricesList.add(def.getId() + ":" + (int) (price * (i == 0 ? 0.75 : i == 1 ? 0.50 : 0.25)));
				//lessPotions.add(newPotion.trim() + ": " + def.getId());
			}
		}
		pricesList.forEach(line -> Utils.writeTextToFile(file, line + "\n", true));*/
	//lessPotions.forEach(line -> Utils.writeTextToFile(file, line + "\n", true));
//		player.getPackets().sendPrivateMessage("Tyluur", "hey");
//		System.out.println(new java.util.Date(1462836696207L).toString());
//		player.getSkills().setXp(Integer.parseInt(cmd[1]), 200_000_000);
//		player.getFacade().setGoldPoints(Integer.parseInt(cmd[1]));
//		System.out.println("bonus xp:\t" + DXPAlgorithms.getBonusExperience(player));
//		System.out.println("spent today(secs):" + player.getFacade().getTimeSpentToday(player.getSignInTime()));
//		System.out.println("since 420(mins): " + TimeUnit.MILLISECONDS.toMinutes(player.getFacade().getTimeSpentSinceDate(new GregorianCalendar(2016, 3, 20).getTime().getTime(), player.getSignInTime())));

// 		player.getPackets().sendRunScript(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
/*		player.getVarsManager().sendVarBit(7232, Integer.parseInt(cmd[1]));
		player.getVarsManager().sendVarBit(7233, Integer.parseInt(cmd[2]));
		player.getPackets().sendConfig(1878, Integer.parseInt(cmd[3]));
//		player.getPackets().sendConfig(1644, Integer.parseInt(cmd[3]));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendRunScript(1180);
				player.getPackets().sendRunScript(1160);
				System.out.println("Sent script");
			}
		}, 1);*/
			/*String line = "14856,3221,3943,0,10,0\n" +
				              "14857,3220,3943,0,10,0\n" +
				              "14856,3222,3943,0,10,0\n" +
				              "14858,3225,3942,0,10,0\n" +
				              "14857,3224,3947,0,10,0\n" +
				              "14856,3223,3947,0,10,0\n" +
				              "14850,3227,3948,0,10,0\n" +
				              "14851,3228,3948,0,10,0\n" +
				              "14850,3228,3949,0,10,0\n" +
				              "14852,3229,3949,0,10,0\n" +
				              "14852,3229,3938,0,10,0\n" +
				              "14851,3230,3938,0,10,0\n" +
				              "14850,3227,3939,0,10,0\n" +
				              "14855,3231,3938,0,10,0\n" +
				              "14855,3231,3937,0,10,0\n" +
				              "14855,3233,3937,0,10,0\n" +
				              "14853,3234,3937,0,10,0\n" +
				              "14854,3232,3937,0,10,0\n" +
				              "14854,3233,3951,0,10,0\n" +
				              "14853,3231,3951,0,10,0\n" +
				              "14854,3232,3951,0,10,0\n" +
				              "14855,3230,3950,0,10,0\n" +
				              "14862,3237,3941,0,10,0\n" +
				              "14862,3238,3940,0,10,0\n" +
				              "14863,3237,3940,0,10,0\n" +
				              "14864,3238,3941,0,10,0\n" +
				              "14864,3238,3948,0,10,0\n" +
				              "14862,3237,3948,0,10,0\n" +
				              "14863,3235,3949,0,10,0\n" +
				              "14864,3239,3948,0,10,0\n" +
				              "14850,3234,3944,0,10,0\n" +
				              "14851,3229,3944,0,10,0\n" +
				              "14852,3228,3942,0,10,0\n" +
				              "14852,3242,3946,0,10,0\n" +
				              "14850,3243,3944,0,10,0\n" +
				              "14850,3243,3948,0,10,0\n" +
				              "14856,3259,3951,0,10,0\n" +
				              "14858,3258,3951,0,10,0\n" +
				              "14858,3259,3955,0,10,0\n" +
				              "14856,3258,3955,0,10,0\n" +
				              "14857,3257,3955,0,10,0\n" +
				              "14857,3253,3952,0,10,0\n" +
				              "14856,3253,3953,0,10,0\n" +
				              "14857,3254,3953,0,10,0\n" +
				              "14850,3252,3949,0,10,0\n" +
				              "14851,3250,3952,0,10,0\n" +
				              "14852,3249,3951,0,10,0\n" +
				              "14852,3251,3953,0,10,0\n" +
				              "14852,3252,3953,0,10,0\n" +
				              "14862,3250,3945,0,10,0\n" +
				              "14864,3249,3945,0,10,0\n" +
				              "14862,3249,3946,0,10,0\n" +
				              "14854,3247,3949,0,10,0\n" +
				              "14853,3246,3949,0,10,0\n" +
				              "14853,3248,3949,0,10,0\n" +
				              "14859,3059,3886,0,10,0\n" +
				              "14859,3061,3884,0,10,2\n" +
				              "1309,3143,3831,0,10,0\n" +
				              "1309,3141,3824,0,10,2\n" +
				              "1309,3142,3819,0,10,1\n" +
				              "1309,3130,3815,0,10,3\n" +
				              "1306,3147,3801,0,10,2\n" +
				              "1306,3141,3805,0,10,1\n" +
				              "37823,3134,3794,0,0,0\n" +
				              "37823,3134,3794,0,10,0\n" +
				              "37823,3139,3800,0,10,2\n" +
				              "37823,3137,3784,0,10,3\n" +
				              "1309,3137,3784,0,10,3\n" +
				              "1307,3137,3784,0,10,3\n" +
				              "1306,3137,3784,0,10,3\n" +
				              "1306,3139,3800,0,10,3\n" +
				              "2782,3211,3736,0,10,1\n" +
				              "1307,2973,3566,0,10,1\n" +
				              "1307,2976,3573,0,10,1\n" +
				              "1307,2982,3574,0,10,3\n" +
				              "1307,2985,3562,0,10,2\n" +
				              "1307,2991,3566,0,10,0\n" +
				              "1307,2986,3570,0,10,0\n" +
				              "1307,2976,3560,0,10,1\n" +
				              "139,3118,3546,0,0,0\n" +
				              "139,3118,3546,0,10,0\n" +
				              "139,3115,3551,0,10,3\n" +
				              "139,3121,3542,0,10,1\n" +
				              "139,3122,3538,0,10,2\n" +
				              "139,3107,3547,0,10,0\n" +
				              "139,3111,3543,0,10,0\n" +
				              "139,3116,3539,0,10,0\n" +
				              "139,3118,3534,0,10,0";
		List<String> lines = new ArrayList<>(Arrays.asList(line.split("\n")));
		for (String text : lines) {
			String[] split = text.split(",");
			WorldObject object = new WorldObject(Integer.parseInt(split[0]), Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
			World.spawnObject(object);

			try {
				ObjectSpawns.dumpObjectSpawn(object.getId(), object.getType(), object.getRotation(), object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		/*int topFromX = Integer.parseInt(cmd[1]), toRegionX = Integer.parseInt(cmd[3]);
		int toFromY = Integer.parseInt(cmd[2]), toRegionY = Integer.parseInt(cmd[4]);
		int ratio = 4;

		RegionBuilder.copyAllPlanesMap(topFromX, toFromY, toRegionX, toRegionY, ratio); // top
		// left
		RegionBuilder.copyAllPlanesMap(topFromX - 4, toFromY, toRegionX - 4, toRegionY, ratio); // top
		// right

		RegionBuilder.copyAllPlanesMap(topFromX - 4, toFromY - 4, toRegionX - 4, toRegionY - 4, ratio); // bottom
		// left
		RegionBuilder.copyAllPlanesMap(topFromX, toFromY - 4, toRegionX, toRegionY - 4, ratio); // bottom
		player.setClientHasntLoadedMapRegion();*/
	// right

//		World.spawnObject(new WorldObject(Integer.parseInt(cmd[1]), 22, 0, player));
//		player.getPackets().sendRebuildMap();
/*
		WorldObject found = null;
		for (WorldObject object : World.getRegion(player.getRegionId()).getObjects()) {
			if (object.getWorldTile().matches(player.getWorldTile())) {
				found = object;
				break;
			}
		}
		System.out.println("obj=" +found + ", tileFree=" + World.isFloorFree(player.getPlane(), player.getX(), player.getY()) + ", mask=" + World.getMask(player.getPlane(), player.getX(), player.getY()));
*/
//		player.getInterfaceManager().sendOverlay(Integer.parseInt(cmd[1]));
		/*final int PARTY_CHEST_INTERFACE = 647;
		final int INVENTORY_INTERFACE = 336;
		int CHEST_INTERFACE_ITEMS_KEY = ItemSetsKeyGenerator.generateKey();
		player.getPackets().sendInterSetItemsOptionsScript(INVENTORY_INTERFACE, 0, 93, 4, 7, "Deposit", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X");
		player.getPackets().sendInterSetItemsOptionsScript(INVENTORY_INTERFACE, 30, CHEST_INTERFACE_ITEMS_KEY, 4, 7, "Value");
		player.getPackets().sendInterSetItemsOptionsScript(PARTY_CHEST_INTERFACE, 33, CHEST_INTERFACE_ITEMS_KEY, true, 4, 7, "Examine");
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 0, 27, 1278);
		for (int i = 0; i < 27; i++) {
			for (int j = 0; j < 20_000; j++) {
				player.getPackets().sendIComponentSettings(PARTY_CHEST_INTERFACE, i, 0, 27, j);
				player.getPackets().sendIComponentSettings(PARTY_CHEST_INTERFACE, i, 0, 27, j);
			}
		}
		//sendAccessMask(1278, 364, 4, 0, 5);
		player.getInterfaceManager().sendInterface(Integer.parseInt(cmd[1]));
		int i = 529;
		Item[] items = new Item[] { new Item(995, i) };
		player.getPackets().sendItems(i, items);
		i = 91;
		items = new Item[] { new Item(995, i) };
		player.getPackets().sendItems(i, items);
*/
	//3, 10, 12, 13, 14, 16, 44
	//player.getPackets().sendHideIComponent(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), Boolean.parseBoolean(cmd[3]));
//		player.getPackets().sendIComponentSprite(34, Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));

	//	QuestTabInteractionEvent.displayAchievements(player, AchievementType.EASY);
	//	World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Wilderness</col>: " + player.getDisplayName() + " has just ruined " + player.getDisplayName() + "'s killstreak of 12 for " + Wilderness.getPointRewardFromKillstreakEnding(12) + " points.", false);
	//	World.sendWorldMessage("<img=6><col=" + ChatColors.RED + ">Wilderness</col>: " + player.getDisplayName() + "'s killstreak of " + player.getFacade().getKillstreak() + " can be ended for 5 points.", false);

		/*if (cmd.length == 3) {
			player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> {
				player.setNextWorldTile(nmz.getInstance().getWorldTile(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2])));
			});
		} else {
			player.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(nmz -> {
				System.out.println(nmz.getInstance().getWorldTile(player.getX(), player.getY()));
			});
		}
		System.out.println((int) (5 * 0.90));*/
	/*	player.getPackets().sendGraphics(new Graphics(Integer.parseInt(cmd[1])), player.getWorldTile());
		StringBuilder bldr = new StringBuilder();
		bldr.append("new Item[] { ");
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			bldr.append("new Item(" + item.getId() + ", " + item.getAmount() + "),");
		}

		bldr.append("}");
		System.out.println(bldr.toString());*/
	//player.getPackets().sendConsoleOutput(getCompleted(cmd, 1));

	//	player.getPackets().receivePrivateMessage("SERVER", "SERVER", 2, "TEST");
	//FadingScreen.displayFade(player, FadeTypes.ALL_BLACK, 3);
	//	player.getAppearence().setDefaultAppearance().generateAppearenceData();
	//	player.getAppearence().getLook()[Integer.parseInt(cmd[1])] = Integer.parseInt(cmd[2]);
	//	player.getAppearence().generateAppearenceData();
/*
		Item item = new Item(21010);*/
/*player.getEquipment().getItem(Integer.parseInt(cmd[1]));*//*

		if (item != null) {
			ItemDefinitions defs = ItemDefinitions.forId(item.getId());
			System.out.println(defs.maleEquip1 + ", " + defs.femaleEquip1 + ", " + defs.name);
		}
*/

	/*	player.getCostumeManager().setActiveCostume(Costumes.DERVISH_WHITE_COSTUME);
		player.getAppearence().generateAppearenceData();*/

	//player.getInterfaceManager().closeInventory();
	//player.getInterfaceManager().sendInventoryInterface(Integer.parseInt(cmd[1]));

	//player.applyHit(new Hit(player, Integer.parseInt(cmd[1])));

/*		int interfaceId = Integer.parseInt(cmd[1]);

		player.getInterfaceManager().sendInterface(interfaceId);
		for (int i = 0; i < Integer.parseInt(cmd[2]); i++) {
			player.getPackets().sendItems(i, new Item[] { new Item(11694, i), new Item(14484, i),  new Item(11694, i), new Item(14484, i),  new Item(11694, i), new Item(14484, i),  new Item(11694, i), new Item(14484, i),  new Item(11694, i), new Item(14484, i), });
		}*/

	//System.out.println(player.getBank().deleteItem(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), true));
	//player.getBank().deleteItem(Integer.parseInt(cmd[1]), true);
		/*WorldObject object = World.getObjectWithId(new WorldTile(3030, 3703, 0), 14859);
		if (object != null) {
			System.out.println("faced it");
			player.faceObject(object);
		}*/
	/*	int[] items = new int[] { 11694, 11696 };

		OptionalInt optional = Arrays.stream(items).filter(value -> value == 11694).findFirst();

		Player[] players = World.getPlayers().toArray(new Player[World.getPlayers().size()]);

		Optional<Player> playerOptional = Arrays.stream(players).filter(p -> p.getUsername().equalsIgnoreCase("tyluur")).findFirst();




		player.getControllerManager().verifyControlerForOperation(DuelArena.class).ifPresent(c -> {
			c.removeEquipment();
		});
*/

	// 278 - ranging guild
	// 11 - deposit box
	// 370 - chompy
	// 392 - resources collected
	// 468 - diango items
	// 686 - mrs winkins
	// 1007 - statue collection bag

	/*	int interfaceId = Integer.parseInt(cmd[1]);
		List<Item> itemList = new ArrayList<>();
		for (int i = 1; i <= 40; i++) {
			itemList.add(new Item(11694, 0));
		}
		player.getPackets().sendItems(93, itemList.toArray(new Item[itemList.size()]));

		player.getPackets().sendInterSetItemsOptionsScript(interfaceId, 17, 93, 7, 5, "Buy-1", "Buy-5", "Buy-10", "Buy-All", "Buy-X", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(interfaceId, 17, 0, 40, 0, 1, 2, 3, 4, 5);

		player.getInterfaceManager().sendInterface(interfaceId);*/

}

}