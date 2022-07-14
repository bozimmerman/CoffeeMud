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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;

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

	protected Map<String,String>		genUsageCost				= new SHashtable<String, String>();

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
			costStr.append(L("Mana (@x1) ",CMLib.help().getActualAbilityUsageDesc(A,Ability.USAGE_MANA,forM)));
		if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
			costStr.append(L("Movement (@x1) ",CMLib.help().getActualAbilityUsageDesc(A,Ability.USAGE_MOVEMENT,forM)));
		if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
			costStr.append(L("Hit Points (@x1) ",CMLib.help().getActualAbilityUsageDesc(A,Ability.USAGE_HITPOINTS,forM)));
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
	public String getHelpText(final String helpStr, final Properties rHelpFile, final MOB forM)
	{
		return getHelpText(helpStr,rHelpFile,forM,false);
	}

	protected String getHelpText(String helpKey, final Properties rHelpFile, final MOB forM, final boolean noFix)
	{
		final String unfixedHelpKey = helpKey;
		helpKey=helpKey.toUpperCase().trim();
		final String origHelpKey = helpKey;
		if(helpKey.indexOf(' ')>=0)
			helpKey=helpKey.replace(' ','_');
		String helpText=null;

		CharClass C=CMClass.findCharClass(helpKey);
		if(C==null)
			C=CMClass.findCharClass(origHelpKey);
		if((C!=null)&&(C.isGeneric()))
			helpText="<CHARCLASS>"+C.getStat("HELP");

		Race R=CMClass.findRace(helpKey);
		if(R==null)
			R=CMClass.findRace(origHelpKey);
		if((R!=null)
		&&((CMProps.isTheme(R.availabilityCode()) && (R.getStat("HELP").length()>0))
			|| (rHelpFile == this.getArcHelpFile())))
		{
			if(R.getStat("HELP").length()==0)
				helpText="<RACE>"+L("No further information available");
			else
				helpText="<RACE>"+R.getStat("HELP");
		}

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
				if(no)
					helpText=rHelpFile.getProperty("NOCHANNEL");
				else
					helpText=rHelpFile.getProperty("CHANNEL");
				helpText=CMStrings.replaceAll(helpText,"[CHANNEL]",s.toUpperCase());
				helpText=CMStrings.replaceAll(helpText,"[channel]",s.toLowerCase());
				final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
				helpText=CMStrings.replaceAll(helpText,"[EXTRA]",extra);
				return helpText;
			}
		}

		// specifically calling out an area
		if(helpKey.startsWith("AREAHELP_"))
		{
			final String ahelpStr=origHelpKey.substring(9);
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((A.name().equalsIgnoreCase(ahelpStr))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
					return CMLib.map().getArea(helpKey.trim()).getAreaStats().toString();
			}
		}

		// now start the exact, or darn near-exact matches
		// ** maintaining the actual helpKey index into rHelpFile is necessary!

		boolean found=false;
		if(helpText==null)
			helpText=rHelpFile.getProperty(helpKey);

		if(helpText == null)
		{
			for(int i=0;i<SKILL_PREFIXES.length;i++)
			{
				final String prefix = SKILL_PREFIXES[i];
				helpText=rHelpFile.getProperty(prefix+helpKey);
				if(helpText!=null)
				{
					helpKey=prefix+helpKey;
					break;
				}
			}
		}

		found=((helpText!=null)&&(helpText.length()>0));

		if(!found)
		{
			String s=CMLib.socials().getSocialsHelp(forM,helpKey.toUpperCase(), true);
			if((s==null)&&(origHelpKey.indexOf(' ')<0))
				s=CMLib.socials().getSocialsHelp(forM,origHelpKey,true);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}
		if(!found)
		{
			String s=CMLib.clans().getGovernmentHelp(forM,helpKey.toUpperCase(), true);
			if(s==null)
				s=CMLib.clans().getGovernmentHelp(forM,origHelpKey,true);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}
		if(!found)
		{
			Ability A=CMClass.findAbility(helpKey.toUpperCase(),-1,-1,true);
			if(A==null)
				A=CMClass.findAbility(origHelpKey,-1,-1,true);
			if((A!=null)&&(A.isGeneric()))
			{
				helpText=A.getStat("HELP");
				found=true;
			}
		}
		if(!found)
		{
			String s=CMLib.expertises().getExpertiseHelp(helpKey.toUpperCase(),true);
			if(s==null)
				s=CMLib.expertises().getExpertiseHelp(origHelpKey,true);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}
		if(!found)
		{
			Deity D = CMLib.map().getDeity(helpKey);
			if(D==null)
				D = CMLib.map().getDeity(origHelpKey);
			if(D != null)
			{
				final Command CMD=CMClass.getCommand("Deities");
				try
				{
					helpText=(String)CMD.executeInternal(forM, MUDCmdProcessor.METAFLAG_FORCED, D);
					helpKey = D.Name().toUpperCase();
				}
				catch(final Exception e)
				{
				}
			}

		}
		if(!found)
		{
			final String s=CMLib.achievements().getAchievementsHelp(unfixedHelpKey.toUpperCase(),true);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}
		if(!found)
		{
			final String ahelpStr=origHelpKey;
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((A.name().equalsIgnoreCase(ahelpStr))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
				{
					helpKey=A.name();
					helpText = CMLib.map().getArea(helpKey.trim()).getAreaStats().toString();
					found=true;
					break;
				}
			}
		}

		// INEXACT searches start here
		if(!found)
		{
			for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
			{
				final String key=((String)e.nextElement()).toUpperCase();
				if(key.startsWith(helpKey))
				{
					helpText=rHelpFile.getProperty(key);
					helpKey=key;
					found=true;
					break;
				}
			}
		}
		if(!found)
		{
			final String currency=CMLib.english().matchAnyCurrencySet(origHelpKey);
			if(currency!=null)
			{
				final double denom=CMLib.english().matchAnyDenomination(currency,origHelpKey);
				if(denom>0.0)
				{
					final Coins C2=CMLib.beanCounter().makeCurrency(currency,denom,1);
					if((C2!=null)&&(C2.description().length()>0))
					{
						helpText = C2.name()+" is "+C2.description().toLowerCase();
						found=true;
					}
				}
			}
		}

		if(!found)
		{
			for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
			{
				final String key=((String)e.nextElement()).toUpperCase();
				if(CMLib.english().containsString(key,helpKey))
				{
					helpText=rHelpFile.getProperty(key);
					helpKey=key;
					found=true;
					break;
				}
			}
		}

		if(!found)
		{
			final String s=CMLib.socials().getSocialsHelp(forM,helpKey.toUpperCase(), false);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}

		if(!found)
		{
			final String s=CMLib.clans().getGovernmentHelp(forM,helpKey.toUpperCase(), false);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}

		if(!found)
		{
			final Ability A=CMClass.findAbility(helpKey.toUpperCase(),-1,-1,false);
			if((A!=null)&&(A.isGeneric()))
			{
				helpText=A.getStat("HELP");
				found=true;
			}
		}

		if(!found)
		{
			final String s=CMLib.expertises().getExpertiseHelp(helpKey.toUpperCase(),false);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}
		if(!found)
		{
			final String s=CMLib.achievements().getAchievementsHelp(unfixedHelpKey.toUpperCase(),false);
			if(s!=null)
			{
				helpText=s;
				helpKey=helpKey.toUpperCase();
				found=true;
			}
		}

		if(!found)
		{
			final String[] split=helpKey.split("_");
			if("SOCIAL".startsWith(split[0].toUpperCase()))
			{
				final String tempHelpStr=CMParms.combineWith(Arrays.asList(split),' ',1,split.length);
				final String s=CMLib.socials().getSocialsHelp(forM,tempHelpStr.toUpperCase(), false);
				if(s!=null)
				{
					helpText=s;
					helpKey=tempHelpStr.toUpperCase().replace(' ','_');
					found=true;
				}
			}
		}
		if(!found)
		{
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((CMLib.english().containsString(A.name(),origHelpKey))
				&&((forM==null)||(CMLib.flags().canAccess(forM, A))))
				{
					helpKey=A.name();
					helpText = CMLib.map().getArea(helpKey.trim()).getAreaStats().toString();
					found=true;
					break;
				}
			}
		}
		while((helpText!=null)&&(helpText.length()>0)&&(helpText.length()<31))
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

		// the area exception
		if((helpText==null)||(helpText.length()==0))
		{
			if(CMLib.map().getArea(helpKey.trim())!=null)
				return CMLib.map().getArea(helpKey.trim()).getAreaStats().toString();
		}

		// internal exceptions
		if((helpText==null)||(helpText.length()==0))
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
					helpText=rHelpFile.getProperty("NOCHANNEL");
				else
					helpText=rHelpFile.getProperty("CHANNEL");
				if(helpText != null)
				{
					helpText=CMStrings.replaceAll(helpText,"[CHANNEL]",s.toUpperCase());
					helpText=CMStrings.replaceAll(helpText,"[channel]",s.toLowerCase());
					final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
					helpText=CMStrings.replaceAll(helpText,"[EXTRA]",extra);
					return helpText;
				}
			}
		}

		if((helpText==null)||(helpText.length()==0))
		{
			if(helpKey.indexOf(' ')>=0)
				helpKey=helpKey.replace(' ','_');
			for(final String suf : SKILL_SUFFIXES)
			{
				if(helpKey.endsWith(suf))
					return getHelpText(helpKey.substring(0,helpKey.length()-suf.length()),rHelpFile,forM,noFix);
			}
			return null;
		}
		if(noFix)
			return helpText;
		return fixHelp(helpKey,helpText,forM);
	}

	@Override
	public String getHelpList(String helpStr,
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
		if(matches.size()==0)
			return "";
		return CMLib.lister().build4ColTable(forM,matches).toString();
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
	public boolean shutdown()
	{
		unloadHelpFile(null);
		return true;
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
}
