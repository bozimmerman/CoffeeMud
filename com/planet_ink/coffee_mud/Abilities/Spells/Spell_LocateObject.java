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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Spell_LocateObject extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_LocateObject";
	}

	private final static String	localizedName	= CMLib.lang().L("Locate Object");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Locate what?"));
			return false;
		}

		int minLevel=Integer.MIN_VALUE;
		int maxLevel=Integer.MAX_VALUE;
		String s=commands.get(commands.size()-1);
		boolean levelAdjust=false;
		while((commands.size()>1)
		&&((CMath.s_int(s)>0)
			||(s.startsWith(">"))
			||(s.startsWith("<"))))
		{
			levelAdjust=true;
			boolean lt=true;
			if(s.startsWith(">"))
			{
				lt=false;
				s=s.substring(1);
			}
			else
			if(s.startsWith("<"))
				s=s.substring(1);
			final int levelFind=CMath.s_int(s);

			if(lt)
				maxLevel=levelFind;
			else
				minLevel=levelFind;

			commands.remove(commands.size()-1);
			if(commands.size()>1)
				s=commands.get(commands.size()-1);
			else
				s="";
		}
		final String what=CMParms.combine(commands,0);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int maxFound=1+(super.getXLEVELLevel(mob));

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> invoke(s) a divination, shouting '@x1'^?.",what));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<String> itemsFound=new Vector<String>();
				final HashSet<Area> areas=new HashSet<Area>();
				final HashSet<Area> areasTried=new HashSet<Area>();
				Area A=null;
				int numAreas=(int)Math.round(CMath.mul(CMLib.map().numAreas(),0.90))+1;
				if(numAreas>CMLib.map().numAreas())
					numAreas=CMLib.map().numAreas();
				int tries=numAreas*numAreas;
				while((areas.size()<numAreas)&&(((--tries)>0)))
				{
					A=CMLib.map().getRandomArea();
					if((A!=null)&&(!areasTried.contains(A)))
					{
						areasTried.add(A);
						if((CMLib.flags().canAccess(mob,A))
						&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
							areas.add(A);
						else
							numAreas--;
					}
				}
				MOB inhab=null;
				Environmental item=null;
				Room room=null;
				ShopKeeper SK=null;
				final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
				int range=50 + (2*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,range);
				for (final Room room2 : checkSet)
				{
					room=CMLib.map().getRoom(room2);
					if(!CMLib.flags().canAccess(mob,room))
						continue;

					item=room.findItem(null,what);
					if((item!=null)
					&&(CMLib.flags().canBeLocated((Item)item)))
					{
						final String str=L("@x1 is in a place called '@x2'.",item.name(),room.displayText(mob));
						itemsFound.add(str);
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						inhab=room.fetchInhabitant(i);
						if(inhab==null)
							break;
						if(((!CMLib.flags().isCloaked(inhab))
						||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))
							&&(mob.phyStats().level()>=inhab.phyStats().level()))))
						{
							item=inhab.findItem(what);
							SK=CMLib.coffeeShops().getShopKeeper(inhab);
							if((item==null)&&(SK!=null))
								item=SK.getShop().getStock(what,mob);
							if((item instanceof Item)
							&&(CMLib.flags().canBeLocated((Item)item))
							&&(((Item)item).phyStats().level()>minLevel)
							&&(((Item)item).phyStats().level()<maxLevel))
							{
								final CMMsg msg2=CMClass.getMsg(mob,inhab,this,verbalCastCode(mob,null,auto),null);
								if(room.okMessage(mob,msg2))
								{
									room.send(mob,msg2);
									final String str=L("@x1@x2 is being carried by @x3 in a place called '@x4'.",item.name(),((!levelAdjust)?"":("("+((Item)item).phyStats().level()+")")),inhab.name(),room.displayText(mob));
									itemsFound.add(str);
									break;
								}
							}
						}
					}
					if(itemsFound.size()>=maxFound)
						break;
				}
				if(itemsFound.size()==0)
					mob.tell(L("Your magic fails to focus on anything called '@x1'.",what));
				else
				{
					while(itemsFound.size()>maxFound)
						itemsFound.remove(CMLib.dice().roll(1,itemsFound.size(),-1));
					for(final String found : itemsFound)
						mob.tell(found);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> invoke(s) a divination, shouting '@x1', but there is no answer.",what));

		// return whether it worked
		return success;
	}
}
