package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_BearForaging extends StdAbility
{
	@Override
	public String ID()
	{
		return "Skill_BearForaging";
	}
	private final static String	localizedName	= CMLib.lang().L("Bear Foraging");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Bear Foraging)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BFORAGE","BEARFORAGE","BEARFORAGING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING;
	}

	protected List<Room>	theTrail		= null;
	public int				nextDirection	= -2;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		super.unInvoke();
	}

	public void performForage(final MOB mob, final Environmental E)
	{
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			final Environmental foodE=this.isFoodHere(mob, E);
			if((foodE instanceof Item)&&(((Item)foodE).owner() == M))
			{
				final Item I=(Item)foodE;
				Item dropI=I;
				final List<Item> containerSet=this.containerSet(mob, I);
				if((containerSet != null)&&(containerSet.size()>0))
					dropI=containerSet.get(0);
				CMMsg msg=CMClass.getMsg(mob,M,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,L("<S-NAME> suddenly attack(s) <T-NAME> ripping @x1 away from <T-HIM-HER>.",dropI.Name()));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					msg=CMClass.getMsg(M,dropI,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
					if(M.location().okMessage(M,msg))
					{
						M.location().send(mob,msg);
						if(M.location().isContent(dropI))
							performForage(mob, foodE);
					}
				}
			}
		}
		else
		if(E instanceof Item)
		{
			final Item I=(Item)E;
			final List<Item> containerSet=this.containerSet(mob, I);
			if((containerSet != null)&&(containerSet.size()>0))
			{
				for(final Item containerI : containerSet)
				{
					final CMMsg msg=CMClass.getMsg(mob,containerI,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> rips through <T-NAME>."));
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(containerI instanceof Container)
							((Container)containerI).emptyPlease(false);
						containerI.destroy();
					}
					else
						return;
				}
			}
			if((I.container()==null)&&(I instanceof Food))
			{
				CMLib.commands().postGet(mob, null, I, true);
				final Command eatC=CMClass.getCommand("Eat");
				if(eatC != null)
				{
					try
					{
						eatC.execute(mob, new XVector<String>("EAT",I.Name()), 0);
					}
					catch (final IOException e)
					{
						Log.errOut(e);
					}
				}
			}
		}
	}

	protected List<Item> containerSet(final MOB mob, Item I)
	{
		final List<Item> containers=new ArrayList<Item>();
		int tries=99;
		while((I.container()!=null)&&(I.container()!=I)&&(--tries>0))
 		{
			I=I.container();
			if((!CMLib.utensils().canBePlayerDestroyed(mob,I,false,true))
			||(CMLib.flags().isABonusItems(I))
			||(I instanceof MiscMagic))
				return null;
			switch(I.material() & RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_CLOTH:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_PAPER:
				containers.add(I);
				break;
			default:
				return null;
			}
		}
		return containers;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			final MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				final Environmental E=isFoodHere(mob,mob.location());
				if(E!=null)
				{
					mob.tell(L("You found a meal!"));
					performForage(mob,E);
				}
				else
					mob.tell(L("The trail dries up here."));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				final Environmental E=isFoodHere(mob,mob.location());
				if(E==null)
					mob.tell(L("The trail dries up here."));
				else
				{
					mob.tell(L("You found a meal!"));
					performForage(mob,E);
				}
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("The way home seems to continue @x1.",CMLib.directions().getDirectionName(nextDirection)));
				if(mob.isMonster())
				{
					final Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						final int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().walk(mob,dir,false,false);
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
		else
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target() instanceof Room)
		&&(msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				final Environmental E=isFoodHere((MOB)affected,(Room)msg.target());
				if(E!=null)
				{
					((MOB)affected).tell(L("You sense a meal here."));
					performForage(mob,E);
					unInvoke();
				}
			}
			else
			{
				final Environmental E=isFoodHere((MOB)affected,(Room)msg.target());
				if(E!=null)
				{
					final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
					msg.addTrailerMsg(msg2);
					// targets the above
				}
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	private Environmental isFoodHere(final MOB mob, final Environmental E)
	{
		if(E==null)
			return null;
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I!=null)
				&&(I instanceof Food)
				&&(containerSet(mob,I)!=null))
					return I;
			}
		}
		else
		if(E instanceof Item)
		{
			if((E instanceof Food)
			&&(containerSet(mob,(Item)E)!=null))
				return E;
		}
		else
		if(E instanceof Room)
		{
			final Room room=(Room)E;
			for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I!=null)
				&&(I instanceof Food)
				&&(containerSet(mob,I)!=null))
					return I;
			}
			for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(mob.mayIFight(M))
				&&(M!=mob))
				{
					final Environmental I=this.isFoodHere(mob, M);
					if(I!=null)
						return M;
				}
			}
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V)
			A.unInvoke();
		if(V.size()>0)
		{
			mob.tell(L("You stop tracking."));
			if(commands.size()==0)
				return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Environmental hereE=isFoodHere(mob, mob.location());
		if(hereE!=null)
		{
			mob.tell(L("You smell something RIGHT HERE!!"));
			performForage(mob,hereE);
			return true;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		final ArrayList<Room> rooms=new ArrayList<Room>();
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingLibrary.TrackingFlag.NOAIR);
		final int range=3 + (adjustedLevel(mob,asLevel)/15) + (super.getXMAXRANGELevel(mob));
		final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,range);
		for (final Room room : checkSet)
		{
			final Room R=CMLib.map().getRoom(room);
			if(isFoodHere(mob,R)!=null)
				rooms.add(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findTrailToAnyRoom(mob.location(),rooms,flags,range);

		if((success)&&(theTrail!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,auto?L("<S-NAME> begin(s) foraging!"):L("<S-NAME> begin(s) foraging for someone elses food."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Skill_BearForaging newOne=(Skill_BearForaging)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to forage for someone elses food, but fail(s)."));

		return success;
	}
}
