package com.runescape.utility.tools;

import com.runescape.utility.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/10/2015
 */
public class ProfitCalculator {

	/** The location of the file with profit information */
	private static final String PROFIT_FILE = "./info/gold_points.txt";

	/** The map that will store payment data */
	private static final Map<String, Double> PAYMENT_DATA = new HashMap<>();

	public static void main(String[] args) {
		System.out.print("Enter the name of the month to calculate:\t");
		String monthName = TextIO.getlnString();
		List<String> payments = Utils.getFileText(PROFIT_FILE);
		for (String payment : payments) {
			String[] split = payment.split(" ");
			String currentMonth = split[0].replaceAll("\\[", "");
			if (!monthName.equals(currentMonth)) {
				//System.out.println("Skipped month: " + payment);
				continue;
			}

			String[] nameInformation = payment.split("has purchased");
			String[] nameSplit = nameInformation[0].split("\\]");

			String name = nameSplit[nameSplit.length - 1].trim();

			String[] paymentInformation = payment.split("gold points for");
			String stringPayment = paymentInformation[paymentInformation.length - 1].replaceAll("\\$", "").trim();
			Double amountPaid = Double.parseDouble(stringPayment);
			insertPayment(name, amountPaid);
		}
		Map<String, Double> sortedMap = PAYMENT_DATA.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("In " + monthName + ", we made " + getMonthlyProfit());
		for(int i = 0; i < 3; i++) {
			System.out.println();
		}
		sortedMap.entrySet().stream().forEach(entry -> System.out.println(Utils.formatPlayerNameForDisplay(entry.getKey()) + " has paid $" + entry.getValue() + ""));
	}

	/**
	 * Gets the total amount of money we made this month
	 *
	 * @return The amount
	 */
	private static Double getMonthlyProfit() {
		Double total = 0.0D;
		for (Entry<String, Double> entry : PAYMENT_DATA.entrySet()) {
			total += entry.getValue();
		}
		return total;
	}

	/**
	 * Inserts a payment into the {@link #PAYMENT_DATA} map
	 *
	 * @param name
	 * 		The name of the user
	 * @param amount
	 * 		The amount to insert
	 */
	private static void insertPayment(String name, double amount) {
		Double totalPaid = PAYMENT_DATA.get(name);
		if (totalPaid == null) {
			totalPaid = amount;
		} else {
			totalPaid += amount;
		}
		PAYMENT_DATA.put(name, totalPaid);
	}
}
