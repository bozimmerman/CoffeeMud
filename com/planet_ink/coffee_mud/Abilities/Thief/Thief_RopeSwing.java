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

public class Thief_RopeSwing extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_RopeSwing";
	}

	private final static String	localizedName	= CMLib.lang().L("Rope Swing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Swinging on Ropes)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ROPESWING" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_BINDING;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected int	code		= 0;
	protected int	newDistance	= 0;
	protected MOB	newTarget	= null;

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
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.addAmbiance("swinging on ropes");
		affectableStats.setArmor(affectableStats.armor()+10+adjustedLevel(invoker,0));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final Physical affected=this.affected;
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		if((msg.amISource((MOB)affected))
		&&(msg.tool()!=this)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))))
		{
			if(msg.sourceMinor()!=CMMsg.TYP_WEAPONATTACK)
				msg.source().tell(L("You can't do that while swinging from a rope."));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if((mob.location()!=null)&&(!mob.amDead()))
				{
					if((this.newTarget!=null)
					&&(!newTarget.amDead())
					&&(newTarget.location()==mob.location()))
					{
						mob.location().show(mob,newTarget,CMMsg.MSG_OK_VISUAL,L("<S-NAME> land(s) back on the deck in front of <T-NAME>."));
						mob.setVictim(newTarget);
						if(newTarget.getVictim()==null)
							newTarget.setVictim(mob);
					}
					else
						mob.location().show(mob,newTarget,CMMsg.MSG_OK_VISUAL,L("<S-NAME> land(s) back on the deck."));
					mob.setRangeToTarget(this.newDistance);
					final MOB vicM=mob.getVictim();
					if((vicM!=null)&&(vicM.getVictim()==mob))
						vicM.setRangeToTarget(this.newDistance);
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
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
		
		if((R.getArea() instanceof BoardableShip)
		&&(R.domainType()==Room.DOMAIN_OUTDOORS_CITY))
		{
			// this is good
		}
		else
		{
			mob.tell(L("You must be on the deck of a ship to swing from its ropes!"));
			return false;
		}

		String parms=CMParms.combine(commands);
		if(parms.length()==0)
		{
			mob.tell(L("You need to either specify a new enemy target to swing over into melee range to, or a distance to swing away from your current target."));
			return false;
		}

		newDistance	= 0;
		newTarget	= null;
		
		if(CMath.isInteger(parms))
		{
			newDistance = CMath.s_int(parms);
			if((newDistance<0)||(newDistance>R.maxRange()))
			{
				mob.tell(L("That is not a valid distance.  Try 0 - @x1.",""+R.maxRange()));
				return false;
			}
			if(!mob.isInCombat())
			{
				mob.tell(L("You need to be in combat to change your distance!"));
				return false;
			}
		}
		else
		{
			newTarget=this.getTarget(mob,commands,givenTarget);
			if(newTarget==null)
				return false;
			if(newTarget == mob.getVictim())
			{
				newTarget=null;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String msgStr;
			if((newTarget != null)&&(mob.getVictim()!=newTarget))
				msgStr=L("<S-NAME> begin(s) swinging on deck ropes to get over to <T-NAME>.");
			else
				msgStr=L("<S-NAME> begin(s) swinging around on the deck ropes.");
			final CMMsg msg=CMClass.getMsg(mob,newTarget,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,auto?L("<T-NAME> swing(s) on ropes!"):msgStr);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_RopeSwing swing = (Thief_RopeSwing)beneficialAffect(mob,mob,asLevel,1);
				if(swing != null)
				{
					swing.newDistance = newDistance;
					swing.newTarget = newTarget;
				}
				for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					MOB M=m.nextElement();
					if((M!=null)
					&&(M!=mob)
					&&(M.getVictim()==mob))
						M.setRangeToTarget(R.maxRange());
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) grab a deck rope to swing on, but miss(es)."));
		return success;
	}
}
