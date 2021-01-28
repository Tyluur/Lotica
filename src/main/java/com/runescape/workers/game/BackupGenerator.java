package com.runescape.workers.game;

import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.PlayerSaving;

import java.io.*;
import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class BackupGenerator implements Runnable {

	/** The directory that the backup will be taken of */
	private static final String BACKUP_DIRECTORY = PlayerSaving.FILES_LOCATION;

	/** The directory that backups will be placed */
	private static final String BACKUP_SAVE_DIRECTORY = GameConstants.HOSTED ? System.getProperty("user.home") + "/Dropbox/Live Server/backups/" : "./data/backups/";

	/** The file that has information about the last time we took a backup */
	private File file = new File(BACKUP_SAVE_DIRECTORY + "backup.txt");

	/** The amount of hours we must wait for the backup to happen */
	private static final int HOURS_PER_BACKUP = 24;

	@Override
	public void run() {
		try {
			// we dont need backups on localhost
			if (GameConstants.DEBUG) {
				return;
			}
			long timeFromFile = getTimeFromFile();
			/**
			 * Checking to see if there should be a backup generated
			 */
			if (timeFromFile == -1 || TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - getTimeFromFile()) >= HOURS_PER_BACKUP) {
				PrintWriter writer;
				backupCharacters();
				try {
					writer = new PrintWriter(file);
					writer.print(System.currentTimeMillis());
					writer.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				System.out.println("Generated a backup!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the long time from the file
	 *
	 * @return A {@code Long} {@code Object}
	 */
	private long getTimeFromFile() {
		if (file.exists()) {
			String text = Utils.getText(file.getAbsolutePath()).trim();
			return Long.parseLong(text);
		}
		return -1;
	}

	@SuppressWarnings("deprecation")
	private void backupCharacters() {
		File characters = new File(BACKUP_DIRECTORY);
		String dateInformation = "" + DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate();
		File archive = new File(BACKUP_SAVE_DIRECTORY + dateInformation + ".zip");
		if (!archive.getParentFile().exists()) {
			archive.getParentFile().mkdirs();
		}
		if (!archive.exists()) {
			try {
				if (characters.list().length == 0) {
					System.out.println("[Auto-Backup] The characters folder is empty.");
					return;
				}
				zipDirectory(characters, archive);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Zips a directory into an archive
	 *
	 * @param directory
	 * 		The directory to zip to
	 * @param base
	 * 		The base folder
	 * @param zos
	 * 		The outputstream
	 * @throws IOException
	 * 		Exception thrown
	 */
	private void zip(File directory, File base, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (File file2 : files) {
			if (file2.isDirectory()) {
				zip(file2, base, zos);
			} else {
				FileInputStream in = new FileInputStream(file2);
				ZipEntry entry = new ZipEntry(file2.getPath().substring(base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	/**
	 * Zips the directory to a .zip file
	 *
	 * @param folder
	 * 		The folder to zip
	 * @param zipFile
	 * 		The zip file
	 * @throws IOException
	 * 		Exception thrown
	 */
	private void zipDirectory(File folder, File zipFile) throws IOException {
		ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zipFile));
		zip(folder, folder, z);
		z.close();
	}
}
