package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface ClanMaster extends MOB
{
	public void setTypeIHandle(int ClanType);
	public int getTypeIHandle();

	public void setDaysClanDeath(Integer daysClanDeath);
	public int getDaysClanDeath();

	public void setMinClanMembers(Integer minMembers);
	public int getMinClanMembers();

}