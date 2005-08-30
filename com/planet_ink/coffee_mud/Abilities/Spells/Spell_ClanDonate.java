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
public class Spell_ClanDonate extends Spell
{
	public String ID() { return "Spell_ClanDonate"; }
	public String name(){return "Clan Donate";}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	protected int overrideMana(){return 5;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,null,givenTarget,null,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(!mob.isMine(target))
		{
			mob.tell("You aren't holding that!");
			return false;
		}
		
		Room clanDonateRoom=null;
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		Clan C=Clans.getClan(mob.getClanID());
		clanDonateRoom=CMMap.getRoom(C.getDonation());
		if(clanDonateRoom==null)
		{
			mob.tell("Your clan does not have a donation home.");
			return false;
		}
		if(!Sense.canAccess(mob,clanDonateRoom))
		{
			mob.tell("This magic can not be used to donate from here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> invoke(s) a donation spell upon <T-NAMESELF>.^?");
			if((mob.location().okMessage(mob,msg))
            &&((target instanceof Coins)||(CommonMsgs.drop(mob,target,true,false))))
			{
				mob.location().send(mob,msg);
                msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"<T-NAME> appears!");
                if(clanDonateRoom.okMessage(mob,msg))
                {
                    mob.location().show(mob,target,this,CMMsg.MSG_OK_VISUAL,"<T-NAME> vanishes!");
                    clanDonateRoom.bringItemHere(target,Item.REFUSE_PLAYER_DROP);
                    clanDonateRoom.recoverRoomStats();
                    clanDonateRoom.sendOthers(mob,msg);
                }
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke donation upon <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
