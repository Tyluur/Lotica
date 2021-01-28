package com.runescape.game.world.entity.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.Foods.Food;
import com.runescape.game.content.Pots.Pot;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.controllers.ControllerHandler;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;

import java.util.Optional;

public final class ControllerManager {

	private Object[] lastControllerArguments;

	private String lastController;

	private transient Player player;

	private transient Controller controller;

	private transient boolean inited;

	public ControllerManager() {
		
	}
	
	public ControllerManager create() {
		lastController = GameConstants.START_CONTROLER;
		return this;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Controller getController() {
		return controller;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> verifyControlerForOperation(Class<T> clazz) {
		if (controller == null || !clazz.equals(controller.getClass())) { return Optional.empty(); }
		return Optional.of((T) controller);
	}
	
	public void startController(Object key, Object... parameters) {
		if (controller != null) { forceStop(); }
		controller = (Controller) (key instanceof Controller ? key : ControllerHandler.getController(key));
		if (controller == null) { return; }
		controller.setPlayer(player);
		lastControllerArguments = parameters;
		lastController = (String) key;
		controller.start();
		inited = true;
	}

	public void forceStop() {
		if (controller != null) {
			controller.forceClose();
			controller = null;
		}
		lastControllerArguments = null;
		lastController = null;
		inited = false;
	}

	public void login() {
		if (lastController == null) { return; }
		controller = ControllerHandler.getController(lastController);
		if (controller == null) {
			forceStop();
			return;
		}
		controller.setPlayer(player);
		if (controller.login()) { forceStop(); } else { inited = true; }
	}

	public void logout() {
		if (controller == null) { return; }
		if (controller.logout()) { forceStop(); }
	}

	public boolean canMove(int dir) {
		return controller == null || !inited || controller.canMove(dir);
	}

	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return controller == null || !inited || controller.checkWalkStep(lastX, lastY, nextX, nextY);
	}

	public boolean keepCombating(Entity target) {
		return controller == null || !inited || controller.keepCombating(target);
	}

	public boolean canEquip(int slotId, int itemId) {
		return controller == null || !inited || controller.canEquip(slotId, itemId);
	}

	public boolean canAddInventoryItem(int itemId, int amount) {
		return controller == null || !inited || controller.canAddInventoryItem(itemId, amount);
	}

	public void trackXP(int skillId, double addedXp) {
		if (controller == null || !inited) { return; }
		controller.trackXP(skillId, addedXp);
	}

	public double getExperienceModifier(int skillId, double addedXp) {
		if (controller == null || !inited) { return -1; }
		return controller.getExperienceModifier(skillId, addedXp);
	}

	public boolean canDeleteInventoryItem(int itemId, int amount) {
		return controller == null || !inited || controller.canDeleteInventoryItem(itemId, amount);
	}

	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		return controller == null || !inited || controller.canUseItemOnItem(itemUsed, usedWith);
	}

	public boolean canAttack(Entity entity) {
		return controller == null || !inited || controller.canAttack(entity);
	}

	public boolean canPlayerOption1(Player target) {
		return controller == null || !inited || controller.canPlayerOption1(target);
	}

	public boolean canHit(Entity entity) {
		return controller == null || !inited || controller.canHit(entity);
	}

	public void moved() {
		if (controller == null || !inited) { return; }
		controller.moved();
	}

	public void magicTeleported(int type) {
		if (controller == null || !inited) { return; }
		controller.magicTeleported(type);
	}

	public void sendInterfaces() {
		if (controller == null || !inited) { return; }
		controller.sendInterfaces();
	}

	public void process() {
		if (controller == null || !inited) { return; }
		controller.process();
	}

	public boolean sendDeath() {
		return controller == null || !inited || controller.sendDeath();
	}

	public boolean canEat(Food food) {
		return controller == null || !inited || controller.canEat(food);
	}

	public boolean canPot(Pot pot) {
		return controller == null || !inited || controller.canPot(pot);
	}

	public boolean useDialogueScript(Object key) {
		return controller == null || !inited || controller.useDialogueScript(key);
	}

	public boolean processMagicTeleport(WorldTile toTile) {
		return controller == null || !inited || controller.processMagicTeleport(toTile);
	}

	public boolean processItemTeleport(WorldTile toTile) {
		return controller == null || !inited || controller.processItemTeleport(toTile);
	}

	public boolean processObjectTeleport(WorldTile toTile) {
		return controller == null || !inited || controller.processObjectTeleport(toTile);
	}

	public boolean processObjectClick1(WorldObject object) {
		return controller == null || !inited || controller.processObjectClick1(object);
	}

	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		return controller == null || !inited || controller.processButtonClick(interfaceId, componentId, slotId, packetId);
	}

	public boolean processNPCClick1(NPC npc) {
		return controller == null || !inited || controller.processNPCClick1(npc);
	}

	public boolean canSummonFamiliar() {
		return controller == null || !inited || controller.canSummonFamiliar();
	}

	public boolean canUseFamiliarSpecial() {
		return controller == null || !inited || controller.canUseFamiliarSpecial();
	}

	public boolean processNPCClick2(NPC npc) {
		return controller == null || !inited || controller.processNPCClick2(npc);
	}

	public boolean processNPCClick3(NPC npc) {
		return controller == null || !inited || controller.processNPCClick3(npc);
	}

	public boolean processObjectClick2(WorldObject object) {
		return controller == null || !inited || controller.processObjectClick2(object);
	}

	public boolean processObjectClick3(WorldObject object) {
		return controller == null || !inited || controller.processObjectClick3(object);
	}

	public boolean processItemOnNPC(NPC npc, Item item) {
		return controller == null || !inited || controller.processItemOnNPC(npc, item);
	}

	public boolean canDropItem(Item item) {
		return controller == null || !inited || controller.canDropItem(item);
	}

	public void removeController() {
		controller = null;
		lastControllerArguments = null;
		lastController = null;
		inited = false;
	}

	public Object[] getLastControlerArguments() {
		return lastControllerArguments;
	}

	public void setLastControlerArguments(Object[] lastControlerArguments) {
		this.lastControllerArguments = lastControlerArguments;
	}

	public boolean processObjectClick4(WorldObject object) {
		return controller == null || !inited || controller.processObjectClick4(object);
	}

	public boolean processObjectClick5(WorldObject object) {
		return controller == null || !inited || controller.processObjectClick5(object);
	}

	public boolean handleItemOnObject(WorldObject object, Item item) {
		return controller == null || !inited || controller.handleItemOnObject(object, item);
	}

	public boolean handleItemOnPlayer(Player p2, Item item) {
		return controller == null || !inited || controller.handleItemOnPlayer(p2, item);
	}

	public boolean handlePlayerOption5(Player p2) {
		return controller == null || !inited || controller.handlePlayerOption5(p2);
	}

	public boolean canPickupItem(FloorItem item) {
		return controller == null || !inited || controller.canPickupItem(item);
	}

	public boolean canTrade() {
		return controller == null || !inited || controller.canTrade();
	}

	public void forceSetLastController(String lastController) {
		this.lastController = lastController;
	}
}
