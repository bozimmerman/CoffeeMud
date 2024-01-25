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
   Copyright 2021-2024 Bo Zimmerman

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
public class Spell_Deflection extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Deflection";
	}

	private final static String localizedName = CMLib.lang().L("Deflection");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Deflection)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ITEMS|CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your deflection ward dissipates."));

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool() instanceof Weapon)
		&&(msg.source()!=affected)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		&&(msg.target() != null))
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(msg.target() == mob)
				{
					Room R=CMLib.map().roomLocation(mob);
					if(R==null)
						R=msg.source().location();
					R.showHappens(CMMsg.MSG_OK_VISUAL, L("A deflection ward sends the attack from @x1 off target.",msg.tool().name()));
					if(CMLib.dice().rollPercentage()>super.getXLEVELLevel(invoker())*5)
						unInvoke();
					return false;
				}
			}
			else
			if(affected instanceof Item)
			{
				final Item I=(Item)affected;
				if(msg.target() == I)
				{
					Room R=CMLib.map().roomLocation(I);
					if(R==null)
						R=msg.source().location();
					R.showHappens(CMMsg.MSG_OK_VISUAL, L("A deflection ward sends the attack from @x1 off target.",msg.tool().name()));
					if(CMLib.dice().rollPercentage()>super.getXLEVELLevel(invoker())*5)
						unInvoke();
					return false;
				}
			}
			else
			if(affected instanceof Room)
			{
				final Room R=(Room)affected;
				final Room tR=CMLib.map().roomLocation(msg.target());
				if(R==tR)
				{
					R.showHappens(CMMsg.MSG_OK_VISUAL, L("A deflection ward sends the attack from @x1 off target.",msg.tool().name()));
					if(CMLib.dice().rollPercentage()>super.getXLEVELLevel(invoker())*5)
						unInvoke();
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target=null;
		if(commands.size()>0)
		{
			final String s=CMParms.combine(commands,0);
			final Area A=CMLib.map().areaLocation(mob);
			if(s.equalsIgnoreCase("room")
			||s.equalsIgnoreCase(CMLib.english().removeArticleLead(mob.location().Name())))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
			{
				if(A instanceof Boardable)
					target=((Boardable)A).getBoardableItem();
				if(target == null)
					target=mob.location();
			}
			else
			if(CMLib.english().containsString(mob.location().ID(),s)
			||CMLib.english().containsString(mob.location().name(),s)
			||CMLib.english().containsString(mob.location().displayText(),s))
				target=mob.location();
			else
			{
				if(A instanceof Boardable)
				{
					final Item I=((Boardable)A).getBoardableItem();
					if(I!=null)
					{
						if(CMLib.english().containsString(I.ID(),s)
						||CMLib.english().containsString(I.name(),s)
						||CMLib.english().containsString(I.displayText(),s))
							target=I;
					}
				}
			}
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null)
		{
			if(mob.isMonster())
				target=mob.location();
			else
				return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 is already warded with deflection.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> seem(s) magically protected."):L("^S<S-NAME> invoke(s) a deflection ward upon <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a deflection ward, but fail(s)."));

		return success;
	}
}
