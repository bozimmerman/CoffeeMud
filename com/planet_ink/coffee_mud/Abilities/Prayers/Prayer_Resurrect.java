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
   Copyright 2000-2006 Bo Zimmerman

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
public class Prayer_Resurrect extends Prayer
{
	public String ID() { return "Prayer_Resurrect"; }
	public String name(){ return "Resurrect";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item body=this.getTarget(mob,mob.location(),givenTarget,commands,Item.WORNREQ_UNWORNONLY);
		if(body==null) return false;
		if((!(body instanceof DeadBody))
		||(!((DeadBody)body).playerCorpse())
		||(((DeadBody)body).mobName().length()==0))
		{
			mob.tell("You can't resurrect that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,body,this,verbalCastCode(mob,body,auto),auto?"<T-NAME> is resurrected!":"^S<S-NAME> resurrect(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				MOB rejuvedMOB=CMLib.map().getPlayer(((DeadBody)body).mobName());
				if(rejuvedMOB!=null)
				{
					rejuvedMOB.tell("You are being resurrected.");
					if(rejuvedMOB.location()!=mob.location())
					{
						rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappears!");
						mob.location().bringMobHere(rejuvedMOB,false);
					}
					Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
					if(A!=null) rejuvedMOB.delAbility(A);
					A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
					if(A!=null) rejuvedMOB.delEffect(A);

					int it=0;
					while(it<rejuvedMOB.location().numItems())
					{
						Item item=rejuvedMOB.location().fetchItem(it);
						if((item!=null)&&(item.container()==body))
						{
							CMMsg msg2=CMClass.getMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
							rejuvedMOB.location().send(rejuvedMOB,msg2);
							CMMsg msg3=CMClass.getMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
							rejuvedMOB.location().send(rejuvedMOB,msg3);
							it=0;
						}
						else
							it++;
					}
					body.destroy();
					rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
					mob.location().recoverRoomStats();
					Vector whatsToDo=CMParms.parse(CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH));
					for(int w=0;w<whatsToDo.size();w++)
					{
						String whatToDo=(String)whatsToDo.elementAt(w);
						if(whatToDo.startsWith("UNL"))
							CMLib.leveler().level(rejuvedMOB);
						else
						if(whatToDo.startsWith("ASTR"))
						{}
						else
						if(whatToDo.startsWith("PUR"))
						{}
						else
						if((whatToDo.trim().equals("0"))||(CMath.s_int(whatToDo)>0))
						{
							int expLost=CMath.s_int(whatToDo)/2;
							rejuvedMOB.tell("^*You regain "+expLost+" experience points.^?^.");
							CMLib.leveler().postExperience(rejuvedMOB,null,null,expLost,false);
						}
						else
						if(whatToDo.length()<3)
							continue;
						else
						{
							int expLost=(100*rejuvedMOB.envStats().level())/2;
							rejuvedMOB.tell("^*You regain "+expLost+" experience points.^?^.");
							CMLib.leveler().postExperience(rejuvedMOB,null,null,expLost,false);
						}
					}
				}
				else
					mob.location().show(mob,body,CMMsg.MSG_OK_VISUAL,"<T-NAME> twitch(es) for a moment, but the spirit is too far gone.");
			}
		}
		else
			beneficialWordsFizzle(mob,body,auto?"":"<S-NAME> attempt(s) to resurrect <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
