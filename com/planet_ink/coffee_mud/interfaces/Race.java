package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;
public interface Race extends Cloneable, Tickable, StatsAffecting, MsgListener
{
	public String ID();
	public String name();
	public boolean playerSelectable();
	public void startRacing(MOB mob, boolean verifyOnly);
	public void setHeightWeight(EnvStats stats, char gender);
	public void outfit(MOB mob);
	public Weapon myNaturalWeapon();
	public String healthText(MOB mob);
	public DeadBody getCorpse(MOB mob, Room room);
	public Vector myResources();
	public String racialCategory();
	public Race copyOf();
	
	public String arriveStr();
	public String leaveStr();

	public void level(MOB mob);
}
