package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

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
public class DefaultArrestWarrant implements LegalWarrant
{
    public String ID(){return "DefaultArrestWarrant";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultArrestWarrant();}}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public void initializeClass(){}
    public CMObject copyOf()
    {
        try
        {
            return (DefaultArrestWarrant)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return newInstance();
        }
    }
    
    private MOB criminal=null;
    private MOB victim=null;
    private MOB witness=null;
    private MOB arrestingOfficer=null;
    private Room jail=null;
    private Room releaseRoom=null;
    private String crime="";
    private DVector punishmentParms=new DVector(2);
    private int punishment=-1;
    private int jailTime=0;
    private int state=0;
    private int offenses=0;
    private long lastOffense=0;
    private long travelAttemptTime=0;
    private String warnMsg=null;
    public void setArrestingOfficer(Area legalArea, MOB mob)
    {
        if((arrestingOfficer!=null)
        &&(arrestingOfficer.getStartRoom()!=null)
        &&(arrestingOfficer.location()!=null)
        &&(legalArea!=null)
        &&(arrestingOfficer.getStartRoom().getArea()!=arrestingOfficer.location().getArea())
        &&(!legalArea.inMyMetroArea(arrestingOfficer.location().getArea())))
            CMLib.tracking().wanderAway(arrestingOfficer,true,true);
        if((mob==null)&&(arrestingOfficer!=null))
            CMLib.tracking().stopTracking(arrestingOfficer);
        arrestingOfficer=mob;
    }
    public MOB criminal(){ return criminal;}
    public MOB victim() { return victim;}
    public MOB witness(){ return witness;}
    public MOB arrestingOfficer(){ return arrestingOfficer;}
    public Room jail(){ return CMLib.map().getRoom(jail);}
    public Room releaseRoom(){ return CMLib.map().getRoom(releaseRoom);}
    public String crime(){ return crime;}
    public int punishment(){ return punishment;}
    public String getPunishmentParm(int code)
    {
        int index=punishmentParms.indexOf(Integer.valueOf(code));
        if(index<0) return "";
        return (String)punishmentParms.elementAt(index,2);
    }
    public void addPunishmentParm(int code, String parm)
    {
        int index=punishmentParms.indexOf(Integer.valueOf(code));
        if(index>=0)
            punishmentParms.removeElementAt(index);
        punishmentParms.addElement(Integer.valueOf(code),parm);
    }
    public int jailTime(){ return jailTime;}
    public int state(){ return state;}
    public int offenses(){ return offenses;}
    public long lastOffense(){ return lastOffense;}
    public long travelAttemptTime(){ return travelAttemptTime;}
    public String warnMsg(){ return warnMsg;}
    public void setCriminal(MOB mob){ criminal=mob;}
    public void setVictim(MOB mob){ victim=mob;}
    public void setWitness(MOB mob){ witness=mob;}
    public void setJail(Room R){ jail=R;}
    public void setReleaseRoom(Room R){ releaseRoom=R;}
    public void setCrime(String newcrime){ crime=newcrime;}
    public void setPunishment(int code){ punishment=code;}
    public void setJailTime(int time){ jailTime=time;}
    public void setState(int newstate){ state=newstate;}
    public void setOffenses(int num){ offenses=num;}
    public void setLastOffense(long last){ lastOffense=last;}
    public void setTravelAttemptTime(long time){ travelAttemptTime=time;}
    public void setWarnMsg(String msg){ warnMsg=msg;}
}
