package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2014-2021 Bo Zimmerman

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
public class Skill_CurtainCall extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_CurtainCall";
	}

	private final static String localizedName = CMLib.lang().L("Curtain Call");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedDisplayText = CMLib.lang().L("(Curtain Call)");

	@Override
	public String displayText()
	{
		return localizedDisplayText;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"CURTAINCALL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_THEATRE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public void unInvoke()
	{
		MOB M=null;
		if(affected instanceof MOB)
			M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(super.canBeUninvoked()))
			M.tell(L("You have left the building."));
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if(R==null)
			{
				unInvoke();
				return false;
			}
			if((!R.isInhabitant(invoker())||(mob.isInCombat())))
			{
				unInvoke();
				return false;
			}

			if(!mob.isMonster())
				CMLib.commands().postStand(mob,true, false);
			if(CMLib.flags().isStanding(mob)
			&& CMLib.flags().isAliveAwakeMobileUnbound(mob, false)
			&& ((mob.curState().getHitPoints() < mob.maxState().getHitPoints())||(mob.curState().getMana() < mob.maxState().getMana())))
			{
				mob.doCommand(new XVector<String>("Bow"), MUDCmdProcessor.METAFLAG_FORCED);
				final int hpGain = (int)Math.round(CMath.mul(mob.maxState().getHitPoints(), 0.10));
				final int manaGain = (int)Math.round(CMath.mul(mob.maxState().getMana(), 0.10));
				final int moveLoss = (int)Math.round(CMath.mul(mob.maxState().getMovement(), 0.06 - (super.getXLOWCOSTLevel(invoker()) * 0.005)));
				if(mob.curState().getMovement() >= moveLoss)
				{
					mob.curState().adjMovement(-moveLoss, mob.maxState());
					CMLib.combat().postHealing(mob, mob, this, hpGain, CMMsg.MSG_HEALING, null);
					mob.curState().adjMana(manaGain, mob.maxState());
				}
				else
				{
					unInvoke();
					return false;
				}
			}
			else
			{
				unInvoke();
				return false;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell(L("There doesn't appear to be any other actors here."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		final Room R=mob.location();
		if(R==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> call(s) the curtain, motioning for everyone to take a bow."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				for(final MOB M : h)
				{
					if(!M.isInCombat())
					{
						final CMMsg msg2=CMClass.getMsg(mob,M,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),null);
						if(mob.location().okMessage(mob,msg2))
						{
							mob.location().send(mob,msg2);
							if(msg2.value()<=0)
							{
								beneficialAffect(mob,M,asLevel,10+super.getXLEVELLevel(mob));
							}
						}
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> call(s) the curtain, but nothing happens."));

		return success;
	}

}
