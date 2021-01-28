package com.runescape.game.interaction.controllers.impl.tutorial;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.StarterList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StartTutorial extends Controller {

	/**
	 * The last stage in the tutorial
	 */
	public static int LAST_TUTORIAL_STAGE = -1;

	/**
	 * The stages that will be in the tutorial
	 */
	private final List<Runnable> tutorialStages = new ArrayList<>();

	/**
	 * The current stage the player is at in the tutorial
	 */
	private int currentStage = 0;

	/**
	 * If the player selected the first option in the dialogue
	 */
	private boolean selectedFirstOption;

	private void openModeSelection() {
		player.getDialogueManager().startDialogue(new Dialogue() {

			private final List<String> options = new ArrayList<>();

			private TutorialMode selectedMode;

			@Override
			public void start() {
				options.add("Select an account mode");
				for (TutorialMode mode : TutorialMode.values()) {
					options.add(mode.getFormattedName());
				}
				sendOptionsDialogue(options.toArray(new String[options.size()]));
			}

			@Override
			public void run(int interfaceId, int option) {
				switch (stage) {
					case -1:
						Optional<TutorialMode> optional = TutorialMode.getByName(options.get(option - 1));
						if (!optional.isPresent()) {
							throw new IllegalStateException();
						}
						TutorialMode mode = optional.get();
						sendDialogue(mode.getDescription());
						selectedMode = mode;
						stage = 0;
						break;
					case 0:
						sendOptionsDialogue("Proceed on " + selectedMode.getFormattedName() + "?", "Yes", "No");
						stage = 1;
						break;
					case 1:
						if (option == FIRST) {
							switch (selectedMode) {
								case IRONMAN:
									player.addForumUsergroup(RightManager.IRONMAN);
									break;
								case ULTIMATE_IRONMAN:
									player.addForumUsergroup(RightManager.ULTIMATE_IRONMAN);
									break;
								default:
									break;
							}
							endGuide();
						} else {
							end();
							openModeSelection();
						}
						break;
				}
			}

			@Override
			public void finish() {

			}
		});
	}

	/**
	 * Proceeds to the next stag
	 */
	protected void nextStage() {
		currentStage++;
		handleCurrentStage();
	}

	/**
	 * Handles the current tutorial stage
	 */
	private void handleCurrentStage() {
		if (currentStage < 0 || currentStage >= tutorialStages.size()) {
			System.out.println("Stage of " + currentStage + " couldn't be found.");
			return;
		}
		try {
			tutorialStages.get(currentStage).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		loadTutorialEvents(player);
		player.setNextWorldTile(GameConstants.START_PLAYER_LOCATION);
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue("Do you need a tutorial?", "Yes - this is my first time (recommended)", "No - I'm not new here");
			}

			@Override
			public void run(int interfaceId, int option) {
				end();

				selectedFirstOption = true;
				if (option == SECOND) {
					currentStage = LAST_TUTORIAL_STAGE;
				}
				handleCurrentStage();
			}

			@Override
			public void finish() {

			}
		});
	}

	@Override
	public boolean keepCombating(Entity target) {
		return false;
	}

	@Override
	public boolean canAttack(Entity target) {
		return false;
	}

	@Override
	public boolean canHit(Entity target) {
		return false;
	}

	@Override
	public void process() {
		// so the player always has the start dialogue
		if (!selectedFirstOption) {
			if (tutorialStages.size() > 0 && currentStage == tutorialStages.size()) {

			} else if (!player.getInterfaceManager().containsChatBoxInter()) {
				start();
			}
			// so the player cant leave the staged-dialogue
		} else if (selectedFirstOption && player.getDialogueManager().getDialogue() == null) {
			handleCurrentStage();
		}
	}

	@Override
	public boolean useDialogueScript(Object key) {
		String keyName = key.toString();
		return !(!keyName.contains("StartTutorial") && !keyName.contains("MyAccountD") && !keyName.contains("SimpleNPCMessage"));
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		return false;
	}

	@Override
	public boolean canPickupItem(FloorItem item) {
		return false;
	}

	@Override
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return false;
	}

	@Override
	public boolean login() {
		start();
		return false;
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public void forceClose() {
		endGuide();
	}

	@Override
	public boolean canTrade() {
		return false;
	}

	public void endGuide() {
		player.closeInterfaces();
		TutorialDialogue.closeChatbox(player);

		removeController();
		giveStarterPack();

		player.setNextWorldTile(GameConstants.START_PLAYER_LOCATION);
		World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Newcomer Alert</col>: " + player.getDisplayName() + " has just joined our community.", false);
	}

	/**
	 * Gives the player their starter pack
	 */
	public boolean giveStarterPack() {
		if (StarterList.canReceiveStarter(player.getMacAddress())) {
			int starterCount = StarterList.getStartersReceived(player.getMacAddress());
			if (starterCount < 2) {
				Item[] starter = new Item[] { new Item(11137, 1), new Item(11137, 1), new Item(995, 100_000), new Item(7947, 250), new Item(1323, 1), new Item(4587, 1), new Item(1153, 1), new Item(1115, 1), new Item(1067, 1), new Item(1191, 1), new Item(579, 1), new Item(577, 1), new Item(1011, 1), new Item(1381, 1), new Item(1169, 1), new Item(1129, 1), new Item(1095, 1), new Item(841, 1), new Item(882, 100), new Item(554, 500), new Item(555, 500), new Item(556, 500), new Item(557, 500), new Item(558, 500), new Item(561, 500), new Item(560, 500), new Item(1712, 1) };
				for (Item item : starter) {
					player.getInventory().addItem(item);
				}
				StarterList.insertStarter(player.getMacAddress());
			}
			return true;
		} else {
			player.sendMessage("You have already received " + StarterList.MAX_STARTERS_PER_ADDRESS + " starter packs.", false);
			return false;
		}
	}

	private void loadTutorialEvents(Player player) {
		tutorialStages.clear();
		tutorialStages.add(() -> {
			player.setNextWorldTile(GameConstants.START_PLAYER_LOCATION);
			sendDialogue(3, Dialogue.CALM, false, this::nextStage, "Welcome to " + GameConstants.SERVER_NAME + " - the most unique server ever.", "Let's show you around some, shall we?");
		});
		tutorialStages.add(() -> {
			NPC wizard = Utils.findLocalNPC(player, 1263);
			int dir = Utils.random(Utils.DIRECTION_DELTA_X.length);
			if (wizard != null) {
				for (int i = 0; i < 5; i++) {
					if (World.checkWalkStep(wizard.getPlane(), wizard.getX(), wizard.getY(), dir, player.getSize())) {
						player.setNextWorldTile(new WorldTile(wizard.getX() + Utils.DIRECTION_DELTA_X[dir], wizard.getY() + Utils.DIRECTION_DELTA_Y[dir], wizard.getPlane()));
						break;
					}
				}
				player.setNextFaceWorldTile(wizard);
				sendDialogue(5, Dialogue.CALM, false, this::nextStage, "This is the Wizard: speak to him if you wish to", "travel anywhere around the world. He will do it", "free of charge.");
			}
		});
		tutorialStages.add(() -> {
			player.resetWalkSteps();
			player.setNextFaceWorldTile(player);
			player.setNextWorldTile(new WorldTile(3096, 3510, 0));
			sendDialogue(3, Dialogue.CALM, false, this::nextStage, "We're now at the quest room, in which you can", "complete quests to gain access to <i>new</i> content that", "will give you advantages over other people.");
		});
		tutorialStages.add(() -> {
			player.getInterfaceManager().openGameTab(3);
			sendDialogue(5, Dialogue.CALM, false, this::nextStage, "This is your diary, with which you can check your progress", "on our quests or achievements. Navigate pages with the", "next button (the arrow).");
		});
		tutorialStages.add(() -> {
			player.setNextWorldTile(new WorldTile(3097, 3496, 0));
			sendDialogue(3, Dialogue.CALM, false, this::nextStage, "We've arrived at the grand exchange, this is where you", "purchase a lot of items that are important for gameplay.", "Speak to any of the clerk after the tutorial to buy items.");
		});
		tutorialStages.add(this::openModeSelection);
		LAST_TUTORIAL_STAGE = tutorialStages.size() - 1;
	}

	private void sendDialogue(int ticks, int animationId, boolean isPlayer, Runnable onClick, String... message) {
		player.getDialogueManager().startDialogue(new TutorialDialogue(ticks, animationId, isPlayer, message) {
			@Override
			public void run(int interfaceId, int option) {
				onClick.run();
			}
		});
	}

}
