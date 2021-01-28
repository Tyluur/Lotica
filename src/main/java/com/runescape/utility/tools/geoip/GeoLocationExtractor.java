package com.runescape.utility.tools.geoip;

import com.google.gson.Gson;
import com.runescape.utility.tools.WebPage;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/15/2016
 */
public class GeoLocationExtractor {

	/**
	 * The url for the free geoip service, replace %IP% with the ip address
	 */
	private static final String FREE_GEOIP_SITE = "http://www.freegeoip.net/json/%IP%";

	/**
	 * The gson instance for reading from files
	 */
	private static final ThreadLocal<Gson> GSON = new ThreadLocal<Gson>() {
		@Override
		protected Gson initialValue() {
			return new Gson();
		}
	};

	public static WorldLocation getWorldLocation(String ipAddress) {
		try {
			WebPage page = new WebPage(FREE_GEOIP_SITE.replaceAll("%IP%", ipAddress)).load(true);
			String jsonQuery = page.getSingleLine();
			return GSON.get().fromJson(jsonQuery, WorldLocation.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
