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

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class CrossBaseClassAbilities extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CrossBaseClassAbilities";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final StringBuffer buf=new StringBuffer("");
		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms.containsKey("DOMAINSKILLSBYCLASS"))
		{
			final String className=parms.get("CHARCLASS");
			if(className==null)
				return " @break@";
			final String domainStr=parms.get("DOMAIN");
			if(domainStr==null)
				return " @break@";
			final CharClass C=CMClass.findCharClass(className);
			if(C==null)
				return " @break@";
			int domain=CMParms.indexOf(Ability.DOMAIN_DESCS, domainStr.toUpperCase().trim());
			if(domain<0)
				return " @break@";
			domain = domain << 5;
			int levelCap=C.getLevelCap();
			if(levelCap < 0)
				levelCap = CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
			List<AbilityMapper.AbilityMapping> list = CMLib.ableMapper().getUpToLevelListings(C.ID(), levelCap, true, false);
			int ct=0;
			for(AbilityMapper.AbilityMapping mapping : list)
			{
				Ability A=(Ability)CMClass.getPrototypeByID(CMClass.CMObjectType.ABILITY, mapping.abilityID());
				if(A==null)
					Log.errOut("CrossBase!","Ability not found?! : "+mapping.abilityID());
				else
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
				&&(!CMLib.ableMapper().getSecretSkill(C.ID(), false, A.ID())))
					ct++;
			}
			buf.append(Integer.toString(ct));
		}
		else
		if(parms.containsKey("DOMAINSKILLSBYUNASSIGNED"))
		{
			final String domainStr=parms.get("DOMAIN");
			if(domainStr==null)
				return " @break@";
			int domain=CMParms.indexOf(Ability.DOMAIN_DESCS, domainStr.toUpperCase().trim());
			if(domain<0)
				return " @break@";
			domain = domain << 5;
			int ct=0;
			for(Enumeration<Ability> a= CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
				&&(!CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
				&&(!CMLib.ableMapper().getSecretSkill(A.ID())))
					ct++;
			}
			buf.append(Integer.toString(ct));
		}
		else
		{
			String baseClass=httpReq.getUrlParameter("BASECLASS");
			if(baseClass==null) 
				return " @break@";
			if(baseClass.length()>0)
			{
				final Vector<String> charClasses=new Vector<String>();
				for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
				{
					final CharClass C=c.nextElement();
					if((CMProps.isTheme(C.availabilityCode()))
					&&(C.baseClass().equals(baseClass))
					&&(!charClasses.contains(C.ID())))
						charClasses.addElement(C.ID());
				}

				final Vector<String> abilities=new Vector<String>();
				final Vector<Integer> levelssum=new Vector<Integer>();
				final Vector<Integer> numberare=new Vector<Integer>();
				for(int c=0;c<charClasses.size();c++)
				{
					final String className=charClasses.elementAt(c);
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						final int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
						if((level>=0)&&(!CMLib.ableMapper().getSecretSkill(className,false,A.ID())))
						{
							final int dex=abilities.indexOf(A.ID());
							if(dex<0)
							{
								abilities.addElement(A.ID());
								levelssum.addElement(Integer.valueOf(level));
								numberare.addElement(Integer.valueOf(1));
							}
							else
							{
								final Integer I=levelssum.elementAt(dex);
								levelssum.setElementAt(Integer.valueOf(I.intValue()+level),dex);
								final Integer I2=numberare.elementAt(dex);
								numberare.setElementAt(Integer.valueOf(I2.intValue()+1),dex);
							}
						}
					}
				}

				final Vector<String> sortedAbilities=new Vector<String>();
				while(abilities.size()>0)
				{
					double lowAvg=Double.MAX_VALUE;
					int lowDex=-1;
					for(int i=0;i<abilities.size();i++)
					{
						final Integer I=levelssum.elementAt(i);
						final Integer I2=numberare.elementAt(i);
						final double avg=CMath.div(I.intValue(),I2.intValue());
						if(avg<lowAvg)
						{
							lowAvg=avg;
							lowDex=i;
						}
					}
					if(lowDex>=0)
					{
						sortedAbilities.addElement(abilities.elementAt(lowDex));
						abilities.removeElementAt(lowDex);
						levelssum.removeElementAt(lowDex);
						numberare.removeElementAt(lowDex);
					}
				}

				buf.append("<BR><BR><BR><B><H3>"+baseClass+"</H3></B>\n\r");
				buf.append("<TABLE WIDTH=100% CELLSPACING=0 CELLPADDING=0 BORDER=1>\n\r");
				buf.append("<TR>");
				buf.append("<TD><B><FONT COLOR=WHITE>Skill</FONT></B></TD>");
				for(int c=0;c<charClasses.size();c++)
				{
					final String charClass=charClasses.elementAt(c);
					buf.append("<TD><B><FONT COLOR=WHITE>"+charClass+"</FONT></B></TD>");
				}
				buf.append("</TR>\n\r");
				for(int a=0;a<sortedAbilities.size();a++)
				{
					final String able=sortedAbilities.elementAt(a);
					buf.append("<TR><TD><B><FONT COLOR=WHITE>"+able+"</FONT></B></TD>");
					for(int c=0;c<charClasses.size();c++)
					{
						final String charClass=charClasses.elementAt(c);
						final int level=CMLib.ableMapper().getQualifyingLevel(charClass,true,able);
						if(level>=0)
							buf.append("<TD><FONT COLOR=CYAN>"+level+"</FONT></TD>");
						else
							buf.append("<TD><BR></TD>");
					}
					buf.append("</TR>\n\r");
				}
				buf.append("</TABLE>");
			}
		}
		return clearWebMacros(buf);
	}

}
