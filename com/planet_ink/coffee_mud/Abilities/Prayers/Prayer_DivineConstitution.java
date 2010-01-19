package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_DivineConstitution extends Prayer
{
	public String ID() { return "Prayer_DivineConstitution"; }
	public String name(){ return "Divine Constitution";}
	public String displayText(){ return "(Divine Constitution)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
    protected int conPts=1;
    protected int xtraHPs=0;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.STAT_CONSTITUTION, affectableStats.getStat(CharStats.STAT_CONSTITUTION)+conPts);
		affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ, affectableStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)+conPts);
	}

	public void affectCharState(MOB affected, CharState affectableMaxState)
	{
		super.affectCharState(affected, affectableMaxState);
		if(affected==null) return;
		affectableMaxState.setHitPoints(affectableMaxState.getHitPoints()+xtraHPs);
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{

		if((msg.target()==affected)
		&&(affected instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_HEALING)
		&&(msg.source().location()!=null)
		&&(msg.source()==invoker())
		&&(conPts<6)
		&&(CMLib.dice().rollPercentage()<(10+getX1Level(msg.source()))))
		{
			MOB M=(MOB)affected;
			Room R=M.location();
			int diff = (M.curState().getHitPoints() - M.maxState().getHitPoints()) + msg.value();
			if((diff>0)
			&&(msg.value()>diff)
			&&(M!=null))
			{
				R.show(M,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> gain(s) divine health!");
				conPts++;
				xtraHPs+=1+diff;
			}
		}
		return super.okMessage(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> divine constitution fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"<T-NAME> become(s) covered by divine constitution.":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to be covered by divine constitution.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				conPts=1;
				xtraHPs=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				target.recoverEnvStats();
				target.recoverCharStats();
				target.recoverMaxState();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to have divine constitution, but nothing happens.");


		// return whether it worked
		return success;
	}
}
