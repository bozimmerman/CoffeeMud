package com.planet_ink.coffee_mud.interfaces;

// new commands, mount, dismount, board, unboard
// death ends the ride (dead rideable loses its riders too) (just have to do the mob now...)
// modify generic stuff to accommodate the rideable
// attack someone you are riding with == instant range 0 for both of you!
// must maintain same range as other riders against identical foes!
public interface Rideable extends Environmental
{
	public final static int RIDEABLE_LAND=0;
	public final static int RIDEABLE_WATER=1;
	public final static int RIDEABLE_AIR=2;
	
	public final static int COMBAT_DIFFICULTY_FROM_RIDING=-50;
	public final static int DAMAGE_DIFFICULTY_FROM_RIDING=-10;
															  
	public int rideBasis();
	public void setRideBasis(int basis);
	public int mobCapacity();
	public void setMobCapacity(int newCapacity);
	public int numRiders();
	public MOB fetchRider(int which);
	public void addRider(MOB mob);
	public void delRider(MOB mob);
	public boolean amRiding(MOB mob);
}
