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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Chant_StoneFriend extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_StoneFriend";
	}

	private final static String localizedName = CMLib.lang().L("Stone Friend");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Charmed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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

	@Override public long flags(){return 0;} // NOT charming because golems are resistant to that.

	protected MOB charmer=null;
	protected MOB getCharmer()
	{
		if(charmer!=null)
			return charmer;
		if((invoker!=null)&&(invoker!=affected))
			charmer=invoker;
		else
		if((text().length()>0)&&(affected instanceof MOB))
		{
			final Room R=((MOB)affected).location();
			if(R!=null)
				charmer=R.fetchInhabitant(text());
		}
		if(charmer==null)
			return invoker;
		return charmer;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.source()==mob.amFollowing()))
				unInvoke();
		if((msg.amISource(mob))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.target()==mob.amFollowing()))
		{
			mob.tell(L("You like @x1 too much.",mob.amFollowing().charStats().himher()));
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(!mob.isMonster())
		&&(msg.target() instanceof Room)
		&&((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_RECALL))
		&&(mob.amFollowing()!=null)
		&&(((Room)msg.target()).isInhabitant(mob.amFollowing())))
		{
			mob.tell(L("You don't want to leave your friend."));
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(mob.amFollowing()!=null)
		&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW))
		{
			mob.tell(L("You like @x1 too much.",mob.amFollowing().name()));
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		final MOB mob=(MOB)affecting();
		if((affected==mob)
		&&(mob!=getCharmer())
		&&((mob.amFollowing()==null)||(mob.amFollowing()!=getCharmer()))
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(getCharmer())))
			CMLib.commands().postFollow(mob,getCharmer(),true);
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(!mob.amDead()))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> free-will returns."));
			if(mob.amFollowing()!=null)
				CMLib.commands().postFollow(mob,null,false);
			CMLib.commands().postStand(mob,true);
			if(mob.isMonster())
			{
				if((CMLib.dice().rollPercentage()>50)
				||((mob.getStartRoom()!=null)
					&&(mob.getStartRoom().getArea()!=mob.location().getArea())
					&&(CMLib.flags().canMove(mob))
					&&(CMLib.flags().isAggressiveTo(mob,null)||(invoker==null)||(!mob.location().isInhabitant(invoker)))))
					CMLib.tracking().wanderAway(mob,true,true);
				else
				if((invoker!=null)&&(invoker!=mob))
					mob.setVictim(invoker);
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((!((MOB)target).charStats().getMyRace().racialCategory().equals("Earth Elemental"))
				&&(!((MOB)target).charStats().getMyRace().racialCategory().equals("Stone Golem"))
				&&(!((MOB)target).charStats().getMyRace().racialCategory().equals("Metal Golem")))
				return Ability.QUALITY_INDIFFERENT;

			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!target.charStats().getMyRace().racialCategory().equals("Earth Elemental"))
		   &&(!target.charStats().getMyRace().racialCategory().equals("Stone Golem"))
		   &&(!target.charStats().getMyRace().racialCategory().equals("Metal Golem")))
		{
			mob.tell(L("@x1 is not an stone/metal golem or earth elemental!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,-1)!=null;
					if(success)
					{
						if(target.isInCombat())
							target.makePeace(true);
						CMLib.commands().postFollow(target,mob,false);
						CMLib.combat().makePeaceInGroup(mob);
						if(target.amFollowing()!=mob)
							mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
