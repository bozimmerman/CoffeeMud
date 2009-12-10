package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdCageRideable extends StdRideable
{
	public String ID(){	return "StdCageRideable";}
	public StdCageRideable()
	{
		super();
		setName("a cage wagon");
		setDisplayText("a cage wagon sits here.");
		setDescription("It\\`s of solid wood construction with metal bracings.  The door has a key hole.");
		capacity=5000;
		setContainTypes(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED);
		material=RawMaterial.RESOURCE_OAK;
		baseGoldValue=15;
		baseEnvStats().setWeight(1000);
		rideBasis=Rideable.RIDEABLE_WAGON;
		recoverEnvStats();
	}



	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this))
        &&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
            synchronized(this)
            {
                boolean wasOpen=isOpen;
                isOpen=true;
                CMLib.commands().handleBeingLookedAt(msg);
                isOpen=wasOpen;
            }
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.executeMsg(this,msg);
			}

			for(int a=0;a<numEffects();a++)
			{
				Ability A=fetchEffect(a);
				if(A!=null)
					A.executeMsg(this,msg);
			}
			return;
		}
		super.executeMsg(myHost,msg);
	}
}
