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
public class Chant_PlantSnare extends Chant
{
	public String ID() { return "Chant_PlantSnare"; }
	public String name(){ return "Plant Snare";}
	public String displayText(){return "(Snared)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int maxRange(){return adjustedMaxInvokerRange(2);}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int amountRemaining=0;
	public long flags(){return Ability.FLAG_BINDING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the snaring plants."))
				{
					amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)*4);
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the plants.");
			CMLib.commands().postStand(mob,true);
		}
	}
    
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            HashSet h=properTargets(mob,target,false);
            if(h==null)
                return Ability.QUALITY_INDIFFERENT;
            Room room=mob.location();
            if(room!=null)
            {
                if((room.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
                &&(room.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
                &&((room.myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
                &&((room.myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
                &&(room.domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
                &&(room.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
                &&(room.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth snaring.");
			return false;
		}
		Room room=mob.location();
		if((room.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		&&((room.myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
		&&((room.myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP))
		{
			mob.tell("There doesn't seem to be a large enough mass of plant life around here...\n\r");
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
			if(room.show(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the plants around <S-HIM-HER>.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();
				Room troom = CMLib.map().roomLocation(target);

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				if((troom!=null)&&(troom.okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					troom.send(mob,msg);
					if(msg.value()<=0)
					{
						amountRemaining=400;
						if(troom==room)
						{
							success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1);
							troom.show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) stuck as tangling mass of plant life grows onto <S-HIM-HER>!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fades.");


		// return whether it worked
		return success;
	}
}
