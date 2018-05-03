package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

import java.lang.ref.WeakReference;
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
public class StdRace implements Race
{
	@Override
	public String ID()
	{
		return "StdRace";
	}

	protected static final int[] breatheAnythingArray	= new int[0];
	protected static final int[] breatheAirArray		= new int[] { RawMaterial.RESOURCE_AIR };
	protected static final int[] breatheWaterArray		= new int[] { RawMaterial.RESOURCE_FRESHWATER, RawMaterial.RESOURCE_SALTWATER };
	protected static final int[] breatheAirWaterArray	= new int[] { RawMaterial.RESOURCE_FRESHWATER, RawMaterial.RESOURCE_SALTWATER, RawMaterial.RESOURCE_AIR };
	protected static final List empty					= new ReadOnlyVector(1);
	//  												   an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] 	parts					= {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	protected static final SearchIDList emptyIDs = new CMUniqSortSVec(1);

	private final int[]		agingChart				= { 0, 1, 3, 15, 35, 53, 70, 74, 78 };
	protected String		baseStatChgDesc			= null;
	protected String		sensesChgDesc			= null;
	protected String		dispChgDesc				= null;
	protected String		abilitiesDesc			= null;
	protected String		languagesDesc			= null;
	protected Weapon		naturalWeapon			= null;
	protected boolean		mappedCulturalAbilities	= false;
	protected List<Item>	outfitChoices			= null;
	protected List<Weapon>	naturalWeaponChoices	= null;
	protected Set<String>	naturalAbilImmunities	= new HashSet<String>();
	protected int			usageCount				= 0;
	
	protected Map<Integer,SearchIDList<Ability>> racialAbilityMap	= null;
	protected Map<Integer,SearchIDList<Ability>> racialEffectMap	= null;

