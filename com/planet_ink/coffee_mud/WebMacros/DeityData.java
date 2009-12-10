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
import java.net.URLEncoder;
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
                    if(D.getStartRoom()==null)
                        str.append("Nowhere, ");
                    else
                        str.append(CMLib.map().getExtendedRoomID(D.getStartRoom())+": "+D.getStartRoom().displayText()+", ");
                }
                if(parms.containsKey("AREA")&&(D.getStartRoom()!=null))
                    if(parms.containsKey("ENCODED"))
                        try {str.append(URLEncoder.encode(D.getStartRoom().getArea().Name(),"UTF-8")+", ");}catch(Exception e){}
                    else
                        str.append(D.getStartRoom().getArea().Name()+", ");
                if(parms.containsKey("ROOM")&&(D.getStartRoom()!=null))
                    if(parms.containsKey("ENCODED"))
                        try {str.append(URLEncoder.encode(D.getStartRoom().roomID(),"UTF-8")+", ");}catch(Exception e){}
                    else
                        str.append(D.getStartRoom().roomID()+", ");
                if(parms.containsKey("MOBCODE"))
                {
                    String roomID=D.getStartRoom().roomID();
                    Vector classes=(Vector)httpReq.getRequestObjects().get("DEITYLIST-"+roomID);
                    if(classes==null)
                    {
                        classes=new Vector();
                        Room R=(Room)httpReq.getRequestObjects().get(roomID);
                        if(R==null)
                        {
                            R=CMLib.map().getRoom(roomID);
                            if(R==null)
                                return "No Room?!";
                            Vector restoreDeities = new Vector();
                            for(Enumeration e=CMLib.map().deities();e.hasMoreElements();)
                            {
                            	Deity D2 = (Deity)e.nextElement();
                            	if((D2.getStartRoom()!=null)
                            	&&(CMLib.map().getExtendedRoomID(D2.getStartRoom()).equalsIgnoreCase(CMLib.map().getExtendedRoomID(R))))
                            		restoreDeities.addElement(D2);
                            }
                            CMLib.map().resetRoom(R);
                            R=CMLib.map().getRoom(roomID);
                            for(int d=restoreDeities.size()-1;d>=0;d--)
                            {
                            	Deity D2=(Deity)restoreDeities.elementAt(d);
                            	if(CMLib.map().getDeity(D2.Name())!=null)
                            		restoreDeities.removeElementAt(d);
                            }
                            for(Enumeration e=restoreDeities.elements();e.hasMoreElements();)
                            {
                            	Deity D2=(Deity)e.nextElement();
                            	for(int i=0;i<R.numInhabitants();i++)
                            	{
                            		MOB M=R.fetchInhabitant(i);
                            		if((M instanceof Deity)
                            		&&(M.Name().equals(D2.Name())))
                            			CMLib.map().addDeity((Deity)M);
                            	}
                            }
                            httpReq.getRequestObjects().put(roomID,R);
                            D=CMLib.map().getDeity(last);
                        }
                        synchronized(("SYNC"+roomID).intern())
                        {
                            R=CMLib.map().getRoom(R);
                            for(int m=0;m<R.numInhabitants();m++)
                            {
                                MOB M=R.fetchInhabitant(m);
                                if(M.savable())
                                    classes.addElement(M);
                            }
                            RoomData.contributeMOBs(classes);
                        }
                        httpReq.getRequestObjects().put("DEITYLIST-"+roomID,classes);
                    }
                    if(parms.containsKey("ENCODED"))
                        try {str.append(URLEncoder.encode(RoomData.getMOBCode(classes,D),"UTF-8")+", ");}catch(Exception e){}
                    else
                        str.append(RoomData.getMOBCode(classes,D)+", ");
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
