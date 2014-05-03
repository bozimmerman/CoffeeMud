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
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String ID(){return "MUDHelp";}

	@Override
	public boolean isPlayerSkill(String helpStr)
	{
		if(helpStr.length()==0) return false;
		if(getHelpFile().size()==0) return false;
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(' ')>=0)
			helpStr=helpStr.replace(' ','_');
		if(helpStr.startsWith("SPELL_")
		 ||helpStr.startsWith("SONG_")
		 ||helpStr.startsWith("DANCE_")
		 ||helpStr.startsWith("BEHAVIOR_")
		 ||helpStr.startsWith("POWER_")
		 ||helpStr.startsWith("CHANT_")
		 ||helpStr.startsWith("PRAYER_")
		 ||helpStr.startsWith("SKILL_")
		 ||helpStr.startsWith("PLAY_"))
			return true;
		final String thisTag=getHelpFile().getProperty(helpStr);
		if((thisTag!=null)
		&&(thisTag.startsWith("<ABILITY>")||thisTag.startsWith("<EXPERTISE>")))
			return true;
		return CMClass.getAbility(helpStr)!=null;
	}

	@Override
	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp)
	{ return getHelpText(helpStr, forMOB, favorAHelp, false);}

	@Override
	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp, boolean noFix)
	{
		if(helpStr.length()==0) return null;
		StringBuilder thisTag=null;
		if(favorAHelp)
		{
			if(getArcHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB,noFix);
			if(thisTag==null)
			{
				if(getHelpFile().size()==0) return null;
				thisTag=getHelpText(helpStr,getHelpFile(),forMOB,noFix);
			}
		}
		else
		{
			if(getHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getHelpFile(),forMOB,noFix);
			if(thisTag==null)
			{
				if(getArcHelpFile().size()==0) return null;
				thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB,noFix);
			}
		}
		return thisTag;
	}

	@Override
	public List<String> getTopics(boolean archonHelp, boolean standardHelp)
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
					if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
						|| (rHelpFile.getProperty(thisTag)== null) )
							reverseList.addElement(ptop);
				}
				rHelpFile=getHelpFile();
			}
		}
		if(rHelpFile!=null)
		for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
		{
			final String ptop = (String)e.nextElement();
			final String thisTag=rHelpFile.getProperty(ptop);
			if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
				|| (rHelpFile.getProperty(thisTag)== null) )
					reverseList.addElement(ptop);
		}
		Collections.sort(reverseList);
		return reverseList;
	}

	@Override
	public String getActualUsage(Ability A, int which, MOB forMOB)
	{
		boolean destroymob=false;
		if(forMOB==null)
		{
			forMOB=CMClass.getFactoryMOB();
			forMOB.maxState().setMana(Integer.MAX_VALUE/2);
			forMOB.maxState().setMovement(Integer.MAX_VALUE/2);
			forMOB.maxState().setHitPoints(Integer.MAX_VALUE/2);
			destroymob=true;
		}
		else
		{
			final Ability myA=forMOB.fetchAbility(A.ID());
			if(myA!=null) A=myA;
		}

		final int[] consumption=A.usageCost(forMOB,true);
		int whichConsumed=consumption[0];
		switch(which)
		{
		case Ability.USAGE_MOVEMENT: whichConsumed=consumption[Ability.USAGEINDEX_MOVEMENT]; break;
		case Ability.USAGE_MANA: whichConsumed=consumption[Ability.USAGEINDEX_MANA]; break;
		case Ability.USAGE_HITPOINTS: whichConsumed=consumption[Ability.USAGEINDEX_HITPOINTS]; break;
		}
		if(destroymob) forMOB.destroy();
		if(whichConsumed==Integer.MAX_VALUE/2) return "all";
		return ""+whichConsumed;
	}

	@Override
	public void addHelpEntry(String ID, String text, boolean archon)
	{
		if(archon)
			getArcHelpFile().put(ID.toUpperCase(),text);
		else
			getHelpFile().put(ID.toUpperCase(),text);
	}

	private void appendAllowed(StringBuilder prepend, String ID)
	{
		final List<String> allows=new SVector<String>();
		for(final Iterator<String> i=CMLib.ableMapper().getAbilityAllowsList(ID);i.hasNext();)
			allows.add(i.next());
		ExpertiseLibrary.ExpertiseDefinition def=null;
		Ability A=null;
		if(allows.size()>0)
		{
			prepend.append("\n\rAllows   : ");
			String test1=null;
			String test2=null;
			boolean roman=false;
			for(int a0=0;a0<allows.size();a0++)
			{
				test1=allows.get(a0);
				int x=test1.length();
				roman=!Character.isDigit(test1.charAt(x-1));
				while(((Character.isDigit(test1.charAt(x-1))&&(!roman))
					||(CMath.isRomanDigit(test1.charAt(x-1))&&(roman)))
				&&(x>=0))
					x--;
				if((x>0)&&(x<test1.length()))
				{
					test1=test1.substring(0,x);
					for(int a1=allows.size()-1;a1>=(a0+1);a1--)
					{
						test2=allows.get(a1);
						if(test2.startsWith(test1)
						&&(((!roman)&&CMath.isInteger(test2.substring(x)))
						   ||(roman&&CMath.isRomanNumeral(test2.substring(x)))))
							allows.remove(a1);
					}
				}
			}
			int lastLine=11;
			for(int a=0;a<allows.size();a++)
			{
				final String allowStr=allows.get(a);
				def=CMLib.expertises().getDefinition(allowStr);
				if(def!=null)
				{
					prepend.append(def.name);
					lastLine+=(def.name.length()+2);
				}
				else
				{
					A=CMClass.getAbility(allowStr);
					if(A!=null)
					{
						prepend.append(A.Name());
						lastLine+=(A.Name().length()+2);
					}
				}
				if((lastLine>60)&&(a<(allows.size()-1)))
				{
					lastLine=11;
					prepend.append("\n\rAllows   : ");
				}
				else
				if(a<allows.size()-1)
					prepend.append(", ");

			}
		}
	}

	protected String columnHelper(String word, String msg, int wrap)
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
	public String fixHelp(String tag, String str, MOB forMOB)
	{
		boolean worldCurrency=str.startsWith("<CURRENCIES>");
		if(str.startsWith("<CURRENCY>")||worldCurrency)
		{
			str=str.substring(worldCurrency?12:10);
			final Vector<String> currencies=new Vector<String>();
			if((forMOB==null)||(forMOB.location()==null)||(worldCurrency))
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
				currencies.addElement(CMLib.beanCounter().getCurrency(forMOB.location()));
			final StringBuilder help=new StringBuilder("");
			if(worldCurrency)
				help.append("\n\r"+CMStrings.padRight("World Currencies",20)+":");
			for (final String currency : currencies)
			{
				if(worldCurrency)
					help.append("\n\r"+CMStrings.padRight("Currency",20)+":");
				else
					help.append("\n\r"+CMStrings.padRight("Local Currency",20)+":");
				if(currency.length()==0)
					help.append("default");
				else
					help.append(CMStrings.capitalizeAndLower(currency));
				final MoneyLibrary.MoneyDenomination denoms[]=CMLib.beanCounter().getCurrencySet(currency);
				for (final MoneyDenomination denom : denoms)
				{
					if(denom.abbr.length()>0)
						help.append("\n\r"+CMStrings.padRight(denom.name+" ("+denom.abbr+")",20)+":");
					else
						help.append("\n\r"+CMStrings.padRight(denom.name,20)+":");
					if(denom.value==CMLib.beanCounter().getLowestDenomination(currency))
						help.append(" (exchange rate is "+denom.value+" of base)");
					else
						help.append(" "+CMLib.beanCounter().getConvertableDescription(currency,denom.value));
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
				if(def.name.toUpperCase().replace(' ','_').equals(tag.toUpperCase()))
				{
					final StringBuilder prepend=new StringBuilder("");
					prepend.append("\n\rExpertise: "+def.name);
					prepend.append("\n\rRequires : "+CMLib.masking().maskDesc(def.allRequirements(),true));
					appendAllowed(prepend,def.ID);
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
		if(str.startsWith("<CHARCLASS>"))
		{
			str=str.substring(11);
			CharClass C = CMClass.findCharClass(tag);
			if(C==null) C=CMClass.findCharClass(tag.replace('_',' '));
			if(C!=null)
			{
				final StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forMOB!=null)&&(forMOB.session()!=null))
					wrap=forMOB.session().getWrap();
				if(wrap <=0 ) wrap=78;
				prepend.append("^HChar Class: ^N"+C.name()+" ^H(^N"+C.baseClass()+"^H)^N");
				prepend.append("\n\r");
				prepend.append(columnHelper("^HMax-Stats :^N",C.getMaxStatDesc(),wrap));
				prepend.append(columnHelper("^HQualifiers:^N",C.getStatQualDesc(),wrap));
				prepend.append(columnHelper("^HRaces     :^N",C.getRaceQualDesc(),wrap));
				prepend.append("^H"+CMStrings.padRight("Prime Stat: ^N"+C.getPrimeStatDesc(),(wrap/2)));
				prepend.append("^HAttack Pts: ^N"+C.getAttackDesc());
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight("Practices : ^N"+C.getPracticeDesc(),(wrap/2)));
				prepend.append("^HTrains    : ^N"+C.getTrainDesc());
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight("Hit Points: ^N"+C.getHitPointDesc(),(wrap/2)));
				prepend.append("^HMana      : ^N"+C.getManaDesc());
				prepend.append("\n\r");
				prepend.append("^H"+CMStrings.padRight("Movement  : ^N"+C.getMovementDesc(),(wrap/2)));
				prepend.append("^HDamage Pts: ^N"+C.getDamageDesc());
				prepend.append("\n\r");
				prepend.append("^HWeapons   : ^N"+C.getWeaponLimitDesc());
				prepend.append("\n\r");
				prepend.append("^HArmor     : ^N"+C.getArmorLimitDesc());
				prepend.append("\n\r");
				prepend.append(columnHelper("^HBonuses   :^N",C.getOtherBonusDesc(),wrap));
				prepend.append(columnHelper("^HLimits    :^N",C.getOtherLimitsDesc(),wrap));
				prepend.append("^HDesc.     : ^N");
				str=prepend.toString()+"\n\r"+str;
			}
		}
		if(str.startsWith("<RACE>"))
		{
			str=str.substring(6);
			Race R=CMClass.findRace(tag);
			if(R==null) R=CMClass.findRace(tag.replace('_',' '));
			if(R!=null)
			{
				final StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forMOB!=null)&&(forMOB.session()!=null))
					wrap=forMOB.session().getWrap();
				if(wrap <=0 ) wrap=78;
				prepend.append("^HRace Name : ^N"+R.name()+" ^H(^N"+R.racialCategory()+"^H)^N");
				prepend.append("\n\r");

				String s=R.getStatAdjDesc();
				if(R.getTrainAdjDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getTrainAdjDesc();
				if(R.getPracAdjDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getPracAdjDesc();
				prepend.append(columnHelper("^HStat Mods.:^N",s,wrap));
				s=R.getSensesChgDesc();
				if(R.getDispositionChgDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getDispositionChgDesc();
				if(R.getAbilitiesDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getAbilitiesDesc();
				prepend.append(columnHelper("^HAbilities :^N",s,wrap));
				prepend.append(columnHelper("^HLanguages :^N",R.getLanguagesDesc(),wrap));
				prepend.append(columnHelper("^HLife Exp. :^N",R.getAgingChart()[Race.AGE_ANCIENT]+" years",wrap));
				prepend.append("^HDesc.     : ^N");
				str=prepend.toString()+"\n\r"+str;
			}
		}
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

			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(((A.ID().equalsIgnoreCase(tag)||A.ID().equalsIgnoreCase(subTag))
						&&((type<0)||(type==(A.classificationCode()&Ability.ALL_ACODES)))
					||(A.name().equalsIgnoreCase(name)))
				&&(!helpedPreviously.contains(A)))
				{
					helpedPreviously.addElement(A);
					final StringBuilder prepend=new StringBuilder("");
					type=(A.classificationCode()&Ability.ALL_ACODES);
					prepend.append("\n\r");
					switch(type)
					{
					case Ability.ACODE_SPELL:
						prepend.append(CMStrings.padRight("Spell",9));
						break;
					case Ability.ACODE_PRAYER:
						prepend.append(CMStrings.padRight("Prayer",9));
						break;
					case Ability.ACODE_CHANT:
						prepend.append(CMStrings.padRight("Chant",9));
						break;
					case Ability.ACODE_SUPERPOWER:
						prepend.append(CMStrings.padRight("SuperPower",9));
						break;
					case Ability.ACODE_SONG:
						prepend.append(CMStrings.padRight("Song",9));
						break;
					default:
						prepend.append(CMStrings.padRight("Skill",9));
						break;
					}
					prepend.append(": "+A.name());
					if((forMOB!=null)&&(forMOB.session()!=null)&&(!forMOB.session().isStopped()))
					{
						final Ability A2=forMOB.fetchAbility(A.ID());
						if(A2!=null) prepend.append("   (Proficiency: "+A2.proficiency()+"%)");
					}
					if((A.classificationCode()&Ability.ALL_DOMAINS)>0)
					{
						prepend.append("\n\rDomain   : ");
						final int school=(A.classificationCode()&Ability.ALL_DOMAINS)>>5;
						prepend.append(CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[school].replace('_',' ')));
					}
					final Vector<String> avail=new Vector<String>();
					final Hashtable<Integer,int[]> sortedByLevel=new Hashtable<Integer,int[]>();
					for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
					{
						final CharClass C=c.nextElement();
						final int lvl=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID());
						if((!C.ID().equalsIgnoreCase("Archon"))
						&&(lvl>=0)
						&&(C.availabilityCode()!=0)
						&&(!CMLib.ableMapper().getSecretSkill(C.ID(),true,A.ID())))
						{
							if(!sortedByLevel.containsKey(Integer.valueOf(lvl)))
								sortedByLevel.put(Integer.valueOf(lvl),new int[1]);
							sortedByLevel.get(Integer.valueOf(lvl))[0]++;
							avail.addElement(C.name(lvl)+"("+lvl+")");
						}
					}
					for(final Enumeration<Integer> e=sortedByLevel.keys();e.hasMoreElements();)
					{
						final Integer I=e.nextElement();
						final int[] count=sortedByLevel.get(I);
						if(count[0]>2)
						{
							String s=null;
							final String endsWith="("+I.intValue()+")";
							for(int i=avail.size()-1;i>=0;i--)
							{
								s=avail.elementAt(i);
								if(s.endsWith(endsWith))
									avail.removeElementAt(i);
							}
							if(count[0]>5)
								avail.addElement("Numerous Classes"+endsWith);
							else
								avail.addElement("Several Classes"+endsWith);
						}
					}
					for(int c=0;c<avail.size();c++)
					{
						if((c%4)==0)
							prepend.append("\n\rAvailable: ");
						prepend.append((avail.elementAt(c))+" ");
					}

					DVector preReqs;
					if(forMOB==null)
						preReqs=CMLib.ableMapper().getCommonPreRequisites(A);
					else
						preReqs=CMLib.ableMapper().getCommonPreRequisites(forMOB,A);
					if(preReqs.size()>0)
					{
						final String names=CMLib.ableMapper().formatPreRequisites(preReqs);
						prepend.append("\n\rRequires : "+names);
					}
					final String mask=CMLib.ableMapper().getCommonExtraMask(A);
					if((mask!=null)&&(mask.length()>0))
						prepend.append("\n\rRequires : "+CMLib.masking().maskDesc(mask,true));
					appendAllowed(prepend,A.ID());
					if(type==Ability.ACODE_PRAYER)
					{
						String rangeDescs=null;
						for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
						{
							final Faction F=e.nextElement();
							rangeDescs=F.usageFactorRangeDescription(A);
							if(rangeDescs.length()>0)
								prepend.append("\n\r"+CMStrings.capitalizeAndLower(F.name())+": "+rangeDescs);
						}
					}

					if(!A.isAutoInvoked())
					{
						final Vector<AbilityComponent> components=(Vector<AbilityComponent>)CMLib.ableMapper().getAbilityComponentMap().get(A.ID().toUpperCase());
						if(components!=null)
						{
							prepend.append("\n\rComponent: ");
							prepend.append(CMLib.ableMapper().getAbilityComponentDesc(forMOB,A.ID()));
						}
						prepend.append("\n\rUse Cost : ");
						if(A.usageType()==Ability.USAGE_NADA)
							prepend.append("None");
						if(CMath.bset(A.usageType(),Ability.USAGE_MANA))
							prepend.append("Mana ("+getActualUsage(A,Ability.USAGE_MANA,forMOB)+") ");
						if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
							prepend.append("Movement ("+getActualUsage(A,Ability.USAGE_MOVEMENT,forMOB)+") ");
						if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
							prepend.append("Hit Points ("+getActualUsage(A,Ability.USAGE_HITPOINTS,forMOB)+") ");
						prepend.append("\n\rQuality  : ");
						switch(A.abstractQuality())
						{
						case Ability.QUALITY_MALICIOUS:
							prepend.append("Malicious");
							break;
						case Ability.QUALITY_BENEFICIAL_OTHERS:
						case Ability.QUALITY_BENEFICIAL_SELF:
							prepend.append("Always Beneficial");
							break;
						case Ability.QUALITY_OK_OTHERS:
						case Ability.QUALITY_OK_SELF:
							prepend.append("Sometimes Beneficial");
							break;
						case Ability.QUALITY_INDIFFERENT:
							prepend.append("Circumstantial");
							break;
						}
						prepend.append("\n\rTargets  : ");
						if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
						||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
							prepend.append("Caster only");
						else
						if((CMClass.basicItems().hasMoreElements())
						&&(CMClass.mobTypes().hasMoreElements())
						&&(CMClass.exits().hasMoreElements())
						&&(CMClass.locales().hasMoreElements()))
						{
							if(A.canAffect(Ability.CAN_ITEMS)||A.canTarget(Ability.CAN_ITEMS))
								prepend.append("Items ");
							if(A.canAffect(Ability.CAN_MOBS)||A.canTarget(Ability.CAN_MOBS))
								prepend.append("Creatures ");
							if(A.canAffect(Ability.CAN_EXITS)||A.canTarget(Ability.CAN_EXITS))
								prepend.append("Exits ");
							if(A.canAffect(Ability.CAN_ROOMS)||A.canTarget(Ability.CAN_ROOMS))
								prepend.append("Rooms ");
						}
						else
						if(A.abstractQuality()==Ability.QUALITY_INDIFFERENT)
							prepend.append("Items or Rooms");
						else
						if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
							prepend.append("Others");
						else
						if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
						||(A.abstractQuality()==Ability.QUALITY_OK_SELF))
							prepend.append("Caster, or others");
						prepend.append("\n\rRange    : ");
						final int min=A.minRange();
						final int max=A.maxRange();
						if(min+max==0)
							prepend.append("Touch, or not applicable");
						else
						{
							if(min==0)
								prepend.append("Touch");
							else
								prepend.append("Range "+min);
							if(max>0)
								prepend.append(" - Range "+max);
						}
						if((A.triggerStrings()!=null)
						   &&(A.triggerStrings().length>0))
						{
							prepend.append("\n\rCommands : ");
							for(int i=0;i<A.triggerStrings().length;i++)
							{
								prepend.append(A.triggerStrings()[i]);
								if(i<(A.triggerStrings().length-1))
								   prepend.append(", ");
							}
						}
					}
					else
						prepend.append("\n\rInvoked  : Automatic");
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
		try
		{
			if(str!=null)
				return CMLib.webMacroFilter().virtualPageFilter(str);
		}catch(final com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException x){}
		return str;
	}

	@Override
	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB)
	{
		return getHelpText(helpStr,rHelpFile,forMOB,false);
	}

	@Override
	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(' ')>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=null;

		final CharClass C=CMClass.findCharClass(helpStr.toUpperCase());
		if((C!=null)&&(C.isGeneric()))
			thisTag="<CHARCLASS>"+C.getStat("HELP");
		final Race R=CMClass.findRace(helpStr.toUpperCase());
		if((R!=null)&&(R.isGeneric()))
			thisTag="<RACE>"+R.getStat("HELP");

		boolean found=false;
		if(thisTag==null) thisTag=rHelpFile.getProperty(helpStr);
		boolean areaTag=(thisTag==null)&&helpStr.startsWith("AREAHELP_");
		if(thisTag==null){thisTag=rHelpFile.getProperty("SPELL_"+helpStr); if(thisTag!=null) helpStr="SPELL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PRAYER_"+helpStr); if(thisTag!=null) helpStr="PRAYER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SONG_"+helpStr); if(thisTag!=null) helpStr="SONG_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("DANCE_"+helpStr); if(thisTag!=null) helpStr="DANCE_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PLAY_"+helpStr); if(thisTag!=null) helpStr="PLAY_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("CHANT_"+helpStr); if(thisTag!=null) helpStr="CHANT_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("BEHAVIOR_"+helpStr); if(thisTag!=null) helpStr="BEHAVIOR_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("POWER_"+helpStr); if(thisTag!=null) helpStr="POWER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SKILL_"+helpStr); if(thisTag!=null) helpStr="SKILL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PROP_"+helpStr); if(thisTag!=null) helpStr="PROP_"+helpStr;}

		// specific calling out of a channel
		if(helpStr.startsWith("CHANNEL_")||helpStr.startsWith("NOCHANNEL_"))
		{
			String s=CMLib.channels().findChannelName(helpStr.substring(8).trim());
			boolean no=false;
			if(((s==null)||(s.length()==0))
			&&(helpStr.startsWith("NO")))
			{
				s=CMLib.channels().findChannelName(helpStr.trim().substring(10));
				no=true;
			}
			if((s!=null)&&(s.length()>0))
			{
				if(no)
					thisTag=rHelpFile.getProperty("NOCHANNEL");
				else
					thisTag=rHelpFile.getProperty("CHANNEL");
				thisTag=CMStrings.replaceAll(thisTag,"[CHANNEL]",s.toUpperCase());
				thisTag=CMStrings.replaceAll(thisTag,"[channel]",s.toLowerCase());
				final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
				thisTag=CMStrings.replaceAll(thisTag,"[EXTRA]",extra);
				return new StringBuilder(thisTag);
			}
		}
		found=((thisTag!=null)&&(thisTag.length()>0));

		if(!found)
		{
			String ahelpStr=helpStr.replaceAll("_"," ").trim();
			if(areaTag) ahelpStr=ahelpStr.substring(9);
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if(A.name().equalsIgnoreCase(ahelpStr))
				{
					helpStr=A.name();
					found=true;
					areaTag=true;
					break;
				}
			}
		}

		if((!areaTag)&&(!found))
		{
			final String ahelpStr=helpStr.replaceAll("_"," ").trim();
			if(!found)
			{
				final String s=CMLib.socials().getSocialsHelp(forMOB,helpStr.toUpperCase(), true);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
			if(!found)
			{
				final String s=CMLib.clans().getGovernmentHelp(forMOB,helpStr.toUpperCase(), true);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
			if(!found)
			{
				final Ability A=CMClass.findAbility(helpStr.toUpperCase(),-1,-1,true);
				if((A!=null)&&(A.isGeneric()))
				{
					thisTag=A.getStat("HELP");
					found=true;
				}
			}
			if(!found)
			{
				final String s=CMLib.expertises().getExpertiseHelp(helpStr.toUpperCase(),true);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
			if(!found)
			{
				final Deity D = CMLib.map().getDeity(helpStr);
				if(D != null)
				{
					final Command CMD=CMClass.getCommand("Deities");
					try
					{
						thisTag=(String)CMD.executeInternal(forMOB, Command.METAFLAG_FORCED, D);
						helpStr = D.Name().toUpperCase();
					}catch(final Exception e){}
				}

			}
			// INEXACT searches start here
			if(!found)
				for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
				{
					final String key=((String)e.nextElement()).toUpperCase();
					if(key.startsWith(helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}
			if(!found)
			{
				final String currency=CMLib.english().matchAnyCurrencySet(ahelpStr);
				if(currency!=null)
				{
					final double denom=CMLib.english().matchAnyDenomination(currency,ahelpStr);
					if(denom>0.0)
					{
						final Coins C2=CMLib.beanCounter().makeCurrency(currency,denom,1);
						if((C2!=null)&&(C2.description().length()>0))
							return new StringBuilder(C2.name()+" is "+C2.description().toLowerCase());
					}
				}
			}

			if(!found)
				for(final Enumeration<Object> e=rHelpFile.keys();e.hasMoreElements();)
				{
					final String key=((String)e.nextElement()).toUpperCase();
					if(CMLib.english().containsString(key,helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}

			if(!found)
			{
				final String s=CMLib.socials().getSocialsHelp(forMOB,helpStr.toUpperCase(), false);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}

			if(!found)
			{
				final String s=CMLib.clans().getGovernmentHelp(forMOB,helpStr.toUpperCase(), false);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}

			if(!found)
			{
				final Ability A=CMClass.findAbility(helpStr.toUpperCase(),-1,-1,false);
				if((A!=null)&&(A.isGeneric()))
				{
					thisTag=A.getStat("HELP");
					found=true;
				}
			}

			if(!found)
				for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
				{
					final Area A=e.nextElement();
					if(CMLib.english().containsString(A.name(),ahelpStr))
					{
						helpStr=A.name();
						break;
					}
				}
			if(!found)
			{
				final String s=CMLib.expertises().getExpertiseHelp(helpStr.toUpperCase(),false);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
		}
		while((thisTag!=null)&&(thisTag.length()>0)&&(thisTag.length()<31)&&(!areaTag))
		{
			final String thisOtherTag=rHelpFile.getProperty(thisTag);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(thisTag)))
				thisTag=null;
			else
			if(thisOtherTag!=null)
			{
				helpStr=thisTag;
				thisTag=thisOtherTag;
			}
			else
				break;
		}

		// the area exception
		if((thisTag==null)||(thisTag.length()==0))
			if(CMLib.map().getArea(helpStr.trim())!=null)
				return new StringBuilder(CMLib.map().getArea(helpStr.trim()).getAreaStats().toString());

		// internal exceptions
		if((thisTag==null)||(thisTag.length()==0))
		{
			String s=CMLib.channels().findChannelName(helpStr.trim());
			boolean no=false;
			if(((s==null)||(s.length()==0))
			&&(helpStr.toLowerCase().startsWith("no")))
			{
				s=CMLib.channels().findChannelName(helpStr.trim().substring(2));
				no=true;
			}
			if((s!=null)&&(s.length()>0))
			{
				if(no)
					thisTag=rHelpFile.getProperty("NOCHANNEL");
				else
					thisTag=rHelpFile.getProperty("CHANNEL");
				if(thisTag != null)
				{
					thisTag=CMStrings.replaceAll(thisTag,"[CHANNEL]",s.toUpperCase());
					thisTag=CMStrings.replaceAll(thisTag,"[channel]",s.toLowerCase());
					final String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
					thisTag=CMStrings.replaceAll(thisTag,"[EXTRA]",extra);
					return new StringBuilder(thisTag);
				}
			}
		}

		if((thisTag==null)||(thisTag.length()==0))
		{
			if(helpStr.indexOf(' ')>=0) helpStr=helpStr.replace(' ','_');
			if(helpStr.endsWith("_SPELL")) return getHelpText(helpStr.substring(0,helpStr.length()-6),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_PRAYER")) return getHelpText(helpStr.substring(0,helpStr.length()-7),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_SONG")) return getHelpText(helpStr.substring(0,helpStr.length()-5),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_DANCE")) return getHelpText(helpStr.substring(0,helpStr.length()-6),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_PLAY")) return getHelpText(helpStr.substring(0,helpStr.length()-5),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_CHANT")) return getHelpText(helpStr.substring(0,helpStr.length()-6),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_SKILL")) return getHelpText(helpStr.substring(0,helpStr.length()-6),rHelpFile,forMOB,noFix);
			if(helpStr.endsWith("_POWER")) return getHelpText(helpStr.substring(0,helpStr.length()-6),rHelpFile,forMOB,noFix);
			return null;
		}
		if(noFix) return new StringBuilder(thisTag);
		return new StringBuilder(fixHelp(helpStr,thisTag,forMOB));
	}

	@Override
	public StringBuilder getHelpList(String helpStr,
								   Properties rHelpFile1,
								   Properties rHelpFile2,
								   MOB forMOB)
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
		for(final Enumeration<Object> e=rHelpFile2.keys();e.hasMoreElements();)
		{
			final String key=(String)e.nextElement();
			final String prop=rHelpFile1.getProperty(key,"");
			if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.add(key.toUpperCase());
		}
		if(matches.size()==0)
			return new StringBuilder("");
		return CMLib.lister().fourColumns(forMOB,matches);
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
									if(Character.isLetter(keyStr.charAt(i))&&Character.isLowerCase(keyStr.charAt(i)))
										Log.debugOut("Lower key in "+item+": "+key+": "+local.getProperty(keyStr));
							}
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
	public void unloadHelpFile(MOB mob)
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
			mob.tell(_("Help files unloaded. Next HELP, AHELP, new char will reload."));
	}
}
