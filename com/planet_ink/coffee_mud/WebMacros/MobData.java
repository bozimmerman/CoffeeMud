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
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.grinder.GrinderMobs.MOBDataField;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class MobData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "MobData";
	}

	public static int getShopCardinality(ShopKeeper SK, Environmental O)
	{
		int x=0;
		for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();x++)
		{
			if(O==i.next())
				return x;
		}
		return -1;
	}

	public static String senses(Physical P,
								boolean firstTime,
								HTTPRequest httpReq,
								java.util.Map<String,String> parms)
	{
		final StringBuffer str=new StringBuffer("");
		for(int d=0;d<PhyStats.CAN_SEE_CODES.length;d++)
		{
			if(parms.containsKey(PhyStats.CAN_SEE_CODES[d]))
			{
				String parm=httpReq.getUrlParameter(PhyStats.CAN_SEE_CODES[d]);
				if(firstTime)
					parm=(((P.basePhyStats().sensesMask()&(1<<d))>0)?"on":"");
				if((parm!=null)&&(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}

	@SuppressWarnings("unchecked")
	public static Enumeration<Race> sortedRaces(HTTPRequest httpReq)
	{
		if(httpReq.getRequestObjects() != null)
		{
			if(httpReq.getRequestObjects().containsKey("SYSTEM_SORTED_RACES"))
				return ((Vector<Race>)httpReq.getRequestObjects().get("SYSTEM_SORTED_RACES")).elements();
		}
		final TreeMap<String,Race> map=new TreeMap<String,Race>();
		for(Enumeration<Race> r = CMClass.races();r.hasMoreElements();)
		{
			final Race R=r.nextElement();
			map.put(R.name(), R);
		}
		final Vector<Race> V=new Vector<Race>(map.size());
		for(final String raceName : map.keySet())
		{
			V.add(map.get(raceName));
		}
		if(httpReq.getRequestObjects() != null)
		{
			httpReq.getRequestObjects().put("SYSTEM_SORTED_RACES",V);
		}
		return V.elements();
	}
	
	public static StringBuffer abilities(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("ABILITIES"))
		{
			final boolean player=E.playerStats()!=null;
			final ArrayList<String> theclasses=new ArrayList<String>();
			final ArrayList<String> theprofs=new ArrayList<String>();
			final ArrayList<String> thetext=new ArrayList<String>();
			if(httpReq.isUrlParameter("ABLES1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("ABLES"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
					{
						theclasses.add(behav);
						if(player)
						{
							String prof=httpReq.getUrlParameter("ABPOF"+num);
							if(prof==null)
								prof="0";
							String text=httpReq.getUrlParameter("ABTXT"+num);
							if(text==null)
								text="";
							theprofs.add(prof);
							thetext.add(text);
						}
					}
					num++;
					behav=httpReq.getUrlParameter("ABLES"+num);
				}
			}
			else
			for(int a=0;a<E.numAbilities();a++)
			{
				final Ability Able=E.fetchAbility(a);
				if((Able!=null)&&(Able.isSavable()))
				{
					theclasses.add(CMClass.classID(Able));
					if(player)
					{
						theprofs.add(Able.proficiency()+"");
						thetext.add(Able.text());
					}
				}
			}
			str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.get(i);
				str.append("<TR><TD WIDTH=35%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=ABLES"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD>");
				if(player)
				{
					str.append("<TD WIDTH=10%>");
					str.append("<INPUT TYPE=TEXT NAME=ABPOF"+(i+1)+" VALUE=\""+theprofs.get(i)+"\" SIZE=3 MAXLENGTH=3><FONT COLOR=WHITE><B>%</B></FONT>");
					str.append("</TD>");
					str.append("<TD WIDTH=50%>");
					str.append("<INPUT TYPE=TEXT NAME=ABTXT"+(i+1)+" VALUE=\""+thetext.get(i)+"\" SIZE=40>");
					str.append("</TD>");
				}
				else
					str.append("<TD WIDTH=65% COLSPAN=2><BR></TD>");
				str.append("</TR>");
			}
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=ABLES"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					final String cnam=A.ID();
					str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
			}
			str.append("</SELECT>");
			str.append("</TD>");
			if(player)
			{
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=TEXT NAME=ABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3><FONT COLOR=WHITE><B>%</B></FONT>");
				str.append("</TD>");
				str.append("<TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT NAME=ABTXT"+(theclasses.size()+1)+" VALUE=\"\" SIZE=40>");
				str.append("</TD>");
			}
			else
				str.append("<TD WIDTH=65% COLSPAN=2><BR></TD>");
			str.append("</TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer expertiseList(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("EXPERTISELIST"))
		{
			final ArrayList<String> theclasses=new ArrayList<String>();
			if(httpReq.isUrlParameter("EXPER1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("EXPER"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.add(behav);
					num++;
					behav=httpReq.getUrlParameter("EXPER"+num);
				}
			}
			else
			for(final Enumeration<String> x=E.expertises();x.hasMoreElements();)
			{
				final String ID=x.nextElement();
				final ExpertiseLibrary.ExpertiseDefinition X=CMLib.expertises().getDefinition(ID);
				if(X!=null)
					theclasses.add(ID);
			}
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.get(i);
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=EXPER"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				final ExpertiseLibrary.ExpertiseDefinition X=CMLib.expertises().getDefinition(theclass);
				if(X==null)
					str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				else
					str.append("<OPTION VALUE=\""+X.ID()+"\" SELECTED>"+X.name());
				str.append("</SELECT>,&nbsp; ");
			}
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=EXPER"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Expertise");
			for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				final ExpertiseLibrary.ExpertiseDefinition X=e.nextElement();
				str.append("<OPTION VALUE=\""+X.ID()+"\">"+X.name());
			}
			str.append("</SELECT>");
		}
		return str;
	}

	public static StringBuffer clans(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("CLANS"))
		{
			final ArrayList<Pair<Clan,Integer>> theclasses=new ArrayList<Pair<Clan,Integer>>();
			if(httpReq.isUrlParameter("CLAN1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("CLAN"+num);
				while(behav!=null)
				{
					final int role=CMath.s_int(httpReq.getUrlParameter("CLANROLE"+num));
					if(behav.length()>0)
					{
						final Clan C=CMLib.clans().getClan(behav);
						if(C!=null)
							theclasses.add(new Pair<Clan,Integer>(C,Integer.valueOf(role)));
					}
					num++;
					behav=httpReq.getUrlParameter("CLAN"+num);
				}
			}
			else
			for(final Pair<Clan,Integer> p : E.clans())
				theclasses.add(p);
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final Pair<Clan,Integer> clanPair=theclasses.get(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CLAN"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+clanPair.first.clanID()+"\" SELECTED>"+clanPair.first.getName());
				str.append("</SELECT>");
				str.append("<SELECT NAME=CLANROLE"+(i+1)+">");
				for(int r=0;r<clanPair.first.getRolesList().length;r++)
				{
					str.append("<OPTION VALUE="+r+" ");
					if(r==clanPair.second.intValue())
						str.append("SELECTED");
					str.append(">"+clanPair.first.getRolesList()[r]);
				}
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CLAN"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a clan");
			for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			{
				final Clan C=e.nextElement();
				str.append("<OPTION VALUE=\""+C.clanID()+"\">"+C.getName());
			}
			str.append("</SELECT>");
			str.append("<SELECT NAME=CLANROLE"+(theclasses.size()+1)+">");
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer blessings(Deity E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("BLESSINGS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<Boolean> theclerics=new Vector<Boolean>();
			if(httpReq.isUrlParameter("BLESS1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("BLESS"+num);
				while(behav!=null)
				{
					final boolean clericOnly=(httpReq.isUrlParameter("BLONLY"+num))&&(httpReq.getUrlParameter("BLONLY"+num)).equalsIgnoreCase("on");
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						theclerics.addElement(Boolean.valueOf(clericOnly));
					}
					num++;
					behav=httpReq.getUrlParameter("BLESS"+num);
				}
			}
			else
			for(int a=0;a<E.numBlessings();a++)
			{
				final Ability Able=E.fetchBlessing(a);
				if(Able!=null)
				{
					theclasses.addElement(CMClass.classID(Able));
					theclerics.addElement(Boolean.valueOf(E.fetchBlessingCleric(a)));
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final boolean clericOnly=theclerics.elementAt(i).booleanValue();
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=BLESS"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("<INPUT TYPE=CHECKBOX NAME=BLONLY"+(i+1)+" "+((clericOnly)?"CHECKED":"")+"><FONT COLOR=WHITE SIZE=-2>Clerics only</FONT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=BLESS"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Blessing");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					final String cnam=A.ID();
					str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
			}
			str.append("</SELECT>");
			str.append("<INPUT TYPE=CHECKBOX NAME=BLONLY"+(theclasses.size()+1)+"><FONT COLOR=WHITE SIZE=-2>Clerics only</FONT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer curses(Deity E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("CURSES"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<Boolean> theclerics=new Vector<Boolean>();
			if(httpReq.isUrlParameter("CURSE1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("CURSE"+num);
				while(behav!=null)
				{
					final boolean clericOnly=(httpReq.isUrlParameter("BLONLY"+num))&&(httpReq.getUrlParameter("BLONLY"+num)).equalsIgnoreCase("on");
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						theclerics.addElement(Boolean.valueOf(clericOnly));
					}
					num++;
					behav=httpReq.getUrlParameter("CURSE"+num);
				}
			}
			else
			for(int a=0;a<E.numCurses();a++)
			{
				final Ability Able=E.fetchCurse(a);
				if(Able!=null)
				{
					theclasses.addElement(CMClass.classID(Able));
					theclerics.addElement(Boolean.valueOf(E.fetchCurseCleric(a)));
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final boolean clericOnly=theclerics.elementAt(i).booleanValue();
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CURSE"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("<INPUT TYPE=CHECKBOX NAME=CUONLY"+(i+1)+" "+((clericOnly)?"CHECKED":"")+"><FONT COLOR=WHITE SIZE=-2>Clerics only</FONT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CURSE"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Curse");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					final String cnam=A.ID();
					str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
			}
			str.append("</SELECT>");
			str.append("<INPUT TYPE=CHECKBOX NAME=CUONLY"+(theclasses.size()+1)+"><FONT COLOR=WHITE SIZE=-2>Clerics only</FONT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer factions(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("FACTIONS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<String> theparms=new Vector<String>();
			if(httpReq.isUrlParameter("FACTION1"))
			{
				int num=1;
				String facti=httpReq.getUrlParameter("FACTION"+num);
				String theparm=httpReq.getUrlParameter("FACTDATA"+num);
				if(theparm==null)
					theparm="";
				while((facti!=null)&&(theparm!=null))
				{
					if(facti.length()>0)
					{
						theclasses.addElement(facti);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					facti=httpReq.getUrlParameter("FACTION"+num);
					theparm=httpReq.getUrlParameter("FACTDATA"+num);
				}
			}
			else
			{
				for(Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
				{
					final Faction F=f.nextElement();
					if(F.showInEditor() && (!E.hasFaction(F.factionID())))
					{
						int autoDefault = F.findAutoDefault(E);
						if(autoDefault != Integer.MAX_VALUE)
							E.addFaction(F.factionID(), autoDefault);
					}
				}
				// the auto factions!
				for(final Enumeration<String> e=E.factions();e.hasMoreElements();)
				{
					final Faction f=CMLib.factions().getFaction(e.nextElement());
					if(f!=null)
					{
						theclasses.addElement(f.factionID());
						theparms.addElement(Integer.toString(E.fetchFaction(f.factionID())));
					}
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final Faction F=CMLib.factions().getFaction(theclass);
				if(F==null)
					continue;
				String theparm=theparms.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditFaction(this);\" NAME=FACTION"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+F.name());
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<SELECT NAME=FACTDATA"+(i+1)+">");
				if(theparm.length()==0)
					theparm=""+F.findDefault(E);
				final Faction.FRange FR=CMLib.factions().getRange(F.factionID(),CMath.s_int(theparm));
				if(FR==null)
					str.append("<OPTION VALUE=\""+CMath.s_int(theparm)+"\">"+CMath.s_int(theparm));
				List<Faction.FRange> sortedRanges = new XVector<Faction.FRange>(F.ranges());
				Collections.sort(sortedRanges, new Comparator<Faction.FRange>()
				{
					@Override
					public int compare(FRange o1, FRange o2)
					{
						return Integer.valueOf((o1.low()+o1.high())/2).compareTo(Integer.valueOf((o2.low()+o2.high())/2));
					}
				});
				
				for(final Iterator<Faction.FRange> e=sortedRanges.iterator();e.hasNext();)
				{
					final Faction.FRange FR2=e.next();
					int value=(FR2.high()+FR2.low())/2;
					if(FR2.low()==F.minimum())
						value=FR2.low();
					if(FR2.high()==F.maximum())
						value=FR2.high();
					if(FR2==FR)
						value=CMath.s_int(theparm);
					str.append("<OPTION VALUE=\""+value+"\"");
					if(FR2==FR)
						str.append(" SELECTED");
					str.append(">"+FR2.name()+" ("+FR2.low()+" to "+FR2.high()+")");
				}
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddFaction(this);\" NAME=FACTION"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Faction");

			Object[] sortedB=null;
			final Vector<String> sortMeB=new Vector<String>();
			for(final Enumeration<Faction> fID=CMLib.factions().factions();fID.hasMoreElements();)
			{
				final Faction F=fID.nextElement();
				if((F!=null)&&(!theclasses.contains(F.factionID())))
					sortMeB.addElement(F.factionID());
			}
			sortedB=(new TreeSet<String>(sortMeB)).toArray();
			for (final Object element : sortedB)
			{
				final String cnam=(String)element;
				final Faction F=CMLib.factions().getFaction(cnam);
				if(F!=null)
					str.append("<OPTION VALUE=\""+cnam+"\">"+F.name());
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%><BR>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer classList(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("CLASSLIST"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<String> theparms=new Vector<String>();
			if(httpReq.isUrlParameter("CHARCLASS1"))
			{
				int num=1;
				String facti=httpReq.getUrlParameter("CHARCLASS"+num);
				String theparm=httpReq.getUrlParameter("CHARCLASSLVL"+num);
				while(facti!=null)
				{
					if(theparm==null)
						theparm="0";
					if(facti.length()>0)
					{
						theclasses.addElement(facti);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					facti=httpReq.getUrlParameter("CHARCLASS"+num);
					theparm=httpReq.getUrlParameter("CHARCLASSLVL"+num);
				}
			}
			else
			{
				final CharStats baseStats = E.baseCharStats();
				if(baseStats!=null)
				for(int c=0;c<baseStats.numClasses();c++)
				{
					final CharClass C=baseStats.getMyClass(c);
					if(C!=null)
					{
						final int lvl=baseStats.getClassLevel(C);
						if(lvl>=0)
						{
							theclasses.addElement(C.ID());
							theparms.addElement(Integer.toString(lvl));
						}
					}
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=0 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final CharClass C=CMClass.getCharClass(theclass);
				if(C==null)
					continue;
				final String theparm=theparms.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditFaction(this);\" NAME=CHARCLASS"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+C.name()
								+((i==theclasses.size()-1)?" (Current)":""));
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=3 MAXLENGTH=3 NAME=CHARCLASSLVL"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddFaction(this);\" NAME=CHARCLASS"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Class");

			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				str.append("<OPTION VALUE=\""+C.ID()+"\">"+C.name());
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%><BR>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer powers(Deity E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("POWERS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			if(httpReq.isUrlParameter("POWER1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("POWER"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.addElement(behav);
					num++;
					behav=httpReq.getUrlParameter("POWER"+num);
				}
			}
			else
			for(int a=0;a<E.numPowers();a++)
			{
				final Ability Able=E.fetchPower(a);
				if(Able!=null)
					theclasses.addElement(CMClass.classID(Able));
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=POWER"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=POWER"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Granted Power");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					final String cnam=A.ID();
					str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
			}
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer priceFactors(Economics E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("PRICEFACTORS"))
		{
			final ArrayList<String> theprices=new ArrayList<String>();
			final ArrayList<String> themasks=new ArrayList<String>();
			int num=1;
			if(!httpReq.isUrlParameter("IPRIC"+num))
			{
				final String[] prics=E.itemPricingAdjustments();
				for (final String pric : prics)
				{
					final int x=pric.indexOf(' ');
					if(x<0)
					{
						theprices.add(pric);
						themasks.add("");
					}
					else
					{
						theprices.add(pric.substring(0,x));
						themasks.add(pric.substring(x+1));
					}
				}
			}
			else
			while(httpReq.isUrlParameter("IPRIC"+num))
			{
				final String PRICE=httpReq.getUrlParameter("IPRIC"+num);
				final String MASK=httpReq.getUrlParameter("IPRICM"+num);
				if((PRICE!=null)&&(PRICE.length()>0)&&(CMath.isNumber(PRICE)))
				{
					theprices.add(PRICE);
					if(MASK!=null)
						themasks.add(MASK);
					else
						themasks.add("");
				}
				num++;
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			str.append("<TR><TD WIDTH=20%>Price Factor</TD><TD>Item type Mask</TD></TR>");
			for(int i=0;i<theprices.size();i++)
			{
				final String PRICE=theprices.get(i);
				final String MASK=themasks.get(i);
				str.append("<TR><TD>");
				str.append("<INPUT TYPE=TEXT SIZE=5 NAME=IPRIC"+(i+1)+" VALUE=\""+PRICE+"\">");
				str.append("</TD><TD>");
				str.append("<INPUT TYPE=TEXT SIZE=50 NAME=IPRICM"+(i+1)+" VALUE=\""+MASK+"\">");
				str.append("</TD>");
				str.append("</TR>");
			}
			str.append("<TR><TD>");
			str.append("<INPUT TYPE=TEXT SIZE=5 NAME=IPRIC"+(theprices.size()+1)+">");
			str.append("</TD><TD>");
			str.append("<INPUT TYPE=TEXT SIZE=50 NAME=IPRICM"+(theprices.size()+1)+">");
			str.append("</TD></TR>");
			str.append("</TABLE>");

		}
		return str;
	}

	public static StringBuffer shopkeeper(Room R, ShopKeeper E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final int theme = (R!=null) ? R.getArea().getTheme() : CMProps.getIntVar(CMProps.Int.MUDTHEME);
		final StringBuffer str=new StringBuffer("");
		str.append(priceFactors(E,httpReq,parms,borderSize));
		if(parms.containsKey("SHOPINVENTORY"))
		{
			final ArrayList<Environmental> theclasses=new ArrayList<Environmental>();
			final ArrayList<String> theparms=new ArrayList<String>();
			final ArrayList<String> theprices=new ArrayList<String>();
			if(httpReq.isUrlParameter("SHP1"))
			{
				int num=1;
				String MATCHING=httpReq.getUrlParameter("SHP"+num);
				String theparm=httpReq.getUrlParameter("SDATA"+num);
				String theprice=httpReq.getUrlParameter("SPRIC"+num);
				final XVector<Environmental> inventory=new XVector<Environmental>(E.getShop().getStoreInventory());
				while((MATCHING!=null)&&(theparm!=null))
				{
					if(CMath.isNumber(MATCHING))
					{
						final Environmental O=inventory.elementAt(CMath.s_int(MATCHING)-1);
						if(O!=null)
							theclasses.add(O);
					}
					else
					if(MATCHING.startsWith("CATALOG-"))
					{
						Environmental O=RoomData.getMOBFromCatalog(MATCHING);
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							theclasses.add(O);
					}
					else
					if(MATCHING.indexOf('@')>0)
					{
						Environmental O=null;
						for (final MOB M2 : RoomData.getMOBCache())
						{
							if(MATCHING.equals(""+M2))
							{
								O = M2;
								break;
							}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							theclasses.add(O);
					}
					else
					{
						Environmental O=null;
						for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
						{
							final MOB M2=m.nextElement();
							if(CMClass.classID(M2).equals(MATCHING)&&(!M2.isGeneric()))
							{
								O = (MOB) M2.copyOf();
								break;
							}
						}
						if(O==null)
						{
							for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
							{
								final Ability A2=a.nextElement();
								if(CMClass.classID(A2).equals(MATCHING))
								{
									O = (Ability) A2.copyOf();
									break;
								}
							}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							theclasses.add(O);
					}
					theparms.add(theparm);
					theprices.add(theprice);
					num++;
					MATCHING=httpReq.getUrlParameter("SHP"+num);
					theparm=httpReq.getUrlParameter("SDATA"+num);
					theprice=httpReq.getUrlParameter("SPRIC"+num);
				}
			}
			else
			{
				final ArrayList<Item> itemClasses=new ArrayList<Item>();
				final ArrayList<MOB> mobClasses=new ArrayList<MOB>();
				for(final Iterator<Environmental> i=E.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental O=i.next();
					if(O instanceof Item)
						itemClasses.add((Item)O);
					if(O instanceof MOB)
						mobClasses.add((MOB)O);
					if(O instanceof Physical)
						CMLib.catalog().updateCatalogIntegrity((Physical)O);
					theclasses.add(O);
					theparms.add(""+E.getShop().numberInStock(O));
					theprices.add(""+E.getShop().stockPrice(O));
				}
				RoomData.contributeItems(itemClasses);
				RoomData.contributeMOBs(mobClasses);
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final Environmental O=theclasses.get(i);
				final String theparm=theparms.get(i);
				String theprice=theprices.get(i);
				str.append("<TR><TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=SHP"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				final int shopIndex=getShopCardinality(E,O);
				if(shopIndex>=0)
					str.append("<OPTION SELECTED VALUE=\""+(shopIndex+1)+"\">"+O.Name()+" ("+O.ID()+")");
				else
				if(CMLib.flags().isCataloged(O))
					str.append("<OPTION SELECTED VALUE=\"CATALOG-"+O.Name()+"\">"+O.Name()+" (Cataloged)");
				else
				if(RoomData.getItemCache().contains(O))
					str.append("<OPTION SELECTED VALUE=\""+O+"\">"+O.Name()+RoomData.getObjIDSuffix(O));
				else
				if(RoomData.getMOBCache().contains(O))
					str.append("<OPTION SELECTED VALUE=\""+O+"\">"+O.Name()+RoomData.getObjIDSuffix(O));
				else
					str.append("<OPTION SELECTED VALUE=\""+O.ID()+"\">"+O.Name()+" ("+O.ID()+")");
				str.append("</SELECT>");
				str.append("<BR>");
				str.append("Stock: ");
				str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SDATA"+(i+1)+" VALUE=\""+theparm+"\">");
				if((theprice==null)||(theprice.equals("null")))
					theprice="-1";
				str.append("&nbsp;&nbsp;&nbsp;");
				str.append("Price: <INPUT TYPE=TEXT SIZE=5 NAME=SPRIC"+(i+1)+" VALUE=\""+theprice+"\">");
				str.append("</TD><TD WIDTH=10%>");
				if(!CMLib.flags().isCataloged(O))
				{
					//if(O instanceof MOB)
						//str.append("<INPUT TYPE=BUTTON NAME=EDITSHOPMOB"+(i+1)+" VALUE=EDIT ONCLICK=\"EditShopMob('"+RoomData.getMOBCode(RoomData.getMOBCache(),(MOB)O)+"');\">");
					//else
					if(O instanceof Item)
						str.append("<INPUT TYPE=BUTTON NAME=EDITSHOPITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditShopItem('"+RoomData.getItemCode(RoomData.getItemCache(),(Item)O)+"');\">");
				}
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=SHP"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an item");
			for (final Item I : RoomData.getItemCache())
			{
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+RoomData.getObjIDSuffix(I));
			}
			for (final MOB M : RoomData.getMOBCache())
			{
				str.append("<OPTION VALUE=\""+M+"\">"+M.Name()+RoomData.getObjIDSuffix(M));
			}
			StringBuffer bufA=(StringBuffer)Resources.getResource("MUDGRINDER-STORESTUFF"+theme);
			if(bufA==null)
			{
				bufA=new StringBuffer("");
				final List<String> sortMeA=new ArrayList<String>();
				for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
						sortMeA.add(CMClass.classID(A));
				}
				for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
					sortMeA.add(CMClass.classID(m.nextElement()));
				CMClass.addAllItemClassNames(sortMeA,true,true,false,theme);
				Collections.sort(sortMeA);
				for (final Object element : sortMeA)
				{
					final String cnam=(String)element;
					bufA.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
				Resources.submitResource("MUDGRINDER-STORESTUFF"+theme,bufA);
			}
			str.append(bufA);
			str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
			String[] names;
			names=CMLib.catalog().getCatalogMobNames();
			for (final String name : names)
				str.append("<OPTION VALUE=\"CATALOG-"+name+"\">"+name);
			names=CMLib.catalog().getCatalogItemNames();
			for (final String name : names)
				str.append("<OPTION VALUE=\"CATALOG-"+name+"\">"+name);
			str.append("</SELECT><BR>");
			str.append("Stock: <INPUT TYPE=TEXT SIZE=5 NAME=SDATA"+(theclasses.size()+1)+" VALUE=\"1\">");
			str.append("&nbsp;&nbsp;&nbsp;");
			str.append("Price: <INPUT TYPE=TEXT SIZE=5 NAME=SPRIC"+(theclasses.size()+1)+" VALUE=\"-1\">");
			str.append("</TD><TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDSHOPITEM VALUE=\"+Item\" ONCLICK=\"AddNewShopItem();\">");
			//str.append("<INPUT TYPE=BUTTON NAME=ADDSHOPMOB VALUE=\"+MOB\" ONCLICK=\"AddNewShopMOB();\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer itemList(Room R, MOB oldM, MOB M, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final int theme = (R!=null) ? R.getArea().getTheme() : CMProps.getIntVar(CMProps.Int.MUDTHEME);
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("ITEMLIST"))
		{
			final ArrayList<Item> classes=new ArrayList<Item>();
			ArrayList<Object> containers=new ArrayList<Object>();
			final ArrayList<Boolean> beingWorn=new ArrayList<Boolean>();
			List<Item> itemlist=null;
			if(httpReq.isUrlParameter("ITEM1"))
			{
				if(oldM!=M)
				{
					for(int i=0;i<oldM.numItems();i++)
						M.addItem(oldM.getItem(i));
				}

				containers=new ArrayList<Object>();
				itemlist=RoomData.getItemCache();
				final Vector<String> cstrings=new Vector<String>();
				for(int i=1;;i++)
				{
					final String MATCHING=httpReq.getUrlParameter("ITEM"+i);
					final String WORN=httpReq.getUrlParameter("ITEMWORN"+i);
					if(MATCHING==null)
						break;
					final Item I2=RoomData.getItemFromAnywhere(M,MATCHING);
					if(I2!=null)
					{
						classes.add(I2);
						beingWorn.add(Boolean.valueOf((WORN!=null)&&(WORN.equalsIgnoreCase("on"))));
						final String CONTAINER=httpReq.getUrlParameter("ITEMCONT"+i);
						cstrings.addElement((CONTAINER==null)?"":CONTAINER);
					}
				}
				for(int i=0;i<cstrings.size();i++)
				{
					final String CONTAINER=cstrings.elementAt(i);
					Item C2=null;
					if(CONTAINER.length()>0)
						C2=(Item)CMLib.english().fetchEnvironmental(classes,CONTAINER,true);
					containers.add((C2!=null)?(Object)C2:"");
				}
			}
			else
			{
				for(int m=0;m<M.numItems();m++)
				{
					final Item I2=M.getItem(m);
					if(I2!=null)
					{
						CMLib.catalog().updateCatalogIntegrity(I2);
						classes.add(I2);
						containers.add((I2.container()==null)?"":(Object)I2.container());
						beingWorn.add(Boolean.valueOf(!I2.amWearingAt(Wearable.IN_INVENTORY)));
					}
				}
				itemlist=RoomData.contributeItems(classes);
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<classes.size();i++)
			{
				final Item I=classes.get(i);
				final Item C=(classes.contains(containers.get(i))?(Item)containers.get(i):null);
				final Boolean W=beingWorn.get(i);
				str.append("<TR>");
				str.append("<TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				final String code=RoomData.getAppropriateCode(I,M,classes,itemlist);
				str.append("<OPTION SELECTED VALUE=\""+code+"\">"+I.Name()+" ("+I.ID()+")");
				str.append("</SELECT><BR>");
				str.append("<FONT COLOR=WHITE SIZE=-1>");
				str.append("Container: ");
				str.append("<SELECT NAME=ITEMCONT"+(i+1)+">");
				str.append("<OPTION VALUE=\"\" "+((C==null)?"SELECTED":"")+">In Inventory");
				for(int i2=0;i2<classes.size();i2++)
				{
					if((classes.get(i2) instanceof Container)&&(i2!=i))
					{
						final Container C2=(Container)classes.get(i2);
						final String name=CMLib.english().getContextName(classes,C2);
						str.append("<OPTION "+((C2==C)?"SELECTED":"")+" VALUE=\""+name+"\">"+name+" ("+C2.ID()+")");
					}
				}
				str.append("</SELECT>&nbsp;&nbsp; ");
				str.append("<INPUT TYPE=CHECKBOX NAME=ITEMWORN"+(i+1)+" "+(W.booleanValue()?"CHECKED":"")+">Worn/Wielded");
				str.append("</FONT></TD>");
				str.append("<TD WIDTH=10%>");
				if(!CMLib.flags().isCataloged(I))
					str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+RoomData.getItemCode(classes,I)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
			for (final Item I : itemlist)
			{
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+RoomData.getObjIDSuffix(I));
			}
			StringBuffer mposs=(StringBuffer)Resources.getResource("MUDGRINDER-MOBPOSS"+theme);
			if(mposs==null)
			{
				mposs=new StringBuffer("");
				final List<String> sortMe=new ArrayList<String>();
				CMClass.addAllItemClassNames(sortMe,true,true,false,theme);
				Collections.sort(sortMe);
				for (final Object element : sortMe)
					mposs.append("<OPTION VALUE=\""+(String)element+"\">"+(String)element);
				Resources.submitResource("MUDGRINDER-MOBPOSS"+theme,mposs);
			}
			str.append(mposs);
			str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
			final String[] names=CMLib.catalog().getCatalogItemNames();
			for (final String name : names)
				str.append("<OPTION VALUE=\"CATALOG-"+name+"\">"+name);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
			str.append("</TD></TR></TABLE>");
		}
		return str;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ROOM");
		if(last==null)
			return " @break@";
		final String mobCode=httpReq.getUrlParameter("MOB");
		if(mobCode==null)
			return "@break@";

		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			if(!last.equalsIgnoreCase("ANY"))
			{
				R=MUDGrinder.getRoomObject(httpReq, last);
				if(R==null)
					return "No Room?!";
				CMLib.map().resetRoom(R);
				httpReq.getRequestObjects().put(last,R);
			}
		}
		MOB M=null;
		synchronized(("SYNC"+((R!=null)?R.roomID():"null")).intern())
		{
			if(R!=null)
				R=CMLib.map().getRoom(R);
			M=(MOB)httpReq.getRequestObjects().get(mobCode);
			if(M==null)
			{
				if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
				{
					M=CMLib.catalog().getCatalogMob(mobCode.substring(8));
					if(M==null)
						M=CMClass.getMOB("GenMob");
					else
						M=(MOB)M.copyOf();
				}
				else
				if(mobCode.equals("NEW"))
					M=CMClass.getMOB("GenMob");
				else
				if(mobCode.equals("NEWDEITY"))
				{
					M=CMClass.getMOB("GenDeity");
					final String deityName=httpReq.getUrlParameter("NEWMOBNAME");
					if((M!=null)&&(deityName!=null))
					{
						M.setDisplayText(CMStrings.replaceAll(((Deity)M).displayText(),CMStrings.capitalizeFirstLetter(M.name()),deityName));
						((Deity)M).setClericRitual(CMStrings.replaceAll(((Deity)M).getClericRitual(),M.name(),deityName));
						((Deity)M).setWorshipRitual(CMStrings.replaceAll(((Deity)M).getWorshipRitual(),M.name(),deityName));
					}
				}
				else
				if(R!=null)
					M=RoomData.getMOBFromCode(R,mobCode);
				else
					M=RoomData.getMOBFromCode(RoomData.getMOBCache(),mobCode);
				if((M==null)
				||((!M.isSavable())&&((R==null)||(R.isSavable()))))
				{
					final StringBuffer str=new StringBuffer("No MOB?!");
					str.append(" Got: "+mobCode);
					str.append(", Includes: ");
					if(R!=null)
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M2=R.fetchInhabitant(m);
						if((M2!=null)&&(M2.isSavable()))
							str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
					}
					return clearWebMacros(str);
				}
				httpReq.getRequestObjects().put(mobCode,M);
			}
		}
		final MOB oldM=M;
		// important generic<->non generic swap!
		final String newClassID=httpReq.getUrlParameter("CLASSES");
		if((newClassID!=null)
		&&(!newClassID.equals(CMClass.classID(M)))
		&&(CMClass.getMOB(newClassID)!=null))
			M=CMClass.getMOB(newClassID);

		boolean changedClass=((httpReq.isUrlParameter("CHANGEDCLASS"))
							 &&(httpReq.getUrlParameter("CHANGEDCLASS")).equals("true"));
		changedClass=changedClass
					 &&(mobCode.equals("NEW")
							 ||mobCode.equalsIgnoreCase("NEWDEITY")
							 ||mobCode.startsWith("CATALOG-")
							 ||mobCode.startsWith("NEWCATA-"));
		final boolean changedLevel=((httpReq.isUrlParameter("CHANGEDLEVEL"))&&(httpReq.getUrlParameter("CHANGEDLEVEL")).equals("true"));
		final boolean firstTime=(!httpReq.isUrlParameter("ACTION"))
				||(!(httpReq.getUrlParameter("ACTION")).equals("MODIFYMOB"))
				||(changedClass);

		if(((changedLevel)||(changedClass))&&(M.isGeneric()))
		{
			CMLib.leveler().fillOutMOB(M,CMath.s_int(firstTime?"0":httpReq.getUrlParameter("LEVEL")));
			httpReq.addFakeUrlParameter("REJUV",""+M.basePhyStats().rejuv());
			httpReq.addFakeUrlParameter("ARMOR",""+M.basePhyStats().armor());
			httpReq.addFakeUrlParameter("DAMAGE",""+M.basePhyStats().damage());
			httpReq.addFakeUrlParameter("SPEED",""+M.basePhyStats().speed());
			httpReq.addFakeUrlParameter("ATTACK",""+M.basePhyStats().attackAdjustment());
			httpReq.addFakeUrlParameter("MONEY",""+CMLib.beanCounter().getMoney(M));
		}

		final StringBuffer str=new StringBuffer("");
		for(MOBDataField o : MOBDataField.values())
		{
			final String parmName=o.name();
			if(parms.containsKey(parmName))
			{
				String old=httpReq.getUrlParameter(parmName);
				if(old==null)
					old="";
				switch(o)
				{
				case NAME: // name
					if(firstTime)
					{
						if((mobCode.equalsIgnoreCase("NEW")||mobCode.equalsIgnoreCase("NEWDEITY")||mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
						&&(httpReq.isUrlParameter("NEWMOBNAME")))
							old=httpReq.getUrlParameter("NEWMOBNAME");
						else
							old=M.Name();
					}
					str.append(old);
					break;
				case CLASSES: // classes
					{
						if(firstTime)
							old=CMClass.classID(M);
						Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-MOBS");
						if(sorted==null)
						{
							final Vector<String> sortMe=new Vector<String>();
							for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
								sortMe.addElement(CMClass.classID(m.nextElement()));
							sorted=(new TreeSet<String>(sortMe)).toArray();
							Resources.submitResource("MUDGRINDER-MOBS",sorted);
						}
						if(parms.containsKey("CLASSESID"))
							str.append(old);
						else
						{
							for (final Object element : sorted)
							{
								final String cnam=(String)element;
								str.append("<OPTION VALUE=\""+cnam+"\"");
								if(old.equals(cnam))
									str.append(" SELECTED");
								str.append(">"+cnam);
							}
						}
					}
					break;
				case DISPLAYTEXT: // displaytext
					if(firstTime)
						old=M.displayText();
					str.append(old);
					break;
				case DESCRIPTION: // description
					if(firstTime)
						old=M.description();
					str.append(old);
					break;
				case LEVEL: // level
					if(firstTime)
						old=""+M.basePhyStats().level();
					str.append(old);
					break;
				case ABILITY: // ability;
					if(firstTime)
						old=""+M.basePhyStats().ability();
					str.append(old);
					break;
				case REJUV: // rejuv;
					if(firstTime)
						old=""+M.basePhyStats().rejuv();
					if(old.equals(""+Integer.MAX_VALUE))
						str.append("0");
					else
						str.append(old);
					break;
				case MISCTEXT: // misctext
					if(firstTime)
						old=M.text();
					str.append(old);
					break;
				case RACE: // race
					if(firstTime)
						old=""+M.baseCharStats().getMyRace().ID();
					for(final Enumeration<Race> r=sortedRaces(httpReq);r.hasMoreElements();)
					{
						final Race R2=r.nextElement();
						str.append("<OPTION VALUE=\""+R2.ID()+"\"");
						if(R2.ID().equals(old))
							str.append(" SELECTED");
						str.append(">"+R2.name());
					}
					if((changedClass)||(changedLevel))
					{
						final Race R3=CMClass.getRace(old);
						char G=(char)M.baseCharStats().getStat(CharStats.STAT_GENDER);
						if((httpReq.isUrlParameter("GENDER"))&&((httpReq.getUrlParameter("GENDER")).length()>0))
							G=(httpReq.getUrlParameter("GENDER")).charAt(0);
						if(R3!=null)
						{
							R3.setHeightWeight(M.basePhyStats(),G);
							httpReq.addFakeUrlParameter("WEIGHT",""+M.basePhyStats().weight());
							httpReq.addFakeUrlParameter("HEIGHT",""+M.basePhyStats().height());
						}
					}
					break;
				case GENDER: // gender
					if(firstTime)
						old=""+((char)M.baseCharStats().getStat(CharStats.STAT_GENDER));
					if(old.toUpperCase().startsWith("M"))
					{
						str.append("<INPUT TYPE=RADIO NAME=GENDER CHECKED VALUE=M>Male");
						str.append("&nbsp;&nbsp; <INPUT TYPE=RADIO NAME=GENDER VALUE=F>Female");
						str.append("&nbsp;&nbsp; <INPUT TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
					}
					else
					if(old.toUpperCase().startsWith("F"))
					{
						str.append("<INPUT TYPE=RADIO NAME=GENDER VALUE=M>Male");
						str.append("&nbsp;&nbsp; <INPUT TYPE=RADIO CHECKED NAME=GENDER VALUE=F>Female");
						str.append("&nbsp;&nbsp; <INPUT TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
					}
					else
					{
						str.append("<INPUT TYPE=RADIO NAME=GENDER VALUE=M>Male");
						str.append("&nbsp;&nbsp; <INPUT TYPE=RADIO NAME=GENDER VALUE=F>Female");
						str.append("&nbsp;&nbsp; <INPUT CHECKED TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
					}
					break;
				case HEIGHT: // height
					if(firstTime)
						old=""+M.basePhyStats().height();
					str.append(old);
					break;
				case WEIGHT: // weight
					if(firstTime)
						old=""+M.basePhyStats().weight();
					str.append(old);
					break;
				case SPEED: // speed
					if(firstTime)
						old=""+M.basePhyStats().speed();
					str.append(old);
					break;
				case ATTACK: // attack
					if(firstTime)
						old=""+M.basePhyStats().attackAdjustment();
					str.append(old);
					break;
				case DAMAGE: // damage
					if(firstTime)
						old=""+M.basePhyStats().damage();
					str.append(old);
					break;
				case ARMOR: // armor
					if(firstTime)
						old=""+M.basePhyStats().armor();
					str.append(old);
					break;
				case ALIGNMENT: // alignment
					if(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)
					{
						if(firstTime)
							old=""+M.fetchFaction(CMLib.factions().AlignID());
						for(final Faction.Align v : Faction.Align.values())
						{
							if(v!=Faction.Align.INDIFF)
							{
								str.append("<OPTION VALUE="+v.toString());
								if(old.equalsIgnoreCase(v.toString()))
									str.append(" SELECTED");
								str.append(">"+CMStrings.capitalizeAndLower(v.toString().toLowerCase()));
							}
						}
					}
					break;
				case MONEY: // money
					if(firstTime)
					{
						old=""+CMLib.beanCounter().getMoney(M);
						CMLib.beanCounter().clearInventoryMoney(M,null);
					}
					str.append(old);
					break;
				case ISRIDEABLE: // is rideable
					if(M instanceof Rideable)
						return "true";
					return "false";
				case RIDEABLETYPE: // rideable type
					if((firstTime)&&(M instanceof Rideable))
						old=""+((Rideable)M).rideBasis();
					for(int r=0;r<Rideable.RIDEABLE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==CMath.s_int(old))
							str.append(" SELECTED");
						str.append(">"+Rideable.RIDEABLE_DESCS[r]);
					}
					break;
				case MOBSHELD: // rideable capacity
					if((firstTime)&&(M instanceof Rideable))
						old=""+((Rideable)M).riderCapacity();
					str.append(old);
					break;
				case ISSHOPKEEPER: // is shopkeeper
					if(M instanceof ShopKeeper)
						return "true";
					return "false";
				case SHOPKEEPERTYPE: // shopkeeper type
				{
					final HashSet<Integer> shopTypes=new HashSet<Integer>();
					if((firstTime)&&(M instanceof ShopKeeper))
					{
						for(int d=0;d<ShopKeeper.DEAL_DESCS.length;d++)
						{
							if(((ShopKeeper)M).isSold(d))
								shopTypes.add(Integer.valueOf(d));
						}
					}
					else
					{
						shopTypes.add(Integer.valueOf(CMath.s_int(old)));
						int x=1;
						while(httpReq.getUrlParameter(parmName+x)!=null)
						{
							shopTypes.add(Integer.valueOf(CMath.s_int(httpReq.getUrlParameter(parmName+x))));
							x++;
						}
					}
					if(M instanceof Banker)
					{
						int r=ShopKeeper.DEAL_BANKER;
						str.append("<OPTION VALUE=\""+r+"\"");
						if(shopTypes.contains(Integer.valueOf(r)))
							str.append(" SELECTED");
						str.append(">"+ShopKeeper.DEAL_DESCS[r]);
						r=ShopKeeper.DEAL_CLANBANKER;
						str.append("<OPTION VALUE=\""+r+"\"");
						if(shopTypes.contains(Integer.valueOf(r)))
							str.append(" SELECTED");
						str.append(">"+ShopKeeper.DEAL_DESCS[r]);
					}
					else
					if(M instanceof PostOffice)
					{
						int r=ShopKeeper.DEAL_POSTMAN;
						str.append("<OPTION VALUE=\""+r+"\"");
						if(shopTypes.contains(Integer.valueOf(r)))
							str.append(" SELECTED");
						str.append(">"+ShopKeeper.DEAL_DESCS[r]);
						r=ShopKeeper.DEAL_CLANPOSTMAN;
						str.append("<OPTION VALUE=\""+r+"\"");
						if(shopTypes.contains(Integer.valueOf(r)))
							str.append(" SELECTED");
						str.append(">"+ShopKeeper.DEAL_DESCS[r]);
					}
					else
					for(int r=0;r<ShopKeeper.DEAL_DESCS.length;r++)
					{
						if((r!=ShopKeeper.DEAL_CLANBANKER)
						&&(r!=ShopKeeper.DEAL_BANKER)
						&&(r!=ShopKeeper.DEAL_POSTMAN)
						&&(r!=ShopKeeper.DEAL_CLANPOSTMAN))
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(shopTypes.contains(Integer.valueOf(r)))
								str.append(" SELECTED");
							str.append(">"+ShopKeeper.DEAL_DESCS[r]);
						}
					}
					break;
				}
				case ISGENERIC:
					if(M.isGeneric())
						return "true";
					return "false";
				case ISBANKER: // is banker
					if(M instanceof Banker)
						return "true";
					return "false";
				case COININT: // coin interest
					if((firstTime)&&(M instanceof Banker))
						old=""+((Banker)M).getCoinInterest();
					str.append(old);
					break;
				case ITEMINT: // item interest
					if((firstTime)&&(M instanceof Banker))
						old=""+((Banker)M).getItemInterest();
					str.append(old);
					break;
				case BANKNAME: // bank name
					if((firstTime)&&(M instanceof Banker))
						old=""+((Banker)M).bankChain();
					str.append(old);
					break;
				case SHOPPREJ: // prejudice factors
					if((firstTime)&&(M instanceof ShopKeeper))
						old=((ShopKeeper)M).prejudiceFactors();
					str.append(old);
					break;
				case ISDEITY: // is deity
					if(M instanceof Deity)
						return "true";
					return "false";
				case CLEREQ: // cleric requirements
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getClericRequirements();
					str.append(old);
					break;
				case CLERIT: // cleric ritual
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getClericRitual();
					str.append(old);
					break;
				case WORREQ: // worship requirements
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getWorshipRequirements();
					str.append(old);
					break;
				case WORRIT: // worship ritual
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getWorshipRitual();
					str.append(old);
					break;
				case CLESIN: // cleric sin
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getClericSin();
					str.append(old);
					break;
				case WORSIN: // worshipper sin
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getWorshipSin();
					str.append(old);
					break;
				case CLEPOW: // cleric power
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getClericPowerup();
					str.append(old);
					break;
				case CLANID: // clanid
					if(firstTime)
					{
						final StringBuilder oldBuf=new StringBuilder("");
						for(final Pair<Clan,Integer> p : M.clans())
							oldBuf.append(p.first.getName()).append("(").append(p.second.toString()).append("), ");
						old=oldBuf.toString();
					}
					str.append(old);
					break;
				case TATTOOS: // tattoos
					if(firstTime)
					{
						old="";
						for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
							str.append(e.nextElement().toString()).append(";");
					}
					else
						str.append(old);
					break;
				case EXPERTISES: // expertises
					if(firstTime)
					{
						old="";
						for(final Enumeration<String> x=M.expertises();x.hasMoreElements();)
							str.append(x.nextElement()).append(';');
					}
					else
						str.append(old);
					break;
				case BUDGET: // budget
					if((firstTime)&&(M instanceof ShopKeeper))
						old=((ShopKeeper)M).budget();
					str.append(old);
					break;
				case DEVALRATE: // devaluation rate
					if((firstTime)&&(M instanceof ShopKeeper))
						old=((ShopKeeper)M).devalueRate();
					str.append(old);
					break;
				case INVRESETRATE: // inventory reset rate
					if((firstTime)&&(M instanceof ShopKeeper))
						old=""+((ShopKeeper)M).invResetRate();
					str.append(old);
					break;
				case IMAGE: // image
					if(firstTime)
						old=M.rawImage();
					str.append(old);
					break;
				case ISPOSTMAN: // ispostman
					if(M instanceof PostOffice)
						return "true";
					return "false";
				case POSTCHAIN: // postal chain
					if((firstTime)&&(M instanceof PostOffice))
						old=((PostOffice)M).postalChain();
					str.append(old);
					break;
				case POSTMIN: // minimum postage
					if((firstTime)&&(M instanceof PostOffice))
						old=""+((PostOffice)M).minimumPostage();
					str.append(old);
					break;
				case POSTLBS: // postage per pound
					if((firstTime)&&(M instanceof PostOffice))
						old=""+((PostOffice)M).postagePerPound();
					str.append(old);
					break;
				case POSTHOLD: // holding fee per pound
					if((firstTime)&&(M instanceof PostOffice))
						old=""+((PostOffice)M).holdFeePerPound();
					str.append(old);
					break;
				case POSTNEW: // new box fee
					if((firstTime)&&(M instanceof PostOffice))
						old=""+((PostOffice)M).feeForNewBox();
					str.append(old);
					break;
				case POSTHELD: // max held months
					if((firstTime)&&(M instanceof PostOffice))
						old=""+((PostOffice)M).maxMudMonthsHeld();
					str.append(old);
					break;
				case ISLIBRARIAN: // is librarian
					if(M instanceof Librarian)
						return "true";
					return "false";
				case LIBRCHAIN: // library chain
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).libraryChain();
					str.append(old);
					break;
				case LIBROVERCHG: // library overdue charge
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).getOverdueCharge();
					str.append(old);
					break;
				case LIBRDAYCHG: // library daily overdue charge
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).getDailyOverdueCharge();
					str.append(old);
					break;
				case LIBROVERPCT: // library overdue pct charge
					if((firstTime)&&(M instanceof Librarian))
						old=CMath.toPct(((Librarian)M).getOverdueChargePct());
					str.append(old);
					break;
				case LIBDAYPCT: // library daily overdue pct charge
					if((firstTime)&&(M instanceof Librarian))
						old=CMath.toPct(((Librarian)M).getDailyOverdueChargePct());
					str.append(old);
					break;
				case LIBMINDAYS: // library overdue days
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).getMinOverdueDays();
					str.append(old);
					break;
				case LIBMAXDAYS: // library reclaim days
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).getMaxOverdueDays();
					str.append(old);
					break;
				case LIBMAXBORROW: // library max borrowed
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).getMaxBorrowed();
					str.append(old);
					break;
				case LIBCMASK: // library contributor mask
					if((firstTime)&&(M instanceof Librarian))
						old=""+((Librarian)M).contributorMask();
					str.append(old);
					break;
				case IGNOREMASK: // ignore mask
					if((firstTime)&&(M instanceof ShopKeeper))
						old=((ShopKeeper)M).ignoreMask();
					str.append(old);
					break;
				case LOANINT: // loan interest
					if((firstTime)&&(M instanceof Banker))
						old=""+((Banker)M).getLoanInterest();
					str.append(old);
					break;
				case SVCRIT: // service ritual
					if((firstTime)&&(M instanceof Deity))
						old=((Deity)M).getServiceRitual();
					str.append(old);
					break;
				case AUCCHAIN: // auction chain
					if((firstTime)&&(M instanceof Auctioneer))
						old=((Auctioneer)M).auctionHouse();
					str.append(old);
					break;
				case LIVELIST: // live list
					//if((firstTime)&&(M instanceof Auctioneer))
					//    old=""+((Auctioneer)M).liveListingPrice();
					//if(CMath.s_double(old)<0.0) old="";
					//str.append(old);
					break;
				case TIMELIST: // timed list
					if((firstTime)&&(M instanceof Auctioneer))
						old=""+((Auctioneer)M).timedListingPrice();
					if(CMath.s_double(old)<0.0)
						old="";
					str.append(old);
					break;
				case TIMELISTPCT: // timed list pct
					if((firstTime)&&(M instanceof Auctioneer))
						old=""+(((Auctioneer)M).timedListingPct()*100.0)+"%";
					if(CMath.s_pct(old)<0.0)
						old="";
					str.append(old);
					break;
				case LIVECUT: // live cut pct
					//if((firstTime)&&(M instanceof Auctioneer))
					//    old=""+(((Auctioneer)M).liveFinalCutPct()*100.0)+"%";
					//if(CMath.s_pct(old)<0.0) old="";
					str.append(old);
					break;
				case TIMECUT: // timed cut pct
					if((firstTime)&&(M instanceof Auctioneer))
						old=""+(((Auctioneer)M).timedFinalCutPct()*100.0)+"%";
					if(CMath.s_pct(old)<0.0)
						old="";
					str.append(old);
					break;
				case MAXDAYS: // max days
					if((firstTime)&&(M instanceof Auctioneer))
						old=""+((Auctioneer)M).maxTimedAuctionDays();
					if(CMath.s_double(old)<0.0)
						old="";
					str.append(old);
					break;
				case MINDAYS: // min days
					if((firstTime)&&(M instanceof Auctioneer))
						old=""+((Auctioneer)M).minTimedAuctionDays();
					if(CMath.s_double(old)<0.0)
						old="";
					str.append(old);
					break;
				case ISAUCTION: // is auction
					if(M instanceof Auctioneer)
						return "true";
					return "false";
				case DEITYID: // deityid
				{
					if(firstTime)
						old=M.getWorshipCharID();
					for(final Enumeration<Deity> d=CMLib.map().deities();d.hasMoreElements();)
					{
						final Deity D=d.nextElement();
						str.append("<OPTION VALUE=\""+D.Name()+"\"");
						if(D.Name().equalsIgnoreCase(old))
							str.append(" SELECTED");
						str.append(">"+D.Name());
					}
					break;
				}
				case VARMONEY: // varmoney
					if(firstTime)
						old=""+M.getMoneyVariation();
					str.append(old);
					break;
				case CATACAT: // catacat
					if((firstTime)&&(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-")))
					{
						final String name=mobCode.substring(8);
						final CatalogLibrary.CataData data=CMLib.catalog().getCatalogMobData(name);
						if(data!=null)
							old=data.category();
					}
					str.append(old+", ");
					break;
				case CURSES:
					// moved to below
					break;
				case POWERS:
					// moved to below
					break;
				case SELLIMASK:
					if(M instanceof ShopKeeper)
						str.append(((ShopKeeper)M).getWhatIsSoldZappermask());
					break;
				case MOUNTSTR: // mountstr
					if((firstTime)&&(M instanceof Rideable))
						old=((Rideable)M).mountString(0,CMClass.sampleMOB());
					str.append(old+", ");
					break;
				case DISMOUNTSTR: // dismountstr
					if((firstTime)&&(M instanceof Rideable))
						old=((Rideable)M).dismountString(CMClass.sampleMOB());
					str.append(old+", ");
					break;
				case STATESTR: // statestr
					if((firstTime)&&(M instanceof Rideable))
						old=((Rideable)M).stateString(CMClass.sampleMOB());
					str.append(old+", ");
					break;
				case STATESUBJSTR: // statesubjstr
					if((firstTime)&&(M instanceof Rideable))
						old=((Rideable)M).stateStringSubject(CMClass.sampleMOB());
					str.append(old+", ");
					break;
				case RIDERSTR: // riderstr
					if((firstTime)&&(M instanceof Rideable))
						old=((Rideable)M).rideString(CMClass.sampleMOB());
					str.append(old+", ");
					break;
				}
				if(firstTime)
					httpReq.addFakeUrlParameter(parmName,old.equals("checked")?"on":old);
			}
		}
		str.append(ExitData.dispositions(M,firstTime,httpReq,parms));
		str.append(MobData.senses(M,firstTime,httpReq,parms));
		str.append(AreaData.affects(M,httpReq,parms,1));
		str.append(AreaData.behaves(M,httpReq,parms,1));
		str.append(factions(M,httpReq,parms,1));
		str.append(MobData.abilities(M,httpReq,parms,1));
		str.append(MobData.clans(M,httpReq,parms,1));
		if(M instanceof Deity)
		{
			str.append(MobData.blessings((Deity)M,httpReq,parms,1));
			str.append(MobData.curses((Deity)M,httpReq,parms,1));
			str.append(MobData.powers((Deity)M,httpReq,parms,1));
		}
		if(M instanceof ShopKeeper)
			str.append(MobData.shopkeeper(R,(ShopKeeper)M,httpReq,parms,1));

		str.append(itemList(R,oldM,M,httpReq,parms,1));

		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
