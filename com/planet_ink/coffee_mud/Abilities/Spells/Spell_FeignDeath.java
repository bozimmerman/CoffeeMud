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
public class Spell_FeignDeath extends Spell
{
	public String ID() { return "Spell_FeignDeath"; }
	public String name(){return "Feign Death";}
	public String displayText(){return "(Feign Death)";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
    public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}

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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH)))
			{
				if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_SOUND))
					mob.tell("You are unable to make sounds in this semi-incorporeal form.");
				else
					mob.tell("You are unable to do that in this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((msg.amITarget(mob))&&(!msg.amISource(mob))
		   &&(!CMath.bset(msg.targetMajor(),CMMsg.MASK_ALWAYS)))
		{
			msg.source().tell(mob.name()+" doesn't seem to be here.");
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

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> point(s) to <T-NAMESELF> and yell(s) for death!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			peaceAt(target);
			deathRoom=mob.location();
			Body=(DeadBody)CMClass.getItem("Corpse");
			Body.setCharStats((CharStats)target.baseCharStats().copyOf());
			beneficialAffect(mob,target,asLevel,10);

			int tries=0;
			while((target.numFollowers()>0)&&((++tries)<1000))
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
					follower.setFollowing(null);
			}
            String msp=CMProps.msp("death"+CMLib.dice().roll(1,4,0)+".wav",50);
            msg=CMClass.getMsg(target,null,null,
                    CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
                    CMMsg.MSG_OK_VISUAL,null,
                    CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r"+msp);
            if(deathRoom.okMessage(target,msg))
            {
                deathRoom.send(target,msg);
    			Body.setName("the body of "+target.name());
    			Body.setDisplayText("the body of "+target.name()+" lies here.");
    			Body.baseEnvStats().setWeight(target.envStats().weight()+100);
    			Body.setSecretIdentity("FAKE");
    			deathRoom.addItemRefuse(Body,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_BODY));
    			Body.recoverEnvStats();
    			deathRoom.recoverRoomStats();
            }
		}

		return success;
	}
}
