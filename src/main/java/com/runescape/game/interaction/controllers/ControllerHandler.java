package com.runescape.game.interaction.controllers;

import com.runescape.game.content.global.minigames.BrimhavenAgility;
import com.runescape.game.content.global.minigames.clanwars.FfaZone;
import com.runescape.game.content.global.minigames.clanwars.RequestController;
import com.runescape.game.content.global.minigames.clanwars.WarControler;
import com.runescape.game.content.global.minigames.creations.StealingCreationGame;
import com.runescape.game.content.global.minigames.creations.StealingCreationLobby;
import com.runescape.game.content.global.minigames.duel.DuelArena;
import com.runescape.game.content.global.minigames.duel.DuelControler;
import com.runescape.game.content.global.minigames.warriors.WarriorsGuild;
import com.runescape.game.content.global.miniquest.dt.DesertTreasureController;
import com.runescape.game.content.global.miniquest.hftd.HorrorFromTheDeepC;
import com.runescape.game.content.global.miniquest.ld.LunarDiplomacyC;
import com.runescape.game.content.global.miniquest.mm.MonkeyMadnessController;
import com.runescape.game.content.global.miniquest.rfd.RecipeForDisasterController;
import com.runescape.game.interaction.controllers.impl.*;
import com.runescape.game.interaction.controllers.impl.fightpits.FightPitsArena;
import com.runescape.game.interaction.controllers.impl.fightpits.FightPitsLobby;
import com.runescape.game.interaction.controllers.impl.nmz.NMZController;
import com.runescape.game.interaction.controllers.impl.nmz.NMZLobby;
import com.runescape.game.interaction.controllers.impl.pestcontrol.PestControlGame;
import com.runescape.game.interaction.controllers.impl.pestcontrol.PestControlLobby;
import com.runescape.game.interaction.controllers.impl.tutorial.StartTutorial;

import java.util.HashMap;

public class ControllerHandler {

	private static final HashMap<Object, Class<Controller>> handledControllers = new HashMap<Object, Class<Controller>>();

	@SuppressWarnings("unchecked")
	public static void init() {
		try {
			handledControllers.put("Wilderness", (Class<Controller>) Class.forName(Wilderness.class.getCanonicalName()));
			handledControllers.put("GodWars", (Class<Controller>) Class.forName(GodWars.class.getCanonicalName()));
			handledControllers.put("ZGDControler", (Class<Controller>) Class.forName(ZGDControler.class.getCanonicalName()));
			handledControllers.put("StartTutorial", (Class<Controller>) Class.forName(StartTutorial.class.getCanonicalName()));
			handledControllers.put("DuelArena", (Class<Controller>) Class.forName(DuelArena.class.getCanonicalName()));
			handledControllers.put("DuelControler", (Class<Controller>) Class.forName(DuelControler.class.getCanonicalName()));
			handledControllers.put("CorpBeastControler", (Class<Controller>) Class.forName(CorpBeastControler.class.getCanonicalName()));
			handledControllers.put("DTControler", (Class<Controller>) Class.forName(DTControler.class.getCanonicalName()));
//			handledControllers.put("CastleWarsPlaying", (Class<Controller>) Class.forName(CastleWarsPlaying.class.getCanonicalName()));
//			handledControllers.put("CastleWarsWaiting", (Class<Controller>) Class.forName(CastleWarsWaiting.class.getCanonicalName()));
			handledControllers.put("clan_wars_request", (Class<Controller>) Class.forName(RequestController.class.getCanonicalName()));
			handledControllers.put("clan_war", (Class<Controller>) Class.forName(WarControler.class.getCanonicalName()));
			handledControllers.put("clan_wars_ffa", (Class<Controller>) Class.forName(FfaZone.class.getCanonicalName()));
			handledControllers.put("NomadsRequiem", (Class<Controller>) Class.forName(NomadsRequiem.class.getCanonicalName()));
			handledControllers.put("BorkControler", (Class<Controller>) Class.forName(BorkControler.class.getCanonicalName()));
			handledControllers.put("BrimhavenAgility", (Class<Controller>) Class.forName(BrimhavenAgility.class.getCanonicalName()));
			handledControllers.put("FightCavesControler", (Class<Controller>) Class.forName(FightCaves.class.getCanonicalName()));
			handledControllers.put("FightKilnControler", (Class<Controller>) Class.forName(FightKiln.class.getCanonicalName()));
			handledControllers.put("FightPitsLobby", (Class<Controller>) Class.forName(FightPitsLobby.class.getCanonicalName()));
			handledControllers.put("FightPitsArena", (Class<Controller>) Class.forName(FightPitsArena.class.getCanonicalName()));
			handledControllers.put("PestControlGame", (Class<Controller>) Class.forName(PestControlGame.class.getCanonicalName()));
			handledControllers.put("PestControlLobby", (Class<Controller>) Class.forName(PestControlLobby.class.getCanonicalName()));
			handledControllers.put("Barrows", (Class<Controller>) Class.forName(Barrows.class.getCanonicalName()));
			handledControllers.put("QueenBlackDragonControler", (Class<Controller>) Class.forName(QueenBlackDragonController.class.getCanonicalName()));
			handledControllers.put("RuneSpanControler", (Class<Controller>) Class.forName(RunespanControler.class.getCanonicalName()));
			handledControllers.put("SorceressGarden", (Class<Controller>) Class.forName(SorceressGarden.class.getCanonicalName()));
			handledControllers.put("CrucibleControler", (Class<Controller>) Class.forName(CrucibleControler.class.getCanonicalName()));
			handledControllers.put("StealingCreationsGame", (Class<Controller>) Class.forName(StealingCreationGame.class.getCanonicalName()));
			handledControllers.put("StealingCreationsLobby", (Class<Controller>) Class.forName(StealingCreationLobby.class.getCanonicalName()));
//			handledControllers.put("HouseControler", (Class<Controller>) Class.forName(HouseController.class.getCanonicalName()));
			handledControllers.put("RecipeForDisasterC", (Class<Controller>) Class.forName(RecipeForDisasterController.class.getCanonicalName()));
			handledControllers.put("DesertTreasureC", (Class<Controller>) Class.forName(DesertTreasureController.class.getCanonicalName()));
			handledControllers.put("MonkeyMadnessC", (Class<Controller>) Class.forName(MonkeyMadnessController.class.getCanonicalName()));
			handledControllers.put("HorrorFromTheDeepC", (Class<Controller>) Class.forName(HorrorFromTheDeepC.class.getCanonicalName()));
			handledControllers.put("LunarDiplomacyC", (Class<Controller>) Class.forName(LunarDiplomacyC.class.getCanonicalName()));
			handledControllers.put("WarriorsGuild", (Class<Controller>) Class.forName(WarriorsGuild.class.getCanonicalName()));
			handledControllers.put("JailController", (Class<Controller>) Class.forName(JailController.class.getCanonicalName()));
			handledControllers.put("NMZLobby", (Class<Controller>) Class.forName(NMZLobby.class.getCanonicalName()));
			handledControllers.put("NMZController", (Class<Controller>) Class.forName(NMZController.class.getCanonicalName()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void reload() {
		handledControllers.clear();
		init();
	}

	public static Controller getController(Object key) {
		if (key instanceof Controller) { return (Controller) key; }
		Class<Controller> classC = handledControllers.get(key);
		if (classC == null) { return null; }
		try {
			return classC.newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
