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
public class Spell_Shove extends Spell
{
	public String ID() { return "Spell_Shove"; }
	public String name(){return "Shove";}
	public String displayText(){return "(Shoved Down)";}
	public int maxRange(){return 4;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return MudHost.TICK_MOB;}
	public boolean doneTicking=false;
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	public long flags(){return Ability.FLAG_MOVING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int dir=-1;
		if(commands.size()>0)
		{
			dir=Directions.getGoodDirectionCode((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
		}
		if(dir<0)
		{
			mob.tell("Shove whom which direction?  Try north, south, east, or west...");
			return false;
		}
		if((mob.location().getRoomInDir(dir)==null)
		   ||(mob.location().getExitInDir(dir)==null)
		   ||(!mob.location().getExitInDir(dir).isOpen()))
		{
			mob.tell("You can't shove anyone that way!");
			return false;
		}

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> get(s) shoved back!":"<S-NAME> incant(s) and shove(s) at <T-NAMESELF>.");
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				if((msg.value()<=0)&&(target.location()==mob.location()))
				{
					mob.location().send(mob,msg);
					target.makePeace();
					Room newRoom=mob.location().getRoomInDir(dir);
					Room thisRoom=mob.location();
					FullMsg enterMsg=new FullMsg(target,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> fly(s) in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir))+".");
					FullMsg leaveMsg=new FullMsg(target,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> <S-IS-ARE> shoved forcefully into the air and out "+Directions.getInDirectionName(dir)+".");
					if(thisRoom.okMessage(target,leaveMsg)&&newRoom.okMessage(target,enterMsg))
					{
						thisRoom.send(target,leaveMsg);
						newRoom.bringMobHere(target,false);
						newRoom.send(target,enterMsg);
						target.tell("\n\r\n\r");
						CommonMsgs.look(target,true);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but nothing seems to happen.");


		// return whether it worked
		return success;
	}
}
