package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_MagneticEarth extends Chant
{
	public String ID() { return "Chant_MagneticEarth"; }
	public String name(){ return "Magnetic Earth";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			Vector toGo=new Vector();
			boolean didSomething=false;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M!=invoker))
				{
					toGo.clear();
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((I!=null)
						&&(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
						   ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
						&&(I.container()==null)
						&&(I.amWearingAt(Item.INVENTORY)
						   ||I.amWearingAt(Item.HELD)
						   ||I.amWearingAt(Item.WIELD)
						   ||I.amWearingAt(Item.ON_EYES)
						   ||I.amWearingAt(Item.ON_MOUTH)))
							toGo.addElement(I);
					}
					for(int i=0;i<toGo.size();i++)
					{
						Item I=(Item)toGo.elementAt(i);
						if(CommonMsgs.drop(M,I,true,true))
						{
							didSomething=true;
							R.show(M,I,CMMsg.MSG_OK_VISUAL,"<T-NAME> is pulled away from <S-NAME> to the magnetic ground!");
						}
					}
				}
			}
			if(didSomething)
			{
				R.recoverRoomStats();
				R.recoverRoomStats();
			}
		}
		return true;
	}

	protected boolean checked=false;
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((!checked)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(affected instanceof Room))
		{
			checked=true;
			if(!CMClass.ThreadEngine().isTicking(this,-1))
				CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_SPELL_AFFECT,1);
		}
		super.executeMsg(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
		{
			mob.tell("This chant only works in caves, mountains, or rocky areas.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ground gains a powerful magnetic field!");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) the ground, but the magic fades.");
		// return whether it worked
		return success;
	}
}
