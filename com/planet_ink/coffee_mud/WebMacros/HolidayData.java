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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class HolidayData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "HolidayData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("HOLIDAY");
		if(last==null)
			return " @break@";
		boolean exists = false;
		if(last.length()>0)
		{
			final int index=CMLib.quests().getHolidayIndex(last);
			exists = index>=0;
			QuestManager.HolidayData encodedData=(QuestManager.HolidayData)httpReq.getRequestObjects().get("HOLIDAY_"+last.toUpperCase().trim());
			if(encodedData==null)
			{
				List<String> steps=null;
				if(index>=0)
				{
					final Object resp=CMLib.quests().getHolidayFile();
					if(resp instanceof List)
						steps=(List<String>)resp;
					if(steps!=null)
						encodedData=CMLib.quests().getEncodedHolidayData(steps.get(index));
				}
				else
				{
					final StringBuffer data=CMLib.quests().getDefaultHoliData(last, "ALL");
					encodedData=CMLib.quests().getEncodedHolidayData(data.toString());
				}
				if(encodedData != null)
					httpReq.getRequestObjects().put("HOLIDAY_"+last.toUpperCase().trim(), encodedData);
			}
			if(encodedData!=null)
			{
				final TriadList<String,String,Integer> settings=encodedData.settings();
				final TriadList<String,String,Integer> behaviors=encodedData.behaviors();
				final TriadList<String,String,Integer> properties=encodedData.properties();
				final TriadList<String,String,Integer> stats=encodedData.stats();
				//List stepV=(List)encodedData.elementAt(4);
				//int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();

				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("EXISTS"))
				{
					return ""+exists;
				}
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("NAME");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="New Holiday";
					}
					str.append(old+", ");
				}
				if(parms.containsKey("DURATION"))
				{
					String old=httpReq.getUrlParameter("DURATION");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("DURATION");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="900";
					}
					str.append(old+", ");
				}
				if(parms.containsKey("AREAS"))
				{
					final int dex=settings.indexOfFirst("AREAGROUP");
					String old=null;
					if(dex>=0)
						old=settings.get(dex).second;
					if((old==null)||(old.length()==0))
						old="ALL";
					str.append(old+", ");
				}
				if(parms.containsKey("AREAGROUP"))
				{
					// any, all, "name" "name" "name" "name"
					String old=httpReq.getUrlParameter("AREAGROUP");
					Vector<String> areaNames=null;
					if(old==null)
					{
						final int dex=settings.indexOfFirst("AREAGROUP");
						if(dex>=0)
							old=settings.get(dex).second;
						if((old==null)||(old.length()==0))
							old="ALL";
						areaNames = CMParms.parse(old.toUpperCase().trim());
					}
					else
					{
						final HashSet<String> areaCodes=new HashSet<String>();
						String id="";
						for(int i=0;httpReq.isUrlParameter("AREAGROUP"+id);id=Integer.toString(++i))
							areaCodes.add(httpReq.getUrlParameter("AREAGROUP"+id));
						areaNames=new Vector<String>();
						if(areaCodes.contains("AREAGROUP0"))
							areaNames.add("ALL");
						else
						if(areaCodes.contains("AREAGROUP1"))
							areaNames.add("ANY");
						else
						{
							int areaNum=2;
							boolean reallyAll=true;

							for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();areaNum++)
							{
								if(areaCodes.contains("AREAGROUP"+areaNum))
									areaNames.add(((Area)e.nextElement()).Name().toUpperCase());
								else
								{
									e.nextElement();
									reallyAll=false;
								}
							}
							if(reallyAll)
							{
								areaCodes.clear();
								areaCodes.add("ALL");
							}
						}
					}
					str.append("<OPTION VALUE=\"AREAGROUP0\" "+(areaNames.contains("ALL")?"SELECTED":"")+">All");
					str.append("<OPTION VALUE=\"AREAGROUP1\" "+(areaNames.contains("ANY")?"SELECTED":"")+">Any (Random)");
					int areaNum=2;
					for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();areaNum++)
					{
						final Area A=(Area)e.nextElement();
						str.append("<OPTION VALUE=\"AREAGROUP"+areaNum+"\" "+(areaNames.contains(A.Name().toUpperCase())?"SELECTED":"")+">"+A.Name());
					}
				}
				if(parms.containsKey("MOBGROUP"))
				{
					// zappermask only
					String old=httpReq.getUrlParameter("MOBGROUP");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("MOBGROUP");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="";
					}
					str.append(CMStrings.replaceAll(old,"\"","&quot;")+", ");
				}
				if(parms.containsKey("MOOD"))
				{
					String old=httpReq.getUrlParameter("MOOD");
					if(old==null)
					{
						final int dex=properties.indexOfFirst("MOOD");
						if(dex>=0)
							old=properties.get(dex).second;
						else
							old="";
					}
					/*else
					if(old.length()>0)
					{
						Vector<String> V=getMoodList();
						if(!V.contains(old.toUpperCase().trim()))
							old="";
					}*/
					final Vector<String> V=getMoodList();
					str.append("<OPTION VALUE=\"\" "+((old.trim().length()==0)?" SELECTED":"")+">None");
					for(int v=0;v<V.size();v++)
					{
						final String s=V.elementAt(v);
						str.append("<OPTION VALUE=\""+s+"\" "+((old.trim().equalsIgnoreCase(s))?" SELECTED":"")+">"+s);
					}
					str.append(old+", ");
				}
				if(parms.containsKey("AGGRESSIVE"))
				{
					String old=httpReq.getUrlParameter("AGGRESSIVE");
					if(old==null)
					{
						final int dex=behaviors.indexOfFirst("AGGRESSIVE");
						if(dex>=0)
							old=behaviors.get(dex).second;
						else
							old="";
					}
					str.append(old+", ");
				}
				if(parms.containsKey("SCHEDULETYPE")||parms.containsKey("SCHEDULETYPEID"))
				{
					String old=httpReq.getUrlParameter("SCHEDULETYPE");
					if(old==null)
						old=httpReq.getUrlParameter("SCHEDULETYPEID");
					final String[] TYPES={"RANDOM INTERVAL","MUD-DAY","RL-DAY"};
					if(old==null)
					{
						final int mudDayIndex=settings.indexOfFirst("MUDDAY");
						final int dateIndex=settings.indexOfFirst("DATE");
						if(mudDayIndex>=0)
							old=TYPES[1];
						else
						if(dateIndex>=0)
							old=TYPES[2];
						else
							old=TYPES[0];
						old=""+CMParms.indexOf(TYPES,old);
					}
					if(parms.containsKey("SCHEDULETYPEID"))
					for(int i=0;i<TYPES.length;i++)
						str.append("<OPTION VALUE="+i+" "+(old.equalsIgnoreCase(""+i)?"SELECTED":"")+">"+TYPES[i]);
					else
						str.append(old);
					httpReq.addFakeUrlParameter("SCHEDULETYPE",old);
					str.append(", ");
				}
				if(parms.containsKey("SCHEDULE"))
				{
					final String[] TYPES={"RANDOM INTERVAL","MUD-DAY","RL-DAY"};
					String old;
					final int mudDayIndex=settings.indexOfFirst("MUDDAY");
					final int dateIndex=settings.indexOfFirst("DATE");
					if(mudDayIndex>=0)
					{
						final int dex=settings.indexOfFirst("MUDDAY");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="1-1";
						old=TYPES[1]+": "+old;
						str.append(old);
					}
					else
					if(dateIndex>=0)
					{
						final int dex=settings.indexOfFirst("DATE");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="1-1";
						old=TYPES[2]+": "+old;
						str.append(old);
					}
					else
					{
						old=TYPES[0];
						final int dex=settings.indexOfFirst("WAIT");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="100";
						str.append(TYPES[0]+": "+old+" ticks");
					}
					str.append(", ");
				}
				if(parms.containsKey("MUDDAY"))
				{
					String old=httpReq.getUrlParameter("MUDDAY");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("MUDDAY");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="1-1";
					}
					str.append(old+", ");
				}
				if(parms.containsKey("DATE"))
				{
					String old=httpReq.getUrlParameter("DATE");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("DATE");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="1-1";
					}
					str.append(old+", ");
				}
				if(parms.containsKey("WAIT"))
				{
					String old=httpReq.getUrlParameter("WAIT");
					if(old==null)
					{
						final int dex=settings.indexOfFirst("WAIT");
						if(dex>=0)
							old=settings.get(dex).second;
						else
							old="100";
					}
					str.append(old+", ");
				}
				str.append(HolidayData.behaviors(behaviors,httpReq,parms,1));
				str.append(HolidayData.properties(properties,httpReq,parms,1));
				str.append(HolidayData.priceFactors(stats, httpReq, parms, 1));
				str.append(HolidayData.mudChat(behaviors,httpReq,parms,1));
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}

	public static StringBuffer behaviors(TriadList<String,String,Integer> behaviors, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("BEHAVIORS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<String> theparms=new Vector<String>();
			if(httpReq.isUrlParameter("BEHAV1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("BEHAV"+num);
				String theparm=httpReq.getUrlParameter("BDATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getUrlParameter("BEHAV"+num);
					theparm=httpReq.getUrlParameter("BDATA"+num);
				}
			}
			else
			for(int b=0;b<behaviors.size();b++)
			{
				final Behavior B=CMClass.getBehavior(behaviors.get(b).first);
				if((B!=null)
				&&(!B.ID().equalsIgnoreCase("MUDCHAT"))
				&&(!B.ID().equalsIgnoreCase("AGGRESSIVE")))
				{
					theclasses.addElement(CMClass.classID(B));
					String t=behaviors.get(b).second;
					t=CMStrings.replaceAll(t,"\"","&quot;");
					theparms.addElement(t);
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final String theparm=theparms.elementAt(i);
				str.append("<TR><TD WIDTH=30%>");
				str.append("<SELECT ONCHANGE=\"EditBehavior(this);\" NAME=BEHAV"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=70%>");
				str.append("<INPUT TYPE=TEXT SIZE=40 NAME=BDATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=30%>");
			str.append("<SELECT ONCHANGE=\"AddBehavior(this);\" NAME=BEHAV"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Behavior");

			Object[] sortedB=null;
			final Vector<String> sortMeB=new Vector<String>();
			for(final Enumeration b=CMClass.behaviors();b.hasMoreElements();)
			{
				final Behavior B=(Behavior)b.nextElement();
				sortMeB.addElement(CMClass.classID(B));
			}
			sortedB=(new TreeSet<String>(sortMeB)).toArray();
			for (final Object element : sortedB)
			{
				final String cnam=(String)element;
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=70%>");
			str.append("<INPUT TYPE=TEXT SIZE=40 NAME=BDATA"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer properties(TriadList<String,String,Integer> properties, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("AFFECTS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<String> theparms=new Vector<String>();
			if(httpReq.isUrlParameter("AFFECT1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("AFFECT"+num);
				String theparm=httpReq.getUrlParameter("ADATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getUrlParameter("AFFECT"+num);
					theparm=httpReq.getUrlParameter("ADATA"+num);
				}
			}
			else
			for(int b=0;b<properties.size();b++)
			{
				final Ability A=CMClass.getAbility(properties.get(b).first);
				if((A!=null)&&(!A.ID().equalsIgnoreCase("MOOD")))
				{
					theclasses.addElement(CMClass.classID(A));
					String t=properties.get(b).second;
					t=CMStrings.replaceAll(t,"\"","&quot;");
					theparms.addElement(t);
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final String theparm=theparms.elementAt(i);
				str.append("<TR><TD WIDTH=30%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=AFFECT"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=70%>");
				str.append("<INPUT TYPE=TEXT SIZE=40 NAME=ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=30%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECT"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Effect");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				final String cnam=A.ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=70%>");
			str.append("<INPUT TYPE=TEXT SIZE=40 NAME=ADATA"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer priceFactors(TriadList<String,String,Integer> stats, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("PRICEFACTORS"))
		{
			final Vector<String> theclasses=new Vector<String>();
			final Vector<String> theparms=new Vector<String>();
			if(httpReq.isUrlParameter("PRCFAC1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("PRCFAC"+num);
				String theparm=httpReq.getUrlParameter("PMASK"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(!behav.trim().endsWith("%"))
						behav=behav.trim()+"%";
					if((behav.length()>0)&&(CMath.isPct(behav)))
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getUrlParameter("PRCFAC"+num);
					theparm=httpReq.getUrlParameter("PMASK"+num);
				}
			}
			else
			{
				final int pndex=stats.indexOf("PRICEMASKS");
				final String priceStr=(pndex<0)?"":(String)stats.get(pndex).second;
				final List<String> priceV=CMParms.parseCommas(priceStr,true);
				for(int v=0;v<priceV.size();v++)
				{
					final String priceLine=priceV.get(v);
					double priceFactor=0.0;
					String mask="";
					final int x=priceLine.indexOf(' ');
					if(x<0)
						priceFactor=CMath.s_double(priceLine);
					else
					{
						priceFactor=CMath.s_double(priceLine.substring(0,x));
						mask=priceLine.substring(x+1).trim();
					}
					theclasses.addElement((priceFactor*100.0)+"%");
					mask=CMStrings.replaceAll(mask,"\"","&quot;");
					theparms.addElement(mask);
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
			final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
			if(parms.containsKey("HEADERCOL1")||parms.containsKey("HEADERCOL2"))
			{
				str.append("<TR><TD WIDTH=25%>");
				if(parms.containsKey("HEADERCOL1"))
					str.append(sfont + (parms.get("HEADERCOL1")) + efont);
				str.append("</TD><TD WIDTH=75%>");
				if(parms.containsKey("HEADERCOL2"))
					str.append(sfont + (parms.get("HEADERCOL2")) + efont);
				str.append("</TD></TR>");
			}
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.elementAt(i);
				final String theparm=theparms.elementAt(i);
				str.append("<TR><TD WIDTH=25%>");
				str.append("<INPUT TYPE=TEXT SIZE=5 NAME=PRCFAC"+(i+1)+" VALUE=\""+theclass+"\">");
				str.append("</TD><TD WIDTH=75%>");
				str.append("<INPUT TYPE=TEXT SIZE=50 NAME=PMASK"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=25%>");
			str.append("<INPUT TYPE=TEXT SIZE=5 NAME=PRCFAC"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD><TD WIDTH=50%>");
			str.append("<INPUT TYPE=TEXT SIZE=50 NAME=PMASK"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer mudChat(TriadList<String,String,Integer> behaviors, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("MUDCHAT"))
		{
			List<List<String>> mudchats=new Vector<List<String>>();
			if(httpReq.isUrlParameter("MCWDS1"))
			{
				int wdsnum=1;
				String wordsList=httpReq.getUrlParameter("MCWDS"+wdsnum);
				String weight=httpReq.getUrlParameter("MCSAYW"+wdsnum+"_1");
				String say=httpReq.getUrlParameter("MCSAYS"+wdsnum+"_1");
				while((wordsList!=null)&&(weight!=null)&&(say!=null))
				{
					final List<String> mudchat=new Vector<String>();
					if(wordsList.length()>0)
					{
						mudchats.add(mudchat);
						mudchat.add(CMStrings.replaceAll(wordsList,",","|"));
						int saynum=1;
						while((weight!=null)&&(say!=null))
						{
							if(CMath.isInteger(weight))
							{
								if(say.trim().length()==0)
									say="What should I say about those words?";
								mudchat.add(weight+say);
							}
							saynum++;
							say=httpReq.getUrlParameter("MCSAYS"+wdsnum+"_"+saynum);
							weight=httpReq.getUrlParameter("MCSAYW"+wdsnum+"_"+saynum);
						}
					}
					wdsnum++;
					wordsList=httpReq.getUrlParameter("MCWDS"+wdsnum);
					say=httpReq.getUrlParameter("MCSAYS"+wdsnum+"_1");
					weight=httpReq.getUrlParameter("MCSAYW"+wdsnum+"_1");
				}
			}
			else
				mudchats=CMLib.quests().breakOutMudChatVs("MUDCHAT",behaviors);

			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
			final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
			if(parms.containsKey("HEADERCOL1"))
			{
				str.append("<TR><TD WIDTH=25% VALIGN=TOP>"+sfont+(parms.get("HEADERCOL1"))+efont+"</TD>");
				if(parms.containsKey("HEADERCOL2"))
				{
					str.append("<TD WIDTH=75%>");
					str.append("<TABLE WIDTH=100% BORDER=0 CELLSPACING=0 CELLPADDING=0>");
					str.append("<TR><TD WIDTH=20% VALIGN=TOP>"+sfont+(parms.get("HEADERCOL2"))+efont+"</TD>");
					if(parms.containsKey("HEADERCOL3"))
						str.append("<TD WIDTH=80%>"+sfont+(parms.get("HEADERCOL3"))+efont+"</TD>");
					else
						str.append("<TD WIDTH=80%></TD>");
					str.append("</TR></TABLE>");
					str.append("</TD>");
				}
				else
					str.append("<TD WIDTH=75%></TD>");
				str.append("</TR>");
			}

			for(int i=0;i<mudchats.size();i++)
			{
				final List<String> mudChat=mudchats.get(i);
				final String sayList=CMStrings.replaceAll(CMStrings.replaceAll(mudChat.get(0),"\"","&quot;"),"|",",");
				str.append("<TR><TD WIDTH=25% VALIGN=TOP>");
				str.append("<INPUT TYPE=TEXT SIZE=15 NAME=MCWDS"+(i+1)+" VALUE=\""+sayList+"\">");
				str.append("</TD><TD WIDTH=75%>");
				str.append("<TABLE WIDTH=100% BORDER=0 CELLSPACING=0 CELLPADDING=0>");
				for(int ii=1;ii<mudChat.size();ii++)
				{
					str.append("<TR><TD WIDTH=20%>");
					String say=mudChat.get(ii);
					final int weight=CMath.s_int(""+say.charAt(0));
					say=CMStrings.replaceAll(say.substring(1),"\"","&quot;");
					str.append("<SELECT NAME=MCSAYW"+(i+1)+"_"+(ii)+" ONCHANGE=\"NoSay(this)\">");
					str.append("<OPTION VALUE=\"\">del");
					for(int i3=0;i3<=9;i3++)
						str.append("<OPTION VALUE="+i3+((i3==weight)?" SELECTED":"")+">"+i3);
					str.append("</SELECT>");
					str.append("</TD><TD WIDTH=80%>");
					str.append("<INPUT TYPE=TEXT SIZE=40 NAME=MCSAYS"+(i+1)+"_"+(ii)+" VALUE=\""+say+"\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=20%>");
				str.append("<SELECT NAME=MCSAYW"+(i+1)+"_"+(mudChat.size())+" ONCHANGE=\"NewSay(this)\">");
				str.append("<OPTION VALUE=\"\">");
				for(int i3=0;i3<=9;i3++)
					str.append("<OPTION VALUE="+i3+">"+i3);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=80%>");
				str.append("<INPUT TYPE=TEXT SIZE=40 NAME=MCSAYS"+(i+1)+"_"+(mudChat.size())+" VALUE=\"\">");
				str.append("</TD></TR>");
				str.append("</TABLE>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD VALIGN=TOP>");
			str.append("<INPUT TYPE=TEXT SIZE=15 NAME=MCWDS"+(mudchats.size()+1)+" VALUE=\"\">");
			str.append("</TD><TD>");
			str.append("<TABLE WIDTH=100% BORDER=0 CELLSPACING=0 CELLPADDING=0>");
			str.append("<TR><TD WIDTH=20%>");
			str.append("<SELECT NAME=MCSAYW"+(mudchats.size()+1)+"_1 ONCHANGE=\"NewSay(this)\">");
			str.append("<OPTION VALUE=\"\">");
			for(int i3=0;i3<=9;i3++)
				str.append("<OPTION VALUE="+i3+">"+i3);
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=80%>");
			str.append("<INPUT TYPE=TEXT SIZE=40 NAME=MCSAYS"+(mudchats.size()+1)+"_1 VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	protected Vector<String> getMoodList()
	{
		final Vector<String> V=new Vector<String>();
		final Ability A=CMClass.getAbility("Mood");
		if(A==null)
			return V;
		int x=0;
		A.setMiscText(""+x);
		while((A.text().length()>0)&&(!V.contains(A.text())))
		{
			V.addElement(A.text().toUpperCase().trim());
			x++;
			A.setMiscText(""+x);
		}
		return V;
	}
}
