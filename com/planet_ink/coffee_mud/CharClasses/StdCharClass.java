package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass.SubClassRule;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdCharClass implements CharClass
{
	@Override
	public String ID()
	{
		return "StdCharClass";
	}

	private final static String	localizedStaticName	= CMLib.lang().L("mob");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String name(int classLevel)
	{
		return name();
	}

	@Override
	public String baseClass()
	{
		return ID();
	}

	@Override
	public int getLevelCap()
	{
		return -1;
	}

	@Override
	public int getBonusPracLevel()
	{
		return 0;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getPracsFirstLevel()
	{
		return 5;
	}

	@Override
	public int getTrainsFirstLevel()
	{
		return 3;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 1;
	}

	@Override
	public String getMovementFormula()
	{
		return "5*((@x2<@x3)/18)";
	}

	public String	movementDesc	= null;

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/3)+(1*(1?6))";
	}

	public String	hitPointsDesc	= null;

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/3)+(1*(1?6))";
	}

	public String		manaDesc		= null;

	protected String[]	names			= null;

	protected int		maxStatAdj[]	= new int[CharStats.CODES.TOTAL()];
	protected List<Item>outfitChoices	= null;

	protected long		lastPropsCheckTime		= 0;
	protected long		previousRaceListHash	= 0;
	protected String	cachedRaceQualList		= null;
	protected Map<Race,Boolean>	finalAllowedRaceSet		= new Hashtable<Race,Boolean>();

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_ANY;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_ANY;
	}

	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return null;
	}

	protected Set<Integer> requiredWeaponMaterials()
	{
		return null;
	}

	protected int requiredArmorSourceMinor()
	{
		return -1;
	}

	protected String armorFailMessage()
	{
		return L("<S-NAME> fumble(s) <S-HIS-HER> <SKILL> due to <S-HIS-HER> armor!");
	}

	@Override
	public boolean raceless()
	{
		return false;
	}

	@Override
	public boolean leveless()
	{
		return false;
	}

	@Override
	public boolean expless()
	{
		return false;
	}

	@Override
	public SubClassRule getSubClassRule()
	{
		return SubClassRule.BASEONLY;
	}

	@Override
	public boolean showThinQualifyList()
	{
		return false;
	}

	@Override
	public int maxNonCraftingSkills()
	{
		return CMProps.getIntVar(CMProps.Int.MAXNONCRAFTINGSKILLS);
	}

	@Override
	public int maxCraftingSkills()
	{
		return CMProps.getIntVar(CMProps.Int.MAXCRAFTINGSKILLS);
	}

	@Override
	public int maxCommonSkills()
	{
		return CMProps.getIntVar(CMProps.Int.MAXCOMMONSKILLS);
	}

	@Override
	public int maxLanguages()
	{
		return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES);
	}

	private static final CMSecurity.SecGroup	empty	= new CMSecurity.SecGroup(new CMSecurity.SecFlag[] {});

	@Override
	public CMSecurity.SecGroup getSecurityFlags(int classLevel)
	{
		return empty;
	}

	private final String[]	raceRequiredList	= new String[0];

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	private final Pair<String, Integer>[]	minimumStatRequirements	= new Pair[0];

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public CMObject newInstance()
	{
		return this;
	}

	@Override
	public String getStartingMoney()
	{
		return "";
	}

	protected String getShortAttackAttribute()
	{
		return CharStats.CODES.SHORTNAME(getAttackAttribute());
	}

	protected final static String[][] hitPointDescReplacePairs={
		{"@x1","Lvl"},{"(@x2<@x3)","Str"},{"@x2<@x3","Str"},{"(@x4<@x5)","Dex"},{"@x4<@x5","Dex"},
		{"(@x6<@x7)","Con"},{"@x6<@x7","Con"},{"@x2","Str"},{"@x3","Str"},{"@x4","Dex"},{"@x5","Dex"},
		{"@x6", "Con"},{"@x7", "Con"},{"@x8", "Int"},{"@x9", "Wis"},{"1?", "d"},{"*", "X"}
	};
	protected final String[][] manaDescReplacePairs={
		{"@x1","Lvl"},{"(@x2<@x3)","Wis"},{"@x2<@x3","Wis"},{"(@x4<@x5)","Int"},{"@x4<@x5","Int"},
		{"(@x6<@x7)",getShortAttackAttribute()},{"@x6<@x7",getShortAttackAttribute()},{"@x2","Wis"},
		{"@x3","Wis"},{"@x4","Int"},{"@x5","Int"},{"@x6", "Con"},{"@x7", "Con"},{"@x8", "Cha"},
		{"@x9", "Dex"},{"1?", "d"},{"*", "X"}
	};
	protected final static String[][] movementDescReplacePairs={
		{"@x1","Lvl"},{"(@x2<@x3)","Str"},{"@x2<@x3","Str"},{"(@x4<@x5)","Dex"},{"@x4<@x5","Dex"},
		{"(@x6<@x7)","Con"},{"@x6<@x7","Con"},{"@x2","Str"},{"@x3","Str"},{"@x4","Dex"},{"@x5","Dex"},
		{"@x6", "Con"},{"@x7", "Con"},{"@x8", "Int"},{"@x9", "Wis"},{"1?", "d"},{"*", "X"}
	};

	@Override
	public String[] nameSet()
	{
		if(names!=null)
			return names;
		names=new String[1];
		names[0]=name();
		return names;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public int availabilityCode()
	{
		return 0;
	}

	public void cloneFix(CharClass C)
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdCharClass E=(StdCharClass)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this;
		}
	}

	@Override
	public int classDurationModifier(MOB myChar, Ability skill, int duration)
	{
		return duration;
	}

	@Override
	public int classLevelModifier(MOB myChar, Ability skill, int level)
	{
		return level;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public boolean tick(Tickable myChar, int tickID)
	{
		return true;
	}

	@Override
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.STDCLASSES) && (!isGeneric()))
			return false;

		final String multiClassRule=CMProps.getVar(CMProps.Str.MULTICLASS);
		String multiClassFirstRule=multiClassRule;
		String multiClassSecondRule="";
		final int x=multiClassRule.indexOf('-');
		if(x>0)
		{
			multiClassFirstRule=multiClassRule.substring(0, x);
			multiClassSecondRule=multiClassRule.substring(x+1);
		}

		final String changeToBaseClassID=baseClass();
		final String changeToClassID=ID();
		final SubClassRule changeToSubClassRule = getSubClassRule();

		String canOnlyBeClassID="";
		String canOnlyBeBaseClassID="";
		if(multiClassSecondRule.length()>0)
		{
			if(multiClassSecondRule.startsWith("GRP-"))
			{
				final CharClass possibleClass=CMClass.findCharClass(multiClassSecondRule.substring(4));
				if(possibleClass != null)
					canOnlyBeBaseClassID=possibleClass.ID();
				multiClassSecondRule="NO";
			}
			else
			{
				final CharClass possibleClass=CMClass.findCharClass(multiClassSecondRule);
				if(possibleClass != null)
					canOnlyBeClassID=possibleClass.ID();
				multiClassSecondRule="NO";
			}
		}

		if(mob == null)
		{
			if(multiClassFirstRule.equals("SUB")||multiClassSecondRule.equals("SUB"))
			{
				if((changeToBaseClassID.equals(changeToClassID))||(changeToSubClassRule==SubClassRule.ANY))
					return true;
				return false;
			}
			else
				return true;
		}

		final CharClass curClass = mob.baseCharStats().getCurrentClass();
		final String currentClassID=curClass.ID();
		final String currentBaseClassID=curClass.baseClass();

		for(final Pair<String,Integer> minReq : getMinimumStatRequirements())
		{
			final int statCode=CharStats.CODES.findWhole(minReq.first, true);
			if(statCode >= 0)
			{
				final CharStats cStats = (CharStats)mob.baseCharStats().copyOf();
				cStats.getMyRace().affectCharStats(mob, cStats);
				if(cStats.getStat(statCode) < minReq.second.intValue())
				{
					if(!quiet)
						mob.tell(L("You need at least a @x1 @x2 to become a @x3.",minReq.second.toString(),CMStrings.capitalizeAndLower(CharStats.CODES.NAME(statCode)),name()));
					return false;
				}
			}
		}
		final Race R=mob.baseCharStats().getMyRace();
		if(!isAllowedRace(R))
		{
			if(!quiet)
			{
				mob.tell(L("You need to be a @x1 to be a @x2.",getRaceQualDesc(),name()));
			}
			return false;
		}
		if((!mob.isMonster())&&(mob.basePhyStats().level()>0))
		{
			if(currentClassID.equals(changeToClassID))
			{
				if(!quiet)
					mob.tell(L("But you are already a @x1!",name()));
				return false;
			}
			if(currentClassID.equalsIgnoreCase("StdCharClass")) // this is the starting character rule
			{
				if((canOnlyBeClassID.length()>0)&&(!changeToClassID.equals(canOnlyBeClassID)))
					return false;
				if((canOnlyBeBaseClassID.length()>0)&&(!changeToBaseClassID.equals(canOnlyBeBaseClassID)))
					return false;
				if((multiClassRule.equals("NO"))||(multiClassRule.equals("MULTI")))
					return true;
				if((multiClassRule.equals("SUB")||multiClassSecondRule.equals("BASE"))
				&&(changeToBaseClassID.equals(changeToClassID)||(changeToSubClassRule==SubClassRule.ANY)))
					return true;
				if((multiClassSecondRule.equals("SUBONLY"))
				&&((!changeToBaseClassID.equals(changeToClassID))||(changeToSubClassRule==SubClassRule.ANY)))
					return true;
				if(multiClassFirstRule.equals("APP")&&(getSubClassRule()==SubClassRule.ANY))
					return true;
				if(!quiet)
					mob.tell(L("You can't train to be a @x1!",name()));
				return false;
			}
			else
			if(curClass.getSubClassRule()==SubClassRule.NONE)
			{
				if(!quiet)
					mob.tell(L("You can't train to be a @x1!",name()));
				return false;
			}
			else
			if(curClass.getSubClassRule()==SubClassRule.ANY) // if you are an apprentice
			{
				if(multiClassFirstRule.equals("NO")
				||multiClassSecondRule.equals("NO")
				||multiClassFirstRule.equals("MULTI")
				||multiClassSecondRule.equals("MULTI"))
					return true;
				if(multiClassFirstRule.equals("SUB")||multiClassSecondRule.equals("SUB"))
				{
					if((changeToBaseClassID.equals(changeToClassID))||(changeToBaseClassID.equals(currentBaseClassID)))
						return true;
					if(!quiet)
						mob.tell(L("You must be a @x1 type to become a @x2.",changeToBaseClassID,name()));
				}
				return false;
			}
			else
			{
				if(multiClassFirstRule.equals("MULTI")||multiClassSecondRule.equals("MULTI"))
					return true;
				else
				if(multiClassFirstRule.equals("NO")||(multiClassSecondRule.equals("NO")))
					mob.tell(L("You should be happy to be a @x1.",curClass.name()));
				else
				if(multiClassFirstRule.equals("SUB")|| multiClassSecondRule.equals("SUB"))
				{
					if(currentBaseClassID.equals(changeToBaseClassID)||(curClass.getSubClassRule()==SubClassRule.ANY))
						return true;
					boolean doesBaseHaveAnAny=false;
					for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
					{
						final CharClass C=c.nextElement();
						if((C.baseClass().equals(currentBaseClassID))&&(C.getSubClassRule()==SubClassRule.ANY))
						{
							doesBaseHaveAnAny=true;
							break;
						}
					}
					if(doesBaseHaveAnAny)
						return true;
					if(!quiet)
						mob.tell(L("You must be a @x1 type to become a @x2.",changeToBaseClassID,name()));
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public String getWeaponLimitDesc()
	{
		return WEAPONS_LONGDESC[allowedWeaponLevel()];
	}

	@Override
	public String getArmorLimitDesc()
	{
		return ARMOR_LONGDESC[allowedArmorLevel()];
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return "";
	}

	@Override
	public String getOtherBonusDesc()
	{
		return "";
	}

	@Override
	public String getStatQualDesc()
	{
		final Pair<String,Integer>[] reqs=getMinimumStatRequirements();
		if(reqs.length==0)
			return "None";
		final StringBuilder str=new StringBuilder("");
		for(int x=0;x<reqs.length;x++)
		{
			final Pair<String,Integer> req=reqs[x];
			if(x>0)
				str.append(", ");
			str.append(CMStrings.capitalizeAndLower(req.first)).append(" ").append(req.second.toString()).append("+");
		}
		return str.toString();
	}

	@Override
	public String getRaceQualDesc()
	{
		this.checkRaceQualifierChanges();
		if(this.cachedRaceQualList != null)
			return this.cachedRaceQualList;

		final XVector<Race> availRaces = new XVector<Race>();
		final CharCreationLibrary loginLib = CMLib.login();
		if((!CMClass.races().hasMoreElements())||(CMLib.login()==null))
			return "None";
		for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			final Race R=r.nextElement();
			if(loginLib.isAvailableRace(R))
				availRaces.add(R);
		}
		if(availRaces.size()==0)
		{
			this.cachedRaceQualList=L("All");
		}
		else
		{
			final XVector<Race> qualRaces = new XVector<Race>();
			for(Race R : availRaces)
			{
				if(isAllowedRace(R))
					qualRaces.add(R);
			}
			if(qualRaces.size()==availRaces.size())
			{
				this.cachedRaceQualList=L("All");
			}
			else
			{
				final StringBuilder str=new StringBuilder();
				ArrayList<String> names = new ArrayList<String>(availRaces.size());
				//if(qualRaces.size()<=(availRaces.size()/2))
				{
					for(Race R : qualRaces)
						names.add(R.name());
					str.append(CMLib.english().toEnglishStringList(names.toArray(new String[0])));
				}
				/*
				else
				{
					str.append(L("All except "));
					for(Race R : availRaces)
					{
						if(!qualRaces.contains(R))
							names.add(R.name());
					}
					str.append(CMLib.english().toEnglishStringList(names.toArray(new String[0])));
				}
				*/
				this.cachedRaceQualList=str.toString();
			}
		}
		return this.cachedRaceQualList;
	}

	@Override
	public String getMaxStatDesc()
	{
		final StringBuilder str=new StringBuilder("");
		for(final int i : CharStats.CODES.BASECODES())
		{
			if(maxStatAdjustments()[i]!=0)
				str.append(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i))+" ("+(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+maxStatAdjustments()[i])+"), ");
		}
		str.append(L("Others (@x1)",""+CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)));
		return str.toString();
	}

	@Override
	public String getPracticeDesc()
	{
		final StringBuilder str=new StringBuilder("");
		str.append(L("@x1 +[(Wisdom/6)",""+getPracsFirstLevel()));
		if(getBonusPracLevel()>0)
			str.append("+"+getBonusPracLevel());
		else
		if(getBonusPracLevel()<0)
			str.append(""+getBonusPracLevel());
		return str.toString()+L(" per lvl]");
	}

	@Override
	public String getTrainDesc()
	{
		return getTrainsFirstLevel()+L(" +1 per level");
	}

	@Override
	public String getDamageDesc()
	{
		return L("+1 damage per @x1 level(s)",""+getLevelsPerBonusDamage());
	}

	@Override
	public String getHitPointDesc()
	{
		if(hitPointsDesc==null)
		{
			String formula=getHitPointsFormula();
			final int x=formula.indexOf("*(1?");
			if(x>0)
			{
				final int y=formula.indexOf(')',x+1);
				if(y>x)
					formula=formula.substring(0, x)+"d"+formula.substring(x+4,y)+formula.substring(y+1);
			}
			formula=CMStrings.replaceAlls(formula, hitPointDescReplacePairs);
			hitPointsDesc=CMProps.getIntVar(CMProps.Int.STARTHP)+" +["+formula+" /lvl]";
		}
		return hitPointsDesc;
	}

	@Override
	public String getManaDesc()
	{
		if(manaDesc==null)
		{
			String formula=getManaFormula();
			final int x=formula.indexOf("*(1?");
			if(x>0)
			{
				final int y=formula.indexOf(')',x+1);
				if(y>x)
					formula=formula.substring(0, x)+"d"+formula.substring(x+4,y)+formula.substring(y+1);
			}
			formula=CMStrings.replaceAlls(formula, manaDescReplacePairs);
			manaDesc=CMProps.getIntVar(CMProps.Int.STARTMANA)+" +["+formula+"/lvl]";
		}
		return manaDesc;
	}

	@Override
	public String getMovementDesc()
	{
		if(movementDesc==null)
		{
			String formula=getMovementFormula();
			final int x=formula.indexOf("*(1?");
			if(x>0)
			{
				final int y=formula.indexOf(')',x+1);
				if(y>x)
					formula=formula.substring(0, x)+"d"+formula.substring(x+4,y)+formula.substring(y+1);
			}
			formula=CMStrings.replaceAlls(formula, movementDescReplacePairs);
			movementDesc=CMProps.getIntVar(CMProps.Int.STARTMOVE)+" +["+formula+" /lvl]";
		}
		return movementDesc;
	}

	@Override
	public String getPrimeStatDesc()
	{
		return CMStrings.capitalizeAndLower(CharStats.CODES.DESC(getAttackAttribute()));
	}

	@Override
	public String getAttackDesc()
	{
		final StringBuilder str=new StringBuilder("");
		str.append("+("+getPrimeStatDesc().substring(0,3)+"/18)");
		if(getBonusAttackLevel()>0)
			str.append("+"+getBonusAttackLevel());
		else
		if(getBonusAttackLevel()<0)
			str.append(""+getBonusAttackLevel());
		str.append(L(" per level"));
		return str.toString();
	}

	protected Set<Integer> buildDisallowedWeaponClasses()
	{
		return buildDisallowedWeaponClasses(allowedWeaponLevel());
	}

	protected Set<Integer> buildDisallowedWeaponClasses(int lvl)
	{
		if(lvl==CharClass.WEAPONS_ANY)
			return null;
		final int[] set=CharClass.WEAPONS_SETS[lvl];
		final Set<Integer> H=new HashSet<Integer>();
		if(set[0]>Weapon.CLASS_DESCS.length)
			return null;
		for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
		{
			boolean found=false;
			for (final int element : set)
			{
				if(element==i)
					found=true;
			}
			if(!found)
				H.add(Integer.valueOf(i));
		}
		return H;
	}
	
	protected HashSet<Integer> buildRequiredWeaponMaterials()
	{
		if(allowedWeaponLevel()==CharClass.WEAPONS_ANY)
			return null;
		final int[] set=CharClass.WEAPONS_SETS[allowedWeaponLevel()];
		if(set[0]>Weapon.CLASS_DESCS.length)
		{
			final HashSet<Integer> H=new HashSet<Integer>();
			for (final int element : set)
				H.add(Integer.valueOf(element));
			return H;
		}
		return null;
	}

	protected boolean isQualifyingAuthority(MOB mob, Ability A)
	{
		CharClass C=null;
		int ql=0;
		for(int i=(mob.charStats().numClasses()-1);i>=0;i--) // last one is current
		{
			C=mob.charStats().getMyClass(i);
			if( C != null )
			{
				ql=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID());
				if((ql>0)
				&&(ql<=mob.charStats().getClassLevel(C)))
					return (C.ID().equals(ID()));
			}
		}
		return false;
	}

	protected boolean armorCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(!(E instanceof Ability))
			return true;
		if((allowedArmorLevel()!=CharClass.ARMOR_ANY)
		&&((requiredArmorSourceMinor()<0)||(sourceCode&CMMsg.MINOR_MASK)==requiredArmorSourceMinor())
		&&(isQualifyingAuthority(mob,(Ability)E))
		&&(mob.isMine(E))
		&&(!E.ID().equals("Skill_Recall"))
		&&((((Ability)E).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)
		&&((((Ability)E).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)
		&&(!CMLib.utensils().armorCheck(mob,allowedArmorLevel()))
		&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2)))
			return false;
		return true;
	}

	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if((((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_WEAPONATTACK)||((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_THROW))
		&&(E instanceof Weapon)
		&&(mob.charStats().getCurrentClass().ID().equals(ID())))
		{
			for(DoubleFilterer<Item> F : mob.charStats().getItemProficiencies())
			{
				final DoubleFilterer.Result filterResult = F.getFilterResult((Item)E);
				if(filterResult != DoubleFilterer.Result.NOTAPPLICABLE)
				{
					return (filterResult == DoubleFilterer.Result.ALLOWED) ? true : false;
				}
			}
			if((((requiredWeaponMaterials()!=null)&&(!requiredWeaponMaterials().contains(Integer.valueOf(((Weapon)E).material()&RawMaterial.MATERIAL_MASK))))
				||((disallowedWeaponClasses(mob)!=null)&&(disallowedWeaponClasses(mob).contains(Integer.valueOf(((Weapon)E).weaponClassification())))))
			&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2))
			&&(mob.fetchWieldedItem()!=null))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> fumble(s) horribly with @x1.",E.name()));
				return false;
			}
		}
		return true;
	}

	protected boolean giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedClass)
	{
		return giveMobAbility(mob,A,proficiency,defaultParm,isBorrowedClass,true);
	}

	protected boolean giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedClass, boolean autoInvoke)
	{
		if(mob.fetchAbility(A.ID())==null)
		{
			A=(Ability)A.copyOf();
			A.setSavable(!isBorrowedClass);
			A.setProficiency(proficiency);
			A.setMiscText(defaultParm);
			mob.addAbility(A);
			if(autoInvoke)
				A.autoInvocation(mob, false);
			return true;
		}
		return false;
	}

	@Override
	public int[] maxStatAdjustments()
	{
		return maxStatAdj;
	}

	@Override
	public int addedExpertise(final MOB host, final ExpertiseLibrary.Flag expertiseCode, final String abilityID)
	{
		return 0;
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		if(CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ALLSKILLS)
		&&(mob.soulMate()==null))
		{
			// the most efficient way of doing this -- just hash em!
			final Map<String,Ability> alreadyAble=new HashMap<String,Ability>();
			final Map<String,Ability> alreadyAff=new HashMap<String,Ability>();
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null)
					alreadyAff.put(A.ID(),A);
			}
			for(int a=0;a<mob.numAbilities();a++)
			{
				final Ability A=mob.fetchAbility(a);
				if(A!=null)
				{
					A.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
					A.setSavable(false);
					final Ability A2=alreadyAff.get(A.ID());
					if(A2!=null)
						A2.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
					else
						A.autoInvocation(mob, false);
					alreadyAble.put(A.ID(),A);
				}
			}
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				final int lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				if((lvl>=0)
				&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
				&&(!alreadyAble.containsKey(A.ID())))
					giveMobAbility(mob,A,100,"",true,false);
			}
			for(final Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
				mob.addExpertise(((ExpertiseLibrary.ExpertiseDefinition)e.nextElement()).ID());
			alreadyAble.clear();
			alreadyAff.clear();
		}
		else
		{
			final PairList<Ability,AbilityMapping> onesToAdd=new PairVector<Ability,AbilityMapping>();
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				final AbilityMapping mapping = CMLib.ableMapper().getQualifyingMapping(ID(), true, A.ID());
				if((mapping != null)
				&&(mapping.qualLevel()>0)
				&&(mapping.qualLevel()<=mob.baseCharStats().getClassLevel(this))
				&&(mapping.autoGain()))
				{
					final String extraMask=mapping.extraMask();
					if((extraMask==null)
					||(extraMask.length()==0)
					||(CMLib.masking().maskCheck(extraMask,mob,true)))
						onesToAdd.add(A,mapping);
				}
			}
			for(int v=0;v<onesToAdd.size();v++)
			{
				final Ability A=onesToAdd.get(v).first;
				final AbilityMapping map=onesToAdd.get(v).second;
				giveMobAbility(mob,A,map.defaultProficiency(),map.defaultParm(),isBorrowedClass);
			}
		}
	}

	protected void checkRaceQualifierChanges()
	{
		final String[] requiredRaceList = getRequiredRaceList();
		synchronized(finalAllowedRaceSet)
		{
			if((previousRaceListHash != requiredRaceList.hashCode())
			||(CMProps.getLastResetTime() != lastPropsCheckTime))
			{
				finalAllowedRaceSet.clear();
				cachedRaceQualList = null;
				previousRaceListHash = requiredRaceList.hashCode();
				lastPropsCheckTime = CMProps.getLastResetTime();
			}
		}
	}
	
	@Override
	public boolean isAllowedRace(Race R)
	{
		checkRaceQualifierChanges();
		synchronized(finalAllowedRaceSet)
		{
			final Boolean finalCheck = finalAllowedRaceSet.get(R);
			if(finalCheck != null)
				return finalCheck.booleanValue();
		}
		
		final String[] requiredRaceList = getRequiredRaceList();
		final boolean secondCheck;
		final String[] overrideRequiredRaceListCheck = CMSecurity.getAnyFlagEnabledParms("CHARCLASS_"+ID());
		if(CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,"All")
		||CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,R.ID())
		||CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,R.name())
		||CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,R.racialCategory()))
			secondCheck = true;
		else
		if(CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,"-"+R.ID())
		||CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,"-"+R.name())
		||CMStrings.containsIgnoreCase(overrideRequiredRaceListCheck,"-"+R.racialCategory()))
			secondCheck=false;
		else
			secondCheck = 
				(CMStrings.containsIgnoreCase(requiredRaceList,"All")
				||CMStrings.containsIgnoreCase(requiredRaceList,R.ID())
				||CMStrings.containsIgnoreCase(requiredRaceList,R.name())
				||CMStrings.containsIgnoreCase(requiredRaceList,R.racialCategory()))
			&&(!CMStrings.containsIgnoreCase(requiredRaceList,"-"+R.ID()))
			&&(!CMStrings.containsIgnoreCase(requiredRaceList,"-"+R.name()))
			&&(!CMStrings.containsIgnoreCase(requiredRaceList,"-"+R.racialCategory()));

		synchronized(finalAllowedRaceSet)
		{
			finalAllowedRaceSet.put(R, Boolean.valueOf(secondCheck));
		}
		return secondCheck;
	}

	@Override
	public CharClass makeGenCharClass()
	{
		if(isGeneric())
			return this;
		final CharClass CR=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
		CR.setClassParms("<CCLASS><ID>"+ID()+"</ID><NAME>"+name()+"</NAME></CCLASS>");
		CR.setStat("BASE",baseClass());
		CR.setStat("HITPOINTSFORMULA",""+getHitPointsFormula());
		CR.setStat("MANAFORMULA",""+getManaFormula());
		CR.setStat("LVLPRAC",""+getBonusPracLevel());
		CR.setStat("MOVEMENTFORMULA",""+getMovementFormula());
		CR.setStat("RACQUAL", CMParms.toListString(getRequiredRaceList()));
		CR.setStat("LVLATT",""+getBonusAttackLevel());
		CR.setStat("ATTATT",""+getAttackAttribute());
		CR.setStat("FSTTRAN",""+getTrainsFirstLevel());
		CR.setStat("FSTPRAC",""+getPracsFirstLevel());
		CR.setStat("LVLDAM",""+getLevelsPerBonusDamage());
		CR.setStat("ARMOR",""+allowedArmorLevel());
		//CR.setStat("STRWEAP",""+this.allowedArmorLevel());
		//CR.setStat("STRARM",""+this.allowedArmorLevel());
		CR.setStat("STRLMT",""+getOtherLimitsDesc());
		CR.setStat("STRBON",""+getOtherBonusDesc());
		CR.setStat("PLAYER",""+availabilityCode());
		CR.setStat("HELP",""+CMLib.help().getHelpText(name(),null,false));
		CR.setStat("MAXNCS",""+maxNonCraftingSkills());
		CR.setStat("MAXCRS",""+maxCraftingSkills());
		CR.setStat("MONEY", getStartingMoney());
		CR.setStat("MAXCMS",""+maxCommonSkills());
		CR.setStat("SUBRUL", ""+getSubClassRule().toString());
		CR.setStat("MAXLGS",""+maxLanguages());
		CR.setStat("NUMMINSTATS", ""+getMinimumStatRequirements().length);
		for(int p=0;p<getMinimumStatRequirements().length;p++)
		{
			final Pair<String,Integer> P=getMinimumStatRequirements()[p];
			CR.setStat("GETMINSTAT"+p,P.first);
			CR.setStat("GETSTATMIN"+p,P.second.toString());
		}

		CR.setStat("QUAL","");

		final MOB fakeMOB=CMClass.getFactoryMOB();
		fakeMOB.baseCharStats().setMyClasses(ID());
		fakeMOB.baseCharStats().setMyLevels("0");
		fakeMOB.recoverCharStats();

		final PhyStats RS=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		RS.setAllValues(0);
		affectPhyStats(fakeMOB,RS);
		RS.setRejuv(PhyStats.NO_REJUV);
		CR.setStat("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(RS));

		final CharStats S1=(CharStats)CMClass.getCommon("DefaultCharStats");
		S1.setMyClasses(ID());
		S1.setMyLevels("0");
		S1.setAllValues(0);
		final CharStats S2=(CharStats)CMClass.getCommon("DefaultCharStats");
		S2.setAllValues(10);
		S2.setMyClasses(ID());
		S2.setMyLevels("0");
		final CharStats S3=(CharStats)CMClass.getCommon("DefaultCharStats");
		S3.setAllValues(11);
		S3.setMyClasses(ID());
		S3.setMyLevels("0");
		final CharStats SETSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		SETSTAT.setAllValues(0);
		final CharStats ADJSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		ADJSTAT.setAllValues(0);
		affectCharStats(fakeMOB,S1);
		affectCharStats(fakeMOB,S2);
		affectCharStats(fakeMOB,S3);
		for(final int i: CharStats.CODES.ALLCODES())
		{
			if(i!=CharStats.STAT_AGE)
			{
				if(CharStats.CODES.isBASE(i))
				{
					if((S2.getStat(i)==S3.getStat(i))
					&&(S1.getStat(CharStats.CODES.toMAXBASE(i))!=0))
					{
						SETSTAT.setStat(i,S2.getStat(i));
						S1.setStat(CharStats.CODES.toMAXBASE(i),0);
						S2.setStat(CharStats.CODES.toMAXBASE(i),0);
						S3.setStat(CharStats.CODES.toMAXBASE(i),0);
					}
					else
						ADJSTAT.setStat(i,S1.getStat(i));
				}
				else
					ADJSTAT.setStat(i,S1.getStat(i));
			}
		}
		CR.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT));
		CR.setStat("CSTATS",CMLib.coffeeMaker().getCharStatsStr(SETSTAT));

		final CharState CS=(CharState)CMClass.getCommon("DefaultCharState"); CS.setAllValues(0);
		affectCharState(fakeMOB,CS);
		CR.setStat("ASTATE",CMLib.coffeeMaker().getCharStateStr(CS));

		final List<AbilityMapper.AbilityMapping> data1=CMLib.ableMapper().getUpToLevelListings(ID(),Integer.MAX_VALUE,true,false);
		if(data1.size()>0)
			CR.setStat("NUMCABLE",""+data1.size());
		else
			CR.setStat("NUMCABLE","");
		for(int i=0;i<data1.size();i++)
		{
			final AbilityMapper.AbilityMapping able = data1.get(i);
			CR.setStat("GETCABLELVL"+i,Integer.toString(able.qualLevel()));
			CR.setStat("GETCABLEPROF"+i,Integer.toString(able.defaultProficiency()));
			CR.setStat("GETCABLEGAIN"+i,Boolean.toString(able.autoGain()));
			CR.setStat("GETCABLESECR"+i,Boolean.toString(able.isSecret()));
			CR.setStat("GETCABLEPARM"+i,able.defaultParm());
			CR.setStat("GETCABLEPREQ"+i,able.originalSkillPreReqList());
			CR.setStat("GETCABLEMASK"+i,able.extraMask()==null?"":able.extraMask());
			CR.setStat("GETCABLEMAXP"+i,Integer.toString(able.maxProficiency()));
			// GETCABLE -- MUST BE LAST --
			CR.setStat("GETCABLE"+i,able.abilityID());
		}

		Set<Integer> H=disallowedWeaponClasses(null);
		if((H==null)||(H.size()==0))
			CR.setStat("NUMWEAP","");
		else
		{
			CR.setStat("NUMWEAP",""+H.size());
			CR.setStat("GETWEAP",""+CMParms.toListString(H));
		}

		List<Item> outfit=outfit(null);
		if(outfit==null)
			outfit=new Vector<Item>();
		CR.setStat("NUMOFT",""+outfit.size());
		for(int i=0;i<outfit.size();i++)
			CR.setStat("GETOFTID"+i,outfit.get(i).ID());
		for(int i=0;i<outfit.size();i++)
			CR.setStat("GETOFTPARM"+i,outfit.get(i).text());

		CR.setStat("HITPOINTSFORMULA",""+getHitPointsFormula());
		CR.setStat("MANAFORMULA",""+getManaFormula());
		CR.setStat("MOVEMENTFORMULA",""+getMovementFormula());
		CR.setStat("LEVELCAP",""+getLevelCap());
		CR.setStat("DISFLAGS",""+((raceless()?CharClass.GENFLAG_NORACE:0)
								|(leveless()?CharClass.GENFLAG_NOLEVELS:0)
								|(expless()?CharClass.GENFLAG_NOEXP:0)
								|(showThinQualifyList()?CharClass.GENFLAG_THINQUALLIST:0)));
		//CharState STARTCS=(CharState)CMClass.getCommon("DefaultCharState"); STARTCS.setAllValues(0);
		//this.startCharacter(mob,isBorrowedClass,verifyOnly)
		//CR.setStat("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(STARTCS));
		final String[] names=nameSet();
		final List<List<String>> securitySets=new Vector<List<String>>();
		final List<Integer> securityLvls=new Vector<Integer>();
		CR.setStat("NUMNAME",""+names.length);
		for(int n=0;n<names.length;n++)
			CR.nameSet()[n]=names[n];
		final int[] lvls=new int[names.length];
		int nameDex=0;
		final List<String> firstSet=CMParms.parseSemicolons(getSecurityFlags(0).toString(';'),true);
		final Vector<String> cumulativeSet=new Vector<String>();
		cumulativeSet.addAll(firstSet);
		securitySets.add(firstSet);
		securityLvls.add(Integer.valueOf(0));
		for(int x=1;x<20000;x++)
		{
			if(!this.name(x).equals(names[nameDex]))
			{
				nameDex++;
				if(nameDex>=names.length)
					break;
				lvls[nameDex]=x;
			}
			if(getSecurityFlags(x).size()!=cumulativeSet.size())
			{
				final List<String> V=new Vector<String>();
				V.addAll(CMParms.parseSemicolons(getSecurityFlags(x).toString(';'),true));
				for(int i=0;i<cumulativeSet.size();i++)
					V.remove(cumulativeSet.elementAt(i));
				securitySets.add(V);
				securityLvls.add(Integer.valueOf(x));
				cumulativeSet.addAll(V);
			}
		}
		for(int l=0;l<lvls.length;l++)
			CR.setStat("NAMELEVEL"+l,""+lvls[l]);
		if((securitySets.size()==1)
		&&(securitySets.get(0).size()==0))
		{
			securitySets.clear();
			securityLvls.clear();
		}
		CR.setStat("NUMSSET",""+securitySets.size());
		for(int s=0;s<securitySets.size();s++)
		{
			CR.setStat("SSET"+s,CMParms.combine(securitySets.get(s),0));
			CR.setStat("SSETLEVEL"+s,""+securityLvls.get(s).intValue());
		}
		H=requiredWeaponMaterials();
		if((H==null)||(H.size()==0))
			CR.setStat("NUMWMAT","");
		else
		{
			CR.setStat("NUMWMAT",""+H.size());
			CR.setStat("GETWMAT",""+CMParms.toListString(H));
		}
		H=disallowedWeaponClasses(fakeMOB);
		if((H==null)||(H.size()==0))
			CR.setStat("NUMWEP","");
		else
		{
			CR.setStat("NUMWEP",""+H.size());
			CR.setStat("GETWEP",""+CMParms.toListString(H));
		}

		CR.setStat("ARMORMINOR",""+requiredArmorSourceMinor());
		CR.setStat("STATCLASS",this.getClass().getName());
		CR.setStat("EVENTCLASS",this.getClass().getName());
		fakeMOB.destroy();
		return CR;
	}

	@Override
	public void endCharacter(MOB mob)
	{
	}
	
	@Override
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			mob.setPractices(mob.getPractices()+getPracsFirstLevel());
			mob.setTrains(mob.getTrains()+getTrainsFirstLevel());
			grantAbilities(mob,isBorrowedClass);
			CMLib.achievements().grantAbilitiesAndExpertises(mob);
		}
		if((mob!=null) && (CMSecurity.isASysOp(mob) || CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.ALLSKILLS)))
		{
			final PlayerStats pStats = mob.playerStats();
			if((pStats != null)&&(!pStats.isOnAutoInvokeList("ANYTHING")))
			{
				for(String s : CMProps.getListFileVarSet(CMProps.ListFile.WIZ_NOAUTOINVOKE))
					pStats.addAutoInvokeList(s);
			}
		}
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		return outfitChoices;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{

	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(affectableStats.getCurrentClass().ID().equals(ID()))
		for(final int i: CharStats.CODES.MAXCODES())
			affectableStats.setStat(i,affectableStats.getStat(i)+maxStatAdjustments()[i]+maxStatAdjustments()[CharStats.CODES.toMAXBASE(i)]);
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==myHost)
		&&(!msg.source().isMonster())
		&&(msg.source().charStats().getCurrentClass()==this)) // this is important because of event buddies and dup checks
		{
			if(!armorCheck(msg.source(),msg.sourceCode(),msg.tool()))
			{
				if(msg.tool()==null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(armorFailMessage(),"<SKILL>",L("maneuver")));
				else
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(armorFailMessage(),"<SKILL>",msg.tool().name()+L(" attempt")));
				return false;
			}
			if(!weaponCheck(msg.source(),msg.sourceCode(),msg.tool()))
				return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==myHost)
		&&(msg.target() instanceof Item)
		&&(msg.source().charStats().getCurrentClass()==this) // this is important because of event buddies and dup checks
		&&(!msg.source().isMonster()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WIELD:
			{
				if((msg.target() instanceof Weapon)
				&&(((requiredWeaponMaterials()!=null)
						&&(!requiredWeaponMaterials().contains(Integer.valueOf(((Weapon)msg.target()).material()&RawMaterial.MATERIAL_MASK))))
					||((disallowedWeaponClasses(msg.source())!=null)
						&&(disallowedWeaponClasses(msg.source()).contains(Integer.valueOf(((Weapon)msg.target()).weaponClassification()))))))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.TYP_OK_VISUAL,L("<T-NAME> feel(s) a bit strange in your hands."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				break;
			}
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_HOLD:
			{
				if(!CMLib.utensils().armorCheck(msg.source(),(Item)msg.target(),allowedArmorLevel()))
				{
					final String choice=CMProps.getAnyListFileValue(CMProps.ListFile.ARMOR_MISFITS);
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.TYP_OK_VISUAL,choice,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				}
				break;
			}
			default:
				break;
			}
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void unLevel(MOB mob)
	{
	}

	@Override
	public void level(MOB mob, List<String> gainedAbilityIDs)
	{
	}

	@Override
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount)
	{
		return amount;
	}
	
	/**
	 * Localize an internal string -- shortcut. Same as calling:
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary#fullSessionTranslation(String, String...)
	 * Call with the string to translate, which may contain variables of the form @x1, @x2, etc. The array in xs
	 * is then used to replace the variables AFTER the string is translated.
	 * @param str the string to translate
	 * @param xs the array of variables to replace
	 * @return the translated string, with all variables in place
	 */
	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public boolean canBeADivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return true;
	}

	@Override
	public boolean canBeABenificiary(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return true;
	}

	@Override
	public boolean isValidClassDivider(MOB killer, MOB killed, final MOB mob, Set<MOB> followers)
	{
		if(mob != null)
		{
			if(!mob.charStats().getCurrentClass().canBeADivider(killer, killed, mob, followers))
				return false;
			if((mob!=killed)
			&&(!mob.amDead())
			&&((mob.getVictim()==killed)
			 ||(followers.contains(mob))
			 ||(mob==killer)))
				return true;
		}
		return false;
	}

	@Override
	public boolean isValidClassBeneficiary(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		if(mob != null)
		{
			if(!mob.charStats().getCurrentClass().canBeABenificiary(killer, killed, mob, followers))
				return false;
			if((mob!=killed)
			&&(!mob.amDead())
			&&((mob.getVictim()==killed)
			 ||(followers.contains(mob))
			 ||(mob==killer)))
				return true;
		}
		return false;
	}

	@Override
	public String classParms()
	{
		return "";
	}

	@Override
	public void setClassParms(String parms)
	{
	}

	protected static String[]	CODES	= { "CLASS", "PARMS" };

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public String getStat(String code)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + classParms();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setClassParms(val);
			break;
		}
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	public boolean sameAs(CharClass E)
	{
		if (!(E instanceof StdCharClass))
			return false;
		for (int i = 0; i < CODES.length; i++)
		{
			if (!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}
}
