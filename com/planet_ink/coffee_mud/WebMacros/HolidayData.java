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
public class HolidayData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("HOLIDAY");
        if(last==null) return " @break@";
        if(last.length()>0)
        {
            int index=CMLib.quests().getHolidayIndex(last);
            Vector encodedData=null;
            Object resp=CMLib.quests().getHolidayFile();
            Vector steps=null;
            if(resp instanceof Vector)
                steps=(Vector)resp;
            if((index>=0)&&(steps!=null)) 
                encodedData=CMLib.quests().getEncodedHolidayData((String)steps.elementAt(index));
            if(encodedData!=null)
            {
                DVector settings=(DVector)encodedData.elementAt(0);
                DVector behaviors=(DVector)encodedData.elementAt(1);
                DVector properties=(DVector)encodedData.elementAt(2);
                DVector stats=(DVector)encodedData.elementAt(3);
                Vector stepV=(Vector)encodedData.elementAt(4);
                int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();
                
                StringBuffer str=new StringBuffer("");
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null)
                    {
                        int dex=settings.indexOf("NAME");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("AREAGROUP"))
                {
                    String old=httpReq.getRequestParameter("AREAGROUP");
                    if(old==null)
                    {
                        int dex=settings.indexOf("AREAGROUP");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("MOBGROUP"))
                {
                    String old=httpReq.getRequestParameter("MOBGROUP");
                    if(old==null)
                    {
                        int dex=settings.indexOf("MOBGROUP");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("MOOD"))
                {
                    String old=httpReq.getRequestParameter("MOOD");
                    if(old==null)
                    {
                        int dex=behaviors.indexOf("MOOD");
                        if(dex>=0)
                            old=(String)behaviors.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("AGGRESSIVE"))
                {
                    String old=httpReq.getRequestParameter("AGGRESSIVE");
                    if(old==null)
                    {
                        int dex=behaviors.indexOf("AGGRESSIVE");
                        if(dex>=0)
                            old=(String)behaviors.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("DURATION"))
                {
                    String old=httpReq.getRequestParameter("DURATION");
                    if(old==null)
                    {
                        int dex=settings.indexOf("DURATION");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="900";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("SCHEDULETYPE")||parms.containsKey("SCHEDULETYPEID"))
                {
                    String old=httpReq.getRequestParameter("SCHEDULETYPE");
                    if(old==null) old=httpReq.getRequestParameter("SCHEDULETYPEID");
                    final String[] TYPES={"RANDOM INTERVAL","MUD-DAY","RL-DAY"};
                    if(old==null)
                    {
                        int mudDayIndex=settings.indexOf("MUDDAY");
                        int dateIndex=settings.indexOf("DATE");
                        if(mudDayIndex>=0) 
                            old=TYPES[1];
                        else
                        if(dateIndex>=0) 
                            old=TYPES[2];
                        else
                            old=TYPES[0];
                    }
                    if(parms.containsKey("SCHEDULETYPEID"))
                    for(int i=0;i<TYPES.length;i++)
                        str.append("<OPTION VALUE="+i+" "+(old.equalsIgnoreCase(TYPES[i])?"SELECTED":"")+">"+TYPES[i]);
                    else
                        str.append(old);
                    str.append(", ");
                }
                /*
                    showNumber=promptDuration(mob,settings,showNumber,showFlag);
                    showNumber=genPricing(mob,stats,++showNumber,showFlag);
                    showNumber=genMudChat(mob,"MUDCHAT",behaviors,++showNumber,showFlag);
                    showNumber=genBehaviors(mob,behaviors,++showNumber,showFlag);
                    showNumber=genProperties(mob,properties,++showNumber,showFlag);
                 */
                String strstr=str.toString();
                if(strstr.endsWith(", "))
                    strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
            }
        }
        return "";
    }
}