package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
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

import java.util.*;

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
@SuppressWarnings({"unchecked","rawtypes"})
public class RaceData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "RaceData";
	}

	// valid parms include HELP, STATS, SENSES, TRAINS, PRACS, ABILITIES,
	// HEALTHTEXTS, NATURALWEAPON, PLAYABLE, DISPOSITIONS, STARTINGEQ,
	// CLASSES, LANGS, EFFECTS

	private String raceDropDown(HTTPRequest httpReq, String old)
	{
		final StringBuffer str=new StringBuffer("");
		str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">None");
		Race R2=null;
		String R2ID=null;
		for(final Enumeration e=MobData.sortedRaces(httpReq);e.hasMoreElements();)
		{
			R2=(Race)e.nextElement();
			R2ID="com.planet_ink.coffee_mud.Races."+R2.ID();
			if(R2.isGeneric() && CMClass.checkForCMClass(CMObjectType.RACE,R2ID))
			{
				str.append("<OPTION VALUE=\""+R2.ID()+"\" "+((old.equalsIgnoreCase(R2.ID()))?"SELECTED":"")+">"+R2.ID()+" (Generic)");
				str.append("<OPTION VALUE=\""+R2ID+"\" "+((old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2ID);
			}
			else
			if(R2.isGeneric())
				str.append("<OPTION VALUE=\""+R2.ID()+"\" "+((old.equalsIgnoreCase(R2.ID())||old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2.ID()+" (Generic)");
			else
				str.append("<OPTION VALUE=\""+R2ID+"\" "+((old.equalsIgnoreCase(R2.ID())||old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2ID);
		}
		return str.toString();
	}

	public static StringBuffer estats(PhyStats E, char c, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		final PairVector<String,String> theclasses=new PairVector<String,String>();
		if(httpReq.isUrlParameter(c+"ESTATS1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"ESTATS"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					if(!behav.equalsIgnoreCase("REJUV"))
					{
						String prof=httpReq.getUrlParameter(c+"ESTATSV"+num);
						if(prof==null)
							prof="0";
						prof=""+CMath.s_int(prof);
						theclasses.addElement(behav,prof);
					}
				}
				num++;
				behav=httpReq.getUrlParameter(c+"ESTATS"+num);
			}
		}
		else
		{
			for(int i=0;i<E.getStatCodes().length;i++)
			{
				if(CMath.s_int(E.getStat(E.getStatCodes()[i]))!=0)
					theclasses.addElement(E.getStatCodes()[i],Integer.toString(CMath.s_int(E.getStat(E.getStatCodes()[i]))));
			}
		}
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.elementAt(i).first;
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"ESTATS"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=65%>");
			str.append("<INPUT TYPE=TEXT NAME="+c+"ESTATSV"+(i+1)+" VALUE=\""+theclasses.elementAt(i).second+"\" SIZE=4 MAXLENGTH=4>");
			str.append("</TD>");
			str.append("</TR>");
		}
		str.append("<TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"ESTATS"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
		for(int i=0;i<E.getStatCodes().length;i++)
		{
			if((CMath.isNumber(E.getStat(E.getStatCodes()[i])))&&(!theclasses.contains(E.getStatCodes()[i])))
				str.append("<OPTION VALUE=\""+E.getStatCodes()[i]+"\">"+E.getStatCodes()[i]);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=65%>");
		str.append("<INPUT TYPE=TEXT NAME="+c+"ESTATSV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=4 MAXLENGTH=4>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer cstats(CharStats E, char c, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		final PairVector<String,String> theclasses=new PairVector<String,String>();
		if(httpReq.isUrlParameter(c+"CSTATS1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"CSTATS"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String prof=httpReq.getUrlParameter(c+"CSTATSV"+num);
					if(prof==null)
						prof="0";
					prof=""+CMath.s_int(prof);
					theclasses.addElement(behav,prof);
				}
				num++;
				behav=httpReq.getUrlParameter(c+"CSTATS"+num);
			}
		}
		else
		{
			for(final int i : CharStats.CODES.ALLCODES())
			{
				if(CMath.s_int(E.getStat(CharStats.CODES.NAME(i)))!=0)
					theclasses.addElement(CharStats.CODES.NAME(i),E.getStat(CharStats.CODES.NAME(i)));
			}
		}
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.elementAt(i).first;
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"CSTATS"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=65%>");
			str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATSV"+(i+1)+" VALUE=\""+theclasses.elementAt(i).second+"\" SIZE=4 MAXLENGTH=4>");
			str.append("</TD>");
			str.append("</TR>");
		}
		str.append("<TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"CSTATS"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(!theclasses.contains(CharStats.CODES.NAME(i)))
				str.append("<OPTION VALUE=\""+CharStats.CODES.NAME(i)+"\">"+CharStats.CODES.DESC(i));
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=65%>");
		str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATSV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=4 MAXLENGTH=4>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer cstate(CharState E, char c, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		final PairVector<String,String> theclasses=new PairVector<String,String>();
		if(httpReq.isUrlParameter(c+"CSTATE1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"CSTATE"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String prof=httpReq.getUrlParameter(c+"CSTATEV"+num);
					if(prof==null)
						prof="0";
					prof=""+CMath.s_int(prof);
					theclasses.addElement(behav,prof);
				}
				num++;
				behav=httpReq.getUrlParameter(c+"CSTATE"+num);
			}
		}
		else
		{
			for(int i=0;i<E.getStatCodes().length;i++)
			{
				if(CMath.s_int(E.getStat(E.getStatCodes()[i]))!=0)
					theclasses.addElement(E.getStatCodes()[i],Integer.valueOf(E.getStat(E.getStatCodes()[i])).toString());
			}
		}
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.elementAt(i).first;
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"CSTATE"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=65%>");
			str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATEV"+(i+1)+" VALUE=\""+theclasses.elementAt(i).second+"\" SIZE=4 MAXLENGTH=4>");
			str.append("</TD>");
			str.append("</TR>");
		}
		str.append("<TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"CSTATE"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
		for(int i=0;i<E.getStatCodes().length;i++)
		{
			if(CMath.isNumber(E.getStat(E.getStatCodes()[i])))
			{
				if(!theclasses.contains(E.getStatCodes()[i]))
					str.append("<OPTION VALUE=\""+E.getStatCodes()[i]+"\">"+E.getStatCodes()[i]);
			}
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=65%>");
		str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATEV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=4 MAXLENGTH=4>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer itemList(List<? extends Item> items, char c, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, boolean one)
	{
		if(items==null)
			items=new Vector<Item>();
		final StringBuffer str=new StringBuffer("");
		final Vector<Item> classes=new Vector<Item>();
		List<Item> itemlist=null;
		if(httpReq.isUrlParameter(c+"ITEM1"))
		{
			itemlist=RoomData.getItemCache();
			for(int i=1;;i++)
			{
				final String MATCHING=httpReq.getUrlParameter(c+"ITEM"+i);
				if(MATCHING==null)
					break;
				Item I2=RoomData.getItemFromAnywhere(itemlist,MATCHING);
				if(I2==null)
				{
					I2=RoomData.getItemFromAnywhere(items,MATCHING);
					if(I2!=null)
						RoomData.contributeItems(new XVector<Item>(I2));
				}
				if(I2!=null)
					classes.addElement(I2);
				if(one)
					break;
			}
		}
		else
		{
			classes.addAll(items);
			itemlist=RoomData.contributeItems(classes);
		}
		str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
		int numItems=0;
		if(!one)
		for(int i=0;i<classes.size();i++)
		{
			numItems++;
			final Item I=classes.elementAt(i);
			str.append("<TR>");
			str.append("<TD WIDTH=90%>");
			str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME="+c+"ITEM"+(numItems)+">");
			if(!one)
				str.append("<OPTION VALUE=\"\">Delete!");
			if(items.contains(I))
				str.append("<OPTION SELECTED VALUE=\""+RoomData.getItemCode(classes,I)+"\">"+I.Name()+" ("+I.ID()+")");
			else
			if(itemlist.contains(I))
				str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
			else
				str.append("<OPTION SELECTED VALUE=\""+I.ID()+"\">"+I.Name()+" ("+I.ID()+")");
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME="+c+"EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+RoomData.getItemCode(classes,I)+"','"+c+"ITEM"+(numItems)+"');\">");
			str.append("</TD></TR>");
		}
		str.append("<TR><TD WIDTH=90%>");
		str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME="+c+"ITEM"+(numItems+1)+">");
		if(!one)
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
		for(final Item I : itemlist)
		{
			if(one&&(classes.contains(I)))
			{
				if(items.contains(I))
					str.append("<OPTION SELECTED VALUE=\""+RoomData.getItemCode(classes,I)+"\">"+I.Name()+" ("+I.ID()+")");
				else
					str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
			}
			else
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+RoomData.getObjIDSuffix(I));
		}
		if(one)
		{
			final List<String> sortMe=new ArrayList();
			CMClass.addAllItemClassNames(sortMe,true,true,false,CMProps.getIntVar(CMProps.Int.MUDTHEME));
			Collections.sort(sortMe);
			Set<String> found=new TreeSet<String>();
			for (final Object element : sortMe)
			{
				boolean selected=false;
				for(int x=0;x<classes.size();x++)
				{
					if(classes.elementAt(x).ID().equals(element))
					{ 
						selected=true;
						found.add(classes.elementAt(x).ID());
						break;
					}
				}
				str.append("<OPTION "+(selected?"SELECTED":"")+" VALUE=\""+(String)element+"\">"+(String)element);
			}
			for(int x=0;x<classes.size();x++)
			{
				if(!found.contains(classes.elementAt(x).ID()))
					str.append("<OPTION SELECTED VALUE=\""+classes.elementAt(x).ID()+"\">"+classes.elementAt(x).ID());
			}
		}
		else
		{
			StringBuffer mposs=(StringBuffer)Resources.getResource("MUDGRINDER-OTHERPOSS");
			if(mposs==null)
			{
				mposs=new StringBuffer("");
				final List<String> sortMe=new ArrayList<String>();
				CMClass.addAllItemClassNames(sortMe,true,true,false,CMProps.getIntVar(CMProps.Int.MUDTHEME));
				Collections.sort(sortMe);
				for (final Object element : sortMe)
					mposs.append("<OPTION VALUE=\""+(String)element+"\">"+(String)element);
				Resources.submitResource("MUDGRINDER-OTHERPOSS",mposs);
			}
			str.append(mposs);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=10%>");
		str.append("<INPUT TYPE=BUTTON NAME="+c+"ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem('"+c+"ITEM"+(numItems+1)+"');\">");
		str.append("</TD></TR></TABLE>");
		return str;
	}

	public static StringBuffer dynAbilities(final MOB mob, List<Ability> ables, String ID, Modifiable obj, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, String font)
	{
		final StringBuffer str=new StringBuffer("");
		final DVector theclasses=new DVector(6);
		final boolean supportsRoles=CMParms.contains(obj.getStatCodes(), "GETRABLEROLE");
		if(httpReq.isUrlParameter("RABLES1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("RABLES"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String prof=httpReq.getUrlParameter("RABPOF"+num);
					if(prof==null)
						prof="0";
					String qual=httpReq.getUrlParameter("RABQUA"+num);
					if(qual==null)
						qual="";
					String parm=httpReq.getUrlParameter("RABPRM"+num);
					if(parm==null)
						parm="";
					String levl=httpReq.getUrlParameter("RABLVL"+num);
					if(levl==null)
						levl="0";
					String roles=null;
					if(supportsRoles)
						roles=httpReq.getUrlParameter("RABROL"+num);
					if(roles==null) 
						roles="";
					theclasses.addElement(behav,prof,qual,levl,parm,roles);
				}
				num++;
				behav=httpReq.getUrlParameter("RABLES"+num);
			}
		}
		else
		{
			QuadVector<String,Integer,Integer,Boolean> cables;
			if(obj instanceof Race)
				cables=((Race)obj).culturalAbilities();
			else
				cables=new QuadVector<String,Integer,Integer,Boolean>();
			for (final Ability A : ables)
			{
				if((A!=null)&&(!cables.containsFirst(A.ID())))
				{
					AbilityMapper.AbilityMapping ableMap=CMLib.ableMapper().getAbleMap(ID, A.ID());
					final boolean defaultGain = ableMap.autoGain();
					final int qualifyingLevel = ableMap.qualLevel();
					final String defaultParm = ableMap.defaultParm();
					String roles=null;
					if(supportsRoles && (obj instanceof ClanGovernment))
					{
						roles="";
						for(String key : ableMap.extFields().keySet())
						{
							ClanPosition P=((ClanGovernment)obj).findPositionRole(key);
							if(P!=null)
								roles+=", "+P.getID();
						}
						if(roles.length()>2)
							roles=roles.substring(2);
					}
					if(roles==null) 
						roles="";
					theclasses.addElement(A.ID(),A.proficiency()+"",defaultGain?"":"on",qualifyingLevel+"",defaultParm,roles);
				}
			}
		}
		if(font==null)
			font="<FONT COLOR=WHITE><B>";
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=(String)theclasses.get(i,1);
			str.append("<TR><TD COLSPAN=4><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=RABLES"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=25%>");
			str.append(font+"Lvl:</B></FONT> <INPUT TYPE=TEXT NAME=RABLVL"+(i+1)+" VALUE=\""+theclasses.get(i,4)+"\" SIZE=3 MAXLENGTH=3>");
			str.append("</TD>");
			str.append("<TD WIDTH=15%>");
			str.append(font+"<INPUT TYPE=TEXT NAME=RABPOF"+(i+1)+" VALUE=\""+theclasses.get(i,2)+"\" SIZE=3 MAXLENGTH=3>%</B></I></FONT>");
			str.append("</TD>");
			str.append("<TD WIDTH=25%>");
			str.append("<INPUT TYPE=CHECKBOX NAME=RABQUA"+(i+1)+" "+(theclasses.get(i,3).toString().equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Qualify Only</B></FONT></I>");
			str.append("</TD>");
			str.append("</TR>");
			str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=3>");
			str.append(font+"Parms:</B></FONT> <INPUT TYPE=TEXT NAME=RABPRM"+(i+1)+" VALUE=\""+theclasses.get(i,5)+"\" SIZE=40 MAXLENGTH=100>");
			str.append("</TD></TR>");
			if(supportsRoles)
			{
				str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=3>");
				str.append(font+"Roles:</B></FONT> <INPUT TYPE=TEXT NAME=RABROL"+(i+1)+" VALUE=\""+theclasses.get(i,6)+"\" SIZE=40 MAXLENGTH=100>");
				str.append("</TD></TR>");
			}
			str.append("</TABLE></TD></TR>");
		}
		str.append("<TR><TD COLSPAN=4><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=RABLES"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			final String cnam=A.ID();
			if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON))
				continue;
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=25%>");
		str.append(font+"Lvl:</B></I></FONT> <INPUT TYPE=TEXT NAME=RABLVL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
		str.append("</TD>");
		str.append("<TD WIDTH=15%>");
		str.append(font+"<INPUT TYPE=TEXT NAME=RABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>%</B></I></FONT>");
		str.append("</TD>");
		str.append("<TD WIDTH=25%>");
		str.append("<INPUT TYPE=CHECKBOX NAME=RABQUA"+(theclasses.size()+1)+" >"+font+"Qualify Only</B></I></FONT>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=3>");
		str.append(font+"Parms:</B></FONT> <INPUT TYPE=TEXT NAME=RABPRM"+(theclasses.size()+1)+" VALUE=\"\" SIZE=40 MAXLENGTH=100>");
		str.append("</TD></TR>");
		if(supportsRoles)
		{
			str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=3>");
			str.append(font+"Roles:</B></FONT> <INPUT TYPE=TEXT NAME=RABROL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=40 MAXLENGTH=100>");
			str.append("</TD></TR>");
		}
		str.append("</TABLE></TD></TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer dynEffects(String ID, Modifiable obj, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, String font)
	{
		final StringBuffer str=new StringBuffer("");
		final QuadVector<String,String,String,String> theclasses=new QuadVector<String,String,String,String>();
		boolean supportsRoles=CMParms.contains(obj.getStatCodes(), "GETREFFROLE");
		if(httpReq.isUrlParameter("REFFS1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("REFFS"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String parm=httpReq.getUrlParameter("REFPRM"+num);
					if(parm==null)
						parm="";
					String levl=httpReq.getUrlParameter("REFLVL"+num);
					if(levl==null)
						levl="0";
					String roles=null;
					if(supportsRoles)
						roles=httpReq.getUrlParameter("REFROL"+num);
					if(roles==null) 
						roles="";
					theclasses.addElement(behav,parm,levl,roles);
				}
				num++;
				behav=httpReq.getUrlParameter("REFFS"+num);
			}
		}
		else
		{
			final int numAbles=CMath.s_int(obj.getStat("NUMREFF"));
			for(int a=0;a<numAbles;a++)
			{
				final String ableID=obj.getStat("GETREFF"+a);
				final String ableParm=obj.getStat("GETREFFPARM"+a);
				final int qualifyingLevel = CMath.s_int(obj.getStat("GETREFFLVL"+a));
				String roles=null;
				final String roleList=obj.getStat("GETREFFROLE"+a);
				if(supportsRoles && (obj instanceof ClanGovernment)&&(roleList!=null)&&(roleList.length()>0))
				{
					roles="";
					for(String key : CMParms.parseCommas(roleList,true))
					{
						ClanPosition P=((ClanGovernment)obj).findPositionRole(key);
						if(P!=null)
							roles+=", "+P.getID();
					}
					if(roles.length()>2)
						roles=roles.substring(2);
				}
				if(roles==null) 
					roles="";
				theclasses.addElement(ableID,ableParm,qualifyingLevel+"",roles);
			}
		}
		if(font==null)
			font="<FONT COLOR=WHITE><B>";
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.elementAt(i).first;
			str.append("<TR><TD COLSPAN=3><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=REFFS"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=25%>");
			str.append(font+"Lvl:</B></FONT> <INPUT TYPE=TEXT NAME=REFLVL"+(i+1)+" VALUE=\""+theclasses.elementAt(i).third+"\" SIZE=3 MAXLENGTH=3>");
			str.append("</TD>");
			str.append("<TD WIDTH=40%>");
			str.append("<INPUT TYPE=TEXT NAME=REFPRM"+(i+1)+" VALUE=\""+theclasses.elementAt(i).second+"\" SIZE=25>");
			str.append("</TD>");
			str.append("</TR>");
			if(supportsRoles)
			{
				str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=2>");
				str.append(font+"Roles:</B></FONT> <INPUT TYPE=TEXT NAME=REFROL"+(i+1)+" VALUE=\""+theclasses.elementAt(i).fourth+"\" SIZE=40 MAXLENGTH=100>");
				str.append("</TD></TR>");
			}
			str.append("</TABLE></TD></TR>");
		}
		str.append("<TR><TD COLSPAN=3><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=REFFS"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final String cnam=a.nextElement().ID();
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=25%>");
		str.append(font+"Lvl:</B></I></FONT> <INPUT TYPE=TEXT NAME=REFLVL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
		str.append("</TD>");
		str.append("<TD WIDTH=40%>");
		str.append("<INPUT TYPE=TEXT NAME=REFPRM"+(theclasses.size()+1)+" VALUE=\"\" SIZE=25>");
		str.append("</TD>");
		str.append("</TR>");
		if(supportsRoles)
		{
			str.append("<TR><TD WIDTH=35%>&nbsp;</TD><TD COLSPAN=2>");
			str.append(font+"Roles:</B></FONT> <INPUT TYPE=TEXT NAME=REFROL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=40 MAXLENGTH=100>");
			str.append("</TD></TR>");
		}
		str.append("</TABLE></TD></TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer dynImmunities(String ID, Modifiable obj, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, String font)
	{
		final StringBuffer str=new StringBuffer("");
		final List<String> theclasses=new Vector<String>();
		if(httpReq.isUrlParameter("IABLE1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("IABLE"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					theclasses.add(behav);
				}
				num++;
				behav=httpReq.getUrlParameter("IABLE"+num);
			}
		}
		else
		{
			final int numAbles=CMath.s_int(obj.getStat("NUMIABLE"));
			for(int a=0;a<numAbles;a++)
			{
				final String ableID=obj.getStat("GETIABLE"+a);
				theclasses.add(ableID);
			}
		}
		if(font==null)
			font="<FONT COLOR=WHITE><B>";
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.get(i);
			str.append("<TR><TD><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=IABLE"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("</TR>");
			str.append("</TABLE></TD></TR>");
		}
		str.append("<TR><TD><TABLE BORDER=0 WIDTH=100% CELLSPACING=0 CELLPADDING=0><TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=IABLE"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final String cnam=a.nextElement().ID();
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("</TABLE></TD></TR>");
		str.append("</TABLE>");
		return str;
	}

	public static StringBuffer cabilities(Race E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, String font)
	{
		final StringBuffer str=new StringBuffer("");
		final QuadVector<String,Integer,Integer,Boolean> theclasses=new QuadVector<String,Integer,Integer,Boolean>();
		if(httpReq.isUrlParameter("CABLES1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("CABLES"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String prof=httpReq.getUrlParameter("CABPOF"+num);
					if(prof==null)
						prof="0";
					String qual=httpReq.getUrlParameter("CABQUA"+num);
					if(qual==null) 
						qual="";// null means unchecked
					String levl=httpReq.getUrlParameter("CABLVL"+num);
					if(levl==null)
						levl="0";
					theclasses.addElement(behav,Integer.valueOf(CMath.s_int(prof)),Integer.valueOf(CMath.s_int(levl)),qual.equals("on")?Boolean.TRUE:Boolean.FALSE);
				}
				num++;
				behav=httpReq.getUrlParameter("CABLES"+num);
			}
		}
		else
		{
			theclasses.addAll(E.culturalAbilities());
			for(Quad<String,Integer,Integer,Boolean> Q : theclasses)
				Q.fourth = Boolean.valueOf(!Q.fourth.booleanValue());
		}
		if(font==null)
			font="<FONT COLOR=WHITE><B>";
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=theclasses.elementAt(i).first;
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CABLES"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=20%>");
			str.append(font+"Pct:</B></I></FONT> <INPUT TYPE=TEXT NAME=CABPOF"+(i+1)+" VALUE=\""+theclasses.elementAt(i).second+"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
			str.append("</TD>");
			str.append("<TD WIDTH=20%>");
			str.append(font+"Lvl:</B></I></FONT><INPUT TYPE=TEXT NAME=CABLVL"+(i+1)+" VALUE=\""+theclasses.elementAt(i).third+"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
			str.append("</TD>");
			str.append("<TD WIDTH=25%>");
			str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+(i+1)+" "+(theclasses.elementAt(i).fourth.booleanValue()?"CHECKED":"")+">"+font+"Qualify Only</B></FONT></I>&nbsp;");
			str.append("</TD>");
			str.append("</TR>");
		}
		str.append("<TR><TD WIDTH=35%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CABLES"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final String cnam=a.nextElement().ID();
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		str.append("</SELECT>");
		str.append("</TD>");
		int i=theclasses.size()+1;
		str.append("<TD WIDTH=20%>");
		str.append(font+"Pct:</B></I></FONT> <INPUT TYPE=TEXT NAME=CABPOF"+i+" VALUE=\"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
		str.append("</TD>");
		str.append("</TD>");
		str.append("<TD WIDTH=20%>");
		str.append(font+"Lvl:</B></I></FONT><INPUT TYPE=TEXT NAME=CABLVL"+i+" VALUE=\"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
		str.append("</TD>");
		str.append("<TD WIDTH=25%>");
		str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+i+" >"+font+"Qualify Only</B></FONT></I>&nbsp;");
		str.append("</TD>");
		str.append("</TR>");
		str.append("</TABLE>");
		return str;
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

		String last=httpReq.getUrlParameter("RACE");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			if(parms.containsKey("ISGENERIC"))
			{
				final Race R2=CMClass.getRace(last);
				return ""+((R2!=null)&&(R2.isGeneric()));
			}

			final String newRaceID=httpReq.getUrlParameter("NEWRACE");
			Race R = (Race)httpReq.getRequestObjects().get("RACE-"+last);
			if((R==null)
			&&(newRaceID!=null)
			&&(newRaceID.length()>0)
			&&(CMClass.getRace(newRaceID)==null))
			{
				R=(Race)CMClass.getRace("GenRace").copyOf();
				R.setRacialParms("<RACE><ID>"+newRaceID+"</ID><NAME>"+newRaceID+"</NAME></RACE>");
				last=newRaceID;
				httpReq.addFakeUrlParameter("RACE",newRaceID);
			}
			if(R==null)
				R=CMClass.getRace(last);
			if(parms.containsKey("ISNEWRACE"))
				return ""+(CMClass.getRace(last)==null);

			if(R!=null)
			{
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText(R.ID(),null,false,true);
					if(s==null)
						s=CMLib.help().getHelpText(R.name(),null,false,true);
					if(s!=null)
					{
						if(s.toString().startsWith("<RACE>"))
							s=new StringBuilder(s.toString().substring(6));
						int limit=78;
						if(parms.containsKey("LIMIT"))
							limit=CMath.s_int(parms.get("LIMIT"));
						str.append(helpHelp(s,limit));
					}
				}
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
						old=R.name();
					str.append(old+", ");
				}
				if(parms.containsKey("CAT"))
				{
					String old=httpReq.getUrlParameter("CAT");
					if(old==null)
						old=R.racialCategory();
					str.append(old+", ");
				}
				if(parms.containsKey("VWEIGHT"))
				{
					String old=httpReq.getUrlParameter("VWEIGHT");
					if(old==null)
						old=""+R.weightVariance();
					str.append(old+", ");
				}
				if(parms.containsKey("BWEIGHT"))
				{
					String old=httpReq.getUrlParameter("BWEIGHT");
					if(old==null)
						old=""+R.lightestWeight();
					str.append(old+", ");
				}
				if(parms.containsKey("WEIGHT"))
				{
					str.append(""+(R.lightestWeight()+(R.weightVariance()/2))+", ");
				}
				if(parms.containsKey("HEIGHT"))
				{
					int m=(R.shortestMale()+R.shortestFemale())/2;
					m+=(R.heightVariance()/2);
					str.append(m+", ");
				}
				if(parms.containsKey("VHEIGHT"))
				{
					String old=httpReq.getUrlParameter("VHEIGHT");
					if(old==null)
						old=""+R.heightVariance();
					str.append(old+", ");
				}
				if(parms.containsKey("MHEIGHT"))
				{
					String old=httpReq.getUrlParameter("MHEIGHT");
					if(old==null)
						old=""+R.shortestMale();
					str.append(old+", ");
				}
				if(parms.containsKey("FHEIGHT"))
				{
					String old=httpReq.getUrlParameter("FHEIGHT");
					if(old==null)
						old=""+R.shortestFemale();
					str.append(old+", ");
				}
				if(parms.containsKey("XPADJ"))
				{
					String old=httpReq.getUrlParameter("XPADJ");
					if(old==null)
						old=R.getXPAdjustment()+"%";
					str.append(old+", ");
				}
				if(parms.containsKey("LEAVESTR"))
				{
					String old=httpReq.getUrlParameter("LEAVESTR");
					if(old==null)
						old=""+R.leaveStr();
					str.append(old+", ");
				}
				if(parms.containsKey("ARRIVESTR"))
				{
					String old=httpReq.getUrlParameter("ARRIVESTR");
					if(old==null)
						old=""+R.arriveStr();
					str.append(old+", ");
				}
				if(parms.containsKey("GENHELP"))
				{
					String old=httpReq.getUrlParameter("GENHELP");
					if(old==null)
					{
						R=R.makeGenRace();
						old=R.getStat("HELP");
					}
					str.append(old+", ");
				}
				if(parms.containsKey("HEALTHRACE"))
				{
					String old=httpReq.getUrlParameter("HEALTHRACE");
					if(old==null)
					{
						R=R.makeGenRace();
						old=""+R.getStat("HEALTHRACE");
					}
					str.append(raceDropDown(httpReq,old));
				}
				if(parms.containsKey("WEAPONRACE"))
				{
					String old=httpReq.getUrlParameter("WEAPONRACE");
					if(old==null)
					{
						R=R.makeGenRace();
						old=""+R.getStat("WEAPONRACE");
					}
					str.append(raceDropDown(httpReq,old));
				}
				if(parms.containsKey("EVENTRACE"))
				{
					String old=httpReq.getUrlParameter("EVENTRACE");
					if(old==null)
					{
						R=R.makeGenRace();
						old=""+R.getStat("EVENTRACE");
					}
					str.append(raceDropDown(httpReq,old));
				}
				if(parms.containsKey("BREATHES"))
				{
					int[] breathes=R.getBreathables();
					if(httpReq.isUrlParameter("BREATHES"))
					{
						int breathe=CMath.s_int(httpReq.getUrlParameter("BREATHES"));
						final List<Integer> l=new Vector<Integer>();
						if(breathe>=0)
						{
							l.add(Integer.valueOf(breathe));
							for(int i=1;;i++)
							{
								if(httpReq.isUrlParameter("BREATHES"+(Integer.toString(i))))
								{
									breathe=CMath.s_int(httpReq.getUrlParameter("BREATHES"+(Integer.toString(i))));
									if(breathe<0)
									{
										l.clear();
										break;
									}
									l.add(Integer.valueOf(breathe));
								}
								else
									break;
							}
							breathes=new int[l.size()];
							for(int i=0;i<l.size();i++)
								breathes[i]=l.get(i).intValue();
						}
					}
					str.append("<OPTION VALUE=-1 "+((breathes.length==0)?"SELECTED":"")+">Anything");
					for(final int r : RawMaterial.CODES.ALL_SBN())
					{
						str.append("<OPTION VALUE="+r);
						if(CMParms.indexOf(breathes, r)>=0)
							str.append(" SELECTED");
						str.append(">"+RawMaterial.CODES.NAME(r));
					}
				}
				if(parms.containsKey("BODY"))
				{
					str.append("<TABLE WIDTH=100% BORDER=0><TR>");
					String font=parms.get("FONT");
					if(font==null)
						font="";
					int col=-1;
					for(int i=0;i<Race.BODYPARTSTR.length;i++)
					{
						String old=httpReq.getUrlParameter("BODYPART"+i);
						if(old==null)
							old=""+R.bodyMask()[i];
						if((++col)==4)
						{
							col=0;
							str.append("</TR><TR>");
						}
						str.append("<TD WIDTH=1%>"+font+Race.BODYPARTSTR[i]+"</B></I></FONT></TD><TD><INPUT TYPE=TEXT NAME=BODYPART"+i+" VALUE=\""+old+"\" SIZE=3></TD>");

					}
					for(int i=col;i<4;i++)
						str.append("<TD></TD><TD></TD>");
					str.append("</TR></TABLE>, ");
				}
				if(parms.containsKey("WEAR"))
				{
					final Wearable.CODES codes = Wearable.CODES.instance();
					for(int b=0;b<codes.total();b++)
					{
						if(CMath.bset(R.forbiddenWornBits(),codes.get(b)))
							str.append(codes.name(b)+", ");
					}
				}
				if(parms.containsKey("IMMUNITIES"))
				{
					for(String ableID : R.abilityImmunities())
					{
						final Ability A=CMClass.getAbilityPrototype(ableID);
						if(A!=null)
							str.append(A.name()+", ");
					}
				}
				if(parms.containsKey("RABLE"))
				{
					final MOB mob=Authenticate.getAuthenticatedMob(httpReq);
					str.append(dynAbilities(mob,R.racialAbilities(null),R.ID(),R,httpReq,parms,0,parms.get("FONT"))+", ");
				}
				if(parms.containsKey("REFFS"))
					str.append(dynEffects(R.ID(),R,httpReq,parms,0,parms.get("FONT"))+", ");
				if(parms.containsKey("IABLE"))
					str.append(dynImmunities(R.ID(),R,httpReq,parms,0,parms.get("FONT"))+", ");
				if(parms.containsKey("CABLE"))
					str.append(cabilities(R,httpReq,parms,0,parms.get("FONT"))+", ");
				if(parms.containsKey("WEARID"))
				{
					final String old=httpReq.getUrlParameter("WEARID");
					long mask=0;
					if(old==null)
						mask=R.forbiddenWornBits();
					else
					{
						mask|=CMath.s_long(old);
						for(int i=1;;i++)
							if(httpReq.isUrlParameter("WEARID"+(Integer.toString(i))))
								mask|=CMath.s_long(httpReq.getUrlParameter("WEARID"+(Integer.toString(i))));
							else
								break;
					}
					final Wearable.CODES codes = Wearable.CODES.instance();
					for(int i=1;i<codes.total();i++)
					{
						str.append("<OPTION VALUE="+codes.get(i)+" ");
						if(CMath.bset(mask,codes.get(i)))
							str.append("SELECTED");
						str.append(">"+codes.name(i));
					}
					str.append(", ");
				}
				if(parms.containsKey("PLAYABLEID"))
				{
					final String old=httpReq.getUrlParameter("PLAYABLEID");
					long mask=0;
					if(old==null)
						mask=R.availabilityCode();
					else
						mask|=CMath.s_long(old);
					for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
						str.append("<OPTION VALUE="+i+" "+((i==mask)?"SELECTED":"")+">"+Area.THEME_PHRASE_EXT[i]);
					str.append(", ");
				}

				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_PHRASE_EXT[R.availabilityCode()]+", ");
				if(parms.containsKey("NATURALWEAPON"))
					str.append(R.myNaturalWeapon().name()+", ");

				if(parms.containsKey("STATS"))
					str.append(R.getStatAdjDesc()+", ");

				if(parms.containsKey("ESTATS")||parms.containsKey("CSTATS")||parms.containsKey("ASTATS")||parms.containsKey("ASTATE")||parms.containsKey("STARTASTATE"))
				{
					R=R.makeGenRace();

					if(parms.containsKey("ESTATS"))
					{
						final String eStats=R.getStat("ESTATS");
						final PhyStats adjPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats"); adjPStats.setAllValues(0);
						if(eStats.length()>0)
						{
							CMLib.coffeeMaker().setPhyStats(adjPStats,eStats);
						}
						str.append(estats(adjPStats,'E',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("CSTATS"))
					{
						final CharStats setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0);
						final String cStats=R.getStat("CSTATS");
						if(cStats.length()>0)
						{
							CMLib.coffeeMaker().setCharStats(setStats,cStats);
						}
						str.append(cstats(setStats,'S',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("ASTATS"))
					{
						final CharStats adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0);
						final String cStats=R.getStat("ASTATS");
						if(cStats.length()>0)
						{
							CMLib.coffeeMaker().setCharStats(adjStats,cStats);
						}
						str.append(cstats(adjStats,'A',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("ASTATE"))
					{
						final CharState adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0);
						final String aState=R.getStat("ASTATE");
						if(aState.length()>0)
						{
							CMLib.coffeeMaker().setCharState(adjState,aState);
						}
						str.append(cstate(adjState,'A',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("STARTASTATE"))
					{
						final CharState startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0);
						final String saState=R.getStat("STARTASTATE");
						if(saState.length()>0)
						{
							CMLib.coffeeMaker().setCharState(startAdjState,saState);
						}
						str.append(cstate(startAdjState,'S',httpReq,parms,0)+", ");
					}
				}

				if(parms.containsKey("OUTFIT"))
					str.append(itemList(R.outfit(null),'O',httpReq,parms,0,false)+", ");
				if(parms.containsKey("WEAPON"))
				{
					final Vector<Item> V=new XVector<Item>(R.myNaturalWeapon());
					str.append(itemList(V,'W',httpReq,parms,0,true)+", ");
				}
				if(parms.containsKey("RESOURCES"))
					str.append(itemList(R.myResources(),'R',httpReq,parms,0,false)+", ");
				if(parms.containsKey("BODYKILL"))
				{
					final String old=httpReq.getUrlParameter("BODYKILL");
					boolean bodyKill=false;
					if(old==null)
						bodyKill=CMath.s_bool(R.makeGenRace().getStat("BODYKILL"));
					else
						bodyKill=old.equalsIgnoreCase("on");
					if(bodyKill)
						str.append(" CHECKED , ");
				}
				if(parms.containsKey("CANRIDE"))
				{
					final String old=httpReq.getUrlParameter("CANRIDE");
					boolean canRide=false;
					if(old==null)
						canRide=R.useRideClass();
					else
						canRide=old.equalsIgnoreCase("on");
					if(canRide)
						str.append(" CHECKED , ");
				}
				if(parms.containsKey("DISFLAGS"))
				{
					if(!httpReq.isUrlParameter("DISFLAGS"))
					{
						R=R.makeGenRace();
						httpReq.addFakeUrlParameter("DISFLAGS",R.getStat("DISFLAGS"));
					}
					final int flags=CMath.s_int(httpReq.getUrlParameter("DISFLAGS"));
					for(int i=0;i<Race.GENFLAG_DESCS.length;i++)
					{
						str.append("<OPTION VALUE="+CMath.pow(2,i));
						if(CMath.bset(flags,CMath.pow(2,i)))
							str.append(" SELECTED");
						str.append(">"+Race.GENFLAG_DESCS[i]);
					}
				}
				if(parms.containsKey("AGING"))
				{
					final int[] ageChart=R.getAgingChart();
					if(!httpReq.isUrlParameter("AGE0"))
					{
						for(int i=0;i<Race.AGE_DESCS.length;i++)
							httpReq.addFakeUrlParameter("AGE"+i,""+ageChart[i]);
					}
					int val=-1;
					for(int i=0;i<Race.AGE_DESCS.length;i++)
					{
						final int lastVal=val;
						val=CMath.s_int(httpReq.getUrlParameter("AGE"+i));
						if(val<lastVal)
						{
							val=lastVal;
							httpReq.addFakeUrlParameter("AGE"+i,""+val);
						}
						str.append("<INPUT TYPE=TEXT SIZE=4 NAME=AGE"+i+" VALUE="+val+">"+Race.AGE_DESCS[i]+"<BR>");
					}
					str.append(", ");
				}

				if(parms.containsKey("SENSES"))
				{
					if(R.getSensesChgDesc().length()>0)
						str.append(R.getSensesChgDesc()+", ");
				}
				if(parms.containsKey("DISPOSITIONS"))
				{
					if(R.getDispositionChgDesc().length()>0)
						str.append(R.getDispositionChgDesc()+", ");
				}
				if(parms.containsKey("TRAINS"))
				{
					if(R.getTrainAdjDesc().length()>0)
						str.append(R.getTrainAdjDesc()+", ");
				}
				if(parms.containsKey("EXPECTANCY"))
					str.append(""+R.getAgingChart()[Race.AGE_ANCIENT]+", ");
				if(parms.containsKey("PRACS"))
				{
					if(R.getPracAdjDesc().length()>0)
						str.append(R.getPracAdjDesc()+", ");
				}
				if(parms.containsKey("ABILITIES"))
				{
					if(R.getAbilitiesDesc().length()>0)
						str.append(R.getAbilitiesDesc()+", ");
				}
				if(parms.containsKey("EFFECTS"))
				{
					for(final Ability A : R.racialEffects(null))
					{
						if(A!=null)
							str.append(A.Name()+", ");
					}
				}
				if(parms.containsKey("LANGS"))
				{
					if(R.getLanguagesDesc().length()>0)
						str.append(R.getLanguagesDesc()+", ");
				}

				if(parms.containsKey("STARTINGEQ"))
				{
					if(R.outfit(null)!=null)
					{
						for(final Item I : R.outfit(null))
						{
							if(I!=null)
								str.append(I.Name()+", ");
						}
					}
				}
				if(parms.containsKey("CLASSES"))
				{
					for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						final CharClass C=(CharClass)c.nextElement();
						if((C!=null)
						&&(CMProps.isTheme(C.availabilityCode()))
						&&(!CMath.bset(C.availabilityCode(), Area.THEME_SKILLONLYMASK))
						&&(C.isAllowedRace(R)))
							str.append(C.name()+", ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				httpReq.getRequestObjects().put("RACE-"+last,R);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
