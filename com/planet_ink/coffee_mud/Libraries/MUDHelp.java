package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrCallback;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.util.*;


/*
   Copyright 2004-2022 Bo Zimmerman

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
public class MUDHelp extends StdLibrary implements HelpLibrary
{
	@Override
	public String ID()
	{
		return "MUDHelp";
	}

	protected final Map<String,String>		genUsageCost	= new SHashtable<String, String>();
	protected final Map<String,String[]>	seeAlsoCache	= Collections.synchronizedMap(new TreeMap<String,String[]>());

	protected final static String[] SKILL_PREFIXES =
	{
		"SPELL_", "PRAYER_", "SONG_", "DANCE_", "PLAY_",
		"CHANT_","BEHAVIOR_","POWER_","SKILL_","PROP_"
	};

	protected final static String[] SKILL_SUFFIXES =
	{
		"_SPELL", "_PRAYER", "_SONG", "_DANCE", "_PLAY",
		"_CHANT_", "_POWER_","_SKILL_"
	};

	protected final int[] proficiencyRanges=new int[]{5,10,20,30,40,50,60,70,75,80,85,90,95,100};

	@Override
	public boolean isPlayerSkill(String helpStr)
	{
		if(helpStr.length()==0)
			return false;
		if(getHelpFile().size()==0)
			return false;
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(' ')>=0)
			helpStr=helpStr.replace(' ','_');
		for(final String pre : SKILL_PREFIXES)
		{
			if(helpStr.startsWith(pre))
				return true;
		}
		final String thisTag=getHelpFile().getProperty(helpStr);
		if((thisTag!=null)
		&&(thisTag.startsWith("<ABILITY>")||thisTag.startsWith("<EXPERTISE>")))
			return true;
		return CMClass.getAbility(helpStr)!=null;
	}

	@Override
	public String getRPProficiencyStr(final int proficiency)
	{
		int ordinal=0;
		for(int i=0;i<proficiencyRanges.length;i++)
		{
			if(proficiency<=proficiencyRanges[i])
			{
				ordinal=i;
				break;
			}
		}
		final String message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.SKILL_PROFICIENCY_DESC, ordinal);
		return message;
	}

	@Override
	public String getHelpText(final String helpStr, final MOB forM, final boolean favorAHelp)
	{
		return getHelpText(helpStr, forM, favorAHelp, false);
	}

	@Override
	public String getHelpText(final String helpStr, final MOB forM, final boolean favorAHelp, final boolean noFix)
	{
		if(helpStr.length()==0)
			return null;
		String thisTag=null;
		if(favorAHelp)
		{
			if(getArcHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getArcHelpFile(),forM,noFix);
			if(thisTag==null)
			{
				if(getHelpFile().size()==0)
					return null;
				thisTag=getHelpText(helpStr,getHelpFile(),forM,noFix);
			}
		}
		else
		{
			if(getHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getHelpFile(),forM,noFix);
			if(thisTag==null)
			{
				if(getArcHelpFile().size()==0)
					return null;
				thisTag=getHelpText(helpStr,getArcHelpFile(),forM,noFix);
			}
		}
		return thisTag;
	}

	@Override
	public List<String> getTopics(final boolean archonHelp, final boolean standardHelp)
	{
		final Vector<String> reverseList=new Vector<String>();
		Properties rHelpFile=null;
		if(archonHelp)
			rHelpFile=getArcHelpFile();
		if(standardHelp)
		{
			if(rHelpFile==null)
				rHelpFile=getHelpFile();
			else
			{
				for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
				{
					final String ptop = (String)e.nextElement();
					final String thisTag=rHelpFile.getProperty(ptop);
					if ((thisTag==null)
					||(thisTag.length()==0)
					||(thisTag.length()>=50)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
				}
				rHelpFile=getHelpFile();
			}
		}
		if(rHelpFile!=null)
		{
			for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
			{
				final String ptop = (String)e.nextElement();
				final String thisTag=rHelpFile.getProperty(ptop);
				if(ptop.length()>0)
				{
					if ((thisTag==null)
					||(thisTag.length()==0)
					||(thisTag.length()>=50)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
				}
			}
		}
		Collections.sort(reverseList);
		return reverseList;
	}

	protected String getActualUsageInternal(Ability A, final int whichUsageCode, final MOB forM)
	{
		final Ability myA=forM.fetchAbility(A.ID());
		if(myA!=null)
			A=myA;

		final int[] consumption=A.usageCost(forM,true);
		int whichConsumed=consumption[0];
		switch(whichUsageCode)
		{
		case Ability.USAGE_MOVEMENT:
			whichConsumed = consumption[Ability.USAGEINDEX_MOVEMENT];
			break;
		case Ability.USAGE_MANA:
			whichConsumed = consumption[Ability.USAGEINDEX_MANA];
			break;
		case Ability.USAGE_HITPOINTS:
			whichConsumed = consumption[Ability.USAGEINDEX_HITPOINTS];
			break;
		}
		if(whichConsumed==Integer.MAX_VALUE/2)
			return "all";
		return ""+whichConsumed;
	}

	@Override
	public String getAbilityQualityDesc(final Ability A)
	{
		final StringBuilder prepend=new StringBuilder("");
		switch(A.abstractQuality())
		{
		case Ability.QUALITY_MALICIOUS:
			prepend.append(L("Malicious"));
			break;
		case Ability.QUALITY_BENEFICIAL_OTHERS:
		case Ability.QUALITY_BENEFICIAL_SELF:
			prepend.append(L("Always Beneficial"));
			break;
		case Ability.QUALITY_OK_OTHERS:
		case Ability.QUALITY_OK_SELF:
			prepend.append(L("Sometimes Beneficial"));
			break;
		case Ability.QUALITY_INDIFFERENT:
			prepend.append(L("Circumstantial"));
			break;
		}
		return prepend.toString();
	}

	@Override
	public String getAbilityCostDesc(final Ability A, final MOB forM)
	{
		final StringBuilder costStr = new StringBuilder("");
		if(A.usageType()==Ability.USAGE_NADA)
			costStr.append(L("None"));
		if(CMath.bset(A.usageType(),Ability.USAGE_MANA))
			costStr.append(L("Mana (@x1) ",getActualAbilityUsageDesc(A,Ability.USAGE_MANA,forM)));
		if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
			costStr.append(L("Movement (@x1) ",getActualAbilityUsageDesc(A,Ability.USAGE_MOVEMENT,forM)));
		if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
			costStr.append(L("Hit Points (@x1) ",getActualAbilityUsageDesc(A,Ability.USAGE_HITPOINTS,forM)));
		return costStr.toString();
	}

	@Override
	public String getAbilityRangeDesc(final Ability A)
	{
		final StringBuilder prepend=new StringBuilder("");
		final int min=A.minRange();
		final int max=A.maxRange();
		if(min+max==0)
			prepend.append(L("Touch, or not applicable"));
		else
		{
			if(min==0)
				prepend.append(L("Touch"));
			else
				prepend.append(L("Range @x1",""+min));
			if(max>0)
				prepend.append(L(" - Range @x1",""+max));
		}
		return prepend.toString();
	}

	@Override
	public String getAbilityTargetDesc(final Ability A)
	{
		final StringBuilder prepend=new StringBuilder("");
		if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
		||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
			prepend.append(L("Caster only"));
		else
		if((CMClass.basicItems().hasMoreElements())
		&&(CMClass.mobTypes().hasMoreElements())
		&&(CMClass.exits().hasMoreElements())
		&&(CMClass.locales().hasMoreElements()))
		{
			if(A.canAffect(Ability.CAN_ITEMS)||A.canTarget(Ability.CAN_ITEMS))
				prepend.append(L("Items "));
			if(A.canAffect(Ability.CAN_MOBS)||A.canTarget(Ability.CAN_MOBS))
				prepend.append(L("Creatures "));
			if(A.canAffect(Ability.CAN_EXITS)||A.canTarget(Ability.CAN_EXITS))
				prepend.append(L("Exits "));
			if(A.canAffect(Ability.CAN_ROOMS)||A.canTarget(Ability.CAN_ROOMS))
				prepend.append(L("Rooms "));
		}
		else
		if(A.abstractQuality()==Ability.QUALITY_INDIFFERENT)
			prepend.append(L("Items or Rooms"));
		else
		if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
			prepend.append(L("Others"));
		else
		if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
		||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
			prepend.append(L("Caster, or others"));
		return prepend.toString();
	}

	@Override
	public String getActualAbilityUsageDesc(final Ability A, final int whichUsageCode, final MOB forM)
	{
		if(forM == null)
			return getActualUsage(A,whichUsageCode);
		else
			return getActualUsageInternal(A,whichUsageCode,forM);
	}

	protected String getActualUsage(final Ability A, final int whichUsageCode)
	{
		String usageCost;
		if(this.genUsageCost.containsKey(A.ID()+"/"+whichUsageCode))
			usageCost=this.genUsageCost.get(A.ID()+"/"+whichUsageCode);
		else
		{
			final MOB forM=CMClass.getFactoryMOB();
			try
			{
				forM.maxState().setMana(Integer.MAX_VALUE/2);
				forM.maxState().setMovement(Integer.MAX_VALUE/2);
				forM.maxState().setHitPoints(Integer.MAX_VALUE/2);
				usageCost=this.getActualUsageInternal(A, whichUsageCode, forM);
			}
			finally
			{
				forM.destroy();
			}
		}
		return usageCost;
	}

	@Override
	public void addHelpEntry(final String ID, final String text, final boolean archon)
	{
		if(archon)
			getArcHelpFile().put(ID.toUpperCase(),text);
		else
			getHelpFile().put(ID.toUpperCase(),text);
	}

	private void appendAllowed(final MOB mob, final StringBuilder prepend, final String ID)
	{
		final Iterator<String> i=CMLib.expertises().filterUniqueExpertiseIDList(CMLib.ableMapper().getAbilityAllowsList(ID));
		int lastLine=11;
		if(i.hasNext())
		{
			prepend.append(L("\n\rAllows   : "));
			while(i.hasNext())
			{
				final String allowStr=i.next();
				final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(allowStr);
				if(def!=null)
				{
					if((mob==null)||CMLib.masking().maskCheck(def.compiledListMask(), mob, true))
					{
						prepend.append(def.name());
						lastLine+=(def.name().length()+2);
					}
					else
						continue;
				}
				else
				{
					final Ability A=CMClass.getAbility(allowStr);
					if(A!=null)
					{
						prepend.append(A.Name());
						lastLine+=(A.Name().length()+2);
					}
				}
				if((lastLine>60)&&(i.hasNext()))
				{
					lastLine=11;
					prepend.append(L("\n\rAllows   : "));
				}
				else
				if(i.hasNext())
					prepend.append(", ");
			}
		}
	}

	protected String columnHelper(String word, final String msg, final int wrap)
	{
		final StringBuilder prepend = new StringBuilder("");
		final String[] maxStats = CMLib.coffeeFilter().wrapOnlyFilter(msg,wrap-12);
		for(final String s : maxStats)
		{
			prepend.append(CMStrings.padRight(word, 12)).append(s).append("\n\r");
			word=" ";
		}
		return prepend.toString();
	}

	@Override
	public String fixHelp(final String tag, String str, final MOB forM)
	{
		boolean worldCurrency=str.startsWith("<CURRENCIES>");
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		final boolean useWords=CMProps.Int.Prowesses.SKILL_PROFICIENCY.is(prowessCode);
		if(str.startsWith("<CURRENCY>")||worldCurrency)
		{
			str=str.substring(worldCurrency?12:10);
			final Vector<String> currencies=new Vector<String>();
			if((forM==null)||(forM.location()==null)||(worldCurrency))
			{
				worldCurrency=true;
				for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
				{
					final String currency=CMLib.beanCounter().getCurrency(e.nextElement());
					if(!currencies.contains(currency))
						currencies.addElement(currency);
				}
			}
			else
				currencies.addElement(CMLib.beanCounter().getCurrency(forM.location()));
			final StringBuilder help=new StringBuilder("");
			if(worldCurrency)
				help.append("\n\r"+CMStrings.padRight(L("World Currencies"),20)+":");
			for (final String currency : currencies)
			{
				if(worldCurrency)
					help.append("\n\r"+CMStrings.padRight(L("Currency"),20)+": ");
				else
					help.append("\n\r"+CMStrings.padRight(L("Local Currency"),20)+": ");
				if(currency.length()==0)
					help.append("default");
				else
					help.append(CMStrings.capitalizeAndLower(currency));
				final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(currency);
				if(def == null)
					Log.errOut("Help","Unknown currency: "+currency);
				else
				{
					final MoneyLibrary.MoneyDenomination denoms[]=def.denominations();
					for (final MoneyDenomination denom : denoms)
					{
						if(denom.abbr().length()>0)
							help.append("\n\r"+CMStrings.padRight(denom.name()+" ("+denom.abbr()+")",20)+":");
						else
							help.append("\n\r"+CMStrings.padRight(denom.name(),20)+":");
						if(denom.value()==CMLib.beanCounter().getLowestDenomination(currency))
							help.append(L(" (exchange rate is @x1 of base)",""+denom.value()));
						else
							help.append(" "+CMLib.beanCounter().getConvertableDescription(currency,denom.value()));
					}
				}
				help.append("\n\r");
			}
			help.append(str);
			str=help.toString();
		}
		else
		if(str.startsWith("<EXPERTISE>"))
		{
			str=str.substring(11);
			ExpertiseLibrary.ExpertiseDefinition def=null;
			for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				def=e.nextElement();
				if(def.name().toUpperCase().replace(' ','_').equals(tag.toUpperCase()))
				{
					final StringBuilder prepend=new StringBuilder("");
					prepend.append(L("\n\rExpertise: @x1",def.name()));
					prepend.append(L("\n\rRequires : @x1",CMLib.masking().maskDesc(def.allRequirements(),true)));
					appendAllowed(forM,prepend,def.ID());
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
		if(str.startsWith("<CHARCLASS>"))
		{
			str=str.substring(11);
			CharClass C = CMClass.findCharClass(tag);
			if(C==null)
				C=CMClass.findCharClass(tag.replace('_',' '));
			if(C!=null)
			{
				final StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forM!=null)&&(forM.session()!=null))
					wrap=forM.session().getWrap();
				if(wrap <=0 )
					wrap=78;
				prepend.append(L("^HChar Class: ^N@x1 ^H(^N@x2^H)^N",C.name(),C.baseClass()));
				prepend.append("\n\r");
				prepend.append(columnHelper(L("^HMax-Stats :^N"),C.getMaxStatDesc(),wrap));
				prepend.append(columnHelper(L("^HQualifiers:^N"),C.getStatQualDesc(),wrap));
				prepend.append(columnHelper(L("^HRaces     :^N"),C.getRaceQualDesc(),wrap));
				prepend.append("^H"+CMStrings.padRight(L("Prime Stat: ^N@x1",C.getPrimeStatDesc()),(wrap/2)));
				prepend.append(L("^HAttack Pts: ^N@x1",C.getAttackDesc()));
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight(L("Practices : ^N@x1",C.getPracticeDesc()),(wrap/2)));
				prepend.append(L("^HTrains    : ^N@x1",C.getTrainDesc()));
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight(L("Hit Points: ^N@x1",C.getHitPointDesc()),(wrap/2)));
				prepend.append(L("^HMana      : ^N@x1",C.getManaDesc()));
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight(L("Movement  : ^N@x1",C.getMovementDesc()),(wrap/2)));
				prepend.append(L("^HDamage Pts: ^N@x1",C.getDamageDesc()));
				prepend.append("\n\r");
				prepend.append(columnHelper(L("^HWeapons   : ^N"),C.getWeaponLimitDesc(),wrap));
				prepend.append(columnHelper(L("^HArmor     : ^N"),C.getArmorLimitDesc(),wrap));
				prepend.append(columnHelper(L("^HBonuses   :^N"),C.getOtherBonusDesc(),wrap));
				prepend.append(columnHelper(L("^HLimits    :^N"),C.getOtherLimitsDesc(),wrap));
				prepend.append(L("^HDesc.     : ^N"));
				str=prepend.toString()+"\n\r"+str;
			}
		}
		if(str.startsWith("<RACE>"))
		{
			str=str.substring(6);
			Race R=CMClass.findRace(tag);
			if(R==null)
				R=CMClass.findRace(tag.replace('_',' '));
			if(R!=null)
			{
				final StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forM!=null)&&(forM.session()!=null))
					wrap=forM.session().getWrap();
				if(wrap <=0 )
					wrap=78;
				prepend.append(L("^HRace Name : ^N@x1 ^H(^N@x2^H)^N",R.name(),R.racialCategory()));
				prepend.append("\n\r");

				String s=R.getStatAdjDesc();
				if(R.getTrainAdjDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getTrainAdjDesc();
				if(R.getPracAdjDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getPracAdjDesc();
				prepend.append(columnHelper(L("^HStat Mods.:^N"),s,wrap));
				s=R.getSensesChgDesc();
				if(R.getDispositionChgDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getDispositionChgDesc();
				if(R.getAbilitiesDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getAbilitiesDesc();
				prepend.append(columnHelper(L("^HAbilities :^N"),s,wrap));
				prepend.append(columnHelper(L("^HLanguages :^N"),R.getLanguagesDesc(),wrap));
				prepend.append(columnHelper(L("^HLife Exp. :^N"),L("@x1 years",""+R.getAgingChart()[Race.AGE_ANCIENT]),wrap));
				if(R.getXPAdjustment() != 0)
					prepend.append(columnHelper(L("^HXP Adjust.:^N"),R.getXPAdjustment()+"%",wrap));
				s="";
				for(final String ableID : R.abilityImmunities())
				{
					final Ability A=CMClass.getAbilityPrototype(ableID);
					if(A!=null)
						s+=((s.length()>0)?", ":"")+A.name();
				}
				prepend.append(columnHelper(L("^HImmunities:^N"),s,wrap));
				s="";
				if(R.outfit(null)!=null)
				{
					for(final Item I : R.outfit(null))
					{
						if(I!=null)
							s+=((s.length()>0)?", ":"")+I.Name();
					}
				}
				prepend.append(columnHelper(L("^HEquipment :^N"),s,wrap));
				prepend.append(L("^HDesc.     : ^N"));
				str=prepend.toString()+"\n\r"+str;
			}
		}
		if(str.endsWith("<COLORS>"))
			str=str.substring(0, str.length()-8)+CMLib.color().getColorInfo(false);
		if(str.endsWith("<COLORS256>"))
			str=str.substring(0, str.length()-11)+CMLib.color().getColorInfo(true);
		if(str.startsWith("<ABILITY>"))
		{
			str=str.substring(9);
			String name=tag;
			int type=-1;
			if(name.startsWith("SPELL_"))
			{
				type=Ability.ACODE_SPELL;
				name=name.substring(6);
			}
			else
			if(name.startsWith("PRAYER_"))
			{
				type=Ability.ACODE_PRAYER;
				name=name.substring(7);
			}
			else
			if(name.startsWith("DANCE_"))
			{
				type=Ability.ACODE_SONG;
				name=name.substring(6);
			}
			else
			if(name.startsWith("POWER_"))
			{
				type=Ability.ACODE_SUPERPOWER;
				name=name.substring(6);
			}
			else
			if((name.startsWith("SONG_"))
			||(name.startsWith("PLAY_")))
			{
				type=Ability.ACODE_SONG;
				name=name.substring(5);
			}
			else
			if(name.startsWith("CHANT_"))
			{
				type=Ability.ACODE_CHANT;
				name=name.substring(6);
			}
			else
			if(name.startsWith("TECH_"))
			{
				type=Ability.ACODE_TECH;
				name=name.substring(5);
			}
			name=name.replace('_',' ');
			final Vector<Ability> helpedPreviously=new Vector<Ability>();
			String subTag=tag;
			while(subTag.indexOf('_')!=subTag.lastIndexOf('_'))
			{
				final int x=subTag.lastIndexOf('_');
				subTag=subTag.substring(0,x)+subTag.substring(x+1);
			}
			Ability A=CMClass.getAbility(tag);
			if((A==null)
			||((type>=0)&&(type!=(A.classificationCode()&Ability.ALL_ACODES))))
				A=CMClass.getAbility(subTag);
			if((A==null)
			||((type>=0)&&(type!=(A.classificationCode()&Ability.ALL_ACODES))))
			{
				for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability chkA=a.nextElement();
					if(((chkA.ID().equalsIgnoreCase(tag)||chkA.ID().equalsIgnoreCase(subTag))
						&&((type<0)||(type==(chkA.classificationCode()&Ability.ALL_ACODES))))
					||(chkA.name().equalsIgnoreCase(name)))
					{
						A=chkA;
					}
				}
			}
			if((A!=null)
			&&(!helpedPreviously.contains(A)))
			{
				helpedPreviously.addElement(A);
				final StringBuilder prepend=new StringBuilder("");
				type=(A.classificationCode()&Ability.ALL_ACODES);
				prepend.append("\n\r");
				switch(type)
				{
				case Ability.ACODE_SPELL:
					prepend.append(CMStrings.padRight(L("Spell"),9));
					break;
				case Ability.ACODE_PRAYER:
					prepend.append(CMStrings.padRight(L("Prayer"),9));
					break;
				case Ability.ACODE_CHANT:
					prepend.append(CMStrings.padRight(L("Chant"),9));
					break;
				case Ability.ACODE_SUPERPOWER:
					prepend.append(CMStrings.padRight(L("SuperPower"),9));
					break;
				case Ability.ACODE_SONG:
					prepend.append(CMStrings.padRight(L("Song"),9));
					break;
				default:
					prepend.append(CMStrings.padRight(L("Skill"),9));
					break;
				}
				prepend.append(": "+A.name());
				if((forM!=null)&&(forM.session()!=null)&&(!forM.session().isStopped()))
				{
					final Ability A2=forM.fetchAbility(A.ID());
					if(A2!=null)
					{
						final String prof;
						if(useWords)
							prof=getRPProficiencyStr(A2.proficiency());
						else
							prof=A2.proficiency()+"%";
						prepend.append(L("   (Proficiency: @x1)",prof));
					}
				}
				if((A.classificationCode()&Ability.ALL_DOMAINS)>0)
				{
					prepend.append(L("\n\rDomain   : "));
					final int school=(A.classificationCode()&Ability.ALL_DOMAINS)>>5;
					prepend.append(CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[school].replace('_',' ')));
				}
				final PairList<String,Integer> avail=CMLib.ableMapper().getAvailabilityList(A, 2);
				for(int c=0;c<avail.size();c++)
				{
					if((c%4)==0)
						prepend.append(L("\n\rAvailable: "));
					final CharClass C=CMClass.getCharClass(avail.getFirst(c));
					final Integer I=avail.getSecond(c);
					prepend.append((C!=null)?C.name(I.intValue()):avail.getFirst(c)).append(" ");
				}

				DVector preReqs;
				if(forM==null)
					preReqs=CMLib.ableMapper().getCommonPreRequisites(A);
				else
					preReqs=CMLib.ableMapper().getCommonPreRequisites(forM,A);
				if(preReqs.size()>0)
				{
					final String names=CMLib.ableMapper().formatPreRequisites(preReqs);
					prepend.append(L("\n\rRequires : @x1",names));
				}
				final String mask;
				if(forM == null)
					mask=CMLib.ableMapper().getCommonExtraMask(A);
				else
					mask=CMLib.ableMapper().getApplicableMask(forM,A);
				if((mask!=null)&&(mask.length()>0))
					prepend.append(L("\n\rRequires : @x1",CMLib.masking().maskDesc(mask,true)));
				appendAllowed(forM,prepend,A.ID());
				if(type==Ability.ACODE_PRAYER)
				{
					String rangeDescs=null;
					for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
					{
						final Faction F=e.nextElement();
						rangeDescs=F.usageFactorRangeDescription(A);
						if(rangeDescs.length()>0)
						{
							prepend.append("\n\r"+CMStrings.padRight(L("Alignment"),9)+": "
									+rangeDescs);
						}
					}
				}

				if((!A.isAutoInvoked())
				||((A.triggerStrings().length>0)))
				{
					final Vector<AbilityComponent> components=(Vector<AbilityComponent>)CMLib.ableComponents().getAbilityComponentMap().get(A.ID().toUpperCase());
					if(components!=null)
					{
						prepend.append(L("\n\rComponent: "));
						prepend.append(CMLib.ableComponents().getAbilityComponentDesc(forM,A.ID()));
					}
					prepend.append(L("\n\rUse Cost : "));
					prepend.append(this.getAbilityCostDesc(A, forM));
					prepend.append(L("\n\rQuality  : "));
					prepend.append(this.getAbilityQualityDesc(A));
					prepend.append(L("\n\rTargets  : "));
					prepend.append(this.getAbilityTargetDesc(A));
					prepend.append(L("\n\rRange    : "));
					prepend.append(this.getAbilityRangeDesc(A));
					if((A.triggerStrings()!=null)
					   &&(A.triggerStrings().length>0))
					{
						prepend.append(L("\n\rCommands : "));
						for(int i=0;i<A.triggerStrings().length;i++)
						{
							prepend.append(A.triggerStrings()[i]);
							if(i<(A.triggerStrings().length-1))
								prepend.append(", ");
						}
					}
				}
				else
					prepend.append(L("\n\rInvoked  : Automatic"));
				str=prepend.toString()+"\n\r"+str;
			}
		}
		try
		{
			if((str!=null)&&(str.indexOf('@')>=0))
				return CMLib.webMacroFilter().virtualPageFilter(str);
		}
		catch (final com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException x)
		{
		}
		return str;
	}

	@Override
	public Pair<String, String> getHelpMatch(final String helpStr, final Properties rHelpFile, final MOB forM, final int skipEntries)
	{
		final Pair<String,String> p = getHelpText(helpStr, rHelpFile, forM, false, new int[]{skipEntries});
		if((p == null)||(p.second==null))
			return null;
		return p;
	}

	protected String normalizeHelpText(final String helpText, final int[] skip)
	{
		if((helpText==null)
		||(helpText.trim().length()==0)
		||(helpText.trim().equals("null")))
			return null;
		if((skip==null)||(skip[0]<=0))
			return helpText;
		--skip[0];
		return null;
	}

	protected String getHelpText(final String helpKey, final Properties rHelpFile, final MOB forM, final boolean noFix)
	{
		final Pair<String,String> p = getHelpText(helpKey, rHelpFile, forM, noFix, new int[]{0});
		if(p == null)
			return null;
		return p.second;
	}

	protected Pair<String,String> getHelpText(String helpKey, final Properties rHelpFile, final MOB forM, final boolean noFix, final int[] skip)
	{
		helpKey=helpKey.toUpperCase().trim();
		final String helpKeyWSpaces = helpKey;
		if(helpKey.indexOf(' ')>=0)
			helpKey=helpKey.replace(' ','_');

		if(helpKey.equals("!"))
			helpKey="EXCLAMATION_POINT";
		if(helpKey.equals(","))
			helpKey="COMMA";
		if(helpKey.equals(":"))
			helpKey="COLON";
		if(helpKey.equals(";"))
			helpKey="SEMICOLON";

		// first come the callouts:

		// specific calling out of a channel
		if(helpKey.startsWith("CHANNEL_")||helpKey.startsWith("NOCHANNEL_"))
		{
			String s=CMLib.channels().findChannelName(helpKey.substring(8).trim());
			boolean no=false;
			if(((s==null)||(s.length()==0))
			&&(helpKey.startsWith("NO")))
			{
				s=CMLib.channels().findChannelName(helpKey.trim().substring(10));
				no=true;
			}
			if((s!=null)&&(s.length()>0))
			{
				String helpText;
				if(no)
					helpText=rHelpFile.getProperty("NOCHANNEL");
				else
					helpText=rHelpFile.getProperty("CHANNEL");
				helpText=CMStrings.replaceAll(helpText,"[CHANNEL]",s.toUpperCase());
				helpText=CMStrings.replaceAll(helpText,"[channel]",s.toLowerCase());
				final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
				helpText=CMStrings.replaceAll(helpText,"[EXTRA]",extra);
				return new Pair<String,String>(helpKey, helpText);
			}
			return null;
		}

		// specifically calling out an area
		if(helpKey.startsWith("AREAHELP_"))
		{
			final String ahelpStr=helpKeyWSpaces.substring(9);
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((A.name().equalsIgnoreCase(ahelpStr))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
					return new Pair<String,String>(ahelpStr, CMLib.map().getArea(A.Name()).getAreaStats()+"");
			}
			return null;
		}

		if(helpKey.startsWith("GENCHARCLASS_"))
		{
			CharClass C=CMClass.findCharClass(helpKey.substring(13));
			if(C==null)
			{
				C=CMClass.findCharClass(helpKeyWSpaces.substring(13));
				if(C!=null)
					helpKey=helpKeyWSpaces;
			}
			if((C!=null)&&(C.isGeneric()))
			{
				final String helpText="<CHARCLASS>"+C.getStat("HELP");
				if(noFix)
					return new Pair<String,String>(helpKey, helpText);
				return new Pair<String,String>(helpKey.substring(13), fixHelp(helpKey.substring(13),helpText,forM));
			}
			return null;
		}

		if(helpKey.startsWith("GENRACE_"))
		{
			Race R=CMClass.findRace(helpKey.substring(8));
			if(R==null)
			{
				R=CMClass.findRace(helpKeyWSpaces.substring(8));
				if(R!=null)
					helpKey=helpKeyWSpaces;
			}
			if((R!=null)
			&&((CMProps.isTheme(R.availabilityCode()) && (R.getStat("HELP").length()>0))
				|| (rHelpFile == this.getArcHelpFile())))
			{
				String helpText;
				if(R.getStat("HELP").length()==0)
					helpText="<RACE>"+L("No further information available");
				else
					helpText="<RACE>"+R.getStat("HELP");
				if(noFix)
					return new Pair<String,String>(helpKey, helpText);
				return new Pair<String,String>(helpKey.substring(8), fixHelp(helpKey.substring(8),helpText,forM));
			}
			return null;
		}
		if(helpKey.startsWith("SOCIAL_"))
		{
			final String helpText=normalizeHelpText(CMLib.socials().getSocialsHelp(forM,helpKeyWSpaces.substring(7), false),skip);
			if(helpText!=null)
				return new Pair<String,String>(helpKeyWSpaces.substring(7), fixHelp(helpKeyWSpaces.substring(7),helpText,forM));
			return null;
		}
		// now start the exact, or darn near-exact matches
		// ** maintaining the actual helpKey index into rHelpFile is necessary!

		String helpText=normalizeHelpText(rHelpFile.getProperty(helpKey),skip);

		if(helpText==null)
		{
			for(int i=0;i<SKILL_PREFIXES.length;i++)
			{
				final String prefix = SKILL_PREFIXES[i];
				helpText=normalizeHelpText(rHelpFile.getProperty(prefix+helpKey),skip);
				if(helpText!=null)
				{
					helpKey=prefix+helpKey;
					break;
				}
			}
		}

		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.socials().getSocialsHelp(forM,helpKey, true),skip);
			if((helpText==null)
			&&(helpKeyWSpaces.indexOf(' ')<0))
			{
				helpText=normalizeHelpText(CMLib.socials().getSocialsHelp(forM,helpKeyWSpaces,true),skip);
				if(helpText!=null)
					helpKey=helpKeyWSpaces;
			}
		}
		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.clans().getGovernmentHelp(forM,helpKey, true),skip);
			if(helpText==null)
			{
				helpText=normalizeHelpText(CMLib.clans().getGovernmentHelp(forM,helpKeyWSpaces,true),skip);
				if(helpText!=null)
					helpKey=helpKeyWSpaces;
			}
		}
		if(helpText==null)
		{
			Ability A=CMClass.findAbility(helpKey,-1,-1,true);
			if((A==null)||(!A.isGeneric()))
			{
				A=CMClass.findAbility(helpKeyWSpaces,-1,-1,true);
				if((A!=null)&&(A.isGeneric()))
					helpKey=helpKeyWSpaces;
			}
			if((A!=null)&&(A.isGeneric()))
				helpText=normalizeHelpText(A.getStat("HELP"),skip);
		}
		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.expertises().getExpertiseHelp(helpKey,true),skip);
			if(helpText==null)
			{
				helpText=normalizeHelpText(CMLib.expertises().getExpertiseHelp(helpKeyWSpaces,true),skip);
				if(helpText!=null)
					helpKey=helpKeyWSpaces;
			}
		}
		if(helpText==null)
		{
			CharClass C=CMClass.findCharClass(helpKey);
			if((C==null)||(!C.isGeneric()))
			{
				C=CMClass.findCharClass(helpKeyWSpaces);
				if((C!=null)&&(C.isGeneric()))
					helpKey=helpKeyWSpaces;
			}
			if((C!=null)&&(C.isGeneric()))
				helpText=normalizeHelpText("<CHARCLASS>"+C.getStat("HELP"),skip);
		}

		if(helpText==null)
		{
			String subKey=helpKey;
			Race R=CMClass.findRace(helpKey);
			if(R==null)
			{
				subKey=helpKeyWSpaces;
				R=CMClass.findRace(helpKeyWSpaces);
			}
			if((R!=null)
			&&((CMProps.isTheme(R.availabilityCode()) && (R.getStat("HELP").length()>0))
				|| (rHelpFile == this.getArcHelpFile())))
			{
				helpKey=subKey;
				if(R.getStat("HELP").length()==0)
					helpText=normalizeHelpText("<RACE>"+L("No further information available"),skip);
				else
					helpText=normalizeHelpText("<RACE>"+R.getStat("HELP"),skip);
			}
		}
		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.achievements().getAchievementsHelp(helpKeyWSpaces,true),skip);
		}
		if(helpText==null)
		{
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((A.name().equalsIgnoreCase(helpKeyWSpaces))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
				{
					helpText = normalizeHelpText(CMLib.map().getArea(A.Name()).getAreaStats()+"",skip);
					if(helpText != null)
					{
						helpKey=A.name();
						break;
					}
				}
			}
		}

		// INEXACT searches start here
		if(helpText==null)
		{
			for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
			{
				final String key=((String)e.nextElement()).toUpperCase();
				if(key.startsWith(helpKey))
				{
					helpText=normalizeHelpText(rHelpFile.getProperty(key),skip);
					if(helpText != null)
					{
						helpKey=key;
						break;
					}
				}
			}
		}
		if(helpText==null)
		{
			final String currency=CMLib.english().matchAnyCurrencySet(helpKeyWSpaces);
			if(currency!=null)
			{
				final double denom=CMLib.english().matchAnyDenomination(currency,helpKeyWSpaces);
				if(denom>0.0)
				{
					final Coins C2=CMLib.beanCounter().makeCurrency(currency,denom,1);
					if((C2!=null)&&(C2.description().length()>0))
					{
						helpKey=helpKeyWSpaces;
						helpText=normalizeHelpText(C2.name()+" is "+C2.description().toLowerCase(),skip);
					}
				}
			}
		}

		if(helpText==null)
		{
			for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
			{
				final String key=((String)e.nextElement()).toUpperCase();
				if(CMLib.english().containsString(key,helpKey))
				{
					helpText=normalizeHelpText(rHelpFile.getProperty(key),skip);
					if(helpText!=null)
					{
						helpKey=key;
						break;
					}
				}
			}
		}

		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.socials().getSocialsHelp(forM,helpKey, false),skip);
		}

		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.clans().getGovernmentHelp(forM,helpKey, false),skip);
		}

		if(helpText==null)
		{
			Ability A=CMClass.findAbility(helpKey,-1,-1,false);
			if((A!=null)&&(A.isGeneric()))
				helpText=normalizeHelpText(A.getStat("HELP"),skip);
			else
			{
				A=CMClass.findAbility(helpKeyWSpaces,-1,-1,false);
				if((A!=null)&&(A.isGeneric()))
					helpText=normalizeHelpText(A.getStat("HELP"),skip);
			}
		}

		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.expertises().getExpertiseHelp(helpKey,false),skip);
		}
		if(helpText==null)
		{
			helpText=normalizeHelpText(CMLib.achievements().getAchievementsHelp(helpKeyWSpaces,false),skip);
		}

		if(helpText==null)
		{
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((CMLib.english().containsString(A.name(),helpKeyWSpaces))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
				{
					helpText=normalizeHelpText(CMLib.map().getArea(A.Name()).getAreaStats()+"",skip);
					if(helpText != null)
					{
						helpKey=A.name();
						break;
					}
				}
			}
		}
		if(helpText==null)
		{
			Deity D = CMLib.map().getDeity(helpKey);
			if(D==null)
			{
				D = CMLib.map().getDeity(helpKeyWSpaces);
				if(D!=null)
					helpKey=helpKeyWSpaces;
			}
			if(D != null)
			{
				final Command CMD=CMClass.getCommand("Deities");
				try
				{
					helpText=normalizeHelpText((String)CMD.executeInternal(forM, MUDCmdProcessor.METAFLAG_FORCED, D),skip);
					if(helpText!=null)
						helpKey = D.Name().toUpperCase();
				}
				catch(final Exception e)
				{
				}
			}
		}

		// the area exception -- why is this here?
		if(helpText==null)
		{
			if(CMLib.map().getArea(helpKey)!=null)
				return new Pair<String,String>(helpKey, normalizeHelpText(CMLib.map().getArea(helpKey).getAreaStats()+"",skip));
		}

		// internal exceptions
		if(helpText==null)
		{
			String s=CMLib.channels().findChannelName(helpKey.trim());
			boolean no=false;
			if(((s==null)||(s.length()==0))
			&&(helpKey.toLowerCase().startsWith("no")))
			{
				s=CMLib.channels().findChannelName(helpKey.trim().substring(2));
				no=true;
			}
			if((s!=null)&&(s.length()>0))
			{
				if(no)
					helpText=normalizeHelpText(rHelpFile.getProperty("NOCHANNEL"),skip);
				else
					helpText=normalizeHelpText(rHelpFile.getProperty("CHANNEL"),skip);
				if(helpText != null)
				{
					helpText=CMStrings.replaceAll(helpText,"[CHANNEL]",s.toUpperCase());
					helpText=CMStrings.replaceAll(helpText,"[channel]",s.toLowerCase());
					final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
					helpText=CMStrings.replaceAll(helpText,"[EXTRA]",extra);
					return new Pair<String,String>(helpKey, helpText);
				}
			}
		}

		// this is also in a weird place, but i guess because its fixing an obvious error
		if(helpText==null)
		{
			if(helpKey.indexOf(' ')>=0)
				helpKey=helpKey.replace(' ','_');
			for(final String suf : SKILL_SUFFIXES)
			{
				if(helpKey.endsWith(suf))
				{
					helpKey=helpKey.substring(0,helpKey.length()-suf.length());
					return new Pair<String,String>(helpKey, getHelpText(helpKey,rHelpFile,forM,noFix));
				}
			}
		}

		// **NOW resolve redirects!
		while((helpText!=null)
		&&(helpText.length()>0)
		&&(helpText.length()<31))
		{
			final String thisOtherTag=rHelpFile.getProperty(helpText);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(helpText)))
				helpText=null;
			else
			if(thisOtherTag!=null)
			{
				helpKey=helpText;
				helpText=thisOtherTag;
			}
			else
				break;
		}

		if(helpText==null)
			return null;

		if(noFix)
			return new Pair<String,String>(helpKey, helpText);
		final String finalHelpText = fixHelp(helpKey,helpText,forM);
		return new Pair<String,String>(helpKey, finalHelpText);
	}

	@Override
	public List<String> getHelpList(String helpStr,
									final Properties rHelpFile1,
									final Properties rHelpFile2,
									final MOB forM)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(' ')>=0)
			helpStr=helpStr.replace(' ','_');
		final List<String> matches=new Vector<String>();

		for(final Enumeration<Object> e=rHelpFile1.keys();e.hasMoreElements();)
		{
			final String key=(String)e.nextElement();
			final String prop=rHelpFile1.getProperty(key,"");
			if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.add(key.toUpperCase());
		}
		if(rHelpFile2!=null)
		{
			for(final Enumeration<Object> e=rHelpFile2.keys();e.hasMoreElements();)
			{
				final String key=(String)e.nextElement();
				final String prop=rHelpFile1.getProperty(key,"");
				if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
					matches.add(key.toUpperCase());
			}
		}
		return matches;
	}

	@Override
	public Properties getArcHelpFile()
	{
		try
		{
			Properties arcHelpFile=(Properties)Resources.getResource("ARCHON HELP FILE");
			if(arcHelpFile==null)
			{
				arcHelpFile=new Properties();
				final CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,CMFile.FLAG_LOGERRORS);
				if((directory.canRead())&&(directory.isDirectory()))
				{
					final String[] list=directory.list();
					for (final String item : list)
					{
						if((item!=null)
						&&(item.length()>0)
						&&item.toUpperCase().endsWith(".INI")
						&&(item.toUpperCase().startsWith("ARC_")))
						{
							final Properties local=new Properties();
							local.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,CMFile.FLAG_LOGERRORS).raw()));
							arcHelpFile.putAll(local);
							for(final Object key : local.keySet())
							{
								final String keyStr=key.toString();
								for(int i=0;i<keyStr.length();i++)
									if(Character.isLetter(keyStr.charAt(i))&&Character.isLowerCase(keyStr.charAt(i)))
										Log.debugOut("Lower key in "+item+": "+key+": "+local.getProperty(keyStr));
							}
						}
					}
				}
				for(final Enumeration<Object> e=arcHelpFile.keys();e.hasMoreElements();)
				{
					final String key=(String)e.nextElement();
					String entry=(String)arcHelpFile.get(key);
					final int x=entry.indexOf("<ZAP=");
					if(x>=0)
					{
						final int y=entry.indexOf('>',x);
						if(y>(x+5))
						{
							final String word=entry.substring(x+5,y).trim();
							entry=entry.substring(0,x)+CMLib.masking().maskHelp("\n\r",word)+entry.substring(y+1);
							arcHelpFile.remove(key);
							arcHelpFile.put(key,entry);
						}
					}
				}
				Resources.submitResource("ARCHON HELP FILE",arcHelpFile);
			}
			return arcHelpFile;
		}
		catch(final IOException e)
		{
			Log.errOut("MUDHelp",e);
		}
		return new Properties();
	}

	@Override
	public Properties getHelpFile()
	{
		try
		{
			Properties helpFile=(Properties)Resources.getResource("MAIN HELP FILE");
			if(helpFile==null)
			{
				helpFile=new Properties();
				final CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,CMFile.FLAG_LOGERRORS);
				if((directory.canRead())&&(directory.isDirectory()))
				{
					final String[] list=directory.list();
					for (final String element : list)
					{
						final String item=element;
						if((item!=null)
						&&(item.length()>0)
						&&item.toUpperCase().endsWith(".INI")
						&&(!item.toUpperCase().startsWith("ARC_")))
						{
							final Properties local=new Properties();
							local.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,CMFile.FLAG_LOGERRORS).raw()));
							helpFile.putAll(local);
							for(final Object key : local.keySet())
							{
								final String keyStr=key.toString();
								for(int i=0;i<keyStr.length();i++)
								{
									if(Character.isLetter(keyStr.charAt(i))&&Character.isLowerCase(keyStr.charAt(i)))
										Log.debugOut("Lower key in "+item+": "+key+": "+local.getProperty(keyStr));
								}
							}
						}
					}
				}
				for(final Enumeration<Object> e=helpFile.keys();e.hasMoreElements();)
				{
					final String key=(String)e.nextElement();
					String entry=(String)helpFile.get(key);
					final int x=entry.indexOf("<ZAP=");
					if(x>=0)
					{
						final int y=entry.indexOf('>',x);
						if(y>(x+5))
						{
							final String word=entry.substring(x+5,y).trim();
							entry=entry.substring(0,x)+CMLib.masking().maskHelp("\n\r",word)+entry.substring(y+1);
							helpFile.remove(key);
							helpFile.put(key,entry);
						}
					}
				}
				Resources.submitResource("MAIN HELP FILE",helpFile);
			}
			return helpFile;
		}
		catch(final IOException e)
		{
			Log.errOut("MUDHelp",e);
		}
		return new Properties();
	}

	@Override
	public String findHelpFile(final String key, final HelpSection searchSection, final boolean exactOnly)
	{
		final String ukey=key.toUpperCase().trim().replace(' ','_');
		final CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,CMFile.FLAG_LOGERRORS);
		final Filterer.TextFilter  arcFilter = new Filterer.TextFilter(".arc", true);
		final Filterer.NotFilterer<String> iniFilter = new Filterer.NotFilterer<String>(arcFilter);
		if((directory.canRead())&&(directory.isDirectory()))
		{
			final String[] list=directory.list();
			final List<Filterer<String>> filters;
			switch(searchSection)
			{
			case ArchonFirst:
				filters = new XVector<Filterer<String>>(arcFilter, iniFilter );
				break;
			case ArchonOnly:
				filters = new XVector<Filterer<String>>(arcFilter);
				break;
			default:
			case NormalFirst:
				filters = new XVector<Filterer<String>>(iniFilter, arcFilter );
				break;
			case NormalOnly:
				filters = new XVector<Filterer<String>>(iniFilter);
				break;
			}
			for(final Filterer<String> f : filters)
			{
				for (final String item : list)
				{
					if((item!=null)
					&&(item.length()>0)
					&&item.toUpperCase().endsWith(".INI")
					&&(f.passesFilter(item)))
					{
						final Properties local=new Properties();
						try
						{
							final CMFile F = new CMFile(Resources.buildResourcePath("help")+item,null,CMFile.FLAG_LOGERRORS);
							local.load(new ByteArrayInputStream(F.raw()));
							if(local.containsKey(ukey))
								return F.getAbsolutePath();
						}
						catch (final IOException e)
						{
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean addModifyHelpEntry(final MOB mob, final String helpFile, final String helpKey, final boolean deleteOnly)
	{
		final StringBuilder upStr=new StringBuilder("");
		final StringBuilder dnStr=new StringBuilder("");
		final CMFile F = new CMFile(helpFile, null, CMFile.FLAG_LOGERRORS);
		if(!F.exists())
			return false;
		final List<String> bV = Resources.getFileLineVector(F.text());
		final List<String> keyBlock = new ArrayList<String>();
		try
		{
			final BufferedReader br = new BufferedReader(new StringReader(CMParms.combineWith(bV, '\n')));
			String line = br.readLine();
			boolean contLine=false;
			while((line != null)&&(keyBlock.size()==0))
			{
				line = CMStrings.trimCRLF(line);
				if(!contLine)
				{
					if(line.startsWith(helpKey))
					{
						final String s = line.substring(helpKey.length());
						if(s.trim().startsWith("="))
						{
							// we have a winner!
							keyBlock.add(line.substring(line.indexOf('=')+1).trim());
						}
					}
				}
				if(keyBlock.size()==0)
					upStr.append(line).append("\n");
				contLine = line.endsWith("\\");
				line = br.readLine();
			}
			while(keyBlock.size()>0 && contLine && (line != null))
			{
				line = CMStrings.trimCRLF(line);
				keyBlock.add(line);
				contLine = line.endsWith("\\");
				line = br.readLine();
			}
			while(line != null)
			{
				line = CMStrings.trimCRLF(line);
				dnStr.append(line).append("\n");
				line = br.readLine();
			}
			br.close();
		}
		catch(final IOException e)
		{
			return false;
		}
		if(deleteOnly)
		{
			unloadHelpFile(null);
			while(upStr.toString().endsWith("\n")&&(dnStr.toString().startsWith("\n")))
				dnStr.deleteCharAt(0);
			if(upStr.toString().endsWith("\n")&&(dnStr.length()==0))
				upStr.deleteCharAt(upStr.length()-1);
			return F.saveText(upStr.toString()+dnStr.toString());
		}
		else
		{
			for(int i=0;i<keyBlock.size();i++)
			{
				final String s=keyBlock.get(i);
				if(s.endsWith("\\"))
					keyBlock.set(i, s.substring(0,s.length()-1));
			}
			CMLib.journals().makeMessageASync(mob, helpKey+": "+helpFile, keyBlock, false, new MsgMkrCallback()
			{
				final String key = helpKey;
				final StringBuilder b4 = new StringBuilder(upStr);
				final StringBuilder af = new StringBuilder(dnStr);
				final List<String> data = keyBlock;
				final CMFile file = F;
				@Override
				public void callBack(final MOB mob, final Session sess, final MsgMkrResolution res)
				{
					if(res == MsgMkrResolution.SAVEFILE)
					{
						final StringBuilder newHelp=new StringBuilder("");
						newHelp.append(b4.toString());
						if(data.size()>0)
						{
							if((!newHelp.toString().endsWith("\n"))&&(newHelp.length()>0))
								newHelp.append("\n");
							newHelp.append(key).append("=");
							for(int i=0;i<data.size();i++)
							{
								newHelp.append(data.get(i));
								if((i<data.size()-1)&&(!data.get(i).endsWith("\\")))
									newHelp.append("\\");
								newHelp.append("\n");
							}
							if((!af.toString().startsWith("\n"))&&(af.length()>0))
								newHelp.append("\n");
						}
						newHelp.append(af.toString());
						file.saveText(newHelp);
						unloadHelpFile(null);
					}
				}
			});
		}
		return true;
	}

	@Override
	public List<String> getSeeAlsoHelpOn(final String helpSearch, final String helpKey, final String helpText, final MOB mob, final int howMany)
	{
		final boolean canArc=CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP);
		final String[] seeAlso = seeAlsoCache.get(canArc+"/"+helpKey);
		if(seeAlso != null)
			return Arrays.asList(seeAlso);
		final String nKey = helpKey.replace(' ', '_');
		final List<String> otherHelps = new Vector<String>();
		final List<String> otherHelpTexts = new ArrayList<String>();
		otherHelpTexts.add(helpText);
		for(int i=1;i<(howMany*4) && (otherHelps.size()<howMany);i++)
		{
			final Pair<String, String> m = getHelpMatch(helpSearch,getHelpFile(),mob, i);
			if((m==null)
			||(m.second==null))
				break;
			if((m.first.replace(' ', '_').equalsIgnoreCase(nKey))
			||(otherHelps.contains(m.first))
			||(otherHelpTexts.contains(m.second)))
				continue;
			otherHelps.add(m.first);
			otherHelpTexts.add(m.second);
		}
		final Properties rHelpFile2=canArc?getArcHelpFile():null;
		if(canArc)
		{
			for(int i=1;i<(howMany*4) && (otherHelps.size()<howMany);i++)
			{
				final Pair<String, String> m = getHelpMatch(helpSearch,rHelpFile2,mob, i);
				if((m==null)
				||(m.second==null))
					break;
				if((m.first.replace(' ', '_').equalsIgnoreCase(nKey))
				||(otherHelps.contains(m.first))
				||(otherHelpTexts.contains(m.second)))
					continue;
				otherHelps.add(m.first);
				otherHelpTexts.add(m.second);
			}
		}
		if(otherHelps.size()==0)
		{
			final List<String> thisList = getHelpList( helpSearch, getHelpFile(), rHelpFile2, mob);
			for(final String s : thisList)
			{
				if(otherHelps.size()>=howMany)
					break;
				if((!s.replace(' ', '_').equalsIgnoreCase(nKey))
				&&(!otherHelps.contains(s))
				&&(getHelpFile().contains(s)||((rHelpFile2!=null)&&(rHelpFile2.contains(s)))))
				{
					Pair<String, String> m = getHelpMatch(s,getHelpFile(),mob, 0);
					if(((m==null)||(m.second==null))
					&&(rHelpFile2 != null))
						m = getHelpMatch(s,rHelpFile2,mob, 0);
					if((m==null)
					||(m.second==null)
					||(m.first.replace(' ', '_').equalsIgnoreCase(nKey))
					||(!m.first.replace(' ', '_').equalsIgnoreCase(s.replace(' ', '_')))
					||(otherHelps.contains(m.first))
					||(otherHelpTexts.contains(m.second)))
						continue;
					otherHelps.add(m.first);
					otherHelpTexts.add(m.second);
				}
			}
		}
		seeAlsoCache.put(canArc+"/"+helpKey,otherHelps.toArray(new String[otherHelps.size()]));
		return otherHelps;
	}

	@Override
	public void unloadHelpFile(final MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help/help.txt")!=null)
			Resources.removeResource("help/help.txt");
		if(Resources.getResource("help/accts.txt")!=null)
			Resources.removeResource("help/accts.txt");
		if(Resources.getResource("text/races.txt")!=null)
			Resources.removeResource("text/races.txt");
		if(Resources.getResource("text/newacct.txt")!=null)
			Resources.removeResource("text/newacct.txt");
		if(Resources.getResource("text/selchar.txt")!=null)
			Resources.removeResource("text/selchar.txt");
		if(Resources.getResource("text/newchar.txt")!=null)
			Resources.removeResource("text/newchar.txt");
		if(Resources.getResource("text/doneacct.txt")!=null)
			Resources.removeResource("text/doneacct.txt");
		if(Resources.getResource("text/stats.txt")!=null)
			Resources.removeResource("text/stats.txt");
		if(Resources.getResource("text/classes.txt")!=null)
			Resources.removeResource("text/classes.txt");
		if(Resources.getResource("text/alignment.txt")!=null)
			Resources.removeResource("text/alignment.txt");
		if(Resources.getResource("help/arc_help.txt")!=null)
			Resources.removeResource("help/arc_help.txt");
		if(Resources.getResource("MAIN HELP FILE")!=null)
			Resources.removeResource("MAIN HELP FILE");
		if(Resources.getResource("ARCHON HELP FILE")!=null)
			Resources.removeResource("ARCHON HELP FILE");
		seeAlsoCache.clear();

		// also the intro page
		final CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,CMFile.FLAG_FORCEALLOW);
		if(introDir.isDirectory())
		{
			final CMFile[] files=introDir.listFiles();
			for (final CMFile file : files)
				if(file.getName().toLowerCase().startsWith("intro")
				&&file.getName().toLowerCase().endsWith(".txt"))
					Resources.removeResource("text/"+file.getName());
		}

		if(Resources.getResource("text/offline.txt")!=null)
			Resources.removeResource("text/offline.txt");

		if(mob!=null)
			mob.tell(L("Help files unloaded. Next HELP, AHELP, new char will reload."));
	}

	@Override
	public boolean shutdown()
	{
		unloadHelpFile(null);
		return true;
	}

}
