package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
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
public class Spell_FeignDeath extends Spell
{
	public String ID() { return "Spell_FeignDeath"; }
	public String name(){return "Feign Death";}
	public String displayText(){return "(Feign Death)";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public DeadBody Body=null;
	public Room deathRoom=null;
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
			mob.tell("Your death is no longer feigned.");
		if((Body!=null)&&(deathRoom!=null)&&(deathRoom.isContent(Body)))
		{
			Body.destroy();
			deathRoom.recoverRoomStats();
		}
		super.unInvoke();
	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH)))
			{
				if(Util.bset(msg.sourceMajor(),CMMsg.MASK_SOUND))
					mob.tell("You are unable to make sounds in this semi-incorporeal form.");
				else
					mob.tell("You are unable to do that this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((msg.amITarget(mob))&&(!msg.amISource(mob))
		   &&(!Util.bset(msg.targetMajor(),CMMsg.MASK_GENERAL)))
		{
			mob.tell(mob.name()+" doesn't seem to be here.");
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) to <T-NAMESELF> and yell(s) for death!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			peaceAt(target);
			deathRoom=mob.location();
			Body=(DeadBody)CMClass.getItem("Corpse");
			Body.setCharStats(target.baseCharStats().cloneCharStats());
			beneficialAffect(mob,target,asLevel,10);

			int tries=0;
			while((target.numFollowers()>0)&&((++tries)<1000))
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
					follower.setFollowing(null);
			}
			deathRoom.show(target,null,CMMsg.MSG_OK_ACTION,"^Z"+target.name()+" is DEAD!!!^.^?\n\r");
			Body.setName("the body of "+target.name());
			Body.setDisplayText("the body of "+target.name()+" lies here.");
			Body.baseEnvStats().setWeight(target.envStats().weight()+100);
			Body.setSecretIdentity("FAKE");
			deathRoom.addItemRefuse(Body,Item.REFUSE_MONSTER_BODY);
			Body.recoverEnvStats();
			deathRoom.recoverRoomStats();
		}

		return success;
	}
}
