package com.planet_ink.coffee_mud.interfaces;

public interface LandTitle
{
	public int landPrice();
	public void setLandPrice(int price);
	public String landOwner();
	public void setLandOwner(String owner);
	public String landRoomID();
	public void setLandRoomID(String landID);
	public void updateLot(Room R, LandTitle T);
}
