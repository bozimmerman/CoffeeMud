package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_SnatchLight extends Chant
{
	public String ID() { return "Chant_SnatchLight"; }
	public String name(){return "Snatch Light";}
	public String displayText(){return "(Snatch Light)";}
	public int quality(){ return Ability.OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}

	public Room snatchLocation()
	{
		if((invoker!=null)
		&&Sense.isInTheGame(invoker,false)
		&&(invoker.fetchEffect(ID())!=null))
		   return ((MOB)invoker).location();
		return null;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(CoffeeUtensils.roomLocation(affected)==snatchLocation())
		{
			affectableStats.setDisposition(affectableStats.disposition() |  EnvStats.IS_DARK);
			if(Util.bset(affectableStats.disposition(),EnvStats.IS_LIGHTSOURCE))
				affectableStats.setDisposition(Util.unsetb(affectableStats.disposition(),EnvStats.IS_LIGHTSOURCE));
			if(Util.bset(affectableStats.disposition(),EnvStats.IS_GLOWING))
				affectableStats.setDisposition(Util.unsetb(affectableStats.disposition(),EnvStats.IS_GLOWING));
		}
		else
			unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==invoker)&&(invoker!=null))
		{
			MOB mob=invoker;
			Room R=mob.location();
			boolean didSomething=false;
			if((R!=null)&&(R.fetchEffect(ID())==null))
			{
				Ability A=(Ability)copyOf();
				A.setBorrowed(R,true);
				R.addEffect(A);
				didSomething=true;
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(Sense.isGlowing(M)||Sense.isLightSource(M))&&(M.fetchEffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					A.setBorrowed(M,true);
					M.addEffect(A);
					didSomething=true;
				}
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null)&&(Sense.isGlowing(I)||Sense.isLightSource(I))&&(I.fetchEffect(ID())==null))
					{
						Ability A=(Ability)copyOf();
						A.setBorrowed(I,true);
						I.addEffect(A);
						didSomething=true;
					}
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(Sense.isGlowing(I)||Sense.isLightSource(I))&&(I.fetchEffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					A.setBorrowed(I,true);
					I.addEffect(A);
					didSomething=true;
				}
			}
			R.recoverRoomStats();
			R.recoverRoomStats();
		}
		else
		if(affected!=null)
		{
			Room R=CoffeeUtensils.roomLocation(affected);
			if((invoker==null)||(R!=invoker.location()))
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(invoker==null)||(affected!=invoker))
		{
			Environmental E=affected;
			MOB oldI=invoker;
			super.unInvoke();
			if(E!=null)
			{
				if(E instanceof MOB)
				{
					MOB M=(MOB)E;
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if(I!=null)
						{
							Ability A=I.fetchEffect(ID());
							if((A!=null)&&(A.invoker()==oldI))
								A.unInvoke();
						}
					}
				}
				Room R=CoffeeUtensils.roomLocation(E);
				if(R!=null) R.recoverRoomStats();
			}
			return;
		}
		MOB mob=invoker;
		super.unInvoke();

		if((canBeUninvoked())&&(mob!=null))
		{
			mob.tell("Your ability to snatch light dissipates.");
			if(mob.location()!=null)
			{
				mob.location().recoverRoomStats();
				mob.location().recoverRoomStats();
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already snatching light.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> gain(s) an aura of light snatching!":"^S<S-NAME> chant(s), feeling <S-HIS-HER> body become a light snatcher!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing more happens.");

		// return whether it worked
		return success;
	}
}
