package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
public class Spell_Blademouth extends Spell
{
	public String ID() { return "Spell_Blademouth"; }
	public String name(){return "Blademouth";}
	public String displayText(){return "(blades in your mouth)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	public Vector limbsToRemove=new Vector();
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
        &&(affected instanceof MOB)
        &&(invoker!=null)
        &&(msg.amISource((MOB)affected))
        &&(msg.source().location()!=null)
	    &&(msg.source().charStats().getMyRace().bodyMask()[Race.BODY_MOUTH]>=0))
	        MUDFight.postDamage(invoker,msg.source(),this,msg.source().maxState().getHitPoints()/10,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_SLASHING,"The blades in <S-YOUPOSS> mouth <DAMAGE> <S-HIM-HER>!");
	    super.executeMsg(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.charStats().getMyRace().bodyMask()[Race.BODY_MOUTH]>=0)
		{
			if(!auto)
				mob.tell("There is no mouth on "+target.name()+" to fill with blades!");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"!":"^S<S-NAME> invoke(s) a sharp spell upon <T-NAMESELF>"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				super.maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) sharply at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}
