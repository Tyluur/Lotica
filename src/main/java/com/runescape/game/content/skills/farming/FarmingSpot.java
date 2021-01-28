package com.runescape.game.content.skills.farming;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/21/2016
 */
public class FarmingSpot implements FarmingConstants {

	private SpotInfo spotInfo;

	private ProductInfo productInfo;

	private int stage;

	private long cycleTime;

	private int harvestAmount;

	private boolean[] attributes;

	private transient Player player;
	
	public FarmingSpot(Player player, SpotInfo spotInfo) {
		this.player = player;
		this.spotInfo = spotInfo;
		cycleTime = Utils.currentTimeMillis();
		stage = 0; // stage 0 is default null
		harvestAmount = 0;
		attributes = new boolean[10]; // diseased, watered, dead,
		// firstCycle, usingCompost,
		// usingSuperCompost;
		renewCycle();
		player.getFarmingManager().getSpots().add(this);
	}
	
	public void renewCycle() {
		long constant = 30000L;
		if (productInfo != null) {
			cycleTime += (stage == 0 || GameConstants.DEBUG) ? 5000 : constant * productInfo.getCycleTime();
		} else { cycleTime += constant * 3; }
	}
	
	public void setActive(ProductInfo productInfo) {
		setProductInfo(productInfo);
		stage = -1;
		resetCycle();
	}
	
	public void setProductInfo(ProductInfo productInfo) {
		this.productInfo = productInfo;
	}
	
	private void resetCycle() {
		cycleTime = Utils.currentTimeMillis();
		harvestAmount = 0;
		for (int index = 0; index < attributes.length; index++) {
			if (index == 4) { continue; }
			attributes[index] = false;
		}
	}
	
	public void setCycleTime(long cycleTime) {
		setCycleTime(false, cycleTime);
	}
	
	public void setCycleTime(boolean reset, long cycleTime) {
		if (reset) { this.cycleTime = 0; }
		if (this.cycleTime == 0) { this.cycleTime = Utils.currentTimeMillis(); }
		this.cycleTime += cycleTime;
	}
	
	public void setIdle() {
		stage = 3;
		setProductInfo(null);
		refresh();
		resetCycle();
	}
	
	public void refresh() {
		if (player != null && player.getPackets() != null && spotInfo != null) {
			int value = spotInfo.getType() == COMPOST ? getConfigValue(spotInfo.getType()) : productInfo != null ? getConfigValue(spotInfo.getType()) + productInfo.getStageSkip() : stage;
			player.getPackets().sendConfig(spotInfo.getConfigFileId(), value);
//			System.out.println("refreshed spot nicely:\t" + spotInfo);
		} else {
			System.err.println("Error parsing data to refresh:\t\tplayer=" + player + "packets=," + (player == null ? "null" : player.getPackets()) + ", spotInfo=" + spotInfo);
		}
	}
	
