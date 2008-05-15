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
public class DeityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include description, worshipreq, clericreq,
	// worshiptrig, clerictrig, worshipsintrig,clericsintrig,powertrig

	private DVector getDeityData(ExternalHTTPRequests httpReq, String deityName)
	{
        DVector folData=(DVector)httpReq.getRequestObjects().get("DEITYDATAFOR-"+deityName.toUpperCase().trim());
        if(folData!=null) return folData;
        folData = CMLib.database().worshippers(deityName);
        httpReq.getRequestObjects().put("DEITYDATAFOR-"+deityName.toUpperCase().trim(),folData);
        return folData;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("DEITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Deity D=CMLib.map().getDeity(last);
			if(D!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("DESCRIPTION"))
					str.append(D.description()+", ");
                if(parms.containsKey("NAME"))
                    str.append(D.Name()+", ");
                if(parms.containsKey("LOCATION"))
                {
                    if(D.location()==null)
                        str.append("Nowhere, ");
                    else
                        str.append(CMLib.map().getExtendedRoomID(D.location())+": "+D.location().displayText()+", ");
                }
				if(parms.containsKey("WORSHIPREQ"))
					str.append(D.getWorshipRequirementsDesc()+", ");
				if(parms.containsKey("CLERICREQ"))
					str.append(D.getClericRequirementsDesc()+", ");
                if(parms.containsKey("SERVICETRIG"))
                    str.append(D.getServiceTriggerDesc()+", ");
				if(D.numCurses()>0)
				{
					if(parms.containsKey("WORSHIPSINTRIG"))
						str.append(D.getWorshipSinDesc()+", ");
					if(parms.containsKey("CLERICSINTRIG"))
						str.append(D.getClericSinDesc()+", ");
				}
				
				if(D.numPowers()>0)
				if(parms.containsKey("POWERTRIG"))
					str.append(D.getClericPowerupDesc()+", ");
				if(D.numBlessings()>0)
				{
					if(parms.containsKey("WORSHIPTRIG"))
						str.append(D.getWorshipTriggerDesc()+", ");
					if(parms.containsKey("CLERICTRIG"))
						str.append(D.getClericTriggerDesc()+", ");
				}
                if(parms.containsKey("NUMFOLLOWERS"))
                {
                    DVector data=getDeityData(httpReq,D.Name());
                    int num=data.size();
                    str.append(num+", ");
                }
				if(parms.containsKey("NUMPRIESTS"))
			    {
				    DVector data=getDeityData(httpReq,D.Name());
				    int num=0;
	                //DV.addElement(username, cclass, ""+level, race);
				    for(int d=0;d<data.size();d++)
				    {
				        CharClass C=CMClass.getCharClass((String)data.elementAt(d, 2));
				        if((C!=null)&&(C.baseClass().equalsIgnoreCase("CLERIC")))
				            num++;
				    }
				    str.append(num+", ");
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
