package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	
	public DefaultEnvStats(){}
	public DefaultEnvStats(int def)
	{
		Level=def;
		SensesMask=def;
		Armor=def;
		Speed=new Integer(def).doubleValue();
		Damage=def;
		AttackAdjustment=def;
		Disposition=def;
		Weight=def;
		Ability=def;
		Height=def;
	}

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
	public int attackAdjustment(){return AttackAdjustment;}
	public String newName(){ return replacementName;}

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
	public void setName(String newName){ replacementName=newName;}
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
	private final static String[] CODES={
		"SENSES","DISPOSITION","LEVEL",
		"ABILITY","REJUV","WEIGHT","HEIGHT",
		"ARMOR","DAMAGE","ATTACK"};
	public String[] getCodes(){return CODES;}
	private int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(EnvStats E){
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
			   return false;
		return true;
	}
	
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code)){
		case 0: setSensesMask(Util.s_int(val)); break;
		case 1: setDisposition(Util.s_int(val)); break;
		case 2: setLevel(Util.s_int(val)); break;
		case 3: setAbility(Util.s_int(val)); break;
		case 4: setRejuv(Util.s_int(val)); break;
		case 5: setWeight(Util.s_int(val)); break;
		case 6: setHeight(Util.s_int(val)); break;
		case 7: setArmor(Util.s_int(val)); break;
		case 8: setDamage(Util.s_int(val)); break;
		case 9: setAttackAdjustment(Util.s_int(val)); break;
		}
	}
	public String getStat(String code)
	{
		switch(getCodeNum(code)){
		case 0: return ""+sensesMask();
		case 1: return ""+disposition();
		case 2: return ""+level();
		case 3: return ""+ability();
		case 4: return ""+rejuv();
		case 5: return ""+weight();
		case 6: return ""+height();
		case 7: return ""+armor();
		case 8: return ""+damage();
		case 9: return ""+attackAdjustment();
		default: return "";
		}
	}
	
}
