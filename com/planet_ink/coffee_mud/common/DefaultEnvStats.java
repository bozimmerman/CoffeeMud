package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;

public class DefaultEnvStats implements Cloneable, EnvStats
{
	protected int Level=0;
	protected int SensesMask=0;			// see Senses class
	protected int Armor=100;			// should be positive
	protected double Speed=1.0;			// should be positive
	protected int Damage=0;				// should be positive
	protected int AttackAdjustment=0;	// should be negative
	protected int Disposition=0;		// see Senses class
	protected int Rejuv=Integer.MAX_VALUE;
	protected int Weight=0;
	protected int Ability=0;			// object dependant
	protected int Height=0;
	protected String replacementName=null;

	public int sensesMask(){return SensesMask;}
	public int disposition(){return Disposition;}
	public int level(){return Level;}
	public int ability(){return Ability;}
	public int rejuv(){return Rejuv;}
	public int weight(){return Weight;}
	public int height(){return Height;}
	public int armor(){return Armor;}
	public int damage(){return Damage;}
	public double speed(){return Speed;}
	public String replacementName(){return replacementName;}
	public int attackAdjustment(){return AttackAdjustment;}

	public void setRejuv(int newRejuv){Rejuv=newRejuv;}
	public void setLevel(int newLevel){Level=newLevel;}
	public void setArmor(int newArmor){Armor=newArmor;}
	public void setDamage(int newDamage){Damage=newDamage;}
	public void setWeight(int newWeight){Weight=newWeight;}
	public void setSpeed(double newSpeed){Speed=newSpeed;}
	public void setAttackAdjustment(int newAdjustment){AttackAdjustment=newAdjustment;}
	public void setAbility(int newAdjustment){Ability=newAdjustment;}
	public void setDisposition(int newDisposition){Disposition=newDisposition;}
	public void setSensesMask(int newMask){SensesMask=newMask;}
	public void setHeight(int newHeight){Height=newHeight;}
	public void setReplacementName(String newName){replacementName=newName;}
	public EnvStats cloneStats()
	{
		try
		{
			return (EnvStats)this.clone();
		}
		catch(java.lang.CloneNotSupportedException e)
		{
			return new DefaultEnvStats();
		}
	}
}
