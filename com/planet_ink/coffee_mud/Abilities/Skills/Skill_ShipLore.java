package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_ShipLore extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ShipLore";
	}

	private final static String	localizedName	= CMLib.lang().L("Ship Lore");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHIPLORE", "SLORE" });

	protected long lastFail = 0;

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_WATERLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		boolean report=false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("REPORT")))
		{
			commands.remove(commands.size()-1);
			report=true;
		}

		if((System.currentTimeMillis() - lastFail) < 10000)
		{
			mob.tell(L("You still can't recall.  Give yourself some more time to think first."));
			return false;
		}

		final String shipName=CMParms.combine(commands);
		Room shipChkR=R;
		if(shipChkR.getArea() instanceof BoardableShip)
			shipChkR=CMLib.map().roomLocation(((BoardableShip)shipChkR.getArea()).getShipItem());
		if(shipChkR==null)
			return false;
		if((shipChkR.domainType()&Room.INDOORS)==Room.INDOORS)
		{
			mob.tell(L("You can't see much from here."));
			return false;
		}
		Item targetI=this.getTarget(mob, shipChkR, givenTarget, null, commands, Item.FILTER_UNWORNONLY);
		int penalty=1;
		if(targetI==null)
		{
			final List<BoardableShip> ships=new XVector<BoardableShip>(CMLib.map().ships());
			targetI=(Item)CMLib.english().fetchAvailable(ships, shipName, null, Item.FILTER_UNWORNONLY, true);
			if(targetI==null)
				targetI=(Item)CMLib.english().fetchAvailable(ships, shipName, null, Item.FILTER_UNWORNONLY, false);
			if(targetI==null)
				return false;
			else
			if((targetI instanceof PrivateProperty)&&(CMLib.law().doesHavePrivilegesWith(mob, (PrivateProperty)targetI)))
				penalty=1;
			else
				penalty++;
		}
		if(!(targetI instanceof SailingShip))
		{
			mob.tell(L("@x1 doesn't look much like a ship."));
			return false;
		}
		final SailingShip shipI=(SailingShip)targetI;
		final Area shipA=shipI.getShipArea();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			lastFail = System.currentTimeMillis();
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to recall something about @x1, but can't.",shipI.Name()));
			return false;
		}
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> <S-IS-ARE> recalling something about the @x1 race.",shipI.Name()));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			final int expertise = super.getXLEVELLevel(mob) / penalty;
			final List<String> tidbits = new ArrayList<String>();
			final PrivateProperty prop=CMLib.law().getPropertyRecord(targetI);
			final MOB ownerM=CMLib.law().getPropertyOwner(prop);
			if((expertise > 0)||(penalty==1))
			{
				if(prop.getOwnerName().length()==0)
					tidbits.add(L("it is an unowned ship"));
				else
				if((ownerM==null)||(ownerM.Name().equalsIgnoreCase(prop.getOwnerName())))
					tidbits.add(L("it is owned and captained by @x1",prop.getOwnerName()));
				else
					tidbits.add(L("it is owned by @x1, and captained by @x1",prop.getOwnerName(),ownerM.Name()));
				tidbits.add(L("the ship has a speed of @x1",""+shipI.getShipSpeed()));
			}
			if(expertise >= 1)
			{
				final Map<String,int[]> weaponNames=new HashMap<String,int[]>();
				final Map<String,int[]> loadedNames=new HashMap<String,int[]>();
				for(final Enumeration<Room> r=shipA.getProperMap();r.hasMoreElements();)
				{
					final Room R1=r.nextElement();
					if(R1 != null)
					{
						for(final Enumeration<Item> i=R1.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof AmmunitionWeapon)
							&&(I instanceof Rideable))
							{
								if(!weaponNames.containsKey(I.Name()))
									weaponNames.put(I.Name(), new int[]{1});
								else
									weaponNames.get(I.Name())[0]++;
								if(((AmmunitionWeapon)I).ammunitionRemaining()>0)
								{
									if(!loadedNames.containsKey(I.Name()))
										loadedNames.put(I.Name(), new int[]{1});
									else
										loadedNames.get(I.Name())[0]++;
								}
							}
						}
					}
				}
				if(weaponNames.size()==0)
					tidbits.add(L("it is unarmed"));
				else
				{
					final List<String> allNames=new ArrayList<String>(weaponNames.size());
					for(final String key : weaponNames.keySet())
					{
						final int[] num=weaponNames.get(key);
						if((num[0]==1)||(expertise<=2))
							allNames.add(key);
						else
							allNames.add(key+" x"+num[0]);
					}
					tidbits.add(L("it is armed with @x1",CMLib.english().toEnglishStringList(allNames)));
					if(expertise >=8)
					{
						allNames.clear();
						for(final String key : loadedNames.keySet())
						{
							final int[] num=loadedNames.get(key);
							if((num[0]==1)||(expertise<=2))
								allNames.add(key);
							else
								allNames.add(key+" x"+num[0]);
						}
					}
					tidbits.add(L("it has loaded @x1",CMLib.english().toEnglishStringList(allNames)));
				}
			}
			if(expertise >= 2)
			{
				int deckRooms=0;
				for(final Enumeration<Room> r=shipA.getProperMap();r.hasMoreElements();)
				{
					final Room R1=r.nextElement();
					if((R1 != null)
					&&((R1.domainType()&Room.INDOORS)==0)
					&&(R1.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
						deckRooms++;
				}
				tidbits.add(L("it has @x1 deck rooms",""+deckRooms));
			}
			if(expertise >= 4)
			{
				int allRooms=0;
				for(final Enumeration<Room> r=shipA.getProperMap();r.hasMoreElements();)
				{
					final Room R1=r.nextElement();
					if((R1 != null)
					&&(R1.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
						allRooms++;
				}
				tidbits.add(L("it has @x1 rooms total",""+allRooms));
			}
			/*
			if(expertise >= 5)
			{
				if(A instanceof BoardableShip)
				{
					final int itemLimit=0;
					final int weightLimit=0;
					//TODO: DOH!
					if((itemLimit == 0)&&(weightLimit == 0))
						tidbits.add(L("this room can hold infinite items"));
					else
					if(itemLimit == 0)
						tidbits.add(L("this room can hold @x1 pounds of items",""+weightLimit));
					else
					if(weightLimit == 0)
						tidbits.add(L("this room can hold @x1 items",""+itemLimit));
					else
						tidbits.add(L("this room can hold @x1 items of @x2 total weight",""+itemLimit,""+weightLimit));
				}
			}
			*/
			if(expertise >= 5)
			{
				if(A instanceof BoardableShip)
				{
					int itemLimit=0;
					int weightLimit=0;
					Ability A1=R.fetchEffect("Prop_ReqCapacity");
					if(A1==null)
						A1=A.fetchEffect("Prop_ReqCapacity");
					if(A1!=null)
					{
						itemLimit=CMParms.getParmInt(A1.text(),"items",itemLimit);
						weightLimit=CMParms.getParmInt(A1.text(),"weight",weightLimit);
					}
					if((itemLimit == 0)&&(weightLimit == 0))
						tidbits.add(L("this room can hold infinite items"));
					else
					if(itemLimit == 0)
						tidbits.add(L("this room can hold @x1 pounds of items",""+weightLimit));
					else
					if(weightLimit == 0)
						tidbits.add(L("this room can hold @x1 items",""+itemLimit));
					else
						tidbits.add(L("this room can hold @x1 items of @x2 total weight",""+itemLimit,""+weightLimit));
				}
			}
			if(expertise >= 6)
			{
				tidbits.add(L("the total value of the ship is @x1",CMLib.beanCounter().abbreviatedPrice(mob, shipI.value())));
			}
			if(expertise >= 7)
			{
				final List<String> affects=new ArrayList<String>();
				for(final Enumeration<Ability> a=shipI.effects();a.hasMoreElements();)
					affects.add(a.nextElement().Name());
				if(affects.size()==0)
					tidbits.add(L("it is under no special effects"));
				else
					tidbits.add(L("it has affected by @x1",CMLib.english().toEnglishStringList(affects)));
			}
			if(tidbits.size()==0)
			{
				if(report)
					CMLib.commands().postSay(mob, L("I know almost nothing about that ship.  I guess it's not my area of Expertise. "));
				else
					mob.tell(L("You know almost nothing about that ship.  I guess it's not your area of Expertise. "));
			}
			else
			{
				for(int i=0;i<expertise+1 && tidbits.size()>0;i++)
				{
					final String str=tidbits.remove(CMLib.dice().roll(1, tidbits.size(), -1));
					if(report)
						CMLib.commands().postSay(mob, L("I recall that @x1.",Character.toLowerCase(str.charAt(0))+str.substring(1)));
					else
						mob.tell(L("You recall that @x1.",Character.toLowerCase(str.charAt(0))+str.substring(1)));
				}
			}
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> get(s) frustrated over having forgotten something."));
		return success;
	}

}
