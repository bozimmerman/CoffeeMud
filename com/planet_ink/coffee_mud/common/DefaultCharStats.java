package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;
public class DefaultCharStats implements Cloneable, CharStats
{

	// competency characteristics
	protected int Strength=10;
	protected int Dexterity=10;
	protected int Constitution=10;
	protected int Wisdom=10;
	protected int Intelligence=10;
	protected int Charisma=10;
	public int getStrength(){ return Strength;}
	public int getDexterity(){ return Dexterity;}
	public int getConstitution(){ return Constitution;}
	public int getWisdom(){ return Wisdom;}
	public int getIntelligence(){ return Intelligence;}
	public int getCharisma(){ return Charisma;}
	public void setStrength(int newVal){Strength=newVal;}
	public void setDexterity(int newVal){Dexterity=newVal;}
	public void setConstitution(int newVal){Constitution=newVal;}
	public void setWisdom(int newVal){Wisdom=newVal;}
	public void setIntelligence(int newVal){Intelligence=newVal;}
	public void setCharisma(int newVal){Charisma=newVal;}

	// physical and static properties
	protected char Gender='M';
	protected CharClass MyClass=null;
	protected Race MyRace=null;
	public char getGender(){return Gender;}
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
	public void setGender(char newGender){Gender=newGender;}
	public void setMyClass(CharClass newVal){MyClass=newVal;}
	public void setMyRace(Race newVal){MyRace=newVal;}

	// create a new one of these
	public CharStats cloneCharStats()
	{
		CharStats newOne=null;
		try
		{
			newOne=(CharStats) this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			newOne=(CharStats) new DefaultCharStats();
		}
		return newOne;
	}

	public String himher()
	{
		return (getGender()=='M')?"him":"her";
	}

	public String hisher()
	{
		return (getGender()=='M')?"his":"her";
	}

	public String heshe()
	{
		return (getGender()=='M')?"he":"she";
	}

	public String HeShe()
	{
		return (getGender()=='M')?"He":"She";
	}

	public void reRoll()
	{
		double avg=0.0;
		while((Math.floor(avg)!=AVG_VALUE)||(avg==0.0))
		{
			Strength=3+(int)Math.floor(Math.random()*16.0);
			Intelligence=3+(int)Math.floor(Math.random()*16.0);
			Dexterity=3+(int)Math.floor(Math.random()*16.0);
			Wisdom=3+(int)Math.floor(Math.random()*16.0);
			Constitution=3+(int)Math.floor(Math.random()*16.0);
			Charisma=3+(int)Math.floor(Math.random()*16.0);
			avg=Util.div((Strength+Intelligence+Dexterity+Wisdom+Constitution+Charisma),6.0);
		}
	}
	public StringBuffer getStats(int maxStat[])
	{
		StringBuffer stats=new StringBuffer("");
		stats.append(Util.padRight("Strength",15)+": "+Util.padRight(Integer.toString(Strength),2)+"/"+maxStat[this.STRENGTH]+"\n\r");
		stats.append(Util.padRight("Intelligence",15)+": "+Util.padRight(Integer.toString(Intelligence),2)+"/"+maxStat[this.INTELLIGENCE]+"\n\r");
		stats.append(Util.padRight("Dexterity",15)+": "+Util.padRight(Integer.toString(Dexterity),2)+"/"+maxStat[this.DEXTERITY]+"\n\r");
		stats.append(Util.padRight("Wisdom",15)+": "+Util.padRight(Integer.toString(Wisdom),2)+"/"+maxStat[this.WISDOM]+"\n\r");
		stats.append(Util.padRight("Constitution",15)+": "+Util.padRight(Integer.toString(Constitution),2)+"/"+maxStat[this.CONSTITUTION]+"\n\r");
		stats.append(Util.padRight("Charisma",15)+": "+Util.padRight(Integer.toString(Charisma),2)+"/"+maxStat[this.CHARISMA]+"\n\r");
		return stats;
	}


	public int getCurStat(int abilityCode)
	{
		int curStat=-1;
		switch(abilityCode)
		{
		case STRENGTH:
			curStat=getStrength();
			break;
		case INTELLIGENCE:
			curStat=getIntelligence();
			break;
		case DEXTERITY:
			curStat=getDexterity();
			break;
		case CONSTITUTION:
			curStat=getConstitution();
			break;
		case CHARISMA:
			curStat=getCharisma();
			break;
		case WISDOM:
			curStat=getWisdom();
			break;
		}
		return curStat;

	}

	public int getCurStat(String abilityName)
	{
		for(int i=0;i<=5;i++)
			if(TRAITS[i].startsWith(abilityName))
				return getCurStat(i);
		return -1;
	}

	public int getAbilityCode(String abilityName)
	{
		for(int i=0;i<=5;i++)
			if(TRAITS[i].startsWith(abilityName))
				return i;
		return -1;
	}

}
