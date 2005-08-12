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
public class StdClanCard extends StdClanItem
{
    public String ID(){ return "StdClanCard";}

    public StdClanCard()
    {
        super();

        setName("a clan membership card");
        baseEnvStats.setWeight(1);
        setDisplayText("a membership card belonging to a clan is here.");
        setDescription("");
        secretIdentity="";
        baseGoldValue=1;
        setCIType(ClanItem.CI_ANTIPROPAGANDA);
        material=EnvResource.RESOURCE_PAPER;
        recoverEnvStats();
    }

    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if((msg.target()==owner())
        &&(msg.tool() instanceof ClanItem)
        &&(owner() instanceof MOB)
        &&(((MOB)owner()).isMonster())
        &&(((ClanItem)msg.tool()).ciType()==ClanItem.CI_PROPAGANDA)
        &&(!((ClanItem)msg.tool()).clanID().equals(clanID()))
        &&(Sense.isInTheGame(owner(),true))
        &&(msg.source()!=owner())
        &&(Sense.isInTheGame(msg.source(),true)))
        {
            if(msg.source().location().show((MOB)msg.target(),msg.source(),msg.tool(),CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> reject(s) <O-NAME> from <T-NAME>."))
            {
                CommonMsgs.say((MOB)msg.target(),msg.source(),"How dare you!  Give me those!",false,true);
                if(msg.source().location().show((MOB)msg.target(),msg.source(),null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> takes(s) "+msg.tool().name()+" away from <T-NAME> and destroys it!"))
                {
                    Item I=null;
                    for(int i=msg.source().inventorySize();i>=0;i--)
                    {
                        I=msg.source().fetchInventory(i);
                        if((I instanceof ClanItem)
                        &&(I!=msg.tool())
                        &&(((ClanItem)I).clanID().equals(((ClanItem)msg.tool()).clanID())))
                            I.destroy();
                    }
                }
                return false;
            }
            
        }
        return super.okMessage(host,msg);
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((tickID==MudHost.TICK_CLANITEM)
        &&(owner() instanceof MOB)
        &&(clanID().length()>0)
        &&(((MOB)owner()).isMonster())
        &&(!Sense.isAnimalIntelligence((MOB)owner()))
        &&(((MOB)owner()).getStartRoom()!=null)
        &&(((MOB)owner()).location()!=null)
        &&(((MOB)owner()).getStartRoom().getArea()==((MOB)owner()).location().getArea()))
        {
            String rulingClan=null;
            Room R=((MOB)owner()).location();
            if(((MOB)owner()).getClanID().length()==0)
            {
                Behavior B=CoffeeUtensils.getLegalBehavior(R);
                if(B!=null)
                {
                    Vector V=new Vector();
                    V.addElement(new Integer(Law.MOD_RULINGCLAN));
                    rulingClan="";
                    if((B.modifyBehavior(CoffeeUtensils.getLegalObject(R),(MOB)owner(),V))
                    &&(V.size()>0)
                    &&(V.firstElement() instanceof String))
                        rulingClan=(String)V.firstElement();
                }
            }
            if((rulingClan!=null)&&(rulingClan.length()>0)
            &&(rulingClan.equals(clanID()))
            &&(!((MOB)owner()).getClanID().equals(rulingClan)))
                ((MOB)owner()).setClanID(rulingClan);
            
        }
        return true;
    }
}
