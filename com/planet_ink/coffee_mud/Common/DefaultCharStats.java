package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DefaultCharStats implements CharStats
{
    public String ID(){return "DefaultCharStats";}
    
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCharStats();}}
    public void initializeClass(){}
	// competency characteristics
	protected short[] stats=new short[CharStats.CODES.instance().total()];
	protected CharClass[] myClasses=null;
	protected Integer[] myLevels=null;
	protected Race myRace=null;
	protected String raceName=null;
	protected String genderName=null;
	protected String displayClassName=null;
	protected String displayClassLevel=null;
	protected short[] bodyAlterations=null;
	protected long unwearableBitmap=0;
	
	public DefaultCharStats()
	{
        setAllBaseValues(VALUE_ALLSTATS_DEFAULT);
		stats[STAT_GENDER]='M';
	}
    public void setAllBaseValues(int def)
    {
        for(int i : CharStats.CODES.BASE())
            stats[i]=(short)def;
    }
    public void setAllValues(int def)
    {
        for(int i: CharStats.CODES.ALL())
            stats[i]=(short)def;
        unwearableBitmap=0;
    }
    
    public void copyInto(CharStats intoStats)
    {
    	if(intoStats instanceof DefaultCharStats)
    	{
    		if(myClasses==null)
	    		((DefaultCharStats)intoStats).myClasses=null;
    		else
    		if((((DefaultCharStats)intoStats).myClasses!=null)
    		&&(((DefaultCharStats)intoStats).myClasses.length==myClasses.length))
    			for(int i=0;i<myClasses.length;i++)
    				((DefaultCharStats)intoStats).myClasses[i]=myClasses[i];
    		else
	    		((DefaultCharStats)intoStats).myClasses=(CharClass[])myClasses.clone();
    		if(myLevels==null)
	    		((DefaultCharStats)intoStats).myLevels=null;
    		else
    		if((((DefaultCharStats)intoStats).myLevels!=null)
    		&&(((DefaultCharStats)intoStats).myLevels.length==myLevels.length))
    			for(int i=0;i<myLevels.length;i++)
    				((DefaultCharStats)intoStats).myLevels[i]=myLevels[i];
    		else
	    		((DefaultCharStats)intoStats).myLevels=(Integer[])myLevels.clone();
    		((DefaultCharStats)intoStats).myRace=myRace;
    		((DefaultCharStats)intoStats).raceName=raceName;
    		((DefaultCharStats)intoStats).genderName=genderName;
    		((DefaultCharStats)intoStats).displayClassName=displayClassName;
    		((DefaultCharStats)intoStats).displayClassLevel=displayClassLevel;
    		if(bodyAlterations==null)
	    		((DefaultCharStats)intoStats).bodyAlterations=null;
    		else
    		if((((DefaultCharStats)intoStats).bodyAlterations!=null)
    		&&(((DefaultCharStats)intoStats).bodyAlterations.length==bodyAlterations.length))
    			for(int i=0;i<bodyAlterations.length;i++)
    				((DefaultCharStats)intoStats).bodyAlterations[i]=bodyAlterations[i];
    		else
	    		((DefaultCharStats)intoStats).bodyAlterations=(short[])bodyAlterations.clone();
    		for(int i=0;i<stats.length;i++)
    			((DefaultCharStats)intoStats).stats[i]=stats[i];
			((DefaultCharStats)intoStats).unwearableBitmap=unwearableBitmap;
    	}
    	else
    	{
    		intoStats.setMyClasses(getMyClassesStr());
    		intoStats.setMyLevels(getMyLevelsStr());
    		intoStats.setMyRace(getMyRace());
    		intoStats.setRaceName(raceName);
    		intoStats.setRaceName(raceName);
    		intoStats.setGenderName(genderName);
    		intoStats.setDisplayClassName(displayClassName);
    		intoStats.setDisplayClassLevel(displayClassLevel);
    		intoStats.setBodyPartsFromStringAfterRace(getBodyPartsAsString());
    		intoStats.setWearableRestrictionsBitmap(unwearableBitmap);
    	}
    }

	public void setMyClasses(String classes)
	{
		int x=classes.indexOf(";");
		Vector MyClasses=new Vector();
        CharClass C=null;
		while(x>=0)
		{
			String theClass=classes.substring(0,x).trim();
			classes=classes.substring(x+1);
			if(theClass.length()>0)
            {
                C=CMClass.getCharClass(theClass);
                if(C==null) C=CMClass.getCharClass("StdCharClass");
                MyClasses.addElement(C);
            }
			x=classes.indexOf(";");
		}
		if(classes.trim().length()>0)
        {
            C=CMClass.getCharClass(classes.trim());
            if(C==null) C=CMClass.getCharClass("StdCharClass");
            MyClasses.addElement(C);
        }
		myClasses=new CharClass[MyClasses.size()];
		for(int i=0;i<MyClasses.size();i++)
			myClasses[i]=(CharClass)MyClasses.elementAt(i);
	}
	public void setMyLevels(String levels)
	{
		if((levels.length()==0)&&(myClasses!=null)&&(myClasses.length>0)) 
			levels="0";
		int x=levels.indexOf(";");
		Vector MyLevels=new Vector();
		while(x>=0)
		{
			String theLevel=levels.substring(0,x).trim();
			levels=levels.substring(x+1);
			if(theLevel.length()>0)
				MyLevels.addElement(Integer.valueOf(CMath.s_int(theLevel)));
			x=levels.indexOf(";");
		}
		if(levels.trim().length()>0)
			MyLevels.addElement(Integer.valueOf(CMath.s_int(levels)));
		Integer[] myNewLevels=new Integer[MyLevels.size()];
		for(int i=0;i<MyLevels.size();i++)
			myNewLevels[i]=(Integer)MyLevels.elementAt(i);
		myLevels=myNewLevels;
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
    public long getWearableRestrictionsBitmap(){return unwearableBitmap|this.getMyRace().forbiddenWornBits();}
    public void setWearableRestrictionsBitmap(long bitmap){ unwearableBitmap=bitmap;}
    
	public int numClasses()
	{
		if(myClasses==null) return 0;
		return myClasses.length;
	}
	public int combinedSubLevels()
	{
		if((myClasses==null)
		   ||(myLevels==null)
		   ||(myClasses.length<2))
			return 0;
		
		int combined=0;
		for(int i=0;i<myLevels.length-1;i++)
			combined+=myLevels[i].intValue();
		return combined;
	}
	public int combinedLevels()
	{
		if((myClasses==null)
		   ||(myLevels==null))
			return 0;
		
		int combined=0;
		for(int i=0;i<myLevels.length-1;i++)
			combined+=myLevels[i].intValue();
		return combined;
	}
	public void setDisplayClassName(String newName){displayClassName=newName;}
	public String displayClassName()
	{	
		if(displayClassName!=null) return displayClassName;
		return getCurrentClass().name(getCurrentClassLevel());
	}
	public void setDisplayClassLevel(String newLevel){displayClassLevel=newLevel;}
	public String displayClassLevel(MOB mob, boolean shortForm)
	{
		if(displayClassLevel!=null)
		{
			if(shortForm)
				return displayClassName()+" "+displayClassLevel;
			return "level "+displayClassLevel+" "+displayClassName;
		}
        if(mob==null) return "";
		int classLevel=getClassLevel(getCurrentClass());
		String levelStr=null;
		if(classLevel>=mob.envStats().level())
			levelStr=""+mob.envStats().level();
		else
			levelStr=classLevel+"/"+mob.envStats().level();
		if(shortForm)
			return displayClassName()+" "+levelStr;
		return "level "+levelStr+" "+displayClassName();
	}
	public String displayClassLevelOnly(MOB mob)
	{
        if(mob==null) return "";
		if(displayClassLevel!=null)
			return displayClassLevel;
		int classLevel=getClassLevel(getCurrentClass());
		String levelStr=null;
		if(classLevel>=mob.envStats().level())
			levelStr=""+mob.envStats().level();
		else
			levelStr=classLevel+"/"+mob.envStats().level();
		return levelStr;
	}

	public String getNonBaseStatsAsString()
	{
		StringBuffer str=new StringBuffer("");
		CharStats.CODES C = CharStats.CODES.instance(); 
		for(int x : C.all())
			if((!C.isBase(x))&&(x!=CharStats.STAT_GENDER))
				str.append(stats[x]+";");
		return str.toString();
	}
	public void setNonBaseStatsFromString(String str)
	{
		Vector V=CMParms.parseSemicolons(str,false);
		CharStats.CODES C = CharStats.CODES.instance(); 
		for(int x : C.all())
			if((!C.isBase(x))&&(x!=CharStats.STAT_GENDER)&&(V.size()>0))
				stats[x]=CMath.s_short((String)V.remove(0));
	}
	public void setRaceName(String newRaceName){raceName=newRaceName;}
	public String raceName(){
		if(raceName!=null) return raceName;
		if(myRace!=null) return myRace.name();
		return "MOB";
	}
	public CharClass getMyClass(int i)
	{
		if((myClasses==null)
		||(i<0)
		||(i>=myClasses.length)) 
			return CMClass.getCharClass("StdCharClass");
		return myClasses[i];
	}
	public int getClassLevel(String aClass)
	{
		if(myClasses==null)	return -1;
		for(int i=0;i<myClasses.length;i++)
			if((myClasses[i]!=null)
			&&(myClasses[i].ID().equals(aClass))
            &&(i<myLevels.length))
			   return myLevels[i].intValue();
		return -1;
	}
	public int getClassLevel(CharClass aClass)
	{
		if((myClasses==null)||(aClass==null))	return -1;
		for(int i=0;i<myClasses.length;i++)
			if((myClasses[i]!=null)
			&&(myClasses[i].ID().equals(aClass.ID()))
            &&(i<myLevels.length))
			   return myLevels[i].intValue();
		return -1;
	}
	
	public void setClassLevel(CharClass aClass, int level)
	{
        if(aClass==null) return;
		if(myClasses==null)
		{
			myClasses=new CharClass[1];
			myLevels=new Integer[1];
			myClasses[0]=aClass;
			myLevels[0]=Integer.valueOf(level);
		}
		else
		{
			if((level<0)&&(myClasses.length>1))
			{
				CharClass[] oldClasses=myClasses;
				Integer[] oldLevels=myLevels;
				CharClass[] myNewClasses=new CharClass[oldClasses.length-1];
				Integer[] myNewLevels=new Integer[oldClasses.length-1];
				for(int c=0;c<myNewClasses.length;c++)
				{
					myNewClasses[c]=oldClasses[c];
					if(c<oldLevels.length)
						myNewLevels[c]=oldLevels[c];
					else
						myNewLevels[c]=Integer.valueOf(0);
				}
				myClasses=myNewClasses;
				myLevels=myNewLevels;
			}
			else
			if(getClassLevel(aClass)<0)
				setCurrentClass(aClass);
			for(int i=0;i<numClasses();i++)
			{
				CharClass C=getMyClass(i);
				if((C==aClass)&&(myLevels[i].intValue()!=level))
				{
					myLevels[i]=Integer.valueOf(level);
					break;
				}
			}
		}
	}
	
    public boolean isLevelCapped(CharClass C) {
        if((C==null)||(C.getLevelCap()<0)||(C.getLevelCap()==Integer.MAX_VALUE))
            return false;
        return getClassLevel(C) >= C.getLevelCap();
    }
    
    public void setCurrentClassLevel(int level)
    {
        CharClass currentClass=getCurrentClass();
        if(currentClass!=null)
            setClassLevel(currentClass,level);
    }
    
	public void setCurrentClass(CharClass aClass)
	{
        if(aClass==null) return;
		if(((myClasses==null)||(myLevels==null))
		||((numClasses()==1)&&(myClasses[0].ID().equals("StdCharClass"))))
		{
			myClasses=new CharClass[1];
			myLevels=new Integer[1];
			myClasses[0]=aClass;
			myLevels[0]=Integer.valueOf(0);
			return;
		}
		
		int level=getClassLevel(aClass);
		if(level<0)
		{
			CharClass[] oldClasses=myClasses;
			Integer[] oldLevels=myLevels;
			CharClass[] myNewClasses=new CharClass[oldClasses.length+1];
			Integer[] myNewLevels=new Integer[oldClasses.length+1];
			for(int c=0;c<oldClasses.length;c++)
			{
				myNewClasses[c]=oldClasses[c];
				myNewLevels[c]=oldLevels[c];
			}
			myNewClasses[oldClasses.length]=aClass;
			myNewLevels[oldClasses.length]=Integer.valueOf(0);
			myClasses=myNewClasses;
			myLevels=myNewLevels;
		}
		else
		{
			if(myClasses[myClasses.length-1]==aClass)
				return;
			Integer oldI=Integer.valueOf(level);
			boolean go=false;
			CharClass[] myNewClasses=(CharClass[])myClasses.clone();
			Integer[] myNewLevels=(Integer[])myLevels.clone();
			for(int i=0;i<myNewClasses.length-1;i++)
			{
				CharClass C=getMyClass(i);
				if((C==aClass)||(go))
				{
					go=true;
					myNewClasses[i]=myNewClasses[i+1];
					myNewLevels[i]=myNewLevels[i+1];
				}
			}
			myNewClasses[myNewClasses.length-1]=aClass;
			myNewLevels[myNewClasses.length-1]=oldI;
			myClasses=myNewClasses;
			myLevels=myNewLevels;
		}
	}
	public CharClass getCurrentClass()
	{
		if(myClasses==null)
			setCurrentClass(CMClass.getCharClass("StdCharClass"));
		return myClasses[myClasses.length-1];
	}
	
    public int getCurrentClassLevel()
    {
        if(myLevels==null) return -1;
        return myLevels[myLevels.length-1].intValue();
    }
    
	public Race getMyRace()
	{
		if(myRace==null) 
			myRace=CMClass.getRace("StdRace");
		return myRace;
	}
	
	public void setMyRace(Race newVal){myRace=newVal;}
	public int getBodyPart(int racialPartNumber)
	{
		int num=getMyRace().bodyMask()[racialPartNumber];
		if((num<0)||(bodyAlterations==null)) return num;
		num+=bodyAlterations[racialPartNumber];
		if(num<0) return 0;
		return num;
	}
	
	public String getBodyPartsAsString()
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<getMyRace().bodyMask().length;i++)
			str.append(getBodyPart(i)+";");
		return str.toString();
	}
	
	public void setBodyPartsFromStringAfterRace(String str)
	{
		Vector V=CMParms.parseSemicolons(str,true);
		bodyAlterations=null;
		for(int i=0;i<getMyRace().bodyMask().length;i++)
		{
			if(V.size()<=i) break;
			int val=CMath.s_int((String)V.elementAt(i));
			int num=getMyRace().bodyMask()[i];
			if(num!=val) alterBodypart(i,val-num);
		}
	}
	
	public int getBodypartAlteration(int racialPartNumber)
	{
	    if(bodyAlterations==null) return 0;
	    return bodyAlterations[racialPartNumber];
	}
	public void alterBodypart(int racialPartNumber, int deviation)
	{
		if(bodyAlterations==null) bodyAlterations=new short[Race.BODY_PARTS];
		bodyAlterations[racialPartNumber]+=deviation;
	}
	
	public int ageCategory()
	{
		int age=getStat(STAT_AGE);
		int cat=Race.AGE_INFANT;
		int[] chart=getMyRace().getAgingChart();
		if(age<chart[1]) return cat;
		while((cat<=Race.AGE_ANCIENT)&&(age>=chart[cat]))
			cat++;
		return cat-1;
	}

	public String ageName()
	{
		int cat=ageCategory();
		if(cat<Race.AGE_ANCIENT) return Race.AGE_DESCS[cat];
		int age=getStat(STAT_AGE);
		int[] chart=getMyRace().getAgingChart();
		int diff=chart[Race.AGE_ANCIENT]-chart[Race.AGE_VENERABLE];
		age=age-chart[Race.AGE_ANCIENT];
		int num=(diff>0)?(int)Math.abs(Math.floor(CMath.div(age,diff))):0;
		if(num<=0) return Race.AGE_DESCS[cat];
		return Race.AGE_DESCS[cat]+" "+CMath.convertToRoman(num);
	}
	
	public int getSave(int which)
	{
		switch(which)
		{
		case STAT_SAVE_PARALYSIS:
			return getStat(STAT_SAVE_PARALYSIS)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_STRENGTH),2.0));
		case STAT_SAVE_FIRE:
			return getStat(STAT_SAVE_FIRE)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_DEXTERITY),2.0));
		case STAT_SAVE_COLD:
			return getStat(STAT_SAVE_COLD)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_DEXTERITY),2.0));
		case STAT_SAVE_WATER:
			return getStat(STAT_SAVE_WATER)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_DEXTERITY),2.0));
		case STAT_SAVE_GAS:
			return getStat(STAT_SAVE_GAS)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_STRENGTH),2.0));
		case STAT_SAVE_MIND:
			return getStat(STAT_SAVE_MIND)+(int)Math.round(CMath.div(getStat(STAT_WISDOM)+getStat(STAT_INTELLIGENCE)+getStat(STAT_CHARISMA),3.0));
		case STAT_SAVE_GENERAL:
			return getStat(STAT_SAVE_GENERAL)+getStat(STAT_CONSTITUTION);
		case STAT_SAVE_JUSTICE:
			return getStat(STAT_SAVE_JUSTICE)+getStat(STAT_CHARISMA);
		case STAT_SAVE_ACID:
			return getStat(STAT_SAVE_ACID)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_DEXTERITY),2.0));
		case STAT_SAVE_ELECTRIC:
			return getStat(STAT_SAVE_ELECTRIC)+(int)Math.round(CMath.div(getStat(STAT_CONSTITUTION)+getStat(STAT_DEXTERITY),2.0));
		case STAT_SAVE_POISON:
			return getStat(STAT_SAVE_POISON)+getStat(STAT_CONSTITUTION);
		case STAT_SAVE_UNDEAD:
			return getStat(STAT_SAVE_UNDEAD)+getStat(STAT_WISDOM)+getStat(STAT_FAITH);
		case STAT_SAVE_DISEASE:
			return getStat(STAT_SAVE_DISEASE)+getStat(STAT_CONSTITUTION);
		case STAT_SAVE_MAGIC:
			return getStat(STAT_SAVE_MAGIC)+getStat(STAT_INTELLIGENCE);
		case STAT_SAVE_TRAPS:
			return getStat(STAT_SAVE_TRAPS)+getStat(STAT_DEXTERITY);
		case STAT_SAVE_OVERLOOKING:
			return getStat(STAT_SAVE_OVERLOOKING);
		case STAT_SAVE_DETECTION: 
			return getStat(STAT_SAVE_DETECTION);
        case STAT_FAITH: 
            return getStat(STAT_FAITH);
		}
		return getStat(which);
	}

	// create a new one of these
    public CMObject copyOf()
	{
		DefaultCharStats newOne=new DefaultCharStats();
		if(myClasses!=null)
			newOne.myClasses=(CharClass[])myClasses.clone();
		newOne.myRace=myRace;
		if(myLevels!=null)
			newOne.myLevels=(Integer[])myLevels.clone();
        if(bodyAlterations!=null)
            newOne.bodyAlterations=(short[])bodyAlterations.clone();
		newOne.stats=(short[])stats.clone();
		return newOne;
	}

	public void setGenderName(String gname)
	{
		genderName=gname;
	}
	
	public String genderName()
	{
		if(genderName!=null) 
			return genderName;
		switch(getStat(STAT_GENDER))
		{
		case 'M': return "male";
		case 'F': return "female";
		default: return "neuter";
		}
	}
	public String himher()
	{
		char c=(char)getStat(STAT_GENDER);
		if((genderName!=null)&&(genderName.length()>0))
			c=Character.toUpperCase(genderName.charAt(0));
		switch(c)
		{
		case 'M': return "him";
		case 'F': return "her";
		default: return "it";
		}
	}

	public String hisher()
	{
		char c=(char)getStat(STAT_GENDER);
		if((genderName!=null)&&(genderName.length()>0))
			c=Character.toUpperCase(genderName.charAt(0));
		switch(c)
		{
		case 'M': return "his";
		case 'F': return "her";
		default: return "its";
		}
	}

	public String heshe()
	{
		char c=(char)getStat(STAT_GENDER);
		if((genderName!=null)&&(genderName.length()>0))
			c=Character.toUpperCase(genderName.charAt(0));
		switch(c)
		{
		case 'M': return "he";
		case 'F': return "she";
		default: return "it";
		}
	}
    public String sirmadam()
    {
        char c=(char)getStat(STAT_GENDER);
        if((genderName!=null)&&(genderName.length()>0))
            c=Character.toUpperCase(genderName.charAt(0));
        switch(c)
        {
        case 'M': return "sir";
        case 'F': return "madam";
        default: return "sir";
        }
    }
    public String SirMadam()
    {
        char c=(char)getStat(STAT_GENDER);
        if((genderName!=null)&&(genderName.length()>0))
            c=Character.toUpperCase(genderName.charAt(0));
        switch(c)
        {
        case 'M': return "Sir";
        case 'F': return "Madam";
        default: return "Sir";
        }
    }

	public String HeShe()
	{
		char c=(char)getStat(STAT_GENDER);
		if((genderName!=null)&&(genderName.length()>0))
			c=Character.toUpperCase(genderName.charAt(0));
		switch(c)
		{
		case 'M': return "He";
		case 'F': return "She";
		default: return "It";
		}
	}

	public int getStat(int abilityCode)
	{
        if(abilityCode<stats.length)
			return stats[abilityCode];
        return 0;
	}

	public void setPermanentStat(int abilityCode, int value)
	{
		setStat(abilityCode,value);
		if(CharStats.CODES.isBASE(abilityCode))
			setStat(CharStats.CODES.toMAXBASE(abilityCode),value-CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT));
	}
	
	public int getMaxStat(int abilityCode)
	{
		int baseMax = CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
		return baseMax + getStat(CharStats.CODES.toMAXBASE(abilityCode));
	}
    
    public void setRacialStat(int abilityCode, int racialMax)
    {
        if((!CharStats.CODES.isBASE(abilityCode))||(getStat(abilityCode)==VALUE_ALLSTATS_DEFAULT)) 
            setPermanentStat(abilityCode,racialMax);
        else
        {
            int baseMax=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
            int currMax=getStat(CharStats.CODES.toMAXBASE(abilityCode))+baseMax;
            if(currMax<=0) currMax=1;
            int curStat=getStat(abilityCode);
            int racialStat=Math.round(((float)curStat/(float)currMax)*(float)racialMax)+Math.round((((float)(currMax-VALUE_ALLSTATS_DEFAULT))/(float)currMax)*(float)racialMax);
            setStat(abilityCode,((racialStat<1)&&(racialMax>0))?1:racialStat);
            setStat(CharStats.CODES.toMAXBASE(abilityCode),racialMax-baseMax);
        }
    }
    
	public void setStat(int abilityCode, int value)
	{
        if((value>Short.MAX_VALUE)||(value<Short.MIN_VALUE))
            Log.errOut("Value out of range",new Exception("Value out of range: "+value+" for "+abilityCode));
        if(abilityCode<stats.length)
			stats[abilityCode]=(short)value;
	}

	public int getCode(String abilityName)
	{
		String[] DESCS = CODES.DESCS();
        for(int i : CharStats.CODES.ALL())
			if(DESCS[i].startsWith(abilityName))
				return i;
		return -1;
	}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public int getSaveStatIndex() { return getStatCodes().length;}
	
	public String getStat(String abilityName)
	{
		int dex=CMParms.indexOfIgnoreCase(getStatCodes(),abilityName);
		if(dex>=0) return Integer.toString(getStat(dex));
		
		String[] DESCS=CODES.DESCS();
        for(int i : CharStats.CODES.ALL())
			if(DESCS[i].startsWith(abilityName))
				return Integer.toString(getStat(i));
		return null;
	}

	
	public String[] getStatCodes() { return CharStats.CODES.NAMES();}
	public boolean isStat(String code) { return CMParms.containsIgnoreCase(getStatCodes(),code);}
	public void setStat(String code, String val) {
		int dex=CMParms.indexOfIgnoreCase(getStatCodes(),code);
		if(dex>=0) 
			setStat(dex,CMath.s_parseIntExpression(val));
		else
        for(int i : CharStats.CODES.ALL())
			if(CODES.DESC(i).startsWith(code))
			{
				setStat(dex,CMath.s_parseIntExpression(val));
				return;
			}
	}
}