	private int getConfigValue(int type) {
		if (type == HERBS) {
			return isDead() ? stage + 169 : getConfigBaseValue() + ((isDiseased() && stage != 0) ? stage + 127 : stage);
		} else if (type == TREES) {
			int baseValue = getConfigBaseValue() + (isDead() ? stage + 128 : (isDiseased() && stage != 0) ? stage + 64 : stage);
			if (hasChecked()) {
				baseValue += 2;
				if (!isEmpty()) { baseValue--; }
			}
			return baseValue;
		} else if (type == FRUIT_TREES) {
			int baseValue = stage + getConfigBaseValue();
			if (hasChecked()) { baseValue += getHarvestAmount(); } else if (isDead()) {
				baseValue += 20;
			} else if (isDiseased()) {
				baseValue += 12;
			} else if (!hasChecked() && reachedMaxStage()) {
				baseValue += 20;
			}
			if (isEmpty()) { baseValue += 19; }
			return baseValue;
		} else if (type == BUSHES) {
			int baseValue = stage + getConfigBaseValue();
			if (hasChecked()) { baseValue += getHarvestAmount(); } else if (isDead()) {
				baseValue += 128;
			} else if (isDiseased()) {
				baseValue += 65;
			} else if (!hasChecked() && reachedMaxStage()) // 250
			{ baseValue += 240; }
			return baseValue;
		} else if (type == COMPOST) {
			return isCleared() ? harvestAmount + 16 + (hasChecked() ? -1 : 0) : productInfo != null && reachedMaxStage() ? 0 : harvestAmount - stage;
		} else if (type == FLOWERS || type == HOPS || type == ALLOTMENT) {
			return getConfigBaseValue() + (isDead() ? stage + 192 : (isDiseased() && stage != 0) ? stage + 128 : isWatered() ? 64 + stage : stage);
		} else if (type == MUSHROOMS) {
			int value = stage + getConfigBaseValue();
			if (isDead()) {
				value += productInfo.getConfigIndex() == 1 ? 19 : 16;//14
			} else if (isDiseased() && stage != 0) { value += productInfo.getConfigIndex() == 1 ? 14 : 11; }
			return value;
		} else if (type == BELLADONNA) {
			int value = stage + getConfigBaseValue();
			if (isDead()) { value += 7; } else if (isDiseased() && stage != 0) { value += 4; }
			return value;
		}
		return stage + getConfigBaseValue();
	}
	
	public boolean isDead() {
		return attributes[2];
	}
	
	public int getConfigBaseValue() {
		if (productInfo != null) {
			if (productInfo.getType() == ALLOTMENT) {
				return 6 + (productInfo.getConfigIndex() * 7);
			} else if (productInfo.getType() == HERBS) {
				return 4 + (productInfo.getConfigIndex() * 7);
			} else if (productInfo.getType() == FLOWERS) {
				return 8 + (productInfo.getConfigIndex() * 5);
			} else if (productInfo.getType() == HOPS) {
				return 3 + (productInfo.getConfigIndex() * 5);
			} else if (productInfo.getType() == TREES || productInfo.getType() == FRUIT_TREES || productInfo.getType() == BUSHES) {
				return 8 + (productInfo.getConfigIndex() ^ 2 - 1);
			} else if (productInfo.getType() == MUSHROOMS) {
				return 4 + (productInfo.getConfigIndex() * 22);
			} else if (productInfo.getType() == BELLADONNA) { return 4; }
		}
		return stage;
	}
	
	public boolean isDiseased() {
		return attributes[0];
	}
	
	public void setDiseased(boolean diseased) {
		this.attributes[0] = diseased;
	}
	
	public boolean hasChecked() {
		return attributes[5];
	}
	
	public boolean isEmpty() {
		return attributes[6];
	}
	
	public void setEmpty(boolean empty) {
		this.attributes[6] = empty;
	}
	
	public boolean reachedMaxStage() {
		return stage == productInfo.getMaxStage();
	}
	
	public boolean isCleared() {
		return attributes[4];
	}
	
	public boolean isWatered() {
		return attributes[1];
	}
	
	public void setWatered(boolean watered) {
		this.attributes[1] = watered;
	}
	
	public void setCleared(boolean cleared) {
		this.attributes[4] = cleared;
	}
	
	public void setHarvestAmount(int harvestAmount) {
		this.harvestAmount = harvestAmount;
	}
	
	public void setDead(boolean dead) {
		this.attributes[2] = dead;
		if (dead) { setDiseased(false); }
	}
	
