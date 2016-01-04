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
   Copyright 2012-2016 Bo Zimmerman

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

public class Spell_PryingEye extends Spell
{
	@Override public String ID() { return "Spell_PryingEye"; }
	private final static String localizedName = CMLib.lang().L("Prying Eye");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	protected List<Integer> dirs=new LinkedList<Integer>();

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		final MOB invoker=invoker();
		if(invoker!=null)
			invoker.delEffect(this);
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			mob.setSession(null);
			if(invoker!=null)
				invoker.tell(L("The prying eye has closed."));
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			if(mob != invoker())
			{
				if((invoker()!=null)&&(!CMLib.flags().isInTheGame(invoker(), true)))
				{
					unInvoke();
					return false;
				}
				if(dirs.size()>0)
				{
					final int dir=dirs.remove(0).intValue();
					CMLib.tracking().walk(mob, dir, false, false);
					if(dirs.size()==0)
					{
						invoker().tell(L("\n\r^SThe eye has reached its destination and will soon close.^N^?"));
						super.tickDown=6;
					}
				}
			}
		}
		else
			unInvoke();
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell(L("You must specify directions for the eye to follow."));
			return false;
		}

		final List<Integer> directions=new LinkedList<Integer>();
		for(final Object o : commands)
		{
			final int dir=Directions.getDirectionCode(o.toString());
			if(dir<0)
			{
				mob.tell(L("'@x1' is not a valid direction.",o.toString()));
				return false;
			}
			directions.add(Integer.valueOf(dir));
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Ability otherA=mob.fetchEffect(ID());
		if(otherA!=null)
		{
			otherA.unInvoke();
			mob.delEffect(otherA);
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?L("A floating eye appears and begins moving around."):L("^S<S-NAME> invoke(s) a floating eye and begin(s) chanting directions!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Room R=mob.location();
				final MOB eyeM=CMClass.getMOB("StdMOB");
				eyeM.basePhyStats().setLevel(1);
				eyeM.basePhyStats().setDisposition(eyeM.basePhyStats().disposition() | PhyStats.IS_FLYING);
				eyeM.basePhyStats().setSensesMask(eyeM.basePhyStats().sensesMask() | PhyStats.CAN_NOT_HEAR);
				eyeM.setName(L("a floating eye"));
				eyeM.setDisplayText(L("a single eye floats around here"));
				CMLib.factions().setAlignment(eyeM,Faction.Align.NEUTRAL);
				eyeM.baseCharStats().setMyRace(CMClass.getRace("Unique"));
				eyeM.baseCharStats().getMyRace().startRacing(eyeM,false);
				eyeM.recoverMaxState();
				eyeM.resetToMaxState();
				eyeM.recoverPhyStats();
				eyeM.recoverCharStats();
				CMLib.leveler().fillOutMOB(eyeM,1);
				eyeM.baseState().setHitPoints(CMLib.dice().rollHP(1, 4));
				eyeM.baseState().setMovement(10000);
				eyeM.setMoney(0);
				eyeM.setLocation(R);
				eyeM.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				eyeM.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				eyeM.recoverCharStats();
				eyeM.recoverPhyStats();
				eyeM.recoverMaxState();
				eyeM.resetToMaxState();
				eyeM.bringToLife(R,true);
				CMLib.beanCounter().clearZeroMoney(eyeM,null);
				R.showOthers(eyeM,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
				eyeM.setStartRoom(null); // keep before postFollow for Conquest
				if(eyeM.amDead()||eyeM.amDestroyed())
					return false;
				eyeM.setSession(mob.session());
				beneficialAffect(mob,eyeM,asLevel,Ability.TICKS_ALMOST_FOREVER);
				final Spell_PryingEye A=(Spell_PryingEye)eyeM.fetchEffect(ID());
				if(A==null)
					eyeM.destroy();
				else
				{
					mob.addEffect(A);
					A.setAffectedOne(eyeM);
					A.dirs=directions;
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to invoke something, but fail(s)."));

		// return whether it worked
		return success;
	}
}
