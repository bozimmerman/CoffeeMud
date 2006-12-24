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
   Copyright 2000-2006 Bo Zimmerman

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
//TODO: ranges                    
                }
                if(parms.containsKey("AUTOVALUES"))
                {
//TODO: autodefaults
// F.autoDefaults();
                }
                if(parms.containsKey("DEFAULTVALUES"))
                {
//TODO: non-auto default values                    
// F.defaults();
                }
                if(parms.containsKey("PLAYERCHOICES"))
                {
//TODO: default new player choices
// F.choices();
                }
                if(parms.containsKey("PLAYERCHOICES"))
                {
//TODO: default new player choices
// F.choices();
                }
                if(parms.containsKey("ADJUSTMENTFACTORS"))
                {
//TODO: default new faction change adjustment factors
// F.Changes();
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