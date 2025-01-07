package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.AreaScriptNext.AreaScriptInstance;
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class AbilityData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityData";
	}

	static final String[][] newMatches = new String[][] {
		new String[] { "NEWABILITY", "GenAbility" },
		new String[] { "NEWLANGUAGE", "GenLanguage" },
		new String[] { "NEWCRAFTSKILL", "GenCraftSkill" },
		new String[] { "NEWWRIGHTSKILL", "GenWrightSkill" },
		new String[] { "NEWGATHERINGSKILL", "GenGatheringSkill" },
		new String[] { "NEWTRAP", "GenTrap" },
	};

	private String itemList(final List<Item> itemList, Item oldItem, final String oldValue)
	{
		final StringBuffer list=new StringBuffer("");
		if(oldItem==null)
			oldItem=CMLib.webMacroFilter().getItemFromCatalog(oldValue);
		for (final Item I : itemList)
		{
			list.append("<OPTION VALUE=\""+CMLib.webMacroFilter().findItemWebCacheCode(itemList, I)+"\" ");
			if((oldItem!=null)&&(oldItem.sameAs(I)))
				list.append("SELECTED");
			list.append(">");
			list.append(I.Name()+CMLib.webMacroFilter().getWebCacheSuffix(I));
		}
		list.append("<OPTION VALUE=\"\">------ CATALOGED -------");
		final String[] names=CMLib.catalog().getCatalogItemNames();
		for (final String name : names)
		{
			list.append("<OPTION VALUE=\"CATALOG-"+name+"\"");
			if((oldItem!=null)
			&&(CMLib.flags().isCataloged(oldItem))
			&&(oldItem.Name().equalsIgnoreCase(name)))
				list.append(" SELECTED");
			list.append(">"+name);
		}
		return list.toString();
	}

	public static StringBuffer interprets(final Language E, final HTTPRequest httpReq, final java.util.Map<String,String> parms, final int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("INTERPRETS"))
		{
			final List<String> theclasses=new ArrayList<String>();
			if(httpReq.isUrlParameter("INTERPRET1"))
			{
				int num=1;
				String ID=httpReq.getUrlParameter("INTERPRET"+num);
				while(ID!=null)
				{
					if(ID.length()>0)
						theclasses.add(ID);
					num++;
					ID=httpReq.getUrlParameter("INTERPRET"+num);
				}
			}
			else
			for(final String ID : E.languagesSupported())
			{
				if(ID!=null)
					theclasses.add(ID);
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.get(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditInterpret(this);\" NAME=INTERPRET"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"EditInterpret(this);\" NAME=INTERPRET"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Language");

			Object[] sortedB=null;
			final List<String> sortMeB=new ArrayList<String>();
			for(final Enumeration<Ability> b=CMClass.abilities(new Filterer<Ability>() {
				@Override
				public boolean passesFilter(Ability obj)
				{
					return (obj instanceof Language)
							&&(!obj.ID().equals("GenLanguage"))
							&&(!obj.ID().equals("StdLanguage"))
							;
				}

			});b.hasMoreElements();)
			{
				final Ability A=b.nextElement();
				if(!theclasses.contains(A.ID()))
					sortMeB.add(A.ID());
			}
			sortedB=(new TreeSet<String>(sortMeB)).toArray();
			for(int r=0;r<sortedB.length;r++)
			{
				final String cnam=(String)sortedB[r];
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final MOB mob=Authenticate.getAuthenticatedMob(httpReq);

		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}

		String last=httpReq.getUrlParameter("ABILITY");
		if(last==null)
			return " @break@";
		Ability A=(Ability)httpReq.getRequestObjects().get("ABILITY-"+last);
		boolean isGeneric = (A==null)?false:A.isGeneric();
		for(final String[] newMatch : newMatches)
		{
			String newAbilityID=httpReq.getUrlParameter(newMatch[0]);
			final String newClass=newMatch[1];
			if((A==null)
			&&(newAbilityID!=null)
			&&(newAbilityID.length()>0))
			{
				newAbilityID = CMStrings.replaceAll(newAbilityID, " ", "");
				A=CMClass.getAbility(newAbilityID);
				if(A == null)
					A=(Ability)CMClass.getAbility(newClass).copyOf();
				else
				{
					final Ability CR;
					if(A.isGeneric())
					{
						newAbilityID=newAbilityID+"_Copy";
						httpReq.addFakeUrlParameter(newMatch[0],newAbilityID);
						CR = CMClass.getAbility(A.getStat("JAVACLASS"));
						CR.setStat("CLASS9", newAbilityID);
						CR.setStat("LEVEL","1");
						CR.setStat("NAME", newAbilityID);
						for(int i=1;i<A.getStatCodes().length;i++)
							CR.setStat(A.getStatCodes()[i], A.getStat(A.getStatCodes()[i]));
					}
					else
					{
						CR=CMLib.ableParms().convertAbilityToGeneric(A);
						CR.setStat("CLASS9", newAbilityID);
						CR.setStat("NAME", A.Name());
						CMClass.addClass(CMObjectType.ABILITY, A);
					}
					A=CR;
				}
				if(newClass.endsWith("Trap"))
					A.setStat("LEVEL","1");
				A.setStat("CLASS9",newAbilityID);
				last=newAbilityID;
				httpReq.addFakeUrlParameter("ABILITY",newAbilityID);
				httpReq.getRequestObjects().put("ABILITY-"+newAbilityID,A);
				isGeneric=true;
				break;
			}
		}
		if(last.length()>0)
		{
			if(A==null)
			{
				A=CMClass.getAbility(last);
				if(A!=null)
					isGeneric = A.isGeneric();
			}
			if(parms.containsKey("ISNEWABILITY"))
				return ""+(CMClass.getAbility(last)==null);
			if(A!=null)
			{
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("ISGENERIC"))
				{
					return ""+isGeneric;
				}
				if(parms.containsKey("ISLANGUAGE"))
				{
					return Boolean.toString(A instanceof Language);
				}
				if(parms.containsKey("ISCRAFTSKILL"))
				{
					return Boolean.toString((A instanceof ItemCraftor)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
							&&(((ItemCraftor)A).getCraftorType()!=CraftorType.LargeConstructions))
							;
				}
				if(parms.containsKey("ISWRIGHTSKILL"))
				{
					return Boolean.toString((A instanceof ItemCraftor)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
							&&(((ItemCraftor)A).getCraftorType()==CraftorType.LargeConstructions));
				}
				if(parms.containsKey("ISTRAP"))
				{
					return Boolean.toString(A instanceof Trap);
				}
				if(parms.containsKey("ISBOMB"))
				{
					if(A instanceof Trap)
						return CMath.s_bool(A.getStat("ISBOMB"))?"CHECKED ":"";
				}
				if(parms.containsKey("ISGATHERSKILL"))
				{
					return Boolean.toString((A instanceof ItemCollection)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL));
				}
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
						old=A.name();
					str.append(old+", ");
				}
				if(parms.containsKey("GENHELP"))
				{
					String old=httpReq.getUrlParameter("GENHELP");
					if(old==null)
						old=A.getStat("HELP");
					str.append(old+", ");
				}

				if(parms.containsKey("LOWESTQUALLEVEL"))
				{
					final int lql = CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					str.append(lql+", ");
				}

				if(parms.containsKey("BASEMANACOST"))
				{
					final int[] mcs = A.usageCost(null, false);
					final StringBuilder costs = new StringBuilder("");
					if(mcs[Ability.USAGEINDEX_MANA] != 0)
						costs.append(mcs[Ability.USAGEINDEX_MANA]+"m ");
					if(mcs[Ability.USAGEINDEX_MOVEMENT] != 0)
						costs.append(mcs[Ability.USAGEINDEX_MOVEMENT]+"v ");
					if(mcs[Ability.USAGEINDEX_HITPOINTS] != 0)
						costs.append(mcs[Ability.USAGEINDEX_HITPOINTS]+"h ");
					str.append(costs.toString()+", ");
				}

				if(parms.containsKey("CHARCLASSLEVEL"))
				{
					final String old=httpReq.getUrlParameter("CLASS");
					if(old != null)
					{
						str.append(CMLib.ableMapper().getQualifyingLevel(old, true, A.ID())).append(", ");
					}
				}

				if(parms.containsKey("CHARCLASSNEXT"))
				{
					if(parms.containsKey("RESET"))
					{
						httpReq.removeUrlParameter("CLASS");
						return "";
					}
					String lastID="";
					final String clast = httpReq.getUrlParameter("CLASS");
					final boolean showAll=parms.containsKey("ALL");
					final boolean includeSkillOnly=parms.containsKey("INCLUDESKILLONLY");
					for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
					{
						final CharClass C=c.nextElement();
						if(((CMProps.isTheme(C.availabilityCode()))||showAll)
						&&((!CMath.bset(C.availabilityCode(), Area.THEME_SKILLONLYMASK))
								||CMSecurity.isCharClassEnabled(C.ID())||includeSkillOnly||showAll)
						&&(!CMSecurity.isCharClassDisabled(C.ID()))
						&&(CMLib.ableMapper().getQualifyingLevel(C.ID(), true, A.ID())>=0))
						{
							if((clast==null)||((clast.length()>0)&&(clast.equals(lastID))&&(!C.ID().equals(lastID))))
							{
								httpReq.addFakeUrlParameter("CLASS",C.ID());
								return "";
							}
							lastID=C.ID();
						}
					}
					httpReq.addFakeUrlParameter("CLASS","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					return " @break@";
				}
				if(A instanceof Language)
				{
					if(parms.containsKey("NATURALLANG"))
					{
						String old=httpReq.getUrlParameter("NATURALLANG");
						if(old==null)
							old=CMath.s_bool(A.getStat("NATURALLANG"))?"on":"";
						str.append(old.equalsIgnoreCase("on")?"CHECKED, ":", ");
					}
					if(parms.containsKey("TRANSVERB"))
					{
						String old=httpReq.getUrlParameter("TRANSVERB");
						if(old==null)
							old=((Language) A).getTranslationVerb();
						str.append(old.trim()+", ");
					}
					if(parms.containsKey("WORDLISTS"))
					{
						List<String[]> wordLists=((Language)A).translationLists(A.ID());
						if(httpReq.isUrlParameter("WORDLIST1"))
						{
							wordLists=new ArrayList<String[]>();
							int x=1;
							while(httpReq.isUrlParameter("WORDLIST"+x))
							{
								wordLists.add(CMParms.parseCommas(httpReq.getUrlParameter("WORDLIST"+x), true).toArray(new String[0]));
								x++;
							}
						}
						else
						{
							if(wordLists == null)
								wordLists=new ArrayList<String[]>();
							for(int i=wordLists.size()-1;i>=0;i--)
								httpReq.addFakeUrlParameter("WORDLIST"+(i+1), CMParms.toListString(wordLists.get(i)));
							httpReq.removeUrlParameter("WORDLIST"+(wordLists.size()+1));
						}

						for(int i=wordLists.size()-1;i>=0;i--)
						{
							if(wordLists.get(i).length==0)
								wordLists.remove(i);
							else
								break;
						}

						if(parms.containsKey("RESET"))
						{
							httpReq.removeUrlParameter("WORDLISTNUM");
							httpReq.removeUrlParameter("WORDLISTNEXT");
							return "";
						}
						else
						if(parms.containsKey("NEXT"))
						{
							String lastID="";
							final String lastNum = httpReq.getUrlParameter("WORDLISTNUM");
							String nextName = "WORDLIST1";
							for(int i=0;i<wordLists.size();i++)
							{
								final String thisName="WORDLIST"+Integer.toString(i+1);
								nextName="WORDLIST"+Integer.toString(i+2);
								if((lastNum==null)||((lastNum.length()>0)&&(lastNum.equals(lastID))&&(!thisName.equals(lastID))))
								{
									httpReq.addFakeUrlParameter("WORDLISTNUM",thisName);
									last=thisName;
									return "";
								}
								lastID=thisName;
							}
							httpReq.addFakeUrlParameter("WORDLISTNUM","");
							httpReq.addFakeUrlParameter("WORDLISTNEXT",nextName);
							if(parms.containsKey("EMPTYOK"))
								return "<!--EMPTY-->";
							return " @break@";
						}
					}

					if(parms.containsKey("INTERPRETS"))
						str.append(interprets((Language)A, httpReq, parms, 0));

					if(parms.containsKey("HASHWORDS"))
					{
						Map<String,String> hashWords=((Language)A).translationHash(A.ID());
						if(httpReq.isUrlParameter("HASHWORD1"))
						{
							hashWords=new Hashtable<String,String>();
							int x=1;
							while(httpReq.isUrlParameter("HASHWORD"+x))
							{
								final String word=httpReq.getUrlParameter("HASHWORD"+x).toUpperCase().trim();
								final String def=httpReq.getUrlParameter("HASHWORDDEF"+x);
								if((def!=null)&&(def.length()>0)&&(word.length()>0))
									hashWords.put(word,def);
								x++;
							}
						}
						else
						{
							int x=1;
							for(final String key : hashWords.keySet())
							{
								httpReq.addFakeUrlParameter("HASHWORD"+x, key);
								httpReq.addFakeUrlParameter("HASHWORDDEF"+x, hashWords.get(key));
								x++;
							}
							httpReq.removeUrlParameter("HASHWORD"+x);
							httpReq.removeUrlParameter("HASHWORDDEF"+x);
						}

						if(parms.containsKey("RESET"))
						{
							httpReq.removeUrlParameter("HASHWORDNUM");
							httpReq.removeUrlParameter("HASHWORDDEFNUM");
							httpReq.removeUrlParameter("HASHWORDNEXT");
							httpReq.removeUrlParameter("HASHWORDDEFNEXT");
							return "";
						}
						else
						if(parms.containsKey("NEXT"))
						{
							final String lastNum = httpReq.getUrlParameter("HASHWORDNUM");
							String nextName = "HASHWORD1";
							String lastID="";
							String nextDefName = "HASHWORDDEF1";
							for(int i=1;i<=hashWords.keySet().size();i++)
							{
								final String thisName="HASHWORD"+Integer.toString(i);
								final String thisDefName="HASHWORDDEF"+Integer.toString(i);
								nextName="HASHWORD"+Integer.toString(i+1);
								nextDefName="HASHWORDDEF"+Integer.toString(i+1);
								if((lastNum==null)||((lastNum.length()>0)&&(lastNum.equals(lastID))&&(!thisName.equals(lastNum))))
								{
									httpReq.addFakeUrlParameter("HASHWORDNUM",thisName);
									httpReq.addFakeUrlParameter("HASHWORDDEFNUM",thisDefName);
									last=thisName;
									return "";
								}
								lastID=thisName;
								//i++;
							}
							httpReq.addFakeUrlParameter("HASHWORDNUM","");
							httpReq.addFakeUrlParameter("HASHWORDDEFNUM","");
							httpReq.addFakeUrlParameter("HASHWORDNEXT",nextName);
							httpReq.addFakeUrlParameter("HASHWORDDEFNEXT",nextDefName);
							if(parms.containsKey("EMPTYOK"))
								return "<!--EMPTY-->";
							return " @break@";
						}
					}
				}

				if((A instanceof Trap)
				&&(A.isGeneric()))
				{
					if(parms.containsKey("DMGT"))
					{
						String old=httpReq.getUrlParameter("DMGT");
						if(old==null)
							old=""+A.getStat("DMGT");
						str.append("<OPTION VALUE=\"\" "+((old.length()==0)?" SELECTED":"")+">N/A");
						for (final String element : Weapon.TYPE_DESCS)
							str.append("<OPTION VALUE=\""+element+"\""+(old.equalsIgnoreCase(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
						str.append(", ");
					}
					if(parms.containsKey("DMGM"))
					{
						String old=httpReq.getUrlParameter("DMGM");
						if(old==null)
							old=""+A.getStat("DMGM");
						str.append("<OPTION VALUE=\""+((old.length()==0)?" SELECTED":"")+">N/A");
						for (int e=0;e<CMMsg.TYPE_DESCS.length;e++)
						{
							final String element=CMMsg.TYPE_DESCS[e];
							if(CMParms.contains(CharStats.DEFAULT_STAT_MSG_MAP,e))
								str.append("<OPTION VALUE=\""+element+"\""+(old.equalsIgnoreCase(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
						}
						str.append(", ");
					}
					if(parms.containsKey("ABLEID"))
					{
						String old=httpReq.getUrlParameter("ABLEID");
						if(old==null)
							old=""+A.getStat("ABILITY");
						final List<String> sortMeB=new ArrayList<String>();
						for(final Enumeration<Ability> b=CMClass.abilities(new Filterer<Ability>() {
							@Override
							public boolean passesFilter(Ability obj)
							{
								return (obj.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON;
							}
						});b.hasMoreElements();)
						{
							final Ability A1=b.nextElement();
							sortMeB.add(A1.ID());
						}
						str.append("<OPTION VALUE=\"\""+((old.length()==0)?" SELECTED":"")+">N/A");
						for (final String ableID : new TreeSet<String>(sortMeB))
							str.append("<OPTION VALUE=\""+ableID+"\""+(old.equalsIgnoreCase(ableID)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(ableID));
						str.append(", ");
					}
					if(parms.containsKey("COMPS"))
					{
						httpReq.addFakeUrlParameter("COMPONENT", A.ID());
						final String key="COMP4_"+A.ID().toUpperCase();
						final Map<String,List<AbilityComponent>> o = new HashMap<String,List<AbilityComponent>>();
						CMLib.ableComponents().addAbilityComponent(key+"="+A.getStat("ACOMP"), o);
						httpReq.getRequestObjects().putAll(o);
					}
					for(final String p : A.getStatCodes())
					{
						if(parms.containsKey("MOD_"+p))
						{
							String old=httpReq.getUrlParameter("MOD_"+p);
							if(old==null)
								old=""+A.getStat(p);
							str.append(old+", ");
						}
					}
				}

				// here starts CLASSIFICATION
				if(parms.containsKey("CLASSIFICATION_ACODE"))
				{
					String old=httpReq.getUrlParameter("CLASSIFICATION_ACODE");
					if(old==null)
						old=""+(A.classificationCode()&Ability.ALL_ACODES);
					for(int i=0;i<Ability.ACODE.DESCS.size();i++)
					{
						if(A instanceof ItemCraftor)
						{
							if(i==Ability.ACODE_COMMON_SKILL)
								str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.ACODE.DESCS.get(i)));
						}
						else
						if(A instanceof ItemCollection)
						{
							if(i==Ability.ACODE_COMMON_SKILL)
								str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.ACODE.DESCS.get(i)));
						}
						else
						if(A instanceof Language)
						{
							if(i==Ability.ACODE_LANGUAGE)
								str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.ACODE.DESCS.get(i)));
						}
						else
							str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.ACODE.DESCS.get(i)));
					}
					str.append(", ");
				}
				if(parms.containsKey("CLASSIFICATION_DOMAIN"))
				{
					String old=httpReq.getUrlParameter("CLASSIFICATION_DOMAIN");
					if(old==null)
						old=""+((A.classificationCode()&Ability.ALL_DOMAINS)>>5);
					for(int i=0;i<Ability.DOMAIN.DESCS.size();i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.DOMAIN.DESCS.get(i)));
					str.append(", ");
				}
				// here ends CLASSIFICATION

				if(parms.containsKey("TRIGSTR"))
				{
					String old=httpReq.getUrlParameter("TRIGSTR");
					if(old==null)
						old=CMParms.toListString(A.triggerStrings());
					// remember to sort by longest->shortest on put-back
					str.append(old.toUpperCase().trim()+", ");
				}
				if(parms.containsKey("MINRANGE"))
				{
					String old=httpReq.getUrlParameter("MINRANGE");
					if(old==null)
						old=""+A.minRange();
					for(int i=0;i<Ability.RANGE_CHOICES.length;i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.RANGE_CHOICES[i]));
					str.append(", ");
				}
				if(parms.containsKey("MAXRANGE"))
				{
					String old=httpReq.getUrlParameter("MAXRANGE");
					if(old==null)
						old=""+A.maxRange();
					for(int i=0;i<Ability.RANGE_CHOICES.length;i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.RANGE_CHOICES[i]));
					str.append(", ");
				}
				if(parms.containsKey("TICKSBETWEENCASTS"))
				{
					String old=httpReq.getUrlParameter("TICKSBETWEENCASTS");
					if(old==null)
						old=""+A.getStat("TICKSBETWEENCASTS");
					str.append(old+", ");
				}
				if(parms.containsKey("TICKSOVERRIDE"))
				{
					String old=httpReq.getUrlParameter("TICKSOVERRIDE");
					if(old==null)
						old=""+A.getStat("TICKSOVERRIDE");
					str.append(old+", ");
				}
				if(parms.containsKey("DISPLAY")) // affected string
				{
					String old=httpReq.getUrlParameter("DISPLAY");
					if(old==null)
						old=A.displayText();
					str.append(old+", ");
				}
				if(parms.containsKey("LEVEL"))
				{
					String old=httpReq.getUrlParameter("LEVEL");
					if(old==null)
						old=A.getStat("LEVEL");
					str.append(old+", ");
				}
				if(parms.containsKey("BASELEVEL"))
				{
					String old=httpReq.getUrlParameter("BASELEVEL");
					if(old==null)
						old=A.getStat("BASELEVEL");
					str.append(old+", ");
				}
				if(parms.containsKey("PERMRESET"))
				{
					String old=httpReq.getUrlParameter("PERMRESET");
					if(old==null)
						old=A.getStat("PERMRESET");
					str.append(old+", ");
				}
				if(parms.containsKey("AUTOINVOKE"))
				{
					String old=httpReq.getUrlParameter("AUTOINVOKE");
					if(old==null)
						old=A.getStat("AUTOINVOKE");
					else
						old=""+old.equalsIgnoreCase("on");
					str.append(CMath.s_bool(old)?"CHECKED":"");
					str.append(", ");
				}
				if(parms.containsKey("TICKAFFECTS"))
				{
					String old=httpReq.getUrlParameter("TICKAFFECTS");
					if(old==null)
						old=A.getStat("TICKAFFECTS");
					else
						old=""+old.equalsIgnoreCase("on");
					str.append(CMath.s_bool(old)?"CHECKED":"");
					str.append(", ");
				}
				if(parms.containsKey("ABILITY_FLAGS"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("ABILITY_FLAGS"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("ABILITY_FLAGS"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("ABILITY_FLAGS"+id));
					}
					else
						list=CMParms.parseCommas(A.getStat("FLAGS"),true);
					for (final String element : Ability.FLAG_DESCS)
						str.append("<OPTION VALUE=\""+element+"\""+(list.contains(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
					str.append(", ");
				}
				if(parms.containsKey("CUSTOMOVERRIDEMANA"))
				{
					String old=httpReq.getUrlParameter("CUSTOMOVERRIDEMANA");
					if(old==null)
						old=""+A.getStat("OVERRIDEMANA");
					final int x=CMath.s_int(old);
					if((x>0) && (x<Ability.COST_PCT))
						str.append(old+", ");
				}
				if(parms.containsKey("OVERRIDEMANA"))
				{
					String old=httpReq.getUrlParameter("OVERRIDEMANA");
					if(old==null)
						old=""+A.getStat("OVERRIDEMANA");
					final int o=CMath.s_int(old);
					str.append("<OPTION VALUE=\"-1\""+((o==-1)?" SELECTED":"")+">Use Default");
					str.append("<OPTION VALUE=\"0\""+((o==0)?" SELECTED":"")+">None (free skill)");
					str.append("<OPTION VALUE=\"\" "+(((o>0)&&(o<Ability.COST_PCT))?" SELECTED":"")+">Custom Value");
					str.append("<OPTION VALUE=\""+Ability.COST_ALL+"\""+((o==Ability.COST_ALL)?" SELECTED":"")+">All Mana");
					for(int v=Ability.COST_ALL-5;v>=Ability.COST_ALL-95;v-=5)
					{
						str.append("<OPTION VALUE=\""+v+"\""+(((o>(v-5))&&(o<=v))?" SELECTED":"")+">"+(Ability.COST_ALL-v)+"%");
					}
					str.append(", ");
				}
				if(parms.containsKey("USAGEMASK"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("USAGEMASK"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("USAGEMASK"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("USAGEMASK"+id));
					}
					else
						list=CMParms.parseCommas(A.getStat("USAGEMASK"),true);
					for (final String element : Ability.USAGE_DESCS)
						str.append("<OPTION VALUE=\""+element+"\""+(list.contains(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
					str.append(", ");
				}
				if(parms.containsKey("CANAFFECTMASK"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("CANAFFECTMASK"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("CANAFFECTMASK"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("CANAFFECTMASK"+id));
					}
					else
						list=CMParms.parseCommas(A.getStat("CANAFFECTMASK"),true);
					for (final String element : Ability.CAN_DESCS)
						str.append("<OPTION VALUE=\""+element+"\""+(list.contains(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
					str.append(", ");
				}
				if(parms.containsKey("CANTARGETMASK"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("CANTARGETMASK"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("CANTARGETMASK"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("CANTARGETMASK"+id));
					}
					else
						list=CMParms.parseCommas(A.getStat("CANTARGETMASK"),true);
					for (final String element : Ability.CAN_DESCS)
						str.append("<OPTION VALUE=\""+element+"\""+(list.contains(element)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(element));
					str.append(", ");
				}
				if(parms.containsKey("VQUALITY")) //QUALITY
				{
					String old=httpReq.getUrlParameter("VQUALITY");
					if(old==null)
						old=""+A.abstractQuality();
					for(int i=0;i<Ability.QUALITY_DESCS.length;i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.QUALITY_DESCS[i]));
					str.append(", ");
				}
				if(parms.containsKey("HERESTATS")) // affect adj: Prop_HereAdjuster
				{
					String old=httpReq.getUrlParameter("HERESTATS");
					if(old==null)
						old=A.getStat("HERESTATS");
					str.append(super.htmlOutgoingFilter(old)+", ");
				}
				if(parms.containsKey("SCRIPT"))
				{
					String old=httpReq.getUrlParameter("SCRIPT");
					if(old==null)
						old=A.getStat("SCRIPT");
					str.append(old+", ");
				}
				if(parms.containsKey("CASTMASK"))
				{
					String old=httpReq.getUrlParameter("CASTMASK");
					if(old==null)
						old=A.getStat("CASTMASK");
					str.append(old+", ");
				}
				if(parms.containsKey("NUMARGS"))
				{
					String old=httpReq.getUrlParameter("NUMARGS");
					if(old==null)
						old=A.getStat("NUMARGS");
					str.append(old+", ");
				}
				if(parms.containsKey("TARGETMASK"))
				{
					String old=httpReq.getUrlParameter("TARGETMASK");
					if(old==null)
						old=A.getStat("TARGETMASK");
					str.append(old+", ");
				}
				if(parms.containsKey("FIZZLEMSG"))
				{
					String old=httpReq.getUrlParameter("FIZZLEMSG");
					if(old==null)
						old=A.getStat("FIZZLEMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("MOCKABLETEXT"))
				{
					String old=httpReq.getUrlParameter("MOCKABLETEXT");
					if(old==null)
						old=A.getStat("MOCKABLETEXT");
					str.append(old+", ");
				}
				if(parms.containsKey("TARGETFAILMSG"))
				{
					String old=httpReq.getUrlParameter("TARGETFAILMSG");
					if(old==null)
						old=A.getStat("TARGETFAILMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("UNINVOKEMSG"))
				{
					String old=httpReq.getUrlParameter("UNINVOKEMSG");
					if(old==null)
						old=A.getStat("UNINVOKEMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("AUTOCASTMSG"))
				{
					String old=httpReq.getUrlParameter("AUTOCASTMSG");
					if(old==null)
						old=A.getStat("AUTOCASTMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("CASTMSG"))
				{
					String old=httpReq.getUrlParameter("CASTMSG");
					if(old==null)
						old=A.getStat("CASTMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("FILENAME"))
				{
					String old=httpReq.getUrlParameter("FILENAME");
					if(old==null)
						old=A.getStat("FILENAME");
					str.append(old+", ");
				}
				if(parms.containsKey("VERB"))
				{
					String old=httpReq.getUrlParameter("VERB");
					if(old==null)
						old=A.getStat("VERB");
					str.append(old+", ");
				}
				if(parms.containsKey("SOUND"))
				{
					String old=httpReq.getUrlParameter("SOUND");
					if(old==null)
						old=A.getStat("SOUND");
					str.append(old+", ");
				}
				if(parms.containsKey("POSTCASTMSG"))
				{
					String old=httpReq.getUrlParameter("POSTCASTMSG");
					if(old==null)
						old=A.getStat("POSTCASTMSG");
					str.append(old+", ");
				}
				if(parms.containsKey("ATTACKCODE"))
				{
					String old=httpReq.getUrlParameter("ATTACKCODE");
					if(old==null)
						old=""+CMParms.indexOf(CMMsg.TYPE_DESCS,A.getStat("ATTACKCODE"));
					for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CMMsg.TYPE_DESCS[i]));
					str.append(", ");
				}
				if(parms.containsKey("CANMEND"))
				{
					String old=httpReq.getUrlParameter("CANMEND");
					if(old==null)
						old=A.getStat("CANMEND");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANDOOR"))
				{
					String old=httpReq.getUrlParameter("CANDOOR");
					if(old==null)
						old=A.getStat("CANDOOR");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANTITLE"))
				{
					String old=httpReq.getUrlParameter("CANTITLE");
					if(old==null)
						old=A.getStat("CANTITLE");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANDESC"))
				{
					String old=httpReq.getUrlParameter("CANDESC");
					if(old==null)
						old=A.getStat("CANDESC");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CLANONLY"))
				{
					String old=httpReq.getUrlParameter("CLANONLY");
					if(old==null)
						old=A.getStat("CLANONLY");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANREFIT"))
				{
					String old=httpReq.getUrlParameter("CANREFIT");
					if(old==null)
						old=A.getStat("CANREFIT");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANBUNDLE"))
				{
					String old=httpReq.getUrlParameter("CANBUNDLE");
					if(old==null)
						old=A.getStat("CANBUNDLE");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("CANSIT"))
				{
					String old=httpReq.getUrlParameter("CANSIT");
					if(old==null)
						old=A.getStat("CANSIT");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("ISCOSMETIC"))
				{
					String old=httpReq.getUrlParameter("ISCOSMETIC");
					if(old==null)
						old=A.getStat("ISCOSMETIC");
					else
						old=old.equalsIgnoreCase("on")?"true":"false";
					str.append(old.equalsIgnoreCase("true")?"checked":"");
				}
				if(parms.containsKey("MATLIST"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("MATLIST"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("MATLIST"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("MATLIST"+id).toUpperCase().trim());
					}
					else
						list=CMParms.parseCommas(A.getStat("MATLIST"),true);
					for(final RawMaterial.Material m : RawMaterial.Material.values())
						str.append("<OPTION VALUE=\""+m.name()+"\""+(list.contains(m.name())?" SELECTED":"")+">"+m.noun());
					for(int i=0;i<RawMaterial.CODES.NAMES().length;i++)
						str.append("<OPTION VALUE=\""+RawMaterial.CODES.NAMES()[i]+"\""+(list.contains(RawMaterial.CODES.NAMES()[i])?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(RawMaterial.CODES.NAMES()[i]));
					str.append(", ");
				}

				if(parms.containsKey("ITEMXML")
				&&(A instanceof ItemCollection))
				{
					final List<Item> itemList=new XVector<Item>(((ItemCollection)A).items());
					final ArrayList<String> oldValues=new ArrayList<String>();
					int which=1;
					final String httpKeyName="ITEMXML";
					String oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
					if(oldValue == null)
					{
						for(final Item I : itemList)
							oldValues.add(CMLib.webMacroFilter().findItemWebCacheCode(itemList, I));
					}
					else
					{
						while(oldValue!=null)
						{
							if((!oldValue.equalsIgnoreCase("DELETE"))&&(oldValue.length()>0))
								oldValues.add(oldValue);
							which++;
							oldValue=httpReq.getUrlParameter(httpKeyName+"_"+which);
						}
					}
					String newKey = httpReq.getUrlParameter("NEWITEM");
					if(newKey != null)
					{
						final int x=newKey.indexOf('=');
						if(x > 0)
							newKey = newKey.substring(x+1).trim();
						final Item newItem = CMLib.webMacroFilter().findItemInWebCache(newKey);
						if(newItem != null)
						{
							itemList.add(newItem);
							((ItemCollection)A).addItem(newItem);
							oldValues.add(newKey);
						}
					}
					oldValues.add("");
					for(int i=0;i<oldValues.size();i++)
					{
						oldValue=oldValues.get(i);
						final Item oldItem=(oldValue.length()>0)?CMLib.webMacroFilter().findItemInAnything(itemList,oldValue):null;
						str.append("<TR><TD><SELECT NAME="+httpKeyName+"_"+(i+1)+" ONCHANGE=\"ReShow();\">");
						if(i<oldValues.size()-1)
							str.append("<OPTION VALUE=\"DELETE\">Delete!");
						if(oldValue.length()==0)
							str.append("<OPTION VALUE=\"\" "+((oldValue.length()==0)?"SELECTED":"")+">");
						str.append(itemList(itemList,oldItem,oldValue));
						str.append("</SELECT></TD></TR>");
						if(i==oldValues.size()-1)
							str.append("<TR><TD ALIGN=CENTER><INPUT TYPE=BUTTON NAME=BUTT_"+httpKeyName+" VALUE=\"NEW\" ONCLICK=\"AddNewItem();\"></TD></TR>");
					}
				}

				if(parms.containsKey("POSTCASTAFFECT"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("POSTCASTAFFECT"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("POSTCASTAFFECT"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("POSTCASTAFFECT"+id).toUpperCase());
					}
					else
						list=CMParms.parseSemicolons(A.getStat("POSTCASTAFFECT").toUpperCase(),true);
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A2=e.nextElement();
						if(((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(!CMSecurity.isASysOp(mob)))
							continue;
						final String AID=A2.ID();
						final String ANAME=CMStrings.ellipse(A2.ID()+" ("+A2.name()+")",40);
						str.append("<OPTION VALUE=\""+AID+"\""+(list.contains(AID.toUpperCase())?" SELECTED":"")+">"+ANAME);
					}
					str.append(", ");
				}
				if(parms.containsKey("MOCKABILITY"))
				{
					String id;
					if(httpReq.isUrlParameter("MOCKABILITY"))
						id=httpReq.getUrlParameter("MOCKABILITY").toUpperCase().trim();
					else
						id=A.getStat("MOCKABILITY").toUpperCase().trim();
					str.append("<OPTION VALUE=\"\""+(id.equals("")?" SELECTED":"")+">"+L("None"));
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A2=e.nextElement();
						if(((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(!CMSecurity.isASysOp(mob)))
							continue;
						final String AID=A2.ID();
						//final String ANAME=A2.name();
						str.append("<OPTION VALUE=\""+AID+"\""+(id.equals(AID.toUpperCase())?" SELECTED":"")+">"+AID);
					}
					str.append(", ");
				}
				if(parms.containsKey("POSTCASTABILITY"))
				{
					List<String> list=new ArrayList<String>();
					if(httpReq.isUrlParameter("POSTCASTABILITY"))
					{
						String id="";
						int num=0;
						for(;httpReq.isUrlParameter("POSTCASTABILITY"+id);id=""+(++num))
							list.add(httpReq.getUrlParameter("POSTCASTABILITY"+id).toUpperCase());
					}
					else
						list=CMParms.parseSemicolons(A.getStat("POSTCASTABILITY").toUpperCase(),true);
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A2=e.nextElement();
						if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)&&(!CMSecurity.isASysOp(mob)))
							continue;
						final String AID=A2.ID();
						final String ANAME=CMStrings.ellipse(A2.ID()+" ("+A2.name()+")",40);
						str.append("<OPTION VALUE=\""+AID+"\""+(list.contains(AID.toUpperCase())?" SELECTED":"")+">"+ANAME);
					}
					str.append(", ");
				}
				final String[] NORMAL_PARMS= {
					"POSTCASTDAMAGE", "ROOMMASK", "PLAYMASK", "YIELDFORMULA",
					"MSGSTART", "MSGFOUND", "MSGNOTFOUND", "MSGCOMPLETE",
					"MINDUR", "BASEDUR", "FINDTICK"
				};
				for(final String normalParm : NORMAL_PARMS)
				{
					if(parms.containsKey(normalParm))
					{
						String old=httpReq.getUrlParameter(normalParm);
						if(old==null)
							old=A.getStat(normalParm);
						str.append(old+", ");
					}
				}

				/*********************************************************************************/
				/*********************************************************************************/
				// here begins the old display data parms

				if(parms.containsKey("HELP"))
				{
					String s=CMLib.help().getHelpText(A.ID(),null,false,parms.containsKey("PLAIN"));
					if(s==null)
						s=CMLib.help().getHelpText(A.Name(),null,false,parms.containsKey("PLAIN"));
					int limit=78;
					if(parms.containsKey("LIMIT"))
						limit=CMath.s_int(parms.get("LIMIT"));
					str.append(helpHelp(s,limit));
				}

				if(parms.containsKey("RANGES"))
				{
					final int min=A.minRange();
					final int max=A.maxRange();
					if(min+max==0)
						str.append(L("Touch, or not applicable, "));
					else
					{
						if(min==0)
							str.append("Touch");
						else
							str.append("Range "+min);
						if(max>0)
							str.append(" - Range "+max);
						str.append(", ");
					}
				}
				if(parms.containsKey("QUALITY"))
				{
					switch(A.abstractQuality())
					{
					case Ability.QUALITY_MALICIOUS:
						str.append("Malicious, ");
						break;
					case Ability.QUALITY_BENEFICIAL_OTHERS:
					case Ability.QUALITY_BENEFICIAL_SELF:
						str.append(L("Always Beneficial, "));
						break;
					case Ability.QUALITY_OK_OTHERS:
					case Ability.QUALITY_OK_SELF:
						str.append(L("Sometimes Beneficial, "));
						break;
					case Ability.QUALITY_INDIFFERENT:
						str.append(L("Circumstantial, "));
						break;
					}
				}
				if(parms.containsKey("AUTO"))
				{
					if(A.isAutoInvoked())
						str.append("Automatic, ");
					else
						str.append(L("Requires invocation, "));
				}
				if(parms.containsKey("TARGET"))
				{
					switch(A.abstractQuality())
					{
					case Ability.QUALITY_INDIFFERENT:
						str.append("Item or Room, ");
						break;
					case Ability.QUALITY_MALICIOUS:
						str.append("Others, ");
						break;
					case Ability.QUALITY_BENEFICIAL_OTHERS:
					case Ability.QUALITY_OK_OTHERS:
						str.append(L("Caster or others, "));
						break;
					case Ability.QUALITY_BENEFICIAL_SELF:
					case Ability.QUALITY_OK_SELF:
						str.append("Caster only, ");
						break;
					}
				}

				if(parms.containsKey("ALIGNMENT"))
				{
					String rangeDesc=null;
					for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
					{
						final Faction F=e.nextElement();
						rangeDesc=F.usageFactorRangeDescription(A);
						if(rangeDesc.length()>0)
							str.append(rangeDesc+", ");
					}
				}
				if(parms.containsKey("ALLOWS"))
				{
					Ability A2=null;
					ExpertiseLibrary.ExpertiseDefinition def=null;
					for(final Iterator<String> i=CMLib.ableMapper().getAbilityAllowsList(A.ID());i.hasNext();)
					{
						final String allowStr=i.next();
						def=CMLib.expertises().getDefinition(allowStr);
						if(def!=null)
							str.append(def.name()+", ");
						else
						{
							A2=CMClass.getAbility(allowStr);
							if(A2!=null)
								str.append(A2.Name()+", ");
						}
					}
				}

				if(parms.containsKey("TYPE"))
					str.append(CMLib.flags().getAbilityType(A)).append(", ");

				if(parms.containsKey("DOMAIN"))
				{
					if(parms.containsKey("PLAIN"))
						str.append(CMLib.flags().getAbilityDomain(A)).append(", ");
					else
					{
						final StringBuffer thang=new StringBuffer("");
						if((A.classificationCode()&Ability.ALL_DOMAINS)!=0)
						{
							int domain=A.classificationCode()&Ability.ALL_DOMAINS;
							domain=domain>>5;
							thang.append(Ability.DOMAIN.DESCS.get(domain).toLowerCase().replace('_',' '));
						}
						else
							thang.append(Ability.ACODE.DESCS.get(A.classificationCode()&Ability.ALL_ACODES).toLowerCase());
						if(thang.length()>0)
						{
							thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

							final int x=thang.toString().indexOf('/');
							if(x>0)
								thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
							str.append(thang.toString()+", ");
						}
					}
				}
				if(parms.containsKey("TYPENDOMAIN"))
				{
					final StringBuffer thang=new StringBuffer("");
					thang.append(CMStrings.capitalizeAndLower(Ability.ACODE.DESCS.get(A.classificationCode()&Ability.ALL_ACODES)));
					if((A.classificationCode()&Ability.ALL_DOMAINS)!=0)
					{
						int domain=A.classificationCode()&Ability.ALL_DOMAINS;
						domain=domain>>5;
						thang.append(": "+CMStrings.capitalizeAndLower(Ability.DOMAIN.DESCS.get(domain)).replace('_',' '));
					}

					if(thang.length()>0)
					{
						thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

						int x=thang.toString().indexOf('/');
						while(x>0)
						{
							thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
							x=thang.toString().indexOf('/',x+1);
						}
						str.append(thang.toString()+", ");
					}
				}
				if(parms.containsKey("QLEVEL"))
				{
					final String className=httpReq.getUrlParameter("CLASS");
					int level=0;
					if((className!=null)&&(className.length()>0))
						level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
					else
						level=CMLib.ableMapper().qualifiesByAnything(A.ID())?CMLib.ableMapper().lowestQualifyingLevel(A.ID()):-1;
					str.append(level+", ");
				}
				if(parms.containsKey("QUALIFYQ")&&(httpReq.isUrlParameter("CLASS")))
				{
					final String className=httpReq.getUrlParameter("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						final boolean defaultGain=CMLib.ableMapper().getDefaultGain(className,true,A.ID());
						if(!defaultGain)
							str.append("(Qualify), ");
					}
				}
				if(parms.containsKey("CLASSONLY")&&(httpReq.isUrlParameter("CLASS")))
				{
					final String className=httpReq.getUrlParameter("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						if(CMLib.ableMapper().getQualifyingLevel(className,false,A.ID())>=0)
							str.append("true");
						else
							str.append("false");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
