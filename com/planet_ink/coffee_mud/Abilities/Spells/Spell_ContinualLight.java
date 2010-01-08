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
public class Spell_ContinualLight extends Spell
{
	public String ID() { return "Spell_ContinualLight"; }
	public String name(){return "Continual Light";}
	public String displayText(){return "(Continual Light)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(!(affected instanceof Room))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		if(CMLib.flags().isInDark(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		Room room=((MOB)affected).location();
		if(canBeUninvoked())
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The light above <S-NAME> dims.");
		super.unInvoke();
		if(canBeUninvoked())
			room.recoverRoomStats();
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((mob==target)&&(!CMLib.flags().canBeSeenBy(mob.location(),mob)))
                return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=null;
		if(commands.size()==0) target=mob;
		else
		target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);

		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String str="^S<S-NAME> invoke(s) a continual light toward(s) <T-NAMESELF>!^?";
		if(!(target instanceof MOB))
			str="^S<S-NAME> invoke(s) a continual light into <T-NAME>!^?";
		CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),str);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE-100);
			mob.location().recoverRoomStats(); // attempt to handle followers
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke light, but fail(s).");

		return success;
	}
}
