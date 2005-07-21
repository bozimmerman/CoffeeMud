package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;

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
public class TrainingSession extends StdItem implements MiscMagic
{
    public String ID(){ return "TrainingSession";}
    public TrainingSession()
    {
        super();
        setName("a training session");
        setDisplayText("A shiny gold coin has been left here.");
        myContainer=null;
        setDescription("A shiny gold coin with magical script around the edges.");
        myUses=Integer.MAX_VALUE;
        myWornCode=0;
        material=0;
        baseEnvStats.setWeight(0);
        recoverEnvStats();
    }


    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        if(msg.amITarget(this))
        {
            MOB mob=msg.source();
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GET:
            case CMMsg.TYP_REMOVE:
            {
                setContainer(null);
                destroy();
                if(!mob.isMine(this))
                    mob.setTrains(mob.getTrains()+1);
                unWear();
                if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
                    mob.location().recoverRoomStats();
                return;
            }
            default:
                break;
            }
        }
        super.executeMsg(myHost,msg);
    }
}