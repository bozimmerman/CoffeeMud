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
public class Spell_Shove extends Spell
{
	public String ID() { return "Spell_Shove"; }
	public String name(){return "Shove";}
	public String displayText(){return "(Shoved Down)";}
	public int maxRange(){return adjustedMaxInvokerRange(4);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Tickable.TICKID_MOB;}
	public boolean doneTicking=false;
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
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
		    if(mob.isMonster())
		    {
		        for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		            if((mob.location().getRoomInDir(d)!=null)
		            &&(mob.location().getExitInDir(d)!=null)
		            &&(mob.location().getExitInDir(d).isOpen()))
		                dir=d;
		    }
		    if(dir<0)
		    {
    			mob.tell("Shove whom which direction?  Try north, south, east, or west...");
    			return false;
		    }
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

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> get(s) shoved back!":"<S-NAME> incant(s) and shove(s) at <T-NAMESELF>.");
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				if((msg.value()<=0)&&(target.location()==mob.location()))
				{
					mob.location().send(mob,msg);
					target.makePeace();
					Room newRoom=mob.location().getRoomInDir(dir);
					Room thisRoom=mob.location();
					CMMsg enterMsg=CMClass.getMsg(target,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> fly(s) in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir))+".");
					CMMsg leaveMsg=CMClass.getMsg(target,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> <S-IS-ARE> shoved forcefully into the air and out "+Directions.getInDirectionName(dir)+".");
					if(thisRoom.okMessage(target,leaveMsg)&&newRoom.okMessage(target,enterMsg))
					{
						thisRoom.send(target,leaveMsg);
						newRoom.bringMobHere(target,false);
						newRoom.send(target,enterMsg);
						target.tell("\n\r\n\r");
						CMLib.commands().postLook(target,true);
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
