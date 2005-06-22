package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class StdClanDonationList extends StdClanItem
{

    public StdClanDonationList()
    {
        super();
        setName("a donation list");
        baseEnvStats.setWeight(1);
        setDisplayText("an list is setting here.");
        setDescription("");
        setCIType(ClanItem.CI_DONATEJOURNAL);
        Sense.setReadable(this,true);
        secretIdentity="";
        baseGoldValue=1;
        material=EnvResource.RESOURCE_PAPER;
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
    
        if(((ClanItem)myHost).clanID().length()>0)
        {
            if((msg.target()==this)&&(msg.targetMinor()==CMMsg.TYP_READSOMETHING))
            {
                MOB mob=msg.source();
                if(Sense.canBeSeenBy(this,mob))
                {
                    StringBuffer text=new StringBuffer("");
                    Vector V=CMClass.DBEngine().DBReadData(clanID(),"DONATIONS");
                    Vector sorted=new Vector();
                    String key=null;
                    int x=0;
                    long val=0;
                    Vector set=null;
                    while(V.size()>0)
                    {
                        set=(Vector)V.firstElement();
                        key=(String)set.elementAt(2);
                        x=key.indexOf("/");
                        if(x>0)
                        {
                            val=Util.s_long(key.substring(0,x));
                            for(int i=0;i<sorted.size();i++)
                                if(((Long)((Object[])sorted.elementAt(i))[0]).longValue()>val)
                                {
                                    Object[] O=new Object[2];
                                    O[0]=new Long(val);
                                    O[1]=(String)set.elementAt(3);
                                    sorted.insertElementAt(O,i);
                                }
                        }
                        V.removeElementAt(0);
                    }
                    for(int i=0;i<sorted.size();i++)
                        text.append(((String)((Object[])sorted.elementAt(i))[1])+"\n\r");
                    if(text.length()>0)
                        mob.tell("It says '"+text.toString()+"'.");
                    else
                        mob.tell("There is nothing written on "+name()+".");
                }
                else
                    mob.tell("You can't see that!");
                return;
            }
            else
            if((msg.target() instanceof Item)
            &&(msg.tool() instanceof Ability)
            &&(msg.tool().ID().equalsIgnoreCase("Spell_ClanDonate")))
                CMClass.DBEngine().DBCreateData(clanID(),"DONATIONS",System.currentTimeMillis()+"/"+msg.source().name()+"/"+Math.random(),msg.source().name()+" donated "+msg.target().name()+" on "+msg.source().location().getArea().getTimeObj().timeDescription(msg.source(),msg.source().location())+".");
        }
        super.executeMsg(myHost,msg);
    }
}
