package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_SnatchLight extends Chant
{
	public String ID() { return "Chant_SnatchLight"; }
	public String name(){return "Snatch Light";}
	public String displayText(){return "(Snatch Light)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}

	public Room snatchLocation()
	{
		if((invoker!=null)
		&&CMLib.flags().isInTheGame(invoker,false)
		&&(invoker.fetchEffect(ID())!=null))
		   return invoker.location();
		return null;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(CMLib.map().roomLocation(affected)==snatchLocation())
		{
			affectableStats.setDisposition(affectableStats.disposition() |  EnvStats.IS_DARK);
			if(CMath.bset(affectableStats.disposition(),EnvStats.IS_LIGHTSOURCE))
				affectableStats.setDisposition(CMath.unsetb(affectableStats.disposition(),EnvStats.IS_LIGHTSOURCE));
			if(CMath.bset(affectableStats.disposition(),EnvStats.IS_GLOWING))
				affectableStats.setDisposition(CMath.unsetb(affectableStats.disposition(),EnvStats.IS_GLOWING));
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
			if(R==null) return true;
			if(R.fetchEffect(ID())==null)
			{
				Ability A=(Ability)copyOf();
				A.setSavable(false);
				R.addEffect(A);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(CMLib.flags().isGlowing(M)||CMLib.flags().isLightSource(M))&&(M.fetchEffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					A.setSavable(false);
					M.addEffect(A);
				}
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null)&&(CMLib.flags().isGlowing(I)||CMLib.flags().isLightSource(I))&&(I.fetchEffect(ID())==null))
					{
						Ability A=(Ability)copyOf();
						A.setSavable(false);
						I.addEffect(A);
					}
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(CMLib.flags().isGlowing(I)||CMLib.flags().isLightSource(I))&&(I.fetchEffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					A.setSavable(false);
					I.addEffect(A);
				}
			}
			R.recoverRoomStats();
			R.recoverRoomStats();
		}
		else
		if(affected!=null)
		{
			Room R=CMLib.map().roomLocation(affected);
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
				Room R=CMLib.map().roomLocation(E);
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
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
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<S-NAME> gain(s) an aura of light snatching!":"^S<S-NAME> chant(s), feeling <S-HIS-HER> body become a light snatcher!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing more happens.");

		// return whether it worked
		return success;
	}
}
