package com.runescape.utility.tools.geoip;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/15/2016
 */
public class WorldLocation {

	private String ip, country_code, country_name, region_code, region_name, city, zip_code, longitude, latitude;
	private String time_zone, metro_code;

	public String getCountryCode() {
		return country_code;
	}

	public String getCountryName() {
		return country_name;
	}

	public String getRegionName() {
		return region_name;
	}

	public String getRegionCode() {
		return region_code;
	}

	public String getCity() {
		return city;
	}

	public String getZipCode() {
		return zip_code;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setCountryCode(String countryCode) {
		this.country_code = countryCode;
	}

	public void setCountryName(String countryName) {
		this.country_name = countryName;
	}

	public void setRegionName(String regionName) {
		this.region_name = regionName;
	}

	public void setRegionCode(String regionCode) {
		this.region_code = regionCode;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setZipCode(String zipCode) {
		this.zip_code = zipCode;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getTimeZone() {
		return time_zone;
	}

	public void setTimeZone(String time_zone) {
		this.time_zone = time_zone;
	}

	public String getMetroCode() {
		return metro_code;
	}

	public void setMetroCode(String metro_code) {
		this.metro_code = metro_code;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
