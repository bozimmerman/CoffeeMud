package com.planet_ink.coffee_mud.interfaces;

public interface ClanItem extends Environmental
{
	public final static int CI_FLAG=0;
	public final static int CI_BANNER=1;
	public final static int CI_GAVEL=2;
	public final static int CI_PROPAGANDA=3;
	public final static int CI_GATHERITEM=4;
	public final static int CI_CRAFTITEM=5;
	public final static int CI_SPECIALSCALES=6;
	public final static int CI_SPECIALSCAVENGER=7;
	public final static int CI_SPECIALOTHER=8;
	
	public final static String[] CI_DESC={
		"FLAG",
		"BANNER",
		"GAVEL",
		"PROPAGANDA",
		"GATHERITEM",
		"CRAFTITEM",
		"SPECIALSCALES",
		"SPECIALSCAVENGER",
		"SPECIALOTHER"
	};
	
	public String clanID();
	public void setClanID(String ID);
	
	public int ciType();
	public void setCIType(int type);
}
