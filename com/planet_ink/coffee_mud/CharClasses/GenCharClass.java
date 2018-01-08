package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMSecurity.SecGroup;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass.SubClassRule;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultTimeClock;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class GenCharClass extends StdCharClass
{
	protected String		ID						= "GenCharClass";
	protected Integer[]		nameLevels				= { Integer.valueOf(0) };
	protected String		baseClass				= "Commoner";
	protected String		hitPointsFormula		= "((@x6<@x7)/3)+(1*(1?6))";
	protected String		manaFormula				= "((@x4<@x5)/6)+(1*(1?3))";
	protected String		movementFormula			= "5*((@x2<@x3)/18)";
	protected int			levelCap				= -1;
	protected int			bonusPracLevel			= 0;
	protected int			bonusAttackLevel		= 0;
	protected int			attackAttribute			= CharStats.STAT_STRENGTH;
	protected int			pracsFirstLevel			= 5;
	protected int			trainsFirstLevel		= 3;
	protected int			levelsPerBonusDamage	= 10;
	protected int			allowedArmorLevel		= CharClass.ARMOR_ANY;
	protected String		otherLimitations		= "";
	protected String		otherBonuses			= "";
	protected String		qualifications			= "";
	protected String[]		xtraValues				= null;
	protected SubClassRule	subClassRule			= SubClassRule.BASEONLY;
	protected int			selectability			= 0;
	protected int			maxNonCraftingSkills	= CMProps.getIntVar(CMProps.Int.MAXNONCRAFTINGSKILLS);
	protected int			maxCraftingSkills		= CMProps.getIntVar(CMProps.Int.MAXCRAFTINGSKILLS);
	protected int			maxCommonSkills			= CMProps.getIntVar(CMProps.Int.MAXCOMMONSKILLS);
	protected int			maxLanguages			= CMProps.getIntVar(CMProps.Int.MAXLANGUAGES);
	private Set<Integer> 	requiredWeaponMaterials = null; // set of Integer material masks
	protected int 			requiredArmorSourceMinor= -1;
	private String[] 		raceRequiredList		= new String[0];
	protected Set<Integer> 	disallowedWeaponSet		= null; // set of Integers for weapon classes
	protected CharStats 	setStats				= null;
	protected CharStats 	adjStats				= null;
	protected PhyStats 		adjPStats				= null;
	protected CharState 	adjState				= null;
	protected CharState 	startAdjState			= null;
	protected CharClass 	statBuddy				= null;
	protected CharClass 	eventBuddy				= null;
	protected int 			disableFlags			= 0;
	protected String 		startingMoney			= "";
	protected List<String>[]securityGroups			= new List[0];
	protected Integer[] 	securityGroupLevels		= {};
	protected String 		helpEntry 				= "";

	//protected Vector<Item> outfitChoices=null; from stdcharclass -- but don't forget them!
	private Pair<String,Integer>[] 			   minimumStatRequirements	= new Pair[0];
	protected Map<Integer,CMSecurity.SecGroup> securityGroupCache		= new Hashtable<Integer,CMSecurity.SecGroup>();
	
	@Override
	public String getManaFormula()
	{
		return manaFormula;
	}

	@Override
	public String getHitPointsFormula()
	{
		return hitPointsFormula;
	}

	@Override
	public SubClassRule getSubClassRule()
	{
		return subClassRule;
	}

	@Override
	public int getLevelCap()
	{
		return levelCap;
	}

	@Override
	public int maxNonCraftingSkills()
	{
		return maxNonCraftingSkills;
	}

	@Override
	public int maxCraftingSkills()
	{
		return maxCraftingSkills;
	}

	@Override
	public int maxCommonSkills()
	{
		return maxCommonSkills;
	}

	@Override
	public int maxLanguages()
	{
		return maxLanguages;
	}

	// IS *only* used by stdcharclass for weaponliminatations,
	// buildDisallowedWeaponClasses, buildRequiredWeaponMaterials
	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_ANY;
	}

	@Override
	protected Set<Integer> requiredWeaponMaterials()
	{
		return requiredWeaponMaterials;
	}

	@Override
	public int requiredArmorSourceMinor()
	{
		return requiredArmorSourceMinor;
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeaponSet;
	}

	@Override
	public boolean raceless()
	{
		return (disableFlags & CharClass.GENFLAG_NORACE) == CharClass.GENFLAG_NORACE;
	}

	@Override
	public boolean leveless()
	{
		return (disableFlags & CharClass.GENFLAG_NOLEVELS) == CharClass.GENFLAG_NOLEVELS;
	}

	@Override
	public boolean expless()
	{
		return (disableFlags & CharClass.GENFLAG_NOEXP) == CharClass.GENFLAG_NOEXP;
	}

	@Override
	public boolean showThinQualifyList()
	{
		return (disableFlags & CharClass.GENFLAG_THINQUALLIST) == CharClass.GENFLAG_THINQUALLIST;
	}

	@Override
	public String getStartingMoney()
	{
		return startingMoney;
	}

	@Override
	public int addedExpertise(final MOB host, final ExpertiseLibrary.Flag expertiseCode, final String abilityID)
	{
		return 0;
	}
	
	@Override
	public SecGroup getSecurityFlags(int classLevel)
	{
		if(securityGroups.length==0)
			return super.getSecurityFlags(classLevel);
		if(securityGroupCache.containsKey(Integer.valueOf(classLevel)))
			return securityGroupCache.get(Integer.valueOf(classLevel));
		final List<String> allFlags=new ArrayList<String>();
		for(int i=securityGroupLevels.length-1;i>=0;i--)
		{
			if((classLevel>=securityGroupLevels[i].intValue())
			&&(i<securityGroups.length))
				allFlags.addAll(securityGroups[i]);
		}
		final SecGroup g = CMSecurity.instance().createGroup("", allFlags);
		securityGroupCache.put(Integer.valueOf(classLevel),g);
		return g;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String ID()
	{
		return ID;
	}

	@Override
	public String name()
	{
		return names[0];
	}

	@Override
	public String name(int classLevel)
	{
		for(int i=nameLevels.length-1;i>=0;i--)
		{
			if((classLevel>=nameLevels[i].intValue())
			&&(i<names.length))
				return names[i];
		}
		return names[0];
	}

	@Override
	public String baseClass()
	{
		return baseClass;
	}

	@Override
	public int getBonusPracLevel()
	{
		return bonusPracLevel;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return bonusAttackLevel;
	}

	@Override
	public int getAttackAttribute()
	{
		return attackAttribute;
	}

	@Override
	public int getPracsFirstLevel()
	{
		return pracsFirstLevel;
	}

	@Override
	public int getTrainsFirstLevel()
	{
		return trainsFirstLevel;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return levelsPerBonusDamage;
	}

	@Override
	public String getMovementFormula()
	{
		return movementFormula;
	}

	@Override
	public int allowedArmorLevel()
	{
		return allowedArmorLevel;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return otherLimitations;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return otherBonuses;
	}

	@Override
	public int availabilityCode()
	{
		return selectability;
	}

	public GenCharClass()
	{
		names=new String[1];
		names[0]="genmob";
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}

	@Override
	public String getWeaponLimitDesc()
	{
		final StringBuffer str=new StringBuffer("");
		if((disallowedWeaponClasses(null)!=null)&&(disallowedWeaponClasses(null).size()>0))
		{
			str.append(L("The following weapon types may not be used: "));
			for(final Iterator i=disallowedWeaponClasses(null).iterator();i.hasNext();)
			{
				final Integer I=(Integer)i.next();
				str.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[I.intValue()])+" ");
			}
			str.append(".  ");
		}
		if((requiredWeaponMaterials()!=null)&&(requiredWeaponMaterials().size()>0))
		{
			str.append(L("Requires using weapons made of the following materials: "));
			for(final Iterator i=requiredWeaponMaterials().iterator();i.hasNext();)
			{
				final Integer I=(Integer)i.next();
				str.append(CMStrings.capitalizeAndLower(CMLib.materials().getMaterialDesc(I.intValue()))+" ");
			}
			str.append(".  ");
		}

		if(str.length()==0)
			str.append(L("No limitations."));
		return str.toString().trim();
	}

	@Override
	public void cloneFix(CharClass C)
	{
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
			return new GenCharClass();
		}
	}

	@Override
	public CMObject copyOf()
	{
		final GenCharClass E=new GenCharClass();
		E.setClassParms(classParms());
		return E;
	}

	public boolean loaded()
	{
		return true;
	}

	public void setLoaded(boolean truefalse)
	{
	}

	@Override
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!super.qualifiesForThisClass(mob,quiet))
			return false;
		if(mob != null)
		{
			if((!mob.isMonster())&&(mob.basePhyStats().level()>0))
			{
				if(!CMLib.masking().maskCheck(qualifications,mob,true))
				{
					if(!quiet)
						mob.tell(L("You must meet the following qualifications to be a @x1:\n@x2",name(),getStatQualDesc()));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String getStatQualDesc()
	{
		final String superQual=super.getStatQualDesc();
		if((qualifications!=null)&&(qualifications.length()>0))
			return superQual+", "+CMLib.masking().maskDesc(qualifications);
		return superQual;
	}

	protected String getCharClassLocatorID(CharClass C)
	{
		if(C==null)
			return "";
		if(C.isGeneric())
			return C.ID();
		if(C==CMClass.getCharClass(C.ID()))
			return C.ID();
		return C.getClass().getName();
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
		if(statBuddy!=null)
			statBuddy.affectPhyStats(affected,affectableStats);
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
					affectableStats.setStat(i,setStats.getStat(i));
			}
		}
		if(statBuddy!=null)
			statBuddy.affectCharStats(affectedMob,affectableStats);
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
		if(statBuddy!=null)
			statBuddy.affectCharState(affectedMob,affectableMaxState);
	}

	@Override
	public String classParms()
	{
		final StringBuffer str=new StringBuffer("");
		str.append("<CCLASS><ID>"+ID()+"</ID>");
		for(int i=0;i<names.length;i++)
		{
			str.append(CMLib.xml().convertXMLtoTag("NAME"+i,names[i]));
			str.append(CMLib.xml().convertXMLtoTag("NAMELEVEL"+i,nameLevels[i].intValue()));
		}
		str.append(CMLib.xml().convertXMLtoTag("BASE",baseClass()));
		str.append(CMLib.xml().convertXMLtoTag("LVLPRAC",""+bonusPracLevel));
		str.append(CMLib.xml().convertXMLtoTag("MANAFRM",""+manaFormula));
		str.append(CMLib.xml().convertXMLtoTag("MVMTFRM",""+movementFormula));
		str.append(CMLib.xml().convertXMLtoTag("HPFRM",""+hitPointsFormula));
		str.append(CMLib.xml().convertXMLtoTag("LEVELCAP",""+levelCap));
		str.append(CMLib.xml().convertXMLtoTag("LVLATT",""+bonusAttackLevel));
		str.append(CMLib.xml().convertXMLtoTag("ATTATT",""+attackAttribute));
		str.append(CMLib.xml().convertXMLtoTag("FSTPRAC",""+pracsFirstLevel));
		str.append(CMLib.xml().convertXMLtoTag("FSTTRAN",""+trainsFirstLevel));
		str.append(CMLib.xml().convertXMLtoTag("LVLDAM",""+levelsPerBonusDamage));
		str.append(CMLib.xml().convertXMLtoTag("ARMOR",""+allowedArmorLevel));
		str.append(CMLib.xml().convertXMLtoTag("STRLMT",otherLimitations));
		str.append(CMLib.xml().convertXMLtoTag("STRBON",otherBonuses));
		str.append(CMLib.xml().convertXMLtoTag("QUAL",qualifications));
		str.append(CMLib.xml().convertXMLtoTag("PLAYER",""+selectability));
		str.append(CMLib.xml().convertXMLtoTag("MAXNCS",""+maxNonCraftingSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXCRS",""+maxCraftingSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXCMS",""+maxCommonSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXLGS",""+maxLanguages));
		str.append(CMLib.xml().convertXMLtoTag("SUBRUL",subClassRule.toString()));
		str.append(CMLib.xml().convertXMLtoTag("RACQUAL",CMParms.toListString(getRequiredRaceList())));
		if(getMinimumStatRequirements().length==0)
			str.append("<MINSTATS />");
		else
		{
			str.append("<MINSTATS>");
			for(final Pair<String,Integer> stat : getMinimumStatRequirements())
				str.append("<STAT NAME=\""+stat.first+"\" MIN="+stat.second.toString()+" />");
			str.append("</MINSTATS>");
		}

		str.append(CMLib.xml().convertXMLtoTag("HELP",CMLib.xml().parseOutAngleBrackets(helpEntry)));
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

		final Vector<AbilityMapper.AbilityMapping> ables=getAbleSet();
		if((ables==null)||(ables.size()==0))
			str.append("<CABILITIES/>");
		else
		{
			str.append("<CABILITIES>");
			for(int r=0;r<ables.size();r++)
			{
				str.append("<CABILITY>");
				str.append("<CACLASS>"+ables.elementAt(r).abilityID()+"</CACLASS>");
				str.append("<CALEVEL>"+ables.elementAt(r).qualLevel()+"</CALEVEL>");
				str.append("<CAPROFF>"+ables.elementAt(r).defaultProficiency()+"</CAPROFF>");
				str.append("<CAAGAIN>"+ables.elementAt(r).autoGain()+"</CAAGAIN>");
				str.append("<CASECR>"+ables.elementAt(r).isSecret()+"</CASECR>");
				str.append("<CAPARM>"+ables.elementAt(r).defaultParm()+"</CAPARM>");
				str.append("<CAPREQ>"+ables.elementAt(r).originalSkillPreReqList()+"</CAPREQ>");
				str.append("<CAMASK>"+ables.elementAt(r).extraMask()+"</CAMASK>");
				str.append("<CAMAXP>"+ables.elementAt(r).maxProficiency()+"</CAMAXP>");
				str.append("</CABILITY>");
			}
			str.append("</CABILITIES>");
		}

		if((disallowedWeaponSet==null)||(disallowedWeaponSet.size()==0))
			str.append("<NOWEAPS/>");
		else
		{
			str.append("<NOWEAPS>");
			for(final Iterator i=disallowedWeaponSet.iterator();i.hasNext();)
			{
				final Integer I=(Integer)i.next();
				str.append(CMLib.xml().convertXMLtoTag("WCLASS",""+I.intValue()));
			}
			str.append("</NOWEAPS>");
		}
		if((requiredWeaponMaterials==null)||(requiredWeaponMaterials.size()==0))
			str.append("<NOWMATS/>");
		else
		{
			str.append("<NOWMATS>");
			for(final Iterator i=requiredWeaponMaterials.iterator();i.hasNext();)
			{
				final Integer I=(Integer)i.next();
				str.append(CMLib.xml().convertXMLtoTag("WMAT",""+I.intValue()));
			}
			str.append("</NOWMATS>");
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
		for(int i=0;i<securityGroups.length;i++)
		if(i<securityGroupLevels.length)
		{
			str.append(CMLib.xml().convertXMLtoTag("SSET"+i,CMParms.combineQuoted(securityGroups[i],0)));
			str.append(CMLib.xml().convertXMLtoTag("SSETLEVEL"+i,securityGroupLevels[i].intValue()));
		}
		str.append(CMLib.xml().convertXMLtoTag("MONEY",startingMoney));
		str.append(CMLib.xml().convertXMLtoTag("ARMORMINOR",""+requiredArmorSourceMinor));
		str.append(CMLib.xml().convertXMLtoTag("STATCLASS",getCharClassLocatorID(statBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("EVENTCLASS",getCharClassLocatorID(eventBuddy)));
		if(xtraValues==null)
			xtraValues=CMProps.getExtraStatCodesHolder(this);
		for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
			str.append(CMLib.xml().convertXMLtoTag(getStatCodes()[i],getStat(getStatCodes()[i])));
		str.append("</CCLASS>");
		return str.toString();
	}

	@Override
	public void setClassParms(String parms)
	{
		if(parms.trim().length()==0)
			return;
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenCharClass","Unable to parse: "+parms);
			return;
		}
		final List<XMLLibrary.XMLTag> classData=CMLib.xml().getContentsFromPieces(xml,"CCLASS");
		if (classData == null)
		{
			Log.errOut("GenCharClass", "Unable to get CCLASS data.");
			return;
		}
		final String classID=CMLib.xml().getValFromPieces(classData,"ID");
		if(classID.length()==0)
			return;
		ID=classID;
		final String singleName=CMLib.xml().getValFromPieces(classData,"NAME");
		if((singleName!=null)&&(singleName.length()>0))
		{
			names=new String[1];
			names[0]=singleName;
			nameLevels=new Integer[1];
			nameLevels[0]=Integer.valueOf(0);
		}
		else
		{
			final Vector<String> nameSet=new Vector<String>();
			final Vector<Integer> levelSet=new Vector<Integer>();
			int index=0;
			int lastLevel=-1;
			while(true)
			{
				final String name=CMLib.xml().getValFromPieces(classData,"NAME"+index);
				final int level=CMLib.xml().getIntFromPieces(classData,"NAMELEVEL"+index);
				if((name.length()==0)||(level<=lastLevel))
					break;
				nameSet.addElement(name);
				levelSet.addElement(Integer.valueOf(level));
				lastLevel=level;
				index++;
			}
			names=new String[nameSet.size()];
			nameLevels=new Integer[levelSet.size()];
			for(int i=0;i<nameSet.size();i++)
			{
				names[i]=nameSet.elementAt(i);
				nameLevels[i]=levelSet.elementAt(i);
			}
		}
		final String base=CMLib.xml().getValFromPieces(classData,"BASE");
		if((base==null)||(base.length()==0))
			return;
		baseClass=base;
		hitPointsFormula=CMLib.xml().getValFromPieces(classData,"HPFRM");
		if((hitPointsFormula==null)||(hitPointsFormula.length()==0))
		{
			int hpDivisor=CMLib.xml().getIntFromPieces(classData,"HPDIV");
			if(hpDivisor==0)
				hpDivisor=3;
			final int hpDice=CMLib.xml().getIntFromPieces(classData,"HPDICE");
			final int hpDie=CMLib.xml().getIntFromPieces(classData,"HPDIE");
			hitPointsFormula="((@x6<@x7)/"+hpDivisor+")+("+hpDice+"*(1?"+hpDie+"))";
		}
		bonusPracLevel=CMLib.xml().getIntFromPieces(classData,"LVLPRAC");
		manaFormula=CMLib.xml().getValFromPieces(classData,"MANAFRM");
		if((manaFormula==null)||(manaFormula.length()==0))
		{
			int manaDivisor=CMLib.xml().getIntFromPieces(classData,"MANADIV");
			if(manaDivisor==0)
				manaDivisor=3;
			final int manaDice=CMLib.xml().getIntFromPieces(classData,"MANADICE");
			final int manaDie=CMLib.xml().getIntFromPieces(classData,"MANADIE");
			manaFormula="((@x4<@x5)/"+manaDivisor+")+("+manaDice+"*(1?"+manaDie+"))";
		}
		levelCap=CMLib.xml().getIntFromPieces(classData,"LEVELCAP");
		bonusAttackLevel=CMLib.xml().getIntFromPieces(classData,"LVLATT");
		attackAttribute=CMLib.xml().getIntFromPieces(classData,"ATTATT");
		trainsFirstLevel=CMLib.xml().getIntFromPieces(classData,"FSTTRAN");
		pracsFirstLevel=CMLib.xml().getIntFromPieces(classData,"FSTPRAC");
		levelsPerBonusDamage=CMLib.xml().getIntFromPieces(classData,"LVLDAM");
		movementFormula=CMLib.xml().getValFromPieces(classData,"MVMTFRM");
		if((movementFormula==null)||(movementFormula.length()==0))
		{
			final int movementMultiplier=CMLib.xml().getIntFromPieces(classData,"LVLMOVE");
			movementFormula=movementMultiplier+"*((@x2<@x3)/18)";
		}
		allowedArmorLevel=CMLib.xml().getIntFromPieces(classData,"ARMOR");
		//weaponLimitations=CMLib.xml().getValFromPieces(classData,"STRWEAP");
		//armorLimitations=CMLib.xml().getValFromPieces(classData,"STRARM");
		otherLimitations=CMLib.xml().getValFromPieces(classData,"STRLMT");
		otherBonuses=CMLib.xml().getValFromPieces(classData,"STRBON");
		qualifications=CMLib.xml().getValFromPieces(classData,"QUAL");
		maxNonCraftingSkills=CMLib.xml().getIntFromPieces(classData,"MAXNCS");
		maxCraftingSkills=CMLib.xml().getIntFromPieces(classData,"MAXCRS");
		maxCommonSkills=CMLib.xml().getIntFromPieces(classData,"MAXCMS");
		maxLanguages=CMLib.xml().getIntFromPieces(classData,"MAXLGS");
		subClassRule=(SubClassRule)CMath.s_valueOf(SubClassRule.class,CMLib.xml().getValFromPieces(classData,"SUBRUL"),SubClassRule.BASEONLY);
		helpEntry=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(classData,"HELP"));
		raceRequiredList=CMParms.parseCommas(CMLib.xml().getValFromPieces(classData,"RACQUAL"), true).toArray(new String[0]);
		final List<Pair<String,Integer>> statQuals=new ArrayList<Pair<String,Integer>>();
		final List<XMLLibrary.XMLTag> mV=CMLib.xml().getContentsFromPieces(classData,"MINSTATS");
		if((mV!=null)&&(mV.size()>0))
		{
			for(final XMLTag p : mV)
			{
				if(p.tag().equalsIgnoreCase("STAT"))
					statQuals.add(new Pair<String,Integer>(p.parms().get("NAME"),Integer.valueOf(CMath.s_int(p.parms().get("MIN")))));
			}
		}
		minimumStatRequirements=statQuals.toArray(new Pair[0]);

		final String s=CMLib.xml().getValFromPieces(classData,"PLAYER");
		if(CMath.isNumber(s))
			selectability=CMath.s_int(s);
		else
			selectability=CMath.s_bool(s)?Area.THEME_FANTASY:0;
		adjPStats=null;
		final String eStats=CMLib.xml().getValFromPieces(classData,"ESTATS");
		if (eStats.length() > 0)
		{
			adjPStats = (PhyStats) CMClass.getCommon("DefaultPhyStats");
			CMLib.coffeeMaker().setPhyStats(adjPStats, eStats);
		}
		adjStats = null;
		final String aStats = CMLib.xml().getValFromPieces(classData, "ASTATS");
		if (aStats.length() > 0)
		{
			adjStats = (CharStats) CMClass.getCommon("DefaultCharStats");
			CMLib.coffeeMaker().setCharStats(adjStats, aStats);
		}
		setStats = null;
		final String cStats = CMLib.xml().getValFromPieces(classData, "CSTATS");
		if (cStats.length() > 0)
		{
			setStats = (CharStats) CMClass.getCommon("DefaultCharStats");
			CMLib.coffeeMaker().setCharStats(setStats, cStats);
		}
		adjState = null;
		final String aState = CMLib.xml().getValFromPieces(classData, "ASTATE");
		if (aState.length() > 0)
		{
			adjState = (CharState) CMClass.getCommon("DefaultCharState");
			CMLib.coffeeMaker().setCharState(adjState, aState);
		}
		startAdjState = null;
		disableFlags = CMLib.xml().getIntFromPieces(classData, "DISFLAGS");
		final String saState = CMLib.xml().getValFromPieces(classData, "STARTASTATE");
		if (saState.length() > 0)
		{
			startAdjState = (CharState) CMClass.getCommon("DefaultCharState");
			startAdjState.setAllValues(0);
			CMLib.coffeeMaker().setCharState(startAdjState, saState);
		}

		List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(classData,"CABILITIES");
		CMLib.ableMapper().delCharMappings(ID());
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("CABILITY"))||(iblk.contents()==null))
					continue;
				// I hate backwards compatibility.
				String maxProff=iblk.getValFromPieces("CAMAXP");
				if((maxProff==null)||(maxProff.trim().length()==0))
					maxProff="100";
				CMLib.ableMapper().addCharAbilityMapping(ID(),
									 iblk.getIntFromPieces("CALEVEL"),
									 iblk.getValFromPieces("CACLASS"),
									 iblk.getIntFromPieces("CAPROFF"),
									 CMath.s_int(maxProff),
									 iblk.getValFromPieces("CAPARM"),
									 iblk.getBoolFromPieces("CAAGAIN"),
									 iblk.getBoolFromPieces("CASECR"),
									 CMParms.parseCommas(iblk.getValFromPieces("CAPREQ"),true),
									 iblk.getValFromPieces("CAMASK"),
									 null);
			}
		}

		// now WEAPON RESTRICTIONS!
		xV=CMLib.xml().getContentsFromPieces(classData,"NOWEAPS");
		disallowedWeaponSet=null;
		if((xV!=null)&&(xV.size()>0))
		{
			disallowedWeaponSet=new HashSet<Integer>();
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("WCLASS"))||(iblk.contents()==null))
					continue;
				disallowedWeaponSet.add(Integer.valueOf(CMath.s_int(iblk.value())));
			}
		}

		// now WEAPON MATERIALS!
		xV=CMLib.xml().getContentsFromPieces(classData,"NOWMATS");
		requiredWeaponMaterials=null;
		if((xV!=null)&&(xV.size()>0))
		{
			requiredWeaponMaterials=new HashSet<Integer>();
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("WMAT"))||(iblk.contents()==null))
					continue;
				requiredWeaponMaterials.add(Integer.valueOf(CMath.s_int(iblk.value())));
			}
		}

		// now OUTFIT!
		final List<XMLLibrary.XMLTag> oV=CMLib.xml().getContentsFromPieces(classData,"OUTFIT");
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
				final String idat=iblk.getValFromPieces("OFDATA");
				newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				newOne.recoverPhyStats();
				outfitChoices.add(newOne);
			}
		}

		// security groups
		final List<List<String>> groupSet=new Vector<List<String>>();
		final List<Integer> groupLevelSet=new Vector<Integer>();
		int index=0;
		int lastLevel=-1;
		while(true)
		{
			final String groups=CMLib.xml().getValFromPieces(classData,"SSET"+index);
			final int groupLevel=CMLib.xml().getIntFromPieces(classData,"SSETLEVEL"+index);
			if((groups.length()==0)||(groupLevel<=lastLevel))
				break;
			groupSet.add(CMParms.parse(groups.toUpperCase()));
			groupLevelSet.add(Integer.valueOf(groupLevel));
			lastLevel=groupLevel;
			index++;
		}
		securityGroups=new Vector[groupSet.size()];
		securityGroupLevels=new Integer[groupLevelSet.size()];
		for(int i=0;i<groupSet.size();i++)
		{
			securityGroups[i]=groupSet.get(i);
			securityGroupLevels[i]=groupLevelSet.get(i);
		}
		securityGroupCache.clear();

		requiredArmorSourceMinor=CMLib.xml().getIntFromPieces(classData,"ARMORMINOR");
		startingMoney=CMLib.xml().getValFromPieces(classData,"MONEY");
		if(startingMoney==null)
			startingMoney="";
		setStat("STATCLASS",CMLib.xml().getValFromPieces(classData,"STATCLASS"));
		setStat("EVENTCLASS",CMLib.xml().getValFromPieces(classData,"EVENTCLASS"));
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
			setStat(getStatCodes()[i],CMLib.xml().getValFromPieces(classData, getStatCodes()[i]));
	}

	protected Vector<AbilityMapper.AbilityMapping> getAbleSet()
	{
		final Vector<AbilityMapper.AbilityMapping> VA=new Vector<AbilityMapper.AbilityMapping>(9);
		final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),Integer.MAX_VALUE,true,false);
		for(final AbilityMapper.AbilityMapping able : V)
		{
			final String AID=able.abilityID();
			final AbilityMapper.AbilityMapping newMAP=CMLib.ableMapper().newAbilityMapping().ID(ID());
			newMAP.abilityID(AID);
			newMAP.qualLevel(CMLib.ableMapper().getQualifyingLevel(ID(),false,AID));
			newMAP.defaultProficiency(CMLib.ableMapper().getDefaultProficiency(ID(),false,AID));
			newMAP.autoGain(CMLib.ableMapper().getDefaultGain(ID(),false,AID));
			newMAP.isSecret(CMLib.ableMapper().getSecretSkill(ID(),false,AID));
			newMAP.defaultParm(CMLib.ableMapper().getDefaultParm(ID(),false,AID));
			newMAP.originalSkillPreReqList(CMLib.ableMapper().getPreReqStrings(ID(),false,AID));
			newMAP.extraMask(CMLib.ableMapper().getExtraMask(ID(),false,AID));
			newMAP.maxProficiency(CMLib.ableMapper().getMaxProficiency(ID(),false,AID));
			VA.addElement(newMAP);
		}
		return VA;
	}

	protected static String[] CODES={"ID","NAME","BASE","HITPOINTSFORMULA","SUBRUL",
									 "LVLPRAC","RACQUAL","LVLATT","ATTATT","FSTTRAN",
									 "FSTPRAC","LVLDAM","MOVEMENTFORMULA","ARMOR","STRWEAP",
									 "STRARM","STRLMT","STRBON","QUAL","PLAYER",
									 "ESTATS","ASTATS","CSTATS","ASTATE","NUMCABLE",
									 "GETCABLE","GETCABLELVL","GETCABLEPROF","GETCABLEGAIN","GETCABLESECR",
									 "GETCABLEPARM","NUMWEP","GETWEP", "NUMOFT","GETOFTID",
									 "GETOFTPARM","NUMMINSTATS","MANAFORMULA","GETMINSTAT","DISFLAGS",
									 "STARTASTATE","NUMNAME","NAMELEVEL","NUMSSET","SSET",
									 "SSETLEVEL","NUMWMAT","GETWMAT","ARMORMINOR","STATCLASS",
									 "EVENTCLASS","GETCABLEPREQ","GETCABLEMASK","HELP","LEVELCAP",
									 "GETCABLEMAXP","MAXNCS","MAXCRS","MAXCMS","MAXLGS","GETSTATMIN",
									 "MONEY"
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
			return ID;
		case 1:
			if (num < names.length)
				return names[num];
			break;
		case 2:
			return baseClass;
		case 3:
			return "" + hitPointsFormula;
		case 4:
			return subClassRule.toString();
		case 5:
			return "" + bonusPracLevel;
		case 6:
			return CMParms.toListString(getRequiredRaceList());
		case 7:
			return "" + bonusAttackLevel;
		case 8:
			return "" + attackAttribute;
		case 9:
			return "" + trainsFirstLevel;
		case 10:
			return "" + pracsFirstLevel;
		case 11:
			return "" + levelsPerBonusDamage;
		case 12:
			return "" + movementFormula;
		case 13:
			return "" + allowedArmorLevel;
		case 14:
			return "";// weaponLimitations;
		case 15:
			return "";// armorLimitations;
		case 16:
			return otherLimitations;
		case 17:
			return otherBonuses;
		case 18:
			return qualifications;
		case 19:
			return "" + selectability;
		case 20:
			return (adjPStats == null) ? "" : CMLib.coffeeMaker().getPhyStatsStr(adjPStats);
		case 21:
			return (adjStats == null) ? "" : CMLib.coffeeMaker().getCharStatsStr(adjStats);
		case 22:
			return (setStats == null) ? "" : CMLib.coffeeMaker().getCharStatsStr(setStats);
		case 23:
			return (adjState == null) ? "" : CMLib.coffeeMaker().getCharStateStr(adjState);
		case 24:
			return Integer.toString(getAbleSet().size());
		case 25:
			return getAbleSet().elementAt(num).abilityID();
		case 26:
			return Integer.toString(getAbleSet().elementAt(num).qualLevel());
		case 27:
			return Integer.toString(getAbleSet().elementAt(num).defaultProficiency());
		case 28:
			return Boolean.toString(getAbleSet().elementAt(num).autoGain());
		case 29:
			return Boolean.toString(getAbleSet().elementAt(num).isSecret());
		case 30:
			return getAbleSet().elementAt(num).defaultParm();
		case 31:
			return "" + ((disallowedWeaponSet != null) ? disallowedWeaponSet.size() : 0);
		case 32:
			return CMParms.toListString(disallowedWeaponSet);
		case 33:
			return "" + ((outfit(null) != null) ? outfit(null).size() : 0);
		case 34:
			return "" + ((outfit(null) != null) ? outfit(null).get(num).ID() : "");
		case 35:
			return "" + ((outfit(null) != null) ? outfit(null).get(num).text() : "");
		case 36:
			return "" + getMinimumStatRequirements().length;
		case 37:
			return "" + manaFormula;
		case 38:
			return getMinimumStatRequirements()[num].first;
		case 39:
			return "" + disableFlags;
		case 40:
			return (startAdjState == null) ? "" : CMLib.coffeeMaker().getCharStateStr(startAdjState);
		case 41:
			return "" + names.length;
		case 42:
			if (num < nameLevels.length)
				return "" + nameLevels[num].intValue();
			break;
		case 43:
			return "" + securityGroups.length;
		case 44:
			if (num < securityGroups.length)
				return CMParms.combineQuoted(securityGroups[num], 0);
			break;
		case 45:
			if (num < securityGroupLevels.length)
				return "" + securityGroupLevels[num];
			break;
		case 46:
			return "" + ((requiredWeaponMaterials != null) ? requiredWeaponMaterials.size() : 0);
		case 47:
			return CMParms.toListString(requiredWeaponMaterials);
		case 48:
			return "" + requiredArmorSourceMinor();
		case 49:
			return this.getCharClassLocatorID(statBuddy);
		case 50:
			return this.getCharClassLocatorID(eventBuddy);
		case 51:
			return getAbleSet().elementAt(num).originalSkillPreReqList();
		case 52:
			return getAbleSet().elementAt(num).extraMask();
		case 53:
			return helpEntry;
		case 54:
			return "" + levelCap;
		case 55:
			return Integer.toString(getAbleSet().elementAt(num).maxProficiency());
		case 56:
			return "" + maxNonCraftingSkills;
		case 57:
			return "" + maxCraftingSkills;
		case 58:
			return "" + maxCommonSkills;
		case 59:
			return "" + maxLanguages;
		case 60:
			return getMinimumStatRequirements()[num].second.toString();
		case 61:
			return startingMoney;
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
		return "";
	}
	
	protected String[] tempables=new String[9];
	
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
			if (num < names.length)
				names[num] = val;
			break;
		case 2:
			baseClass = val;
			break;
		case 3:
			hitPointsFormula = val;
			super.hitPointsDesc = null;
			break;
		case 4:
			subClassRule = (SubClassRule) CMath.s_valueOf(SubClassRule.class, val.toUpperCase().trim(), SubClassRule.BASEONLY);
			break;
		case 5:
			bonusPracLevel = CMath.s_parseIntExpression(val);
			break;
		case 6:
			raceRequiredList = CMParms.parseCommas(val, true).toArray(new String[0]);
			break;
		case 7:
			bonusAttackLevel = CMath.s_parseIntExpression(val);
			break;
		case 8:
			attackAttribute = CMath.s_parseListIntExpression(CharStats.CODES.NAMES(), val);
			break;
		case 9:
			trainsFirstLevel = CMath.s_parseIntExpression(val);
			break;
		case 10:
			pracsFirstLevel = CMath.s_parseIntExpression(val);
			break;
		case 11:
			levelsPerBonusDamage = CMath.s_parseIntExpression(val);
			break;
		case 12:
			movementFormula = val;
			super.movementDesc = null;
			break;
		case 13:
			allowedArmorLevel = CMath.s_parseListIntExpression(CharClass.ARMOR_DESCS, val);
			break;
		case 14:
			break;// weaponLimitations=val;break;
		case 15:
			break;// armorLimitations=val;break;
		case 16:
			otherLimitations = val;
			break;
		case 17:
			otherBonuses = val;
			break;
		case 18:
			qualifications = val;
			break;
		case 19:
			selectability = CMath.s_parseBitIntExpression(Area.THEME_BIT_NAMES, val);
			break;
		case 20:
			adjPStats = null;
			if (val.length() > 0)
			{
				adjPStats = (PhyStats) CMClass.getCommon("DefaultPhyStats");
				adjPStats.setAllValues(0);
				CMLib.coffeeMaker().setPhyStats(adjPStats, val);
			}
			break;
		case 21:
			adjStats = null;
			if (val.length() > 0)
			{
				adjStats = (CharStats) CMClass.getCommon("DefaultCharStats");
				adjStats.setAllValues(0);
				CMLib.coffeeMaker().setCharStats(adjStats, val);
			}
			break;
		case 22:
			setStats = null;
			if (val.length() > 0)
			{
				setStats = (CharStats) CMClass.getCommon("DefaultCharStats");
				setStats.setAllValues(0);
				CMLib.coffeeMaker().setCharStats(setStats, val);
			}
			break;
		case 23:
			adjState = null;
			if (val.length() > 0)
			{
				adjState = (CharState) CMClass.getCommon("DefaultCharState");
				adjState.setAllValues(0);
				CMLib.coffeeMaker().setCharState(adjState, val);
			}
			break;
		case 24:
			CMLib.ableMapper().delCharMappings(ID());
			break;
		case 25: 
			CMLib.ableMapper().addCharAbilityMapping(ID(),
													 CMath.s_int(tempables[1]),
													 val,
													 CMath.s_int(tempables[2]),
													 CMath.s_int(tempables[8]),
													 tempables[5],
													 CMath.s_bool(tempables[3]),
													 CMath.s_bool(tempables[4]),
													 CMParms.parseCommas(tempables[6],true),
													 tempables[7],
													 null);
			break;
		case 26:
			tempables[1] = val;
			break;
		case 27:
			tempables[2] = val;
			break;
		case 28:
			tempables[3] = val;
			break;
		case 29:
			tempables[4] = val;
			break;
		case 30:
			tempables[5] = val;
			break;
		case 31:
			if (CMath.s_int(val) == 0)
				disallowedWeaponSet = null;
			else
				disallowedWeaponSet = new HashSet<Integer>();
			break;
		case 32:
		{
			final List<String> V = CMParms.parseCommas(val, true);
			if (V.size() > 0)
			{
				disallowedWeaponSet = new HashSet<Integer>();
				for (int v = 0; v < V.size(); v++)
					disallowedWeaponSet.add(Integer.valueOf(CMath.s_int(V.get(v))));
			}
			else
				disallowedWeaponSet = null;
			break;
		}
		case 33:
			if (CMath.s_int(val) == 0)
				outfitChoices = null;
			break;
		case 34:
		{
			if (outfitChoices == null)
				outfitChoices = new Vector<Item>();
			if (num >= outfitChoices.size())
				outfitChoices.add(CMClass.getItem(val));
			else
				outfitChoices.set(num, CMClass.getItem(val));
			break;
		}
		case 35:
		{
			if ((outfitChoices != null) && (num < outfitChoices.size()))
			{
				final Item I = outfitChoices.get(num);
				I.setMiscText(val);
				I.recoverPhyStats();
			}
			break;
		}
		case 36:
		{
			minimumStatRequirements = new Pair[CMath.s_int(val)];
			for (int i = 0; i < CMath.s_int(val); i++)
				minimumStatRequirements[i] = new Pair<String, Integer>("", Integer.valueOf(0));
			break;
		}
		case 37:
			manaFormula = val;
			super.manaDesc = null;
			break;
		case 38:
			minimumStatRequirements[num].first = val;
			break;
		case 39:
			disableFlags = CMath.s_int(val);
			break;
		case 40:
			startAdjState = null;
			if (val.length() > 0)
			{
				startAdjState = (CharState) CMClass.getCommon("DefaultCharState");
				startAdjState.setAllValues(0);
				CMLib.coffeeMaker().setCharState(startAdjState, val);
			}
			break;
		case 41:
		{
			num = CMath.s_int(val);
			if (num > 0)
			{
				final String[] newNames = new String[num];
				final Integer[] newLevels = new Integer[num];
				for (int i = 0; i < names.length; i++)
				{
					if (i < num)
					{
						newNames[i] = names[i];
						newLevels[i] = nameLevels[i];
					}
				}
				if (newNames.length > names.length)
				{
					for (int i = names.length; i < newNames.length; i++)
					{
						newNames[i] = names[names.length - 1];
						newLevels[i] = Integer.valueOf(newLevels[i - 1].intValue() + 1);
					}
				}
				names = newNames;
				nameLevels = newLevels;
			}
			break;
		}
		case 42:
			if (num < nameLevels.length)
				nameLevels[num] = Integer.valueOf(CMath.s_int(val));
			break;
		case 43:
		{
			num = CMath.s_int(val);
			if (num < 0)
				num = 0;
			final List<String>[] newGroups = new Vector[num];
			final Integer[] newLevels = new Integer[num];
			for (int i = 0; i < securityGroups.length; i++)
			{
				if (i < num)
				{
					newGroups[i] = securityGroups[i];
					newLevels[i] = securityGroupLevels[i];
				}
			}
			if (newGroups.length > securityGroups.length)
			{
				for (int i = securityGroups.length; i < newGroups.length; i++)
				{
					newGroups[i] = new Vector<String>();
					if (i == 0)
						newLevels[0] = Integer.valueOf(0);
					else
						newLevels[i] = Integer.valueOf(newLevels[i - 1].intValue() + 1);
				}
			}
			securityGroups = newGroups;
			securityGroupLevels = newLevels;
			securityGroupCache.clear();
			break;
		}
		case 44:
			if (num < securityGroups.length)
				securityGroups[num] = CMParms.parse(val.toUpperCase());
			securityGroupCache.clear();
			break;
		case 45:
			if (num < securityGroupLevels.length)
				securityGroupLevels[num] = Integer.valueOf(CMath.s_int(val));
			securityGroupCache.clear();
			break;
		case 46:
			if (CMath.s_int(val) == 0)
				requiredWeaponMaterials = null;
			else
				requiredWeaponMaterials = new HashSet<Integer>();
			break;
		case 47:
		{
			final List<String> V = CMParms.parseCommas(val, true);
			if (V.size() > 0)
			{
				requiredWeaponMaterials = new HashSet<Integer>();
				for (int v = 0; v < V.size(); v++)
					requiredWeaponMaterials.add(Integer.valueOf(CMath.s_int(V.get(v))));
			}
			else
				requiredWeaponMaterials = null;
			break;
		}
		case 48:
			requiredArmorSourceMinor = CMath.s_int(val);
			break;
		case 49:
		{
			statBuddy = CMClass.getCharClass(val);
			try
			{
				if (statBuddy == null)
					statBuddy = (CharClass) CMClass.getLoadNewClassInstance(CMObjectType.CHARCLASS, val, true);
			}
			catch (final Exception e)
			{
			}
			break;
		}
		case 50:
		{
			eventBuddy = CMClass.getCharClass(val);
			try
			{
				if (eventBuddy == null)
					eventBuddy = (CharClass) CMClass.getLoadNewClassInstance(CMObjectType.CHARCLASS, val, true);
			}
			catch (final Exception e)
			{
			}
			break;
		}
		case 51:
			tempables[6] = val;
			break;
		case 52:
			tempables[7] = val;
			break;
		case 53:
			helpEntry = val;
			break;
		case 54:
			levelCap = CMath.s_int(val);
			break;
		case 55:
			tempables[8] = val;
			break;
		case 56:
			maxNonCraftingSkills = CMath.s_int(val);
			break;
		case 57:
			maxCraftingSkills = CMath.s_int(val);
			break;
		case 58:
			maxCommonSkills = CMath.s_int(val);
			break;
		case 59:
			maxLanguages = CMath.s_int(val);
			break;
		case 60:
			minimumStatRequirements[num].second = Integer.valueOf(CMath.s_int(val));
			break;
		case 61:
			startingMoney = val;
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		super.startCharacter(mob,isBorrowedClass,verifyOnly);
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
	public boolean sameAs(CharClass E)
	{
		if(!(E instanceof GenCharClass))
			return false;
		if(E.classParms().equals(classParms()))
			return true;
		return false;
	}
}
