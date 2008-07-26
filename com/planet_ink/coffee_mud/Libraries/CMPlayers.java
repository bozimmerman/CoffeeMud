package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
    public String ID(){return "CMPlayers";}
    public Vector playersList = new Vector();
    
    public int numPlayers() { return playersList.size(); }
    public void addPlayer(MOB newOne)
    {
        synchronized(playersList)
        {
            if(getPlayer(newOne.Name())!=null) return;
            if(playersList.contains(newOne)) return;
            playersList.add(newOne);
        }
    }
    public void delPlayer(MOB oneToDel) { synchronized(playersList){playersList.remove(oneToDel);} }
    public MOB getPlayer(String calledThis)
    {
        MOB M = null;
        synchronized(playersList)
        {
            for (Enumeration p=players(); p.hasMoreElements();)
            {
                M = (MOB)p.nextElement();
                if (M.Name().equalsIgnoreCase(calledThis))
                    return M;
            }
        }
        return null;
    }

    public MOB getLoadPlayer(String last)
    {
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
            return null;
        MOB M=null;
        synchronized(playersList)
        {
            M=getPlayer(last);
            if(M!=null) return M;

            for(Enumeration p=players();p.hasMoreElements();)
            {
                MOB mob2=(MOB)p.nextElement();
                if(mob2.Name().equalsIgnoreCase(last))
                { return mob2;}
            }

            MOB TM=CMClass.getMOB("StdMOB");
            if(CMLib.database().DBUserSearch(TM,last))
            {
                M=CMClass.getMOB("StdMOB");
                M.setName(TM.Name());
                CMLib.database().DBReadPlayer(M);
                CMLib.database().DBReadFollowers(M,false);
                if(M.playerStats()!=null)
                    M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
                M.recoverEnvStats();
                M.recoverCharStats();
            }
            TM.destroy();
        }
        return M;
    }

    public Enumeration players() { return DVector.s_enum(playersList); }

    public void unLoad()
    {
        playersList.clear();
    }
    
    public void obliteratePlayer(MOB deadMOB, boolean quiet)
    {
        if(getPlayer(deadMOB.Name())!=null)
        {
           deadMOB=getPlayer(deadMOB.Name());
           delPlayer(deadMOB);
        }
        for(int s=0;s<CMLib.sessions().size();s++)
        {
            Session S=CMLib.sessions().elementAt(s);
            if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
               deadMOB=S.mob();
        }
        CMMsg msg=CMClass.getMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:"A horrible death cry is heard throughout the land.");
        Room deadLoc=deadMOB.location();
        if(deadLoc!=null)
            deadLoc.send(deadMOB,msg);
        try
        {
            for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                if((R!=null)&&(R!=deadLoc))
                {
                    if(R.okMessage(deadMOB,msg))
                        R.sendOthers(deadMOB,msg);
                    else
                    {
                        addPlayer(deadMOB);
                        return;
                    }
                }
            }
        }catch(NoSuchElementException e){}
        StringBuffer newNoPurge=new StringBuffer("");
        Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        boolean somethingDone=false;
        if((protectedOnes!=null)&&(protectedOnes.size()>0))
        {
            for(int b=0;b<protectedOnes.size();b++)
            {
                String B=(String)protectedOnes.elementAt(b);
                if(!B.equalsIgnoreCase(deadMOB.name()))
                    newNoPurge.append(B+"\n");
                else
                    somethingDone=true;
            }
            if(somethingDone)
            {
                Resources.updateResource("protectedplayers.ini",newNoPurge);
                Resources.saveFileResource("::protectedplayers.ini");
            }
        }

        CMLib.database().DBDeleteMOB(deadMOB);
        if(deadMOB.session()!=null)
            deadMOB.session().setKillFlag(true);
        Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
        deadMOB.destroy();
    }
}
