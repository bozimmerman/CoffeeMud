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
public class GrinderHolidays {
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}


    protected static String setText(DVector sets, String var, String newVAL)
    {
        if(newVAL==null) newVAL="";
        //var=var.toUpperCase().trim();
        int index=sets.indexOf(var);
        String oldVal=index>=0?(String)sets.elementAt(index,2):"";
        if(index>=0)
        {
            if(!newVAL.equals(oldVal))
                sets.setElementAt(index,2,newVAL);
        }
        else
            sets.addElement(var,newVAL,Integer.valueOf(-1));
        return newVAL;
    }


    public static String createModifyHoliday(ExternalHTTPRequests httpReq, Hashtable parms, String holidayName)
    {
        int index=CMLib.quests().getHolidayIndex(holidayName);
        if(index<=0)
        {
            String err = CMLib.quests().createHoliday(holidayName,"ALL",true);
            if((err != null) && (err.trim().length()>0))
                return err;
            index=CMLib.quests().getHolidayIndex(holidayName);
            if(index < 0)
                return "Error creating holiday file.";
        }
        Vector steps=null;
        Vector encodedData = null;
        Object resp=CMLib.quests().getHolidayFile();
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
        if(resp instanceof String)
            return (String)resp;
        if(steps!=null)
            encodedData=CMLib.quests().getEncodedHolidayData((String)steps.elementAt(index));
        if(encodedData==null)
            return "Error reading holiday data (code: "+((resp instanceof Vector)?"T":"F")+":"+((steps==null)?"F":"T")+":"+((encodedData==null)?"F":"T")+")";
        DVector settings=(DVector)encodedData.elementAt(0);
        DVector behaviors=(DVector)encodedData.elementAt(1);
        DVector properties=(DVector)encodedData.elementAt(2);
        DVector stats=(DVector)encodedData.elementAt(3);
        //Vector stepV=(Vector)encodedData.elementAt(4);
        //int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();

        String name=setText(settings,"NAME",httpReq.getRequestParameter("NAME"));
        if((name==null)||(name.trim().length()==0)) return "A name is required.";

        String duration=setText(settings,"DURATION",httpReq.getRequestParameter("DURATION"));
        if((duration==null)||(!CMath.isMathExpression(duration))) return "Duration is mal-formed.";

        if(!httpReq.isRequestParameter("SCHEDULETYPE")) return "Schedule not found.";
        int typeIndex=CMath.s_int(httpReq.getRequestParameter("SCHEDULETYPE"));
        int mudDayIndex=settings.indexOf("MUDDAY");
        int dateIndex=settings.indexOf("DATE");
        int waitIndex=settings.indexOf("WAIT");
        String scheduleName=new String[]{"WAIT","MUDDAY","DATE"}[typeIndex];
        if((typeIndex!=0)&&(waitIndex>=0))
            settings.removeElement("WAIT");
        if((typeIndex!=1)&&(mudDayIndex>=0))
            settings.removeElement("MUDDAY");
        if((typeIndex!=2)&&(dateIndex>=0))
            settings.removeElement("DATE");
        String newWait = setText(settings,scheduleName,httpReq.getRequestParameter(scheduleName));
        switch(typeIndex)
        {
        case 0: {
            if(!CMath.isMathExpression(newWait))
                return "Wait expression is invalid.";
            break;
            }
        case 1:
        case 2: {
            int dash=newWait.indexOf('-');
            if(dash < 0) return "Given date is invalid. Use Month#-Day# format";
            if(!CMath.isInteger(newWait.substring(0,dash).trim()))
                return "Month value in the given date is not valid.";
            if(!CMath.isInteger(newWait.substring(dash+1).trim()))
                return "Day value in the given date is not valid.";
            break;
            }
        }

        StringBuffer areaGroup = new StringBuffer("");
        HashSet areaCodes=new HashSet();
        String id="";
        for(int i=0;httpReq.isRequestParameter("AREAGROUP"+id);id=Integer.toString(++i))
            areaCodes.add(httpReq.getRequestParameter("AREAGROUP"+id));
        if(areaCodes.contains("AREAGROUP1"))
            areaGroup.append("ANY");
        else
        {
            int areaNum=2;
            boolean reallyAll=true;
            for(Enumeration e=CMLib.map().areas();e.hasMoreElements();areaNum++)
                if(areaCodes.contains("AREAGROUP"+areaNum))
                    areaGroup.append(" \"" + ((Area)e.nextElement()).Name()+"\"");
                else
                {
                    reallyAll=false;
                    e.nextElement();
                }
            if(reallyAll)
                areaGroup.setLength(0);
        }

        setText(settings,"AREAGROUP",areaGroup.toString().trim());
        setText(settings,"MOBGROUP",httpReq.getRequestParameter("MOBGROUP"));

        behaviors.clear();
        setText(behaviors,"AGGRESSIVE",httpReq.getRequestParameter("AGGRESSIVE"));
        for(int i=1;httpReq.isRequestParameter("BEHAV"+i);i++)
            if(httpReq.getRequestParameter("BEHAV"+i).trim().length()>0)
                setText(behaviors,httpReq.getRequestParameter("BEHAV"+i),httpReq.getRequestParameter("BDATA"+i));
        StringBuffer mudChats=new StringBuffer("");
        for(int i=1;httpReq.isRequestParameter("MCWDS"+i);i++)
        {
            String words=httpReq.getRequestParameter("MCWDS"+i).trim();
            words=CMStrings.replaceAll(words,",","|");
            if((words.length()>0)&&(httpReq.isRequestParameter("MCSAYS"+i+"_1")))
            {
                mudChats.append("("+words+");");
                for(int ii=1;httpReq.isRequestParameter("MCSAYW"+i+"_"+ii);ii++)
                    if(CMath.isInteger(httpReq.getRequestParameter("MCSAYW"+i+"_"+ii)))
                        mudChats.append(httpReq.getRequestParameter("MCSAYW"+i+"_"+ii)+httpReq.getRequestParameter("MCSAYS"+i+"_"+ii)+";");
                mudChats.append(";");
            }
        }
        setText(behaviors,"MUDCHAT",mudChats.toString());

        properties.clear();
        setText(properties,"MOOD",httpReq.getRequestParameter("MOOD"));
        for(int i=1;httpReq.isRequestParameter("AFFECT"+i);i++)
            if(httpReq.getRequestParameter("AFFECT"+i).trim().length()>0)
                setText(properties,httpReq.getRequestParameter("AFFECT"+i),httpReq.getRequestParameter("ADATA"+i));


        Vector priceFV=new Vector();
        for(int i=1;httpReq.isRequestParameter("PRCFAC"+i);i++)
            if(CMath.isPct(httpReq.getRequestParameter("PRCFAC"+i).trim()))
                priceFV.add(((String)(CMath.s_pct(httpReq.getRequestParameter("PRCFAC"+i).trim())+" "+httpReq.getRequestParameter("PMASK"+i).trim())).trim());
        setText(stats,"PRICEMASKS",CMParms.toStringList(priceFV));

        String err=CMLib.quests().alterHoliday(holidayName, encodedData);
        if(err.length()==0)
            httpReq.addRequestParameters("HOLIDAY",name);
        return err;
    }
}
