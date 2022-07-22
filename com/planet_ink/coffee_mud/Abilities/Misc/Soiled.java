package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2005-2022 Bo Zimmerman

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
public class Soiled extends StdAbility
{
	@Override
	public String ID()
	{
		return "Soiled";
	}

	private final static String	localizedName	= CMLib.lang().L("Soiled");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Soiled)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SOIL" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)/2);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Environmental E=affected;
		if(E==null)
			return;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(E instanceof MOB)
			{
				final MOB mob=(MOB)E;
				mob.tell(L("You are no longer soiled."));
				final MOB following=((MOB)E).amFollowing();
				if((following!=null)
				&&(following.location()==mob.location())
				&&(CMLib.flags().isInTheGame(mob,true))
				&&(CMLib.flags().canBeSeenBy(mob,following)))
					following.tell(L("@x1 is no longer soiled.",E.name()));
			}
			else
			if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
				((MOB)((Item)E).owner()).tell(L("@x1 is no longer soiled.",E.name()));
		}
	}

	public String getDiaperSmell()
	{
		switch(CMLib.dice().roll(1,5,0))
		{
		case 1:
			return L("<T-NAME> is stinky!");
		case 2:
			return L("<T-NAME> smells like poo.");
		case 3:
			return L("<T-NAME> <T-HAS-HAVE> soiled <T-HIM-HERSELF>.");
		case 4:
			return L("Whew! <T-NAME> stinks!");
		case 5:
			return L("<T-NAME> must have let one go!");
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.source()==affected)
		||((affected instanceof Item)
			&&(((Item)affected).owner()==msg.source()))))
		{
			if((msg.sourceMajor(CMMsg.MASK_MOVE))
			&&(msg.source().riding()==null)
			&&(msg.source().location()!=null)
			&&(CMLib.flags().isWateryRoom(msg.source().location())))
				unInvoke();
			else
			if((msg.sourceMajor(CMMsg.MASK_MOVE))
			&&(msg.source().riding() instanceof Drink)
			&&(((Drink)msg.source().riding()).containsDrink()))
				unInvoke();
			else
			if(CMLib.commands().isHygienicMessage(msg))
				unInvoke();
			else
			if((affected instanceof Item)
			&&(((Item)affected).container() instanceof Drink)
			&&(msg.target()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_PUT)
			&&(((Drink)((Item)affected).container()).containsDrink()))
				unInvoke();
		}
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF))
		{
			final Physical target=(affected==null)?((myHost instanceof Physical)?(Physical)myHost:msg.source()):affected;
			final String smell=getDiaperSmell();
			if((CMLib.flags().canSmell(msg.source()))&&(smell!=null))
				msg.source().tell(msg.source(),target,null,smell);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(affected!=null)
		if(CMLib.dice().rollPercentage()==1)
		{
			final Environmental E=affected;
			final Room R=CMLib.map().roomLocation(E);
			if(R!=null)
			{
				MOB M=(E instanceof MOB)?(MOB)E:null;
				boolean killmob=false;
				if(M==null)
				{
					M=CMClass.getFactoryMOB();
					M.setName(affected.name());
					M.setDisplayText(L("@x1 is here.",affected.name()));
					M.setDescription("");
					if(M.location()!=R)
						M.setLocation(R);
					killmob=true;
				}
				else
				if((M.playerStats()!=null)&&(M.playerStats().getHygiene()<10000))
				{
					M.playerStats().setHygiene(10000);
					M.recoverCharStats();
				}
				final String smell=getDiaperSmell();
				if((smell!=null)
				&&(CMLib.flags().isInTheGame(M,true)))
				{
					final CMMsg msg=CMClass.getMsg(M,null,null,CMMsg.TYP_EMOTE|CMMsg.MASK_ALWAYS,smell);
					if(R.okMessage(M,msg))
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB mob=R.fetchInhabitant(m);
						if(CMLib.flags().canSmell(mob))
							mob.executeMsg(M,msg);
					}
				}
				if(killmob)
					M.destroy();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if((target==null)||(target.fetchEffect(ID())!=null))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Ability A=(Ability)copyOf();
		A.startTickDown(mob,target,Ability.TICKS_ALMOST_FOREVER);
		Environmental msgTarget=target;
		if(target instanceof CagedAnimal)
			msgTarget=((CagedAnimal)target).unCageMe();
		mob.location().show(mob,msgTarget,CMMsg.MSG_OK_VISUAL,L("<T-NAME> has soiled <T-HIM-HERSELF>!"));
		if(target instanceof MOB)
		{
			final Item pants=((MOB)target).fetchFirstWornItem(Wearable.WORN_WAIST);
			if((pants!=null)&&(pants.fetchEffect(ID())==null))
			{
				A=(Ability)copyOf();
				A.startTickDown((MOB)target,pants,Ability.TICKS_ALMOST_FOREVER);
			}
		}
		return true;
	}
}
