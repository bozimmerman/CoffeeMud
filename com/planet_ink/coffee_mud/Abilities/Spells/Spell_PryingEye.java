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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_PryingEye extends Spell
{
	public String ID() { return "Spell_PryingEye"; }
	public String name(){return "Prying Eye";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	protected List<Integer> dirs=new LinkedList<Integer>();
	
	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		MOB invoker=invoker();
		if(invoker!=null)
			invoker.delEffect(this);
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) 
				mob.setLocation(null);
			mob.setSession(null);
			if(invoker!=null)
				invoker.tell("The prying eye has closed.");
			mob.destroy();
		}
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) 
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			MOB mob=(MOB)ticking;
			if(mob != invoker())
			{
				if((invoker()!=null)&&(!CMLib.flags().isInTheGame(invoker(), true)))
				{
					unInvoke();
					return false;
				}
				if(dirs.size()>0)
				{
					int dir=dirs.remove(0).intValue();
					CMLib.tracking().walk(mob, dir, false, false);
					if(dirs.size()==0)
					{
						invoker().tell("\n\r^SThe eye has reached its destination and will soon close.^N^?");
						super.tickDown=6;
					}
				}
			}
		}
		else
			unInvoke();
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("You must specify directions for the eye to follow.");
			return false;
		}
		
		List<Integer> directions=new LinkedList<Integer>();
		for(Object o : commands)
		{
			int dir=Directions.getDirectionCode(o.toString());
			if(dir<0)
			{
				mob.tell("'"+o.toString()+"' is not a valid direction.");
				return false;
			}
			directions.add(Integer.valueOf(dir));
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Ability otherA=mob.fetchEffect(ID());
		if(otherA!=null)
		{
			otherA.unInvoke();
			mob.delEffect(otherA);
		}
		
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"A floating eye appears and begins moving around.":"^S<S-NAME> invoke(s) a floating eye and begin(s) chanting directions!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room R=mob.location();
				MOB eyeM=CMClass.getMOB("StdMOB");
				eyeM.basePhyStats().setLevel(1);
				eyeM.basePhyStats().setDisposition(eyeM.basePhyStats().disposition() | PhyStats.IS_FLYING);
				eyeM.basePhyStats().setSensesMask(eyeM.basePhyStats().sensesMask() | PhyStats.CAN_NOT_HEAR);
				eyeM.setName("a floating eye");
				eyeM.setDisplayText("a single eye floats around here");
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
				R.showOthers(eyeM,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				eyeM.setStartRoom(null); // keep before postFollow for Conquest
				if(eyeM.amDead()||eyeM.amDestroyed()) 
					return false;
				eyeM.setSession(mob.session());
				beneficialAffect(mob,eyeM,asLevel,Ability.TICKS_ALMOST_FOREVER);
				Spell_PryingEye A=(Spell_PryingEye)eyeM.fetchEffect(ID());
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
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke something, but fail(s).");

		// return whether it worked
		return success;
	}
}
