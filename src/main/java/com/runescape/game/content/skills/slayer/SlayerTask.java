package com.runescape.game.content.skills.slayer;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class SlayerTask {
	
	/**
	 * The name of the task to kill
	 */
	private final String name;
	
	/**
	 * The tasks type
	 */
	private final SlayerTasks task;
	
	/**
	 * The amount we should kill
	 */
	private int amountToKill;
	
	public SlayerTask(String name, int amountToKill, SlayerTasks task) {
		this.name = name;
		this.setAmountToKill(amountToKill);
		this.task = task;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the task
	 */
	public SlayerTasks getTask() {
		return task;
	}

	/**
	 * @return the amountToKill
	 */
	public int getAmountToKill() {
		return amountToKill;
	}

	/**
	 * @param amountToKill the amountToKill to set
	 */
	public void setAmountToKill(int amountToKill) {
		this.amountToKill = amountToKill;
	}

	/**
	 * Deducts the task amount by 1
	 */
	public void deductTaskAmount() {
		this.amountToKill--;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SlayerTask) {
			return ((SlayerTask) obj).getName().equals(name);
		}
		return super.equals(obj);
	}
}
