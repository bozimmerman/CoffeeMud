package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class DefaultCharStats implements Cloneable, CharStats
{

	// competency characteristics
	protected int[] stats=new int[NUM_STATS];
	protected CharClass[] myClasses=null;
	protected Integer[] myLevels=null;
	protected Race myRace=null;
	
	public DefaultCharStats()
	{
		for(int i=0;i<NUM_BASE_STATS;i++)
			stats[i]=10;
		stats[GENDER]=(int)'M';
	}

	public void setMyClasses(String classes)
	{
		int x=classes.indexOf(";");
		Vector MyClasses=new Vector();
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
		myClasses=(CharClass[])MyClasses.toArray();
	}
	public void setMyLevels(String levels)
	{
		int x=levels.indexOf(";");
		Vector MyLevels=new Vector();
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
		myLevels=(Integer[])MyLevels.toArray();
	}
	public String getMyClassesStr()
	{
		if(myClasses==null)	return "StdCharClass";
		String classStr="";
		for(int i=0;i<myClasses.length;i++)
			classStr+=";"+myClasses[i].ID();
		if(classStr.length()>0)
			classStr=classStr.substring(1);
		return classStr;
	}
	public String getMyLevelsStr()
	{
		if(myLevels==null) return "";
		String levelStr="";
		for(int i=0;i<myLevels.length;i++)
			levelStr+=";"+myLevels[i].intValue();
		if(levelStr.length()>0)
			levelStr=levelStr.substring(1);
		return levelStr;
	}
	public int numClasses()
	{
		if(myClasses==null) return 0;
		return myClasses.length;
	}
	public CharClass getMyClass(int i)
	{
		if((myClasses==null)
		||(i<0)
		||(i>=myClasses.length)) 
			return CMClass.getCharClass("StdCharClass");
		return myClasses[i];
	}
	public int getClassLevel(String classID)
	{
		if((myClasses==null)||(myLevels==null)
		||(myClasses.length!=myLevels.length))
			return -1;
		for(int i=0;i<myClasses.length;i++)
			if(myClasses[i].ID().equalsIgnoreCase(classID))
			   return myLevels[i].intValue();
		return -1;
	}
	
	public void setClassLevel(String classID, int level)
	{
		if((myClasses==null)||(myLevels==null))
		{
			myClasses=new CharClass[1];
			myLevels=new Integer[1];
			myClasses[0]=CMClass.getCharClass(classID);
			myLevels[0]=new Integer(level);
		}
		else
		if(myClasses.length==myLevels.length)
		{
			if((level<0)&&(myClasses.length>1))
			{
				CharClass[] oldClasses=myClasses;
				Integer[] oldLevels=myLevels;
				myClasses=new CharClass[oldClasses.length-1];
				myLevels=new Integer[oldClasses.length-1];
				for(int c=0;c<myClasses.length;c++)
				{
					myClasses[c]=oldClasses[c];
					if(c<oldLevels.length)
						myLevels[c]=oldLevels[c];
					else
						myLevels[c]=new Integer(0);
				}
			}
			else
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
					myLevels[i]=new Integer(level);
					break;
				}
			}
			
		}
	}
	
	public void setCurrentClass(String classID)
	{
		if(((myClasses==null)||(myLevels==null))
		||((numClasses()==1)&&(myClasses[0].ID().equals("StdCharClass"))))
		{
			myClasses=new CharClass[1];
			myLevels=new Integer[1];
			myClasses[0]=CMClass.getCharClass(classID);
			myLevels[0]=new Integer(0);
		}
		
		if(getClassLevel(classID)<0)
		{
			CharClass[] oldClasses=myClasses;
			Integer[] oldLevels=myLevels;
			myClasses=new CharClass[oldClasses.length+1];
			myLevels=new Integer[oldClasses.length+1];
			for(int c=0;c<oldClasses.length;c++)
			{
				myClasses[c]=oldClasses[c];
				if(c<oldLevels.length)
					myLevels[c]=oldLevels[c];
				else
					myLevels[c]=new Integer(0);
				
			}
			myClasses[oldClasses.length]=CMClass.getCharClass(classID);
			myLevels[oldClasses.length]=new Integer(0);
		}
		else
		if(myClasses.length==myLevels.length)
		{
			if(myClasses[myClasses.length-1].ID().equals(classID))
				return;
			CharClass oldC=CMClass.getCharClass(classID);
			Integer oldI=new Integer(getClassLevel(classID));
			int startHere=-1;
			for(int i=0;i<myClasses.length;i++)
			{
				CharClass C=getMyClass(i);
				if(C.ID().equals(classID)){	startHere=i; break;}
			}
			if(startHere>=0)
			{
				for(int i=startHere;i<(myClasses.length-1);i++)
				{
					myClasses[i]=myClasses[i+1];
					myLevels[i]=myLevels[i+1];
				}
				myClasses[myClasses.length-1]=oldC;
				myLevels[myClasses.length-1]=oldI;
			}
		}
	}
	public CharClass getCurrentClass()
	{
		if(myClasses==null)
			setCurrentClass("StdCharClass");
		return myClasses[myClasses.length-1];
	}
	public Race getMyRace()
	{
		if(myRace==null) 
			myRace=CMClass.getRace("StdRace");
		return myRace;
	}
	public void setMyRace(Race newVal){myRace=newVal;}
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
		if(myClasses!=null)
			newOne.myClasses=(CharClass[])myClasses.clone();
		newOne.myRace=myRace;
		if(myLevels!=null)
			newOne.myLevels=(Integer[])myLevels.clone();
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
