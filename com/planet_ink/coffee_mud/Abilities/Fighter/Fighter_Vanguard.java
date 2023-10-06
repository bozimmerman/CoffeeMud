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
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_Vanguard extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Vanguard";
	}

	private final static String localizedName = CMLib.lang().L("Vanguard");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Vanguard)");

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

	private static final String[] triggerStrings =I(new String[] {"VANGUARD"});
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

	protected Pair<MOB,Integer> vanGuard = null;

	protected volatile MOB		lastDriverM		= null;
	protected volatile Room		lastCaravanRoom	= null;
	protected volatile long		lastAttack		= 0;
	protected volatile boolean	warned			= false;

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
		final Pair<MOB,Integer> vanGuard = this.vanGuard;
		if(vanGuard == null)
		{
			unInvoke();
			return false;
		}
		final MOB M = vanGuard.first;
		final Room vR = M.location();
		lastCaravanRoom = R;
		if((System.currentTimeMillis()-lastAttack)<4000)
		{
			final int max = 0;
			final List<Room> caravanRadiant = new XVector<Room>(R);
			{
				if((!(M.riding() instanceof MOB))
				&&(vR!=R))
				{
					M.tell(L("You resign from *mounted* vanguard duty."));
					unInvoke();
					return false;
				}
				if((vR!=R)
				&&(!caravanRadiant.contains(vR)))
				{
					List<Room> myRadiant = caravanRadiant;
					final int x = vanGuard.second.intValue();
					if(x != max)
						myRadiant = new XVector<Room>(R);
					final List<Room> trail = CMLib.tracking().findTrailToAnyRoom(vR, new XVector<Room>(myRadiant), flags, 10);
					if(trail == null)
					{
						unInvoke();
						return false;
					}
					M.tell(L("@x1 is under attack!",I.name(M)));
					int tries = 10;
					while((M.actions() > 0.0)
					&& (--tries>=0)
					&& (!myRadiant.contains(vR)))
					{
						final int dir = CMLib.tracking().trackNextDirectionFromHere(trail, vR, false);
						if(dir == 999) // arrival!
							break;
						else
						if((dir<0)||(dir > Directions.NUM_DIRECTIONS()))
						{
							unInvoke();
							return false;
						}
						final String dirName = CMLib.directions().getDirectionName(dir, CMLib.flags().getDirType(vR));
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
		else
		if((R != vR)
		&&(vR != null)
		&&((!(affected instanceof Boardable))||(vR.getArea() != ((Boardable)affected).getArea())))
		{
			final NavigableItem B = (affected instanceof NavigableItem)?(NavigableItem)affected:null;
			if((B != null)
			&&((this.lastDriverM==null)
				||(this.lastDriverM.location()==null)
				||(this.lastDriverM.location().getArea()!=B.getArea())
				||(!B.canSteer(this.lastDriverM,this.lastDriverM.location()))))
			{
				M.tell(L("Your driver has left their post."));
				unInvoke();
				return false;
			}
			final List<Room> trail = CMLib.tracking().findTrailToAnyRoom(R, new XVector<Room>(vR), flags, vanGuard.second.intValue());
			if(trail == null)
			{
				unInvoke();
				return false;
			}
			final List<String> dirs = new ArrayList<String>();
			Room tR = R;
			int dir = CMLib.tracking().trackNextDirectionFromHere(trail, tR, false);
			while((dir>=0)&&(dir < Directions.NUM_DIRECTIONS()))
			{
				final Room nR = tR.getRoomInDir(dir);
				if(nR == null)
					dir = -1;
				else
				{
					dirs.add(CMLib.directions().getDirectionName(dir, CMLib.flags().getDirType(tR)));
					tR = nR;
					dir = CMLib.tracking().trackNextDirectionFromHere(trail, tR, false);
				}
			}
			if((dir != 999) // arrival!
			&&((dir<0)||(dir > Directions.NUM_DIRECTIONS())))
			{
				unInvoke();
				return false;
			}
			if(!M.isInCombat())
			{
				List<String> cmds;
				if(affected instanceof NavigableItem)
					cmds = new XVector<String>("COURSE",CMParms.toListString(dirs));
				else
					cmds = new XVector<String>("GO",CMParms.toListString(dirs));
				this.lastDriverM.clearCommandQueue();
				this.lastDriverM.enqueCommands(new XVector<List<String>>(cmds), 0);
				this.lastDriverM.dequeCommand();
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
				if(vanGuard != null)
				{
					final MOB M = vanGuard.first;
					if((M!=null)
					&&(M.location() == R))
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

		if(canBeUninvoked() && (vanGuard!=null))
		{
			vanGuard.first.tell(L("You are no longer on vanguard duty."));
			vanGuard = null;
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
			final Fighter_Vanguard oldA = (Fighter_Vanguard)target.fetchEffect(ID());
			if((oldA == null)
			||(oldA.vanGuard==null)
			||(oldA.vanGuard.first != mob))
				mob.tell(L("You are not presently on vanguard duty for @x1.",target.name(mob)));
			else
				oldA.unInvoke();
			return false;
		}
		MOB driver = null;
		if((target instanceof Rideable)
		&&(!(target instanceof Boardable))
		&&((((Rideable)target).rideBasis() == Rideable.Basis.LAND_BASED)
			||(((Rideable)target).rideBasis() == Rideable.Basis.WAGON)))
		{
			// yay!
			for(final Enumeration<Rider> r= ((Rideable)target).riders(); r.hasMoreElements();)
			{
				final Rider R = r.nextElement();
				if((R instanceof MOB)
				&&(!CMLib.flags().isAnimalIntelligence((MOB)R)))
				{
					driver = (MOB)R;
					break;
				}
			}
		}
		else
		if((target instanceof NavigableItem)
		&&((((NavigableItem)target).navBasis() == Rideable.Basis.LAND_BASED)
			||(((NavigableItem)target).navBasis() == Rideable.Basis.WAGON)))
		{
			// yay!
			final NavigableItem nI = (NavigableItem)target;
			final Area A = nI.getArea();
			for(final Enumeration<Room> r = A.getProperMap();r.hasMoreElements();)
			{
				final Room R = r.nextElement();
				if((R != null) && (R.numInhabitants()>0) && (driver == null))
				{
					for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
					{
						final MOB M = m.nextElement();
						if((!CMLib.flags().isAnimalIntelligence(M))
						&&nI.canSteer(M,R))
						{
							driver = M;
							break;
						}
					}
				}
			}
		}
		else
		{
			mob.tell(L("@x1 does not appear to be a navagable caravan or wagon.",target.name(mob)));
			return false;
		}

		if(driver == null)
		{
			mob.tell(L("@x1 lacks anyone on board who is driving.",target.name(mob)));
			return false;
		}

		final Fighter_RearGuard fA=(Fighter_RearGuard)target.fetchEffect("Fighter_RearGuard");
		if(fA != null)
		{
			if(fA.rearGuards.containsKey(mob))
			{
				mob.tell(L("You can't be both vanguard AND rearguard!"));
				return false;
			}
		}

		if(!(mob.riding() instanceof MOB))
		{
			mob.tell(L("You must be mounted to be an effective vanguard."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?"":L("^S<S-NAME> volunteer(s) to be the vanguard for <T-NAME>^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Fighter_Vanguard oldA = (Fighter_Vanguard)target.fetchEffect(ID());
				if(oldA == null)
					oldA = (Fighter_Vanguard)beneficialAffect(mob,target,asLevel,0);
				final int myTime = super.getBeneficialTickdownTime(mob, target, 0, asLevel);
				if(myTime > oldA.tickDown)
					oldA.tickDown = myTime;
				final int distance = 2+(int)Math.round(Math.floor(CMath.div(super.getXLEVELLevel(mob), 4.0)));
				oldA.vanGuard = new Pair<MOB,Integer>(mob, Integer.valueOf(distance));
				oldA.lastDriverM = driver;
			}
		}
		else
			beneficialWordsFizzle(mob,null,auto?"":L("<S-NAME> thought about being the vanguard, but lose(s) <S-HIS-HER> nerve."));

		// return whether it worked
		return success;
	}
}
