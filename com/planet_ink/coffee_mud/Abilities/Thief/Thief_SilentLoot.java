package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_SilentLoot extends ThiefSkill
{
	public String ID() { return "Thief_SilentLoot"; }
	public String displayText() {return "(Silent AutoLoot)";}
	public String name(){ return "Silent AutoLoot";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"SILENTLOOT"};
	public String[] triggerStrings(){return triggerStrings;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.source()!=affected)
			&&(Sense.canBeSeenBy(msg.source(),(MOB)affected))
			&&(msg.source().location()==((MOB)affected).location())
			&&((msg.source().inventorySize())>0))
			{
				Item item=msg.source().fetchCarried(null,"all");
				if(item==null) item=msg.source().fetchWornItem("all");
				if((item!=null)&&(msg.source().isMine(item)))
				{
					item.unWear();
					item.removeFromOwnerContainer();
					item.setContainer(null);
					MOB mob=(MOB)affected;
					mob.location().addItemRefuse(item,Item.REFUSE_MONSTER_EQ);
					MOB victim=mob.getVictim();
					mob.setVictim(null);
					FullMsg msg2=new FullMsg(mob,item,this,CMMsg.MSG_THIEF_ACT,"You silently autoloot "+item.name()+" from the corpse of "+msg.source().name(),CMMsg.MSG_THIEF_ACT,null,CMMsg.NO_EFFECT,null);
					if(mob.location().okMessage(mob,msg2))
					{
						mob.location().send(mob,msg2);
						CommonMsgs.get(mob,null,item,true);
					}
					if(victim!=null) mob.setVictim(victim);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell("You are no longer automatically looting items from corpses silently.");
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell("You will now automatically loot items from corpses silently.");
			beneficialAffect(mob,mob,asLevel,0);
			Ability A=mob.fetchEffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to start silently looting items from corpses, but fail(s).");
		return success;
	}

}
