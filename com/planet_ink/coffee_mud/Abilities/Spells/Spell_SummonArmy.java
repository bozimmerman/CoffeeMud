package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_SummonArmy extends Spell
{
	public String ID() { return "Spell_SummonArmy"; }
	public String name(){return "Summon Army";}
	public String displayText(){return "(Monster Summoning)";}
	public int quality(){return BENEFICIAL_SELF;};
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) help from the Java Plain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String[] choices={"Dog","Orc","Tiger","Troll","Chimp","BrownBear","Goblin","LargeBat","GiantScorpion","Rattlesnake","Ogre"};
				for(int i=0;i<mob.envStats().level()/3;i++)
				{
					MOB newMOB=CMClass.getMOB(choices[Dice.roll(1,choices.length,-1)]);
					newMOB.setLocation(mob.location());
					newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					newMOB.setMoney(0);
					newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
					newMOB.setStartRoom(null);
					newMOB.setVictim(mob.getVictim());
					CommonMsgs.follow(newMOB,mob,true);
					if(newMOB.amFollowing()!=mob)
						newMOB.setFollowing(mob);
					if(newMOB.getVictim()!=null)
						newMOB.getVictim().setVictim(newMOB);
					beneficialAffect(mob,newMOB,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for magical help, but chokes on the words.");

		// return whether it worked
		return success;
	}

}
