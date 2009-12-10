package com.planet_ink.coffee_mud.Races;
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

	private int[] agingChart={0,4,8,12,16,20,24,28,32};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+3);
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
		&&(CMLib.dice().rollPercentage()<50)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
		&&(!((MOB)msg.target()).isMonster())
		&&(((msg.value())>(((MOB)msg.target()).maxState().getHitPoints()/5)))
		&&(!CMSecurity.isDisabled("AUTODISEASE")))
		{
			Ability A=CMClass.getAbility("Disease_Lycanthropy");
			if((A!=null)&&(msg.target().fetchEffect(A.ID())==null))
				A.invoke(mob,msg.target(),true,0);
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
				("some "+name().toLowerCase()+" claws",RawMaterial.RESOURCE_BONE));
				for(int i=0;i<4;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",RawMaterial.RESOURCE_FUR));
				for(int i=0;i<2;i++)
				{
					Item meat=makeResource
					("some "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_MEAT);
					if(!CMSecurity.isDisabled("AUTODISEASE"))
					{
						Ability A=CMClass.getAbility("Disease_Lycanthropy");
						if(A!=null)	meat.addNonUninvokableEffect(A);
					}
					resources.addElement(meat);
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_MEAT));
				}
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
