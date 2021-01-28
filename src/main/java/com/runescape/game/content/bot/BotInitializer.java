package com.runescape.game.content.bot;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.OutputStream;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/18/2016
 */
public class BotInitializer {

	/**
	 * The list of active bots
	 */
	private static final List<AbstractBot> BOTS = new ArrayList<>();

	public static void initializeBots() {
		for (String directory : Utils.getSubDirectories(BotInitializer.class)) {
			try {
				BOTS.addAll(Utils.getClassesInDirectory(AbstractBot.class.getPackage().getName() + "." + directory).stream().map(clazz -> (AbstractBot) clazz).collect(Collectors.toList()));
				BOTS.forEach(BotInitializer::prepareBot);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		createWorker();
	}

	private static void prepareBot(AbstractBot bot) {
		for (int i = 0; i < bot.getBotAmounts(); i++) {
			bot.setIndex(i);
		}
		loginBot(bot);
	}

	private static void loginBot(AbstractBot bot) {
		try {
			for (int i = 0; i < bot.getBotAmounts(); i++) {
				String name = bot.botNames()[bot.getIndex()];
				OutputStream body = new OutputStream();
				body.writeByte(36);
				body.writeString(name);
				body.writeString(bot.getIdentifier());
				for (int k = 5; k < 9; k++) { body.writeInt(k); }
				final Socket socket = new Socket("127.0.0.1", 43594);
				socket.getOutputStream().write(body.getBuffer());
				CoresManager.scheduleAtFixedRate(() -> {
					try {
						handleBotTick(socket);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}, 30, 30, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void handleBotTick(Socket socket) throws IOException, InterruptedException {
		/*int avail = socket.getInputStream().available();
		if (avail == -1) { socket.close(); }
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.write(0);*/
		OutputStream body = new OutputStream();
		body.writeByte(16);
		body.writeShort(0);
		socket.getOutputStream().write(body.getBuffer());
	}

	private static void createWorker() {
		CoresManager.scheduleAtFixedRate(() -> BOTS.forEach(botInstance -> {
			try {
				checkBots();
				if (botInstance.getBot() != null && botInstance.getBot().isRunning()) {
					if (botInstance.shouldPulse()) {
						botInstance.setLastTimePulsed(System.currentTimeMillis());
						botInstance.onPulse();
					} else if (!botInstance.getHoverTile().withinDistance(botInstance.getBot(), 1)) {
						botInstance.getBot().addWalkSteps(botInstance.getHoverTile().getX(), botInstance.getHoverTile().getY());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}), 1, 1, TimeUnit.SECONDS);
	}

	private static void checkBots() {
		BOTS.forEach(botInstance -> {
			// if the bot was created but no longer exists.
			if (botInstance.getBot() != null && !World.containsPlayer(botInstance.botNames()[botInstance.getIndex()]) && botInstance.getBot().getAttribute("logged_in_bot", false)) {
				botInstance.setBot(null);
				loginBot(botInstance);
			}
		});
	}

	public static void onLogin(Player bot) {
		BOTS.forEach(botInstance -> {
			String identifier = bot.getAttribute("fake_bot_identifier", "");
			if (identifier.equals(botInstance.getIdentifier())) {
				botInstance.setBot(bot);
				bot.putAttribute("logged_in_bot", true);
			}
		});
	}


}
