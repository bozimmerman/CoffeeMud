package com.planet_ink.coffee_mud.interfaces;

public interface ClanItem extends Environmental
{
	public String clanID();
	public void setClanID(String ID);
	
	public int ciType();
}
