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
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent;
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

public class GrinderFactions 
{
	public String name()
	{
		return "GrinderFactions";
	}

	public static String modifyFaction(HTTPRequest httpReq, java.util.Map<String,String> parms, Faction F)
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
		F.setName(old==null?("NAME"):old);

		old=httpReq.getUrlParameter("SHOWINSCORE");
		F.setShowInScore((old!=null)&&(old.equalsIgnoreCase("on")));

		old=httpReq.getUrlParameter("SHOWINFACTIONS");
		F.setShowInFactionsCommand((old!=null)&&(old.equalsIgnoreCase("on")));

		old=httpReq.getUrlParameter("SHOWINEDITOR");
		F.setShowInEditor((old!=null)&&(old.equalsIgnoreCase("on")));

		old=httpReq.getUrlParameter("SHOWINREPORTS");
		F.setShowInSpecialReported((old!=null)&&(old.equalsIgnoreCase("on")));

		int num=0;
		for(final Enumeration<Faction.FRange> e=F.ranges();e.hasMoreElements();)
			F.delRange(e.nextElement());
		while(httpReq.getUrlParameter("RANGENAME"+num)!=null)
		{
			old=httpReq.getUrlParameter("RANGENAME"+num);
			String code=httpReq.getUrlParameter("RANGECODE"+num);
			if(old.length()>0)
			{
				if(code.length()==0)
					code=CMStrings.replaceAll(old.toUpperCase().trim()," ","_");
				final int low=CMath.s_int(httpReq.getUrlParameter("RANGELOW"+num));
				int high=CMath.s_int(httpReq.getUrlParameter("RANGEHIGH"+num));
				if(high<low)
					high=low;
				final String flag=httpReq.getUrlParameter("RANGEFLAG"+num);
				F.addRange(low+";"+high+";"+old+";"+code+";"+flag);
			}
			num++;
		}

		old=httpReq.getUrlParameter("PLAYERCHOICETEXT");
		F.setChoiceIntro(old==null?"":old);

		final String[] prefixes={"AUTOVALUE","DEFAULTVALUE","PLAYERCHOICE"};
		for(int i=0;i<prefixes.length;i++)
		{
			final String prefix=prefixes[i];
			final Vector<String> V=new Vector<String>();
			num=0;
			while(httpReq.getUrlParameter(prefix+num)!=null)
			{
				final String value=httpReq.getUrlParameter(prefix+num);
				if(value.length()>0)
				{
					final String mask=httpReq.getUrlParameter(prefix+"MASK"+num);
					V.addElement((CMath.s_long(value)+" "+mask).trim());
				}
				num++;
			}
			switch(i)
			{
			case 0:
				F.setAutoDefaults(V);
				break;
			case 1:
				F.setDefaults(V);
				break;
			case 2:
				F.setChoices(V);
				break;
			}
		}

