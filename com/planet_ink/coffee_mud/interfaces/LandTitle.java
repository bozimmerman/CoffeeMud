package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;


public interface LandTitle extends Environmental
{
	public int landPrice();
	public void setLandPrice(int price);
	public String landOwner();
	public void setLandOwner(String owner);
	public String landPropertyID();
	public void setLandPropertyID(String landID);
	public void updateLot();
	public void updateTitle();
	public Vector getPropertyRooms();
}
