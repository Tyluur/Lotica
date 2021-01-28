package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.others.BarrowsBrother;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.medium.Barrows_Addiction;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

public final class Barrows extends Controller {
	
	private static final Item[] COMMUM_REWARDS = { new Item(558, 1795), new Item(562, 773), new Item(560, 391), new Item(565, 164), new Item(4740, 188) };

	private static final Item[] RARE_REWARDS = { new Item(1149, 1), new Item(987, 1), new Item(985, 1) };
	
	public static final Item[] BARROW_REWARDS = { new Item(4708, 1), new Item(4710, 1), new Item(4712, 1), new Item(4714, 1), new Item(4716, 1), new Item(4718, 1), new Item(4720, 1), new Item(4722, 1), new Item(4724, 1), new Item(4726, 1), new Item(4728, 1), new Item(4730, 1), new Item(4732, 1), new Item(4734, 1), new Item(4736, 1), new Item(4738, 1), new Item(4745, 1), new Item(4747, 1), new Item(4749, 1), new Item(4751, 1), new Item(4753, 1), new Item(4755, 1), new Item(4757, 1), };
	
	private BarrowsBrother target;
	
	private int headComponentId;
	
	private int timer;
	
	@Override
	public void start() {
		if (player.getHiddenBrother() == -1) {
			player.setHiddenBrother(Utils.random(Hills.values().length));
		}
		loadData();
		sendInterfaces();
	}
	
	@Override
	public boolean canAttack(Entity target) {
		if (target instanceof BarrowsBrother && target != this.target) {
			player.getPackets().sendGameMessage("This isn't your target.");
			return false;
		}
		return true;
	}
	
	@Override
	public void process() {
		if (timer > 0) {
			timer--;
			return;
		}
		if (headComponentId == 0) {
			if (player.getHiddenBrother() == -1) {
				player.applyHit(new Hit(player, Utils.random(50) + 1, HitLook.REGULAR_DAMAGE));
				resetHeadTimer();
				return;
			}
			int headIndex = getAndIncreaseHeadIndex();
			if (headIndex == -1) {
				resetHeadTimer();
				return;
			}
			headComponentId = 9 + Utils.random(2);
			player.getPackets().sendItemOnIComponent(24, headComponentId, 4761 + headIndex, 0);
			player.getPackets().sendIComponentAnimation(9810, 24, headComponentId);
			int activeLevel = player.getPrayer().getPrayerpoints();
			if (activeLevel > 0) {
				int level = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
				player.getPrayer().drainPrayer(level / 6);
			}
			timer = 3;
		} else {
			player.getPackets().sendItemOnIComponent(24, headComponentId, -1, 0);
			headComponentId = 0;
			resetHeadTimer();
		}
	}
	
	@Override
	public void magicTeleported(int type) {
		leave(false);
	}