	public boolean process() {
		boolean remove = false;
		if (cycleTime == 0) { return false; }
		while (cycleTime < Utils.currentTimeMillis()) {
			if (productInfo != null) {
				if (hasChecked() && (isEmpty() || !hasMaximumRegeneration())) {
					if (isEmpty()) {
						setEmpty(false);
						if (productInfo.getType() == FRUIT_TREES) { setCycleTime(REGENERATION_CONSTANT); } else {
							cycleTime = 0;
						}
					} else if (!hasMaximumRegeneration()) {
						if (harvestAmount == 5) { cycleTime = 0; } else { cycleTime += REGENERATION_CONSTANT; }
						harvestAmount++;
					} else { cycleTime = 0; }
					refresh();
					return false;
				} else {
					increaseStage();
					if (reachedMaxStage() || isDead()) {
						cycleTime = 0;
						break;
					}
				}
			} else {
				if (spotInfo.getType() != COMPOST) {
					desecreaseStage();
					if (stage <= 0) {
						//remove();
						remove = true;
						break;
					}
				}
			}
			renewCycle();
		}
		return remove;
	}
	
	private void checkFactors() {
		if (isDiseased()) {
			if (reachedMaxStage()) {
				setDead(false);
				setDiseased(false);
			} else {
				if (isFirstCycle()) { setFirstCycle(false); } else { setDead(true); }
			}
		}
		if (productInfo.getType() == FRUIT_TREES || productInfo.getType() == BUSHES) {
			if (reachedMaxStage()) {
				setHarvestAmount(productInfo.getType() == BUSHES || productInfo == ProductInfo.Palm ? 4 : 6);
			}
		}
		setWatered(false);
		checkDisease();
	}
	
	private boolean hasMaximumRegeneration() {
		if (spotInfo.getType() != FRUIT_TREES && spotInfo.getType() != BUSHES) {
			return true;
		} else if (getHarvestAmount() != HARVEST_AMOUNTS[productInfo.getType()][1]) { return false; }
		return true;
	}
	
	public boolean canBeDiseased() {
		return !(stage == 0 && productInfo.getType() != BUSHES || reachedMaxStage() || isDiseased() || productInfo == ProductInfo.White_lily || productInfo == ProductInfo.Poison_ivy || productInfo.getType() == COMPOST);
	}
	
	private void checkDisease() {
		if (canBeDiseased()) {
			int baseValue = 35;
			if (isWatered()) { baseValue += 10; }
			if (getCompost()) { baseValue += 10; } else if (getSuperCompost()) { baseValue += 20; }
			if (Utils.random(baseValue) == 0) {
				setDiseased(true);
				refresh();
			}
		}
	}
	
	public void increaseStage() {
		stage++;
		if (productInfo != null) { checkFactors(); }
		refresh();
	}
	
	public void desecreaseStage() {
		setCleared(false);
		stage--;
		refresh();
	}
	
	public void remove() {
		player.getFarmingManager().getSpots().remove(this);
	}
	
	public boolean isFirstCycle() {
		return attributes[3];
	}
	
	public void setFirstCycle(boolean firstCycle) {
		this.attributes[3] = firstCycle;
	}
	
	public void setChecked(boolean checked) {
		this.attributes[5] = checked;
	}
	
	public boolean hasCompost() {
		return attributes[7] || attributes[8];
	}
	
	public boolean getSuperCompost() {
		return attributes[8];
	}
	
	public void setSuperCompost(boolean superCompost) {
		this.attributes[8] = superCompost;
	}
	
	public boolean hasGivenAmount() {
		return attributes[9];
	}
	
	public void setHasGivenAmount(boolean amount) {
		this.attributes[9] = amount;
	}
	
	public boolean getCompost() {
		return attributes[7];
	}
	
	public void setCompost(boolean compost) {
		this.attributes[7] = compost;
	}
	
	public boolean hasEmptyHarvestAmount() {
		return harvestAmount == 0;
	}

    public SpotInfo getSpotInfo() {
        return this.spotInfo;
    }

    public ProductInfo getProductInfo() {
        return this.productInfo;
    }

    public int getStage() {
        return this.stage;
    }

    public long getCycleTime() {
        return this.cycleTime;
    }

    public int getHarvestAmount() {
        return this.harvestAmount;
    }

    public boolean[] getAttributes() {
        return this.attributes;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}