package org.mixare.data.convert;

public class PlacePos {

	private double lat;
	private double lon;
	private String name;
	private String address;
	private String categoryIcon;

	public PlacePos() {
		super();
	}

	/**
	 * @param lat
	 * @param lon
	 * @param name
	 * @param address
	 */
	public PlacePos(final double lat, final double lon, final String name,
			final String address, final String categoryIcon) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.address = address;
		this.categoryIcon = categoryIcon;
	}

	/**
	 * @return the lat
	 */
	public final double getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *            the lat to set
	 */
	public final void setLat(final double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public final double getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            the lon to set
	 */
	public final void setLon(final double lon) {
		this.lon = lon;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public final void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the address
	 */
	public final String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public final void setAddress(final String address) {
		this.address = address;
	}

	public String getCategoryIcon() {
		return categoryIcon;
	}

	public void setCategoryIcon(final String categoryIcon) {
		this.categoryIcon = categoryIcon;
	}

}
