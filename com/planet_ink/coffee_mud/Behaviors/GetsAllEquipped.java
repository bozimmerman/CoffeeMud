package com.planet_ink.coffee_mud.Behaviors;
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
public class GetsAllEquipped extends ActiveTicker
{
	public String ID(){return "GetsAllEquipped";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public GetsAllEquipped()
	{
        super();
		maxTicks=5;minTicks=10;chance=100;
		tickReset();
	}

	protected boolean DoneEquipping=false;

	public void executeMsg(Environmental host, CMMsg msg)
    {
	    super.executeMsg(host,msg);
        if((msg.sourceMinor()==CMMsg.TYP_DEATH)
        &&(msg.source()!=host)
        &&(msg.source().location()!=CMLib.map().roomLocation(host)))
            DoneEquipping=false;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			if(DoneEquipping)
				return true;

			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;

			DoneEquipping=true;
            Vector stuffIHad=new Vector();
            for(int i=0;i<mob.inventorySize();i++)
                stuffIHad.addElement(mob.fetchInventory(i));
            mob.enqueCommand(CMParms.makeVector("GET","ALL"),Command.METAFLAG_FORCED,0);
            Item I=null;
            Vector dropThisStuff=new Vector();
            for(int i=0;i<mob.inventorySize();i++)
            {
                I=mob.fetchInventory(i);
                if((I!=null)&&(!stuffIHad.contains(I)))
                {
                    if(I instanceof DeadBody)
                        dropThisStuff.addElement(I);
                    else
                    if((I.container()!=null)&&(I.container() instanceof DeadBody))
                        I.setContainer(null);
                }
            }
            for(int d=0;d<dropThisStuff.size();d++)
                mob.enqueCommand(CMParms.makeVector("DROP",((Item)dropThisStuff.elementAt(d)).Name()),Command.METAFLAG_FORCED,0);
			mob.enqueCommand(CMParms.makeVector("WEAR","ALL"),Command.METAFLAG_FORCED,0);
		}
		return true;
	}
}
