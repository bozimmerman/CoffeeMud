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
   Copyright 2003-2024 Bo Zimmerman

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
public class Spell_KnowOrigin extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_KnowOrigin";
	}

	private final static String localizedName = CMLib.lang().L("Know Origin");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	public Pair<Room,Environmental> origin(final MOB mob, final Environmental meThang)
	{
		if(meThang instanceof LandTitle)
			return new Pair<Room,Environmental>(((LandTitle)meThang).getATitledRoom(), null);
		else
		if(meThang instanceof MOB)
			return new Pair<Room,Environmental>(((MOB)meThang).getStartRoom(), null);
		else
		if(meThang instanceof Item)
		{
			final Item me=(Item)meThang;
			try
			{
				// check mobs worn items first!
				final String srchStr="$"+me.Name()+"$";
				Environmental E=CMLib.hunt().findFirstShopStocker(CMLib.map().rooms(), mob, srchStr, 10);
				if(E!=null)
					return new Pair<Room,Environmental>(CMLib.map().getStartRoom(E),E);
				E=CMLib.hunt().findFirstInventory(CMLib.map().rooms(), mob, srchStr, 10);
				if(E!=null)
					return new Pair<Room,Environmental>(CMLib.map().getStartRoom(E),(E instanceof Item)?((Item)E).owner():E);
				E=CMLib.hunt().findFirstRoomItem(CMLib.map().rooms(), mob, srchStr, true, 10);
				if(E!=null)
					return new Pair<Room,Environmental>(CMLib.map().roomLocation(E),((Item)E).owner());
				return new Pair<Room,Environmental>(CMLib.hunt().findWorldRoomLiberally(mob,srchStr, "I",10,600000), null);
			}
			catch(final NoSuchElementException nse)
			{
			}
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Pair<Room,Environmental> o=origin(mob,target);
		final boolean success=proficiencyCheck(mob,0,auto);
		if((success)
		&&(o != null)
		&&(o.first != null)
		&&(CMLib.flags().canAccess(mob, o.first)))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s), divining the origin of <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(super.getXLEVELLevel(mob))
				{
				case 0:
					commonTelL(mob,"@x1 seems to come from '@x2''.",target.name(mob),o.first.getArea().name(mob));
					break;
				case 1:
					commonTelL(mob,"@x1 seems to come from '@x2' in a place called '@x3'.",target.name(mob),o.first.displayText(mob),o.first.getArea().name(mob));
					break;
				case 2:
					if(o.second instanceof Item)
					{
						commonTelL(mob,"@x1 seems to come from '@x2' in a container @x3, at a place called '@x4'.",
								target.name(mob),
								o.first.displayText(mob),
								((Item)o.second).name(mob),
								o.first.getArea().name(mob));
					}
					else
						commonTelL(mob,"@x1 seems to come from '@x2' in a place called '@x3'.",target.name(mob),o.first.displayText(mob),o.first.getArea().name(mob));
					break;
				case 3:
					if(o.second instanceof Item)
					{
						commonTelL(mob,"@x1 seems to come from '@x2' in a container @x3, at a place called '@x4'.",
								target.name(mob),
								o.first.displayText(mob),
								((Item)o.second).name(mob),
								o.first.getArea().name(mob));
					}
					else
					if((o.second instanceof ShopKeeper)
					&&(o.second instanceof MOB))
					{
						commonTelL(mob,"@x1 seems to come from '@x2' in the shop of @x3, at a place called '@x4'.",
								target.name(mob),
								o.first.displayText(mob),
								((MOB)o.second).name(mob),
								o.first.getArea().name(mob));
					}
					else
						commonTelL(mob,"@x1 seems to come from '@x2' in a place called '@x3'.",target.name(mob),o.first.displayText(mob),o.first.getArea().name(mob));
					break;
				default:
					if(o.second instanceof Item)
					{
						commonTelL(mob,"@x1 seems to come from '@x2' in a container @x3, at a place called '@x4'.",
								target.name(mob),
								o.first.displayText(mob),
								((Item)o.second).name(mob),
								o.first.getArea().name(mob));
					}
					else
					if(o.second instanceof MOB)
					{
						commonTelL(mob,"@x1 seems to come from '@x2' belonging to @x3, at a place called '@x4'.",
								target.name(mob),
								o.first.displayText(mob),
								((MOB)o.second).name(mob),
								o.first.getArea().name(mob));
					}
					else
						commonTelL(mob,"@x1 seems to come from '@x2' in a place called '@x3'.",target.name(mob),o.first.displayText(mob),o.first.getArea().name(mob));
					break;
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to divine something, but fail(s)."));

		return success;
	}
}
