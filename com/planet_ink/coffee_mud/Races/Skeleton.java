package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class Skeleton extends Undead
{
	public String ID(){	return "Skeleton"; }
	public String name(){ return "Skeleton"; }

	protected static Vector resources=new Vector();

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(myHost instanceof MOB)
		{
			MOB mob=(MOB)myHost;
			if((msg.amITarget(mob))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&((((Weapon)msg.tool()).weaponType()==Weapon.TYPE_PIERCING)
				||(((Weapon)msg.tool()).weaponType()==Weapon.TYPE_SLASHING))
			&&(!mob.amDead()))
			{
				int recovery=(int)Math.round(Util.div((msg.value()),2.0));
				msg.setValue(recovery);
			}
		}
		return super.okMessage(myHost,msg);
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("knuckle bone",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
