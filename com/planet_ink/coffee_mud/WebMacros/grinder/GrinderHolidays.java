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
public class GrinderHolidays {
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

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
        Vector stepV=(Vector)encodedData.elementAt(4);
        int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();
        
        
        
        return "";
    }
}
