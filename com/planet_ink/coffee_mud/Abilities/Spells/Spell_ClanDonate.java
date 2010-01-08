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
public class Spell_ClanDonate extends Spell
{
	public String ID() { return "Spell_ClanDonate"; }
	public String name(){return "Clan Donate";}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
    public long flags(){return super.flags()|Ability.FLAG_CLANMAGIC;}
	protected int overrideMana(){return 5;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,null,givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if(!mob.isMine(target))
		{
			mob.tell("You aren't holding that!");
			return false;
		}
		
		Room clanDonateRoom=null;
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase(""))||(mob.getClanRole()==0))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		Clan C=CMLib.clans().getClan(mob.getClanID());
		clanDonateRoom=CMLib.map().getRoom(C.getDonation());
		if(clanDonateRoom==null)
		{
			mob.tell("Your clan does not have a donation home.");
			return false;
		}
		if(!CMLib.flags().canAccess(mob,clanDonateRoom))
		{
			mob.tell("This magic can not be used to donate from here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> invoke(s) a donation spell upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				MOB victim=mob.getVictim();
                boolean proceed=(target instanceof Coins);
                if(!proceed){
                    Room prevRoom=mob.location();
                    clanDonateRoom.bringMobHere(mob,false);
                    proceed=CMLib.commands().postDrop(mob,target,true,false);
                    prevRoom.bringMobHere(mob,false);
                }
                if(proceed)
                {
    				mob.location().send(mob,msg);
                    msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"<T-NAME> appears!");
                    if(clanDonateRoom.okMessage(mob,msg))
                    {
                        mob.location().show(mob,target,this,CMMsg.MSG_OK_VISUAL,"<T-NAME> vanishes!");
                        if(!clanDonateRoom.isContent(target))
                            clanDonateRoom.bringItemHere(target,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),false);
                        if(!(target.amDestroyed()))
                        {
                            if(target instanceof Coins)
                                ((Coins)target).putCoinsBack();
                            else
                            if(target instanceof RawMaterial)
                                ((RawMaterial)target).rebundle();
                        }
                        clanDonateRoom.recoverRoomStats();
                        clanDonateRoom.sendOthers(mob,msg);
                    }
                }
                mob.setVictim(victim);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke donation upon <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
