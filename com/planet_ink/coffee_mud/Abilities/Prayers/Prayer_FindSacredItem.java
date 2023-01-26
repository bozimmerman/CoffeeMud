package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2020-2023 Bo Zimmerman

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
public class Prayer_FindSacredItem extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_FindSacredItem";
	}

	private final static String localizedName = CMLib.lang().L("Find Sacred Item");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}


	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}


	public Item getHere(final MOB mob, final Room R, final String what, final boolean auto)
	{
		if(R==null)
			return null;
		final Room room=R;
		if(!CMLib.flags().canAccess(mob,room))
			return null;

		Environmental E=room.findItem(null,what);
		if((E instanceof Item)
		&&(CMLib.flags().canBeLocated((Item)E)))
			return (Item)E;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB M=room.fetchInhabitant(i);
			if(M==null)
				break;
			if(((!CMLib.flags().isCloaked(M))
			||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))
				&&(mob.phyStats().level()>=M.phyStats().level()))))
			{
				E=M.findItem(what);
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
				if((E==null)&&(SK!=null))
					E=SK.getShop().getStock(what,mob);
				if((E instanceof Item)
				&&(CMLib.flags().canBeLocated((Item)E)))
					return (Item)E;
			}
		}
		return null;
	}

	public boolean itsHere(final MOB mob, final Room R, final String what, final boolean auto, final String deityName)
	{
		final Item I=getHere(mob,R,what,auto);
		if(I==null)
			return false;
		final String infusion=CMLib.law().getClericInfused(I);
		return (infusion!=null)&&(infusion.equalsIgnoreCase(deityName));
	}

	protected TrackingLibrary.TrackingFlags getTrackingFlags()
	{
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingLibrary.TrackingFlag.NOAIR)
				.plus(TrackingLibrary.TrackingFlag.PASSABLE)
				.plus(TrackingLibrary.TrackingFlag.NOWATER);
		return flags;
	}

	protected List<Room> makeTheTrail(final MOB mob, final Room mobRoom, final String what, final int asLevel, final boolean auto, final String deityName)
	{
		List<Room> rooms=new ArrayList<Room>();
		TrackingLibrary.TrackingFlags flags = getTrackingFlags();
		flags.plus(TrackingLibrary.TrackingFlag.PASSABLE);
		final int range = 5+(adjustedLevel(mob,asLevel)/10)+(1*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
		final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mobRoom,flags,range);
		for (final Room R : checkSet)
		{
			if(this.itsHere(mob, R, what, auto, deityName))
				rooms.add(R);
		}

		flags = getTrackingFlags();
		if(rooms.size()>0)
		{
			rooms = CMLib.tracking().findTrailToAnyRoom(mobRoom,rooms,flags,range);
			if((rooms==null)||(rooms.size()==0))
				return null;
			return rooms;
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		if((commands.size()==0)&&(text().length()>0))
			commands.add(text());

		if(commands.size()==0)
		{
			mob.tell(L("Find what sacred item?."));
			return false;
		}
		final String what=CMParms.combine(commands,0);
		final String deityName=mob.charStats().getWorshipCharID();
		if(deityName.length()==0)
		{
			mob.tell(L("You must worship a deity to find one of his or her sacred items."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final boolean here=this.itsHere(mob, mob.location(), what, auto,deityName);
		final List<Room> theTrail = this.makeTheTrail(mob, mob.location(), what, asLevel, auto,deityName);
		if((success)&&((theTrail!=null)||(here)))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),
					L("^S<S-NAME> @x1 about the location of '@x2'.^?",prayWord(mob),what));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(here)
					mob.tell(L("Thou feelst thou shouldst look around."));
				else
				if(theTrail!=null)
				{
					Room R=mob.location();
					int direction = CMLib.tracking().trackNextDirectionFromHere(theTrail,R,false);
					if(direction >=0)
					{
						final StringBuilder str=new StringBuilder(L("First, thou shalt go "+CMLib.directions().getDirectionName(direction)));
						R=R.getRoomInDir(direction);
						while((R!=null)&&(R!=theTrail.get(0)))
						{
							direction = CMLib.tracking().trackNextDirectionFromHere(theTrail,R,false);
							if(direction<0)
							{
								str.append(L(", but then everything goes dark"));
								break;
							}
							switch(CMLib.dice().roll(1, 5, 0))
							{
							case 1:
								str.append(L(", and then shalt thou goest "+CMLib.directions().getDirectionName(direction)));
								break;
							case 2:
								str.append(L(", and then thou shalt goest "+CMLib.directions().getDirectionName(direction)));
								break;
							case 3:
								str.append(L(", and then goest thou "+CMLib.directions().getDirectionName(direction)));
								break;
							case 4:
								str.append(L(", and then "+CMLib.directions().getDirectionName(direction)));
								break;
							case 5:
								str.append(L(", and then goest shalt thou "+CMLib.directions().getDirectionName(direction)));
								break;
							}
							R=R.getRoomInDir(direction);
						}
						str.append(". ");
						mob.tell(str.toString());
					}
					else
						mob.tell(L("Your mind remains dark."));
				}

			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> @x1 about '@x2', but <S-IS-ARE> not answered.",prayWord(mob),what));

		return success;
	}
}
