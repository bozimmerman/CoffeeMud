package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2000-2006 Bo Zimmerman

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

public class Prayer_CurseFlames extends Prayer
{
	public String ID() { return "Prayer_CurseFlames"; }
	public String name(){ return "Curse Flames";}
	public int abstractQuality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public int maxRange(){return 5;}
	public int minRange(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		Item fireSource=null;
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I=target.fetchInventory(i);
			if((CMLib.flags().isOnFire(I))&&(I.container()==null))
			{
				fireSource=I;
				break;
			}
		}

		if(fireSource==null)
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((CMLib.flags().isOnFire(I))&&(I.container()==null))
			{
				fireSource=I;
				break;
			}
		}

		if((success)&&(fireSource!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),(auto?"Suddenly "+fireSource.name()+" flares up and attacks <T-HIM-HER>!^?":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+".  Suddenly "+fireSource.name()+" flares up and attacks <T-HIM-HER>!^?")+CMProps.msp("fireball.wav",40));
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
                int numDice = (int)Math.round(CMath.div(adjustedLevel(mob,asLevel),2.0))+1;
				int damage = CMLib.dice().roll(numDice, 6, 20);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
			fireSource.destroy();
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
