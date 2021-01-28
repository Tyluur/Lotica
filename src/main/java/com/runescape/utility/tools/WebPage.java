package com.runescape.utility.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WebPage {

	private String url;

	private ArrayList<String> lines;

	public WebPage(String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		url = url.replaceAll(" ", "%20");
		this.url = url;
		System.out.println("Loading webpage " + url + " on thread " + Thread.currentThread().getName());
	}

	public WebPage load(boolean advanced) throws IOException {
		lines = new ArrayList<>();
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			if (advanced) {
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "Mozilla Firefox");
				connection.setDoOutput(true);
				connection.setDoInput(true);
			} else {
				connection.setConnectTimeout(3000);
				connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			}
			InputStream input;
			if (advanced) {
				if (connection.getResponseCode() >= 400) {
					input = connection.getErrorStream();
				} else {
					input = connection.getInputStream();
				}
			} else {
				input = connection.getInputStream();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public ArrayList<String> getLines() {
		return lines;
	}

	public String getSingleLine() {
		String text = "";
		for (String line : getLines()) {
			text += line + "\n";
		}
		return text;
	}
}
