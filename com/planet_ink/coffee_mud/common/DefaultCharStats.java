package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;
public class DefaultCharStats implements Cloneable, CharStats
{

	// competency characteristics
	protected int[] stats=new int[NUM_STATS];
	protected CharClass MyClass=null;
	protected Race MyRace=null;
	
	public DefaultCharStats()
	{
		for(int i=0;i<NUM_BASE_STATS;i++)
			stats[i]=10;
		stats[GENDER]=(int)'M';
	}

	public CharClass getMyClass()
	{
		if(MyClass==null)
			MyClass=CMClass.getCharClass("StdCharClass");
		return MyClass;
	}
	public Race getMyRace()
	{
		if(MyRace==null)
			MyRace=CMClass.getRace("StdRace");
		return MyRace;
	}
	public void setMyClass(CharClass newVal){MyClass=newVal;}
	public void setMyRace(Race newVal){MyRace=newVal;}
	public int getSave(int which)
	{
		switch(which)
		{
		case SAVE_PARALYSIS:
			return getStat(SAVE_PARALYSIS)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(STRENGTH),2.0));
		case SAVE_FIRE:
			return getStat(SAVE_FIRE)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(DEXTERITY),2.0));
		case SAVE_COLD:
			return getStat(SAVE_COLD)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(DEXTERITY),2.0));
		case SAVE_WATER:
			return getStat(SAVE_WATER)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(DEXTERITY),2.0));
		case SAVE_GAS:
			return getStat(SAVE_GAS)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(STRENGTH),2.0));
		case SAVE_MIND:
			return getStat(SAVE_MIND)+(int)Math.round(Util.div(getStat(WISDOM)+getStat(INTELLIGENCE)+getStat(CHARISMA),3.0));
		case SAVE_GENERAL:
			return getStat(SAVE_GENERAL)+getStat(CONSTITUTION);
		case SAVE_JUSTICE:
			return getStat(SAVE_JUSTICE)+getStat(CHARISMA);
		case SAVE_ACID:
			return getStat(SAVE_ACID)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(DEXTERITY),2.0));
		case SAVE_ELECTRIC:
			return getStat(SAVE_ELECTRIC)+(int)Math.round(Util.div(getStat(CONSTITUTION)+getStat(DEXTERITY),2.0));
		case SAVE_POISON:
			return getStat(SAVE_POISON)+getStat(CONSTITUTION);
		case SAVE_UNDEAD:
			return getStat(SAVE_UNDEAD)+getStat(WISDOM);
		case SAVE_DISEASE:
			return getStat(SAVE_DISEASE)+getStat(CONSTITUTION);
		case SAVE_MAGIC:
			return getStat(SAVE_MAGIC)+getStat(INTELLIGENCE);
		}
		return 0;
	}

	// create a new one of these
	public CharStats cloneCharStats()
	{
		CharStats newOne=new DefaultCharStats();
		newOne.setMyClass(MyClass);
		newOne.setMyRace(MyRace);
		for(int s=0;s<stats.length;s++)
			newOne.setStat(s,stats[s]);
		return newOne;
	}

	public String genderName()
	{
		switch(getStat(GENDER))
		{
		case 'M': return "male";
		case 'F': return "female";
		default: return "neuter";
		}
	}
	public String himher()
	{
		switch(getStat(GENDER))
		{
		case 'M': return "him";
		case 'F': return "her";
		default: return "it";
		}
	}

	public String hisher()
	{
		switch(getStat(GENDER))
		{
		case 'M': return "his";
		case 'F': return "her";
		default: return "its";
		}
	}

	public String heshe()
	{
		switch(getStat(GENDER))
		{
		case 'M': return "he";
		case 'F': return "she";
		default: return "it";
		}
	}

	public String HeShe()
	{
		switch(getStat(GENDER))
		{
		case 'M': return "He";
		case 'F': return "She";
		default: return "It";
		}
	}

	public void reRoll()
	{
		int avg=0;
		while((avg!=MAX_STATS)||(avg==0))
		{
			setStat(STRENGTH,3+(int)Math.floor(Math.random()*16.0));
			setStat(INTELLIGENCE,3+(int)Math.floor(Math.random()*16.0));
			setStat(DEXTERITY,3+(int)Math.floor(Math.random()*16.0));
			setStat(WISDOM,3+(int)Math.floor(Math.random()*16.0));
			setStat(CONSTITUTION,3+(int)Math.floor(Math.random()*16.0));
			setStat(CHARISMA,3+(int)Math.floor(Math.random()*16.0));
			avg=(getStat(STRENGTH)+getStat(INTELLIGENCE)+getStat(DEXTERITY)+getStat(WISDOM)+getStat(CONSTITUTION)+getStat(CHARISMA));
		}
	}
	public StringBuffer getStats(int maxStat[])
	{
		StringBuffer statstr=new StringBuffer("");
		statstr.append(Util.padRight("Strength",15)+": "+Util.padRight(Integer.toString(getStat(STRENGTH)),2)+"/"+maxStat[STRENGTH]+"\n\r");
		statstr.append(Util.padRight("Intelligence",15)+": "+Util.padRight(Integer.toString(getStat(INTELLIGENCE)),2)+"/"+maxStat[INTELLIGENCE]+"\n\r");
		statstr.append(Util.padRight("Dexterity",15)+": "+Util.padRight(Integer.toString(getStat(DEXTERITY)),2)+"/"+maxStat[DEXTERITY]+"\n\r");
		statstr.append(Util.padRight("Wisdom",15)+": "+Util.padRight(Integer.toString(getStat(WISDOM)),2)+"/"+maxStat[WISDOM]+"\n\r");
		statstr.append(Util.padRight("Constitution",15)+": "+Util.padRight(Integer.toString(getStat(CONSTITUTION)),2)+"/"+maxStat[CONSTITUTION]+"\n\r");
		statstr.append(Util.padRight("Charisma",15)+": "+Util.padRight(Integer.toString(getStat(CHARISMA)),2)+"/"+maxStat[CHARISMA]+"\n\r");
		return statstr;
	}


	public int getStat(int abilityCode)
	{
		return stats[abilityCode];
	}

	public void setStat(int abilityCode, int value)
	{
		stats[abilityCode]=value;
	}

	public int getStat(String abilityName)
	{
		for(int i=0;i<20;i++)
			if(TRAITS[i].startsWith(abilityName))
				return getStat(i);
		return -1;
	}

	public int getCode(String abilityName)
	{
		for(int i=0;i<20;i++)
			if(TRAITS[i].startsWith(abilityName))
				return i;
		return -1;
	}

}
