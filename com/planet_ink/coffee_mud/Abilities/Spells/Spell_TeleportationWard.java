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
public class Spell_TeleportationWard extends Spell
{
	public String ID() { return "Spell_TeleportationWard"; }
	public String name(){return "Teleportation Ward";}
	public String displayText(){return "(Teleportation Ward)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your teleportation ward dissipates.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(!msg.amISource(mob))
			&&(mob.location()!=msg.source().location())
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING))
			&&(!mob.amDead()))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE))
			{
				boolean summon=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
				boolean teleport=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
				boolean shere=(msg.source().location()==affected)
						||((affected instanceof Area)&&(((Area)affected).inMetroArea(msg.source().location().getArea())));
				if((!shere)&&(!summon)&&(teleport)&&(!CoffeeUtensils.doesHavePriviledgesHere(msg.source(),R)))
				{
					if((msg.source().location()!=null)&&(msg.source().location()!=R))
						msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
					R.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=null;
		if(commands.size()>0)
		{
			String s=Util.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(EnglishParser.containsString(mob.location().ID(),s)
			||EnglishParser.containsString(mob.location().name(),s)
			||EnglishParser.containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell("This place is already under a teleportation ward.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> seem(s) magically protected.":"^S<S-NAME> invoke(s) a teleportation ward upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&(CoffeeUtensils.doesOwnThisProperty(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMClass.DBEngine().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a teleportation ward, but fail(s).");

		return success;
	}
}