		F.clearChangeEvents();
		num=0;
		while(httpReq.getUrlParameter("CHANGESTRIGGER"+num)!=null)
		{
			old=httpReq.getUrlParameter("CHANGESTRIGGER"+num);
			if(old.length()>0)
			{
				final String ctparms=httpReq.getUrlParameter("CHANGESTPARM"+num);
				if(ctparms.trim().length()>0)
					old+="("+ctparms.trim()+")";
				old+=";";
				old+=Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CMath.s_int(httpReq.getUrlParameter("CHANGESDIR"+num))];
				old+=";";
				old+=CMath.toPct(httpReq.getUrlParameter("CHANGESFACTOR"+num));
				old+=";";
				String id="";
				int x=0;
				for(;httpReq.isUrlParameter("CHANGESFLAGS"+num+"_"+id);id=""+(++x))
					old+=" "+httpReq.getUrlParameter("CHANGESFLAGS"+num+"_"+id).toUpperCase();
				old+=";";
				old+=httpReq.getUrlParameter("CHANGESMASK"+num);
				F.createChangeEvent(old);
			}
			num++;
		}

		for(final Enumeration<Faction.FZapFactor> e=F.factors();e.hasMoreElements();)
			F.delFactor(e.nextElement());
		num=0;
		while(httpReq.getUrlParameter("ADJFACTOR"+num)!=null)
		{
			old=httpReq.getUrlParameter("ADJFACTOR"+num);
			if(old.length()>0)
			{
				final double gain=CMath.s_pct(httpReq.getUrlParameter("ADJFACTORGAIN"+num));
				final double loss=CMath.s_pct(httpReq.getUrlParameter("ADJFACTORLOSS"+num));
				F.addFactor(gain,loss,old);
			}
			num++;
		}

		num=0;
		for(final Enumeration<String> e=F.relationFactions();e.hasMoreElements();)
			F.delRelation(e.nextElement());
		while(httpReq.getUrlParameter("RELATIONS"+num)!=null)
		{
			old=httpReq.getUrlParameter("RELATIONS"+num);
			if(old.length()>0)
				F.addRelation(old,CMath.s_pct(httpReq.getUrlParameter("RELATIONSAMT"+num)));
			num++;
		}

		num=0;
		final TriadVector<String,String,String> affBehav=new TriadVector<String,String,String>();
		final HashSet<String> affBehavKeepers=new HashSet<String>();
		// its done this strange way to minimize impact on mob recalculations.
		while(httpReq.getUrlParameter("AFFBEHAV"+num)!=null)
		{
			old=httpReq.getUrlParameter("AFFBEHAV"+num);
			if(old.length()>0)
			{
				final String parm=""+httpReq.getUrlParameter("AFFBEHAVPARM"+num);
				final String mask=""+httpReq.getUrlParameter("AFFBEHAVMASK"+num);
				final String[] oldParms=F.getAffectBehav(old);
				if((oldParms==null)||(!oldParms[0].equals(parm))||(!oldParms[1].equals(mask)))
					affBehav.addElement(old.toUpperCase().trim(),parm,mask);
				else
					affBehavKeepers.add(old.toUpperCase().trim());
			}
			num++;
		}
		for(final Enumeration<String> e=F.affectsBehavs();e.hasMoreElements();)
		{
			old=e.nextElement();
			if(!affBehavKeepers.contains(old.toUpperCase().trim()))
				F.delAffectBehav(old);
		}
		for(int d=0;d<affBehav.size();d++)
		{
			F.delAffectBehav(affBehav.get(d).first);
			F.addAffectBehav(affBehav.get(d).first,affBehav.get(d).second,affBehav.get(d).third);
		}

		num=0;
		for(final Enumeration<Faction.FAbilityUsage> e=F.abilityUsages();e.hasMoreElements();)
			F.delAbilityUsage(e.nextElement());
		while(httpReq.getUrlParameter("ABILITYUSE"+num)!=null)
		{
			old=httpReq.getUrlParameter("ABILITYUSE"+num);
			if(old.length()>0)
			{
				final int usedType=CMLib.factions().getAbilityFlagType(old);
				if(usedType>0)
				{
					int x=-1;
					while(httpReq.isUrlParameter("ABILITYUSE"+num+"_"+(++x)))
					{
						final String s=httpReq.getUrlParameter("ABILITYUSE"+num+"_"+x);
						if(s.length()>0)
						{
							old+=" "+s.toUpperCase().trim();
						}
					}
				}
				old+=";"+CMath.s_int(httpReq.getUrlParameter("ABILITYMIN"+num));
				old+=";"+CMath.s_int(httpReq.getUrlParameter("ABILITYMAX"+num));
				F.addAbilityUsage(old);
			}
			num++;
		}

		num=0;
		for(final Enumeration<Faction.FReactionItem> e=F.reactions();e.hasMoreElements();)
			F.delReaction(e.nextElement());
		while(httpReq.getUrlParameter("REACTIONRANGE"+num)!=null)
		{
			old=httpReq.getUrlParameter("REACTIONRANGE"+num);
			final String old1=httpReq.getUrlParameter("REACTIONMASK"+num);
			final String old2=httpReq.getUrlParameter("REACTIONABC"+num);
			final String old3=httpReq.getUrlParameter("REACTIONPARM"+num);
			if(old.length()>0)
				F.addReaction(old,old1,old2,old3);
			num++;
		}
		old=httpReq.getUrlParameter("USELIGHTREACTIONS");
		F.setLightReactions((old!=null)&&(old.equalsIgnoreCase("on")));

		old=httpReq.getUrlParameter("RATEMODIFIER");
		F.setRateModifier(old==null?0.0:CMath.s_pct(old));

		old=httpReq.getUrlParameter("AFFECTONEXP");
		F.setExperienceFlag(old);

		return "";
	}
}
