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
        F.setShowInScore((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINFACTIONS");
        F.setShowInFactionsCommand((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINEDITOR");
        F.setShowInEditor((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("SHOWINREPORTS");
        F.setShowInSpecialReported((old!=null)&&(old.equalsIgnoreCase("on")));
        
        int num=0;
        for(Enumeration e=F.ranges();e.hasMoreElements();)
            F.delRange((Faction.FactionRange)e.nextElement());
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
                F.addRange(low+";"+high+";"+old+";"+code+";"+flag);
            }
            num++;
        }
        
        old=httpReq.getRequestParameter("PLAYERCHOICETEXT");
        F.setChoiceIntro(old==null?"":old);
        
        String[] prefixes={"AUTOVALUE","DEFAULTVALUE","PLAYERCHOICE"};
        for(int i=0;i<prefixes.length;i++)
        {
            String prefix=prefixes[i];
            Vector V=new Vector();
            switch(i)
            {
            case 0: F.setAutoDefaults(V); break;
            case 1: F.setDefaults(V); break;
            case 2: F.setChoices(V); break;
            }
            num=0;
            while(httpReq.getRequestParameter(prefix+num)!=null)
            {
                String value=httpReq.getRequestParameter(prefix+num);
                if(value.length()>0)
                {
                    String mask=httpReq.getRequestParameter(prefix+"MASK"+num);
                    V.addElement((CMath.s_long(value)+" "+mask).trim());
                }
                num++;
            }
        }
        
        F.clearChangeEvents();
        num=0;
        while(httpReq.getRequestParameter("CHANGESTRIGGER"+num)!=null)
        {
            old=httpReq.getRequestParameter("CHANGESTRIGGER"+num);
            if(old.length()>0)
            {
            	String ctparms=httpReq.getRequestParameter("CHANGESTPARM"+num);
            	if(ctparms.trim().length()>0)
            		old+="("+ctparms.trim()+")";
                old+=";";
                old+=Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CMath.s_int(httpReq.getRequestParameter("CHANGESDIR"+num))];
                old+=";";
                old+=CMath.toPct(httpReq.getRequestParameter("CHANGESFACTOR"+num));
                old+=";";
                String id="";
                int x=0;
                for(;httpReq.isRequestParameter("CHANGESFLAGS"+num+"_"+id);id=""+(++x))
                    old+=" "+httpReq.getRequestParameter("CHANGESFLAGS"+num+"_"+id).toUpperCase();
                old+=";";
                old+=httpReq.getRequestParameter("CHANGESMASK"+num);
                F.createChangeEvent(old);
            }
            num++;
        }
        
        for(Enumeration<Faction.FactionZapFactor> e=F.factors();e.hasMoreElements();)
            F.delFactor(e.nextElement());
        num=0;
        while(httpReq.getRequestParameter("ADJFACTOR"+num)!=null)
        {
            old=httpReq.getRequestParameter("ADJFACTOR"+num);
            if(old.length()>0)
            {
                double gain=CMath.s_pct(httpReq.getRequestParameter("ADJFACTORGAIN"+num));
                double loss=CMath.s_pct(httpReq.getRequestParameter("ADJFACTORLOSS"+num));
                F.addFactor(gain,loss,old);
            }
            num++;
        }
        
        num=0;
        for(Enumeration e=F.relationFactions();e.hasMoreElements();)
            F.delRelation((String)e.nextElement());
        while(httpReq.getRequestParameter("RELATIONS"+num)!=null)
        {
            old=httpReq.getRequestParameter("RELATIONS"+num);
            if(old.length()>0)
                F.addRelation(old,CMath.s_pct(httpReq.getRequestParameter("RELATIONSAMT"+num)));
            num++;
        }
        
        num=0;
        DVector affBehav=new DVector(3);
        HashSet affBehavKeepers=new HashSet();
        // its done this strange way to minimize impact on mob recalculations.
        while(httpReq.getRequestParameter("AFFBEHAV"+num)!=null)
        {
            old=httpReq.getRequestParameter("AFFBEHAV"+num);
            if(old.length()>0)
            {
                String parm=""+httpReq.getRequestParameter("AFFBEHAVPARM"+num);
                String mask=""+httpReq.getRequestParameter("AFFBEHAVMASK"+num);
                String[] oldParms=F.getAffectBehav(old);
                if((oldParms==null)||(!oldParms[0].equals(parm))||(!oldParms[1].equals(mask)))
                    affBehav.addElement(old.toUpperCase().trim(),parm,mask);
                else
                    affBehavKeepers.add(old.toUpperCase().trim());
            }
            num++;
        }
        for(Enumeration e=F.affectsBehavs();e.hasMoreElements();)
        {
            old=(String)e.nextElement();
            if(!affBehavKeepers.contains(old.toUpperCase().trim()))
                F.delAffectBehav(old);
        }
        for(int d=0;d<affBehav.size();d++)
        {
            F.delAffectBehav((String)affBehav.elementAt(d,1));
            F.addAffectBehav((String)affBehav.elementAt(d,1),(String)affBehav.elementAt(d,2),(String)affBehav.elementAt(d,3));
        }
        
        num=0;
        for(Enumeration e=F.abilityUsages();e.hasMoreElements();)
            F.delAbilityUsage((Faction.FactionAbilityUsage)e.nextElement());
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
                F.addAbilityUsage(old);
            }
            num++;
        }
        
        
        num=0;
        for(Enumeration e=F.reactions();e.hasMoreElements();)
            F.delReaction((Faction.FactionReactionItem)e.nextElement());
        while(httpReq.getRequestParameter("REACTIONRANGE"+num)!=null)
        {
            old=httpReq.getRequestParameter("REACTIONRANGE"+num);
            String old1=httpReq.getRequestParameter("REACTIONMASK"+num);
            String old2=httpReq.getRequestParameter("REACTIONABC"+num);
            String old3=httpReq.getRequestParameter("REACTIONPARM"+num);
            if(old.length()>0)
                F.addReaction(old,old1,old2,old3);
            num++;
        }
        old=httpReq.getRequestParameter("USELIGHTREACTIONS");
        F.setLightReactions((old!=null)&&(old.equalsIgnoreCase("on")));
        
        old=httpReq.getRequestParameter("RATEMODIFIER");
        F.setRateModifier(old==null?0.0:CMath.s_pct(old));

        old=httpReq.getRequestParameter("AFFECTONEXP");
        F.setExperienceFlag(old);
        
        return "";
    }
}
