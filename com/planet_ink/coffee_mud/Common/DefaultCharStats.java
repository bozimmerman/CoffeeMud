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
   Copyright 2001-2024 Bo Zimmerman

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
			final DefaultCharStats newStats=getClass().getDeclaredConstructor().newInstance();
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
	protected String		worshipCharID		= "";
	protected String		deityName			= null;
	protected short[]		bodyAlterations		= null;
	protected long			unwearableBitmap	= 0;
	protected int[]			breathables			= null;
	protected String		arriveStr			= null;
	protected String		leaveStr			= null;

	protected Map<String, Integer>	profAdj		= null;

	@SuppressWarnings("unchecked")
	private static final DoubleFilterer<Item>[]	emptyFiltererArray	= new DoubleFilterer[0];
	protected DoubleFilterer<Item>[]			proficiencies		= emptyFiltererArray;

	public DefaultCharStats()
	{
		reset();
	}

	private static int GEND_MNF    = 0;
	private static int GEND_NOUN   = 1;
	private static int GEND_HIMHER = 2;
	private static int GEND_HISHER = 3;
	private static int GEND_HESHE  = 4;
	private static int GEND_SIRMDM = 5;
	private static int GEND_MRMRS  = 6;
	private static int GEND_MISMDM = 7;
	private static int GEND_MANWOM = 8;
	private static int GEND_SONDAT = 9;
	private static int GEND_BOYGRL = 10;
	private static int GEND_HIMHEF = 11;
	private static int GEND_HISHEF = 12;

	@Override
	public void setAllBaseValues(final int def)
	{
		if((def>Short.MAX_VALUE)||(def<Short.MIN_VALUE))
			Log.errOut("Value out of range",new CMException("Value out of range: "+def+" for all"));
		for(final int i : CharStats.CODES.BASECODES())
			stats[i]=(short)def;
	}

	@Override
	public void setAllValues(final int def)
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
		deityName = null;
		genderName = null;
		displayClassName = null;
		displayClassLevel = null;
		bodyAlterations = null;
		unwearableBitmap = 0;
		breathables = null;
		arriveStr = null;
		leaveStr = null;
		profAdj = null;
		worshipCharID = "";
		proficiencies = emptyFiltererArray;
		setMyRace(CMClass.getRace("StdRace"));
		setCurrentClass(CMClass.getCharClass("StdCharClass"));
	}

	@Override
	public void copyInto(final CharStats intoStats)
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
			((DefaultCharStats)intoStats).deityName=deityName;
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
			((DefaultCharStats)intoStats).worshipCharID=worshipCharID;
		}
		else
		{
			final Pair<String,String> allClassInfo = getAllClassInfo();
			intoStats.setAllClassInfo(allClassInfo.first, allClassInfo.second);
			intoStats.setMyRace(getMyRace());
			intoStats.setRaceName(raceName);
			intoStats.setDeityName(deityName);
			intoStats.setGenderName(genderName);
			intoStats.setArriveLeaveStr(arriveStr,leaveStr);
			intoStats.setDisplayClassName(displayClassName);
			intoStats.setDisplayClassLevel(displayClassLevel);
			intoStats.setBodyPartsFromStringAfterRace(getBodyPartsAsString());
			intoStats.setWearableRestrictionsBitmap(unwearableBitmap|getMyRace().forbiddenWornBits());
			intoStats.setBreathables(breathables);
			intoStats.setItemProficiencies(proficiencies);
			intoStats.setWorshipCharID(worshipCharID);
		}
	}

	@Override
	public void setAllClassInfo(final String classes, final String levels)
	{
		final String[] classIDs = classes.trim().split(";");
		final String[] classLvls = levels.trim().split(";");
		final PairArrayList<CharClass,Integer> classV=new PairArrayList<CharClass,Integer>();
		int i = 0;
		for(final String id : classIDs)
		{
			if(id.length()==0)
				continue;
			CharClass C=CMClass.getCharClass(id);
			if(C==null)
				C=CMClass.getCharClass("StdCharClass");
			classV.add(C, Integer.valueOf((i < classLvls.length)?CMath.s_int(classLvls[i++]):0));
		}
		if(classV.size()==0)
			classV.add(CMClass.getCharClass("StdCharClass"),Integer.valueOf(0));
		myClasses=classV.toArrayFirst(new CharClass[classV.size()]);
		myLevels=classV.toArraySecond(new Integer[classV.size()]);
	}

	@Override
	public Pair<String,String> getAllClassInfo()
	{
		if((myClasses==null)||(myLevels==null))
			return new Pair<String,String>("StdCharClass","0");
		final StringBuilder classStr=new StringBuilder("");
		for (final CharClass myClasse : myClasses)
			classStr.append(";").append(myClasse.ID());
		if(classStr.length()>0)
			classStr.deleteCharAt(0);
		final StringBuilder levelStr=new StringBuilder("");
		for (final Integer myLevel : myLevels)
			levelStr.append(";").append(myLevel.toString());
		if(levelStr.length()>0)
			levelStr.deleteCharAt(0);
		return new Pair<String,String>(classStr.toString(), levelStr.toString());
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
	public String getWorshipCharID()
	{
		return worshipCharID;
	}

	@Override
	public void setWorshipCharID(final String newVal)
	{
		worshipCharID = (newVal == null)?"":newVal;
	}

	@Override
	public void setDeityName(final String newDeityName)
	{
		deityName=newDeityName;
	}

	@Override
	public String deityName()
	{
		if(deityName!=null)
			return deityName;
		return getWorshipCharID();
	}

	@Override
	public Deity getMyDeity()
	{
		if (worshipCharID.length() == 0)
			return null;
		return CMLib.map().getDeity(worshipCharID);
	}

	@Override
	public void setArriveLeaveStr(final String arriveStr, final String leaveStr)
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
	public void setWearableRestrictionsBitmap(final long bitmap)
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
	public void setDisplayClassName(final String newName)
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
	public void setDisplayClassLevel(final String newLevel)
	{
		displayClassLevel = newLevel;
	}

	@Override
	public String displayClassLevel(final MOB mob, final boolean shortForm)
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
	public String displayClassLevelOnly(final MOB mob)
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
	public void setNonBaseStatsFromString(final String str)
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
	public void setRaceName(final String newRaceName)
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
	public CharClass getMyClass(final int i)
	{
		if((myClasses==null)
		||(i<0)
		||(i>=myClasses.length))
			return CMClass.getCharClass("StdCharClass");
		return myClasses[i];
	}

	@Override
	public int getClassLevel(final String aClass)
	{
		if(myClasses==null)
			return -1;
		for(int i=0;i<myClasses.length;i++)
		{
			final CharClass C = myClasses[i];
			if((C!=null)
			&&(C.ID().equals(aClass))
			&&(i<myLevels.length))
			{
				final Integer I=myLevels[i];
				if(I!=null)
					return I.intValue();
			}
		}
		return -1;
	}

	@Override
	public int getClassLevel(final CharClass aClass)
	{
		if(aClass==null)
			return -1;
		return getClassLevel(aClass.ID());
	}

	protected void removeClass(final CharClass aClass)
	{
		int i, j;
		for (i = j = 0; j < myClasses.length; ++j)
		{
			final CharClass C = myClasses[j];
			if((C!=null) && (!aClass.ID().equals(C.ID())))
			{
				myLevels[i] = myLevels[j];
				myClasses[i++] = C;
			}
		}
		if(i == j)
			return;
		myClasses = Arrays.copyOf(myClasses, i);
		myLevels = Arrays.copyOf(myLevels, i);
	}

	@Override
	public void setClassLevel(final CharClass aClass, final int level)
	{
		if(aClass==null)
			return;
		if(myClasses==null)
		{
			myClasses=new CharClass[] { aClass };
			myLevels=new Integer[] { Integer.valueOf(level) };
			return;
		}
		if(level<0)
		{
			if(myClasses.length>1)
				removeClass(aClass);
			return;
		}
		if(getClassLevel(aClass)<0)
			setCurrentClass(aClass); // adds the class
		// finally sets the level
		for(int i=0;i<myClasses.length;i++)
		{
			final CharClass C=myClasses[i];
			if(C.ID().equals(aClass.ID()))
			{
				if(myLevels[i].intValue()!=level)
					myLevels[i]=Integer.valueOf(level);
				break;
			}
		}
	}

	@Override
	public boolean isLevelCapped(final CharClass C)
	{
		if((C==null)||(C.getLevelCap()<0)||(C.getLevelCap()==Integer.MAX_VALUE))
			return false;
		return getClassLevel(C) >= C.getLevelCap();
	}

	@Override
	public void setCurrentClassLevel(final int level)
	{
		final CharClass currentClass=getCurrentClass();
		if(currentClass!=null)
			setClassLevel(currentClass,level);
	}

	@Override
	public void setCurrentClass(final CharClass aClass)
	{
		if(aClass==null)
			return;
		if(((myClasses==null)||(myLevels==null))
		||((myClasses.length==1)
			&&((myClasses[0]==null)||(myClasses[0].ID().equals("StdCharClass")))))
		{
			myClasses=new CharClass[] {aClass};
			myLevels=new Integer[] {Integer.valueOf(0) };
			return;
		}

		final int level=getClassLevel(aClass);
		if(level<0) // need to add
		{
			// calculate length of all classes to see if new one would fit
			int newLen = aClass.ID().length()+1;
			for(final CharClass C : myClasses)
				newLen += C.ID().length()+1;
			while(newLen > 250)
			{
				final int oldLen = newLen;
				for(int i=0;i<myClasses.length;i++)
				{
					if((i<myLevels.length)&&(myLevels[i].intValue() == 0))
					{

						newLen -= (myClasses[i].ID().length()+1);
						removeClass(myClasses[i]);
						break;
					}
				}
				if(oldLen == newLen) // just give up, as aClass would be the only 0 level class
					return;
			}
			myClasses=Arrays.copyOf(myClasses, myClasses.length+1);
			myLevels=Arrays.copyOf(myLevels, myLevels.length+1);
			myClasses[myClasses.length-1]=aClass;
			myLevels[myLevels.length-1]=Integer.valueOf(0);
		}
		else // need to move
		{
			if(myClasses[myClasses.length-1]==aClass)
				return;
			final Integer classLvl=Integer.valueOf(level);
			boolean found=false;
			for(int i=0;i<myClasses.length-1;i++)
			{
				final CharClass C=myClasses[i];
				if((C==aClass)||(found))
				{
					found=true;
					myClasses[i]=myClasses[i+1];
					myLevels[i]=myLevels[i+1];
				}
			}
			myClasses[myClasses.length-1]=aClass;
			myLevels[myLevels.length-1]=classLvl;
		}
	}

	@Override
	public CharClass getCurrentClass()
	{
		return myClasses[myClasses.length-1];
	}

	@Override
	public Iterable<CharClass> getCharClasses()
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
	public void setMyRace(final Race newVal)
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
	public void setBreathables(final int[] newArray)
	{
		breathables=newArray;
	}

	@Override
	public int getBodyPart(final int racialPartNumber)
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
	public void setBodyPartsFromStringAfterRace(final String str)
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
	public int getBodypartAlteration(final int racialPartNumber)
	{
		if(bodyAlterations==null)
			return 0;
		return bodyAlterations[racialPartNumber];
	}

	@Override
	public void alterBodypart(final int racialPartNumber, final int deviation)
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
	public int getSave(final int which)
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
			return getStat(STAT_SAVE_UNDEAD)+getStat(STAT_WISDOM)+getStat(STAT_SAVE_DOUBT);
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
		case STAT_SAVE_DOUBT:
			return getStat(STAT_SAVE_DOUBT);
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
		newOne.stats=stats.clone();
		if(myClasses!=null)
			newOne.myClasses=myClasses.clone();
		if(myLevels!=null)
			newOne.myLevels=myLevels.clone();
		if(myRace!=null)
			newOne.myRace=myRace;
		newOne.raceName=raceName;
		newOne.genderName=genderName;
		newOne.displayClassName=displayClassName;
		newOne.displayClassLevel=displayClassLevel;
		newOne.worshipCharID=worshipCharID;
		if(bodyAlterations!=null)
			newOne.bodyAlterations=bodyAlterations.clone();
		newOne.unwearableBitmap=unwearableBitmap;
		if(breathables!=null)
			newOne.breathables=breathables.clone();
		newOne.arriveStr=arriveStr;
		newOne.leaveStr=leaveStr;
		if(profAdj!=null)
			newOne.profAdj = new TreeMap<String,Integer>(profAdj);
		if(proficiencies!=null)
			newOne.proficiencies=proficiencies.clone();
		return newOne;
	}

	@Override
	public int getAbilityAdjustment(final String ableID)
	{

		final Map<String,Integer> prof=this.profAdj;
		if(prof == null)
			return 0;
		final Integer value = prof.get(ableID.toUpperCase());
		if(value == null)
			return 0;
		return value.intValue();
	}

	@Override
	public void adjustAbilityAdjustment(final String ableID, final int newValue)
	{
		Map<String,Integer> prof=this.profAdj;
		if(prof == null)
		{
			prof = new TreeMap<String,Integer>();
			this.profAdj=prof;
		}
		prof.put(ableID.toUpperCase(), Integer.valueOf(newValue));
	}

	@Override
	public void setGenderName(final String gname)
	{
		genderName=gname;
	}

	@Override
	public char reproductiveCode()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
				? Character.toUpperCase(genderName.charAt(0))
				: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_MNF].charAt(0);
	}

	@Override
	public String realGenderName()
	{
		final String[] set = CMProps.getGenderDef(stats[STAT_GENDER]);
		return set[GEND_NOUN];
	}

	@Override
	public String genderName()
	{
		if(genderName!=null)
			return genderName;
		return realGenderName();
	}

	@Override
	public String himher()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_HIMHER];
	}

	@Override
	public String hisher()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_HISHER];
	}

	@Override
	public String himherself()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_HIMHEF];
	}

	@Override
	public String hisherself()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_HISHEF];
	}

	@Override
	public String heshe()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_HESHE];
	}

	@Override
	public String sirmadam()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_SIRMDM];
	}

	@Override
	public String SirMadam()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return CMStrings.capitalizeFirstLetter(set[GEND_SIRMDM]);
	}

	@Override
	public String MrMs()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return CMStrings.capitalizeFirstLetter(set[GEND_MRMRS]);
	}

	@Override
	public String MisterMadam()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return CMStrings.capitalizeAndLower(set[GEND_MISMDM]);
	}

	@Override
	public String manwoman()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_MANWOM];
	}

	@Override
	public String sondaughter()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_SONDAT];
	}

	@Override
	public String boygirl()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return set[GEND_BOYGRL];
	}

	@Override
	public String HeShe()
	{
		final char c=((genderName!=null)&&(genderName.length()>0))
					? Character.toUpperCase(genderName.charAt(0))
					: (char)stats[STAT_GENDER];
		final String[] set = CMProps.getGenderDef(c);
		return CMStrings.capitalizeFirstLetter(set[GEND_HESHE]);
	}

	@Override
	public int getStat(final int abilityCode)
	{
		if(abilityCode<stats.length)
			return stats[abilityCode];
		return 0;
	}

	@Override
	public void setPermanentStat(final int abilityCode, final int value)
	{
		setStat(abilityCode,value);
		if(CharStats.CODES.isBASE(abilityCode))
			setStat(CharStats.CODES.toMAXBASE(abilityCode),value-CMProps.getIntVar(CMProps.Int.BASEMAXSTAT));
	}

	@Override
	public int getMaxStat(final int abilityCode)
	{
		final int baseMax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		return baseMax + getStat(CharStats.CODES.toMAXBASE(abilityCode));
	}

	@Override
	public int getRacialStat(final MOB mob, final int statNum)
	{
		final CharStats copyStats=(CharStats)copyOf();
		getMyRace().affectCharStats(mob,copyStats);
		for(final CharClass C : myClasses)
			C.affectCharStats(mob,copyStats);
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
	public void setStat(final int abilityCode, final int value)
	{
		// needs to support all kinds of crazy values here because this is also
		// a work-container
		if(abilityCode<stats.length)
			stats[abilityCode]=(short)value;
	}

	@Override
	public void adjStat(final int statNum, final int value)
	{
		if(statNum<stats.length)
		{
			stats[statNum]+=(short)value;
			if(CharStats.CODES.isBASE(statNum))
			{
				if(stats[statNum]<1) // negative/0 absolute stat values are bad.
					stats[statNum]=1;
				final int maxStatNum=CharStats.CODES.toMAXBASE(statNum);
				stats[maxStatNum]+=(short)value;
				// negative maxstats are ok, as they are deltas!
			}
		}
	}

	@Override
	public int getStatCode(final String abilityName)
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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public String getStat(final String abilityName)
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
	public boolean isStat(final String code)
	{
		return CMParms.containsIgnoreCase(getStatCodes(), code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		final int dex=CMParms.indexOfIgnoreCase(getStatCodes(),code);
		if(dex>=0)
		{
			if((dex == CharStats.STAT_GENDER)
			&&(val.length()>0))
			{
				if((val.length()==1)||(Character.isLetter(val.charAt(0))))
					setStat(dex,Character.toUpperCase(val.charAt(0)));
				else
					setStat(dex,CMath.s_parseIntExpression(val));
			}
			else
				setStat(dex,CMath.s_parseIntExpression(val));
		}
		else
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(CODES.DESC(i).startsWith(code))
			{
				setStat(i,CMath.s_parseIntExpression(val));
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
	public void setItemProficiencies(final DoubleFilterer<Item>[] newArray)
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
			public DoubleFilterer.Result getFilterResult(final Item obj)
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
			public DoubleFilterer.Result getFilterResult(final Item obj)
			{
				return CMLib.masking().maskCheck(mask, obj, true) ? DoubleFilterer.Result.REJECTED : DoubleFilterer.Result.NOTAPPLICABLE;
			}
		};
		this.proficiencies = newerArray;
	}
}
