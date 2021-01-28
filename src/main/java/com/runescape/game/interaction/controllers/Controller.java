package com.runescape.game.interaction.controllers;

import com.runescape.game.content.Foods.Food;
import com.runescape.game.content.Pots.Pot;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;

public abstract class Controller {

	// private static final long serialVersionUID = 8384350746724116339L;

	public abstract void start();

	protected Player player;

	public Player getPlayer() {
		return player;
	}

	public final void setPlayer(Player player) {
		this.player = player;
	}

	@SuppressWarnings("unchecked")
	public <K> K getArgument(int index) {
		return (K) getArguments()[index];
	}

	public final Object[] getArguments() {
		return player.getControllerManager().getLastControlerArguments();
	}

	public final void setArguments(Object[] objects) {
		player.getControllerManager().setLastControlerArguments(objects);
	}

	public void removeController() {
		player.getControllerManager().removeController();
	}

	public boolean canEat(Food food) {
		return true;
	}

	public boolean canPot(Pot pot) {
		return true;
	}

	/**
	 * after the normal checks, extra checks, only called when you attacking
	 */
	public boolean keepCombating(Entity target) {
		return true;
	}

	public boolean canEquip(int slotId, int itemId) {
		return true;
	}

	public boolean canTrade() { return true; }

	/**
	 * after the normal checks, extra checks, only called when you start trying to attack
	 */
	public boolean canAttack(Entity target) {
		return true;
	}

	public void trackXP(int skillId, double addedXp) {

	}

	public double getExperienceModifier(int skillId, double experience) { return -1; }

	public boolean canDeleteInventoryItem(int itemId, int amount) {
		return true;
	}

	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		return true;
	}

	public boolean canAddInventoryItem(int itemId, int amount) {
		return true;
	}

	public boolean canPlayerOption1(Player target) {
		return true;
	}

	/**
	 * hits as ice barrage and that on multi areas
	 */
	public boolean canHit(Entity entity) {
		return true;
	}

	/**
	 * processes every game ticket, usualy not used
	 */
	public void process() {

	}

	public void moved() {

	}

	/**
	 * called once teleport is performed
	 */
	public void magicTeleported(int type) {

	}

	public void sendInterfaces() {

	}

	/**
	 * return can use script
	 */
	public boolean useDialogueScript(Object key) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processMagicTeleport(WorldTile toTile) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processItemTeleport(WorldTile toTile) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processObjectTeleport(WorldTile toTile) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick1(WorldObject object) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick1(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick2(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick3(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick2(WorldObject object) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick3(WorldObject object) {
		return true;
	}

	public boolean processObjectClick4(WorldObject object) {
		return true;
	}

	public boolean processObjectClick5(WorldObject object) {
		return true;
	}

	/**
	 * return let default death
	 */
	public boolean sendDeath() {
		return true;
	}

	/**
	 * return can move that step
	 */
	public boolean canMove(int dir) {
		return true;
	}

	/**
	 * return can set that step
	 */
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return true;
	}

	/**
	 * return remove controler
	 */
	public boolean login() {
		return true;
	}

	/**
	 * return remove controler
	 */
	public boolean logout() {
		return true;
	}

	public void forceClose() {
	}

	public boolean processItemOnNPC(NPC npc, Item item) {
		return true;
	}

	public boolean canDropItem(Item item) {
		return true;
	}

	public boolean canSummonFamiliar() {
		return true;
	}

	public boolean canUseFamiliarSpecial() {
		return true;
	}

	public boolean handleItemOnObject(WorldObject object, Item item) {
		return true;
	}

	public boolean handleItemOnPlayer(Player p2, Item item) {
		return true;
	}

	public boolean handlePlayerOption5(Player p2) { return true; }

	public boolean canPickupItem(FloorItem item) { return true; }

}
