package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class WereWolf extends GiantWolf
{
	public String ID(){	return "WereWolf"; }
	public String name(){ return "WereWolf"; }
	public int shortestMale(){return 59;}
	public int shortestFemale(){return 59;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 80;}
	public int weightVariance(){return 80;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Canine";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setPermaStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+3);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=(MOB)myHost;
		if(msg.amISource(mob)
		&&(!msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(mob.fetchWieldedItem()==null)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(Dice.rollPercentage()<50)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
		&&(!((MOB)msg.target()).isMonster())
		&&(((msg.value())>(((MOB)msg.target()).maxState().getHitPoints()/5))))
		{
			Ability A=CMClass.getAbility("Disease_Lycanthropy");
			if((A!=null)&&(msg.target().fetchEffect(A.ID())==null))
				A.invoke(mob,(MOB)msg.target(),true);
		}
		super.executeMsg(myHost,msg);
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" claws",EnvResource.RESOURCE_BONE));
				for(int i=0;i<4;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				for(int i=0;i<2;i++)
				{
					Item meat=makeResource
					("some "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT);
					Ability A=CMClass.getAbility("Disease_Lycanthropy");
					if(A!=null)	meat.addNonUninvokableEffect(A);
					resources.addElement(meat);
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				}
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
