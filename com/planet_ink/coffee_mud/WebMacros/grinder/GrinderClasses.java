package com.planet_ink.coffee_mud.WebMacros.grinder;

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

public class GrinderClasses
{
	public String name()
	{
		return "GrinderClasses";
	}

	public static DVector cabilities(HTTPRequest httpReq)
	{
		final DVector theclasses=new DVector(9);
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
					String secr=httpReq.getUrlParameter("CABSCR"+num);
					if(secr==null) 
						secr="";// null means unchecked
					Object parm=httpReq.getUrlParameter("CABPRM"+num);
					if(parm==null)
						parm="";
					Object prereq=httpReq.getUrlParameter("CABPRE"+num);
					if(prereq==null)
						prereq="";
					Object mask=httpReq.getUrlParameter("CABMSK"+num);
					if(mask==null)
						mask="";
					String maxp=httpReq.getUrlParameter("CABMPOF"+num);
					if(maxp==null)
						maxp="100";
					theclasses.addElement(behav,levl,prof,qual,secr,parm,prereq,mask,maxp);
				}
				num++;
				behav=httpReq.getUrlParameter("CABLES"+num);
			}
		}
		return theclasses;
	}

	public static String modifyCharClass(HTTPRequest httpReq, java.util.Map<String,String> parms, CharClass oldC, CharClass C)
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
		// names are numerous
		DVector DV=new DVector(2);
		int num=0;
		while(httpReq.isUrlParameter("NAME"+(++num)))
		{
			if(CMath.isInteger(httpReq.getUrlParameter("NAMELEVEL"+(num))))
			{
				final int minLevel = CMath.s_int(httpReq.getUrlParameter("NAMELEVEL"+(num)));
				final String name=httpReq.getUrlParameter("NAME"+(num));
				if((name!=null)&&(name.length()>0))
				{
					if(DV.size()==0)
						DV.addElement(Integer.valueOf(minLevel),name);
					else
					{
						boolean added=false;
						for(int n=0;n<DV.size();n++)
						{
							if(minLevel<((Integer)DV.elementAt(n,1)).intValue())
							{
								DV.insertElementAt(n,Integer.valueOf(minLevel),name);
								added=true;
								break;
							}
							else
							if(minLevel==((Integer)DV.elementAt(n,1)).intValue())
							{
								added=true;
								break;
							}
						}
						if(!added)
							DV.addElement(Integer.valueOf(minLevel),name);
					}
				}
			}
		}
		if(DV.size()==0)
			DV.addElement(Integer.valueOf(0),C.ID());
		C.setStat("NUMNAME",""+DV.size());
		for(int l=0;l<DV.size();l++)
		{
			C.setStat("NAME"+l, (String)DV.elementAt(l,2));
			C.setStat("NAMELEVEL"+l, ((Integer)DV.elementAt(l,1)).toString());
		}

		old=httpReq.getUrlParameter("");
		C.setStat("",(old==null)?"":old);

		old=httpReq.getUrlParameter("BASE");
		C.setStat("BASE",(old==null)?"BASECLASS":old);
		old=httpReq.getUrlParameter("HITPOINTSFORMULA");
		C.setStat("HITPOINTSFORMULA",(old==null)?"1":old);
		old=httpReq.getUrlParameter("MANAFORMULA");
		C.setStat("MANAFORMULA",(old==null)?"1":old);
		old=httpReq.getUrlParameter("MOVEMENTFORMULA");
		C.setStat("MOVEMENTFORMULA",(old==null)?"1":old);
		old=httpReq.getUrlParameter("LVLPRAC");
		C.setStat("LVLPRAC",(old==null)?"0":old);
		old=httpReq.getUrlParameter("LVLATT");
		C.setStat("LVLATT",(old==null)?"0":old);
		old=httpReq.getUrlParameter("ATTATT");
		C.setStat("ATTATT",(old==null)?"0":old);
		old=httpReq.getUrlParameter("FSTTRAN");
		C.setStat("FSTTRAN",(old==null)?"0":old);
		old=httpReq.getUrlParameter("FSTPRAC");
		C.setStat("FSTPRAC",(old==null)?"0":old);
		old=httpReq.getUrlParameter("LVLDAM");
		C.setStat("LVLDAM",(old==null)?"10":old);
		old=httpReq.getUrlParameter("MAXNCS");
		C.setStat("MAXNCS",(old==null)?"0":old);
		old=httpReq.getUrlParameter("MAXCRS");
		C.setStat("MAXCRS",(old==null)?"0":old);
		old=httpReq.getUrlParameter("MONEY");
		C.setStat("MONEY",(old==null)?"":old);
		old=httpReq.getUrlParameter("MAXCMS");
		C.setStat("MAXCMS",(old==null)?"0":old);
		old=httpReq.getUrlParameter("MAXLGS");
		C.setStat("MAXLGS",(old==null)?"0":old);
		old=httpReq.getUrlParameter("SUBRUL");
		C.setStat("SUBRUL", (old==null)?"BASE":old);
		old=httpReq.getUrlParameter("LEVELCAP");
		C.setStat("LEVELCAP",(old==null)?"-1":old);
		old=httpReq.getUrlParameter("ARMOR");
		C.setStat("ARMOR",(old==null)?"0":old);
		old=httpReq.getUrlParameter("STRLMT");
		C.setStat("STRLMT",(old==null)?"STRLMT":old);
		old=httpReq.getUrlParameter("STRBON");
		C.setStat("STRBON",(old==null)?"STRBON":old);
		old=httpReq.getUrlParameter("QUAL");
		C.setStat("QUAL",(old==null)?"":old);
		old=httpReq.getUrlParameter("PLAYER");
		C.setStat("PLAYER",(old==null)?"0":old);
		C.setStat("ESTATS",GrinderRaces.getPStats('E',httpReq));
		C.setStat("CSTATS",GrinderRaces.getCStats('S',httpReq));
		C.setStat("ASTATS",GrinderRaces.getCStats('A',httpReq));
		C.setStat("ASTATE",GrinderRaces.getCState('A',httpReq));
		C.setStat("STARTASTATE",GrinderRaces.getCState('S',httpReq));
		old=httpReq.getUrlParameter("GENHELP");
		C.setStat("HELP", ((old==null)?"":old));
		old=httpReq.getUrlParameter("RACQUAL");
		C.setStat("RACQUAL",(old==null)?"All":old);
		String id="";
		List<String> V=new Vector<String>();
		for(int i=0;httpReq.isUrlParameter("NOWEAPS"+id);id=""+(++i))
			V.add(httpReq.getUrlParameter("NOWEAPS"+id));
		C.setStat("GETWEP",CMParms.toListString(V));
		int x=0;
		final List<Pair<String,Integer>> minStats=new LinkedList<Pair<String,Integer>>();
		while(httpReq.getUrlParameter("MINSTAT"+x)!=null)
		{
			final String minStat=httpReq.getUrlParameter("MINSTAT"+x);
			final String statMin=httpReq.getUrlParameter("STATMIN"+x);
			if((minStat!=null)&&(minStat.length()>0)&&(CMath.isInteger(statMin)))
				minStats.add(new Pair<String,Integer>(minStat,Integer.valueOf(CMath.s_int(statMin))));
			x++;
		}
		C.setStat("NUMMINSTATS", ""+minStats.size());
		for(int m=0;m<minStats.size();m++)
		{
			C.setStat("GETMINSTAT"+m, minStats.get(m).first);
			C.setStat("GETSTATMIN"+m, minStats.get(m).second.toString());
		}
		final List<Item> Ivs=GrinderRaces.itemList(oldC.outfit(null),'O',httpReq,false);
		C.setStat("NUMOFT",""+Ivs.size());
		for(int l=0;l<Ivs.size();l++)
		{
			C.setStat("GETOFTID"+l,((Environmental)Ivs.get(l)).ID());
			C.setStat("GETOFTPARM"+l,((Environmental)Ivs.get(l)).text());
		}
		C.setStat("DISFLAGS",""+CMath.s_long(httpReq.getUrlParameter("DISFLAGS")));
		num=0;
		DV.clear();
		while(httpReq.isUrlParameter("SSET"+(++num)))
		{
			if(CMath.isInteger(httpReq.getUrlParameter("SSETLEVEL"+(num))))
			{
				final int minLevel = CMath.s_int(httpReq.getUrlParameter("SSETLEVEL"+(num)));
				final String name=httpReq.getUrlParameter("SSET"+(num));
				if((name!=null)&&(name.length()>0))
				{
					if(DV.size()==0)
						DV.addElement(Integer.valueOf(minLevel),name);
					else
					{
						boolean added=false;
						for(int n=0;n<DV.size();n++)
						{
							if(minLevel<((Integer)DV.elementAt(n,1)).intValue())
							{
								DV.insertElementAt(n,Integer.valueOf(minLevel),name);
								added=true;
								break;
							}
							else
							if(minLevel==((Integer)DV.elementAt(n,1)).intValue())
							{
								added=true;
								break;
							}
						}
						if(!added)
							DV.addElement(Integer.valueOf(minLevel),name);
					}
				}
			}
		}
		C.setStat("NUMSSET",""+DV.size());
		for(int l=0;l<DV.size();l++)
		{
			final String sec=(String)DV.elementAt(l,2);
			V=CMParms.parseCommas(sec, true);
			C.setStat("SSET"+l, CMParms.combineQuoted(V,0));
			C.setStat("SSETLEVEL"+l, ((Integer)DV.elementAt(l,1)).toString());
		}
		id="";
		V=new Vector<String>();
		for(int i=0;httpReq.isUrlParameter("WEAPMATS"+id);id=""+(++i))
		{
			if(CMath.isInteger(httpReq.getUrlParameter("WEAPMATS"+id)))
				V.add(httpReq.getUrlParameter("WEAPMATS"+id));
		}
		C.setStat("NUMWMAT",""+V.size());
		C.setStat("GETWMAT",CMParms.toListString(V));
		old=httpReq.getUrlParameter("ARMORMINOR");
		C.setStat("ARMORMINOR",(old==null)?"-1":old);
		old=httpReq.getUrlParameter("STATCLASS");
		C.setStat("STATCLASS",(old==null)?"":old);
		old=httpReq.getUrlParameter("EVENTCLASS");
		C.setStat("EVENTCLASS",(old==null)?"":old);
		DV=cabilities(httpReq);
		C.setStat("NUMCABLE", ""+DV.size());
		for(int i=0;i<DV.size();i++)
		{
			C.setStat("GETCABLELVL"+i, (String)DV.elementAt(i,2));
			C.setStat("GETCABLEPROF"+i, (String)DV.elementAt(i,3));
			C.setStat("GETCABLEGAIN"+i, ((String)DV.elementAt(i,4)).equalsIgnoreCase("on")?"false":"true");
			C.setStat("GETCABLESECR"+i, ((String)DV.elementAt(i,5)).equalsIgnoreCase("on")?"true":"false");
			if(DV.elementAt(i,6) instanceof String)
				C.setStat("GETCABLEPARM"+i, (String)DV.elementAt(i,6));
			if(DV.elementAt(i,7) instanceof String)
				C.setStat("GETCABLEPREQ"+i, (String)DV.elementAt(i,7));
			if(DV.elementAt(i,8) instanceof String)
				C.setStat("GETCABLEMASK"+i, (String)DV.elementAt(i,8));
			C.setStat("GETCABLEMAXP"+i, (String)DV.elementAt(i,9));
			// CABLE MUST BE LAST
			C.setStat("GETCABLE"+i, (String)DV.elementAt(i,1));
		}
		return "";
	}
}
