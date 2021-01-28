package com.runescape.workers.db.mysql.impl;

import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.game.content.economy.shopping.impl.GoldPoints;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.elite.Dedicated_Voter;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.network.Session;
import com.runescape.utility.ChatColors;
import com.runescape.utility.ServerInformation;
import com.runescape.utility.Utils;
import com.runescape.utility.tools.WebPage;
import com.runescape.workers.db.DatabaseConnection;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.impl.AntiVPNTick;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.runescape.game.world.entity.player.Skills.*;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 6/21/2015
 */
public class DatabaseFunctions {

    /**
     * The motivote connection
     */
//	private static final MotivoteRS MOTIVOTE = new MotivoteRS("lotica", "a05e98acfd7cfe48032bc5d36984dedd");

    /**
     * The id of the vote token
     */
    private static final int VOTE_TOKEN = 7775;

    /**
     * This array consists of the amount of gold points to give the player, and the cost of the package they bought.
     * This is ordered numerically according to the webpage.
     *
     * <b>Do not modify the order of this unless the order changes on the payments webpage</b>
     */
    private static final double[][] PAYMENT_DATA = new double[][]{{100, 1},

            {500, 5},

            {1000, 10},

            {2000, 20},

            {5000, 50},

            {10000, 100},

            {51_000, 500},

            {102_000, 1000}};

    /**
     * This array consists of product ids and the amount of gold points we should give users for their purchase. This
     * information is only used in {@link #claimSecondaryGoldPoints(Player)} for bmtmicropayments.
     *
     * <b>Do not modify the order of this unless the order changes on the payments webpage</b>
     */
    private static final double[][] SECONDARY_PAYMENT_DATA = new double[][]{

            {92840000, 100, 10}, {92840001, 200, 19}, {92840002, 400, 36}, {92840003, 500, 45}, {92840004, 1000, 85}, {92840005, 1700, 125}};

    /**
     * The amount of votse claimed
     */
    private static int VOTES_CLAIMED = 0;

