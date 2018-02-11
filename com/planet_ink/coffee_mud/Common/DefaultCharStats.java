package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CoffeeMudException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class DefaultCharStats implements CharStats
{
	@Override
	public String ID()
	{
		return "DefaultCharStats";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final DefaultCharStats newStats=getClass().newInstance();
			if(newStats.myRace==null)
				newStats.myRace=CMClass.getRace("StdRace");
			return newStats;
		}
		catch(final Exception e)
		{
			return new DefaultCharStats();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	// competency characteristics
			
	protected short[]		stats				= new short[CharStats.CODES.instance().total()];
	protected CharClass[]	myClasses			= null;
	protected Integer[]		myLevels			= null;
	protected Race			myRace;
	protected String		raceName			= null;
	protected String		genderName			= null;
	protected String		displayClassName	= null;
	protected String		displayClassLevel	= null;
	protected short[]		bodyAlterations		= null;
	protected long			unwearableBitmap	= 0;
	protected int[]			breathables			= null;
	protected String		arriveStr			= null;
	protected String		leaveStr			= null;
	
	protected Map<String, Integer>	profAdj			= null;
	
	@SuppressWarnings("unchecked")
	private static final DoubleFilterer<Item>[]	emptyFiltererArray	= new DoubleFilterer[0]; 
	protected DoubleFilterer<Item>[]			proficiencies		= emptyFiltererArray;
	
	public DefaultCharStats()
	{
		reset();
	}
	
	@Override
	public void setAllBaseValues(int def)
	{
		if((def>Short.MAX_VALUE)||(def<Short.MIN_VALUE))
			Log.errOut("Value out of range",new CMException("Value out of range: "+def+" for all"));
		for(final int i : CharStats.CODES.BASECODES())
			stats[i]=(short)def;
	}

	@Override
	public void setAllValues(int def)
	{
		if((def>Short.MAX_VALUE)||(def<Short.MIN_VALUE))
			Log.errOut("Value out of range",new CMException("Value out of range: "+def+" for all"));
		for(final int i: CharStats.CODES.ALLCODES())
			stats[i]=(short)def;
		unwearableBitmap=0;
	}

	@Override
	public void reset()
	{
		setAllBaseValues(VALUE_ALLSTATS_DEFAULT);
		stats[STAT_GENDER]='M';
		//myClasses;  // never null
		myLevels = null;
		//myRace; // never null
		raceName = null;
		genderName = null;
		displayClassName = null;
		displayClassLevel = null;
		bodyAlterations = null;
		unwearableBitmap = 0;
		breathables = null;
		arriveStr = null;
		leaveStr = null;
		profAdj = null;
		proficiencies = emptyFiltererArray;
		setMyRace(CMClass.getRace("StdRace"));
		setCurrentClass(CMClass.getCharClass("StdCharClass"));
	}

	@Override
	public void copyInto(CharStats intoStats)
	{
		if(intoStats instanceof DefaultCharStats)
		{
			((DefaultCharStats)intoStats).arriveStr = arriveStr;
			((DefaultCharStats)intoStats).leaveStr = leaveStr;
			((DefaultCharStats)intoStats).breathables = breathables;
			((DefaultCharStats)intoStats).proficiencies = proficiencies;
			if(myClasses==null)
				((DefaultCharStats)intoStats).myClasses=null;
			else
			if((((DefaultCharStats)intoStats).myClasses!=null)
			&&(((DefaultCharStats)intoStats).myClasses.length==myClasses.length))
			{
				for(int i=0;i<myClasses.length;i++)
					((DefaultCharStats)intoStats).myClasses[i]=myClasses[i];
			}
			else
				((DefaultCharStats)intoStats).myClasses=myClasses.clone();
			if(myLevels==null)
				((DefaultCharStats)intoStats).myLevels=null;
			else
			if((((DefaultCharStats)intoStats).myLevels!=null)
			&&(((DefaultCharStats)intoStats).myLevels.length==myLevels.length))
			{
				for(int i=0;i<myLevels.length;i++)
					((DefaultCharStats)intoStats).myLevels[i]=myLevels[i];
			}
			else
				((DefaultCharStats)intoStats).myLevels=myLevels.clone();
			if(myRace!=null)
				((DefaultCharStats)intoStats).myRace=myRace;
			((DefaultCharStats)intoStats).raceName=raceName;
			((DefaultCharStats)intoStats).genderName=genderName;
			((DefaultCharStats)intoStats).displayClassName=displayClassName;
			((DefaultCharStats)intoStats).displayClassLevel=displayClassLevel;
			if(profAdj==null)
				((DefaultCharStats)intoStats).profAdj=null;
			else
				((DefaultCharStats)intoStats).profAdj=new TreeMap<String,Integer>(profAdj);
			if(bodyAlterations==null)
				((DefaultCharStats)intoStats).bodyAlterations=null;
			else
			if((((DefaultCharStats)intoStats).bodyAlterations!=null)
			&&(((DefaultCharStats)intoStats).bodyAlterations.length==bodyAlterations.length))
			{
				for(int i=0;i<bodyAlterations.length;i++)
					((DefaultCharStats)intoStats).bodyAlterations[i]=bodyAlterations[i];
			}
			else
				((DefaultCharStats)intoStats).bodyAlterations=bodyAlterations.clone();
			for(int i=0;i<stats.length;i++)
				((DefaultCharStats)intoStats).stats[i]=stats[i];
			((DefaultCharStats)intoStats).unwearableBitmap=unwearableBitmap|myRace.forbiddenWornBits();
		}
		else
		{
			intoStats.setMyClasses(getMyClassesStr());
			intoStats.setMyLevels(getMyLevelsStr());
			intoStats.setMyRace(getMyRace());
			intoStats.setRaceName(raceName);
			intoStats.setRaceName(raceName);
			intoStats.setGenderName(genderName);
			intoStats.setArriveLeaveStr(arriveStr,leaveStr);
			intoStats.setDisplayClassName(displayClassName);
			intoStats.setDisplayClassLevel(displayClassLevel);
			intoStats.setBodyPartsFromStringAfterRace(getBodyPartsAsString());
			intoStats.setWearableRestrictionsBitmap(unwearableBitmap|getMyRace().forbiddenWornBits());
			intoStats.setBreathables(breathables);
			intoStats.setItemProficiencies(proficiencies);
		}
	}

	@Override
	public void setMyClasses(String classes)
	{
		int x=classes.indexOf(';');
		final ArrayList<CharClass> classV=new ArrayList<CharClass>();
		CharClass C=null;
		while(x>=0)
		{
			final String theClass=classes.substring(0,x).trim();
			classes=classes.substring(x+1);
			if(theClass.length()>0)
			{
				C=CMClass.getCharClass(theClass);
				if(C==null)
					C=CMClass.getCharClass("StdCharClass");
				classV.add(C);
			}
			x=classes.indexOf(';');
		}
		if(classes.trim().length()>0)
		{
			C=CMClass.getCharClass(classes.trim());
			if(C==null)
				C=CMClass.getCharClass("StdCharClass");
			classV.add(C);
		}
		myClasses=classV.toArray(new CharClass[0]);
	}

	@Override
	public void setMyLevels(String levels)
	{
		if((levels.length()==0)&&(myClasses!=null)&&(myClasses.length>0))
			levels="0";
		int x=levels.indexOf(';');
		final ArrayList<Integer> levelV=new ArrayList<Integer>();
		while(x>=0)
		{
			final String theLevel=levels.substring(0,x).trim();
			levels=levels.substring(x+1);
			if(theLevel.length()>0)
				levelV.add(Integer.valueOf(CMath.s_int(theLevel)));
			x=levels.indexOf(';');
		}
		if(levels.trim().length()>0)
			levelV.add(Integer.valueOf(CMath.s_int(levels)));
		myLevels=levelV.toArray(new Integer[0]);
	}

	@Override
	public String getMyClassesStr()
	{
		if(myClasses==null)
			return "StdCharClass";
		String classStr="";
		for (final CharClass myClasse : myClasses)
			classStr+=";"+myClasse.ID();
		if(classStr.length()>0)
			classStr=classStr.substring(1);
		return classStr;
	}

	@Override
	public String getMyLevelsStr()
	{
		if(myLevels==null)
			return "";
		String levelStr="";
		for (final Integer myLevel : myLevels)
			levelStr+=";"+myLevel.intValue();
		if(levelStr.length()>0)
			levelStr=levelStr.substring(1);
		return levelStr;
	}

	@Override
	public String getArriveStr()
	{
		if(arriveStr==null)
			return myRace.arriveStr();
		return arriveStr;
	}

	@Override
	public String getLeaveStr()
	{
		if(leaveStr==null)
			return myRace.leaveStr();
		return leaveStr;
	}

	@Override
	public void setArriveLeaveStr(String arriveStr, String leaveStr)
	{
		this.arriveStr = arriveStr;
		this.leaveStr=leaveStr;
	}
	
	@Override
	public long getWearableRestrictionsBitmap()
	{
		return unwearableBitmap;
	}

	@Override
	public void setWearableRestrictionsBitmap(long bitmap)
	{
		unwearableBitmap = bitmap;
	}

	@Override
	public int numClasses()
	{
		if(myClasses==null)
			return 0;
		return myClasses.length;
	}

	@Override
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

	@Override
	public void setDisplayClassName(String newName)
	{
		displayClassName = newName;
	}

	@Override
	public String displayClassName()
	{
		if(displayClassName!=null)
			return displayClassName;
		return getCurrentClass().name(getCurrentClassLevel());
	}

	@Override
	public void setDisplayClassLevel(String newLevel)
	{
		displayClassLevel = newLevel;
	}

	@Override
	public String displayClassLevel(MOB mob, boolean shortForm)
	{
		if(displayClassLevel!=null)
		{
			if(shortForm)
				return displayClassName()+" "+displayClassLevel;
			return "level "+displayClassLevel+" "+displayClassName;
		}
		if(mob==null)
			return "";
		final int classLevel=getClassLevel(getCurrentClass());
		String levelStr=null;
		if(classLevel>=mob.phyStats().level())
			levelStr=""+mob.phyStats().level();
		else
			levelStr=classLevel+"/"+mob.phyStats().level();
		if(shortForm)
			return displayClassName()+" "+levelStr;
		return "level "+levelStr+" "+displayClassName();
	}

	@Override
	public String displayClassLevelOnly(MOB mob)
	{
		if(mob==null)
			return "";
		if(displayClassLevel!=null)
			return displayClassLevel;
		final int classLevel=getClassLevel(getCurrentClass());
		String levelStr=null;
		if(classLevel>=mob.phyStats().level())
			levelStr=""+mob.phyStats().level();
		else
			levelStr=classLevel+"/"+mob.phyStats().level();
		return levelStr;
	}

	@Override
	public String getNonBaseStatsAsString()
	{
		final StringBuffer str=new StringBuffer("");
		final CharStats.CODES C = CharStats.CODES.instance();
		for(final int x : C.all())
		{
			if((!C.isBase(x))&&(x!=CharStats.STAT_GENDER))
				str.append(stats[x]+";");
		}
		return str.toString();
	}

	@Override
	public void setNonBaseStatsFromString(String str)
	{
		final List<String> V=CMParms.parseSemicolons(str,false);
		final CharStats.CODES C = CharStats.CODES.instance();
		for(final int x : C.all())
		{
			if((!C.isBase(x))&&(x!=CharStats.STAT_GENDER)&&(V.size()>0))
			{
				final long val=CMath.s_long(V.remove(0));
				if((val>Short.MAX_VALUE)||(val<Short.MIN_VALUE))
					Log.errOut("Value out of range","Value out of range: "+val+" for "+x+" from "+str);
				stats[x]=(short)val;
			}
		}
	}

	@Override
	public void setRaceName(String newRaceName)
	{
		raceName=newRaceName;
	}

	@Override
	public String raceName()
	{
		if(raceName!=null)
			return raceName;
		if(myRace!=null)
			return myRace.name();
		return "MOB";
	}

	@Override
	public CharClass getMyClass(int i)
	{
		if((myClasses==null)
		||(i<0)
		||(i>=myClasses.length))
			return CMClass.getCharClass("StdCharClass");
		return myClasses[i];
	}

	@Override
	public int getClassLevel(String aClass)
	{
		if(myClasses==null)
			return -1;
		for(int i=0;i<myClasses.length;i++)
		{
			if((myClasses[i]!=null)
			&&(myClasses[i].ID().equals(aClass))
			&&(i<myLevels.length))
				return myLevels[i].intValue();
		}
		return -1;
	}

	@Override
	public int getClassLevel(CharClass aClass)
	{
		if((myClasses==null)||(aClass==null))
			return -1;
		for(int i=0;i<myClasses.length;i++)
		{
			if((myClasses[i]!=null)
			&&(myClasses[i].ID().equals(aClass.ID()))
			&&(i<myLevels.length))
				return myLevels[i].intValue();
		}
		return -1;
	}

	@Override
	public void setClassLevel(CharClass aClass, int level)
	{
		if(aClass==null)
			return;
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
				final CharClass[] oldClasses=myClasses;
				final Integer[] oldLevels=myLevels;
				final CharClass[] myNewClasses=new CharClass[oldClasses.length-1];
				final Integer[] myNewLevels=new Integer[oldClasses.length-1];
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
				final CharClass C=getMyClass(i);
				if((C==aClass)&&(myLevels[i].intValue()!=level))
				{
					myLevels[i]=Integer.valueOf(level);
					break;
				}
			}
		}
	}

	@Override
	public boolean isLevelCapped(CharClass C)
	{
		if((C==null)||(C.getLevelCap()<0)||(C.getLevelCap()==Integer.MAX_VALUE))
			return false;
		return getClassLevel(C) >= C.getLevelCap();
	}

	@Override
	public void setCurrentClassLevel(int level)
	{
		final CharClass currentClass=getCurrentClass();
		if(currentClass!=null)
			setClassLevel(currentClass,level);
	}

	@Override
	public void setCurrentClass(CharClass aClass)
	{
		if(aClass==null)
			return;
		if(((myClasses==null)||(myLevels==null))
		||((numClasses()==1)&&(myClasses[0].ID().equals("StdCharClass"))))
		{
			myClasses=new CharClass[1];
			myLevels=new Integer[1];
			myClasses[0]=aClass;
			myLevels[0]=Integer.valueOf(0);
			return;
		}

		final int level=getClassLevel(aClass);
		if(level<0)
		{
			final CharClass[] oldClasses=myClasses;
			final Integer[] oldLevels=myLevels;
			final CharClass[] myNewClasses=new CharClass[oldClasses.length+1];
			final Integer[] myNewLevels=new Integer[oldClasses.length+1];
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
			final Integer oldI=Integer.valueOf(level);
			boolean go=false;
			final CharClass[] myNewClasses=myClasses.clone();
			final Integer[] myNewLevels=myLevels.clone();
			for(int i=0;i<myNewClasses.length-1;i++)
			{
				final CharClass C=getMyClass(i);
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

	@Override
	public CharClass getCurrentClass()
	{
		return myClasses[myClasses.length-1];
	}

	@Override
	public Collection<CharClass> getCharClasses()
	{
		return Arrays.asList(myClasses);
	}

	@Override
	public int getCurrentClassLevel()
	{
		if(myLevels==null)
			return -1;
		return myLevels[myLevels.length-1].intValue();
	}

	@Override
	public Race getMyRace()
	{
		return myRace;
	}

	@Override
	public void setMyRace(Race newVal)
	{
		if(newVal != null)
			myRace=newVal;
	}

	@Override
	public int[] getBreathables()
	{
		return (breathables!=null)?breathables:myRace.getBreathables();
	}

	@Override
	public void setBreathables(int[] newArray)
	{
		breathables=newArray;
	}
	
	@Override
	public int getBodyPart(int racialPartNumber)
	{
		int num=getMyRace().bodyMask()[racialPartNumber];
		if((num<0)||(bodyAlterations==null))
			return num;
		num+=bodyAlterations[racialPartNumber];
		if(num<0)
			return 0;
		return num;
	}

	@Override
	public String getBodyPartsAsString()
	{
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<getMyRace().bodyMask().length;i++)
			str.append(getBodyPart(i)+";");
		return str.toString();
	}

	@Override
	public void setBodyPartsFromStringAfterRace(String str)
	{
		final List<String> V=CMParms.parseSemicolons(str,true);
		bodyAlterations=null;
		for(int i=0;i<getMyRace().bodyMask().length;i++)
		{
			if(V.size()<=i)
				break;
			final int val=CMath.s_int(V.get(i));
			final int num=getMyRace().bodyMask()[i];
			if(num!=val)
				alterBodypart(i,val-num);
		}
	}

	@Override
	public int getBodypartAlteration(int racialPartNumber)
	{
		if(bodyAlterations==null)
			return 0;
		return bodyAlterations[racialPartNumber];
	}

	@Override
	public void alterBodypart(int racialPartNumber, int deviation)
	{
		if(bodyAlterations==null)
			bodyAlterations=new short[Race.BODY_PARTS];
		bodyAlterations[racialPartNumber]+=deviation;
	}

	@Override
	public int ageCategory()
	{
		final int age=getStat(STAT_AGE);
		int cat=Race.AGE_INFANT;
		final int[] chart=getMyRace().getAgingChart();
		if(age<chart[1])
			return cat;
		while((cat<=Race.AGE_ANCIENT)&&(age>=chart[cat]))
			cat++;
		return cat-1;
	}

	@Override
	public String ageName()
	{
		final int cat=ageCategory();
		if(cat<Race.AGE_ANCIENT)
			return Race.AGE_DESCS[cat];
		int age=getStat(STAT_AGE);
		final int[] chart=getMyRace().getAgingChart();
		final int diff=chart[Race.AGE_ANCIENT]-chart[Race.AGE_VENERABLE];
		age=age-chart[Race.AGE_ANCIENT];
		final int num=(diff>0)?(int)Math.abs(Math.floor(CMath.div(age,diff))):0;
		if(num<=0)
			return Race.AGE_DESCS[cat];
		return Race.AGE_DESCS[cat]+" "+CMath.convertToRoman(num);
	}

	@Override
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
		case STAT_SAVE_BLUNT:
			return getStat(STAT_SAVE_BLUNT);
		case STAT_SAVE_PIERCE:
			return getStat(STAT_SAVE_PIERCE);
		case STAT_SAVE_SLASH:
			return getStat(STAT_SAVE_SLASH);
		case STAT_SAVE_SPELLS:
			return getStat(STAT_SAVE_SPELLS);
		case STAT_SAVE_PRAYERS:
			return getStat(STAT_SAVE_PRAYERS);
		case STAT_SAVE_SONGS:
			return getStat(STAT_SAVE_SONGS);
		case STAT_SAVE_CHANTS:
			return getStat(STAT_SAVE_CHANTS);
		}
		return getStat(which);
	}

	// create a new one of these
	@Override
	public CMObject copyOf()
	{
		final DefaultCharStats newOne=new DefaultCharStats();
		if(myClasses!=null)
			newOne.myClasses=myClasses.clone();
		if(myRace!=null)
			newOne.myRace=myRace;
		if(myLevels!=null)
			newOne.myLevels=myLevels.clone();
		if(bodyAlterations!=null)
			newOne.bodyAlterations=bodyAlterations.clone();
		if(profAdj!=null)
			newOne.profAdj = new TreeMap<String,Integer>(profAdj);
		newOne.stats=stats.clone();
		return newOne;
	}

	@Override
	public int getAbilityAdjustment(String ableID)
	{
		
		final Map<String,Integer> prof=this.profAdj;
		if(prof == null)
			return 0;
		final Integer value = prof.get(ableID);
		if(value == null)
			return 0;
		return value.intValue();
	}

	@Override
	public void adjustAbilityAdjustment(String ableID, int newValue)
	{
		Map<String,Integer> prof=this.profAdj;
		if(prof == null)
		{
			prof = new TreeMap<String,Integer>();
			this.profAdj=prof;
		}
		prof.put(ableID, Integer.valueOf(newValue));
	}
	
	@Override
	public void setGenderName(String gname)
	{
		genderName=gname;
	}

	@Override
	public String genderName()
	{
		if(genderName!=null)
			return genderName;
		switch(getStat(STAT_GENDER))
		{
		case 'M':
			return CMLib.lang().L("male");
		case 'F':
			return CMLib.lang().L("female");
		default:
			return CMLib.lang().L("neuter");
		}
	}

	@Override
	public String himher()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("him");
		case 'F':
			return CMLib.lang().L("her");
		default:
			return CMLib.lang().L("it");
		}
	}

	@Override
	public String hisher()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("his");
		case 'F':
			return CMLib.lang().L("her");
		default:
			return CMLib.lang().L("its");
		}
	}

	@Override
	public String heshe()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("he");
		case 'F':
			return CMLib.lang().L("she");
		default:
			return CMLib.lang().L("it");
		}
	}

	@Override
	public String sirmadam()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("sir");
		case 'F':
			return CMLib.lang().L("madam");
		default:
			return CMLib.lang().L("sir");
		}
	}

	@Override
	public String SirMadam()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("Sir");
		case 'F':
			return CMLib.lang().L("Madam");
		default:
			return CMLib.lang().L("Sir");
		}
	}

	@Override
	public String HeShe()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0)) 
					: (char)getStat(STAT_GENDER);
		switch(c)
		{
		case 'M':
			return CMLib.lang().L("He");
		case 'F':
			return CMLib.lang().L("She");
		default:
			return CMLib.lang().L("It");
		}
	}

	@Override
	public int getStat(int abilityCode)
	{
		if(abilityCode<stats.length)
			return stats[abilityCode];
		return 0;
	}

	@Override
	public void setPermanentStat(int abilityCode, int value)
	{
		setStat(abilityCode,value);
		if(CharStats.CODES.isBASE(abilityCode))
			setStat(CharStats.CODES.toMAXBASE(abilityCode),value-CMProps.getIntVar(CMProps.Int.BASEMAXSTAT));
	}

	@Override
	public int getMaxStat(int abilityCode)
	{
		final int baseMax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		return baseMax + getStat(CharStats.CODES.toMAXBASE(abilityCode));
	}

	@Override
	public int getRacialStat(MOB mob, int statNum)
	{
		final CharStats copyStats=(CharStats)copyOf();
		getMyRace().affectCharStats(mob,copyStats);
		for(int c=0;c<numClasses();c++)
			getMyClass(c).affectCharStats(mob,copyStats);
		return copyStats.getStat(statNum);
	}

	@Override
	public void setRacialStat(final int abilityCode, final int racialMax)
	{
		if((!CharStats.CODES.isBASE(abilityCode))||(getStat(abilityCode)==VALUE_ALLSTATS_DEFAULT))
			setPermanentStat(abilityCode,racialMax);
		else
		{
			final int baseMax=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
			int currMax=getStat(CharStats.CODES.toMAXBASE(abilityCode))+baseMax;
			if(currMax<=0)
				currMax=1;
			int curStat=getStat(abilityCode);
			if(curStat > currMax*7)
			{
				final String errorMsg="Detected mob with "+curStat+"/"+currMax+" "+CharStats.CODES.ABBR(abilityCode);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Set<String> errs=(Set)Resources.getResource("SYSTEM_DEFCHARSTATS_ERRORS");
				if(errs==null)
				{
					errs=new TreeSet<String>();
					Resources.submitResource("SYSTEM_DEFCHARSTATS_ERRORS", errs);
				}
				if(!errs.contains(errorMsg))
				{
					errs.add(errorMsg);
					final StringBuilder str=new StringBuilder(errorMsg);
					//ByteArrayOutputStream stream=new ByteArrayOutputStream();
					//new Exception().printStackTrace(new PrintStream(stream));
					//str.append("\n\r"+new String(stream.toByteArray()));
					Log.errOut("DefCharStats",str.toString());
				}
				curStat=currMax*7;
			}
			final int pctOfMax=Math.round(((float)curStat/(float)currMax)*racialMax);
			final int stdMaxAdj=Math.round((((float)(currMax-VALUE_ALLSTATS_DEFAULT))/(float)currMax)*racialMax);
			final int racialStat=pctOfMax+stdMaxAdj;
			setStat(abilityCode,((racialStat<1)&&(racialMax>0))?1:racialStat);
			setStat(CharStats.CODES.toMAXBASE(abilityCode),racialMax-baseMax);
		}
	}

	@Override
	public void setStat(int abilityCode, int value)
	{
		if(abilityCode<stats.length)
			stats[abilityCode]=(short)value;
	}

	@Override
	public int getCode(String abilityName)
	{
		final String[] DESCS = CODES.DESCS();
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(DESCS[i].startsWith(abilityName))
				return i;
		}
		return -1;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public String getStat(String abilityName)
	{
		final int dex=CMParms.indexOfIgnoreCase(getStatCodes(),abilityName);
		if(dex>=0)
			return Integer.toString(getStat(dex));

		final String[] DESCS=CODES.DESCS();
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(DESCS[i].startsWith(abilityName))
				return Integer.toString(getStat(i));
		}
		return null;
	}

	@Override
	public String[] getStatCodes()
	{
		return CharStats.CODES.NAMES();
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.containsIgnoreCase(getStatCodes(), code);
	}

	@Override
	public void setStat(String code, String val)
	{
		final int dex=CMParms.indexOfIgnoreCase(getStatCodes(),code);
		if(dex>=0)
			setStat(dex,CMath.s_parseIntExpression(val));
		else
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(CODES.DESC(i).startsWith(code))
			{
				setStat(dex,CMath.s_parseIntExpression(val));
				return;
			}
		}
	}

	@Override
	public DoubleFilterer<Item>[] getItemProficiencies()
	{
		return proficiencies;
	}

	@Override
	public void setItemProficiencies(DoubleFilterer<Item>[] newArray)
	{
		this.proficiencies = newArray;
	}

	@Override
	public void addItemProficiency(final String zapperMask)
	{
		final DoubleFilterer<Item>[] newerArray = Arrays.copyOf(this.proficiencies, this.proficiencies.length + 1);
		newerArray[newerArray.length-1] = new DoubleFilterer<Item>()
		{
			final MaskingLibrary.CompiledZMask mask = CMLib.masking().getPreCompiledMask(zapperMask);
			
			@Override
			public DoubleFilterer.Result getFilterResult(Item obj)
			{
				return CMLib.masking().maskCheck(mask, obj, true) ? DoubleFilterer.Result.ALLOWED : DoubleFilterer.Result.NOTAPPLICABLE; 
			}
		};
		this.proficiencies = newerArray;
	}
	
	@Override
	public void addItemDeficiency(final String zapperMask)
	{
		final DoubleFilterer<Item>[] newerArray = Arrays.copyOf(this.proficiencies, this.proficiencies.length + 1);
		newerArray[newerArray.length-1] = new DoubleFilterer<Item>()
		{
			final MaskingLibrary.CompiledZMask mask = CMLib.masking().getPreCompiledMask(zapperMask);
			
			@Override
			public DoubleFilterer.Result getFilterResult(Item obj)
			{
				return CMLib.masking().maskCheck(mask, obj, true) ? DoubleFilterer.Result.REJECTED : DoubleFilterer.Result.NOTAPPLICABLE; 
			}
		};
		this.proficiencies = newerArray;
	}
}
