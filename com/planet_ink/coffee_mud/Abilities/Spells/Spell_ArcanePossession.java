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
public class Spell_ArcanePossession extends Spell
{
	public String ID() { return "Spell_ArcanePossession"; }
	public String name(){return "Arcane Possession";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	private MOB owner=null;

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SELL))
		{
			unInvoke();
			if(affected!=null)	affected.delEffect(this);
		}
		else
		if(msg.amITarget(affected)
		&&(owner!=null)
		&&((msg.targetMinor()==CMMsg.TYP_WEAR)
		   ||(msg.targetMinor()==CMMsg.TYP_HOLD)
		   ||(msg.targetMinor()==CMMsg.TYP_WIELD))
		&&(msg.source()!=owner))
		{
			msg.source().location().show(msg.source(),null,affected,CMMsg.MSG_OK_ACTION,"<O-NAME> flashes and flies out of <S-HIS-HER> hands!");
			CommonMsgs.drop(msg.source(),affected,true,false);
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected instanceof Item)&&(text().length()>0))
		{
			Item I=(Item)affected;
			if((owner==null)&&(I.owner()!=null)
			&&(I.owner() instanceof MOB)
			&&(I.owner().Name().equals(text())))
			    owner=(MOB)I.owner();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) a <T-NAMESELF> tightly and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows slightly!");
				setMiscText(mob.Name());
				beneficialAffect(mob,target,0);
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}
