package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2020 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class AbilityNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityNext";
	}

	@Override
	@SuppressWarnings("unchecked")
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ABILITY");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("ABILITY");
			return "";
		}
		final String ableType=httpReq.getUrlParameter("ABILITYTYPE");
		if((ableType!=null)&&(ableType.length()>0))
			parms.put(ableType,ableType);
		final String domainType=httpReq.getUrlParameter("DOMAIN");
		if((domainType!=null)&&(domainType.length()>0))
			parms.put("DOMAIN",domainType);
		long flags=0;
		final String flagString=httpReq.getUrlParameter("FLAGS");
		if((flagString!=null)&&(flagString.length()>0))
		{
			final List<String> V=CMParms.parseSquiggles(flagString.toUpperCase());
			for(int i=0;i<Ability.FLAG_DESCS.length;i++)
			{
				if(V.contains(Ability.FLAG_DESCS[i]))
					flags=flags|(CMath.pow(2,i));
			}
		}

		String lastID="";
		final int[] normalAcodes = {
			Ability.ACODE_CHANT,
			Ability.ACODE_COMMON_SKILL,
			Ability.ACODE_LANGUAGE,
			Ability.ACODE_PRAYER,
			Ability.ACODE_SKILL,
			Ability.ACODE_SONG,
			Ability.ACODE_SPELL,
			Ability.ACODE_SUPERPOWER,
			Ability.ACODE_TECH,
			Ability.ACODE_THIEF_SKILL
		};
		final String className=httpReq.getUrlParameter("CLASS");
		final boolean genericOnly =parms.containsKey("GENERIC");
		final boolean parmsEditable=parms.containsKey("PARMSEDITABLE");
		final boolean unqualifiedOK=parms.containsKey("UNQUALIFIEDOK");
		final boolean unqualifiedNormalOK=parms.containsKey("UNQUALIFIEDNORMALOK");
		final String levelName=httpReq.getUrlParameter("LEVEL");
		final boolean notFlag =parms.containsKey("NOT");
		final boolean allFlag =parms.containsKey("ALL") || (parm==null);
		final boolean domainFlag=parms.containsKey("DOMAIN");
		final String domain=parms.get("DOMAIN");
		boolean containsACodeMask=false;
		for (final String element : Ability.ACODE_DESCS)
		{
			if(parms.containsKey(element))
			{
				containsACodeMask = true;
				break;
			}
		}

		final Enumeration<Ability> a;
		if(!parms.containsKey("SORTEDBYNAME"))
			a=CMClass.abilities();
		else
		if(httpReq.getRequestObjects().containsKey("ABILITIESSORTEDBYNAME"))
			a=((Vector)httpReq.getRequestObjects().get("ABILITIESSORTEDBYNAME")).elements();
		else
		{
			final Vector<Ability> fullList=new Vector<Ability>();
			for(final Enumeration<Ability> aa=CMClass.abilities();aa.hasMoreElements();)
				fullList.add(aa.nextElement());
			final Ability[] aaray=fullList.toArray(new Ability[0]);
			Arrays.sort(aaray, new Comparator<Ability>()
			{
				@Override
				public int compare(final Ability o1, final Ability o2)
				{
					return o1.Name().compareToIgnoreCase(o2.Name());
				}
			});
			fullList.clear();
			fullList.addAll(Arrays.asList(aaray));
			httpReq.getRequestObjects().put("ABILITIESSORTEDBYNAME",fullList);
			a=fullList.elements();
		}
		for(;a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			boolean okToShow=true;
			final int classType=A.classificationCode()&Ability.ALL_ACODES;
			if(genericOnly)
				okToShow=A.isGeneric();
			else
			if(parmsEditable)
			{
				okToShow=((A instanceof CraftorAbility)
						&&(((CraftorAbility)A).parametersFile()!=null)
						&&(((CraftorAbility)A).parametersFile().length()>0)
						&&(((CraftorAbility)A).parametersFormat()!=null)
						&&(((CraftorAbility)A).parametersFormat().length()>0));
			}

			if((className!=null)&&(className.length()>0))
			{
				final int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
				if((level<0)
				&&(!unqualifiedOK)
				&&((!unqualifiedNormalOK)
					||(!CMParms.contains(normalAcodes, A.classificationCode()&Ability.ALL_ACODES))))
					okToShow=false;
				else
				if(CMLib.ableMapper().getSecretSkill(className,false,A.ID())!=SecretFlag.PUBLIC)
					okToShow=false;
				else
				if((flags>0)&&((A.flags()&flags)!=flags))
					okToShow=false;
				else
				{
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			else
			if(!allFlag)
			{
				final int level=CMLib.ableMapper().getQualifyingLevel("Archon",true,A.ID());
				if((level<0)
				&&(!unqualifiedOK)
				&&((!unqualifiedNormalOK)
					||(!CMParms.contains(normalAcodes, A.classificationCode()&Ability.ALL_ACODES))))
					okToShow=false;
				else
				if(CMLib.ableMapper().getAllSecretSkill(A.ID())!=SecretFlag.PUBLIC)
					okToShow=false;
				else
				if((flags>0)&&((A.flags()&flags)!=flags))
					okToShow=false;
				else
				{
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			if(okToShow)
			{
				if((domainFlag)&&(!domain.equalsIgnoreCase(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5])))
					okToShow=false;
				if(containsACodeMask&&(!parms.containsKey(Ability.ACODE_DESCS[classType])))
					okToShow=false;
			}
			if(notFlag)
				okToShow=!okToShow;
			if(okToShow)
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("ABILITY",A.ID());
					return "";
				}
				lastID=A.ID();
			}
		}
		httpReq.addFakeUrlParameter("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
