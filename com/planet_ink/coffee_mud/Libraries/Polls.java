package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultPoll;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.IOException;
import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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

public class Polls extends StdLibrary implements PollManager
{
    public String ID(){return "Polls";}
    
    public Vector pollCache=null;
    public void unload(){pollCache=null;}
    
    public void addPoll(Poll P){if(getCache()!=null) getCache().addElement(P);}
    public void removePoll(Poll P){if(getCache()!=null) getCache().removeElement(P);}
    
    public synchronized Vector getCache()
    {
        if(CMSecurity.isDisabled("POLLCACHE")) 
            return null;
        if(pollCache==null) 
        {
            pollCache=new Vector();
            Vector list=CMLib.database().DBReadPollList();
            Vector V=null;
            Poll P=null;
            for(int l=0;l<list.size();l++)
            {
                V=(Vector)list.elementAt(l);
                P=(Poll)CMClass.getCommon("DefaultPoll");
                P.setName((String)V.firstElement());
                if(P.dbloadbyname())
                    pollCache.addElement(P);
            }
        }
        return pollCache;
    }
    public Poll getPoll(String named)
    {
        Vector V=getCache();
        if(V!=null)
        {
            Poll P=null;
            for(int c=0;c<V.size();c++)
            {
                P=(Poll)V.elementAt(c);
                if(P.getName().equalsIgnoreCase(named))
                    return P;
            }
        }
        else
        {
            Poll P=(Poll)CMClass.getCommon("DefaultPoll");
            P.setName(named);
            if(P.dbloadbyname())
                return P;
        }
        return null;
    }
    
    public Poll getPoll(int x)
    {
        if(x<0) return null;
        Vector V=getPollList();
        if(x<V.size())
        {
            Poll P=(Poll)V.elementAt(x);
            if((P.loaded())||(P.dbloadbyname()))
                return P;
        }
        return null;
    }
    
    public Vector[] getMyPolls(MOB mob, boolean login)
    {
        Vector V=getPollList();
        Vector list[]=new Vector[3];
        for(int l=0;l<3;l++)
            list[l]=new Vector();
        
        Poll P=null;
        for(int v=0;v<V.size();v++)
        {
            P=(Poll)V.elementAt(v);
            if((P.loaded())||(P.dbloadbyname()))
            {
                if((P.mayIVote(mob))&&(login)&&(CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN)))
                    list[1].addElement(P);
                else
                if(P.mayIVote(mob))
                    list[0].addElement(P);
                else
                if(P.mayISeeResults(mob))
                    list[2].addElement(P);
            }
        }
        return list;
    }
    
    public Vector getPollList()
    {
        Vector V=getCache();
        if(V!=null) 
            return ((Vector)V.clone());
        V=CMLib.database().DBReadPollList();
        Vector list=new Vector();
        Vector V2=null;
        Poll P=null;
        for(int v=0;v<V.size();v++)
        {
            V2=(Vector)V.elementAt(v);
            P=(Poll)CMClass.getCommon("DefaultPoll");
            P.setName((String)V2.elementAt(0));
            P.setFlags(((Long)V2.elementAt(1)).longValue());
            P.setQualZapper((String)V2.elementAt(2));
            P.setExpiration(((Long)V2.elementAt(3)).longValue());
            list.addElement(P);
        }
        return list;
    }
}
