package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class GrinderFactions {
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    
    public static String modifyFaction(ExternalHTTPRequests httpReq, Hashtable parms, Faction F)
    {
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
        String old;
        
        old=httpReq.getRequestParameter("NAME");
        F.setName(old==null?"NAME":old);
        
        old=httpReq.getRequestParameter("SHOWINSCORE");
        F.setShowinscore((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINFACTIONS");
        F.setShowinfactionscommand((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINEDITOR");
        F.setShowineditor((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINREPORTS");
        F.setShowinspecialreported((old!=null)&&(old.equalsIgnoreCase("on")));
        
        int num=0;
        F.ranges().clear();
        while(httpReq.getRequestParameter("RANGENAME"+num)!=null)
        {
            old=httpReq.getRequestParameter("RANGENAME"+num);
            String code=httpReq.getRequestParameter("RANGECODE"+num);
            if(old.length()>0)
            {
                if(code.length()==0) 
                    code=CMStrings.replaceAll(old.toUpperCase().trim()," ","_");
                int low=CMath.s_int(httpReq.getRequestParameter("RANGELOW"+num));
                int high=CMath.s_int(httpReq.getRequestParameter("RANGEHIGH"+num));
                if(high<low) high=low;
                String flag=httpReq.getRequestParameter("RANGEFLAG"+num);
                F.ranges().addElement(F.newRange(low+";"+high+";"+old+";"+code+";"+flag));
            }
            num++;
        }
        
        old=httpReq.getRequestParameter("PLAYERCHOICETEXT");
        F.setChoiceIntro(old==null?"":old);
        
        String[] prefixes={"AUTOVALUE","DEFAULTVALUE","PLAYERCHOICE"};
        for(int i=0;i<prefixes.length;i++)
        {
            String prefix=prefixes[i];
            switch(i)
            {
            case 0: F.autoDefaults().clear(); break;
            case 1: F.defaults().clear(); break;
            case 2: F.choices().clear(); break;
            }
            num=0;
            while(httpReq.getRequestParameter(prefix+num)!=null)
            {
                String value=httpReq.getRequestParameter(prefix+num);
                if(value.length()>0)
                {
                    String mask=httpReq.getRequestParameter(prefix+"MASK"+num);
                    switch(i)
                    {
                    case 0: F.autoDefaults().addElement((CMath.s_long(value)+" "+mask).trim()); break;
                    case 1: F.defaults().addElement((CMath.s_long(value)+" "+mask).trim()); break;
                    case 2: F.choices().addElement((CMath.s_long(value)+" "+mask).trim()); break;
                    }
                }
                num++;
            }
        }
        
        F.Changes().clear();
        num=0;
        while(httpReq.getRequestParameter("CHANGESTRIGGER"+num)!=null)
        {
            old=httpReq.getRequestParameter("CHANGESTRIGGER"+num);
            if(old.length()>0)
            {
                old+=";";
                old+=CMath.s_int(httpReq.getRequestParameter("CHANGESDIR"+num));
                old+=";";
                old+=(CMath.s_pct(httpReq.getRequestParameter("CHANGESFACTOR"+num))*100.0)+"%";
                old+=";";
                String id="";
                int x=0;
                for(;httpReq.isRequestParameter("CHANGESFLAGS"+num+"_"+id);id=""+(++x))
                    old+=" "+httpReq.getRequestParameter("CHANGESFLAGS"+num+"_"+id).toUpperCase();
                old+=";";
                old+=httpReq.getRequestParameter("CHANGESMASK"+num);
                FactionChangeEvent FC=F.newChangeEvent(old);
                F.Changes().put(FC.eventID().toUpperCase(),FC);
            }
            num++;
        }
        
        F.factors().clear();
        num=0;
        while(httpReq.getRequestParameter("ADJFACTOR"+num)!=null)
        {
            old=httpReq.getRequestParameter("ADJFACTOR"+num);
            if(old.length()>0)
            {
                String gain=""+CMath.s_pct(httpReq.getRequestParameter("ADJFACTORGAIN"+num));
                String loss=""+CMath.s_pct(httpReq.getRequestParameter("ADJFACTORLOSS"+num));
                F.factors().addElement(CMParms.makeVector(gain,loss,old));
            }
            num++;
        }
        
        num=0;
        F.relations().clear();
        while(httpReq.getRequestParameter("RELATIONS"+num)!=null)
        {
            old=httpReq.getRequestParameter("RELATIONS"+num);
            if(old.length()>0)
                F.relations().put(old,
                                  new Double(CMath.s_pct(httpReq.getRequestParameter("RELATIONSAMT"+num))));
            num++;
        }
        
        num=0;
        F.abilityUsages().clear();
        while(httpReq.getRequestParameter("ABILITYUSE"+num)!=null)
        {
            old=httpReq.getRequestParameter("ABILITYUSE"+num);
            if(old.length()>0)
            {
                int usedType=CMLib.factions().getAbilityFlagType(old);
                if(usedType>0)
                {
                    int x=-1;
                    int sx=-1;
                    while(httpReq.isRequestParameter("ABILITYUSE"+num+"_"+(++x)))
                    {
                        String s=httpReq.getRequestParameter("ABILITYUSE"+num+"_"+x);
                        if(s.length()>0)
                        {
                            old+=" "+s.toUpperCase().trim();
                            ++sx;
                        }
                    }
                    ++sx;
                }
                old+=";"+CMath.s_int(httpReq.getRequestParameter("ABILITYMIN"+num));
                old+=";"+CMath.s_int(httpReq.getRequestParameter("ABILITYMAX"+num));
                F.abilityUsages().addElement(F.newAbilityUsage(old));
            }
            num++;
        }
        
        old=httpReq.getRequestParameter("RATEMODIFIER");
        F.setRateModifier(old==null?0.0:CMath.s_pct(old));

        old=httpReq.getRequestParameter("AFFECTONEXP");
        F.setExperienceFlag(old);
        
        return "";
    }
}
