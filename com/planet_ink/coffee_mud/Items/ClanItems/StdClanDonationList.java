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

    private Item lastItem=null;
    
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
    
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((((ClanItem)this).clanID().length()>0)
        &&(Sense.isGettable(this))
        &&(msg.target()==this)
        &&(owner() instanceof Room))
        {
            Clan C=Clans.getClan(clanID());
            if((C!=null)&&(C.getDonation().length()>0))
            {
                Room R=CMMap.getRoom(C.getDonation());
                if(R==owner())
                {
                    Sense.setGettable(this,false);
                    text();
                }
            }
        }
        return super.okMessage(myHost,msg);
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        if(((ClanItem)this).clanID().length()>0)
        {
            if((msg.target()==this)
            &&(msg.targetMinor()==CMMsg.TYP_READSOMETHING))
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
                            boolean did=false;
                            for(int i=0;i<sorted.size();i++)
                                if(((Long)((Object[])sorted.elementAt(i))[0]).longValue()>val)
                                {
                                    did=true;
                                    Object[] O=new Object[2];
                                    O[0]=new Long(val);
                                    O[1]=(String)set.elementAt(3);
                                    sorted.insertElementAt(O,i);
                                }
                            if(!did) 
                            {
                                Object[] O=new Object[2];
                                O[0]=new Long(val);
                                O[1]=(String)set.elementAt(3);
                                sorted.addElement(O);
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
            &&(msg.target()!=lastItem)
            &&(msg.tool().ID().equalsIgnoreCase("Spell_ClanDonate")))
            {
                lastItem=(Item)msg.target();
                CMClass.DBEngine().DBCreateData(clanID(),"DONATIONS",System.currentTimeMillis()+"/"+msg.source().name()+"/"+Math.random(),msg.source().name()+" donated "+msg.target().name()+" at "+msg.source().location().getArea().getTimeObj().getShortTimeDescription()+".");
            }
            else
            if((msg.targetMinor()==CMMsg.TYP_GET)
            &&(msg.target() instanceof Item)
            &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE"))))
                CMClass.DBEngine().DBCreateData(clanID(),"DONATIONS",System.currentTimeMillis()+"/"+msg.source().name()+"/"+Math.random(),msg.source().name()+" gets "+msg.target().name()+" at "+msg.source().location().getArea().getTimeObj().getShortTimeDescription()+".");
            else
            if((msg.targetMinor()==CMMsg.TYP_DROP)
            &&(msg.target() instanceof Item)
            &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE"))))
                CMClass.DBEngine().DBCreateData(clanID(),"DONATIONS",System.currentTimeMillis()+"/"+msg.source().name()+"/"+Math.random(),msg.source().name()+" drops "+msg.target().name()+" at "+msg.source().location().getArea().getTimeObj().getShortTimeDescription()+".");
        }
        super.executeMsg(myHost,msg);
    }
}
