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
public class Spell_Flagportation extends Spell
{
	public String ID() { return "Spell_Flagportation"; }
	public String name(){return "Flagportation";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

	    Clan C=mob.getClanID().length()>0?Clans.getClan(mob.getClanID()):null;
	    if(C==null)
	    {
	        mob.tell("You must belong to a clan to use this spell.");
	        return false;
	    }
	    if((!auto)&&(mob.getClanRole()<Clan.POS_ENCHANTER))
	    {
	        mob.tell("You do not have priviledges to use this spell.");
	        return false;
	    }
		Vector candidates=new Vector();
		Room R=null;
		Item I=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
			{
			    for(int i=0;i<R.numItems();i++)
			    {
			        I=R.fetchItem(i);
			        if((I!=null)
			        &&(I instanceof ClanItem)
			        &&(((ClanItem)I).clanID().equals(C.ID()))
			        &&(((ClanItem)I).ciType()==ClanItem.CI_BANNER))
			        {
						candidates.addElement(R);
						break;
			        }
			    }
			}
		}
		if(candidates.size()==0)
		{
			mob.tell("You don't have any flags to flagportate to!");
			return false;
		}

		if(Sense.isSitting(mob)||Sense.isSleeping(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=(Room)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
			FullMsg enterMsg=new FullMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell("Your magic seems unable to take you to that flag.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while((room==null)||(room==newRoom)||(room.getArea()==newRoom.getArea())||((++x)>1000)||(room==mob.location())||(!Sense.canAccess(mob,room)))
				room=CMMap.getRandomRoom();
			if(room==null)
				beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke flagportating transportation, but fizzle(s) the spell.");
			newRoom=room;
		}

		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MASK_MOVE|affectType(auto),"^S<S-NAME> invoke(s) a flagportating teleportation spell.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			HashSet h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CommonStrings.msp("appear.wav",10));
				FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CommonMsgs.flee(follower,("NOWHERE"));
						follower.makePeace();
					}
					thisRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell("\n\r\n\r");
					CommonMsgs.look(follower,true);
				}
			}
		}
		// return whether it worked
		return success;
	}
}
