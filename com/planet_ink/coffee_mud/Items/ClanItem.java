package com.planet_ink.coffee_mud.Items;

public interface ClanItem
{
	public final static int CI_FLAG=0;
	
	
	public String clanOwnership();
	public void setClanOwnership(String clan);
	public int itemType();
	public void setItemType(int cicode);
}
