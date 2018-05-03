package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_PubContacts extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PubContacts";
	}

	private final static String	localizedName	= CMLib.lang().L("Make Pub Contacts");

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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MAKEPUBCONTACTS","PUBCONTACTS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}
	
	protected final static int baseWaterRange = 8;

	protected Room pubRoom = null;
	protected boolean success = false;
	
	public Triad<Item,Double,String> cheapestAlcoholHere(MOB mob, Room room)
	{
		double lowestPrice=Integer.MAX_VALUE;
		Item lowestItem=null;
		String currency="";
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB M=room.fetchInhabitant(m);
			if((M!=null)&&(M!=mob))
			{
				if(CMLib.flags().canBeSeenBy(M,mob))
				{
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
						{
							final Environmental E=i.next();
							if((E instanceof Item)&&(CMLib.flags().isAlcoholic((Item)E)))
							{
								double moneyPrice=0;
								ShopKeeper.ShopPrice price=CMLib.coffeeShops().sellingPrice(M,mob,E,SK,SK.getShop(), true);
								if(price.experiencePrice>0)
									moneyPrice=(100 * price.experiencePrice);
								else
								if(price.questPointPrice>0)
									moneyPrice=(100 * price.questPointPrice);
								else
								{
									moneyPrice=price.absoluteGoldPrice;
								}
								if(moneyPrice < lowestPrice)
								{
									lowestPrice=moneyPrice;
									lowestItem=(Item)E;
									currency=CMLib.beanCounter().getCurrency(M);
								}
							}
						}
					}
				}
			}
		}
		if(lowestItem == null)
			return null;
		return new Triad<Item,Double,String>(lowestItem,Double.valueOf(lowestPrice),currency);
	}
	
	@Override
	public void unInvoke()
	{
		final Physical affected=this.affected;
		super.unInvoke();
		if((this.unInvoked)&&(affected!=null))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if(R!=null)
			{
				if((pubRoom != R)&&(!success))
					mob.tell(L("You stop trying to make pub contacts."));
				else
				if((pubRoom==R)&&(!success))
					mob.tell(L("No one is interested in talking to you."));
				else
				{
					final Ability A=mob.fetchAbility(ID());
					final String roomID=CMLib.map().getExtendedRoomID(R);
					int level=0;
					if((A!=null)&&(roomID.length()>0))
					{
						final List<String> roomIDs=CMParms.parseSemicolons(A.text(),true);
						for(int i=0;i<roomIDs.size();i++)
						{
							if(roomIDs.get(i).equals(roomID))
								level++;
						}
						if(level<3)
						{
							roomIDs.add(roomID);
							A.setMiscText(CMParms.combineWith(roomIDs, ';'));
						}
					}
					final Map<Room,List<Item>> allShips=new HashMap<Room,List<Item>>(CMLib.map().numShips());
					for(final Enumeration<BoardableShip> ship=CMLib.map().ships();ship.hasMoreElements();)
					{
						final BoardableShip S=ship.nextElement();
						if((S!=null)
						&&(S instanceof Item)
						&&((level==3)||(CMLib.flags().canBeSeenBy(S, mob))))
						{
							final Room sR=CMLib.map().roomLocation(S);
							if((sR!=null)
							&&(CMLib.flags().isWaterySurfaceRoom(sR))
							&&(CMLib.flags().canAccess(mob, R)))
							{
								if(!allShips.containsKey(sR))
									allShips.put(sR,new ArrayList<Item>(1));
								allShips.get(sR).add((Item)S);
							}
						}
					}
					final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
					int range = baseWaterRange + super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob);
					final List<Room> nearby=CMLib.tracking().findTrailToAnyRoom(R, TrackingFlag.WATERSURFACEONLY.myFilter, flags, range);
					Room shore=null;
					Room notShore=null;
					for(int n=nearby.size()-1;(n>=0) && (shore==null);n--)
					{
						final Room R2=nearby.get(n);
						if(CMLib.flags().isWateryRoom(R2))
						{
							if(notShore==null)
								notShore=R2;
							if(!CMLib.flags().isUnderWateryRoom(R2.getRoomInDir(Directions.DOWN)))
							{
								shore=R2;
								notShore=R2;
							}
							else
							{
								for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
								{
									Room R3=R2.getRoomInDir(d);
									if(R3!=null)
									{
										switch(R3.domainType())
										{
										case Room.DOMAIN_INDOORS_AIR:
										case Room.DOMAIN_OUTDOORS_AIR:
										case Room.DOMAIN_INDOORS_UNDERWATER:
										case Room.DOMAIN_OUTDOORS_UNDERWATER:
										case Room.DOMAIN_INDOORS_WATERSURFACE:
										case Room.DOMAIN_OUTDOORS_WATERSURFACE:
											break;
										default:
											shore=R3;
											notShore=R2;
											break;
										}
									}
								}
							}
						}
					}
					if(shore==null)
						shore=notShore;
					
					if((shore==null)||(notShore==null))
					{
						mob.tell(L("No one really knows anything."));
						return;
					}
					flags.add(TrackingLibrary.TrackingFlag.WATERSURFACEONLY);
					int radius=50 + (10*(super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob)));
					final List<Room> ocean = CMLib.tracking().getRadiantRooms(notShore, flags, radius);
					final Map<Room,List<Room>> trails=new Hashtable<Room,List<Room>>();
					int farthest=0;
					int totalShips=0;
					for(Room R2 : ocean)
					{
						if(allShips.containsKey(R2))
						{
							List<Room> trail = CMLib.tracking().findTrailToRoom(notShore, R2, flags, radius, ocean);
							if((trail != null)&&(trail.size()>0))
							{
								trails.put(R2,trail);
								totalShips += allShips.get(R2).size();
								if(trail.size()>farthest)
									farthest=trail.size();
							}
						}
					}
					String roomName = notShore.displayText(mob);
					if(shore != notShore)
						roomName+=L(" (just off @x1)",shore.displayText(mob));
					StringBuilder shipList=new StringBuilder(L("^NYour contacts tell you about @x1 ships within @x2 of ^W@x3^N: \n\r",
											""+totalShips,""+farthest,roomName));
					for(Room R2 : trails.keySet())
					{
						List<Room> trail = trails.get(R2);
						int distance=trail.size();
						List<Item> ships=allShips.get(R2);
						for(Item I : ships)
						{
							switch(level)
							{
							case 0:
								shipList.append("^H"+I.name()+"^N").append("\n\r");
								break;
							case 1:
								shipList.append("^H"+I.name()+"^N").append(L(" (distance: @x2)",""+distance)).append("\n\r");
								break;
							default:
								shipList.append("^H"+I.name()+"^N").append(", directions: ^W");
								Room lastRoom=notShore;
								for(int r=trail.size()-2;r>=0;r--)
								{
									int dir=CMLib.map().getRoomDir(lastRoom, trail.get(r));
									lastRoom=trail.get(r);
									shipList.append(CMLib.directions().getDirectionChar(dir)).append(" ");
								}
								shipList.append("^N\n\r");
								break;
							}
						}
					}
					mob.tell(shipList.toString());
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if((pubRoom!=null)&&(pubRoom!=M.location()))
			{
				success=false;
				pubRoom=null;
				unInvoke();
			}
			else
			if((super.tickDown % 3)==0)
			{
				Room R=CMLib.map().roomLocation(M);
				if(R.isInhabitant(M))
				{
					switch(CMLib.dice().roll(1, 5, -1))
					{
					case 0:
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> continue(s) making contacts."));
						break;
					case 1:
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> continue(s) drinking and carefully socializing."));
						break;
					case 2:
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> continue(s) offering drinks and making the right friends."));
						break;
					case 3:
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> continue(s) descreetly making friends."));
						break;
					case 4:
						R.show(M, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> continue(s) drinking and carefully socializing."));
						break;
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		double money=0.0;
		String moneyStr="money";
		if(!auto)
		{
			final Triad<Item,Double,String> alco = cheapestAlcoholHere(mob,R);
			if(alco == null)
			{
				mob.tell(L("You can only establish contacts at a pub."));
				return false;
			}
			TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			int range = baseWaterRange + super.getXLEVELLevel(mob)+super.getXMAXRANGELevel(mob);
			List<Room> nearby=CMLib.tracking().findTrailToAnyRoom(R, TrackingFlag.WATERSURFACEONLY.myFilter, flags, range);
			if((nearby==null)||(nearby.size()==0))
			{
				mob.tell(L("There's no sea or river nearby, so no one here will know anything."));
				return false;
			}
			double pct=0.5 + (CMath.mul(CMath.div(10-super.getXLOWCOSTLevel(mob),2.0), 0.1));
			money = CMath.mul(pct,alco.second.doubleValue()*6.0);
			moneyStr = CMLib.beanCounter().abbreviatedPrice(alco.third, money);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, alco.third) < money)
			{
				mob.tell(L("You need at least @x1 to buy enough drinks to loosen tongues.",moneyStr));
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(money > 0.0)
			CMLib.beanCounter().subtractMoney(mob, money);
		
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> drop(s) @x1 on drinks and start(s) socializing.",moneyStr));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			int ticks=12 - super.getXLEVELLevel(mob);
			if(!success)
				ticks /= 2;
			if(ticks<1)
				ticks=1;
			Thief_PubContacts pub = (Thief_PubContacts)this.beneficialAffect(mob, mob, asLevel, ticks);
			if(pub != null)
			{
				pub.pubRoom=R;
				pub.success=success;
			}
			
		}
		return success;
	}
}