	private final static String localizedStaticName = CMLib.lang().L("StdRace");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 24;
	}

	@Override
	public int shortestFemale()
	{
		return 24;
	}

	@Override
	public int heightVariance()
	{
		return 5;
	}

	@Override
	public int lightestWeight()
	{
		return 60;
	}

	@Override
	public int weightVariance()
	{
		return 10;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Unknown");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public boolean classless()
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
	public int getXPAdjustment()
	{
		return 0;
	}

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAirArray;
	}

	@Override
	public boolean useRideClass()
	{
		return false;
	}

	protected int practicesAtFirstLevel()
	{
		return 0;
	}

	protected int trainsAtFirstLevel()
	{
		return 0;
	}

	protected String[] racialEffectNames()
	{
		return null;
	}

	protected int[] racialEffectLevels()
	{
		return null;
	}

	protected String[] racialEffectParms()
	{
		return null;
	}

	protected String[] racialAbilityNames()
	{
		return null;
	}

	protected String[] racialAbilityParms()
	{
		return null;
	}

	protected int[] racialAbilityLevels()
	{
		return null;
	}

	protected int[] racialAbilityProficiencies()
	{
		return null;
	}

	protected boolean[] racialAbilityQuals()
	{
		return null;
	}

	protected String[] culturalAbilityNames()
	{
		return null;
	}

	protected int[] culturalAbilityLevels()
	{
		return null;
	}

	protected boolean[] culturalAbilityAutoGains()
	{
		return null;
	}

	protected int[] culturalAbilityProficiencies()
	{
		return null;
	}

	@Override
	public String[] abilityImmunities()
	{
		return CMParms.toStringArray(this.naturalAbilImmunities);
	}

	protected boolean uncharmable()
	{
		return false;
	}

	protected boolean destroyBodyAfterUse()
	{
		return false;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public CMObject newInstance()
	{
		return this;
	}

	@Override
	public void initializeClass()
	{
	}
	
	public boolean fertile()
	{
		return true;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdRace E=(StdRace)this.clone();
			return E;
		}
		catch(final CloneNotSupportedException e)
		{
			return this;
		}
	}

	public Race healthBuddy()
	{
		return this;
	}

	@Override
	public boolean canBreedWith(Race R)
	{
		if(!fertile())
			return false;
		if(R==null)
			return false;
		if(ID().equals("Human")||R.ID().equals("Human"))
			return true;
		return ID().equals(R.ID());
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{

	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return L("baby boy @x1", name().toLowerCase());
				case 'F':
				case 'f':
					return L("baby girl @x1", name().toLowerCase());
				default:
					return L("baby @x1", name().toLowerCase());
				}
			}
			case Race.AGE_TODDLER:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return L("baby boy @x1", name().toLowerCase());
				case 'F':
				case 'f':
					return L("baby girl @x1", name().toLowerCase());
				default:
					return L("baby @x1", name().toLowerCase());
				}
			}
			case Race.AGE_CHILD:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return L("little boy @x1", name().toLowerCase());
				case 'F':
				case 'f':
					return L("little girl @x1", name().toLowerCase());
				default:
					return L("young @x1", name().toLowerCase());
				}
			}
			case Race.AGE_YOUNGADULT:
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return L("male @x1", name().toLowerCase());
				case 'F':
				case 'f':
					return L("female @x1", name().toLowerCase());
				default:
					return name().toLowerCase();
				}
			}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
			{
				switch(gender)
				{
				case 'M':
				case 'm':
					return L("old male @x1",name().toLowerCase());
				case 'F':
				case 'f':
					return L("old female @x1",name().toLowerCase());
				default:
					return L("old @x1",name().toLowerCase());
				}
			}
		}
	}

	@Override
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats)
	{
		if((baseStats.getStat(CharStats.STAT_AGE)>0)
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMMORT))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING)))
		{
			switch(baseStats.ageCategory())
			{
				case -1: break;
				case Race.AGE_INFANT:
				case Race.AGE_TODDLER:
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)-10);
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-2);
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-4);
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-4);
					break;
				case Race.AGE_CHILD:
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)-5);
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-1);
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-2);
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-2);
					break;
				case Race.AGE_YOUNGADULT:
				case Race.AGE_MATURE:
					break;
				case Race.AGE_MIDDLEAGED:
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+5);
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-1);
					charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-1);
					charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-1);
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+1);
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+1);
					charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+1);
					break;
				case Race.AGE_OLD:
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+10);
					charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-5);
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-2);
					charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-2);
					charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-2);
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+2);
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+2);
					charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+2);
					break;
				case Race.AGE_VENERABLE:
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+15);
					charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-25);
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-3);
					charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-3);
					charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-3);
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+3);
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+3);
					charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+3);
					break;
				case Race.AGE_ANCIENT:
				{
					final int[] chart=getAgingChart();
					final int diff=chart[Race.AGE_ANCIENT]-chart[Race.AGE_VENERABLE];
					final int age=baseStats.getStat(CharStats.STAT_AGE)-chart[Race.AGE_ANCIENT];
					int num=(diff>0)?(int)Math.abs(Math.floor(CMath.div(age,diff)))-1:1;
					if(num==0)
						num=1;
					if(num>16)
						num=16;
					charStats.setStat(CharStats.STAT_SAVE_MIND,charStats.getStat(CharStats.STAT_SAVE_MIND)+20+(5*num));
					charStats.setStat(CharStats.STAT_SAVE_UNDEAD,charStats.getStat(CharStats.STAT_SAVE_UNDEAD)-50+15+(5*num));
					charStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,charStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-(3+(1*num)));
					charStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,charStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-(3+(num)));
					charStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,charStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-(3+(num)));
					charStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,charStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+(3+(num)));
					charStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,charStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)+(3+(num)));
					charStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,charStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)+(3+(num)));
					break;
				}
			}
		}
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==myHost)
		&&(msg.tool() instanceof Ability)
		&&(myHost instanceof MOB))
		{
			if(uncharmable()
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
			{
				msg.source().location().show(msg.source(),myHost,CMMsg.MSG_OK_VISUAL,L("<T-NAME> seem(s) unaffected by the charm magic from <S-NAMESELF>."));
				return false;
			}
			else
			if(naturalAbilImmunities.contains(msg.tool().ID()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		// the sex rules
		if(!(myHost instanceof MOB))
			return;

		final MOB myChar=(MOB)myHost;
		if((msg.source()==myChar)
		&&(msg.sourceMinor()==CMMsg.TYP_EXPCHANGE) // race gets execmsg before expchange application, so this works.
		&&(this.getXPAdjustment()!=0)
		&&(msg.value()!=0))
			msg.setValue(msg.value() + (int)Math.round(msg.value() * CMath.div(getXPAdjustment(), 100.0)));

		if((msg.tool() instanceof Social)
		&&(msg.amITarget(myChar)||(msg.source()==myChar))
		&&(myChar.location()==msg.source().location())
		&&(msg.sourceMinor()!=CMMsg.TYP_CHANNEL)
		&&(msg.tool().Name().startsWith("MATE ")
			||msg.tool().Name().startsWith("SEX ")))
		{
			if(msg.tool().Name().endsWith("SELF"))
			{
				if((msg.source()==myChar)
				&&(fertile())
				&&(msg.source().fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0))
				{
					if(msg.source().maxState().getFatigue()>Long.MIN_VALUE/2)
						msg.source().curState().adjFatigue(CharState.FATIGUED_MILLIS,msg.source().maxState());
					if(myChar.maxState().getFatigue()>Long.MIN_VALUE/2)
						myChar.curState().adjFatigue(CharState.FATIGUED_MILLIS,myChar.maxState());
					final Ability A=CMClass.getAbility("Spell_Blindness");
					if(A!=null)
						A.invoke(myChar,myChar,true,myChar.phyStats().level());
				}
			}
			else
			if((msg.target()==myChar)
			&&(msg.tool().Name().endsWith("<T-NAME>")))
			{
				final boolean srcExhausted=((msg.source().curState().getMovement()<(msg.source().maxState().getMovement()/2))
						||(msg.source().curState().getFatigue()>=CharState.FATIGUED_MILLIS));
				final boolean meExhausted=((myChar.curState().getMovement()<(myChar.maxState().getMovement()/2))
						||(myChar.curState().getFatigue()>=CharState.FATIGUED_MILLIS));
				if((myChar.charStats().getStat(CharStats.STAT_GENDER)==('F'))
				&&(msg.source().charStats().getStat(CharStats.STAT_GENDER)==('M'))
				&&(myChar.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
				&&(msg.source().fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
				&&(msg.source().charStats().getMyRace().canBreedWith(this))
				&&((msg.source().charStats().getStat(CharStats.STAT_AGE)==0)
						||((msg.source().charStats().ageCategory()>Race.AGE_CHILD)
								&&(msg.source().charStats().ageCategory()<Race.AGE_OLD)))
				&&((myChar.charStats().getStat(CharStats.STAT_AGE)==0)
						||((myChar.charStats().ageCategory()>Race.AGE_CHILD)
								&&(myChar.charStats().ageCategory()<Race.AGE_OLD))))
				{
					if(srcExhausted)
						msg.source().tell(L("You are exhausted!"));
					else
					{
						if(msg.source().maxState().getFatigue()>Long.MIN_VALUE/2)
							msg.source().curState().adjFatigue(CharState.FATIGUED_MILLIS,msg.source().maxState());
						msg.source().curState().adjMovement(-msg.source().maxState().getMovement()/2, msg.source().maxState());
					}
					if(meExhausted)
						myChar.tell(L("You are exhausted!"));
					else
					{
						if(myChar.maxState().getFatigue()>Long.MIN_VALUE/2)
							myChar.curState().adjFatigue(CharState.FATIGUED_MILLIS,myChar.maxState());
						myChar.curState().adjMovement(-myChar.maxState().getMovement()/2, myChar.maxState());
					}
					if(!srcExhausted && !meExhausted && (CMLib.dice().rollPercentage()<10))
					{
						final Ability A=CMClass.getAbility("Pregnancy");
						if((A!=null)
						&&(myChar.fetchAbility(A.ID())==null)
						&&(myChar.fetchEffect(A.ID())==null))
							A.invoke(msg.source(),myChar,true,0);
					}
				}
			}
		}
	}

	@Override
	public String arriveStr()
	{
		return "arrives";
	}

	@Override
	public String leaveStr()
	{
		return "leaves";
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

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected boolean giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedRace)
	{
		return giveMobAbility(mob,A,proficiency,defaultParm,isBorrowedRace,true);
	}

	protected boolean giveMobAbility(MOB mob, Ability A, int proficiency, String defaultParm, boolean isBorrowedRace, boolean autoInvoke)
	{
		if(mob.fetchAbility(A.ID())==null)
		{
			A=(Ability)A.copyOf();
			A.setSavable(!isBorrowedRace);
			A.setProficiency(proficiency);
			A.setMiscText(defaultParm);
			mob.addAbility(A);
			if(autoInvoke)
			{
				A.autoInvocation(mob, false);
				final boolean isChild=CMLib.flags().isChild(mob);
				final boolean isAnimal=CMLib.flags().isAnimalIntelligence(mob);
				final boolean isMonster=mob.isMonster();
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
				&&(isMonster)
				&&(!isChild))
				{
					if(A.proficiency()>0)
						A.setProficiency(100);
					A.invoke(mob,mob,false,0);
					if(isChild && (!isAnimal))
					{
						A=mob.fetchAbility("Common");
						if(A==null)
						{
							A=CMClass.getAbility("Common");
							if(A!=null)
								mob.addAbility(A);
						}
						if(A!=null)
							A.setProficiency(100);
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedRace)
	{
		grantAbilities(mob,isBorrowedRace,false);
	}
	
	protected void grantAbilities(MOB mob, boolean isBorrowedRace, boolean skipCultural)
	{
		final String[] alreadyAbilitied = (racialAbilityNames()==null)?new String[0]:racialAbilityNames();
		final Map<String,AbilityMapping> ableMap=CMLib.ableMapper().getAbleMapping(ID());
		for(final Map.Entry<String, AbilityMapping> entry : ableMap.entrySet())
		{
			final AbilityMapping mapping = entry.getValue();
			if((mapping != null)
			&&(mapping.qualLevel()>=0)
			&&((mapping.qualLevel()<=mob.phyStats().level())||(mapping.qualLevel()<=1))
			&&(mapping.autoGain()))
			{
				if(CMParms.contains(alreadyAbilitied, mapping.abilityID()))
				{
					final Ability A=mob.fetchAbility(mapping.abilityID());
					if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					{
						A.setProficiency(mapping.defaultProficiency());
						A.autoInvocation(mob, false);
					}
				}
				else
				if(!skipCultural)
				{
					final Ability A=CMClass.getAbility(mapping.abilityID());
					if(A!=null)
					{
						final String extraMask=mapping.extraMask();
						if((extraMask==null)
						||(extraMask.length()==0)
						||(CMLib.masking().maskCheck(extraMask,mob,true)))
						{
							giveMobAbility(mob,A,mapping.defaultProficiency(),mapping.defaultParm(),isBorrowedRace);
						}
					}
					else
					if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
						Log.errOut("Race "+ID()+" has unknown cultural ability "+mapping.abilityID());
				}
			}
		}
	}
	
	protected void mapCulturalAbilities()
	{
		if((!mappedCulturalAbilities)
		&&(culturalAbilityNames()!=null))
		{
			for(int a=0;a<culturalAbilityNames().length;a++)
			{
				final int lvl = (culturalAbilityLevels() != null) ? culturalAbilityLevels()[a] : 0;
				boolean gain = true;
				if((culturalAbilityAutoGains() != null) && (culturalAbilityAutoGains().length>a))
					gain = culturalAbilityAutoGains()[a];
				int prof = 0;
				if((culturalAbilityProficiencies() != null) && (culturalAbilityProficiencies().length>a))
					prof = culturalAbilityProficiencies()[a];
				
				CMLib.ableMapper().addCharAbilityMapping(ID(),lvl,culturalAbilityNames()[a],prof,"",gain);
			}
			mappedCulturalAbilities=true;
		}
	}

	@Override
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		if(CMProps.getBoolVar(CMProps.Bool.POPULATIONSTARTED))
			usageCount++;

		// racialAbilities() call, if necc, will delete ALL mappings
		for(final Ability A : racialAbilities(mob))
		{
			if(A!=null)
			{
				if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
				{
					A.autoInvocation(mob, false);
					A.invoke(mob,mob,false,0);
					break;
				}
			}
		}
		mapCulturalAbilities();
		if(!verifyOnly)
		{
			if(mob.basePhyStats().level()<=1)
			{
				mob.setPractices(mob.getPractices()+practicesAtFirstLevel());
				mob.setTrains(mob.getTrains()+trainsAtFirstLevel());
			}
			setHeightWeight(mob.basePhyStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));
			grantAbilities(mob,false);
		}
		else
		{
			grantAbilities(mob,false,true);
		}
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
			naturalWeapon=CMClass.getWeapon("Natural");
		return naturalWeapon;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		return outfitChoices;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		return CMLib.combat().standardMobCondition(viewer,mob);
	}

	protected Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector<Weapon>();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				if(naturalWeapon==null)
					continue;
				naturalWeapon.setMaterial(RawMaterial.RESOURCE_LEATHER);
				switch (i)
				{
				case 1:
				case 2:
				case 3:
					naturalWeapon.setName(L("a quick punch"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 4:
					naturalWeapon.setName(L("fingernails and teeth"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
					break;
				case 5:
					naturalWeapon.setName(L("an elbow"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 6:
					naturalWeapon.setName(L("a backhand"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 7:
					naturalWeapon.setName(L("a strong jab"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 8:
					naturalWeapon.setName(L("a stinging punch"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 9:
					if(bodyMask()[Race.BODY_LEG]>0)
						naturalWeapon.setName(L("a knee"));
					else
					if(bodyMask()[Race.BODY_GILL]>0)
						naturalWeapon.setName(L("a fin"));
					else
						naturalWeapon.setName(L("a limb"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 10:
					naturalWeapon.setName(L("a head butt"));
					naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeapon.setUsesRemaining(1000);
				naturalWeaponChoices.add(naturalWeapon);
			}
		}
		if(naturalWeaponChoices.size()>0)
			return naturalWeaponChoices.get(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
		return CMClass.getWeapon("Natural");
	}

	@Override
	public List<RawMaterial> myResources()
	{
		return new Vector<RawMaterial>();
	}

	@Override
	public void setHeightWeight(PhyStats stats, char gender)
	{
		int weightModifier=0;
		if(weightVariance()>0)
			weightModifier=CMLib.dice().roll(1,weightVariance(),0);
		stats.setWeight(lightestWeight()+weightModifier);
		int heightModifier=0;
		if(heightVariance()>0)
		{
			if(weightModifier>0)
			{
				final double variance=CMath.div(weightModifier,weightVariance());
				heightModifier=(int)Math.round(CMath.mul(heightVariance(),variance));
			}
			else
				heightModifier=CMLib.dice().roll(1,heightVariance(),0);
		}
		if (gender == 'M')
			stats.setHeight(shortestMale()+heightModifier);
		 else
			stats.setHeight(shortestFemale()+heightModifier);
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected RawMaterial makeResource(String name, int type)
	{
		final PhysicalAgent A = CMLib.materials().makeResource(type,ID(),true,name);
		if(A instanceof RawMaterial)
			return (RawMaterial)A;
		return null;
	}

	@Override
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		if(room==null)
			room=mob.location();

		final DeadBody bodyI=(DeadBody)CMClass.getItem("Corpse");
		if(mob.isMonster())
		{
			if((mob.amFollowing()!=null)
			&&((!mob.amFollowing().isMonster())||(!mob.amUltimatelyFollowing().isMonster())))
			{
				final MOB M=(MOB)mob.copyOf();
				M.setStartRoom(null);
				bodyI.setSavedMOB(M, false);
			}
			else
				bodyI.setSavedMOB(mob, true);
		}
		
		bodyI.setCharStats((CharStats)mob.baseCharStats().copyOf());
		bodyI.basePhyStats().setLevel(mob.basePhyStats().level());
		bodyI.basePhyStats().setWeight(mob.basePhyStats().weight());
		bodyI.setIsPlayerCorpse(!mob.isMonster());
		bodyI.setTimeOfDeath(System.currentTimeMillis());
		bodyI.setMobPKFlag(mob.isAttributeSet(MOB.Attrib.PLAYERKILL));
		bodyI.setName(L("the body of @x1",mob.Name().replace('\'','`')));
		bodyI.setMobName(mob.Name().replace('\'','`'));
		bodyI.setMobHash(mob.hashCode());
		bodyI.setMobDescription(mob.description().replace('\'','`'));
		bodyI.setDisplayText(L("the body of @x1 lies here.",mob.Name().replace('\'','`')));
		final Ability ageA=mob.fetchEffect("Age");
		if(ageA!=null)
			bodyI.addNonUninvokableEffect(ageA);
		if(room!=null)
		{
			final ItemPossessor.Expire expireCode;
			if(mob.isMonster() && (mob.playerStats()==null))
				expireCode=ItemPossessor.Expire.Monster_Body;
			else
				expireCode=ItemPossessor.Expire.Player_Body;
			room.addItem(bodyI,expireCode);
		}
		bodyI.setIsDestroyAfterLooting(destroyBodyAfterUse());
		bodyI.recoverPhyStats();
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof DiseaseAffect))
			{
				if((CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONSUMPTION))
				||(CMath.bset(((DiseaseAffect)A).spreadBitmap(),DiseaseAffect.SPREAD_CONTACT)))
					bodyI.addNonUninvokableEffect((Ability)A.copyOf());
			}
		}

		final List<Item> items=new ArrayList<Item>();
		final HashMap<Item,Container> containerMap=new HashMap<Item,Container>();
		final HashMap<Item,Container> itemMap=new HashMap<Item,Container>();
		final LinkedList<Item> itemsToGo=new LinkedList<Item>();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item thisItem=mob.getItem(i);
			if(thisItem != null)
				itemsToGo.add(thisItem);
		}
		for(Item thisItem : itemsToGo)
		{
			if(thisItem.isSavable() 
			|| (thisItem.fetchEffect("QuestBound")!=null)) // a quest-item drop must be preserved, even unsavable ones!
			{
				if(mob.isMonster())
				{
					Item newItem=CMLib.utensils().isRuinedLoot(mob,thisItem);
					if(newItem==null)
						continue;
					if(newItem==thisItem)
						newItem=(Item)thisItem.copyOf();
					if(newItem != null)
					{
						if(newItem instanceof Container)
							itemMap.put(thisItem,(Container)newItem);
						if(thisItem.container()!=null)
							containerMap.put(thisItem,thisItem.container());
						newItem.setContainer(null);
						newItem.setExpirationDate( System.currentTimeMillis() +
												   CMProps.getIntVar( CMProps.Int.EXPIRE_MONSTER_EQ )* TimeManager.MILI_HOUR );
						newItem.recoverPhyStats();
						thisItem=newItem;
					}
				}
				else
					mob.delItem(thisItem);
				thisItem.unWear();
				if(thisItem.container()==null)
					thisItem.setContainer(bodyI);
				if(room!=null)
					room.addItem(thisItem);
				items.add(thisItem);
			}
			else
			{
				mob.delItem(thisItem);
			}
		}
		itemsToGo.clear();

		final Item dropItem=CMLib.catalog().getDropItem(mob,false);
		if(dropItem!=null)
		{
			dropItem.unWear();
			if(dropItem.container()==null)
				dropItem.setContainer(bodyI);
			if(room!=null)
				room.addItem(dropItem);
			items.add(dropItem);
		}

		for(final Item oldItem : itemMap.keySet())
		{
			final Item newItem=itemMap.get(oldItem);
			final Item oldContainer=containerMap.get(oldItem);
			if((oldContainer!=null)&&(newItem!=null))
				newItem.setContainer(itemMap.get(oldContainer));
		}
		if(destroyBodyAfterUse())
		{
			for(Item I : myResources())
			{
				if(I!=null)
				{
					I=(Item)I.copyOf();
					I.setContainer(bodyI);
					if(room!=null)
						room.addItem(I,ItemPossessor.Expire.Monster_EQ);
				}
				else
				if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
					Log.errOut("Race "+ID()+" had NULL resource!");
			}
		}
		else
		if((room != null)&&(room.isContent(bodyI)))
		{
			// remove duplicate already looted corpses
			for(int i=0;i<room.numItems();i++)
			{
				final Item thisItem=mob.getItem(i);
				if((thisItem instanceof DeadBody)
				&&(thisItem.Name().equals(bodyI.Name()))
				&&(((DeadBody)thisItem).getContents().size()==0))
				{
					thisItem.destroy();
					break;
				}
			}
		}
		return bodyI;
	}

	@Override
	public int numRacialEffects(MOB mob)
	{
		return racialEffectsList(mob).size();
	}

	@Override
	public ChameleonList<Ability> racialEffects(final MOB mob)
	{
		final StdRace myRace = this;
		final List<Ability> myList=racialEffectsList(mob);
		final List<Ability> finalV=new Vector<Ability>(myList.size());
		for(final Ability A : myList)
		{
			Ability A2=(Ability)A.copyOf();
			A2.setMiscText(A.text());
			A2.makeNonUninvokable();
			A2.setSavable(false); // must come AFTER the above
			A2.setAffectedOne(mob);
			finalV.add(A2);
		}
		final ChameleonList<Ability> finalFinalV;
		if(mob==null)
		{
			finalFinalV = new ChameleonList<Ability>(finalV,
			new ChameleonList.Signaler<Ability>(myList)
			{
				@Override
				public boolean isDeprecated()
				{
					return false;
				}

				@Override
				public void rebuild(final ChameleonList<Ability> me)
				{
				}
			});
		}
		else
		{
			finalFinalV = new ChameleonList(finalV,
			new ChameleonList.Signaler<Ability>(myList)
			{
				@Override
				public boolean isDeprecated()
				{
					if((mob.amDestroyed())
					||(mob.charStats().getMyRace() != myRace)
					|| (racialEffectsList(mob) != oldReferenceListRef.get()))
						return true;
					return false;
				}

				@Override
				public void rebuild(final ChameleonList<Ability> me)
				{
					if(mob.amDestroyed())
						oldReferenceListRef=new WeakReference<List<Ability>>(empty);
					else
					{
						final ChameleonList<Ability> newList = mob.charStats().getMyRace().racialEffects(mob);
						me.changeMeInto(newList);
					}
				}
			});
		}
		return finalFinalV;
	}

	public final List<Ability> racialEffectsList(final MOB mob)
	{
		if(!CMClass.abilities().hasMoreElements())
			return empty;
		List<Ability> finalV = empty;
		final Integer level;
		if(mob!=null)
			level=Integer.valueOf(mob.phyStats().level());
		else
			level=Integer.valueOf(Integer.MAX_VALUE);
		if(racialEffectMap==null)
			racialEffectMap=new Hashtable<Integer,SearchIDList<Ability>>();
		if(racialEffectMap.containsKey(level) && (mob != null))
			return racialEffectMap.get(level);
		if((racialEffectNames()!=null)
		&&(racialEffectLevels()!=null)
		&&(racialEffectParms()!=null))
		{
			if(finalV == empty)
				finalV = new CMUniqSortSVec<Ability>();
			for(int v=0;v<racialEffectLevels().length;v++)
			{
				if(((racialEffectLevels()[v]<=level.intValue())||(racialEffectLevels()[v]<=1))
				&&(racialEffectNames().length>v)
				&&(racialEffectParms().length>v))
				{
					final Ability A=CMClass.getAbility(racialEffectNames()[v]);
					if(A!=null)
					{
						// mob was set to null here to make the cache map actually relevant .. see caching below
						A.setProficiency(CMLib.ableMapper().getMaxProficiency((MOB)null,true,A.ID()));
						A.setMiscText(racialEffectParms()[v]);
						A.makeNonUninvokable();
						A.setSavable(false); // must go AFTER the ablve
						finalV.add(A);
					}
					else
					if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
						Log.errOut("Race "+ID()+" has unknown affect "+racialEffectNames()[v]);
				}
			}
		}
		if(mob != null)
		{
			if(finalV == empty)
				finalV = new CMUniqSortSVec<Ability>();
			for(final Ability A : racialAbilities(mob))
			{
				if(A.isAutoInvoked())
				{
					final Ability A1=(Ability)A.copyOf();
					A1.makeNonUninvokable();
					A1.setSavable(false);
					A1.setInvoker(mob);
					A1.setStat("ISANAUTOEFFECT", "true");
					if(A1 instanceof Language)
						((Language) A1).setBeingSpoken(A1.ID(),true);
					finalV.add(A1);
				}
			}
			if((finalV != empty)&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				((CMUniqSortSVec)finalV).trimToSize();
				racialEffectMap.put(level, (CMUniqSortSVec)finalV);
			}
		}
		return finalV;
	}

	@Override
	public Race makeGenRace()
	{
		final Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+ID()+"</ID><NAME>"+name()+"</NAME></RACE>");
		GR.setStat("CAT",racialCategory());
		GR.setStat("BWEIGHT",""+lightestWeight());
		GR.setStat("VWEIGHT",""+weightVariance());
		GR.setStat("MHEIGHT",""+shortestMale());
		GR.setStat("FHEIGHT",""+shortestFemale());
		GR.setStat("WEAR",""+forbiddenWornBits());
		GR.setStat("AVAIL",""+availabilityCode());
		GR.setStat("VHEIGHT",""+heightVariance());
		GR.setStat("PLAYER",""+CMProps.getIntVar(CMProps.Int.MUDTHEME));
		GR.setStat("LEAVE",leaveStr());
		GR.setStat("ARRIVE",arriveStr());
		GR.setStat("HEALTHRACE",CMClass.classID(this));
		GR.setStat("EVENTRACE",CMClass.classID(this));
		GR.setStat("BODYKILL",""+destroyBodyAfterUse());
		GR.setStat("HELP",""+CMLib.help().getHelpText(name(),null,false));
		GR.setStat("AGING",CMParms.toListString(getAgingChart()));
		GR.setStat("XPADJ", ""+this.getXPAdjustment());
		GR.setStat("CANRIDE", ""+useRideClass());
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			GR.bodyMask()[i]=bodyMask()[i];

		Weapon W=myNaturalWeapon();
		final Weapon NW=CMClass.getWeapon("Natural");
		if((W!=null)&&(W!=NW))
		{
			if(!W.isGeneric())
			{
				final Weapon W2=CMClass.getWeapon("GenWeapon");
				W2.setName(W.name());
				W2.setWeaponClassification(W.weaponClassification());
				W2.setWeaponDamageType(W.weaponDamageType());
				W2.basePhyStats().setDamage(W.phyStats().damage());
				W2.basePhyStats().setAttackAdjustment(W.phyStats().attackAdjustment());
				W2.recoverPhyStats();
				W2.text();
				W=W2;
			}
			GR.setStat("WEAPONCLASS",W.ID());
			GR.setStat("WEAPONXML",W.text());
		}
		GR.setStat("WEAPONRACE",getClass().getName());

		final PhyStats RS=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		RS.setAllValues(0);
		
		final MOB fakeMOB=CMClass.getFactoryMOB();
		final Session fakeSession = (Session)CMClass.getCommon("DefaultSession");
		fakeMOB.setSession(fakeSession);
		affectPhyStats(fakeMOB,RS);
		RS.setRejuv(PhyStats.NO_REJUV);
		GR.setStat("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(RS));

		final CharStats S1=(CharStats)CMClass.getCommon("DefaultCharStats");
		S1.setAllValues(0);
		S1.setStat(CharStats.STAT_GENDER, 'M');
		
		final CharStats S2=(CharStats)CMClass.getCommon("DefaultCharStats");
		S2.setAllValues(10);
		S2.setStat(CharStats.STAT_GENDER, 'N');
		
		final CharStats S3=(CharStats)CMClass.getCommon("DefaultCharStats");
		S3.setAllValues(14);
		S3.setStat(CharStats.STAT_GENDER, 'F');
		
		final CharStats SETSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		SETSTAT.setAllValues(0);
		
		final CharStats ADJSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		ADJSTAT.setAllValues(0);
		affectCharStats(fakeMOB,S1);
		affectCharStats(fakeMOB,S2);
		affectCharStats(fakeMOB,S3);
		fakeMOB.setSession(null);
		for(final int i: CharStats.CODES.ALLCODES())
		{
			if(i==CharStats.STAT_GENDER)
			{
				if(S1.getStat(i)==S2.getStat(i))
					SETSTAT.setStat(i,S2.getStat(i));
			}
			else
			if(i!=CharStats.STAT_AGE)
			{
				if(CharStats.CODES.isBASE(i))
				{
					final int max = CharStats.CODES.toMAXBASE(i);
					if((Math.abs(S2.getStat(i)-10) != Math.abs(S3.getStat(i)-14))
					&&(S1.getStat(max)!=0))
					{
						SETSTAT.setStat(i,S2.getStat(i));
						S1.setStat(max,0);
						S2.setStat(max,0);
						S3.setStat(max,0);
					}
					else
						ADJSTAT.setStat(i,S1.getStat(i));
				}
				else
					ADJSTAT.setStat(i,S1.getStat(i));
			}
		}
		GR.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT));
		GR.setStat("CSTATS",CMLib.coffeeMaker().getCharStatsStr(SETSTAT));

		final CharState CS=(CharState)CMClass.getCommon("DefaultCharState"); CS.setAllValues(0);
		affectCharState(fakeMOB,CS);
		GR.setStat("ASTATE",CMLib.coffeeMaker().getCharStateStr(CS));

		//CharState STARTCS=(CharState)CMClass.getCommon("DefaultCharState"); STARTCS.setAllValues(0);
		//startRacing(fakeMOB,falsed);
		//GR.setStat("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(STARTCS));

		GR.setStat("DISFLAGS",""+((classless()?Race.GENFLAG_NOCLASS:0)
								|(leveless()?Race.GENFLAG_NOLEVELS:0)
								|(uncharmable()?Race.GENFLAG_NOCHARM:0)
								|(fertile()?0:Race.GENFLAG_NOFERTILE)
								|(expless()?Race.GENFLAG_NOEXP:0)));

		List<RawMaterial> rscs=myResources();
		if(rscs==null)
			rscs=new Vector<RawMaterial>();
		String txt=null;
		Item I;
		GR.setStat("NUMRSC",""+rscs.size());
		for(int i=0;i<rscs.size();i++)
		{
			I=rscs.get(i);
			if(I!=null)
			{
				I.recoverPhyStats();
				txt=I.text();
				GR.setStat("GETRSCID"+i,I.ID());
				GR.setStat("GETRSCPARM"+i,txt);
			}
			else
				Log.errOut("Race "+ID()+" had NULL resource!");
		}

		List<Item> outfit=outfit(null);
		if(outfit==null)
			outfit=new Vector<Item>();
		GR.setStat("NUMOFT",""+outfit.size());
		for(int i=0;i<outfit.size();i++)
			GR.setStat("GETOFTID"+i,outfit.get(i).ID());
		for(int i=0;i<outfit.size();i++)
			GR.setStat("GETOFTPARM"+i,outfit.get(i).text());

		GR.setStat("NUMRABLE","");
		if(racialAbilityNames()!=null)
		{
			GR.setStat("NUMRABLE",""+racialAbilityNames().length);
			for(int i=0;i<racialAbilityNames().length;i++)
			{
				GR.setStat("GETRABLE"+i,racialAbilityNames()[i]);
				GR.setStat("GETRABLELVL"+i,""+racialAbilityLevels()[i]);
				GR.setStat("GETRABLEQUAL"+i,""+racialAbilityQuals()[i]);
				GR.setStat("GETRABLEPROF"+i,""+racialAbilityProficiencies()[i]);
				GR.setStat("GETRABLEPARM"+i,""+racialAbilityParms()[i]);
			}
		}

		GR.setStat("NUMIABLE","");
		{
			int i=0;
			for(final String ableID : this.naturalAbilImmunities)
				GR.setStat("GETIABLE"+(i++),ableID);
		}

		GR.setStat("NUMCABLE","");
		if(culturalAbilityNames()!=null)
		{
			GR.setStat("NUMCABLE",""+culturalAbilityNames().length);
			for(int i=0;i<culturalAbilityNames().length;i++)
			{
				GR.setStat("GETCABLE"+i,culturalAbilityNames()[i]);
				GR.setStat("GETCABLEPROF"+i,""+culturalAbilityProficiencies()[i]);
				int lvl = (culturalAbilityLevels() != null) ? culturalAbilityLevels()[i] : 0;
				GR.setStat("GETCABLELVL"+i,""+lvl);
				boolean gain = (culturalAbilityAutoGains() != null) ? culturalAbilityAutoGains()[i] : true;
				GR.setStat("GETCABLEGAIN"+i,""+gain);
			}
		}

		GR.setStat("NUMREFF","");
		if(racialEffectNames()!=null)
		{
			GR.setStat("NUMREFF",""+racialEffectNames().length);
			for(int i=0;i<racialEffectNames().length;i++)
			{
				GR.setStat("GETREFF"+i,racialEffectNames()[i]);
				GR.setStat("GETREFFLVL"+i,""+racialEffectLevels()[i]);
				GR.setStat("GETREFFPARM"+i,racialEffectParms()[i]);
			}
		}
		fakeMOB.destroy();
		return GR;
	}

	@Override
	public Race mixRace(final Race race, final String newRaceID, final String newRaceName)
	{
		final Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+newRaceID+"</ID><NAME>"+newRaceName+"</NAME></RACE>");
		final Race race1 = (!isGeneric()) ? makeGenRace() :  this;
		final Race race2 = (!race.isGeneric()) ? race.makeGenRace() : race;
		final Race nonHuman=(race1.ID().equals("Human"))?race2:race1;
		final Race otherRace=(nonHuman==race1)?race2:race1;
		GR.setStat("CAT",nonHuman.racialCategory());
		GR.setStat("BWEIGHT",""+((race1.lightestWeight()+race2.lightestWeight())/2));
		GR.setStat("VWEIGHT",""+((race1.weightVariance()+race2.weightVariance())/2));
		GR.setStat("MHEIGHT",""+((race1.shortestMale()+race2.shortestMale())/2));
		GR.setStat("FHEIGHT",""+((race1.shortestFemale()+race2.shortestFemale())/2));
		GR.setStat("VHEIGHT",""+((race1.heightVariance()+race2.heightVariance())/2));
		GR.setStat("PLAYER",""+CMProps.getIntVar(CMProps.Int.MUDTHEME));
		GR.setStat("LEAVE",nonHuman.leaveStr());
		GR.setStat("AVAIL",""+(availabilityCode()|race.availabilityCode()|Area.THEME_SKILLONLYMASK));
		GR.setStat("ARRIVE",nonHuman.arriveStr());
		GR.setStat("HEALTHRACE",otherRace.getStat("HEALTHRACE"));
		GR.setStat("EVENTRACE",otherRace.getStat("EVENTRACE"));
		GR.setStat("WEAPONRACE",otherRace.getStat("WEAPONRACE"));
		final int[] aging=race1.getAgingChart().clone();
		for(int i=0;i<aging.length;i++)
		{
			if((aging[i]==Race.YEARS_AGE_LIVES_FOREVER)&&(race2.getAgingChart()[i]==Race.YEARS_AGE_LIVES_FOREVER))
			{
			}
			else
			if(((aging[i]==Race.YEARS_AGE_LIVES_FOREVER)&&(race2.getAgingChart()[i]==0))
			||((aging[i]==0)&&(race2.getAgingChart()[i]==Race.YEARS_AGE_LIVES_FOREVER)))
				aging[i]=0;
			else
			if((aging[i]==Race.YEARS_AGE_LIVES_FOREVER))
				aging[i]=race2.getAgingChart()[i]*2;
			else
			if((race2.getAgingChart()[i]==Race.YEARS_AGE_LIVES_FOREVER))
				aging[i]=aging[i]*2;
			else
			{
				aging[i]+=race2.getAgingChart()[i];
				aging[i]=aging[i]/2;
			}
		}
		for(int i=aging.length-2;i>=0;i--)
		{
			if(aging[i]>aging[i+1])
				aging[i]=aging[i+1];
		}

		final long race1worn=CMath.s_long(otherRace.getStat("WEAR"));
		final long race2worn=CMath.s_long(nonHuman.getStat("WEAR"));
		long finalWear=0;
		boolean toggle=false;
		for(final long wornCode : Wearable.CODES.ALL())
		{
			if(wornCode != Wearable.IN_INVENTORY)
			{
				if((!CMath.bset(race1worn,wornCode))&&(!CMath.bset(race2worn,wornCode)))
				{
				}
				else
				if(CMath.bset(race1worn,wornCode)&&CMath.bset(race2worn,wornCode))
					finalWear=finalWear|wornCode;
				else
				if(CMath.bset(race1worn,wornCode))
					finalWear=finalWear|wornCode;
				else
				if(toggle)
				{
					finalWear=finalWear|wornCode;
					toggle=!toggle;
				}
			}
		}

		GR.setStat("WEAR",""+finalWear);
		Weapon W=otherRace.myNaturalWeapon();
		if(W==null)
			W=nonHuman.myNaturalWeapon();
		if(W!=null)
		{
			GR.setStat("WEAPONCLASS",W.ID());
			GR.setStat("WEAPONXML",W.text());
		}

		final int xpAdj1=CMath.s_int(GR.getStat("XPADJ"));
		final int xpAdj2=CMath.s_int(otherRace.getStat("XPADJ"));
		GR.setStat("XPADJ", ""+((xpAdj1+xpAdj2)/2));

		GR.setStat("BODYKILL",""+otherRace.getStat("BODYKILL"));
		GR.setStat("CANRIDE",""+otherRace.getStat("CANRIDE"));
		GR.setStat("AGING",CMParms.toListString(aging));
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
		{
			if((race1.bodyMask()[i]>0)&&(race2.bodyMask()[i]>0))
			{
				if(((race1.bodyMask()[i]%2)==0) && ((race2.bodyMask()[i]%2)==0))
				{
					int newVal = ((race1.bodyMask()[i]+race2.bodyMask()[i])/2);
					if((newVal % 2)==0)
						GR.bodyMask()[i]=newVal;
					else
						GR.bodyMask()[i]=newVal+1;
				}
				else
					GR.bodyMask()[i]=((race1.bodyMask()[i]+race2.bodyMask()[i])/2);
			}
			else
			if((race1.bodyMask()[i]<=0)&&(race2.bodyMask()[i]>=0))
				GR.bodyMask()[i]=race2.bodyMask()[i];
			else
				GR.bodyMask()[i]=race1.bodyMask()[i];
		}

		final PhyStats RS1=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		RS1.setAllValues(0);
		CMLib.coffeeMaker().setPhyStats(RS1,race1.getStat("ESTATS"));

		final PhyStats RS2=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		RS2.setAllValues(0);
		CMLib.coffeeMaker().setPhyStats(RS2,race2.getStat("ESTATS"));

		final PhyStats RS=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		RS.setAbility((RS1.ability()+RS2.ability())/2);
		RS.setArmor((RS2.armor()+RS2.armor())/2);
		RS.setAttackAdjustment((RS1.attackAdjustment()+RS2.attackAdjustment())/2);
		RS.setSensesMask(RS1.sensesMask()|RS2.sensesMask());
		RS.setDisposition(RS1.disposition());
		RS.setDamage((RS1.damage()+RS2.damage())/2);
		RS.setHeight((RS1.height()+RS2.height())/2);
		RS.setSpeed((RS1.speed()+RS2.speed())/2.0);
		RS.setWeight((RS1.weight()+RS2.weight())/2);
		RS.setRejuv(PhyStats.NO_REJUV);
		GR.setStat("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(RS));

		final CharStats SETSTAT1=(CharStats)CMClass.getCommon("DefaultCharStats");
		SETSTAT1.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(SETSTAT1,race1.getStat("CSTATS"));

		final CharStats SETSTAT2=(CharStats)CMClass.getCommon("DefaultCharStats");
		SETSTAT2.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(SETSTAT2,race2.getStat("CSTATS"));

		final CharStats SETSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		SETSTAT.setAllValues(0);

		final CharStats ADJSTAT1=(CharStats)CMClass.getCommon("DefaultCharStats");
		ADJSTAT1.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(ADJSTAT1,race1.getStat("ASTATS"));

		final CharStats ADJSTAT2=(CharStats)CMClass.getCommon("DefaultCharStats");
		ADJSTAT2.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(ADJSTAT2,race2.getStat("ASTATS"));

		final CharStats ADJSTAT=(CharStats)CMClass.getCommon("DefaultCharStats");
		ADJSTAT.setAllValues(0);

		for(final int i: CharStats.CODES.ALLCODES())
		{
			if(i==CharStats.STAT_GENDER)
			{
				int setStat1=SETSTAT1.getStat(i);
				int setStat2=SETSTAT2.getStat(i);
				if((setStat1>0)&&(setStat2>0)&&(setStat1==setStat2))
					SETSTAT.setStat(i,setStat1);
			}
			else
			if(CharStats.CODES.isBASE(i))
			{
				final int newStat=((ADJSTAT1.getStat(i)+ADJSTAT2.getStat(i))/2);
				if(newStat>5)
					ADJSTAT.setStat(i,5);
				else
					ADJSTAT.setStat(i,newStat);
				int setStat1=SETSTAT1.getStat(i);
				int setStat2=SETSTAT2.getStat(i);
				if((setStat1>0)||(setStat2>0))
				{
					if(setStat1 == 0)
						setStat1 = 10;
					if(setStat2 == 0)
						setStat2 = 10;
					SETSTAT.setStat(i,(setStat1 + setStat2)/2);
				}
			}
			else
			if((i!=CharStats.STAT_GENDER)&&(i!=CharStats.STAT_AGE))
				ADJSTAT.setStat(i,(ADJSTAT1.getStat(i)+ADJSTAT2.getStat(i))/2);
		}
		GR.setStat("ASTATS",CMLib.coffeeMaker().getCharStatsStr(ADJSTAT));
		GR.setStat("CSTATS",CMLib.coffeeMaker().getCharStatsStr(SETSTAT));

		final CharState CS1=(CharState)CMClass.getCommon("DefaultCharState");
		CS1.setAllValues(0);
		CMLib.coffeeMaker().setCharState(CS1,race1.getStat("ASTATE"));
		final CharState CS2=(CharState)CMClass.getCommon("DefaultCharState");
		CS2.setAllValues(0);
		CMLib.coffeeMaker().setCharState(CS2,race2.getStat("ASTATE"));
		final CharState CS=(CharState)CMClass.getCommon("DefaultCharState");
		CS.setAllValues(0);

		CS.setFatigue((CS1.getFatigue()+CS2.getFatigue())/2);
		CS.setHitPoints((CS1.getHitPoints()+CS2.getHitPoints())/2);
		CS.setHunger((CS1.getHunger()+CS2.getHunger())/2);
		CS.setMana((CS1.getMana()+CS2.getMana())/2);
		CS.setMovement((CS1.getMovement()+CS2.getMovement())/2);
		CS.setThirst((CS1.getThirst()+CS2.getThirst())/2);
		GR.setStat("ASTATE",CMLib.coffeeMaker().getCharStateStr(CS));

		final CharState STARTCS1=(CharState)CMClass.getCommon("DefaultCharState");
		STARTCS1.setAllValues(0);
		CMLib.coffeeMaker().setCharState(STARTCS1,race1.getStat("STARTASTATE"));

		final CharState STARTCS2=(CharState)CMClass.getCommon("DefaultCharState");
		STARTCS2.setAllValues(0);
		CMLib.coffeeMaker().setCharState(STARTCS1,race2.getStat("STARTASTATE"));

		final CharState STARTCS=(CharState)CMClass.getCommon("DefaultCharState");
		STARTCS.setAllValues(0);

		STARTCS.setFatigue((STARTCS1.getFatigue()+STARTCS2.getFatigue())/2);
		STARTCS.setHitPoints((STARTCS1.getHitPoints()+STARTCS2.getHitPoints())/2);
		STARTCS.setHunger((STARTCS1.getHunger()+STARTCS2.getHunger())/2);
		STARTCS.setMana((STARTCS1.getMana()+STARTCS2.getMana())/2);
		STARTCS.setMovement((STARTCS1.getMovement()+STARTCS2.getMovement())/2);
		STARTCS.setThirst((STARTCS1.getThirst()+STARTCS2.getThirst())/2);
		GR.setStat("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(STARTCS));

		GR.setStat("DISFLAGS",""+(CMath.s_int(race1.getStat("DISFLAGS"))|CMath.s_int(race2.getStat("DISFLAGS"))));

		final List<RawMaterial> rscs=nonHuman.myResources();
		GR.setStat("NUMRSC",""+rscs.size());
		for(int i=0;i<rscs.size();i++)
			GR.setStat("GETRSCID"+i,((Item)rscs.get(i)).ID());
		for(int i=0;i<rscs.size();i++)
			GR.setStat("GETRSCPARM"+i,((Item)rscs.get(i)).text());

		GR.setStat("NUMOFT","");
		final Race outfitRace=(nonHuman.outfit(null)!=null)?nonHuman:otherRace;
		final List<Item> outfit=outfitRace.outfit(null);
		if((outfit!=null)&&(outfit.size()>0))
		{
			GR.setStat("NUMOFT",""+outfit.size());
			for(int i=0;i<outfit.size();i++)
				GR.setStat("GETOFTID"+i,outfit.get(i).ID());
			for(int i=0;i<outfit.size();i++)
				GR.setStat("GETOFTPARM"+i,outfit.get(i).text());
		}

		final Converter<String,AbilityMapping> race1conv = CMLib.ableMapper().getMapper(race1.ID());
		final Converter<String,AbilityMapping> race2conv = CMLib.ableMapper().getMapper(race2.ID());
		final Converter<String,Boolean> isLanguage = new Converter<String,Boolean>()
		{
			@Override
			public Boolean convert(String obj)
			{
				final Ability A=CMClass.getAbility(obj);
				return Boolean.valueOf((A instanceof Language) && ((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE));
			}
		};
		final boolean[] foundLanguage = new boolean[] { false } ;
		final Converter<Iterator<AbilityMapping>,Pair<String,AbilityMapping>> mapFix = new Converter<Iterator<AbilityMapping>,Pair<String,AbilityMapping>>()
		{
			@Override
			public Pair<String,AbilityMapping> convert(final Iterator<AbilityMapping> i)
			{
				if(i.hasNext())
				{
					final AbilityMapping r1 = i.next();
					if((r1 != null) && 
					((!foundLanguage[0]) || (!isLanguage.convert(r1.abilityID()).booleanValue())))
					{
						foundLanguage[0] = foundLanguage[0] || isLanguage.convert(r1.abilityID()).booleanValue();
						return new Pair<String,AbilityMapping>(r1.abilityID(), r1);
					}
				}
				return null;
			}
		};
		final List<AbilityMapping> rable1=new ConvertingList<String,AbilityMapping>(CMParms.toIDList(race1.racialAbilities(null)),race1conv);
		final List<AbilityMapping> rable2=new ConvertingList<String,AbilityMapping>(CMParms.toIDList(race2.racialAbilities(null)),race2conv);
		final PairList<String,AbilityMapping> dvataf = new PairVector<String,AbilityMapping>();
		for(Iterator<AbilityMapping> i1=rable1.iterator(), i2=rable2.iterator(); i1.hasNext() || i2.hasNext(); )
		{
			final Pair<String,AbilityMapping> p1 = mapFix.convert(i1);
			if((p1 != null) && (!dvataf.containsFirst(p1.first)))
				dvataf.add(p1);
			final Pair<String,AbilityMapping> p2 = mapFix.convert(i2);
			if((p2 != null) && (!dvataf.containsFirst(p2.first)))
				dvataf.add(p2);
		}
		for(int i=4;i<dvataf.size();i++)
			dvataf.remove(i);
		if(dvataf.size()>0)
			GR.setStat("NUMRABLE",""+dvataf.size());
		else
			GR.setStat("NUMRABLE","");
		for(int i=0;i<dvataf.size();i++)
		{
			GR.setStat("GETRABLE"+i,dvataf.get(i).second.abilityID());
			GR.setStat("GETRABLELVL"+i,""+dvataf.get(i).second.qualLevel());
			GR.setStat("GETRABLEQUAL"+i,""+(!dvataf.get(i).second.autoGain()));
			GR.setStat("GETRABLEPROF"+i,""+dvataf.get(i).second.defaultProficiency());
			GR.setStat("GETRABLEPARM"+i, ""+dvataf.get(i).second.defaultParm());
		}

		final PairList<String,AbilityMapping> cvataf = new PairVector<String,AbilityMapping>();
		final List<AbilityMapping> cable1=new ConvertingList<String,AbilityMapping>(new XVector<String>(race1.culturalAbilities().firstIterator()),race1conv);
		final List<AbilityMapping> cable2=new ConvertingList<String,AbilityMapping>(new XVector<String>(race2.culturalAbilities().firstIterator()),race2conv);
		for(Iterator<AbilityMapping> i1=cable1.iterator(), i2=cable2.iterator(); i1.hasNext() || i2.hasNext(); )
		{
			final Pair<String,AbilityMapping> p1 = mapFix.convert(i1);
			if((p1 != null) && (!cvataf.containsFirst(p1.first)))
				cvataf.add(p1);
			final Pair<String,AbilityMapping> p2 = mapFix.convert(i2);
			if((p2 != null) && (!cvataf.containsFirst(p2.first)))
				cvataf.add(p2);
		}
		for(int i=4;i<cvataf.size();i++)
			cvataf.remove(i);
		if(cvataf.size()>0)
			GR.setStat("NUMCABLE",""+cvataf.size());
		else
			GR.setStat("NUMCABLE","");
		for(int i=0;i<cvataf.size();i++)
		{
			GR.setStat("GETCABLE"+i,cvataf.get(i).second.abilityID());
			GR.setStat("GETCABLELVL"+i,""+cvataf.get(i).second.qualLevel());
			GR.setStat("GETCABLEGAIN"+i,""+(cvataf.get(i).second.autoGain()));
			GR.setStat("GETCABLEPROF"+i,""+cvataf.get(i).second.defaultProficiency());
		}

		final TriadVector<String,Integer,Integer> dataa=new TriadVector<String,Integer,Integer>();
		int numReff1 = CMath.s_int(race1.getStat("NUMREFF"));
		int numReff2 = CMath.s_int(race2.getStat("NUMREFF"));
		for(int a=0; (a<numReff1) || (a<numReff2); a++)
		{
			if(a<numReff1)
			{
				dataa.add(
					race1.getStat("GETREFF"+a),
					Integer.valueOf(CMath.s_int(race1.getStat("GETREFFLVL"+a))),
					Integer.valueOf(CMath.s_int(race1.getStat("GETREFFPARM"+a)))
				);
			}
			if(a<numReff2)
			{
				dataa.add(
					race2.getStat("GETREFF"+a),
					Integer.valueOf(CMath.s_int(race2.getStat("GETREFFLVL"+a))),
					Integer.valueOf(CMath.s_int(race2.getStat("GETREFFPARM"+a)))
				);
			}
		}
		for(int i=4;i<dataa.size();i++)
			dataa.remove(i);

		if(dataa.size()>0)
			GR.setStat("NUMREFF",""+dataa.size());
		else
			GR.setStat("NUMREFF","");
		for(int i=0;i<dataa.size();i++)
		{
			GR.setStat("GETREFF"+i,dataa.getFirst(i));
			GR.setStat("GETREFFLVL"+i,dataa.getSecond(i).toString());
			GR.setStat("GETREFFPARM"+i,dataa.getThird(i).toString());
		}
		
		final List<String> imms=new XVector<String>();
		for(int i=0;(i<race1.abilityImmunities().length) || (i<race2.abilityImmunities().length);i++)
		{
			if(i<race1.abilityImmunities().length)
				imms.add(race1.abilityImmunities()[i]);
			if(i<race2.abilityImmunities().length)
				imms.add(race2.abilityImmunities()[i]);
		}
		for(int i=4;i<imms.size();i++)
			imms.remove(i);
		
		GR.setStat("NUMIABLE",""+imms.size());
		for(int i=0;i<imms.size();i++)
		{
			final String AID=imms.get(i);
			GR.setStat("GETIABLE"+i,AID);
		}
		return GR;
	}

	@Override
	public QuadVector<String,Integer,Integer,Boolean> culturalAbilities()
	{
		final QuadVector<String,Integer,Integer,Boolean> ables=new QuadVector<String,Integer,Integer,Boolean>();
		if((culturalAbilityNames()!=null)
		&&(culturalAbilityProficiencies()!=null))
		{
			for(int i=0;i<culturalAbilityNames().length;i++)
			{
				final Integer level = Integer.valueOf((culturalAbilityLevels() != null) ? culturalAbilityLevels()[i] : 0); 
				final Integer prof = Integer.valueOf(culturalAbilityProficiencies()[i]);
				final Boolean autoGain = Boolean.valueOf((culturalAbilityAutoGains() != null) ? culturalAbilityAutoGains()[i] : true);
				ables.addElement(culturalAbilityNames()[i],prof,level,autoGain);
			}
		}
		return ables;
	}

	@Override
	public SearchIDList<Ability> racialAbilities(MOB mob)
	{
		if((racialAbilityMap==null)
		&&(racialAbilityNames()!=null)
		&&(racialAbilityLevels()!=null)
		&&(racialAbilityProficiencies()!=null)
		&&(racialAbilityQuals()!=null))
		{
			CMLib.ableMapper().delCharMappings(ID()); // necessary for a "clean start"
			mappedCulturalAbilities=false; // because of the clean start. :(
			racialAbilityMap=new Hashtable<Integer,SearchIDList<Ability>>();
			for(int i=0;i<racialAbilityNames().length;i++)
			{
				CMLib.ableMapper().addDynaAbilityMapping(
														 ID(),
														 racialAbilityLevels()[i],
														 racialAbilityNames()[i],
														 racialAbilityProficiencies()[i],
														 racialAbilityParms()[i],
														 !racialAbilityQuals()[i],
														 false,
														 "");
			}
		}
		if(racialAbilityMap==null)
			return emptyIDs;
		Integer level=null;
		if(mob!=null)
		{
			level=Integer.valueOf(mob.phyStats().level());
			if(level.intValue() == 0) // for mudstarting situations
				level = Integer.valueOf(mob.basePhyStats().level());
		}
		else
			level=Integer.valueOf(Integer.MAX_VALUE);
		if(racialAbilityMap.containsKey(level))
			return racialAbilityMap.get(level);
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return emptyIDs;
		final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),level.intValue(),true,(mob!=null));
		final CMUniqSortSVec<Ability> finalV=new CMUniqSortSVec<Ability>(V.size());
		for(final AbilityMapper.AbilityMapping able : V)
		{
			final Ability A=CMClass.getAbility(able.abilityID());
			if((A!=null)&&(CMStrings.contains(racialAbilityNames(),A.ID())))
			{
				if(!able.abilityID().equals(A.ID()))
					Log.errOut("Badly defined racial ability ID in "+ID()+": "+A.ID());
				A.setProficiency(CMLib.ableMapper().getDefaultProficiency(ID(),false,A.ID()));
				A.setSavable(false);
				A.setMiscText(CMLib.ableMapper().getDefaultParm(ID(),false,A.ID()));
				finalV.add(A);
			}
			else
			if((A==null)&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
				Log.errOut("Race "+ID()+" has unknown racial ability "+able.abilityID());
		}
		finalV.trimToSize();
		finalV.setReadOnly(true);
		racialAbilityMap.put(level,finalV);
		return finalV;
	}

	@Override
	public String getStatAdjDesc()
	{
		makeStatChgDesc();
		return baseStatChgDesc;
	}

	@Override
	public String getSensesChgDesc()
	{
		makeStatChgDesc();
		return sensesChgDesc;
	}

	@Override
	public String getDispositionChgDesc()
	{
		makeStatChgDesc();
		return dispChgDesc;
	}

	@Override
	public String getTrainAdjDesc()
	{
		if(trainsAtFirstLevel()>0)
			return "trains+"+trainsAtFirstLevel();
		if(trainsAtFirstLevel()<0)
			return "trains"+trainsAtFirstLevel();
		return "";
	}

	@Override
	public String getPracAdjDesc()
	{
		if(practicesAtFirstLevel()>0)
			return "practices+"+practicesAtFirstLevel();
		if(practicesAtFirstLevel()<0)
			return "practices"+practicesAtFirstLevel();
		return "";
	}

	@Override
	public String getAbilitiesDesc()
	{
		makeStatChgDesc();
		return abilitiesDesc;
	}

	@Override
	public String getLanguagesDesc()
	{
		makeStatChgDesc();
		return languagesDesc;
	}

	@Override
	public String racialParms()
	{
		return "";
	}

	@Override
	public int usageCount(int alter)
	{
		usageCount += alter;
		return usageCount;
	}

	@Override
	public void setRacialParms(String parms)
	{
	}

	protected void clrStatChgDesc()
	{
		baseStatChgDesc=null;
		dispChgDesc=null;
		sensesChgDesc=null;
		abilitiesDesc = null;
		languagesDesc = null;
	}

	protected void makeStatChgDesc()
	{
		if((baseStatChgDesc == null)
		||(dispChgDesc==null)
		||(sensesChgDesc==null))
		{
			StringBuilder str=new StringBuilder("");
			final Session sess = (Session)CMClass.getCommon("DefaultSession");
			final MOB mob=CMClass.getMOB("StdMOB"); // factory mobs didn't work because char stats...
			final MOB mob2=CMClass.getMOB("StdMOB"); // factory mobs didn't work because char stats...
			try
			{
				mob.setSession(sess);
				mob.baseCharStats().setMyRace(this);
				startRacing(mob,false);
				mob.baseCharStats().setStat(CharStats.STAT_GENDER, 'N');
				mob.recoverCharStats();
				mob.recoverPhyStats();
				mob.recoverMaxState();
				mob2.setSession(sess);
				mob2.baseCharStats().setMyRace(new StdRace());
				mob2.baseCharStats().setStat(CharStats.STAT_GENDER, 'N');
				mob2.recoverCharStats();
				mob2.recoverPhyStats();
				mob2.recoverMaxState();
				for(final int c: CharStats.CODES.ALLCODES())
				{
					if(c != CharStats.STAT_GENDER)
					{
						final int oldStat=mob2.charStats().getStat(c);
						final int newStat=mob.charStats().getStat(c);
						if(oldStat>newStat)
							str.append(CharStats.CODES.DESC(c).toLowerCase()+"-"+(oldStat-newStat)+", ");
						else
						if(newStat>oldStat)
							str.append(CharStats.CODES.DESC(c).toLowerCase()+"+"+(newStat-oldStat)+", ");
					}
				}
				dispChgDesc=CMLib.flags().getDispositionStateList(mob);
				sensesChgDesc=CMLib.flags().getSensesStateList(mob);
			}
			finally
			{
				mob.setSession(null);
				mob.destroy();
				mob2.setSession(null);
				mob2.destroy();
			}
			baseStatChgDesc=str.toString();
			if(baseStatChgDesc.endsWith(", "))
				baseStatChgDesc=baseStatChgDesc.substring(0,baseStatChgDesc.length()-2);
			final StringBuilder astr=new StringBuilder("");
			final StringBuilder lstr=new StringBuilder("");

			final List<Ability> ables=new ArrayList<Ability>();
			ables.addAll(racialAbilities(null));
			ables.addAll(racialEffects(null));

			final QuadVector<String,Integer,Integer,Boolean> cables=culturalAbilities();
			Ability A=null;
			if(cables!=null)
			{
				for(int c=0;c<cables.size();c++)
				{
					A=CMClass.getAbility(cables.getFirst(c));
					if(A!=null)
					{
						A.setProficiency(cables.getSecond(c).intValue());
						ables.add(A);
					}
				}
			}
			for(final Iterator<Ability> e=ables.iterator();e.hasNext();)
			{
				A=e.next();
				str = ((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)?lstr:astr;
				str.append(A.name());
				if(A.proficiency()>0)
					str.append("("+A.proficiency()+"%)");
				str.append(", ");
			}
			abilitiesDesc=astr.toString();
			if(abilitiesDesc.endsWith(", "))
				abilitiesDesc=abilitiesDesc.substring(0,abilitiesDesc.length()-2);
			languagesDesc=lstr.toString();
			if(languagesDesc.endsWith(", "))
				languagesDesc=languagesDesc.substring(0,languagesDesc.length()-2);
		}
	}

	protected static String[] CODES={"CLASS","PARMS"};

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + racialParms();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setRacialParms(val);
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
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	public boolean sameAs(Race E)
	{
		if(!(E instanceof StdRace))
			return false;
		for(int i=0;i<CODES.length;i++)
		{
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}
}
