package com.planet_ink.coffee_mud.interfaces;

public interface EnvStats extends Cloneable
{
	public int sensesMask();
	public int disposition();
	public int level();
	public int ability();
	public int rejuv();
	public int weight();
	public int armor();
	public int damage();
	public double speed();
	public int attackAdjustment();
	
	public void setRejuv(int newRejuv);
	public void setLevel(int newLevel);
	public void setArmor(int newArmor);
	public void setDamage(int newDamage);
	public void setWeight(int newWeight);
	public void setSpeed(double newSpeed);
	public void setAttackAdjustment(int newAdjustment);
	public void setAbility(int newAdjustment);
	public void setDisposition(int newDisposition);
	public void setSensesMask(int newMask);

	public EnvStats cloneStats();
}
