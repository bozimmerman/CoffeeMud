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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class AbilityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
        Hashtable parms=parseParms(parm);
        
        String replaceCommand=httpReq.getRequestParameter("REPLACE");
        if((replaceCommand != null) 
        && (replaceCommand.length()>0)
        && (replaceCommand.indexOf('=')>0))
        {
            int eq=replaceCommand.indexOf('=');
            String field=replaceCommand.substring(0,eq);
            String value=replaceCommand.substring(eq+1);
            httpReq.addRequestParameters(field, value);
            httpReq.addRequestParameters("REPLACE","");
        }
        
        String last=httpReq.getRequestParameter("ABILITY");
        if(last==null) return " @break@";
        Ability A=null;
        String newAbilityID=httpReq.getRequestParameter("NEWABILITY");
        if(A==null)
            A=(Ability)httpReq.getRequestObjects().get("ABILITY-"+last);
        if((A==null)
        &&(newAbilityID!=null)
        &&(newAbilityID.length()>0)
        &&(CMClass.getAbility(newAbilityID)==null))
        {
            A=(Ability)CMClass.getAbility("GenAbility").copyOf();
            A.setStat("CLASS9",newAbilityID);
            last=newAbilityID;
            httpReq.addRequestParameters("ABILITY",newAbilityID);
        }
        if(last.length()>0)
        {
            if(A==null)
                A=CMClass.getAbility(last);
            if(parms.containsKey("ISNEWABILITY"))
                return ""+(CMClass.getAbility(last)==null);
            if(A!=null)
            {
				StringBuffer str=new StringBuffer("");
                if(parms.containsKey("ISGENERIC"))
                {
                    Ability A2=CMClass.getAbility(A.ID());
                    return ""+((A2!=null)&&(A2.isGeneric()));
                }
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null) old=A.name();
                    str.append(old+", ");
                }
                if(parms.containsKey("GENHELP"))
                {
                    String old=httpReq.getRequestParameter("GENHELP");
                    if(old==null) old=A.getStat("HELP");
                    str.append(old+", ");
                }
                // here starts CLASSIFICATION
                if(parms.containsKey("CLASSIFICATION_ACODE"))
                {
                    String old=httpReq.getRequestParameter("CLASSIFICATION_ACODE");
                    if(old==null) old=""+(A.classificationCode()&Ability.ALL_ACODES);
                    for(int i=0;i<Ability.ACODE_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.ACODE_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("CLASSIFICATION_DOMAIN"))
                {
                    String old=httpReq.getRequestParameter("CLASSIFICATION_DOMAIN");
                    if(old==null) old=""+((A.classificationCode()&Ability.ALL_DOMAINS)>>5);
                    for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[i]));
                    str.append(", ");
                }
                // here ends CLASSIFICATION
                
                if(parms.containsKey("TRIGSTR"))
                {
                    String old=httpReq.getRequestParameter("TRIGSTR");
                    if(old==null) old=CMParms.toStringList(A.triggerStrings());
                    // remember to sort by longest->shortest on put-back
                    str.append(old.toUpperCase().trim()+", ");
                }
                if(parms.containsKey("MINRANGE"))
                {
                    String old=httpReq.getRequestParameter("MINRANGE");
                    if(old==null) old=""+A.minRange();
                    for(int i=0;i<Ability.RANGE_CHOICES.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.RANGE_CHOICES[i]));
                    str.append(", ");
                }
                if(parms.containsKey("MAXRANGE"))
                {
                    String old=httpReq.getRequestParameter("MAXRANGE");
                    if(old==null) old=""+A.maxRange();
                    for(int i=0;i<Ability.RANGE_CHOICES.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.RANGE_CHOICES[i]));
                    str.append(", ");
                }
                if(parms.containsKey("TICKSBETWEENCASTS"))
                {
                    String old=httpReq.getRequestParameter("TICKSBETWEENCASTS");
                    if(old==null) old=""+A.getTicksBetweenCasts();
                    str.append(old+", ");
                }
                if(parms.containsKey("DISPLAY")) // affected string
                {
                    String old=httpReq.getRequestParameter("DISPLAY");
                    if(old==null) old=A.displayText();
                    str.append(old+", ");
                }
                if(parms.containsKey("AUTOINVOKE"))
                {
                    String old=httpReq.getRequestParameter("AUTOINVOKE");
                    if(old==null) 
                        old=A.getStat("AUTOINVOKE");
                    else
                        old=""+old.equalsIgnoreCase("on");
                    str.append(CMath.s_bool(old)?"CHECKED":"");
                    str.append(", ");
                }
                if(parms.containsKey("ABILITY_FLAGS"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("ABILITY_FLAGS"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("ABILITY_FLAGS"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("ABILITY_FLAGS"+id));
                    } 
                    else 
                        list=CMParms.parseCommas(A.getStat("FLAGS"),true);
                    for(int i=0;i<Ability.FLAG_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+Ability.FLAG_DESCS[i]+"\""+(list.contains(Ability.FLAG_DESCS[i])?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.FLAG_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("CUSTOMOVERRIDEMANA"))
                {
                    String old=httpReq.getRequestParameter("OVERRIDEMANA");
                    if(old==null) old=""+A.getStat("OVERRIDEMANA");
                    int x=CMath.s_int(old);
                    if((x>0) && (x<Integer.MAX_VALUE-101))
                        str.append(old+", ");
                }
                if(parms.containsKey("OVERRIDEMANA"))
                {
                    String old=httpReq.getRequestParameter("OVERRIDEMANA");
                    if(old==null) old=""+A.getStat("OVERRIDEMANA");
                    int o=CMath.s_int(old);
                    str.append("<OPTION VALUE=\"-1\""+((o==-1)?" SELECTED":"")+">Use Default");
                    str.append("<OPTION VALUE=\"0\""+((o==0)?" SELECTED":"")+">None (free skill)");
                    str.append("<OPTION VALUE=\"\""+(((o>0)&&(o<Integer.MAX_VALUE-101))?" SELECTED":"")+"\">Custom Value");
                    str.append("<OPTION VALUE=\""+Integer.MAX_VALUE+"\""+((o==Integer.MAX_VALUE)?" SELECTED":"")+">All Mana");
                    for(int v=Integer.MAX_VALUE-5;v>=Integer.MAX_VALUE-95;v-=5) {
                        str.append("<OPTION VALUE=\""+v+"\""+(((o>(v-5))&&(o<=v))?" SELECTED":"")+">"+(Integer.MAX_VALUE-v)+"%");
                    }
                    str.append(", ");
                }
                if(parms.containsKey("USAGEMASK"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("USAGEMASK"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("USAGEMASK"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("USAGEMASK"+id));
                    } 
                    else 
                        list=CMParms.parseCommas(A.getStat("USAGEMASK"),true);
                    for(int i=0;i<Ability.USAGE_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+Ability.USAGE_DESCS[i]+"\""+(list.contains(Ability.USAGE_DESCS[i])?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.USAGE_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("CANAFFECTMASK"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("CANAFFECTMASK"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("CANAFFECTMASK"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("CANAFFECTMASK"+id));
                    } 
                    else 
                        list=CMParms.parseCommas(A.getStat("CANAFFECTMASK"),true);
                    for(int i=0;i<Ability.CAN_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+Ability.CAN_DESCS[i]+"\""+(list.contains(Ability.CAN_DESCS[i])?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.CAN_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("CANTARGETMASK"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("CANTARGETMASK"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("CANTARGETMASK"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("CANTARGETMASK"+id));
                    } 
                    else 
                        list=CMParms.parseCommas(A.getStat("CANTARGETMASK"),true);
                    for(int i=0;i<Ability.CAN_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+Ability.CAN_DESCS[i]+"\""+(list.contains(Ability.CAN_DESCS[i])?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.CAN_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("VQUALITY")) //QUALITY
                {
                    String old=httpReq.getRequestParameter("VQUALITY");
                    if(old==null) old=""+A.abstractQuality();
                    for(int i=0;i<Ability.QUALITY_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(Ability.QUALITY_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("HERESTATS")) // affect adj: Prop_HereAdjuster
                {
                    String old=httpReq.getRequestParameter("HERESTATS");
                    if(old==null) old=A.getStat("HERESTATS");
                    str.append(old+", ");
                }
                if(parms.containsKey("SCRIPT"))
                {
                    String old=httpReq.getRequestParameter("SCRIPT");
                    if(old==null) old=A.getStat("SCRIPT");
                    str.append(old+", ");
                }
                if(parms.containsKey("CASTMASK"))
                {
                    String old=httpReq.getRequestParameter("CASTMASK");
                    if(old==null) old=A.getStat("CASTMASK");
                    str.append(old+", ");
                }
                if(parms.containsKey("TARGETMASK"))
                {
                    String old=httpReq.getRequestParameter("TARGETMASK");
                    if(old==null) old=A.getStat("TARGETMASK");
                    str.append(old+", ");
                }
                if(parms.containsKey("FIZZLEMSG"))
                {
                    String old=httpReq.getRequestParameter("FIZZLEMSG");
                    if(old==null) old=A.getStat("FIZZLEMSG");
                    str.append(old+", ");
                }
                if(parms.containsKey("AUTOCASTMSG"))
                {
                    String old=httpReq.getRequestParameter("AUTOCASTMSG");
                    if(old==null) old=A.getStat("AUTOCASTMSG");
                    str.append(old+", ");
                }
                if(parms.containsKey("CASTMSG"))
                {
                    String old=httpReq.getRequestParameter("CASTMSG");
                    if(old==null) old=A.getStat("CASTMSG");
                    str.append(old+", ");
                }
                if(parms.containsKey("POSTCASTMSG"))
                {
                    String old=httpReq.getRequestParameter("POSTCASTMSG");
                    if(old==null) old=A.getStat("POSTCASTMSG");
                    str.append(old+", ");
                }
                if(parms.containsKey("ATTACKCODE"))
                {
                    String old=httpReq.getRequestParameter("ATTACKCODE");
                    if(old==null) old=""+CMParms.indexOf(CMMsg.TYPE_DESCS,A.getStat("ATTACKCODE"));
                    for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CMMsg.TYPE_DESCS[i]));
                    str.append(", ");
                }
                if(parms.containsKey("POSTCASTAFFECT"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("POSTCASTAFFECT"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("POSTCASTAFFECT"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("POSTCASTAFFECT"+id).toUpperCase());
                    } 
                    else 
                        list=CMParms.parseSemicolons(A.getStat("POSTCASTAFFECT").toUpperCase(),true);
                    for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                    {
                        Ability A2=(Ability)e.nextElement();
                        String AID=A2.ID();
                        String ANAME=A2.name();
                        str.append("<OPTION VALUE=\""+AID+"\""+(list.contains(AID.toUpperCase())?" SELECTED":"")+">"+ANAME);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("POSTCASTABILITY"))
                {
                    Vector list=new Vector();
                    if(httpReq.isRequestParameter("POSTCASTABILITY"))
                    {
                        String id="";
                        int num=0;
                        for(;httpReq.isRequestParameter("POSTCASTABILITY"+id);id=""+(++num))
                            list.addElement(httpReq.getRequestParameter("POSTCASTABILITY"+id).toUpperCase());
                    } 
                    else 
                        list=CMParms.parseSemicolons(A.getStat("POSTCASTABILITY").toUpperCase(),true);
                    for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                    {
                        Ability A2=(Ability)e.nextElement();
                        String AID=A2.ID();
                        String ANAME=A2.name();
                        str.append("<OPTION VALUE=\""+AID+"\""+(list.contains(AID.toUpperCase())?" SELECTED":"")+">"+ANAME);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("POSTCASTDAMAGE"))
                {
                    /*
                        Enter a damage or healing formula.
                        Use +/-*()?. @x1=caster level, @x2=target level.
                        Formula evaluates >0 for damage, <0 for healing. Requires Can Target!"
                    */
                    String old=httpReq.getRequestParameter("POSTCASTDAMAGE");
                    if(old==null) old=A.getStat("POSTCASTDAMAGE");
                    str.append(old+", ");
                }
                
                /*********************************************************************************/
                /*********************************************************************************/
	            // here begins the old display data parms
	            
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText(A.ID(),null,false);
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
				    String rangeDesc=null;
				    for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
				    {
				        Faction F=(Faction)e.nextElement();
				        rangeDesc=F.usageFactorRangeDescription(A);
				        if(rangeDesc.length()>0)
				            str.append(rangeDesc+", ");
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
