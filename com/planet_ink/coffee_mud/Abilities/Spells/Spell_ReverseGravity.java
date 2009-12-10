package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_ReverseGravity extends Spell
{
	public String ID() { return "Spell_ReverseGravity"; }
	public String name(){return "Reverse Gravity";}
	public String displayText(){return "(Gravity is Reversed)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected Vector childrenAffects=new Vector();
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
	public long flags(){return Ability.FLAG_MOVING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		else
		if((affected!=null)&&(affected instanceof Room)&&(invoker!=null))
		{
			Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if(!CMLib.flags().isInFlight(inhab))
				{
					inhab.makePeace();
					Ability A=CMClass.getAbility("Falling");
					A.setAffectedOne(null);
					A.setProficiency(100);
					A.invoke(null,null,inhab,true,0);
					A=inhab.fetchEffect("Falling");
					if(A!=null)
						childrenAffects.addElement(A);
				}
			}
			for(int i=0;i<room.numItems();i++)
			{
				Item inhab=room.fetchItem(i);
				if((inhab!=null)
                &&(inhab.container()==null)
                &&(!CMLib.flags().isInFlight(inhab.ultimateContainer())))
				{
					Ability A=CMClass.getAbility("Falling");
					A.setAffectedOne(room);
					A.setProficiency(100);
					A.invoke(null,null,inhab,true,0);
					A=inhab.fetchEffect("Falling");
					if(A!=null)
						childrenAffects.addElement(A);
				}
			}
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(CMMsg.MSG_OK_VISUAL, "Gravity returns to normal...");
				if(invoker!=null)
				{
					Ability me=invoker.fetchEffect(ID());
					if(me!=null) me.setProficiency(0);
				}
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if(mob.location()!=null)
				{
					mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, "Gravity returns to normal..");
					Ability me=mob.location().fetchEffect(ID());
					if(me!=null) me.setProficiency(0);
				}
			}
			while(childrenAffects.size()>0)
			{
				Ability A=(Ability)childrenAffects.elementAt(0);
				A.setProficiency(0);
				childrenAffects.removeElement(A);
			}
		}
		super.unInvoke();
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.location().fetchEffect(this.ID())!=null)
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
		    mob.tell(mob,null,null,"Gravity has already been reversed here!");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), (auto?"G":"^S<S-NAME> speak(s) and wave(s) and g")+"ravity begins to reverse!^?");
			if(mob.location().okMessage(mob,msg))
			{
				childrenAffects=new Vector();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,7);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) in reverse, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
