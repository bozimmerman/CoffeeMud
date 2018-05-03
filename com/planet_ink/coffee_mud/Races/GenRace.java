package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.GenCharClass;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class GenRace extends StdRace
{
	protected String			ID					= "GenRace";
	protected String			name				= "GenRace";
	public int					availability		= 0;
	public int[]				agingChart			= null;
	protected String[]			xtraValues			= null;
	public boolean				isRideable			= false;
	public int					shortestMale		= 24;
	public int					shortestFemale		= 24;
	public int					heightVariance		= 5;
	public int					lightestWeight		= 60;
	public int					weightVariance		= 10;
	public int					xpAdjustmentPct		= 0;
	public long					forbiddenWornBits	= 0;
	public String				racialCategory		= "Unknown";
	protected int				disableFlags		= 0;
	protected CharStats			setStats			= null;
	protected CharStats			adjStats			= null;
	protected PhyStats			adjPStats			= null;
	protected CharState			adjState			= null;
	protected CharState			startAdjState		= null;
	protected List<RawMaterial>	resourceChoices		= null;
	protected Race				healthBuddy			= null;
	protected Race				eventBuddy			= null;
	protected Race				weaponBuddy			= null;
	protected String			helpEntry			= "";
	protected String[]			racialEffectNames	= null;
	protected int[]				racialEffectLevels	= null;
	protected String[]			racialEffectParms	= null;
	protected String[]			racialAbilityNames	= null;
	protected int[]				racialAbilityLevels	= null;
	protected int[]				racialAbilityProfs	= null;
	protected boolean[]			racialAbilityQuals	= null;
	protected String[]			racialAbilityParms	= null;
	protected String[]			culturalAbilityNames= null;
	protected int[]				culturalAbilityProfs= null;
	protected int[]				culturalAbilityLvls = null;
	protected boolean[]			culturalAbilityGains= null;
	protected int[]				sortedBreathables	= new int[] { RawMaterial.RESOURCE_AIR };
	protected boolean			destroyBodyAfterUse	= false;
	protected String			arriveStr			= "arrives";
	protected String			leaveStr			= "leaves";

	//  				   an ey ea he ne ar ha to le fo no gi mo wa ta wi
	protected int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public String ID()
	{
		return ID;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public int practicesAtFirstLevel()
	{
		return 0;
	}

	@Override
	public int trainsAtFirstLevel()
	{
		return 0;
	}

	@Override
	public long forbiddenWornBits()
	{
		return forbiddenWornBits;
	}

	@Override
	public String racialCategory()
	{
		return racialCategory;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public int getXPAdjustment()
	{
		return xpAdjustmentPct;
	}
	
	@Override
	public int shortestFemale()
	{
		return shortestFemale;
	}

	@Override
	public int heightVariance()
	{
		return heightVariance;
	}

	@Override
	public int lightestWeight()
	{
		return lightestWeight;
	}

	@Override
	public int weightVariance()
	{
		return weightVariance;
	}

	@Override
	public int shortestMale()
	{
		return shortestMale;
	}

	@Override
	public boolean classless()
	{
		return (disableFlags & Race.GENFLAG_NOCLASS) == Race.GENFLAG_NOCLASS;
	}

	@Override
	public boolean leveless()
	{
		return (disableFlags & Race.GENFLAG_NOLEVELS) == Race.GENFLAG_NOLEVELS;
	}

	@Override
	public boolean expless()
	{
		return (disableFlags & Race.GENFLAG_NOEXP) == Race.GENFLAG_NOEXP;
	}

	@Override
	public boolean fertile()
	{
		return !((disableFlags & Race.GENFLAG_NOFERTILE) == Race.GENFLAG_NOFERTILE);
	}

	@Override
	protected boolean uncharmable()
	{
		return ((disableFlags & Race.GENFLAG_NOCHARM) == Race.GENFLAG_NOCHARM);
	}

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	@Override
	public int availabilityCode()
	{
		return availability;
	}

	@Override
	public int[] getAgingChart()
	{
		if(agingChart==null)
			agingChart=super.getAgingChart().clone();
		return agingChart;
	}

	@Override
	protected String[] racialEffectNames()
	{
		return racialEffectNames;
	}

	@Override
	protected int[] racialEffectLevels()
	{
		return racialEffectLevels;
	}

	@Override
	protected String[] racialEffectParms()
	{
		return racialEffectParms;
	}

	@Override
	public int[] getBreathables()
	{
		return sortedBreathables;
	}

	@Override
	public boolean useRideClass()
	{
		return isRideable;
	}

	@Override
	protected String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	protected int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	protected int[] racialAbilityProficiencies()
	{
		return racialAbilityProfs;
	}

	@Override
	protected boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	@Override
	public String[] culturalAbilityNames()
	{
		return culturalAbilityNames;
	}

	@Override
	public int[] culturalAbilityProficiencies()
	{
		return culturalAbilityProfs;
	}

	@Override
	protected int[] culturalAbilityLevels()
	{
		return culturalAbilityLvls;
	}

	@Override
	protected boolean[] culturalAbilityAutoGains()
	{
		return culturalAbilityGains;
	}

	@Override
	protected boolean destroyBodyAfterUse()
	{
		return destroyBodyAfterUse;
	}

	public GenRace()
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new GenRace();
		}
	}

	@Override
	public CMObject copyOf()
	{
		final GenRace E=new GenRace();
		E.setRacialParms(racialParms());
		return E;
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(weaponBuddy!=null)
			return weaponBuddy.myNaturalWeapon();
		if(naturalWeapon!=null)
			return naturalWeapon;
		return funHumanoidWeapon();
	}

	@Override
	public String arriveStr()
	{
		return arriveStr;
	}

	@Override
	public String leaveStr()
	{
		return leaveStr;
	}

	@Override
	public Race makeGenRace()
	{
		return this;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		if((healthBuddy!=null)&&(healthBuddy!=this))
			return healthBuddy.healthText(viewer,mob);
		return CMLib.combat().standardMobCondition(viewer,mob);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(adjPStats!=null)
		{
			affectableStats.setAbility(affectableStats.ability()+adjPStats.ability());
			affectableStats.setArmor(affectableStats.armor()+adjPStats.armor());
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+adjPStats.attackAdjustment());
			affectableStats.setDamage(affectableStats.damage()+adjPStats.damage());
			affectableStats.setDisposition(affectableStats.disposition()|adjPStats.disposition());
			affectableStats.setHeight(affectableStats.height()+adjPStats.height());
			affectableStats.setLevel(affectableStats.level()+adjPStats.level());
			affectableStats.setSensesMask(affectableStats.sensesMask()|adjPStats.sensesMask());
			affectableStats.setSpeed(affectableStats.speed()+adjPStats.speed());
			affectableStats.setWeight(affectableStats.weight()+adjPStats.weight());
		}
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(adjStats!=null)
		{
			for(final int i: CharStats.CODES.ALLCODES())
				affectableStats.setStat(i,affectableStats.getStat(i)+adjStats.getStat(i));
		}
		if(setStats!=null)
		{
			for(final int i: CharStats.CODES.ALLCODES())
			{
				if(setStats.getStat(i)!=0)
					affectableStats.setRacialStat(i,setStats.getStat(i));
			}
		}
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		if(adjState!=null)
		{
			affectableMaxState.setFatigue(affectableMaxState.getFatigue()+adjState.getFatigue());
			affectableMaxState.setHitPoints(affectableMaxState.getHitPoints()+adjState.getHitPoints());
			affectableMaxState.setHunger(affectableMaxState.getHunger()+adjState.getHunger());
			affectableMaxState.setMana(affectableMaxState.getMana()+adjState.getMana());
			affectableMaxState.setMovement(affectableMaxState.getMovement()+adjState.getMovement());
			affectableMaxState.setThirst(affectableMaxState.getThirst()+adjState.getThirst());
		}
	}

	@Override
	public List<RawMaterial> myResources()
	{
		if(resourceChoices==null)
			return new Vector<RawMaterial>();
		return resourceChoices;
	}

	protected String getRaceLocatorID(Race R)
	{
		if(R==null)
			return "";
		if(R.isGeneric())
			return R.ID();
		if(R==CMClass.getRace(R.ID()))
			return R.ID();
		return R.getClass().getName();
	}

	@Override
	public String racialParms()
	{
		final StringBuffer str=new StringBuffer("");
		str.append("<RACE><ID>"+ID()+"</ID>");
		str.append(CMLib.xml().convertXMLtoTag("NAME",name()));
		str.append(CMLib.xml().convertXMLtoTag("CAT",racialCategory()));
		str.append(CMLib.xml().convertXMLtoTag("MHEIGHT",""+shortestMale()));
		str.append(CMLib.xml().convertXMLtoTag("FHEIGHT",""+shortestFemale()));
		str.append(CMLib.xml().convertXMLtoTag("VHEIGHT",""+heightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("BWEIGHT",""+lightestWeight()));
		str.append(CMLib.xml().convertXMLtoTag("VWEIGHT",""+weightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("WEAR",""+forbiddenWornBits()));
		str.append(CMLib.xml().convertXMLtoTag("AVAIL",""+availability));
		str.append(CMLib.xml().convertXMLtoTag("RIDE", ""+useRideClass()));
		str.append(CMLib.xml().convertXMLtoTag("DESTROYBODY",""+destroyBodyAfterUse()));
		final StringBuffer bbody=new StringBuffer("");
		for(int i=0;i<bodyMask().length;i++)
			bbody.append((""+bodyMask()[i])+";");
		str.append(CMLib.xml().convertXMLtoTag("BODY",bbody.toString()));
		str.append(CMLib.xml().convertXMLtoTag("XPADJ",""+getXPAdjustment()));
		str.append(CMLib.xml().convertXMLtoTag("HEALTHRACE",getRaceLocatorID(healthBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("EVENTRACE",getRaceLocatorID(eventBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("WEAPONRACE",getRaceLocatorID(weaponBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("ARRIVE",arriveStr()));
		str.append(CMLib.xml().convertXMLtoTag("LEAVE",leaveStr()));
		str.append(CMLib.xml().convertXMLtoTag("HELP",CMLib.xml().parseOutAngleBrackets(helpEntry)));
		str.append(CMLib.xml().convertXMLtoTag("AGING",CMParms.toListString(getAgingChart())));
		if(adjPStats==null)
			str.append("<ESTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(adjPStats)));
		if(adjStats==null)
			str.append("<ASTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ASTATS",CMLib.coffeeMaker().getCharStatsStr(adjStats)));
		if(setStats==null)
			str.append("<CSTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("CSTATS",CMLib.coffeeMaker().getCharStatsStr(setStats)));
		if(adjState==null)
			str.append("<ASTATE/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ASTATE",CMLib.coffeeMaker().getCharStateStr(adjState)));
		if(startAdjState==null)
			str.append("<STARTASTATE/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(startAdjState)));
		str.append(CMLib.xml().convertXMLtoTag("DISFLAGS",""+disableFlags));

		if(myResources().size()==0)
			str.append("<RESOURCES/>");
		else
		{
			str.append("<RESOURCES>");
			for(final RawMaterial I : myResources())
			{
				str.append("<RSCITEM>");
				str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</RSCITEM>");
			}
			str.append("</RESOURCES>");
		}
		if((outfit(null)==null)||(outfit(null).size()==0))
			str.append("<OUTFIT/>");
		else
		{
			str.append("<OUTFIT>");
			for(final Item I : outfit(null))
			{
				str.append("<OFTITEM>");
				str.append(CMLib.xml().convertXMLtoTag("OFCLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("OFDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</OFTITEM>");
			}
			str.append("</OUTFIT>");
		}
		if(naturalWeapon==null)
			str.append("<WEAPON/>");
		else
		{
			str.append("<WEAPON>");
			str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(naturalWeapon)));
			str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(naturalWeapon.text())));
			str.append("</WEAPON>");
		}
		if((racialAbilityNames==null)||(racialAbilityNames.length==0))
			str.append("<RABILITIES/>");
		else
		{
			str.append("<RABILITIES>");
			for(int r=0;r<racialAbilityNames.length;r++)
			{
				str.append("<RABILITY>");
				str.append("<RCLASS>"+racialAbilityNames[r]+"</RCLASS>");
				str.append("<RLEVEL>"+racialAbilityLevels[r]+"</RLEVEL>");
				str.append("<RPROFF>"+racialAbilityProfs[r]+"</RPROFF>");
				str.append("<RAGAIN>"+racialAbilityQuals[r]+"</RAGAIN>");
				str.append("<RPARM>"+CMLib.xml().parseOutAngleBrackets(racialAbilityParms[r])+"</RPARM>");
				str.append("</RABILITY>");
			}
			str.append("</RABILITIES>");
		}

		str.append("<BREATHELIST>").append(CMParms.toListString(sortedBreathables)).append("</BREATHELIST>");
		if((racialEffectNames==null)||(racialEffectNames.length==0))
			str.append("<REFFECTS/>");
		else
		{
			str.append("<REFFECTS>");
			for(int r=0;r<racialEffectNames.length;r++)
			{
				str.append("<REFFECT>");
				str.append("<RFCLASS>"+racialEffectNames[r]+"</RFCLASS>");
				str.append("<RFLEVEL>"+racialEffectLevels[r]+"</RFLEVEL>");
				str.append("<RFPARM>"+racialEffectParms[r]+"</RFPARM>");
				str.append("</REFFECT>");
			}
			str.append("</REFFECTS>");
		}

		if((culturalAbilityNames==null)||(culturalAbilityNames.length==0))
			str.append("<CABILITIES/>");
		else
		{
			str.append("<CABILITIES>");
			for(int r=0;r<culturalAbilityNames.length;r++)
			{
				str.append("<CABILITY>");
				str.append("<CCLASS>"+culturalAbilityNames[r]+"</CCLASS>");
				str.append("<CPROFF>"+culturalAbilityProfs[r]+"</CPROFF>");
				str.append("<CPLEVL>"+culturalAbilityLvls[r]+"</CPLEVL>");
				str.append("<CGAIN>"+culturalAbilityGains[r]+"</CGAIN>");
				str.append("</CABILITY>");
			}
			str.append("</CABILITIES>");
		}
		if((naturalAbilImmunities==null)||(naturalAbilImmunities.size()==0))
			str.append("<IABILITIES/>");
		else
		{
			str.append("<IABILITIES>");
			for(String ableID : naturalAbilImmunities)
			{
				str.append("<IABILITY>");
				str.append("<ICLASS>"+ableID+"</ICLASS>");
				str.append("</IABILITY>");
			}
			str.append("</IABILITIES>");
		}
		
		if(xtraValues==null)
			xtraValues=CMProps.getExtraStatCodesHolder(this);
		for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
			str.append(CMLib.xml().convertXMLtoTag(getStatCodes()[i],getStat(getStatCodes()[i])));
		str.append("</RACE>");
		return str.toString();
	}

	@Override
	public void setRacialParms(String parms)
	{
		if(parms.trim().length()==0)
		{
			Log.errOut("GenRace","Unable to parse empty xml");
			return;
		}
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenRace","Unable to parse xml: "+parms);
			return;
		}
		final List<XMLLibrary.XMLTag> raceData=CMLib.xml().getContentsFromPieces(xml,"RACE");
		if(raceData==null)
		{
			Log.errOut("GenRace","Unable to get RACE data: ("+parms.length()+"): "+CMStrings.padRight(parms,30)+".");
			return;
		}
		final String id=CMLib.xml().getValFromPieces(raceData,"ID");
		if(id.length()==0)
		{
			Log.errOut("GenRace","Unable to parse: "+parms);
			return;
		}
		ID=id;
		name=CMLib.xml().getValFromPieces(raceData,"NAME");
		if((name==null)||(name.length()==0))
		{
			Log.errOut("GenRace","Not able to parse: "+parms);
			return;
		}

		String rcat=CMLib.xml().getValFromPieces(raceData,"CAT");
		if((rcat==null)||(rcat.length()==0))
		{
			rcat=name;
			return;
		}

		racialCategory=rcat;
		forbiddenWornBits=CMLib.xml().getLongFromPieces(raceData,"WEAR");
		weightVariance=CMLib.xml().getIntFromPieces(raceData,"VWEIGHT");
		lightestWeight=CMLib.xml().getIntFromPieces(raceData,"BWEIGHT");
		heightVariance=CMLib.xml().getIntFromPieces(raceData,"VHEIGHT");
		shortestFemale=CMLib.xml().getIntFromPieces(raceData,"FHEIGHT");
		shortestMale=CMLib.xml().getIntFromPieces(raceData,"MHEIGHT");
		isRideable=CMLib.xml().getBoolFromPieces(raceData,"RIDE");
		
		helpEntry=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(raceData,"HELP"));
		final String playerval=CMLib.xml().getValFromPieces(raceData,"PLAYER").trim().toUpperCase();
		if(playerval.length()>0)
		{
			if(playerval.startsWith("T"))
				availability=Area.THEME_FANTASY;
			else
			if(playerval.startsWith("F"))
				availability=0;
			else
			switch(CMath.s_int(playerval))
			{
			case 0:
				availability = Area.THEME_FANTASY;
				break;
			case 1:
				availability = Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
				break;
			case 2:
				availability = 0;
				break;
			}
		}
		final String avail=CMLib.xml().getValFromPieces(raceData,"AVAIL").trim().toUpperCase();
		if((avail!=null)&&(avail.length()>0)&&(CMath.isNumber(avail)))
			availability=CMath.s_int(avail);
		destroyBodyAfterUse=CMLib.xml().getBoolFromPieces(raceData,"DESTROYBODY");
		leaveStr=CMLib.xml().getValFromPieces(raceData,"LEAVE");
		arriveStr=CMLib.xml().getValFromPieces(raceData,"ARRIVE");
		setStat("HEALTHRACE",CMLib.xml().getValFromPieces(raceData,"HEALTHRACE"));
		setStat("EVENTRACE",CMLib.xml().getValFromPieces(raceData,"EVENTRACE"));
		setStat("WEAPONRACE",CMLib.xml().getValFromPieces(raceData,"WEAPONRACE"));
		xpAdjustmentPct=CMLib.xml().getIntFromPieces(raceData, "XPADJ");
		final String body=CMLib.xml().getValFromPieces(raceData,"BODY");
		final List<String> V=CMParms.parseSemicolons(body,false);
		for(int v=0;v<V.size();v++)
		{
			if(v<bodyMask().length)
				bodyMask()[v]=CMath.s_int(V.get(v));
		}
		adjPStats=null;
		final String eStats=CMLib.xml().getValFromPieces(raceData,"ESTATS");
		if(eStats.length()>0)
		{
			adjPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
			adjPStats.setAllValues(0);
			CMLib.coffeeMaker().setPhyStats(adjPStats,eStats);
		}
		adjStats=null;
		final String aStats=CMLib.xml().getValFromPieces(raceData,"ASTATS");
		if(aStats.length()>0)
		{
			adjStats=(CharStats)CMClass.getCommon("DefaultCharStats");
			adjStats.setAllValues(0);
			CMLib.coffeeMaker().setCharStats(adjStats,aStats);
		}
		setStats=null;
		final String cStats=CMLib.xml().getValFromPieces(raceData,"CSTATS");
		if(cStats.length()>0)
		{
			setStats=(CharStats)CMClass.getCommon("DefaultCharStats");
			setStats.setAllValues(0);
			CMLib.coffeeMaker().setCharStats(setStats,cStats);
		}
		adjState=null;
		final String aState=CMLib.xml().getValFromPieces(raceData,"ASTATE");
		if(aState.length()>0)
		{
			adjState=(CharState)CMClass.getCommon("DefaultCharState");
			adjState.setAllValues(0);
			CMLib.coffeeMaker().setCharState(adjState,aState);
		}
		startAdjState=null;
		disableFlags=CMLib.xml().getIntFromPieces(raceData,"DISFLAGS");
		final String saState=CMLib.xml().getValFromPieces(raceData,"STARTASTATE");
		if(saState.length()>0)
		{
			startAdjState=(CharState)CMClass.getCommon("DefaultCharState");
			startAdjState.setAllValues(0);
			CMLib.coffeeMaker().setCharState(startAdjState,saState);
		}
		final String aging=CMLib.xml().getValFromPieces(raceData,"AGING");
		final List<String> aV=CMParms.parseCommas(aging,true);
		for(int v=0;v<aV.size();v++)
			getAgingChart()[v]=CMath.s_int(aV.get(v));
		clrStatChgDesc();
		// now RESOURCES!
		List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(raceData,"RESOURCES");
		resourceChoices=null;
		if((xV!=null)&&(xV.size()>0))
		{
			resourceChoices=new Vector<RawMaterial>();
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("RSCITEM"))||(iblk.contents()==null))
					continue;
				final Item I=CMClass.getItem(iblk.getValFromPieces("ICLASS"));
				if(I instanceof RawMaterial)
				{
					final RawMaterial newOne=(RawMaterial)I;
					final String idat=iblk.getValFromPieces("IDATA");
					newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
					newOne.recoverPhyStats();
					resourceChoices.add(newOne);
				}
			}
		}

		// now OUTFIT!
		final List<XMLLibrary.XMLTag> oV=CMLib.xml().getContentsFromPieces(raceData,"OUTFIT");
		outfitChoices=null;
		if((oV!=null)&&(oV.size()>0))
		{
			outfitChoices=new Vector<Item>();
			for(int x=0;x<oV.size();x++)
			{
				final XMLTag iblk=oV.get(x);
				if((!iblk.tag().equalsIgnoreCase("OFTITEM"))||(iblk.contents()==null))
					continue;
				final Item newOne=CMClass.getItem(iblk.getValFromPieces("OFCLASS"));
				if(newOne != null)
				{
					final String idat=iblk.getValFromPieces("OFDATA");
					newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
					newOne.recoverPhyStats();
					outfitChoices.add(newOne);
				}
				else
					Log.errOut("GenRace","Unknown newOne race: " + iblk.getValFromPieces("OFCLASS"));
			}
		}

		naturalWeapon=null;
		final List<XMLLibrary.XMLTag> wblk=CMLib.xml().getContentsFromPieces(raceData,"WEAPON");
		if(wblk!=null)
		{
			naturalWeapon=CMClass.getWeapon(CMLib.xml().getValFromPieces(wblk,"ICLASS"));
			final String idat=CMLib.xml().getValFromPieces(wblk,"IDATA");
			if((idat!=null)&&(naturalWeapon!=null))
			{
				naturalWeapon.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				naturalWeapon.recoverPhyStats();
			}
		}
		xV=CMLib.xml().getContentsFromPieces(raceData,"RABILITIES");
		racialAbilityNames=null;
		racialAbilityProfs=null;
		racialAbilityQuals=null;
		racialAbilityLevels=null;
		racialAbilityParms=null;
		if((xV!=null)&&(xV.size()>0))
		{
			racialAbilityNames=new String[xV.size()];
			racialAbilityProfs=new int[xV.size()];
			racialAbilityQuals=new boolean[xV.size()];
			racialAbilityLevels=new int[xV.size()];
			racialAbilityParms=new String[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("RABILITY"))||(iblk.contents()==null))
					continue;
				racialAbilityNames[x]=iblk.getValFromPieces("RCLASS");
				racialAbilityProfs[x]=iblk.getIntFromPieces("RPROFF");
				racialAbilityQuals[x]=iblk.getBoolFromPieces("RAGAIN");
				racialAbilityLevels[x]=iblk.getIntFromPieces("RLEVEL");
				racialAbilityParms[x]=CMLib.xml().restoreAngleBrackets(iblk.getValFromPieces("RPARM"));
			}
		}

		sortedBreathables=CMParms.toIntArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(raceData, "BREATHELIST",""+RawMaterial.RESOURCE_AIR),true));
		Arrays.sort(sortedBreathables);

		xV=CMLib.xml().getContentsFromPieces(raceData,"REFFECTS");
		racialEffectNames=null;
		racialEffectParms=null;
		racialEffectLevels=null;
		if((xV!=null)&&(xV.size()>0))
		{
			racialEffectNames=new String[xV.size()];
			racialEffectParms=new String[xV.size()];
			racialEffectLevels=new int[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("REFFECT"))||(iblk.contents()==null))
					continue;
				racialEffectNames[x]=iblk.getValFromPieces("RFCLASS");
				racialEffectParms[x]=iblk.getValFromPieces("RFPARM");
				racialEffectLevels[x]=iblk.getIntFromPieces("RFLEVEL");
			}
		}

		xV=CMLib.xml().getContentsFromPieces(raceData,"CABILITIES");
		culturalAbilityNames=null;
		culturalAbilityProfs=null;
		culturalAbilityLvls=null;
		culturalAbilityGains=null;
		if((xV!=null)&&(xV.size()>0))
		{
			culturalAbilityNames=new String[xV.size()];
			culturalAbilityProfs=new int[xV.size()];
			culturalAbilityLvls=new int[xV.size()];
			culturalAbilityGains=new boolean[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("CABILITY"))||(iblk.contents()==null))
					continue;
				culturalAbilityNames[x]=iblk.getValFromPieces("CCLASS");
				culturalAbilityProfs[x]=iblk.getIntFromPieces("CPROFF");
				culturalAbilityLvls[x]=iblk.getIntFromPieces("CPLEVL");
				if(CMLib.xml().isTagInPieces(iblk.contents(), "CGAIN"))
					culturalAbilityGains[x]=iblk.getBoolFromPieces("CGAIN");
				else
					culturalAbilityGains[x]=true;
			}
		}

		xV=CMLib.xml().getContentsFromPieces(raceData,"IABILITIES");
		this.naturalAbilImmunities.clear();
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("IABILITY"))||(iblk.contents()==null))
					continue;
				this.naturalAbilImmunities.add(iblk.getValFromPieces("ICLASS"));
			}
		}

		xtraValues=CMProps.getExtraStatCodesHolder(this);
		for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
			setStat(getStatCodes()[i],CMLib.xml().getValFromPieces(raceData, getStatCodes()[i]));
	}

	protected static String[] CODES={"ID","NAME","CAT","WEAR","VWEIGHT","BWEIGHT",
									 "VHEIGHT","FHEIGHT","MHEIGHT","AVAIL","LEAVE",
									 "ARRIVE","HEALTHRACE","BODY","ESTATS",
									 "ASTATS","CSTATS","ASTATE",
									 "NUMRSC","GETRSCID","GETRSCPARM",
									 "WEAPONCLASS","WEAPONXML",
									 "NUMRABLE","GETRABLE","GETRABLEPROF","GETRABLEQUAL","GETRABLELVL","GETRABLEPARM",
									 "NUMCABLE","GETCABLE","GETCABLEPROF","GETCABLELVL","GETCABLEGAIN",
									 "NUMOFT","GETOFTID","GETOFTPARM","BODYKILL",
									 "NUMREFF","GETREFF","GETREFFPARM","GETREFFLVL","AGING",
									 "DISFLAGS","STARTASTATE","EVENTRACE","WEAPONRACE", "HELP",
									 "BREATHES","CANRIDE",
									 "NUMIABLE","GETIABLE",
									 "XPADJ"
									 };

	@Override
	public String getStat(String code)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return name();
		case 2:
			return racialCategory;
		case 3:
			return "" + forbiddenWornBits();
		case 4:
			return "" + weightVariance();
		case 5:
			return "" + lightestWeight();
		case 6:
			return "" + heightVariance();
		case 7:
			return "" + shortestFemale();
		case 8:
			return "" + shortestMale();
		case 9:
			return "" + availabilityCode();
		case 10:
			return leaveStr();
		case 11:
			return arriveStr();
		case 12:
			return getRaceLocatorID(healthBuddy);
		case 13:
		{
			final StringBuffer bbody = new StringBuffer("");
			for (int i = 0; i < bodyMask().length; i++)
				bbody.append(("" + bodyMask()[i]) + ";");
			return bbody.toString();
		}
		case 14:
			return (adjPStats == null) ? "" : CMLib.coffeeMaker().getPhyStatsStr(adjPStats);
		case 15:
			return (adjStats == null) ? "" : CMLib.coffeeMaker().getCharStatsStr(adjStats);
		case 16:
			return (setStats == null) ? "" : CMLib.coffeeMaker().getCharStatsStr(setStats);
		case 17:
			return (adjState == null) ? "" : CMLib.coffeeMaker().getCharStateStr(adjState);
		case 18:
			return "" + myResources().size();
		case 19:
			return "" + ((Item) myResources().get(num)).ID();
		case 20:
			return "" + ((Item) myResources().get(num)).text();
		case 21:
			return (naturalWeapon == null) ? "" : naturalWeapon.ID();
		case 22:
			return (naturalWeapon == null) ? "" : naturalWeapon.text();
		case 23:
			return (racialAbilityNames == null) ? "0" : ("" + racialAbilityNames.length);
		case 24:
			return (racialAbilityNames == null) ? "" : ("" + racialAbilityNames[num]);
		case 25:
			return (racialAbilityProfs == null) ? "0" : ("" + racialAbilityProfs[num]);
		case 26:
			return (racialAbilityQuals == null) ? "false" : ("" + racialAbilityQuals[num]);
		case 27:
			return (racialAbilityLevels == null) ? "0" : ("" + racialAbilityLevels[num]);
		case 28:
			return (racialAbilityParms == null) ? "" : ("" + racialAbilityParms[num]);
		case 29:
			return (culturalAbilityNames == null) ? "0" : ("" + culturalAbilityNames.length);
		case 30:
			return (culturalAbilityNames == null) ? "" : ("" + culturalAbilityNames[num]);
		case 31:
			return (culturalAbilityProfs == null) ? "0" : ("" + culturalAbilityProfs[num]);
		case 32:
			return (culturalAbilityLvls == null) ? "0" : ("" + culturalAbilityLvls[num]);
		case 33:
			return (culturalAbilityGains == null) ? "0" : ("" + culturalAbilityGains[num]);
		case 34:
			return "" + ((outfit(null) != null) ? outfit(null).size() : 0);
		case 35:
			return "" + ((outfit(null) != null) ? outfit(null).get(num).ID() : "");
		case 36:
			return "" + ((outfit(null) != null) ? outfit(null).get(num).text() : "");
		case 37:
			return "" + destroyBodyAfterUse();
		case 38:
			return (racialEffectNames == null) ? "0" : ("" + racialEffectNames.length);
		case 39:
			return (racialEffectNames == null) ? "" : ("" + racialEffectNames[num]);
		case 40:
			return (racialEffectParms == null) ? "0" : ("" + racialEffectParms[num]);
		case 41:
			return (racialEffectLevels == null) ? "0" : ("" + racialEffectLevels[num]);
		case 42:
			return CMParms.toListString(getAgingChart());
		case 43:
			return "" + disableFlags;
		case 44:
			return (startAdjState == null) ? "" : CMLib.coffeeMaker().getCharStateStr(startAdjState);
		case 45:
			return getRaceLocatorID(eventBuddy);
		case 46:
			return getRaceLocatorID(weaponBuddy);
		case 47:
			return helpEntry;
		case 48:
			return CMParms.toListString(sortedBreathables);
		case 49:
			return "" + isRideable;
		case 50:
			return (this.naturalAbilImmunities==null)?"0":("" + this.naturalAbilImmunities.size());
		case 51:
			return ((this.naturalAbilImmunities==null)||(num>=this.naturalAbilImmunities.size()))?"":this.abilityImmunities()[num];
		case 52:
			return ""+getXPAdjustment();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public boolean tick(Tickable myChar, int tickID)
	{
		if(eventBuddy!=null)
		{
			if(!eventBuddy.tick(myChar,tickID))
				return false;
		}
		return super.tick(myChar, tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(eventBuddy!=null)
			eventBuddy.executeMsg(myHost, msg);
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((eventBuddy!=null)
		&&(!eventBuddy.okMessage(myHost, msg)))
			return false;
		return super.okMessage(myHost, msg);

	}

	@Override
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
		if((!verifyOnly)&&(startAdjState!=null))
		{
			mob.baseState().setFatigue(mob.baseState().getFatigue()+startAdjState.getFatigue());
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+startAdjState.getHitPoints());
			mob.baseState().setHunger(mob.baseState().getHunger()+startAdjState.getHunger());
			mob.baseState().setMana(mob.baseState().getMana()+startAdjState.getMana());
			mob.baseState().setMovement(mob.baseState().getMovement()+startAdjState.getMovement());
			mob.baseState().setThirst(mob.baseState().getThirst()+startAdjState.getThirst());
		}
	}

	private void setBaseStat(String val, Modifiable C)
	{
		for(String stat : C.getStatCodes())
		{
			String statVal=CMParms.getParmStr(val, stat, "");
			if((statVal!=null)&&(statVal.length()>0))
				C.setStat(stat, statVal);
		}
	}
	
	@Override
	public void setStat(String code, String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			ID = val;
			break;
		case 1:
			name = val;
			break;
		case 2:
			racialCategory = val;
			break;
		case 3:
		{
			if(CMath.isLong(val))
				forbiddenWornBits=CMath.s_long(val);
			else
			if(val.indexOf('=')>0)
			{
				forbiddenWornBits=0;
				for(int i=1;i<Wearable.DEFAULT_WORN_DESCS.length;i++)
				{
					String s=CMParms.getParmStr(val, Wearable.DEFAULT_WORN_DESCS[i].toUpperCase().replace(' ','_'), "");
					if((s!=null)&&(s.length()>0)&&(CMath.isBool(s)))
						if(CMath.s_bool(s))
							forbiddenWornBits=CMath.setb(forbiddenWornBits, 2^(i-1));
				}
			}
			break;
		}
		case 4:
			weightVariance = CMath.s_parseIntExpression(val);
			break;
		case 5:
			lightestWeight = CMath.s_parseIntExpression(val);
			break;
		case 6:
			heightVariance = CMath.s_parseIntExpression(val);
			break;
		case 7:
			shortestFemale = CMath.s_parseIntExpression(val);
			break;
		case 8:
			shortestMale = CMath.s_parseIntExpression(val);
			break;
		case 9:
			availability = CMath.s_parseBitIntExpression(Area.THEME_BIT_NAMES, val);
			break;
		case 10:
			leaveStr = val;
			break;
		case 11:
			arriveStr = val;
			break;
		case 12:
		{
			healthBuddy=CMClass.getRace(val);
			try
			{
				if(healthBuddy==null)
					healthBuddy=(Race)CMClass.getLoadNewClassInstance(CMObjectType.RACE,val,true);
			}
			catch(final Exception e)
			{
			}
			break;
		}
		case 13:
		{
			final List<String> V=CMParms.parseSemicolons(val,false);
			if(V.size()>1)
			{
				for(int v=0;v<V.size();v++)
					if(v<bodyMask().length)
						bodyMask()[v]=CMath.s_int(V.get(v));
			}
			else
			if(val.indexOf('=')>0)
			{
				for(int b=0;b<BODYPARTSTR.length;b++)
				{
					final int numPart=CMParms.getParmInt(val, BODYPARTSTR[b], Integer.MIN_VALUE);
					if(numPart!=Integer.MIN_VALUE)
						bodyMask()[numPart]=numPart;
				}
			}
			break;
		}
		case 14:
		{
			adjPStats=null;
			clrStatChgDesc();
			if(val.length()>0)
			{
				adjPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats"); 
				adjPStats.setAllValues(0);
				if((val.indexOf('=')>0)&&(val.indexOf('|')<0))
					setBaseStat(val, adjPStats);
				else
					CMLib.coffeeMaker().setPhyStats(adjPStats,val);
			}
			break;
		}
		case 15:
		{
			adjStats=null;
			clrStatChgDesc();
			if(val.length()>0)
			{
				adjStats=(CharStats)CMClass.getCommon("DefaultCharStats");
				adjStats.setAllValues(0);
				if((val.indexOf('=')>0)&&(val.indexOf('|')<0))
					setBaseStat(val, adjStats);
				else
					CMLib.coffeeMaker().setCharStats(adjStats,val);
			}
			break;
		}
		case 16:
		{
			setStats=null;
			clrStatChgDesc();
			if(val.length()>0)
			{
				setStats=(CharStats)CMClass.getCommon("DefaultCharStats");
				setStats.setAllValues(0);
				if((val.indexOf('=')>0)&&(val.indexOf('|')<0))
					setBaseStat(val, setStats);
				else
					CMLib.coffeeMaker().setCharStats(setStats,val);
			}
			break;
		}
		case 17:
		{
			adjState=null;
			clrStatChgDesc();
			if(val.length()>0)
			{
				adjState=(CharState)CMClass.getCommon("DefaultCharState");
				adjState.setAllValues(0);
				if((val.indexOf('=')>0)&&(val.indexOf('|')<0))
					setBaseStat(val, adjState);
				else
					CMLib.coffeeMaker().setCharState(adjState,val);
			}
			break;
		}
		case 18:
		{
			if(CMath.s_int(val)==0) 
				resourceChoices=null; 
			else 
				resourceChoices=new Vector<RawMaterial>(CMath.s_int(val)); 
			break;
		}
		case 19: 
		{
			if(resourceChoices==null) 
				resourceChoices=new Vector<RawMaterial>();
			final Item I=CMClass.getItem(val);
			if(I instanceof RawMaterial)
			{
				if(num>=resourceChoices.size())
					resourceChoices.add((RawMaterial)I);
				else
					resourceChoices.set(num,(RawMaterial)I);
			}
			break;
		}
		case 20:
		{
			if((resourceChoices!=null)&&(num<resourceChoices.size()))
			{
				final Item I=resourceChoices.get(num);
				I.setMiscText(val);
				I.recoverPhyStats();
			}
			break;
		}
		case 21:
		{
			naturalWeapon=null;
			if(val.length()>0) 
				naturalWeapon=CMClass.getWeapon(val);
			break;
		}
		case 22:
		{
			if(naturalWeapon!=null)
			{
				naturalWeapon.setMiscText(val);
				naturalWeapon.recoverPhyStats();
			}
			break;
		}
		case 23:
		{
			racialAbilityMap=null;
			if(CMath.s_int(val)==0)
			{
				racialAbilityNames=null;
				racialAbilityProfs=null;
				racialAbilityQuals=null;
				racialAbilityLevels=null;
				racialAbilityParms=null;
			}
			else
			{
				racialAbilityNames=new String[CMath.s_int(val)];
				racialAbilityProfs=new int[CMath.s_int(val)];
				racialAbilityQuals=new boolean[CMath.s_int(val)];
				racialAbilityLevels=new int[CMath.s_int(val)];
				racialAbilityParms=new String[CMath.s_int(val)];
			}
			break;
		}
		case 24:
		{
			if(racialAbilityNames==null) 
				racialAbilityNames=new String[num+1];
			racialAbilityNames[num]=val;
			break;
		}
		case 25: 
		{
			if(racialAbilityProfs==null)
				racialAbilityProfs=new int[num+1];
			racialAbilityProfs[num]=CMath.s_parseIntExpression(val);
			break;
		}
		case 26:
		{
			if(racialAbilityQuals==null) 
				racialAbilityQuals=new boolean[num+1];
			racialAbilityQuals[num]=CMath.s_bool(val);
			break;
		}
		case 27:
		{
			if(racialAbilityLevels==null)
				racialAbilityLevels=new int[num+1];
			racialAbilityLevels[num]=CMath.s_parseIntExpression(val);
			break;
		}
		case 28:
		{
			if(racialAbilityParms==null)
				racialAbilityParms=new String[num+1];
			racialAbilityParms[num]=val;
			break;
		}
		case 29:
		{
			if(CMath.s_int(val)==0)
			{
				culturalAbilityNames=null;
				culturalAbilityProfs=null;
				culturalAbilityLvls=null;
				culturalAbilityGains=null;
			}
			else
			{
				culturalAbilityNames=new String[CMath.s_int(val)];
				culturalAbilityProfs=new int[CMath.s_int(val)];
				culturalAbilityLvls=new int[CMath.s_int(val)];
				culturalAbilityGains=new boolean[CMath.s_int(val)];
			}
			this.mappedCulturalAbilities=false;
			break;
		}
		case 30:
		{
			if(culturalAbilityNames==null)
				culturalAbilityNames=new String[num+1];
			culturalAbilityNames[num]=val;
			this.mappedCulturalAbilities=false;
			break;
		}
		case 31:
		{
			if(culturalAbilityProfs==null)
				culturalAbilityProfs=new int[num+1];
			culturalAbilityProfs[num]=CMath.s_int(val);
			this.mappedCulturalAbilities=false;
			break;
		}
		case 32:
		{
			if(culturalAbilityLvls==null)
				culturalAbilityLvls=new int[num+1];
			culturalAbilityLvls[num]=CMath.s_int(val);
			this.mappedCulturalAbilities=false;
			break;
		}
		case 33:
		{
			if(culturalAbilityGains==null)
				culturalAbilityGains=new boolean[num+1];
			culturalAbilityGains[num]=CMath.s_bool(val);
			this.mappedCulturalAbilities=false;
			break;
		}
		case 34:
		{
			if(CMath.s_int(val)==0) 
				outfitChoices=null; 
			else 
				outfitChoices=new Vector<Item>(CMath.s_int(val)); 
			break;
		}
		case 35:
		{
			if(outfitChoices==null)
				outfitChoices=new Vector<Item>();
			if(num>=outfitChoices.size())
				outfitChoices.add(CMClass.getItem(val));
			else
				outfitChoices.set(num,CMClass.getItem(val));
			break;
		}
		case 36:
		{
			if((outfitChoices!=null)&&(num<outfitChoices.size()))
			{
				final Item I=outfitChoices.get(num);
				I.setMiscText(val);
				I.recoverPhyStats();
			}
			break;
		}
		case 37:
			destroyBodyAfterUse = CMath.s_bool(val);
			break;
		case 38:
		{
			racialEffectMap=null;
			if(CMath.s_int(val)==0)
			{
				racialEffectNames=null;
				racialEffectParms=null;
				racialEffectLevels=null;
			}
			else
			{
				racialEffectNames=new String[CMath.s_int(val)];
				racialEffectParms=new String[CMath.s_int(val)];
				racialEffectLevels=new int[CMath.s_int(val)];
			}
			break;
		}
		case 39:
		{
			if(racialEffectNames==null)
				racialEffectNames=new String[num+1];
			racialEffectNames[num]=val;
			break;
		}
		case 40:
		{
			if(racialEffectParms==null)
				racialEffectParms=new String[num+1];
			racialEffectParms[num]=val;
			break;
		}
		case 41:
		{
			if(racialEffectLevels==null) 
				racialEffectLevels=new int[num+1];
			racialEffectLevels[num]=CMath.s_int(val);
			break;
		}
		case 42:
		{
			final List<String> aV=CMParms.parseCommas(val,true);
			for(int v=0;v<aV.size();v++)
			{
				final int x=CMath.s_int(aV.get(v));
				if(x<0)
					getAgingChart()[v]=Integer.MAX_VALUE;
				else
					getAgingChart()[v]=x;
			}
			break;
		}
		case 43:
			disableFlags = CMath.s_int(val);
			break;
		case 44:
		{
			startAdjState=null;
			clrStatChgDesc();
			if(val.length()>0)
			{
				startAdjState=(CharState)CMClass.getCommon("DefaultCharState");
				startAdjState.setAllValues(0);
				if((val.indexOf('=')>0)&&(val.indexOf('|')<0))
					setBaseStat(val, startAdjState);
				else
					CMLib.coffeeMaker().setCharState(startAdjState,val);
			}
			break;
		}
		case 45:
		{
			eventBuddy=CMClass.getRace(val);
			if(eventBuddy==null)
				eventBuddy=(Race)CMClass.getLoadNewClassInstance(CMObjectType.RACE,val,true);
			break;
		}
		case 46:
		{
			weaponBuddy=CMClass.getRace(val);
			if(weaponBuddy==null)
				weaponBuddy=(Race)CMClass.getLoadNewClassInstance(CMObjectType.RACE,val,true);
			break;
		}
		case 47:
		{
			helpEntry=val;
			break;
		}
		case 48:
		{
			sortedBreathables=CMParms.toIntArray(CMParms.parseCommas(val,true));
			Arrays.sort(sortedBreathables);
			break;
		}
		case 49:
		{
			isRideable=CMath.s_bool(val);
			break;
		}
		case 50:
		{
			naturalAbilImmunities.clear();
			break;
		}
		case 51:
		{
			naturalAbilImmunities.add(val);
			break;
		}
		case 52:
		{
			xpAdjustmentPct = CMath.s_int(val);
			break;
		}
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		codes=CMProps.getStatCodesList(CODES,this);
		return codes;
	}

	@Override
	protected int getCodeNum(String code)
	{
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
			code=code.substring(0,code.length()-1);
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(Race E)
	{
		if(!(E instanceof GenRace))
			return false;
		if(((GenRace)E).racialParms().equals(racialParms()))
			return true;
		return false;
	}
}
