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
public class Spell_FakeSpring extends Spell
{
	public String ID() { return "Spell_FakeSpring"; }
	public String name(){return "Fake Spring";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		Item spring=(Item)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(spring!=null))
		{
			Room SpringLocation=CoffeeUtensils.roomLocation(spring);
			spring.destroy();
			SpringLocation.recoverRoomStats();
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(affected))
		{
			if(msg.targetMinor()==CMMsg.TYP_DRINK)
			{
				if(msg.othersMessage()!=null)
					msg.source().location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_QUIETMOVEMENT,msg.othersMessage());
				msg.source().tell("You have drunk all you can.");
				return false;
			}
		}
		else
		if((msg.tool()!=null)&&(msg.tool()==affected)&&(msg.target()!=null)&&(msg.target() instanceof Drink))
		{
			if(msg.targetMinor()==CMMsg.TYP_FILL)
			{
				msg.source().tell(msg.target().name()+" is full.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);

	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell dramatically.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=CMClass.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				Drink W=(Drink)CMClass.getItem("GenWater");
				W.setName(newItem.Name());
				W.setDisplayText(newItem.displayText());
				W.setDescription(newItem.description());
				W.baseEnvStats().setWeight(newItem.baseEnvStats().weight());
				Sense.setGettable(((Item)W),false);
				W.setThirstQuenched(0);
				W.recoverEnvStats();
				mob.location().addItem((Item)W);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				if(CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
				{
					Ability A=(Ability)copyOf();
					A.setInvoker(mob);
					W.addNonUninvokableEffect(A);
				}
				else
					beneficialAffect(mob,W,asLevel,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
