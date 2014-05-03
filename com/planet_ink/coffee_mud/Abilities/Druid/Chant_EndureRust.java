package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_EndureRust extends Chant
{
	@Override public String ID() { return "Chant_EndureRust"; }
	@Override public String name(){ return "Endure Rust";}
	@Override public String displayText(){return "(Endure Rust)";}
	@Override protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	@Override protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	@Override public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_OTHERS;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;}
	protected HashSet dontbother=new HashSet();

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(canBeUninvoked()))
			((MOB)affected).tell(_("Your rust endurance fades."));
		super.unInvoke();
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((((msg.target()==affected)&&(affected instanceof Item))
			||(msg.target() instanceof Item)&&(affected instanceof MOB)&&(((MOB)affected).isMine(msg.target())))
		&&(msg.targetMinor()==CMMsg.TYP_WATER))
		{
			if(!dontbother.contains(msg.target()))
			{
				final Room R=CMLib.map().roomLocation(affected);
				dontbother.add(msg.target());
				if(R!=null)
					R.show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,"<T-NAME> resist(s) the oxidizing affects.");
			}
			return false;
		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=this.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;
		if(target instanceof Item)
		{
		}
		else
		if(target instanceof MOB)
		{
		}
		else
		{
			mob.tell("This chant won't affect "+target.name(mob)+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
		{
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but fail(s).");
		}
		final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, causing a rust proof film to envelope <T-HIM-HER>!^?");
		if(mob.location().okMessage(mob,msg))
		{
			dontbother.clear();
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