	@Override
	public void sendInterfaces() {
		if (player.getHiddenBrother() != -1) {
			player.getInterfaceManager().sendOverlay(24);
		}
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() >= 6702 && object.getId() <= 6707) {
			WorldTile out = Hills.values()[object.getId() - 6702].outBound;
			//cant make a perfect middle since 3/ 2 wont make a real integer number or wahtever u call it..
			exit(new WorldTile(out.getX() + 1, out.getY() + 1, out.getPlane()));
			return false;
		} else if (object.getId() == 10284) {
			if (player.getHiddenBrother() == -1) {//reached chest
				player.getPackets().sendGameMessage("You found nothing.");
				return false;
			}
			if (!player.getKilledBarrowBrothers()[player.getHiddenBrother()]) {
				sendTarget(2025 + player.getHiddenBrother(), player);
			}
			if (target != null) {
				player.getPackets().sendGameMessage("You found nothing.");
				return false;
			}
			sendReward();
			player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
			player.getInterfaceManager().closeOverlay();
			player.getPackets().sendSpawnedObject(new WorldObject(6775, 10, 0, 3551, 9695, 0));
			player.resetBarrows();
			return false;
		} else if (object.getId() >= 6716 && object.getId() <= 6749) {
			WorldTile walkTo;
			if (object.getRotation() == 0) {
				walkTo = new WorldTile(object.getX() + 5, object.getY(), 0);
			} else if (object.getRotation() == 1) {
				walkTo = new WorldTile(object.getX(), object.getY() - 5, 0);
			} else if (object.getRotation() == 2) {
				walkTo = new WorldTile(object.getX() - 5, object.getY(), 0);
			} else {
				walkTo = new WorldTile(object.getX(), object.getY() + 5, 0);
			}
			if (!World.isNotCliped(walkTo.getPlane(), walkTo.getX(), walkTo.getY(), 1)) {
				return false;
			}
			player.setNextWorldTile(walkTo);
//			player.addWalkSteps(walkTo.getX(), walkTo.getY(), -1, false);
//			player.getLockManagement().lockAll(1000);
			if (player.getHiddenBrother() != -1) {
				int brother = getRandomBrother();
				if (brother != -1) {
					sendTarget(2025 + brother, walkTo);
				}
			}
			return false;
		} else {
			int sarcoId = getSarcophagusId(object.getId());
			if (sarcoId != -1) {
				if (sarcoId == player.getHiddenBrother()) {
					player.getDialogueManager().startDialogue("BarrowsD");
				} else if (target != null || player.getKilledBarrowBrothers()[sarcoId]) {
					player.getPackets().sendGameMessage("You found nothing.");
				} else {
					sendTarget(2025 + sarcoId, player);
				}
				return false;
			}
		}
		return true;
	}
	
	private void exit(WorldTile outside) {
		player.setNextWorldTile(outside);
		leave(false);
	}

	public void sendTarget(int id, WorldTile tile) {
		if (target != null) {
			target.disapear();
		}
		target = new BarrowsBrother(id, tile, this);
		target.setTarget(player);
		target.setNextForceTalk(new ForceTalk("You dare disturb my rest!"));
		player.getHintIconsManager().addHintIcon(target, 1, -1, false);
	}
	
	//4% prob barrows reward per bro
	//1% per 10kills.
	public void sendReward() {
		double percentage = 0;
		for (boolean died : player.getKilledBarrowBrothers()) {
			if (died) {
				percentage += 4;
			}
		}
		percentage += (player.getBarrowsKillCount() / 10);
		if (percentage >= Math.random() * 110) {
			//reward barrows
			Item item = Utils.randomArraySlot(BARROW_REWARDS);
			drop(item);
			World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Barrows</col>: " + player.getDisplayName() + " has just received " + (item.getAmount() > 1 ? item.getAmount() + "x " : "") + item.getName().toLowerCase() + " from the barrows chest!", false);
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("barrows", player.getUsername(), "Received " + item + " (" + item.getName() + ") from barrows"));
		}
		if (Utils.random(10) == 0)  //rare
		{
			drop(RARE_REWARDS[Utils.random(RARE_REWARDS.length)]);
		}
		if (Utils.random(3) != 0) {
			Item reward = COMMUM_REWARDS[Utils.random(COMMUM_REWARDS.length)];

			drop(new Item(reward.getId(), Utils.random(1, reward.getAmount())));
		}
		//here reward other stuff normaly

		drop(new Item(995, 4162)); //money reward at least always
		AchievementHandler.incrementProgress(player, Barrows_Addiction.class, 1);
	}

	public int getRandomBrother() {
		List<Integer> bros = new ArrayList<>();
		for (int i = 0; i < Hills.values().length; i++) {
			if (player.getKilledBarrowBrothers()[i] || player.getHiddenBrother() == i) {
				continue;
			}
			bros.add(i);
		}
		if (bros.isEmpty()) {
			return -1;
		}
		return bros.get(Utils.random(bros.size()));

	}
	
	public int getSarcophagusId(int objectId) {
		switch (objectId) {
			case 6821:
				return 0;
			case 6771:
				return 1;
			case 6773:
				return 2;
			case 6822:
				return 3;
			case 6772:
				return 4;
			case 6823:
				return 5;
			default:
				return -1;
		}
	}
	
	public void drop(Item item) {
		Item dropItem = new Item(item.getId(), Utils.random(item.getDefinitions().isStackable() ? item.getAmount() : item.getAmount()) + 1);
		player.getInventory().addItemDrop(dropItem);
	}
	
	@Override
	public boolean sendDeath() {
		leave(false);
		return true;
	}
	
	@Override
	public boolean login() {
		if (player.getHiddenBrother() == -1) {
			player.getPackets().sendCameraShake(3, 25, 50, 25, 50);
		}
		loadData();
		sendInterfaces();
		return false;
	}
	
	@Override
	public boolean logout() {
		leave(true);
		return false;
	}
	
	//component 9, 10, 11
	
	@Override
	public void forceClose() {
		leave(true);
	}

	private void leave(boolean logout) {
		if (target != null) {
			target.finish(); //target also calls removing hint icon at remove
		}
		player.getInterfaceManager().closeOverlay(); //removes inter
		if (!logout) {
			player.getPackets().sendBlackOut(0); //unblacks minimap
			if (player.getHiddenBrother() == -1) {
				player.getPackets().sendStopCameraShake();
			}
			removeController();
		}
	}
	
	public int getAndIncreaseHeadIndex() {
		Integer head = (Integer) player.getAttributes().remove("BarrowsHead");
		if (head == null || head == player.getKilledBarrowBrothers().length - 1) {
			head = 0;
		}
		player.getAttributes().put("BarrowsHead", head + 1);
		return player.getKilledBarrowBrothers()[head] ? head : -1;
	}
	
	public void loadData() {
		resetHeadTimer();
		for (int i = 0; i < player.getKilledBarrowBrothers().length; i++) {
			sendBrotherSlain(i, player.getKilledBarrowBrothers()[i]);
		}
		sendCreaturesSlainCount(player.getBarrowsKillCount());
		player.getPackets().sendBlackOut(2); //blacks minimap
	}
	
	public void resetHeadTimer() {
		timer = 20 + Utils.random(6);
	}
	
	public void sendBrotherSlain(int index, boolean slain) {
		player.getPackets().sendConfigByFile(457 + index, slain ? 1 : 0);
	}
	
	public void sendCreaturesSlainCount(int count) {
		player.getPackets().sendConfigByFile(464, count);
	}
	
	public Barrows() {

	}
	
	public static boolean digIntoGrave(final Player player) {
		for (Hills hill : Hills.values()) {
			if (player.getPlane() == hill.outBound.getPlane() && player.getX() >= hill.outBound.getX() && player.getY() >= hill.outBound.getY() && player.getX() <= hill.outBound.getX() + 3 && player.getY() <= hill.outBound.getY() + 3) {
				player.useStairs(-1, hill.inside, 1, 2, "You've broken into a crypt.");
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.getControllerManager().startController("Barrows");
					}
				});
				return true;
			}
		}

		return false;
	}
	
	public void targetDied() {
		player.getHintIconsManager().removeUnsavedHintIcon();
		setBrotherSlained(target.getId() - 2025);
		target = null;

	}
	
	public void setBrotherSlained(int index) {
		player.getKilledBarrowBrothers()[index] = true;
		sendBrotherSlain(index, true);
	}
	
	public void targetFinishedWithoutDie() {
		player.getHintIconsManager().removeUnsavedHintIcon();
		target = null;
	}
	
	private enum Hills {
		AHRIM_HILL(new WorldTile(3564, 3287, 0), new WorldTile(3557, 9703, 3)),
		DHAROK_HILL(new WorldTile(3573, 3296, 0), new WorldTile(3556, 9718, 3)),
		GUTHAN_HILL(new WorldTile(3574, 3279, 0), new WorldTile(3534, 9704, 3)),
		KARIL_HILL(new WorldTile(3563, 3276, 0), new WorldTile(3546, 9684, 3)),
		TORAG_HILL(new WorldTile(3553, 3281, 0), new WorldTile(3568, 9683, 3)),
		VERAC_HILL(new WorldTile(3556, 3296, 0), new WorldTile(3578, 9706, 3));

		private WorldTile outBound;

		private WorldTile inside;

		Hills(WorldTile outBound, WorldTile in) {
			this.outBound = outBound;
			inside = in;
		}
	}
	
}
