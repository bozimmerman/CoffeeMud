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
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2023 Bo Zimmerman

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
public class AutoAwardData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AutoAwardData";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("AUTOAWARD");
		if((last==null)&&(!parms.containsKey("EDIT")))
			return " @break@";

		if(parms.containsKey("EDIT"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.AUTOAWARDS))
				return "[authentication error]";
			final String newPMask=httpReq.getUrlParameter("PMASK");
			if(newPMask==null)
				return "[missing data error]";
			final String newDMask=httpReq.getUrlParameter("DMASK");
			if(newDMask==null)
				return "[missing data error]";
			final StringBuilder pstr = new StringBuilder("");
			if(httpReq.isUrlParameter("AFFECTBEHAVS_AFFECT1"))
			{
				int i1=1;
				while(httpReq.isUrlParameter("AFFECTBEHAVS_AFFECT"+i1))
				{
					final Pair<String,String> p = new Pair<String,String>("","");
					p.first=httpReq.getUrlParameter("AFFECTBEHAVS_AFFECT"+i1);
					p.second=httpReq.getUrlParameter("AFFECTBEHAVS_ADATA"+i1);
					if(p.first.trim().length()>0)
					{
						pstr.append(p.first.trim())
							.append("(")
							.append(CMStrings.replaceAll(p.second,")","\\)"))
							.append(") ");
					}
					i1++;
				}
			}
			else
				return "[missing data error]";
			final StringBuilder line = new StringBuilder("");
			line.append(newPMask).append(" :: ");
			line.append(newDMask).append(" :: ");
			line.append(pstr.toString());
			int i=Integer.MAX_VALUE;
			if((last != null)&&(CMath.isInteger(last)))
				i=CMath.s_int(last);
			CMLib.awards().modifyAutoProperty(i, line.toString());
			return "";
		}
		else
		if(parms.containsKey("DELETE"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.AUTOAWARDS))
				return "[authentication error]";
			if((last==null)||(CMath.s_int(last)<1))
				return " @break@";

			final int num = CMath.s_int(last);
			if(CMLib.awards().modifyAutoProperty(num, null))
				return "Award rule deleted.";
			else
				return "Unknown award rule!";
		}
		else
		if(last==null)
			return " @break@";
		final int num=CMath.s_int(last);
		int i=1;
		AutoProperties P = null;
		if(num>0)
		{
			for(final Enumeration<AutoProperties> p =CMLib.awards().getAutoProperties();p.hasMoreElements();)
			{
				P=p.nextElement();
				if(num == i)
					break;
				i++;
			}
		}

		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("PMASK"))
		{
			String mask=httpReq.getUrlParameter("PMASK");
			if((mask==null)&&(last!=null)&&(last.length()>0)&&(P!=null))
				mask=P.getPlayerMask();
			if(mask!=null)
				str.append(CMStrings.replaceAll(mask,"\"","&quot;")+", ");
		}
		if(parms.containsKey("DMASK"))
		{
			String mask=httpReq.getUrlParameter("DMASK");
			if((mask==null)&&(last!=null)&&(last.length()>0)&&(P!=null))
				mask=CMStrings.replaceAll(P.getDateMask(),"\\)",")");
			if(mask!=null)
				str.append(CMStrings.replaceAll(mask,"\"","&quot;")+", ");
		}
		if(parms.containsKey("PMASKDESC"))
		{
			String mask=httpReq.getUrlParameter("PMASK");
			if((mask==null)&&(last!=null)&&(last.length()>0)&&(P!=null))
				mask=P.getPlayerMask();
			if(mask!=null)
				str.append(CMLib.masking().maskDesc(mask)+", ");
		}
		if(parms.containsKey("DMASKDESC"))
		{
			String mask=httpReq.getUrlParameter("DMASK");
			if((mask==null)&&(last!=null)&&(last.length()>0)&&(P!=null))
				mask=CMStrings.replaceAll(P.getDateMask(),"\\)",")");
			if(mask!=null)
				str.append(CMLib.masking().maskDesc(mask)+", ");
		}
		if(parms.containsKey("PROPDESC")&&(P!=null))
		{
			final List<Pair<String,String>> properties=Arrays.asList(P.getProps());
			final StringBuilder p2 = new StringBuilder("");
			for(final Pair<String,String> a : properties)
				p2.append(a.first).append("(").append(a.second).append(") ");
			str.append(p2.toString()).append("\n\r");
		}
		if(parms.containsKey("PROPERTIES"))
		{
			final List<Pair<String,String>> properties;
			if(httpReq.isUrlParameter("AFFECTBEHAVS_AFFECT1"))
			{
				properties = new ArrayList<Pair<String,String>>();
				int i1=1;
				while(httpReq.isUrlParameter("AFFECTBEHAVS_AFFECT"+i1))
				{
					final Pair<String,String> p = new Pair<String,String>("","");
					p.first=httpReq.getUrlParameter("AFFECTBEHAVS_AFFECT"+i1);
					p.second=httpReq.getUrlParameter("AFFECTBEHAVS_ADATA"+i1);
					if(p.first.trim().length()>0)
						properties.add(p);
					i1++;
				}
			}
			else
			if(P!=null)
				properties=Arrays.asList(P.getProps());
			else
				properties = new ArrayList<Pair<String,String>>();
			str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
			for(int i1=0;i1<properties.size();i1++)
			{
				final Pair<String,String> SP=properties.get(i1);
				str.append("<TR><TD WIDTH=50%>");
				str.append("\n\r<SELECT ONCHANGE=\"EditAffect(this);\" NAME=AFFECTBEHAVS_AFFECT"+(i1+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+SP.first+"\" SELECTED>"+SP.first);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				final String parmstr=SP.second;
				final String theparm=CMStrings.replaceAll(parmstr,"\"","&quot;");
				str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME=AFFECTBEHAVS_ADATA"+(i1+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("\n\r<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECTBEHAVS_AFFECT"+(properties.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Effect/Behav");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
					continue;
				final String cnam=A.ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
			{
				final Behavior B=b.nextElement();
				final String cnam=B.ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>");
			str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME=AFFECTBEHAVS_ADATA"+(properties.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
