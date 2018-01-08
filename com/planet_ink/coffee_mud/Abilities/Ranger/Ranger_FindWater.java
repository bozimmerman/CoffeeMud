package com.planet_ink.coffee_mud.Abilities.Ranger;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Ranger_FindWater extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_FindWater";
	}

	private final static String	localizedName	= CMLib.lang().L("Find Water");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(finding water)");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FINDWATER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_NATURELORE;
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

	@Override
	public boolean tick(Tickable ticking, int tickID)
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
				mob.tell(waterHere(mob,mob.location(),null));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				if(waterHere(mob,mob.location(),null).length()==0)
					mob.tell(L("The water trail dries up here."));
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("The water trail seems to continue @x1.",CMLib.directions().getDirectionName(nextDirection)));
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
		   &&(msg.target()!=null)
		   &&(msg.amISource((MOB)affected))
		   &&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				final String str=waterHere((MOB)affected,msg.target(),null);
				if(str.length()>0)
					((MOB)affected).tell(str);
			}
			else
			if((msg.target()!=null)
			&&(waterHere((MOB)affected,msg.target(),null).length()>0)
			&&(msg.source()!=msg.target()))
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	public String waterCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null)
			return "";
		if(I.container()==container)
		{
			if(((I instanceof Drink))
			&&(((Drink)I).containsDrink())
			&&(CMLib.flags().canBeSeenBy(I,mob)))
				msg.append(L("@x1 contains some sort of liquid.\n\r",I.name()));
		}
		else
		if((I.container()!=null)&&(I.container().container()==container))
		{
			if(msg.toString().indexOf(I.container().name()+" contains some sort of liquid.")<0)
				msg.append(L("@x1 contains some sort of liquid.\n\r",I.container().name()));
		}
		return msg.toString();
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	public String waterHere(MOB mob, Environmental E, Item container)
	{
		final StringBuffer msg=new StringBuffer("");
		if(E==null)
			return msg.toString();
		if((E instanceof Room)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			final Room room=(Room)E;
			if(CMLib.flags().isWateryRoom(room))
				msg.append(L("Your water-finding senses are saturated.  This is a very wet place.\n\r"));
			else
			if(CMath.bset(room.getClimateType(),Places.CLIMASK_WET))
				msg.append(L("Your water-finding senses are saturated.  This is a damp place.\n\r"));
			else
			if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_RAIN)
			||(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_THUNDERSTORM))
				msg.append(L("It is raining here! Your water-finding senses are saturated!\n\r"));
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				msg.append(L("It is hailing here! Your water-finding senses are saturated!\n\r"));
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
				msg.append(L("It is snowing here! Your water-finding senses are saturated!\n\r"));
			else
			{
				for(int i=0;i<room.numItems();i++)
				{
					final Item I=room.getItem(i);
					waterCheck(mob,I,container,msg);
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					final MOB M=room.fetchInhabitant(m);
					if((M!=null)&&(M!=mob))
						msg.append(waterHere(mob,M,null));
				}
			}
		}
		else
		if((E instanceof Item)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			waterCheck(mob,(Item)E,container,msg);
			msg.append(waterHere(mob,((Item)E).owner(),(Item)E));
		}
		else
		if((E instanceof MOB)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).numItems();i++)
			{
				final Item I=((MOB)E).getItem(i);
				final StringBuffer msg2=new StringBuffer("");
				waterCheck(mob,I,container,msg2);
				if(msg2.length()>0)
					return L("@x1 is carrying some liquids.",E.name());
			}
			final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
			if(SK!=null)
			{
				final StringBuffer msg2=new StringBuffer("");
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if(E2 instanceof Item)
						waterCheck(mob,(Item)E2,container,msg2);
					if(msg2.length()>0)
						return L("@x1 has some liquids in stock.",E.name());
				}
			}
		}
		return msg.toString();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V) A.unInvoke();
		if(V.size()>0)
		{
			mob.tell(L("You stop tracking."));
			if(commands.size()==0)
				return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final String here=waterHere(mob,mob.location(),null);
		if(here.length()>0)
		{
			mob.tell(here);
			return true;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		final Vector<Room> rooms=new Vector<Room>();
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingLibrary.TrackingFlag.NOAIR);
		int range=60 + (2*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
		final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,range);
		for (final Room room : checkSet)
		{
			final Room R=CMLib.map().getRoom(room);
			if(waterHere(mob,R,null).length()>0)
				rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findTrailToAnyRoom(mob.location(),rooms,flags,range);

		if((success)&&(theTrail!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,auto?L("<S-NAME> begin(s) sniffing around for water!"):L("<S-NAME> begin(s) sensing water."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ranger_FindWater newOne=(Ranger_FindWater)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to find water, but fail(s)."));

		return success;
	}
}
