package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_RearGuard extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_RearGuard";
	}

	private final static String localizedName = CMLib.lang().L("Rearguard");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Rearguard)");

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

	private static final String[] triggerStrings =I(new String[] {"REARGUARD"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_TRAVEL;
	}

	protected SHashtable<MOB,Integer> rearGuards = null;

	protected volatile Room lastCaravanRoom = null;
	protected volatile long lastAttack = 0;

	protected final static TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.PASSABLE);


	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!super.tick(ticking,tickID))
		||(!(affected instanceof Item)))
			return false;
		final Item I = (Item)affected;
		final Room R = CMLib.map().roomLocation(I);
		if(R == null)
		{
			unInvoke();
			return false;
		}
		final boolean combatRun = ((System.currentTimeMillis()-lastAttack)<4000);
		if((lastCaravanRoom==null)
		||(R != lastCaravanRoom)
		||(combatRun))
		{
			lastCaravanRoom = R;
			int max = 0;
			if(rearGuards!=null)
			{
				if(!combatRun)
				{
					for(final Integer x : rearGuards.values())
					{
						if(x.intValue() > max)
							max = x.intValue();
					}
				}
				List<Room> caravanRadiant = null;
				if(max == 0)
					caravanRadiant = new XVector<Room>(R);
				else
					caravanRadiant = CMLib.tracking().getRadiantRooms(R, flags, max);
				for(final MOB M : rearGuards.keySet())
				{
					if((!(M.riding() instanceof MOB))
					&&(M.location()!=R))
					{
						M.tell(L("You resign from *mounted* rearguard duty."));
						if(rearGuards.size()==1)
						{
							unInvoke();
							return false;
						}
						else
						{
							rearGuards.remove(M);
							continue;
						}
					}
					if((M.location()!=R)
					&&(!caravanRadiant.contains(M.location())))
					{
						List<Room> myRadiant = caravanRadiant;
						final int x = rearGuards.get(M).intValue();
						if(x != max)
						{
							if((max == 0)||(combatRun))
								myRadiant = new XVector<Room>(R);
							else
								myRadiant = CMLib.tracking().getRadiantRooms(R, flags, x);
						}
						final List<Room> trail = CMLib.tracking().findTrailToAnyRoom(M.location(), new XVector<Room>(myRadiant), flags, 10);
						if(trail == null)
						{
							if(rearGuards.size()==1)
							{
								unInvoke();
								return false;
							}
							else
							{
								M.tell(L("You resign from rearguard duty."));
								rearGuards.remove(M);
								break;
							}
						}
						if(combatRun)
							M.tell(L("@x1 is under attack!",I.name(M)));
						int tries = 10;
						while((M.actions() > 0.0)
						&& (--tries>=0)
						&& (!myRadiant.contains(M.location())))
						{
							final int dir = CMLib.tracking().trackNextDirectionFromHere(trail, M.location(), false);
							if(dir == 999) // arrival!
								break;
							else
							if((dir<0)||(dir > Directions.NUM_DIRECTIONS()))
							{
								if(rearGuards.size()==1)
								{
									unInvoke();
									return false;
								}
								else
								{
									M.tell(L("You resign from rearguard duty."));
									rearGuards.remove(M);
									break;
								}
							}
							final String dirName = CMLib.directions().getDirectionName(dir, CMLib.flags().getDirType(M.location()));
							final boolean flee = M.isInCombat();
							final List<String> cmds;
							if(flee)
								cmds = new XVector<String>("FLEE",dirName);
							else
								cmds = new XVector<String>(dirName);
							M.clearCommandQueue();
							M.enqueCommands(new XVector<List<String>>(cmds), 0);
							M.dequeCommand();
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.target() == affected)
		{
			if((msg.tool() instanceof AmmunitionWeapon)
			&&((msg.targetMinor()==CMMsg.MSG_NOISYMOVEMENT)
				||(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)))
			{
				final Physical P = affected;
				if(P == null)
					return true;
				lastAttack = System.currentTimeMillis();
				final Room R = CMLib.map().roomLocation(P);
				for(final MOB M : rearGuards.keySet())
				{
					if(M.location() == R)
					{
						if((CMLib.dice().rollPercentage() <= 10 + super.getXLEVELLevel(M))
						&&(M.riding() instanceof MOB))
						{
							if(R.show(M, msg.source(), CMMsg.MSG_NOISYMOVEMENT,
									L("<S-NAME> harass(es) <T-NAME>, preventing <T-HIS-HER> attack on @x1.",P.name(msg.source()))))
							{
								return false;
							}
						}
					}
				}
			}
			else
			if(CMath.bset(msg.targetMinor(), CMMsg.MASK_MALICIOUS))
				lastAttack = System.currentTimeMillis();
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();

		if(canBeUninvoked() && (rearGuards!=null))
		{
			for(final MOB M : rearGuards.keySet())
				M.tell(L("You are no longer on rearguard duty."));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		boolean stop=false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("stop")))
		{
			commands.remove(commands.size()-1);
			stop=true;
		}
		final Item target = super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_ROOMONLY);
		if(target == null)
			return false;
		if(stop)
		{
			final Fighter_RearGuard oldA = (Fighter_RearGuard)target.fetchEffect(ID());
			if((oldA == null)
			||(oldA.rearGuards==null)
			||(!oldA.rearGuards.containsKey(mob)))
				mob.tell(L("You are not presently on rearguard duty to @x1.",target.name(mob)));
			else
			if(oldA.rearGuards.size()==1)
				oldA.unInvoke();
			else
			{
				oldA.rearGuards.remove(mob);
				mob.tell(L("You cease your own rearguard duties to @x1.",target.name(mob)));
			}
			return false;
		}
		if((target instanceof Rideable)
		&&(!(target instanceof Boardable))
		&&((((Rideable)target).rideBasis() == Rideable.Basis.LAND_BASED)
			||(((Rideable)target).rideBasis() == Rideable.Basis.WAGON)))
		{} // yay!
		else
		if((target instanceof NavigableItem)
		&&((((NavigableItem)target).navBasis() == Rideable.Basis.LAND_BASED)
			||(((NavigableItem)target).navBasis() == Rideable.Basis.WAGON)))
		{} // yay!
		else
		{
			mob.tell(L("@x1 does not appear to be a navagable caravan or wagon.",target.name(mob)));
			return false;
		}

		if(!(mob.riding() instanceof MOB))
		{
			mob.tell(L("You must be mounted to be an effective rearguard."));
			return false;
		}
		final Fighter_Vanguard fA=(Fighter_Vanguard)target.fetchEffect("Fighter_Vanguard");
		if(fA != null)
		{
			if((fA.vanGuard != null)&&(fA.vanGuard.first == mob))
			{
				mob.tell(L("You can't be both vanguard AND rearguard!"));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?"":L("^S<S-NAME> volunteer(s) to be a rearguard for <T-NAME>^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Fighter_RearGuard oldA = (Fighter_RearGuard)target.fetchEffect(ID());
				if(oldA == null)
					oldA = (Fighter_RearGuard)beneficialAffect(mob,target,asLevel,0);
				final int myTime = super.getBeneficialTickdownTime(mob, target, 0, asLevel);
				if(myTime > oldA.tickDown)
					oldA.tickDown = myTime;
				if(oldA.rearGuards == null)
					oldA.rearGuards = new SHashtable<MOB,Integer>();
				final int distance = (int)Math.round(Math.floor(CMath.div(super.getXLEVELLevel(mob), 4.0)));
				if(!oldA.rearGuards.containsKey(mob))
					oldA.rearGuards.put(mob, Integer.valueOf(distance));
			}
		}
		else
			beneficialWordsFizzle(mob,null,auto?"":L("<S-NAME> thought about being a rearguard, lose(s) <S-HIS-HER> nerve."));

		// return whether it worked
		return success;
	}
}
