package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Thief_SilentRunning extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SilentRunning";
	}

	private final static String localizedName = CMLib.lang().L("Silent Running");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
	}

	private static final String[] triggerStrings = I(new String[] { "SSNEAK","SILENTRUN" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	public int	code	= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code = newCode;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		
		final Physical affected=this.affected;
		final MOB invoker = invoker();
		if((msg.source().riding()==affected)
		&&(affected instanceof SailingShip)
		&&(msg.source().isMonster())
		&&(msg.source().Name().equals(affected.Name())))
		{
			final SailingShip affShip = (SailingShip)affected;
			if((affShip.isInCombat())
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE))
			{
				unInvoke();
				affected.recoverPhyStats();
			}
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(affShip.getCurrentCourse().size()==0))
			{
				final Ability A;
				if((invoker != null)&&(invoker.fetchAbility(ID())!=null))
					A=invoker.fetchAbility("Thief_HideShip");
				else
				{
					A=CMClass.getAbility("Thief_HideShip");
					if(A!=null)
						A.setProficiency(100);
				}
				final Ability unInVokeMe=this;
				msg.addTrailerRunnable(new Runnable(){
					@Override
					public void run()
					{
						if((A!=null)&&(invoker!=null)&&(!affShip.isInCombat()))
							A.invoke(invoker, affected, false, 0);
						unInVokeMe.unInvoke();
						affected.delEffect(unInVokeMe);
					}
				});
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SNEAKING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}
		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You are on the floor!"));
			return false;
		}

		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;
		
		final Room R=mob.location();
		if(R==null)
			return false;
		
		final SailingShip ship;
		if((R.getArea() instanceof BoardableShip)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip))
		{
			ship=(SailingShip)((BoardableShip)R.getArea()).getShipItem();
		}
		else
		{
			mob.tell(L("You must be on a big sailing ship to rig it for silent running!"));
			return false;
		}
		
		if(ship.fetchEffect(ID())!=null)
		{
			mob.tell(L("Your ship is already rigged for silent running!"));
			return false;
		}
		
		final Room shipR=CMLib.map().roomLocation(ship);
		if((shipR==null)||(!CMLib.flags().isWaterySurfaceRoom(shipR))||(!ship.subjectToWearAndTear()))
		{
			mob.tell(L("You must be on a sailing ship to rig it for silent running!"));
			return false;
		}
		
		if(ship.isInCombat())
		{
			mob.tell(L("Your ship must not be in combat to rig for silent running!"));
			return false;
		}
		
		int direction=-1;
		StringBuilder course=new StringBuilder("");
		if(commands.size()>0)
		{
			for(int i=0;i<commands.size();i++)
			{
				direction=CMLib.directions().getDirectionCode(commands.get(i));
				if(direction < 0)
				{
					mob.tell(L("'@x1' is not a valid direction.",commands.get(0)));
					return false;
				}
				course.append(CMLib.directions().getDirectionName(direction)).append(" ");
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
		{
			beneficialVisualFizzle(mob,ship,L("<S-NAME> attempt(s) to rig <T-NAMESELF> for silent running and fail(s)."));
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,ship,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),L("<S-NAME> rig(s) <T-NAMESELF> for silent running."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				super.beneficialAffect(mob,ship,asLevel,Ability.TICKS_ALMOST_FOREVER);
				ship.recoverPhyStats();
				if(direction > 0)
				{
					String courseMsgStr="COURSE "+course.toString();
					final CMMsg huhMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,null,courseMsgStr,null);
					if(R.okMessage(mob,huhMsg))
						R.send(mob,huhMsg);
				}
			}
			else
				success=false;
		}
		return success;
	}
}
