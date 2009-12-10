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
public class Spell_Timeport extends Spell
{
	public String ID() { return "Spell_Timeport"; }
	public String name(){return "Timeport";}
	public String displayText(){return "(Time Travelling)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

    protected final static int mask=
			EnvStats.CAN_NOT_TASTE|EnvStats.CAN_NOT_SMELL|EnvStats.CAN_NOT_SEE
		    |EnvStats.CAN_NOT_HEAR;
    protected final static int mask2=Integer.MAX_VALUE
			-EnvStats.CAN_SEE_BONUS
		    -EnvStats.CAN_SEE_DARK
		    -EnvStats.CAN_SEE_EVIL
		    -EnvStats.CAN_SEE_GOOD
		    -EnvStats.CAN_SEE_HIDDEN
		    -EnvStats.CAN_SEE_INFRARED
		    -EnvStats.CAN_SEE_INVISIBLE
		    -EnvStats.CAN_SEE_METAL
		    -EnvStats.CAN_SEE_SNEAKERS
		    -EnvStats.CAN_SEE_VICTIM;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(mask&mask2);
		affectableStats.setDisposition(EnvStats.IS_NOT_SEEN);
        affectableStats.setDisposition(EnvStats.IS_CLOAKED);
		affectableStats.setDisposition(EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(EnvStats.IS_HIDDEN);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		MOB mob=null;
		Room room=null;
		if((affected!=null)&&(canBeUninvoked())&&(affected instanceof MOB))
		{
			mob=(MOB)affected;
			room=mob.location();
			CMLib.threads().resumeTicking(mob,-1);
		}
		super.unInvoke();
		if(room!=null)
			room.show(mob, null, CMMsg.MSG_OK_VISUAL, "<S-NAME> reappear(s)!");
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
		    if(!canBeUninvoked())
		    {
				msg.source().tell("The timeport spell on you fizzles away.");
		        affected.delEffect(this);
		    }
		    else
			if(msg.amISource((MOB)affected))
				if((!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
				&&(!CMath.bset(msg.targetCode(),CMMsg.MASK_ALWAYS)))
				{
					msg.source().tell("Nothing just happened.  You are time travelling, and can't do that.");
					return false;
				}
		}
		return super.okMessage(myHost,msg);
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

			CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),(auto?"":"^S<S-NAME> speak(s) and gesture(s)")+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room room=mob.location();
				target.makePeace();
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if((M!=null)&&(M.getVictim()==target))
						M.makePeace();
				}
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es)!");
				CMLib.threads().suspendTicking(target,-1);
				beneficialAffect(mob,target,asLevel,3);
				Ability A=target.fetchEffect(ID());
				if(A!=null)	CMLib.threads().startTickDown(A,Tickable.TICKID_MOB,1);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) for awhile, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
