package com.runescape.game.world.entity.npc.combat;

public class NPCCombatDefinitions {

    public static final int MELEE = 0;
    public static final int RANGE = 1;
    public static final int MAGE = 2;
    public static final int SPECIAL = 3;
    public static final int SPECIAL2 = 4; // follows no distance
    public static final int PASSIVE = 0;
    public static final int AGRESSIVE = 1;

    private int hitpoints;
    private int attackAnim;
    private int defenceAnim;
    private int deathAnim;
    private int attackDelay;
    private int deathDelay;
    private int respawnDelay;
    private int maxHit;
    private int attackStyle;
    private int attackGfx;
    private int attackProjectile;

	private int aggressivenessType;

    public NPCCombatDefinitions(int hitpoints, int attackAnim, int defenceAnim, int deathAnim, int attackDelay, int deathDelay, int respawnDelay, int maxHit, int attackStyle, int attackGfx, int attackProjectile, int agressivenessType) {
        this.hitpoints = hitpoints;
        this.attackAnim = attackAnim;
        this.defenceAnim = defenceAnim;
        this.deathAnim = deathAnim;
        this.attackDelay = attackDelay;
        this.deathDelay = deathDelay;
        this.respawnDelay = respawnDelay;
        this.maxHit = maxHit;
        this.attackStyle = attackStyle;
        this.attackGfx = attackGfx;
        this.attackProjectile = attackProjectile;
        this.aggressivenessType = agressivenessType;
    }

    public static NPCCombatDefinitions constructFromText(String line) {
        // 1914 - 2000 393 397 2304 4 1 60 190 MELEE -1 -1 AGRESSIVE
        String[] splitedLine2 = line.split(" ");
        int hitpoints = Integer.parseInt(splitedLine2[0]);
        int attackAnim = Integer.parseInt(splitedLine2[1]);
        int defenceAnim = Integer.parseInt(splitedLine2[2]);
        int deathAnim = Integer.parseInt(splitedLine2[3]);
        int attackDelay = Integer.parseInt(splitedLine2[4]);
        int deathDelay = Integer.parseInt(splitedLine2[5]);
        int respawnDelay = Integer.parseInt(splitedLine2[6]);
        int maxHit = Integer.parseInt(splitedLine2[7]);
        int attackStyle;
        if (splitedLine2[8].equalsIgnoreCase("MELEE")) {
            attackStyle = NPCCombatDefinitions.MELEE;
        } else if (splitedLine2[8].equalsIgnoreCase("RANGE")) {
            attackStyle = NPCCombatDefinitions.RANGE;
        } else if (splitedLine2[8].equalsIgnoreCase("MAGE")) {
            attackStyle = NPCCombatDefinitions.MAGE;
        } else if (splitedLine2[8].equalsIgnoreCase("SPECIAL")) {
            attackStyle = NPCCombatDefinitions.SPECIAL;
        } else if (splitedLine2[8].equalsIgnoreCase("SPECIAL2")) {
            attackStyle = NPCCombatDefinitions.SPECIAL2;
        } else {
            throw new RuntimeException();
        }
        int attackGfx = Integer.parseInt(splitedLine2[9]);
        int attackProjectile = Integer.parseInt(splitedLine2[10]);
        int agressivenessType;
        if (splitedLine2[11].equalsIgnoreCase("PASSIVE")) {
            agressivenessType = NPCCombatDefinitions.PASSIVE;
        } else if (splitedLine2[11].equalsIgnoreCase("AGRESSIVE")) {
            agressivenessType = NPCCombatDefinitions.AGRESSIVE;
        } else {
            throw new RuntimeException();
        }
        return new NPCCombatDefinitions(hitpoints, attackAnim, defenceAnim, deathAnim, attackDelay, deathDelay, respawnDelay, maxHit, attackStyle, attackGfx, attackProjectile, agressivenessType);
    }

    public int getRespawnDelay() {
        return respawnDelay;
    }

    public int getDeathEmote() {
        return deathAnim;
    }

    public int getDefenceEmote() {
        return defenceAnim;
    }

    public int getAttackEmote() {
        return attackAnim;
    }

    public int getAttackGfx() {
        return attackGfx;
    }

    public int getAggressivenessType() {
        return aggressivenessType;
    }

    public int getAttackProjectile() {
        return attackProjectile;
    }

    public int getAttackStyle() {
        return attackStyle;
    }

    public int getAttackDelay() {
        return attackDelay;
    }

    public void setHitpoints(int amount) {
        this.hitpoints = amount;
    }

    public int getMaxHit() {
        return maxHit;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public int getDeathDelay() {
        return deathDelay;
    }

	public void setAttackDelay(int attackDelay) {
		this.attackDelay = attackDelay;
	}

	@Override
    public String toString() {
        return "[hitpoints=" + hitpoints + ", attackAnim=" + attackAnim + ", defenceAnim=" + defenceAnim + ", deathAnim=" + deathAnim + "]";
    }

    public void setMaxHit(int maxHit) {
        this.maxHit = maxHit;
    }

	public void setAttackStyle(int attackStyle) {
		this.attackStyle = attackStyle;
	}

    public void setAggressivenessType(int aggressivenessType) {
        this.aggressivenessType = aggressivenessType;
    }
}
