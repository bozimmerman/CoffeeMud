package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_WhisperWard extends Chant implements Trap
{
	public String ID() { return "Chant_WhisperWard"; }
	public String name(){ return "Whisperward";}
	public String displayText(){return "(Whisperward)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	Room myRoomContainer=null;
	int myTrigger=CMMsg.TYP_ENTER;
	boolean waitingForLook=false;

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
    public Vector getTrapComponents() { return new Vector();}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{beneficialAffect(mob,E,qualifyingClassLevel+trapBonus,0); return (Trap)E.fetchEffect(ID());}

	public boolean disabled(){return false;}
	public boolean sprung(){return false;}
	public void disable(){unInvoke();}
	public void spring(MOB M)
	{
		doMyThing();
	}

	public void doMyThing()
	{
		if(invoker!=null)
			invoker.tell("** You hear the wind whisper to you; your ward has been triggered.");
		unInvoke();
		return;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);


		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if(msg.amITarget(myRoomContainer))
		{
			if((waitingForLook)&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			{
				doMyThing();
				return;
			}
			else
			if(msg.targetMinor()==myTrigger)
				waitingForLook=true;
		}
		else
		if(msg.amITarget(affected))
		{
			if((msg.targetMinor()==myTrigger)&&(!CMLib.flags().isSneaking(msg.source())))
			{
				doMyThing();
				return;
			}
			else
			if((myTrigger==CMMsg.TYP_GET)
			&&((msg.targetMinor()==CMMsg.TYP_OPEN)
				 ||(msg.targetMinor()==CMMsg.TYP_GIVE)
				 ||(msg.targetMinor()==CMMsg.TYP_DELICATE_HANDS_ACT)
				 ||(msg.targetMinor()==CMMsg.TYP_JUSTICE)
				 ||(msg.targetMinor()==CMMsg.TYP_GENERAL)
				 ||(msg.targetMinor()==CMMsg.TYP_LOCK)
				 ||(msg.targetMinor()==CMMsg.TYP_PULL)
				 ||(msg.targetMinor()==CMMsg.TYP_PUSH)
				 ||(msg.targetMinor()==CMMsg.TYP_UNLOCK)))
			{
				doMyThing();
				return;
			}
		}

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<2)
		{
			mob.tell("You must specify:\n\r What object you want the spell cast on.\n\r AND Whether it is triggered by TOUCH, HOLD, WIELD, WEAR, or someone ENTERing the same room. ");
			return false;
		}
		String triggerStr=((String)commands.lastElement()).trim().toUpperCase();

		if(triggerStr.startsWith("HOLD"))
			myTrigger=CMMsg.TYP_HOLD;
		else
		if(triggerStr.startsWith("WIELD"))
			myTrigger=CMMsg.TYP_WIELD;
		else
		if(triggerStr.startsWith("WEAR"))
			myTrigger=CMMsg.TYP_WEAR;
		else
		if(triggerStr.startsWith("TOUCH"))
			myTrigger=CMMsg.TYP_GET;
		else
		if(triggerStr.startsWith("ENTER"))
			myTrigger=CMMsg.TYP_ENTER;
		else
		{
			mob.tell("You must specify the trigger event that will cause the wind to whisper to you.\n\r'"+triggerStr+"' is not correct, but you can try TOUCH, WEAR, WIELD, HOLD, or ENTER.\n\r");
			return false;
		}

		Environmental target;
		String itemName=CMParms.combine(commands,0,commands.size()-1);
		if(itemName.equalsIgnoreCase("room"))
			target=mob.location();
		else
			target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target instanceof MOB)
		{
			mob.tell("You can't can't cast this on "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				beneficialAffect(mob,target,asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fizzles.");


		// return whether it worked
		return success;
	}
}
