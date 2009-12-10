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
public class Spell_Blademouth extends Spell
{
	public String ID() { return "Spell_Blademouth"; }
	public String name(){return "Blademouth";}
	public String displayText(){return "(blades in your mouth)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	public Vector limbsToRemove=new Vector();
    protected boolean noRecurse=false;
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
        &&(!noRecurse)
        &&(affected instanceof MOB)
        &&(invoker!=null)
        &&(msg.amISource((MOB)affected))
        &&(msg.source().location()!=null)
	    &&(msg.source().charStats().getMyRace().bodyMask()[Race.BODY_MOUTH]>=0))
        {
	        noRecurse=true;
            try{CMLib.combat().postDamage(invoker,msg.source(),this,msg.source().maxState().getHitPoints()/10,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_SLASHING,"The blades in <T-YOUPOSS> mouth <DAMAGE> <T-HIM-HER>!");
            }finally{noRecurse=false;}
        }
	    super.executeMsg(host,msg);
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(((MOB)target).charStats().getMyRace().bodyMask()[Race.BODY_MOUTH]<=0)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.charStats().getMyRace().bodyMask()[Race.BODY_MOUTH]<=0)
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
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"!":"^S<S-NAME> invoke(s) a sharp spell upon <T-NAMESELF>"));
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
