package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class DefaultCharStats implements Cloneable, CharStats
{

	// competency characteristics
	protected int[] stats=new int[NUM_STATS];
	protected Vector MyClasses=null;
	protected Vector MyLevels=null;
	protected Race MyRace=null;
	
	public DefaultCharStats()
	{
		for(int i=0;i<NUM_BASE_STATS;i++)
			stats[i]=10;
		stats[GENDER]=(int)'M';
	}

	public void setMyClasses(String classes)
	{
		int x=classes.indexOf(";");
		MyClasses=new Vector();
		while(x>=0)
		{
			String theClass=classes.substring(0,x).trim();
			classes=classes.substring(x+1);
			if(theClass.length()>0)
				MyClasses.addElement(CMClass.getCharClass(theClass));
			x=classes.indexOf(";");
		}
		if(classes.trim().length()>0)
			MyClasses.addElement(CMClass.getCharClass(classes.trim()));
	}
	public void setMyLevels(String levels)
	{
		int x=levels.indexOf(";");
		MyLevels=new Vector();
		while(x>=0)
		{
			String theLevel=levels.substring(0,x).trim();
			levels=levels.substring(x+1);
			if(theLevel.length()>0)
				MyLevels.addElement(new Integer(Util.s_int(theLevel)));
			x=levels.indexOf(";");
		}
		if(levels.trim().length()>0)
			MyLevels.addElement(new Integer(Util.s_int(levels)));
	}
	public String getMyClassesStr()
	{
		if(MyClasses==null)	return "";
		String classStr="";
		for(int i=0;i<MyClasses.size();i++)
			classStr+=";"+((CharClass)MyClasses.elementAt(i)).ID();
		if(classStr.length()>0)
			classStr=classStr.substring(1);
		return classStr;
	}
	public String getMyLevelsStr()
	{
		if(MyLevels==null) return "";
		String levelStr="";
		for(int i=0;i<MyLevels.size();i++)
			levelStr+=";"+((Integer)MyLevels.elementAt(i)).intValue();
		if(levelStr.length()>0)
			levelStr=levelStr.substring(1);
		return levelStr;
	}
	public int numClasses()
	{
		if(MyClasses==null) return 0;
		return MyClasses.size();
	}
	public CharClass getMyClass(int i)
	{
		if(MyClasses==null) return CMClass.getCharClass("StdCharClass");
		if((i<0)||(i>=MyClasses.size())) return CMClass.getCharClass("StdCharClass");
		return (CharClass)MyClasses.elementAt(i);
	}
	public int getClassLevel(String classID)
	{
		if(MyClasses==null) return -1;
		if(MyClasses.size()!=MyLevels.size())
			return -1;
		for(int i=0;i<numClasses();i++)
			if(getMyClass(i).ID().equalsIgnoreCase(classID))
			   return ((Integer)MyLevels.elementAt(i)).intValue();
		return -1;
	}
	
	public void setClassLevel(String classID, int level)
	{
		if(MyClasses.size()==MyLevels.size())
		{
			if(getClassLevel(classID)<0)
			{
				setCurrentClass(classID);
				if(getClassLevel(classID)>=0)
					setClassLevel(classID,level);
			}
			else
			for(int i=0;i<numClasses();i++)
			{
				CharClass C=getMyClass(i);
				if(C.ID().equals(classID))
				{
					Integer I=(Integer)MyLevels.elementAt(i);
					if(I!=null)
					{
						MyLevels.setElementAt(new Integer(level),i);
						return;
					}
				}
			}
			
		}
	}
	public void setCurrentClass(String classID)
	{
		if(MyClasses==null) MyClasses=new Vector();
		if(MyLevels==null) MyLevels=new Vector();
		
		if((!classID.equalsIgnoreCase("StdCharClass"))
		&&(numClasses()==1)
		&&(((CharClass)getMyClass(0)).ID().equals("StdCharClass")))
		{
			MyClasses.clear();
			MyLevels.clear();
		}
		
		if(getClassLevel(classID)<0)
		{
			MyClasses.addElement(CMClass.getCharClass(classID));
			MyLevels.addElement(new Integer(0));
		}
		else
		if(MyClasses.size()==MyLevels.size())
			for(int i=0;i<numClasses();i++)
			{
				CharClass C=getMyClass(i);
				if(C.ID().equals(classID))
				{
					Integer I=(Integer)MyLevels.elementAt(i);
					if(I!=null)
					{
						MyLevels.removeElement(I);
						MyClasses.removeElement(C);
						MyClasses.addElement(C);
						MyLevels.addElement(I);
						return;
					}
				}
			}
	}
	public CharClass getCurrentClass()
	{
		if((MyClasses==null)||(MyClasses.size()==0))
			setCurrentClass("StdCharClass");
		return (CharClass)MyClasses.lastElement();
	}
	public Race getMyRace()
	{
		if(MyRace==null) 
			MyRace=CMClass.getRace("StdRace");
		return MyRace;
	}
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
		DefaultCharStats newOne=new DefaultCharStats();
		newOne.MyClasses=(Vector)MyClasses.clone();
		newOne.MyRace=MyRace;
		newOne.MyLevels=(Vector)MyLevels.clone();
		newOne.stats=(int[])stats.clone();
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
