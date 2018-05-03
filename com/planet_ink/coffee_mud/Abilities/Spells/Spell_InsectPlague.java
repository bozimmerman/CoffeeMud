package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_InsectPlague extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_InsectPlague";
	}

	private final static String localizedName = CMLib.lang().L("Insect Plague");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Plague of Insects)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			room.recoverRoomStats();
			room.recoverRoomStats();
			room.showHappens(CMMsg.MSG_OK_VISUAL, L("The insect plague disperses."));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if (CMLib.dice().rollPercentage() < (20 + CMath.s_int(text())))
			{
				MOB M=R.fetchRandomInhabitant();
				if(M==invoker())
					M=R.fetchRandomInhabitant();
				if((M!=null)&&(M!=invoker()))
				{
					final MOB invoker=(invoker()!=null) ? invoker() : M;
					CMLib.combat().postDamage(invoker,M,this,CMLib.dice().roll(1,1+super.getXLEVELLevel(invoker),0),CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,L("<T-NAME> <T-IS-ARE> stung by the plague of insects!"));
					CMLib.combat().postRevengeAttack(M, invoker);
				}
			}
			else
			if(CMLib.dice().rollPercentage() > 50)
				R.showHappens(CMMsg.MSG_OK_ACTION, L("A plague of insects buzzes and swarms through the air."));
			else
			{
				final Ability A=R.fetchEffect("Farming");
				if((A!=null)&&(A.canBeUninvoked()))
				{
					R.showHappens(CMMsg.MSG_OK_ACTION, L("A plague of insects consumes the growing crop."));
					A.setMiscText("abort");
					R.delEffect(A);
					CMLib.threads().deleteTick(A, -1);
				}
				else
				if((R.myResource()>0)&&(CMath.bset(R.myResource(),RawMaterial.MATERIAL_VEGETATION)))
				{
					R.showHappens(CMMsg.MSG_OK_ACTION, L("A plague of insects consumed all the @x1 here.",RawMaterial.CODES.NAME(R.myResource()).toLowerCase()));
					R.setResource(0);
				}
				else
				{
					for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						final Ability fA=M.fetchEffect("Foraging");
						if((fA!=null)&&(fA.canBeUninvoked()))
						{
							R.showHappens(CMMsg.MSG_OK_ACTION, L("A plague of insects consumes the plant life here."));
							fA.setMiscText("abort");
							M.delEffect(fA);
						}
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.location().fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,L("An insect plague is already here!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob,target,auto), (auto?L("An insect plague descends here."):"^S<S-NAME> conjure(s) up a plague of insects!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=beneficialAffect(mob,mob.location(),asLevel,0);
				if(A!=null)
					A.setMiscText(""+super.getXLEVELLevel(mob));
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to conjure, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
