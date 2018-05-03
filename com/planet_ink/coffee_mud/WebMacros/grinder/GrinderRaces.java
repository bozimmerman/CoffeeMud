package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
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

import java.util.*;

/*
   Copyright 2008-2018 Bo Zimmerman

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
public class GrinderRaces
{
	public String name()
	{
		return "GrinderRaces";
	}

	public static String getPStats(char c, HTTPRequest httpReq)
	{
		boolean changes = false;
		final PhyStats adjPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		adjPStats.setAllValues(0);
		if(httpReq.isUrlParameter(c+"ESTATS1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"ESTATS"+num);
			while(behav!=null)
			{
				if((behav.length()>0) && (new XVector<String>(adjPStats.getStatCodes()).contains(behav.toUpperCase().trim())))
				{
					String prof=httpReq.getUrlParameter(c+"ESTATSV"+num);
					if(prof==null)
						prof="0";
					if(CMath.s_int(prof)!=0)
					{
						adjPStats.setStat(behav.toUpperCase().trim(), prof);
						changes = true;
					}
				}
				num++;
				behav=httpReq.getUrlParameter(c+"ESTATS"+num);
			}
		}
		if(!changes)
			return "";
		return CMLib.coffeeMaker().getPhyStatsStr(adjPStats);
	}

	public static String getCStats(char c, HTTPRequest httpReq)
	{
		boolean changes = false;
		final CharStats adjCStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		adjCStats.setAllValues(0);
		if(httpReq.isUrlParameter(c+"CSTATS1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"CSTATS"+num);
			while(behav!=null)
			{
				if((behav.length()>0) && (new XVector<String>(CharStats.CODES.NAMES()).contains(behav.toUpperCase().trim())))
				{
					final int val=CMath.s_int(httpReq.getUrlParameter(c+"CSTATSV"+num));
					if(val!=0)
					{
						adjCStats.setStat(CMParms.indexOf(CharStats.CODES.NAMES(),behav.toUpperCase().trim()), val);
						changes = true;
					}
				}
				num++;
				behav=httpReq.getUrlParameter(c+"CSTATS"+num);
			}
		}
		if(!changes)
			return "";
		return CMLib.coffeeMaker().getCharStatsStr(adjCStats);
	}

	public static String getCState(char c, HTTPRequest httpReq)
	{
		boolean changes = false;
		final CharState adjCState=(CharState)CMClass.getCommon("DefaultCharState");
		adjCState.setAllValues(0);
		if(httpReq.isUrlParameter(c+"CSTATE1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter(c+"CSTATE"+num);
			while(behav!=null)
			{
				if((behav.length()>0) && (new XVector<String>(adjCState.getStatCodes()).contains(behav.toUpperCase().trim())))
				{
					String prof=httpReq.getUrlParameter(c+"CSTATEV"+num);
					if(prof==null)
						prof="0";
					if(CMath.s_int(prof)!=0)
					{
						adjCState.setStat(behav.toUpperCase().trim(), prof);
						changes = true;
					}
				}
				num++;
				behav=httpReq.getUrlParameter(c+"CSTATE"+num);
			}
		}
		if(!changes)
			return "";
		return CMLib.coffeeMaker().getCharStateStr(adjCState);
	}

	public static List<Item> itemList(List<? extends Item> items, char c, HTTPRequest httpReq, boolean one)
	{
		if(items==null)
			items=new Vector<Item>();
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
		return classes;
	}

	public static void setDynAbilities(Modifiable M, HTTPRequest httpReq)
	{
		final boolean supportsRoles=CMParms.contains(M.getStatCodes(), "GETRABLEROLE");
		final DVector theclasses=new DVector(6);
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
					String levl=httpReq.getUrlParameter("RABLVL"+num);
					if(levl==null)
						levl="0";
					String parm=httpReq.getUrlParameter("RABPRM"+num);
					if(parm==null)
						parm="";
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
		M.setStat("NUMRABLE", ""+theclasses.size());
		for(int i=0;i<theclasses.size();i++)
		{
			M.setStat("GETRABLE"+i, (String)theclasses.get(i,1));
			M.setStat("GETRABLEPROF"+i, (String)theclasses.get(i,2));
			M.setStat("GETRABLEQUAL"+i, ((String)theclasses.get(i,3)).equalsIgnoreCase("on")?"true":"false");
			M.setStat("GETRABLELVL"+i, (String)theclasses.get(i,4));
			M.setStat("GETRABLEPARM"+i, (String)theclasses.get(i,5));
			if(supportsRoles)
				M.setStat("GETRABLEROLE"+i, (String)theclasses.get(i,6));
		}
	}

	public static void setDynEffects(Modifiable M, HTTPRequest httpReq)
	{
		final QuadVector<String,String,String,String> theclasses=new QuadVector<String,String,String,String>();
		boolean supportsRoles=CMParms.contains(M.getStatCodes(), "GETREFFROLE");
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
		M.setStat("NUMREFF", ""+theclasses.size());
		for(int i=0;i<theclasses.size();i++)
		{
			M.setStat("GETREFF"+i, theclasses.elementAt(i).first);
			M.setStat("GETREFFLVL"+i, theclasses.elementAt(i).third);
			M.setStat("GETREFFPARM"+i, theclasses.elementAt(i).second);
			if(supportsRoles)
				M.setStat("GETREFFROLE"+i, theclasses.elementAt(i).fourth);
		}
	}

	public static void setDynImmunities(Modifiable M, HTTPRequest httpReq)
	{
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
		M.setStat("NUMIABLE", ""+theclasses.size());
		for(int i=0;i<theclasses.size();i++)
			M.setStat("GETIABLE"+i, theclasses.get(i));
	}

	public static QuadVector<String,Integer,Integer,Boolean> cabilities(HTTPRequest httpReq)
	{
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
		return theclasses;
	}

	public static String modifyRace(HTTPRequest httpReq, java.util.Map<String,String> parms, Race oldR, Race R)
	{
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
		String old;

		old=httpReq.getUrlParameter("NAME");
		R.setStat("NAME",(old==null)?"NAME":old);
		old=httpReq.getUrlParameter("CAT");
		R.setStat("CAT",(old==null)?"CAT":old);
		old=httpReq.getUrlParameter("XPADJ");
		R.setStat("XPADJ",""+(int)Math.round(100.0*CMath.s_pct((old==null)?"0":old)));
		old=httpReq.getUrlParameter("VWEIGHT");
		R.setStat("VWEIGHT",(old==null)?"VWEIGHT":old);
		old=httpReq.getUrlParameter("BWEIGHT");
		R.setStat("BWEIGHT",(old==null)?"BWEIGHT":old);
		old=httpReq.getUrlParameter("VHEIGHT");
		R.setStat("VHEIGHT",(old==null)?"VHEIGHT":old);
		old=httpReq.getUrlParameter("MHEIGHT");
		R.setStat("MHEIGHT",(old==null)?"MHEIGHT":old);
		old=httpReq.getUrlParameter("FHEIGHT");
		R.setStat("FHEIGHT",(old==null)?"FHEIGHT":old);
		old=httpReq.getUrlParameter("LEAVESTR");
		R.setStat("LEAVE",(old==null)?"LEAVESTR":old);
		old=httpReq.getUrlParameter("ARRIVESTR");
		R.setStat("ARRIVE",(old==null)?"ARRIVESTR":old);
		old=httpReq.getUrlParameter("HEALTHRACE");
		R.setStat("HEALTHRACE",(old==null)?"HEALTHRACE":old);
		old=httpReq.getUrlParameter("WEAPONRACE");
		R.setStat("WEAPONRACE",(old==null)?"WEAPONRACE":old);
		old=httpReq.getUrlParameter("EVENTRACE");
		R.setStat("EVENTRACE",(old==null)?"EVENTRACE":old);
		old=httpReq.getUrlParameter("CANRIDE");
		R.setStat("CANRIDE",(old==null)?"false":(""+old.equalsIgnoreCase("on")));
		old=httpReq.getUrlParameter("GENHELP");
		R.setStat("HELP", ((old==null)?"":old));
		final StringBuffer bodyOld=new StringBuffer("");
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
		{
			old=httpReq.getUrlParameter("BODYPART"+i);
			bodyOld.append((old==null)?"":old).append(";");
		}
		R.setStat("BODY",bodyOld.toString());
		old=httpReq.getUrlParameter("WEARID");
		long mask=0;
		if(old!=null)
		{
			mask|=CMath.s_long(old);
			for(int i=1;;i++)
			{
				if(httpReq.isUrlParameter("WEARID"+(Integer.toString(i))))
					mask|=CMath.s_long(httpReq.getUrlParameter("WEARID"+(Integer.toString(i))));
				else
					break;
			}
		}
		R.setStat("WEAR",""+mask);
		R.setStat("AVAIL",""+CMath.s_long(httpReq.getUrlParameter("PLAYABLEID")));
		String bodyKill=httpReq.getUrlParameter("BODYKILL");
		if(bodyKill != null)
			R.setStat("BODYKILL",""+bodyKill.equalsIgnoreCase("on"));
		R.setStat("DISFLAGS",""+CMath.s_long(httpReq.getUrlParameter("DISFLAGS")));
		R.setStat("ESTATS",getPStats('E',httpReq));
		R.setStat("CSTATS",getCStats('S',httpReq));
		R.setStat("ASTATS",getCStats('A',httpReq));
		R.setStat("ASTATE",getCState('A',httpReq));
		R.setStat("STARTASTATE",getCState('S',httpReq));
		final StringBuffer commaList = new StringBuffer("");
		int val=0;
		for(int i=0;i<Race.AGE_DESCS.length;i++)
		{
			final int lastVal=val;
			val=CMath.s_int(httpReq.getUrlParameter("AGE"+i));
			if(val<lastVal)
				val=lastVal;
			if(i>0)
				commaList.append(",");
			commaList.append(val);
		}
		R.setStat("AGING",commaList.toString());
		List<Item> V=itemList(oldR.myResources(),'R',httpReq,false);
		R.setStat("NUMRSC",""+V.size());
		for(int l=0;l<V.size();l++)
		{
			R.setStat("GETRSCID"+l,((Environmental)V.get(l)).ID());
			R.setStat("GETRSCPARM"+l,((Environmental)V.get(l)).text());
		}
		V=itemList(oldR.outfit(null),'O',httpReq,false);
		R.setStat("NUMOFT",""+V.size());
		for(int l=0;l<V.size();l++)
		{
			R.setStat("GETOFTID"+l,((Environmental)V.get(l)).ID());
			R.setStat("GETOFTPARM"+l,((Environmental)V.get(l)).text());
		}
		V=itemList(new XVector<Item>(oldR.myNaturalWeapon()),'W',httpReq,true);
		if(V.size()==0)
			R.setStat("WEAPONCLASS","StdWeapon");
		else
		{
			R.setStat("WEAPONCLASS",((Environmental)V.get(0)).ID());
			R.setStat("WEAPONXML",((Environmental)V.get(0)).text());
		}
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
		}
		R.setStat("BREATHES", CMParms.toListString(l));

		setDynAbilities(R,httpReq);
		setDynEffects(R,httpReq);
		setDynImmunities(R,httpReq);

		QuadVector<String,Integer,Integer,Boolean> CV=cabilities(httpReq);
		R.setStat("NUMCABLE", ""+CV.size());
		for(int i=0;i<CV.size();i++)
		{
			R.setStat("GETCABLE"+i, CV.elementAt(i).first);
			R.setStat("GETCABLEPROF"+i, CV.elementAt(i).second.toString());
			R.setStat("GETCABLELVL"+i, CV.elementAt(i).third.toString());
			R.setStat("GETCABLEGAIN"+i, Boolean.valueOf(!CV.elementAt(i).fourth.booleanValue()).toString());
		}
		return "";
	}
}
