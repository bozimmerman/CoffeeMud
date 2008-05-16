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
   Copyright 2000-2008 Bo Zimmerman

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
public class FactionData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("FACTION");
        if(last==null) return " @break@";
        if(last.length()>0)
        {
            Faction F=CMLib.factions().getFaction(last);
            if(F!=null)
            {
                StringBuffer str=new StringBuffer("");
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null) old=F.name();
                    str.append(old+", ");
                }
                if(parms.containsKey("SHOWINSCORE"))
                {
                    String old=httpReq.getRequestParameter("SHOWINSCORE");
                    if(old==null) old=F.showinscore()?"on":"";
                    str.append(old+", ");
                }
                if(parms.containsKey("SHOWINFACTIONS"))
                {
                    String old=httpReq.getRequestParameter("SHOWINFACTIONS");
                    if(old==null) old=F.showinfactionscommand()?"on":"";
                    str.append(old+", ");
                }
                if(parms.containsKey("SHOWINEDITOR"))
                {
                    String old=httpReq.getRequestParameter("SHOWINEDITOR");
                    if(old==null) old=F.showineditor()?"on":"";
                    str.append(old+", ");
                }
                if(parms.containsKey("SHOWINREPORTS"))
                {
                    String old=httpReq.getRequestParameter("SHOWINREPORTS");
                    if(old==null) old=F.showinspecialreported()?"on":"";
                    str.append(old+", ");
                }
                if(parms.containsKey("RANGES"))
                {
                    String oldName=httpReq.getRequestParameter("RANGENAME0");
                    String oldLow=null;
                    String oldHigh=null;
                    if((oldName==null)&&(F.ranges()!=null))
	        		    for(int v=0;v<F.ranges().size();v++)
	        		    {
            		        Faction.FactionRange FR=(Faction.FactionRange)F.ranges().elementAt(v);
	        		    	httpReq.addRequestParameters("RANGENAME"+v,FR.name());
	        		    	httpReq.addRequestParameters("RANGELOW"+v,""+FR.low());
	        		    	httpReq.addRequestParameters("RANGEHIGH"+v,""+FR.high());
	        		    }
                    
                    int num=0;
                    while(httpReq.getRequestParameter("RANGENAME"+num)!=null)
        		    {
                        oldName=httpReq.getRequestParameter("RANGENAME"+num);
                        if(oldName.length()>0)
                        {
	                        oldLow=httpReq.getRequestParameter("RANGELOW"+num);
	                        oldHigh=httpReq.getRequestParameter("RANGEHIGH"+num);
	                        if(CMath.s_int(oldHigh)<CMath.s_int(oldLow)) oldHigh=oldLow;
	        		        str.append("<TR><TD>");
	        		        str.append("<INPUT TYPE=TEXT NAME=RANGENAME"+num+" SIZE=20 VALUE=\""+oldName+"\">");
	        		        str.append("</TD><TD>");
	        		        str.append("<INPUT TYPE=TEXT NAME=RANGELOW"+num+" SIZE=8 VALUE=\""+oldLow+"\">");
	        		        str.append("</TD><TD>");
	        		        str.append("<INPUT TYPE=TEXT NAME=RANGEHIGH"+num+" SIZE=8 VALUE=\""+oldHigh+"\">");
	        		        str.append("</TD></TR>");
                        }
        		        num++;
        		    }
    		        str.append("<TR><TD>");
    		        str.append("<INPUT TYPE=TEXT NAME=RANGENAME"+num+" SIZE=20 VALUE=\"\">");
    		        str.append("</TD><TD>");
    		        str.append("<INPUT TYPE=TEXT NAME=RANGELOW"+num+" SIZE=8 VALUE=\"\">");
    		        str.append("</TD><TD>");
    		        str.append("<INPUT TYPE=TEXT NAME=RANGEHIGH"+num+" SIZE=8 VALUE=\"\">");
    		        str.append("</TD></TR>");
                }
                
                if(parms.containsKey("AUTOVALUES")
                || parms.containsKey("DEFAULTVALUES")
                || parms.containsKey("PLAYERCHOICES"))
                {
                    String prefix="";
                    Vector Fset=null;
                    if(parms.containsKey("AUTOVALUES"))
                    {
                        prefix="AUTOVALUE";
                        Fset=F.autoDefaults();
                    }
                    else
                    if(parms.containsKey("DEFAULTVALUES"))
                    {
                        prefix="DEFAULTVALUE";
                        Fset=F.defaults();
                    }
                    else
                    if(parms.containsKey("PLAYERCHOICES"))
                    {
                        prefix="PLAYERCHOICE";
                        Fset=F.defaults();
                    }
                        
                    
                    String value=httpReq.getRequestParameter(prefix+"0");
                    String mask="";
                    if((value==null)&&(Fset!=null))
                        for(int v=0;v<Fset.size();v++)
                        {
                            String def=(String)F.autoDefaults().elementAt(v);
                            int lastSp=0;
                            int spDex=def.indexOf(' ',lastSp+1);
                            int finalValue=-1;
                            while(spDex>0)
                            {
                                if(CMath.isInteger(def.substring(lastSp,spDex).trim()))
                                {
                                    finalValue=CMath.s_int(def.substring(lastSp,spDex).trim());
                                    def=def.substring(0,lastSp)+def.substring(spDex);
                                    break;
                                }
                                lastSp=spDex;
                                spDex=def.indexOf(' ',lastSp+1);
                            }
                            if((finalValue<0)&&CMath.isInteger(def.substring(lastSp).trim()))
                            {
                                finalValue=CMath.s_int(def.substring(lastSp).trim());
                                def=def.substring(0,lastSp);
                            }
                            httpReq.addRequestParameters(prefix+v,""+finalValue);
                            httpReq.addRequestParameters(prefix+"MASK"+v,def);
                        }
                    
                    int num=0;
                    while(httpReq.getRequestParameter(prefix+num)!=null)
                    {
                        mask=httpReq.getRequestParameter(prefix+num);
                        if(value.length()>0)
                        {
                            mask=httpReq.getRequestParameter(prefix+"MASK"+num);
                            str.append("<TR><TD>");
                            str.append("<INPUT TYPE=TEXT NAME="+prefix+num+" SIZE=8 VALUE=\""+CMath.s_int(value)+"\">");
                            str.append("</TD><TD>");
                            str.append("<INPUT TYPE=TEXT NAME="+prefix+"MASK"+num+" SIZE=60 MAXLENGTH=255 VALUE=\""+mask+"\">");
                            str.append("</TD></TR>");
                        }
                        num++;
                    }
                    str.append("<TR><TD>");
                    str.append("<INPUT TYPE=TEXT NAME="+prefix+num+" SIZE=8 VALUE=\"\">");
                    str.append("</TD><TD>");
                    str.append("<INPUT TYPE=TEXT NAME="+prefix+"MASK"+num+" SIZE=60 MAXLENGTH=255 VALUE=\"\">");
                    str.append("</TD></TR>");
                }
                
                if(parms.containsKey("ADJUSTMENTCHANGES"))
                {
                    
//TODO: default new faction change adjustment factors
// F.Changes();
                    /*
                     * 
                     * Faction.FactionChangeEvent
         Trigger choices:
             for(int i=0;i<Faction.FactionChangeEvent.MISC_TRIGGERS.length;i++) 
                 ALL_TYPES.append(Faction.FactionChangeEvent.MISC_TRIGGERS[i]+", ");
             for(int i=0;i<Ability.ACODE_DESCS.length;i++) 
                 ALL_TYPES.append(Ability.ACODE_DESCS[i]+", ");
             for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
                 ALL_TYPES.append(Ability.DOMAIN_DESCS[i]+", ");
             for(int i=0;i<Ability.FLAG_DESCS.length;i++) 
                 ALL_TYPES.append(Ability.FLAG_DESCS[i]+", ");
             _ALL_TYPES=ALL_TYPES.toString()+" a valid Skill, Spell, Chant, etc. ID.";
         Faction.FACTION_DIRECTIONS
         factor (the value)
         Faction.VALID_FLAGS (multi)
         ZapperMask    
                     */
                }
                if(parms.containsKey("ADJUSTMENTFACTORS"))
                {
//TODO: default new faction change adjustment factors
// F.factors();
                }
                if(parms.containsKey("FACTIONRELATIONS"))
                {
//TODO: default new faction relations
// F.relations();
                }
                if(parms.containsKey("FACTIONTRIGGERS"))
                {
//TODO: default new faction triggers
// F.Changes();
                }
                if(parms.containsKey("ABILITYALLOWANCES"))
                {
//TODO: default new ability allowances
// F.abilityUsages();
                }
                if(parms.containsKey("RATEMODIFIER"))
                {
                    String old=httpReq.getRequestParameter("RATEMODIFIER");
                    if(old==null) 
                        old=((int)Math.round(F.rateModifier()*100.0))+"%";
                    else
                        old=((int)Math.round(CMath.s_pct(old)*100.0))+"%";
                    str.append(old+", ");
                }
                if(parms.containsKey("AFFECTONEXP"))
                {
                    String old=httpReq.getRequestParameter("AFFECTONEXP");
                    if(old==null) old=F.experienceFlag();
                    for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
                    {
                        str.append("<OPTION VALUE=\""+Faction.EXPAFFECT_NAMES[i]+"\" ");
                        if(Faction.EXPAFFECT_NAMES[i].equalsIgnoreCase(old)) str.append("SELECTED");
                        str.append(">"+Faction.EXPAFFECT_DESCS[i]);
                    }
                    str.append(", ");
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