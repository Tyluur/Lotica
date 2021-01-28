package com.runescape.game.world.entity.player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 9/22/2015
 */
public class SecurityDetails {

	public SecurityDetails() {
		this.securityQuestion = this.securityAnswer = "none";
	}

	/**
	 * The computer address the security question was set with
	 */
	private String computerAddressSetWith;

	/**
	 * The security question the player sets for themselves
	 */
	private String securityQuestion;

	/**
	 * The answer to the player's security question
	 */
	private String securityAnswer;

	/**
	 * To find if the player has advanced security enabled, we check if they have modified the values for the security
	 * details
	 *
	 * @return {@code True} if they have it enabled
	 */
	public boolean hasSecurityEnabled() {
		return !securityQuestion.equals("none") && !securityAnswer.equals("none");
	}

	/**
	 * Sets the answer to the security question
	 *
	 * @param securityAnswer
	 * 		The answer
	 */
	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	/**
	 * Sets the security question
	 * @param securityQuestion The question to set
	 */
	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	/**
	 * Gets the security answer
	 */
	public String getSecurityAnswer() {
		return securityAnswer;
	}

	/**
	 * Gets the security question
	 */
	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public String getComputerAddressSetWith() {
		return this.computerAddressSetWith;
	}

	public void setComputerAddressSetWith(String computerAddressSetWith) {
		this.computerAddressSetWith = computerAddressSetWith;
	}
}