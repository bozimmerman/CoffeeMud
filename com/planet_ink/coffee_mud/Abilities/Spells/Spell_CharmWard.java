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
public class Spell_CharmWard extends Spell
{
	public String ID() { return "Spell_CharmWard"; }
	public String name(){return "Charm Ward";}
	public String displayText(){return "(Charm Ward)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your charm ward dissipates.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(!msg.amISource(mob))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING))
			&&(!mob.amDead()))
			{
                Ability A=(Ability)msg.tool();
                if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
                ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
                ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
                ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
    				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
			{
				if((msg.source().location()!=null)&&(msg.source().location()!=R))
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				R.showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                MOB victim=((MOB)target).getVictim();
                if((victim!=null)&&(CMLib.flags().flaggedAbilities(victim,Ability.FLAG_CHARMING).size()>0))
                    return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=null;
		if(commands.size()>0)
		{
			String s=CMParms.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(CMLib.english().containsString(mob.location().ID(),s)
			||CMLib.english().containsString(mob.location().name(),s)
			||CMLib.english().containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null) {
		    if(mob.isMonster())
		        target=mob.location();
		    else
    		    return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already charmed.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> seem(s) magically protected.":"^S<S-NAME> invoke(s) a charm ward upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a charm ward, but fail(s).");

		return success;
	}
}
