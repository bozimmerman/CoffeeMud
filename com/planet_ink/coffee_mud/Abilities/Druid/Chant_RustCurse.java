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
public class Chant_RustCurse extends Chant
{
	public String ID() { return "Chant_RustCurse"; }
	public String name(){return "Rust Curse";}
	public String displayText(){return "(Rust Curse)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}

	public void unInvoke()
	{
		MOB M=null;
		if(affected instanceof MOB)
			M=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(M!=null)&&(!M.amDead()))
			M.tell("You don't feel so damp any more.");
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			boolean goodChoices=false;
			Vector choices=new Vector();
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I.subjectToWearAndTear())
				   &&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
					  ||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)))
				{
					if(!I.amWearingAt(Wearable.IN_INVENTORY))
					{
						goodChoices=true;
						choices.addElement(I);
					}
					else
					if(!goodChoices)
						choices.addElement(I);
				}
			}
			if(goodChoices)
			for(int i=choices.size()-1;i>=0;i--)
				if(((Item)choices.elementAt(i)).amWearingAt(Wearable.IN_INVENTORY))
					choices.removeElementAt(i);
			if(choices.size()>0)
			{
				Item I=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
				if(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
				||(CMLib.dice().rollPercentage()<10))
				{
					CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_WATER,"<T-NAME> rusts!","You rust!",null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						I.setUsesRemaining(I.usesRemaining()-1);
						if(I.usesRemaining()<=0)
						{
							mob.tell(I.name()+" is destroyed!");
							I.destroy();
						}
						else
						if(I.usesRemaining()<=10)
						{
							mob.tell(I.name()+" is looking really bad.");
						}
					}
				}
			}
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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

			CMMsg msg = CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) at <T-NAME> rustily!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					maliciousAffect(mob,target,asLevel,0,-1);
					target.tell("You feel damp!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
