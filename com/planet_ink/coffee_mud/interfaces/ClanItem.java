package com.planet_ink.coffee_mud.interfaces;

public interface ClanItem extends Environmental
{
	public final static int CI_FLAG=0;
	public final static int CI_BANNER=1;
	public final static int CI_GAVEL=2;
	
	public final static String[] CI_DESC={
		"FLAG",
		"BANNER",
		"GAVEL"
	};
	
	public String clanID();
	public void setClanID(String ID);
	
	public int ciType();
	public void setCIType(int type);
}