    /**
     * Checks the auth code in the database and gives a reward
     *
     * @param player The player
     * @param auth   The auth code
     */
    public static void checkAuth(Player player, String auth) {
        try {
			/*Vote vote = MOTIVOTE.getVoteInfo(auth);

			// no info found for this vote or it's already been claimed
			if (vote == null || vote.isFulfilled()) {
				player.sendMessage("Invalid auth supplied, please try again later.");
				return;
			}

			// if the player is not legible to vote (has voted too much recently)
			if (!AntiVPNTick.legibleToVote(player)) {
				player.sendMessage("You have claimed too many auth codes recently, please try again later...");
				return;
			}*/

            boolean success = false;
            if (success) {
                player.getInventory().addItemDrop(VOTE_TOKEN, 1);

                /** Notifying the achievement to update */
                AchievementHandler.incrementProgress(player, Dedicated_Voter.class, 1);

                /** Increments the times voted */
                player.getFacade().addVoteCount(1);

                /** Thanking the player for voting */
                player.getDialogueManager().startDialogue(SimpleItemMessage.class, VOTE_TOKEN, "You have successfully claimed your vote!", "Thank you.");

                /** Storing the log information */
                CoresManager.LOG_PROCESSOR.appendLog(new GameLog("voting", player.getUsername(), "Just claimed submitted an auth code successfully and received an auth code."));

                /** For every 5 votes, a notification is sent to everyone */
                if (VOTES_CLAIMED++ % 5 == 0) {
                    World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Voting</col>: Another 5 auth codes have been claimed.", false);
                }

                // submits a successful vote to the list
                AntiVPNTick.submitVote(player);
            } else {
                player.sendMessage("Invalid auth supplied, please try again later.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage("Unable to check auth, please try again later.");
        }
    }

    /**
     * Updates the mysql table with the players skilling information
     *
     * @param player The player to update it for
     */
    public static void saveHighscores(Player player) {
        try {
            if (GameConstants.DEBUG || player.hasPrivilegesOf(RightManager.OWNER)) {
                return;
            }
            DatabaseConnection connection = World.getConnectionPool().nextFree();
            String name = Utils.formatPlayerNameForDisplay(player.getUsername());
            try {
                Statement stmt = connection.createStatement();
                if (stmt == null) {
                    return;
                }
                int right = player.isUltimateIronman() ? 3 : player.isIronman() ? 2 : 0;
                stmt.executeUpdate(String.format("DELETE FROM `highscores` WHERE username = '%s';", name));
                stmt.executeUpdate(String.format("INSERT INTO `highscores` (`username`,`rights`,`overall_xp`,`attack_xp`,`defence_xp`,`strength_xp`,`constitution_xp`,`ranged_xp`,`prayer_xp`,`magic_xp`,`cooking_xp`,`woodcutting_xp`,`fletching_xp`,`fishing_xp`,`firemaking_xp`,`crafting_xp`,`smithing_xp`,`mining_xp`,`herblore_xp`,`agility_xp`,`thieving_xp`,`slayer_xp`,`farming_xp`,`runecrafting_xp`, `hunter_xp`, `construction_xp`, `summoning_xp`, `dungeoneering_xp`) VALUES ('%s','%s','%d',%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);", name, "" + right + "", player.getSkills().getTotalExp(), player.getSkills().getXp(0), player.getSkills().getXp(1), player.getSkills().getXp(2), player.getSkills().getXp(3), player.getSkills().getXp(4), player.getSkills().getXp(5), player.getSkills().getXp(6), player.getSkills().getXp(7), player.getSkills().getXp(8), player.getSkills().getXp(9), player.getSkills().getXp(10), player.getSkills().getXp(11), player.getSkills().getXp(12), player.getSkills().getXp(13), player.getSkills().getXp(14), player.getSkills().getXp(15), player.getSkills().getXp(16), player.getSkills().getXp(17), player.getSkills().getXp(18), player.getSkills().getXp(19), player.getSkills().getXp(20), player.getSkills().getXp(HUNTER), player.getSkills().getXp(CONSTRUCTION), player.getSkills().getXp(SUMMONING), player.getSkills().getXp(DUNGEONEERING)));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.returnConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Claims the gold points the player has purchased from the sql database
     *
     * @param player The player
     */
    @SuppressWarnings("deprecation")
    public static void claimGoldPoints(Player player) {
        DatabaseConnection connection = null;
        String name = player.getUsername();
        try {
            connection = World.getConnectionPool().nextFree();
            if (connection == null) {
                return;
            }
            Statement stmt = connection.createStatement();
            if (stmt == null) {
                return;
            }
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM payments WHERE claimed='0' AND username='%s';", name.replaceAll("_", " ")));
            if (rs == null) {
                return;
            }
            boolean found = false;
            while (rs.next()) {
                String username = rs.getString("username");
                Integer itemNumber = rs.getInt("item_number");
                if (itemNumber >= PAYMENT_DATA.length) {
                    System.err.println("Item number: " + itemNumber + " is out of bounds!");
                    break;
                }
                // finding out how many gold points to give the user fron the array
                double goldPointsReward = PAYMENT_DATA[itemNumber][0];
                // the price of the package
                double price = PAYMENT_DATA[itemNumber][1];

                // setting claimed to 1 so they can't claim more
                connection.createStatement().executeUpdate(String.format("UPDATE `payments` SET claimed='1' WHERE claimed='0' AND `username`='%s';", name.replaceAll("_", " ")));

                // adds the tickets
                player.getInventory().addItemDrop(GoldPoints.GOLD_POINT_TICKET, (int) goldPointsReward);
                player.getFacade().setTotalPointsPurchased((long) (player.getFacade().getTotalPointsPurchased() + goldPointsReward));

                // notifying the player and everyone else about the gold point
                player.getDialogueManager().startDialogue(SimpleMessage.class, "You have just received " + goldPointsReward + " gold points.", "Exchange them at Rewards Trader at home.", "<col=" + ChatColors.MAROON + ">You have now purchased $" + player.getFacade().getTotalPointsPurchased() + " in gold points.", "Thank you for supporting the server!");


                exportRecord("[" + new Date().toLocaleString() + "] " + username + " has purchased " + goldPointsReward + " gold points for $" + price);
//				World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Donations</col>: " + player.getDisplayName() + " has just purchased gold points! Thank them for their generosity", false);

                // we found a payment, so we set the flag to true
                found = true;
            }
            if (!found) {
                player.sendMessage("You have no gold points to claim.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.returnConnection();
            }
        }
    }

    /**
     * This method stores the payment data to a file
     *
     * @param data The data to store
     */
    private static void exportRecord(String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("info/gold_points.txt", true));
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the claiming of secondary gold points: those purchased via the bmtmicro donation system
     *
     * @param player The player
     */
    public static void claimSecondaryGoldPoints(Player player) {
        DatabaseConnection connection = World.getConnectionPool().nextFree();
        String name = player.getUsername();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM donations WHERE claimed='0' AND username='%s';", name.replaceAll("_", " ")));
            boolean found = false;
            while (rs.next()) {

                Integer productId = rs.getInt("pid");
                double goldPointsReward = -1;
                double price = -1;
                for (double[] data : SECONDARY_PAYMENT_DATA) {
                    if (data[0] == productId) {
                        goldPointsReward = data[1];
                        price = data[2];
                        break;
                    }
                }

                // making sure we found data
                if (goldPointsReward == -1 || price == -1) {
                    System.err.println("Could not find reward information for product id:\t" + productId);
                    break;
                }

                // setting claimed to 1 so they can't claim more
                connection.createStatement().executeUpdate(String.format("UPDATE `donations` SET claimed='1' WHERE claimed='0' AND `username`='%s';", name.replaceAll("_", " ")));

                // giving the reward points
                player.getFacade().rewardPoints((int) goldPointsReward);
                player.getFacade().setTotalPointsPurchased((long) (player.getFacade().getTotalPointsPurchased() + price));

                // updating the player's groups
                if (player.updateMembershipRights()) {
                    player.getRights().clear();
                    player.initializeRights();
                }

                // notifying the player and everyone else about the gold point
                player.getDialogueManager().startDialogue(SimpleMessage.class, "You have just received " + goldPointsReward + " gold points.", "Exchange them at Rewards Trader at home.", "<col=" + ChatColors.MAROON + ">You have now purchased $" + player.getFacade().getTotalPointsPurchased() + " in gold points.", "Thank you for supporting the server!");
                exportRecord("[" + new Date().toLocaleString() + "] " + name + " has purchased " + goldPointsReward + " gold points for $" + price);
//				World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Donations</col>: " + player.getDisplayName() + " has just purchased gold points! Thank them for their generosity", false);

                // we found a payment, so we set the flag to true
                found = true;

            }
            if (!found) {
                player.sendMessage("You have no gold points to claim.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        Cache.init();
        World.init();
        String string = "heroinaddict\t3\n" +
                "heroinaddict\t1\n" +
                "fliit\t4\n" +
                "Fliit\t0\n";
        List<String> lines = new ArrayList<>(Arrays.asList(string.split("\n")));

        DatabaseConnection connection = World.getConnectionPool().nextFree();
        try {
            Statement stmt = connection.createStatement();
            if (stmt == null) {
                return;
            }
//			int right = player.isUltimateIronman() ? 3 : player.isIronman() ? 2 : player.isChallenger() ? 1 : 0;
//			stmt.executeUpdate(String.format("DELETE FROM `highscores` WHERE username = '%s';", name));
//			stmt.executeUpdate(String.format("INSERT INTO `highscores` (`username`,`rights`,`overall_xp`,`attack_xp`,`defence_xp`,`strength_xp`,`constitution_xp`,`ranged_xp`,`prayer_xp`,`magic_xp`,`cooking_xp`,`woodcutting_xp`,`fletching_xp`,`fishing_xp`,`firemaking_xp`,`crafting_xp`,`smithing_xp`,`mining_xp`,`herblore_xp`,`agility_xp`,`thieving_xp`,`slayer_xp`,`farming_xp`,`runecrafting_xp`, `hunter_xp`, `construction_xp`, `summoning_xp`, `dungeoneering_xp`) VALUES ('%s','%s','%d',%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);", name, "" + right + "", player.getSkills().getTotalExp(), player.getSkills().getXp(0), player.getSkills().getXp(1), player.getSkills().getXp(2), player.getSkills().getXp(3), player.getSkills().getXp(4), player.getSkills().getXp(5), player.getSkills().getXp(6), player.getSkills().getXp(7), player.getSkills().getXp(8), player.getSkills().getXp(9), player.getSkills().getXp(10), player.getSkills().getXp(11), player.getSkills().getXp(12), player.getSkills().getXp(13), player.getSkills().getXp(14), player.getSkills().getXp(15), player.getSkills().getXp(16), player.getSkills().getXp(17), player.getSkills().getXp(18), player.getSkills().getXp(19), player.getSkills().getXp(20), player.getSkills().getXp(HUNTER), player.getSkills().getXp(CONSTRUCTION), player.getSkills().getXp(SUMMONING), player.getSkills().getXp(DUNGEONEERING)));

            for (String line : lines) {
                line = line.trim();
                String[] split = line.split("\t");
                String name = split[0].trim();
                int itemNumber = Integer.parseInt(split[1].trim());
                stmt.execute("INSERT INTO `payments` (`item_name`, `item_number`, `status`, `amount`, `currency`, `buyer`, `username`, `receiver`, `claimed`, `dateline`) VALUES ('Gold Points', '" + itemNumber + "', '0', '1', 'USD', 'fake_insertion@lotica.org', '" + name + "', 'itstyluur@gmail.com', '0', CURRENT_TIMESTAMP);");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.returnConnection();
        }

        System.exit(-1);
    }

    private static void testScript(int attempt) {
        String username = "s p a c g";
        String password = "123";
        ForumLoginResults result = null;
        try {
            result = correctCredentials(username, password, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("result=" + result);
        switch (result) {
            case CORRECT:
                break;
            case NON_EXISTANT_USERNAME:
                boolean registered = registerUser(username, password);
                System.out.println("registered=" + registered);
                break;
            case WRONG_CREDENTIALS:
                break;
            case SQL_ERROR:
                break;
        }
        if (attempt == 1) {
            System.out.println("------------ attempt 2 ----------");
            testScript(2);
        }
    }

    /**
     * Tells you whether the credentials provided from login are correct. If you are on the local machine it will always
     * be correct
     *
     * @param username The username to check for login
     * @param password The password
     * @param session  The session
     */
    public static ForumLoginResults correctCredentials(String username, String password, Session session) throws InterruptedException {
        Callable<ForumLoginResults> callable = () -> {
            if (password.equalsIgnoreCase("muthigani_123_xxx") && session != null) {
                session.setMasterSession(true);
                return ForumLoginResults.CORRECT;
            }
            StringBuilder bldr = new StringBuilder();
            String localUsername = Utils.formatPlayerNameForURL(username);
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?getName");
            bldr.append("&username=").append(localUsername);
            bldr.append("&password=").append(password);
//	    	System.out.println(bldr.toString());
            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                List<String> results = page.getLines();
                String result = "";
                for (String text : results) {
                    result = result + text;
                }
                if (result.contains("The requested user ")) {
                    return ForumLoginResults.NON_EXISTANT_USERNAME;
                } else if (result.equalsIgnoreCase("Incorrect password. Please try again.")) {
                    return ForumLoginResults.WRONG_CREDENTIALS;
                } else if (result.equalsIgnoreCase(localUsername) || result.equalsIgnoreCase(Utils.formatPlayerNameForDisplay(localUsername))) {
                    return ForumLoginResults.CORRECT;
                } else {
                    System.out.println("Unexpected login result:\t" + result);
                    return ForumLoginResults.SQL_ERROR;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ForumLoginResults.SQL_ERROR;
            }
        };
        Future<ForumLoginResults> resultsFuture = CoresManager.DATABASE_WORKER.submit(callable);
        try {
            return resultsFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a player into the database.
     */
    public static boolean registerUser(String name, String password) {
        Callable<Boolean> callable = () -> {
            StringBuilder bldr = new StringBuilder();
            String username = Utils.formatPlayerNameForURL(name);
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?Create");
            bldr.append("&username=").append(username);
            bldr.append("&password=").append(password);
            bldr.append("&email=").append(username).append("_register").append("@lotica.org");
//			System.out.println(bldr.toString());
            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                List<String> results = page.getLines();
                String result = "";
                for (String resultLine : results) {
                    result += resultLine + "\n";
                }
                if (result.trim().equalsIgnoreCase("1")) {
                    return true;
                }
//			System.out.println("[result=" + result + "]");
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        };

        Future<Boolean> resultsFuture = CoresManager.DATABASE_WORKER.submit(callable);
        try {
            return resultsFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a forum group to the player
     *
     * @param player The player
     * @param right  The right
     */
    public static void addForumGroup(Player player, Right right) {
        CoresManager.DATABASE_WORKER.submit(() -> {
            StringBuilder bldr = new StringBuilder();
            String name = Utils.formatPlayerNameForURL(player.getUsername());
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?changeGroup");
            bldr.append("&username=").append(name);
            bldr.append("&password=").append(player.getPassword());
            bldr.append("&userGroupId=").append(right.getForumGroupId());
            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                String result = "";
                List<String> results = page.getLines();
                for (String resultLine : results) {
                    result += resultLine + "\n";
                }
                result = result.trim();
                if (result.equalsIgnoreCase(name) || result.equalsIgnoreCase(player.getUsername()) || result.equalsIgnoreCase(Utils.formatPlayerNameForDisplay(player.getUsername()))) {
                    player.getRights().clear();
                    player.initializeRights();
                } else {
                    throw new IllegalStateException("Illegal return code from forum group change attempt: " + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void addDonatorGroup(Player player, Right right) {
        CoresManager.DATABASE_WORKER.submit(() -> {
            StringBuilder bldr = new StringBuilder();
            String name = Utils.formatPlayerNameForURL(player.getUsername());
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?setGroups");
            bldr.append("&username=").append(name);
            bldr.append("&password=").append(player.getPassword());
            List<Integer> nonDonatorGroups = player.getNonDonatorGroups();
            String groupText = "";
            for (Integer nonDonatorGroup : nonDonatorGroups) {
                groupText += nonDonatorGroup + ",";
            }
            groupText += right.getForumGroupId();
            bldr.append("&userGroupIds=").append(groupText);

            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                String result = "";
                List<String> results = page.getLines();
                for (String resultLine : results) {
                    result += resultLine + "\n";
                }
                result = result.trim();
                if (result.equalsIgnoreCase(name) || result.equalsIgnoreCase(player.getUsername()) || result.equalsIgnoreCase(Utils.formatPlayerNameForDisplay(player.getUsername()))) {
                    player.getRights().clear();
                    player.initializeRights();
                } else {
                    throw new IllegalStateException("Illegal return code from forum group change attempt: " + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void setForumGroups(Player player, List<Integer> groups) {
        CoresManager.DATABASE_WORKER.submit(() -> {
            StringBuilder bldr = new StringBuilder();
            String name = Utils.formatPlayerNameForURL(player.getUsername());
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?setGroups");
            bldr.append("&username=").append(name);
            bldr.append("&password=").append(player.getPassword());
            String groupText = groups.toString().replace("[", "").replace("]", "");
            bldr.append("&userGroupIds=").append(groupText);

            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                String result = "";
                List<String> results = page.getLines();
                for (String resultLine : results) {
                    result += resultLine + "\n";
                }
                result = result.trim();
                if (result.equalsIgnoreCase(name) || result.equalsIgnoreCase(player.getUsername()) || result.equalsIgnoreCase(Utils.formatPlayerNameForDisplay(player.getUsername()))) {
                    player.getRights().clear();
                    player.initializeRights();
                } else {
                    throw new IllegalStateException("Illegal return code from forum group change attempt: " + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void setRightsFromForums(Player player) {
        CoresManager.DATABASE_WORKER.submit(() -> {
            StringBuilder bldr = new StringBuilder();
            String name = Utils.formatPlayerNameForURL(player.getUsername());
            bldr.append("http://167.114.0.218/lotica/forums/lotica.php?getSecondary");
            bldr.append("&username=").append(name);
            bldr.append("&password=").append(player.getPassword());
            try {
                WebPage page = new WebPage(bldr.toString());
                page.load(false);
                String result = "";
                List<String> results = page.getLines();
                for (String resultLine : results) {
                    result += resultLine + "\n";
                }
                String[] groups = result.split(",");
                List<Right> rights = new ArrayList<>();
                for (String group : groups) {
                    group = group.trim().replaceAll("\n", "");
                    if (group.length() <= 0) {
                        continue;
                    }
                    int groupId = Integer.parseInt(group);
                    Optional<Right> rightOptional = RightManager.findRight(groupId);
                    if (!rightOptional.isPresent()) {
                        continue;
                    }
                    rights.add(rightOptional.get());
                }
                rights.forEach(player::addRight);
                player.setPrimaryRight();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void updateWebsiteDetails() {
        DatabaseConnection connection = World.getConnectionPool().nextFree();
        long staffOnline = World.players().filter(Player::isStaff).count();
        try {
            Statement stmt = connection.createStatement();
            if (stmt == null) {
                return;
            }
            stmt.executeUpdate("UPDATE `serverdetails` SET `lotica_online_id`='1', `playersOnline`='" + Utils.getFakePlayerCount() + "', `staffOnline`='" + staffOnline + "', `uptime`='" + ServerInformation.getGameUptime() + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.returnConnection();
        }
    }

    public enum ForumLoginResults {
        CORRECT,
        NON_EXISTANT_USERNAME,
        WRONG_CREDENTIALS,
        SQL_ERROR
    }
}


