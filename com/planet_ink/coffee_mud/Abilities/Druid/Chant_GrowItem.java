package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
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

public class Chant_GrowItem extends Chant
{
	public String ID() { return "Chant_GrowItem"; }
	public String name(){ return "Grow Item";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_OAK;
		if((mob.location().myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				if((((Integer)V.elementAt(v)).intValue()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
					V2.addElement(V.elementAt(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(Dice.roll(1,V2.size(),-1))).intValue();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the trees.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item building=null;
				Item key=null;
				Ability A=CMClass.getAbility("Carpentry");
				if(A!=null)
				{
					while((building==null)||(building.name().endsWith(" bundle")))
					{
						Vector V=new Vector();
						V.addElement(new Integer(material));
						A.invoke(mob,V,A,true,asLevel);
						if((V.size()>0)&&(V.lastElement() instanceof Item))
						{
							if((V.size()>1)&&((V.elementAt(V.size()-2) instanceof Item)))
								key=(Item)V.elementAt(V.size()-2);
							building=(Item)V.lastElement();
						}
						else
							break;
					}
				}
				if(building==null)
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}

				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();

				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				if(key!=null)
					mob.location().addItemRefuse(key,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" grows out of a tree and drops.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the trees, but nothing happens.");

		// return whether it worked
		return success;
	}
}
