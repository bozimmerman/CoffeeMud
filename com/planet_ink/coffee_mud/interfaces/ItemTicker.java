package com.planet_ink.coffee_mud.interfaces;

public interface ItemTicker extends Ability
{
	public void loadContent(ItemTicker ticker, 
							Item item, 
							Room room);
	public void loadMeUp(Item item, Room room);
	public void unloadIfNecessary(Item item);
	public void verifyFixContents(Item item, Room room);
	public Room properLocation();
	public void setProperLocation(Room room);
}
