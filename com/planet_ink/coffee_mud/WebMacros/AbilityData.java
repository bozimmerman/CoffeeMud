package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2007 Bo Zimmerman

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
public class AbilityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ABILITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=CMLib.help().getHelpText(A.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(A.Name(),null,false);
					int limit=70;
					if(parms.containsKey("LIMIT")) limit=CMath.s_int((String)parms.get("LIMIT"));
					str.append(helpHelp(s,limit));
				}
				if(parms.containsKey("RANGES"))
				{
					int min=A.minRange();
					int max=A.maxRange();
					if(min+max==0)
						str.append("Touch, or not applicable, ");
					else
					{
						if(min==0)
							str.append("Touch");
						else
							str.append("Range "+min);
						if(max>0)
							str.append(" - Range "+max);
						str.append(", ");
					}
				}
				if(parms.containsKey("QUALITY"))
				{
					switch(A.abstractQuality())
					{
					case Ability.QUALITY_MALICIOUS:
						str.append("Malicious, ");
						break;
					case Ability.QUALITY_BENEFICIAL_OTHERS:
					case Ability.QUALITY_BENEFICIAL_SELF:
						str.append("Always Beneficial, ");
						break;
					case Ability.QUALITY_OK_OTHERS:
					case Ability.QUALITY_OK_SELF:
						str.append("Sometimes Beneficial, ");
						break;
					case Ability.QUALITY_INDIFFERENT:
						str.append("Circumstantial, ");
						break;
					}
				}
				if(parms.containsKey("AUTO"))
				{
					if(A.isAutoInvoked())
						str.append("Automatic, ");
					else
						str.append("Requires invocation, ");
				}
				if(parms.containsKey("TARGET"))
				{
					switch(A.abstractQuality())
					{
					case Ability.QUALITY_INDIFFERENT:
						str.append("Item or Room, ");
						break;
					case Ability.QUALITY_MALICIOUS:
						str.append("Others, ");
						break;
					case Ability.QUALITY_BENEFICIAL_OTHERS:
					case Ability.QUALITY_OK_OTHERS:
						str.append("Caster or others, ");
						break;
					case Ability.QUALITY_BENEFICIAL_SELF:
					case Ability.QUALITY_OK_SELF:
						str.append("Caster only, ");
						break;
					}
				}

				if(parms.containsKey("ALIGNMENT"))
				{
				    for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
				    {
				        Faction F=(Faction)e.nextElement();
				        if(F.usageFactors(A).length()>0)
				            str.append(F.usageFactors(A)+", ");
				    }
				}
				if(parms.containsKey("ALLOWS"))
				{
					Vector allows=CMLib.ableMapper().getAbilityAllowsList(A.ID());
					Ability A2=null;
					if((allows!=null)&&(allows.size()>0))
					{
						ExpertiseLibrary.ExpertiseDefinition def=null;
						for(int a=0;a<allows.size();a++)
						{
							String allowStr=(String)allows.elementAt(a);
							def=CMLib.expertises().getDefinition(allowStr);
							if(def!=null)
								str.append(def.name+", ");
							else
							{
								A2=CMClass.getAbility(allowStr);
								if(A2!=null)
									str.append(A2.Name()+", ");
							}
						}
					}
				}
				if(parms.containsKey("DOMAIN"))
				{
					StringBuffer thang=new StringBuffer("");
					if((A.classificationCode()&Ability.ALL_DOMAINS)!=0)
					{
						int domain=A.classificationCode()&Ability.ALL_DOMAINS;
						domain=domain>>5;
						thang.append(Ability.DOMAIN_DESCS[domain].toLowerCase().replace('_',' '));
					}
					else
						thang.append(Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES].toLowerCase());
					if(thang.length()>0)
					{
						thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

						int x=thang.toString().indexOf("/");
						if(x>0) thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
						str.append(thang.toString()+", ");
					}
				}
                if(parms.containsKey("TYPENDOMAIN"))
                {
                    StringBuffer thang=new StringBuffer("");
                    thang.append(CMStrings.capitalizeAndLower(Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES]));
                    if((A.classificationCode()&Ability.ALL_DOMAINS)!=0)
                    {
                        int domain=A.classificationCode()&Ability.ALL_DOMAINS;
                        domain=domain>>5;
                        thang.append(": "+CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[domain]).replace('_',' '));
                    }
                    
                    if(thang.length()>0)
                    {
                        thang.setCharAt(0,Character.toUpperCase(thang.charAt(0)));

                        int x=thang.toString().indexOf("/");
                        while(x>0){
                            thang.setCharAt(x+1,Character.toUpperCase(thang.charAt(x+1)));
                            x=thang.toString().indexOf("/",x+1);
                        }
                        str.append(thang.toString()+", ");
                    }
                }
				if(parms.containsKey("QLEVEL"))
				{
					String className=httpReq.getRequestParameter("CLASS");
					int level=0;
					if((className!=null)&&(className.length()>0))
						level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
					else
						level=CMLib.ableMapper().getQualifyingLevel("Archon",true,A.ID());
					str.append(level+", ");
				}
				if(parms.containsKey("QUALIFYQ")&&(httpReq.isRequestParameter("CLASS")))
				{
					String className=httpReq.getRequestParameter("CLASS");
					if((className!=null)&&(className.length()>0))
					{
						boolean defaultGain=CMLib.ableMapper().getDefaultGain(className,true,A.ID());
						if(!defaultGain)
							str.append("(Qualify), ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
