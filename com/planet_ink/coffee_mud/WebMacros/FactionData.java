package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class FactionData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "FactionData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
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

		String last=httpReq.getUrlParameter("FACTION");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final String newFactionID=httpReq.getUrlParameter("NEWFACTION");
			Faction F=(Faction)httpReq.getRequestObjects().get("FACTION-"+last);
			if((F==null)
			&&(newFactionID!=null)
			&&(newFactionID.length()>0)
			&&(CMLib.factions().getFaction(newFactionID)==null))
			{
				F=(Faction)CMClass.getCommon("DefaultFaction");
				F.initializeFaction(newFactionID);
				last=newFactionID;
				httpReq.addFakeUrlParameter("FACTION",newFactionID);
			}
			if(F==null)
				F=CMLib.factions().getFaction(last);
			if(parms.containsKey("ISNEWFACTION"))
				return ""+(CMLib.factions().getFaction(last)==null);
			if(F!=null)
			{
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
						old=F.name();
					str.append(old+", ");
				}
				if(parms.containsKey("MINRANGE"))
					str.append(F.minimum()+", ");
				if(parms.containsKey("MAXRANGE"))
					str.append(F.maximum()+", ");
				if(parms.containsKey("SHOWINSCORE"))
				{
					String old=httpReq.getUrlParameter("SHOWINSCORE");
					if(old==null)
						old=F.showInScore()?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}
				if(parms.containsKey("SHOWINFACTIONS"))
				{
					String old=httpReq.getUrlParameter("SHOWINFACTIONS");
					if(old==null)
						old=F.showInFactionsCommand()?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}
				if(parms.containsKey("ISPRELOAD"))
				{
					String old=httpReq.getUrlParameter("PRELOAD"+F.factionID());
					if(old==null)
						old=(F.isSavable() && F.isPreLoaded())?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}
				if(parms.containsKey("SHOWINEDITOR"))
				{
					String old=httpReq.getUrlParameter("SHOWINEDITOR");
					if(old==null)
						old=F.showInEditor()?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}
				if(parms.containsKey("SHOWINREPORTS"))
				{
					String old=httpReq.getUrlParameter("SHOWINREPORTS");
					if(old==null)
						old=F.showInSpecialReported()?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}
				if(parms.containsKey("RANGES"))
				{
					String oldName=httpReq.getUrlParameter("RANGENAME0");
					String oldLow=null;
					String oldHigh=null;
					String code=null;
					String align=null;
					if(oldName==null)
					{
						int v=0;
						for(final Enumeration<Faction.FRange> e=F.ranges();e.hasMoreElements();)
						{
							final Faction.FRange FR=e.nextElement();
							httpReq.addFakeUrlParameter("RANGENAME"+v,FR.name());
							httpReq.addFakeUrlParameter("RANGELOW"+v,""+FR.low());
							httpReq.addFakeUrlParameter("RANGEHIGH"+v,""+FR.high());
							httpReq.addFakeUrlParameter("RANGECODE"+v,""+FR.codeName());
							httpReq.addFakeUrlParameter("RANGEFLAG"+v,""+FR.alignEquiv().toString());
							v++;
						}
					}

					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("RANGENAME"+num)!=null)
					{
						oldName=httpReq.getUrlParameter("RANGENAME"+num);
						if(oldName.length()>0)
						{
							++showNum;
							oldLow=httpReq.getUrlParameter("RANGELOW"+num);
							oldHigh=httpReq.getUrlParameter("RANGEHIGH"+num);
							code=httpReq.getUrlParameter("RANGECODE"+num);
							align=httpReq.getUrlParameter("RANGEFLAG"+num);
							if(CMath.s_int(oldHigh)<CMath.s_int(oldLow))
								oldHigh=oldLow;
							str.append("<TR><TD>");
							str.append("<INPUT TYPE=TEXT NAME=RANGENAME"+showNum+" SIZE=20 VALUE=\""+oldName+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=RANGELOW"+showNum+" SIZE=8 VALUE=\""+oldLow+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=RANGEHIGH"+showNum+" SIZE=8 VALUE=\""+oldHigh+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=RANGECODE"+showNum+" SIZE=10 VALUE=\""+code+"\">");
							str.append("</TD><TD>");
							str.append("<SELECT NAME=RANGEFLAG"+showNum+">");
							for(final Faction.Align i : Faction.Align.values())
							{
								str.append("<OPTION VALUE=\""+i.toString()+"\"");
								if(i.toString().equalsIgnoreCase(align))
									str.append(" SELECTED");
								str.append(">"+CMStrings.capitalizeAndLower(i.toString()));

							}
							str.append("</SELECT>");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<INPUT TYPE=TEXT NAME=RANGENAME"+showNum+" SIZE=20 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=RANGELOW"+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=RANGEHIGH"+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=RANGECODE"+showNum+" SIZE=10 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<SELECT NAME=RANGEFLAG"+showNum+">");
					for(final Faction.Align i : Faction.Align.values())
						str.append("<OPTION VALUE=\""+i.toString()+"\">"+CMStrings.capitalizeAndLower(i.toString()));
					str.append("</SELECT>");
					str.append("</TD></TR>");
				}

				if(parms.containsKey("PLAYERCHOICETEXT"))
				{
					String oldName=httpReq.getUrlParameter("PLAYERCHOICETEXT");
					if(oldName==null)
						oldName=F.choiceIntro();
					str.append(oldName+", ");
				}

				if(parms.containsKey("AUTOVALUES")
				|| parms.containsKey("DEFAULTVALUES")
				|| parms.containsKey("PLAYERCHOICES"))
				{
					String prefix="";
					Enumeration<String> Fset=null;
					if(parms.containsKey("AUTOVALUES"))
					{
						prefix="AUTOVALUE";
						Fset=F.autoDefaults();
					}
					else
					if(parms.containsKey("DEFAULTVALUES"))
					{
						prefix="DEFAULTVALUE";
						Fset=F.defaults();
					}
					else
					if(parms.containsKey("PLAYERCHOICES"))
					{
						prefix="PLAYERCHOICE";
						Fset=F.choices();
					}

					String value=httpReq.getUrlParameter(prefix+"0");
					String mask="";
					int v=0;
					if((value==null)&&(Fset!=null))
					{
						for(;Fset.hasMoreElements();)
						{
							String def=Fset.nextElement();
							int lastSp=0;
							int spDex=def.indexOf(' ',lastSp+1);
							int finalValue=-1;
							while(spDex>0)
							{
								if(CMath.isInteger(def.substring(lastSp,spDex).trim()))
								{
									finalValue=CMath.s_int(def.substring(lastSp,spDex).trim());
									def=def.substring(0,lastSp)+def.substring(spDex);
									break;
								}
								lastSp=spDex;
								spDex=def.indexOf(' ',lastSp+1);
							}
							if((finalValue<0)&&CMath.isInteger(def.substring(lastSp).trim()))
							{
								finalValue=CMath.s_int(def.substring(lastSp).trim());
								def=def.substring(0,lastSp);
							}
							httpReq.addFakeUrlParameter(prefix+v,""+finalValue);
							httpReq.addFakeUrlParameter(prefix+"MASK"+v,def);
							v++;
						}

					}
					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter(prefix+num)!=null)
					{
						value=httpReq.getUrlParameter(prefix+num);
						if(value.length()>0)
						{
							++showNum;
							mask=httpReq.getUrlParameter(prefix+"MASK"+num);
							str.append("<TR><TD>");
							str.append("<INPUT TYPE=TEXT NAME="+prefix+showNum+" SIZE=8 VALUE=\""+CMath.s_int(value)+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME="+prefix+"MASK"+showNum+" SIZE=60 MAXLENGTH=255 VALUE=\""+htmlOutgoingFilter(mask)+"\">");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<INPUT TYPE=TEXT NAME="+prefix+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME="+prefix+"MASK"+showNum+" SIZE=60 MAXLENGTH=255 VALUE=\"\">");
					str.append("</TD></TR>");
				}

				if(parms.containsKey("ADJUSTMENTCHANGES"))
				{
					String trigger=httpReq.getUrlParameter("CHANGESTRIGGER0");
					if(trigger==null)
					{
						int v=0;
						for(final Enumeration<String> e=F.changeEventKeys();e.hasMoreElements();)
						{
							final String def=e.nextElement();
							final Faction.FactionChangeEvent[] Es=F.getChangeEvents(def);
							if(Es!=null)
							{
								for (final FactionChangeEvent E : Es)
								{
									httpReq.addFakeUrlParameter("CHANGESTRIGGER"+v,def);
									httpReq.addFakeUrlParameter("CHANGESDIR"+v,""+E.direction());
									httpReq.addFakeUrlParameter("CHANGESFACTOR"+v,CMath.toPct(E.factor()));
									httpReq.addFakeUrlParameter("CHANGESTPARM"+v,E.triggerParameters());
									String id="";
									final Vector<String> flags=CMParms.parse(E.flagCache());
									for(int f=0;f<flags.size();f++)
									{
										httpReq.addFakeUrlParameter("CHANGESFLAGS"+v+"_"+id,""+(flags.elementAt(f)));
										id=""+(f+1);
									}
									httpReq.addFakeUrlParameter("CHANGESMASK"+v,E.targetZapper());
									v++;
								}
							}
						}
					}

					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("CHANGESTRIGGER"+num)!=null)
					{
						trigger=httpReq.getUrlParameter("CHANGESTRIGGER"+num);
						if(trigger.length()>0)
						{
							++showNum;
							String val=trigger;
							str.append("<TR><TD>");
							str.append("<SELECT NAME=CHANGESTRIGGER"+showNum+" ONCHANGE=\"DelItem(this);\">");
							str.append("<OPTION VALUE=\"\">Delete");
							str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+CMStrings.capitalizeAndLower(val));
							str.append("</SELECT>");
							str.append("<BR>");
							val=""+httpReq.getUrlParameter("CHANGESTPARM"+num);
							str.append("<INPUT TYPE=TEXT NAME=CHANGESTPARM"+showNum+" SIZE=10 MAXLENGTH=255 VALUE=\""+htmlOutgoingFilter(val)+"\">");
							str.append("</TD><TD>");
							val=""+CMath.s_int(httpReq.getUrlParameter("CHANGESDIR"+num));
							str.append("<SELECT NAME=CHANGESDIR"+showNum+">");
							for(int f=0;f<Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS.length;f++)
							{
								str.append("<OPTION VALUE=\""+f+"\"");
								if(f==CMath.s_int(val))
									str.append(" SELECTED");
								str.append(">"+CMStrings.capitalizeAndLower(Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[f]));
							}
							str.append("</SELECT>");
							str.append("</TD><TD>");
							val=CMath.toPct(httpReq.getUrlParameter("CHANGESFACTOR"+num));
							str.append("<INPUT TYPE=TEXT NAME=CHANGESFACTOR"+showNum+" SIZE=4 VALUE=\""+val+"\">");
							str.append("</TD><TD>");
							final Vector<String> flags=new Vector<String>();
							String id="";
							int x=0;
							for(;httpReq.isUrlParameter("CHANGESFLAGS"+num+"_"+id);id=""+(++x))
								flags.addElement(httpReq.getUrlParameter("CHANGESFLAGS"+num+"_"+id).toUpperCase());
							str.append("<SELECT NAME=CHANGESFLAGS"+showNum+"_ MULTIPLE>");
							for (final String element : Faction.FactionChangeEvent.FLAG_DESCS)
							{
								str.append("<OPTION VALUE=\""+element+"\"");
								if(flags.contains(element))
									str.append(" SELECTED");
								str.append(">"+CMStrings.capitalizeAndLower(element));
							}
							str.append("</SELECT>");
							str.append("</TD><TD>");
							val=""+httpReq.getUrlParameter("CHANGESMASK"+num);
							str.append("<INPUT TYPE=TEXT NAME=CHANGESMASK"+showNum+" SIZE=20 MAXLENGTH=255 VALUE=\""+htmlOutgoingFilter(val)+"\">");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<SELECT NAME=CHANGESTRIGGER"+showNum+" ONCHANGE=\"AddItem(this);\">");
					str.append("<OPTION VALUE=\"\">Select a trigger");
					for (final String element : Faction.FactionChangeEvent.MISC_TRIGGERS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.ACODE_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.DOMAIN_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.FLAG_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A=e.nextElement();
						str.append("<OPTION VALUE=\""+A.ID()+"\">"+A.ID());
					}
					str.append("</SELECT>");
					str.append("<BR>");
					str.append("<INPUT TYPE=TEXT NAME=CHANGESTPARM"+showNum+" SIZE=10 MAXLENGTH=255 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<SELECT NAME=CHANGESDIR"+showNum+">");
					for(int f=0;f<Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS.length;f++)
						str.append("<OPTION VALUE=\""+f+"\">"+CMStrings.capitalizeAndLower(Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[f]));
					str.append("</SELECT>");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=CHANGESFACTOR"+showNum+" SIZE=4 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<SELECT NAME=CHANGESFLAGS"+showNum+"_ MULTIPLE>");
					for (final String element : Faction.FactionChangeEvent.FLAG_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					str.append("</SELECT>");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=CHANGESMASK"+showNum+" SIZE=20 MAXLENGTH=255 VALUE=\"\">");
					str.append("</TD></TR>");
				}
				if(parms.containsKey("ADJUSTMENTFACTORS"))
				{
					String mask=httpReq.getUrlParameter("ADJFACTOR0");
					String gain="";
					String loss="";
					if((mask==null)&&(F.factors()!=null))
					{
						int v=0;
						for(final Enumeration<Faction.FZapFactor> e=F.factors();e.hasMoreElements();)
						{
							final Faction.FZapFactor factor=e.nextElement();
							httpReq.addFakeUrlParameter("ADJFACTOR"+v,factor.MOBMask());
							httpReq.addFakeUrlParameter("ADJFACTORGAIN"+v,CMath.toPct(factor.gainFactor()));
							httpReq.addFakeUrlParameter("ADJFACTORLOSS"+v,CMath.toPct(factor.gainFactor()));
							v++;
						}
					}

					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("ADJFACTOR"+num)!=null)
					{
						mask=httpReq.getUrlParameter("ADJFACTOR"+num);
						if(mask.length()>0)
						{
							++showNum;
							gain=CMath.toPct(httpReq.getUrlParameter("ADJFACTORGAIN"+num));
							loss=CMath.toPct(httpReq.getUrlParameter("ADJFACTORLOSS"+num));
							str.append("<TR><TD>");
							str.append("<INPUT TYPE=TEXT NAME=ADJFACTOR"+showNum+" SIZE=40 MAXLENGTH=255 VALUE=\""+htmlOutgoingFilter(mask)+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=ADJFACTORGAIN"+showNum+" SIZE=8 VALUE=\""+gain+"\">");
							str.append("</TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=ADJFACTORLOSS"+showNum+" SIZE=8 VALUE=\""+loss+"\">");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<INPUT TYPE=TEXT NAME=ADJFACTOR"+showNum+" SIZE=40 MAXLENGTH=255 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=ADJFACTORGAIN"+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=ADJFACTORLOSS"+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD></TR>");
				}
				if(parms.containsKey("FACTIONRELATIONS"))
				{
					String faction=httpReq.getUrlParameter("RELATIONS0");
					int x=0;
					if(faction==null)
					{
						for(final Enumeration<String> e=F.relationFactions();e.hasMoreElements();x++)
						{
							final String def=e.nextElement();
							final double pctD=F.getRelation(def);
							httpReq.addFakeUrlParameter("RELATIONS"+x,""+def);
							httpReq.addFakeUrlParameter("RELATIONSAMT"+x,CMath.toPct(pctD));
						}

					}
					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("RELATIONS"+num)!=null)
					{
						faction=httpReq.getUrlParameter("RELATIONS"+num);
						if(faction.length()>0)
						{
							++showNum;
							final String pct=CMath.toPct(httpReq.getUrlParameter("RELATIONSAMT"+num));
							str.append("<TR><TD>");
							str.append("<SELECT NAME=RELATIONS"+showNum+" ONCHANGE=\"DelItem(this);\">");
							str.append("<OPTION VALUE=\"\">Delete");
							final Faction F2=CMLib.factions().getFaction(faction);
							if(F2!=null)
								str.append("<OPTION VALUE=\""+F2.factionID()+"\" SELECTED>"+F2.name());
							else
								str.append("<OPTION VALUE=\""+faction+"\" SELECTED>"+faction);
							str.append("</SELECT></TD><TD>");
							str.append("<INPUT TYPE=TEXT NAME=RELATIONSAMT"+showNum+" SIZE=8 VALUE=\""+pct+"\">");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<SELECT NAME=RELATIONS"+showNum+" ONCHANGE=\"AddItem(this);\">");
					str.append("<OPTION VALUE=\"\">Select a faction");
					for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
					{
						final Faction F2=e.nextElement();
						str.append("<OPTION VALUE=\""+F2.factionID()+"\">"+F2.name());
					}
					str.append("</SELECT></TD><TD>");
					str.append("<INPUT TYPE=TEXT NAME=RELATIONSAMT"+showNum+" SIZE=8 VALUE=\"\">");
					str.append("</TD></TR>");
				}
				if(parms.containsKey("ABILITYALLOWANCES"))
				{
					String abilityID="";
					abilityID=httpReq.getUrlParameter("ABILITYUSE0");
					if((abilityID==null)&&(F.abilityUsages()!=null))
					{
						int v=0;
						for(final Enumeration<Faction.FAbilityUsage> e=F.abilityUsages();e.hasMoreElements();v++)
						{
							final Faction.FAbilityUsage E=e.nextElement();
							if(!E.possibleAbilityID()||CMClass.getAbility(E.abilityFlags())==null)
							{
								final Vector<String> V=CMParms.parse(E.abilityFlags());
								String id="";
								int x=-1;
								for(final Enumeration<String> e2=V.elements();e2.hasMoreElements();id="_"+(++x))
									httpReq.addFakeUrlParameter("ABILITYUSE"+v+id,e2.nextElement());
							}
							else
								httpReq.addFakeUrlParameter("ABILITYUSE"+v,CMClass.getAbility(E.abilityFlags()).ID());
							httpReq.addFakeUrlParameter("ABILITYMIN"+v,""+E.low());
							httpReq.addFakeUrlParameter("ABILITYMAX"+v,""+E.high());
						}
					}

					final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
					final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("ABILITYUSE"+num)!=null)
					{
						abilityID=httpReq.getUrlParameter("ABILITYUSE"+num);
						if(abilityID.length()>0)
						{
							showNum++;
							String val=abilityID;
							str.append("<TR><TD>");
							final int usedType=CMLib.factions().getAbilityFlagType(val);
							str.append("<SELECT NAME=ABILITYUSE"+showNum+" ONCHANGE=\"DelItem(this);\">");
							str.append("<OPTION VALUE=\"\">Delete This Row");
							str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+CMStrings.capitalizeAndLower(val));
							str.append("</SELECT>");
							if(usedType>0)
							{
								int x=-1;
								int sx=-1;
								final HashSet<String> doneSet=new HashSet<String>();
								addDoneAbilityUsage(doneSet,val);
								while(httpReq.isUrlParameter("ABILITYUSE"+num+"_"+(++x)))
								{
									val=httpReq.getUrlParameter("ABILITYUSE"+num+"_"+x);
									if(val.length()>0)
									{
										++sx;
										addDoneAbilityUsage(doneSet,val);
										str.append("<BR>"+sfont+"AND&nbsp;"+efont);
										str.append("<SELECT NAME=ABILITYUSE"+showNum+"_"+sx+" ONCHANGE=\"DelItem(this);\">");
										str.append("<OPTION VALUE=\"\">Delete");
										str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+CMStrings.capitalizeAndLower(val));
										str.append("</SELECT>");
									}
								}
								++sx;
								str.append("<BR>"+sfont+"AND&nbsp;"+efont);
								str.append("<SELECT NAME=ABILITYUSE"+showNum+"_"+sx+" ONCHANGE=\"AddItem(this);\">");
								str.append("<OPTION VALUE=\"\" SELECTED>Select an option");
								for(int i=0;i<Ability.ACODE_DESCS.length;i++)
									if(!doneSet.contains(Ability.ACODE_DESCS[i]))
									str.append("<OPTION VALUE=\""+Ability.ACODE_DESCS[i]+"\">"+CMStrings.capitalizeAndLower(Ability.ACODE_DESCS[i]));
								for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
									if(!doneSet.contains(Ability.DOMAIN_DESCS[i]))
									str.append("<OPTION VALUE=\""+Ability.DOMAIN_DESCS[i]+"\">"+CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[i]));
								for(int i=0;i<Ability.FLAG_DESCS.length;i++)
									if(!doneSet.contains(Ability.FLAG_DESCS[i]))
									str.append("<OPTION VALUE=\""+Ability.FLAG_DESCS[i]+"\">"+CMStrings.capitalizeAndLower(Ability.FLAG_DESCS[i]));
								for(int i=0;i<Ability.FLAG_DESCS.length;i++)
									if(!doneSet.contains("!"+Ability.FLAG_DESCS[i]))
									str.append("<OPTION VALUE=\"!"+Ability.FLAG_DESCS[i]+"\">Not "+CMStrings.capitalizeAndLower(Ability.FLAG_DESCS[i]));
								str.append("</SELECT>");
							}
							str.append("</TD><TD VALIGN=TOP>");
							val=""+CMath.s_int(httpReq.getUrlParameter("ABILITYMIN"+num));
							str.append("<INPUT TYPE=TEXT NAME=ABILITYMIN"+showNum+" SIZE=5 VALUE=\""+val+"\">");
							str.append("</TD><TD VALIGN=TOP>");
							val=""+CMath.s_int(httpReq.getUrlParameter("ABILITYMAX"+num));
							str.append("<INPUT TYPE=TEXT NAME=ABILITYMAX"+showNum+" SIZE=5 VALUE=\""+val+"\">");
							str.append("</TD></TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<SELECT NAME=ABILITYUSE"+showNum+" ONCHANGE=\"AddItem(this);\">");
					str.append("<OPTION VALUE=\"\" SELECTED>Select an option");
					for (final String element : Ability.ACODE_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.DOMAIN_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.FLAG_DESCS)
						str.append("<OPTION VALUE=\""+element+"\">"+CMStrings.capitalizeAndLower(element));
					for (final String element : Ability.FLAG_DESCS)
						str.append("<OPTION VALUE=\"!"+element+"\">Not "+CMStrings.capitalizeAndLower(element));
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A=e.nextElement();
						str.append("<OPTION VALUE=\""+A.ID()+"\">"+A.ID());
					}
					str.append("</SELECT>");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=ABILITYMIN"+showNum+" SIZE=5 VALUE=\"\">");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=ABILITYMAX"+showNum+" SIZE=5 VALUE=\"\">");
					str.append("</TD></TR>");
				}
				if(parms.containsKey("AFFECTSBEHAVIORS"))
				{
					String abilityID="";
					abilityID=httpReq.getUrlParameter("AFFBEHAV0");
					if((abilityID==null)&&(F.affectsBehavs()!=null))
					{
						int v=0;
						for(final Enumeration<String> e=F.affectsBehavs();e.hasMoreElements();v++)
						{
							final String ID=e.nextElement();
							httpReq.addFakeUrlParameter("AFFBEHAV"+v,ID);
							final String[] affBehavParms=F.getAffectBehav(ID);
							httpReq.addFakeUrlParameter("AFFBEHAVPARM"+v,affBehavParms[0]);
							httpReq.addFakeUrlParameter("AFFBEHAVMASK"+v,affBehavParms[1]);
						}
					}

					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("AFFBEHAV"+num)!=null)
					{
						abilityID=httpReq.getUrlParameter("AFFBEHAV"+num);
						if(abilityID.length()>0)
						{
							showNum++;
							String val=abilityID;
							str.append("<TR><TD>");
							str.append("<SELECT NAME=AFFBEHAV"+showNum+" ONCHANGE=\"DelItem(this);\">");
							str.append("<OPTION VALUE=\"\">Delete This Row");
							final String name=getAbleBehavCmdName(val,false);
							if(name!=null)
							{
								str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+name);
								str.append("</SELECT>");
								str.append("</TD><TD VALIGN=TOP>");
								val=""+httpReq.getUrlParameter("AFFBEHAVPARM"+num);
								str.append("<INPUT TYPE=TEXT NAME=AFFBEHAVPARM"+showNum+" SIZE=20 VALUE=\""+htmlOutgoingFilter(val)+"\">");
								str.append("</TD><TD VALIGN=TOP>");
								val=""+httpReq.getUrlParameter("AFFBEHAVMASK"+num);
								str.append("<INPUT TYPE=TEXT NAME=AFFBEHAVMASK"+showNum+" SIZE=20 VALUE=\""+htmlOutgoingFilter(val)+"\">");
								str.append("</TD></TR>");
							}
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<SELECT NAME=AFFBEHAV"+showNum+" ONCHANGE=\"AddItem(this);\">");
					str.append("<OPTION VALUE=\"\" SELECTED>Select an ability/behavior");
					for(final Enumeration<Behavior> e=CMClass.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						str.append("<OPTION VALUE=\""+B.ID()+"\">"+B.name());
					}
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A=e.nextElement();
						str.append("<OPTION VALUE=\""+A.ID()+"\">"+A.name());
					}
					str.append("</SELECT>");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=AFFBEHAVPARM"+showNum+" SIZE=20 VALUE=\"\">");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=AFFBEHAVMASK"+showNum+" SIZE=20 VALUE=\"\">");
					str.append("</TD></TR>");
				}

				if(parms.containsKey("USELIGHTREACTIONS"))
				{
					String old=httpReq.getUrlParameter("USELIGHTREACTIONS");
					if(old==null)
						old=F.useLightReactions()?"on":"";
					str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
				}

				if(parms.containsKey("REACTIONS"))
				{
					String rangeCode="";
					rangeCode=httpReq.getUrlParameter("REACTIONRANGE0");
					if((rangeCode==null)&&(F.reactions().hasMoreElements()))
					{
						int v=0;
						for(final Enumeration<Faction.FReactionItem> e=F.reactions();e.hasMoreElements();v++)
						{
							final Faction.FReactionItem item=e.nextElement();
							httpReq.addFakeUrlParameter("REACTIONRANGE"+v,item.rangeCodeName());
							httpReq.addFakeUrlParameter("REACTIONABC"+v,item.reactionObjectID());
							httpReq.addFakeUrlParameter("REACTIONPARM"+v,item.parameters());
							httpReq.addFakeUrlParameter("REACTIONMASK"+v,item.presentMOBMask());
						}
					}

					final PairVector<String,String> rangeCodes = getRangeCodesNames(F,httpReq);

					int num=0;
					int showNum=-1;
					while(httpReq.getUrlParameter("REACTIONRANGE"+num)!=null)
					{
						rangeCode=httpReq.getUrlParameter("REACTIONRANGE"+num);
						if(rangeCode.length()>0)
						{
							showNum++;
							String val=rangeCode;
							str.append("<TR><TD>");
							str.append("<SELECT NAME=REACTIONRANGE"+showNum+" ONCHANGE=\"DelItem(this);\">");
							str.append("<OPTION VALUE=\"\">Delete This Row");
							final int x=rangeCodes.indexOfFirst(val);
							String name="Unknown!";
							if(x>=0)
								name=rangeCodes.getSecond(x);
							str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+name);
							str.append("</SELECT>");
							str.append("</TD><TD VALIGN=TOP>");
							val=""+httpReq.getUrlParameter("REACTIONMASK"+num);
							str.append("<INPUT TYPE=TEXT NAME=REACTIONMASK"+showNum+" SIZE=20 VALUE=\""+htmlOutgoingFilter(val)+"\">");
							str.append("</TD><TD>");
							str.append("<SELECT NAME=REACTIONABC"+showNum+">");
							val=""+httpReq.getUrlParameter("REACTIONABC"+num);
							name=getAbleBehavCmdName(val,true);
							if(name==null)
								name="";
							str.append("<OPTION VALUE=\""+val+"\" SELECTED>"+name);
							for(final Enumeration<Behavior> e=CMClass.behaviors();e.hasMoreElements();)
							{
								final Behavior B=e.nextElement();
								str.append("<OPTION VALUE=\""+B.ID()+"\">"+CMStrings.limit(B.name(),20));
							}
							for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
							{
								final Ability A=e.nextElement();
								str.append("<OPTION VALUE=\""+A.ID()+"\">"+CMStrings.limit(A.name(),20));
							}
							for(final Enumeration<Command> e=CMClass.commands();e.hasMoreElements();)
							{
								final Command C=e.nextElement();
								if((C.getAccessWords()!=null)&&(C.getAccessWords().length>0))
									str.append("<OPTION VALUE=\""+C.ID()+"\">"+CMStrings.capitalizeAndLower(C.getAccessWords()[0]));
								else
									str.append("<OPTION VALUE=\""+C.ID()+"\">"+C.ID());
							}
							str.append("</SELECT>");
							str.append("</TD><TD VALIGN=TOP>");
							val=""+httpReq.getUrlParameter("REACTIONPARM"+num);
							str.append("<INPUT TYPE=TEXT NAME=REACTIONPARM"+showNum+" SIZE=20 VALUE=\""+htmlOutgoingFilter(val)+"\">");
							str.append("</TD>");
							str.append("</TR>");
						}
						num++;
					}
					++showNum;
					str.append("<TR><TD>");
					str.append("<SELECT NAME=REACTIONRANGE"+showNum+" ONCHANGE=\"AddItem(this);\">");
					str.append("<OPTION VALUE=\"\" SELECTED>Select an range");
					for(int i=0;i<rangeCodes.size();i++)
						str.append("<OPTION VALUE=\""+(rangeCodes.getFirst(i)+"\">"+rangeCodes.getSecond(i)));
					str.append("</SELECT>");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=REACTIONMASK"+showNum+" SIZE=20 VALUE=\"\">");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<SELECT NAME=REACTIONABC"+showNum+">");
					str.append("<OPTION VALUE=\"\" SELECTED>Select an able/behav/cmd");
					for(final Enumeration<Behavior> e=CMClass.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						str.append("<OPTION VALUE=\""+B.ID()+"\">"+CMStrings.limit(B.name(),20));
					}
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A=e.nextElement();
						str.append("<OPTION VALUE=\""+A.ID()+"\">"+CMStrings.limit(A.name(),20));
					}
					for(final Enumeration<Command> e=CMClass.commands();e.hasMoreElements();)
					{
						final Command C=e.nextElement();
						if((C.getAccessWords()!=null)&&(C.getAccessWords().length>0))
							str.append("<OPTION VALUE=\""+C.ID()+"\">"+CMStrings.capitalizeAndLower(C.getAccessWords()[0]));
						else
							str.append("<OPTION VALUE=\""+C.ID()+"\">"+C.ID());
					}
					str.append("</SELECT>");
					str.append("</TD><TD VALIGN=TOP>");
					str.append("<INPUT TYPE=TEXT NAME=REACTIONPARM"+showNum+" SIZE=20 VALUE=\"\">");
					str.append("</TD></TR>");
				}

				if(parms.containsKey("RATEMODIFIER"))
				{
					String old=httpReq.getUrlParameter("RATEMODIFIER");
					if(old==null)
						old=CMath.toPct(F.rateModifier());
					else
						old=CMath.toPct(old);
					str.append(old+", ");
				}
				if(parms.containsKey("AFFECTONEXP"))
				{
					String old=httpReq.getUrlParameter("AFFECTONEXP");
					if(old==null)
						old=F.experienceFlag();
					for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
					{
						str.append("<OPTION VALUE=\""+Faction.EXPAFFECT_NAMES[i]+"\" ");
						if(Faction.EXPAFFECT_NAMES[i].equalsIgnoreCase(old))
							str.append("SELECTED");
						str.append(">"+Faction.EXPAFFECT_DESCS[i]);
					}
					str.append(", ");
				}

				httpReq.getRequestObjects().put("FACTION-"+last,F);
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}

	public void addDoneAbilityUsage(HashSet<String> done, String val)
	{
		switch(CMLib.factions().getAbilityFlagType(val))
		{
		case 1:
			for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			{
				if(!done.contains(Ability.ACODE_DESCS[i].toUpperCase()))
					done.add(Ability.ACODE_DESCS[i].toUpperCase());
			}
			break;
		case 2:
			for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
			{
				if(!done.contains(Ability.DOMAIN_DESCS[i].toUpperCase()))
					done.add(Ability.DOMAIN_DESCS[i].toUpperCase());
			}
			break;
		case 3:
			done.add(val.toUpperCase());
			break;
		}
	}

	public String getAbleBehavCmdName(String val, boolean includeCmd)
	{
		final Behavior B=CMClass.getBehavior(val);
		if(B!=null)
			return CMStrings.limit(B.name(),20);
		final Ability A=CMClass.getAbility(val);
		if(A!=null)
			return CMStrings.limit(A.name(),20);
		if(!includeCmd)
			return null;
		final Command C=CMClass.getCommand(val);
		if(C==null)
			return null;
		if((C.getAccessWords()!=null)&&(C.getAccessWords().length>0))
			return CMStrings.capitalizeAndLower(C.getAccessWords()[0]);
		return C.ID();
	}

	public PairVector<String,String> getRangeCodesNames(Faction F, HTTPRequest httpReq)
	{
		String oldName=httpReq.getUrlParameter("RANGENAME0");
		String code=null;
		final PairVector<String,String> codes=new PairVector<String,String>();
		int num=0;
		if(oldName==null)
		{
			for(final Enumeration<Faction.FRange> e=F.ranges();e.hasMoreElements();)
			{
				final Faction.FRange FR=e.nextElement();
				codes.addElement(FR.codeName(),FR.name());
			}
		}
		else
		{
			while(httpReq.getUrlParameter("RANGENAME"+num)!=null)
			{
				oldName=httpReq.getUrlParameter("RANGENAME"+num);
				code=httpReq.getUrlParameter("RANGECODE"+num);
				codes.addElement(code,oldName);
				num++;
			}
		}
		return codes;
	}

}
