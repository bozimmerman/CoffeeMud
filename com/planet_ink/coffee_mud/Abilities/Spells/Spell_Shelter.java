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
public class Spell_Shelter extends Spell
{
	public String ID() { return "Spell_Shelter"; }
	public String name(){return "Shelter";}
	public String displayText(){return "(In a shelter)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public Room previousLocation=null;
	public Room shelter=null;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
		{
			int i=0;
			while(i<shelter.numInhabitants())
			{
				mob=shelter.fetchInhabitant(0);
				if(mob==null) break;
				mob.tell("You return to your previous location.");

				FullMsg enterMsg=new FullMsg(mob,previousLocation,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of nowhere!");
				previousLocation.bringMobHere(mob,false);
				previousLocation.send(mob,enterMsg);
				CommonMsgs.look(mob,true);
			}
			shelter=null;
			previousLocation=null;
		}
		super.unInvoke();
	}

    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if((msg.sourceMinor()==CMMsg.TYP_QUIT)
        &&(shelter!=null)
        &&(shelter.isInhabitant(msg.source())))
        {
            if(previousLocation!=null)
                previousLocation.bringMobHere(msg.source(),false);
            if(msg.source().fetchEffect(ID())==this)
                unInvoke();
        }
        return super.okMessage(host,msg);
    }
    
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.fetchEffect(ID())!=null)
		{
			mob.fetchEffect(ID()).unInvoke();
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms, speak(s), and suddenly vanish(es)!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			HashSet h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			previousLocation=thisRoom;
			shelter=CMClass.getLocale("MagicShelter");
			Room newRoom=shelter;
			shelter.setArea(mob.location().getArea());
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				FullMsg enterMsg=new FullMsg(follower,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of nowhere.");
				FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,affectType(auto),"<S-NAME> disappear(s) into oblivion.");
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
					if(follower==mob)
						beneficialAffect(mob,mob,asLevel,999999);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and and speak(s), but nothing happens.");

		return success;
	}
}
