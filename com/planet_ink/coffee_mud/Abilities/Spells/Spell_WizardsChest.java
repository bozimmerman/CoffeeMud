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
public class Spell_WizardsChest extends Spell
{
	public String ID() { return "Spell_WizardsChest"; }
	public String name(){return "Wizards Chest";}
	public String displayText(){return "(Wizard Chest)";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return true;

		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if(((!msg.amITarget(affected))&&(msg.tool()!=affected))
		||(msg.source()==invoker())
		||((invoker()!=null)&&(invoker().Name().equals(text()))))
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_OPEN:
			mob.tell(affected.name()+" appears to be magically protected.");
			return false;
		case CMMsg.TYP_UNLOCK:
			mob.tell(affected.name()+" appears to be magically protected.");
			return false;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
			mob.tell(affected.name()+" appears to be magically protected.");
			return false;
		default:
			break;
		}
		return true;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.target()==affected)
	    &&((msg.source()==invoker())||(msg.source().Name().equals(text())))
	    &&(msg.sourceMessage()!=null)
	    &&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
	    &&(msg.sourceMessage().toUpperCase().indexOf("OPEN")>=0)
	    &&(affected instanceof Container))
	    {
	        Container container=(Container)affected;
			container.setLidsNLocks(container.hasALid(),true,container.hasALock(),false);
			msg.addTrailerMsg(new FullMsg(msg.source(),affected,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> pop(s) open!"));
	    }
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Enchant what?.");
			return false;
		}
		Environmental target=null;
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((!(target instanceof Container))||(!((Container)target).hasALock())||(!((Container)target).hasALid()))
		{
			mob.tell("You can only enchant the locks on open containers with lids.");
			return false;
		}
		
		if(!((Container)target).isOpen())
		{
		    mob.tell(target.name()+" must be opened before this magic will work.");
		    return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already a wizards chest!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Container)
				{
					beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE/2);
					Container container=(Container)target;
					container.setLidsNLocks(container.hasALid(),false,container.hasALock(),true);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> look(s) well protected!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}

