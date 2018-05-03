package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
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

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Patroller extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "Patroller";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS | Behavior.CAN_ITEMS;
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_MOBILITY;
	}

	protected int			step		= 0;
	protected int			diameter	= 20;
	protected boolean		rideOk		= false;
	protected boolean		rideOnly	= false;
	protected List<Room>	correction	= null;
	protected List<String>	cachedSteps	= null;
	protected int			tickStatus	= Tickable.STATUS_NOT;
	protected volatile int	rideCheckCt	= 0;

	protected WeakReference<Room> startRoom=new WeakReference<Room>(null);
	
	@Override
	public String accountForYourself()
	{
		return "regular patrolling";
	}

	public Patroller()
	{
		super();
		minTicks=5; maxTicks=10; chance=100;
		rideOk=false;
		diameter=20;
		tickReset();
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		final String rideokString=CMParms.getParmStr(newParms,"rideok","false");
		rideOnly=rideokString.equalsIgnoreCase("only");
		rideOk=rideOnly||rideokString.equalsIgnoreCase("true");
		diameter=CMParms.getParmInt(newParms,"diameter",20);
		cachedSteps = null;
	}

	protected List<String> getSteps()
	{
		if(cachedSteps != null)
			return cachedSteps;

		final Vector<String> V=new Vector<String>();
		String path=getParms().trim();
		int x=path.indexOf(';');
		if(x<0)
			return V;
		path=path.substring(x+1).trim();
		x=path.indexOf(';');
		String s=null;
		while(x>=0)
		{
			s=path.substring(0,x).trim();
			if(s.length()>0)
				V.addElement(s);
			path=path.substring(x+1).trim();
			x=path.indexOf(';');
		}
		if(path.length()>0)
			V.addElement(path);
		if(V.size()>1)
		{
			for(int i=V.size()-1;i>=0;i--)
			{
				s=V.elementAt(i);
				if(s.equalsIgnoreCase("RESTART")||s.equalsIgnoreCase("REPEAT"))
					break;
				final int dir=CMLib.directions().getGoodDirectionCode(s);
				if(dir>=0)
					V.addElement(CMLib.directions().getDirectionName(Directions.getOpDirectionCode(dir)));
				else
				if(i<(V.size()-1))
					V.addElement(V.elementAt(i));
			}
		}
		V.trimToSize();
		cachedSteps = V;
		return cachedSteps;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((rideOnly)
		&&(rideCheckCt<=0)
		&&(rideOk)
		&&(host instanceof Rideable)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_LEAVE))
		&&(msg.source()!=host)
		&&(msg.source().riding()==host)
		&&(!(host instanceof BoardableShip)))
 		{
			if(host instanceof MOB)
				msg.source().tell(L("You must dismount before you can do that."));
			else
				msg.source().tell(L("You must disembark before you can do that."));
			return false;
		}
		return super.okMessage(host,msg);
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((startRoom.get()==null)&&(ticking instanceof Physical))
			startRoom=new WeakReference<Room>(CMLib.map().roomLocation((Physical)ticking));
		if(canAct(ticking,tickID))
		{
			tickStatus=Tickable.STATUS_MISC+0;
			if(!rideOk)
			{
				if(((ticking instanceof Rideable)
					&&(((Rideable)ticking).numRiders()>0))
				||((ticking instanceof MOB)
					&&(((MOB)ticking).amFollowing()!=null)))
				{
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}
			if((ticking instanceof Physical)
			&&(!CMLib.flags().canTrack((Physical)ticking)) 
			&& (CMLib.dice().roll(1,100,0)>1))
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}
			
			if((ticking instanceof BoardableShip)
			&&(((BoardableShip)ticking).getShipItem() instanceof SailingShip))
			{
				final SailingShip ship=(SailingShip)((BoardableShip)ticking).getShipItem();
				if((ship.isInCombat())
				||((ship.subjectToWearAndTear())&&(ship.usesRemaining()<=0))
				||(CMLib.flags().isFalling(ship)))
				{
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}
			
			tickStatus=Tickable.STATUS_MISC+1;
			ArrayList<Rider> riders=null;
			if((ticking instanceof Rideable)
			&&(!(ticking instanceof BoardableShip)))
			{
				riders=new ArrayList<Rider>(((Rideable)ticking).numRiders());
				for(int i=0;i<((Rideable)ticking).numRiders();i++)
					riders.add(((Rideable)ticking).fetchRider(i));
			}

			tickStatus=Tickable.STATUS_START;
			Room thisRoom=null;
			if(ticking instanceof MOB)
				thisRoom=((MOB)ticking).location();
			else
			if((ticking instanceof Item)
			&&(((Item)ticking).owner() instanceof Room)
			&&(!((Item)ticking).amDestroyed()))
				thisRoom=(Room)((Item)ticking).owner();
			if(thisRoom instanceof GridLocale)
			{
				final Room R=((GridLocale)thisRoom).getRandomGridChild();
				if(R!=null)
				{
					if(ticking instanceof Item)
						R.moveItemTo((Item)ticking);
					else
					if((ticking instanceof MOB)
					&&(CMLib.flags().isInTheGame((MOB)ticking,true)))
					{
						R.bringMobHere((MOB)ticking,true);
						R.show((MOB)ticking,R,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,null);
					}
				}
				thisRoom=R;
			}
			if(thisRoom==null)
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}

			Room thatRoom=null;
			final List<String> steps=getSteps();
			if(steps.size()==0)
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}
			tickStatus=Tickable.STATUS_MISC+2;
			if((step<0)||(step>=steps.size()))
				step=0;
			String nxt=steps.get(step);

			if((nxt.equalsIgnoreCase("RESTART")||nxt.equalsIgnoreCase("REPEAT"))&&(step>0))
			{
				step=0;
				nxt=steps.get(step);
			}

			if(nxt.equalsIgnoreCase("."))
			{
				step++;
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}

			tickStatus=Tickable.STATUS_MISC+3;
			int direction=CMLib.directions().getGoodDirectionCode(nxt);
			if(direction<0)
			{
				if(CMLib.map().getExtendedRoomID(thisRoom).toUpperCase().endsWith(nxt.toUpperCase()))
				{
					correction=null;
					step++;
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}

				tickStatus=Tickable.STATUS_MISC+4;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=thisRoom.getRoomInDir(d);
					if((R!=null)
					&&(CMLib.map().getExtendedRoomID(R).toUpperCase().endsWith(nxt.toUpperCase())))
					{
						correction=null;
						thatRoom=R;
						direction=d;
						break;
					}
				}
			}
			else
			{
				thatRoom=thisRoom.getRoomInDir(direction);
				if(thatRoom==null)
				{
					if(step>0)
						Log.errOut("Patroller","'"+nxt+"' for "+ticking.name()+" at "+CMLib.map().getDescriptiveExtendedRoomID(thisRoom)+" is impossible!");
					step=-1;
					tickStatus=Tickable.STATUS_NOT;
					final Room myStartRoom=startRoom.get();
					if((startRoom!=null)&&(startRoom!=thisRoom))
					{
						final Environmental E=(Environmental)ticking;
						if(ticking instanceof MOB)
							myStartRoom.bringMobHere((MOB)E,true);
						else
						if(E instanceof Item)
							myStartRoom.moveItemTo((Item)E,Expire.Never,Move.Followers);
					}
					return true;
				}
			}
			Room destinationRoomForThisStep=thatRoom;

			tickStatus=Tickable.STATUS_MISC+5;
			if((direction<0)||(destinationRoomForThisStep==null))
			{
				Room R=CMLib.map().getRoom(nxt);
				if(R==null)
					R=CMLib.map().getRoom(thisRoom.getArea()+nxt);
				if(R==null)
					R=CMLib.map().getRoom(thisRoom.getArea()+"#"+nxt);
				if(R!=null)
				{
					final boolean airOk=(((ticking instanceof Physical)&&CMLib.flags().isFlying((Physical)ticking))
						||((ticking instanceof Rider)&&(((Rider)ticking).riding()!=null)&&(((Rider)ticking).riding().rideBasis()==Rideable.RIDEABLE_AIR))
						||((ticking instanceof Rideable)&&(((Rideable)ticking).rideBasis()==Rideable.RIDEABLE_AIR)));
					final boolean waterOk=((ticking instanceof Physical)&&CMLib.flags().isWaterWorthy((Physical)ticking));

					tickStatus=Tickable.STATUS_MISC+6;
					if(R instanceof GridLocale)
					{
						boolean GridLocaleFixed=false;
						if(correction!=null)
						{
							for(int r=0;r<correction.size();r++)
							{
								if(((GridLocale)R).isMyGridChild(correction.get(r)))
								{
									GridLocaleFixed=true;
									R=correction.get(r);
									break;
								}
							}
						}
						if(!GridLocaleFixed)
						{
							correction=null;
							R=((GridLocale)R).getRandomGridChild();
						}
					}
					tickStatus=Tickable.STATUS_MISC+7;
					destinationRoomForThisStep=R;
					direction=-1;
					if(correction!=null)
					{
						direction=CMLib.tracking().trackNextDirectionFromHere(correction,thisRoom,ticking instanceof Item);
						if(direction<0)
							correction=null;
						else
							thatRoom=thisRoom.getRoomInDir(direction);
					}
					tickStatus=Tickable.STATUS_MISC+8;
					if((direction<0)||(thatRoom==null))
					{
						final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
						if(ticking instanceof Item)
							flags.plus(TrackingLibrary.TrackingFlag.OPENONLY);
						flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
						if(!airOk)
							flags.plus(TrackingLibrary.TrackingFlag.NOAIR);
						if(!waterOk)
							flags.plus(TrackingLibrary.TrackingFlag.NOWATER);
						correction=CMLib.tracking().findTrailToAnyRoom(thisRoom,new XVector<Room>(R),flags,diameter);
						tickStatus=Tickable.STATUS_MISC+9;
						if(correction!=null)
							direction=CMLib.tracking().trackNextDirectionFromHere(correction,thisRoom,ticking instanceof Item);
						else
							direction=-1;
						tickStatus=Tickable.STATUS_MISC+10;
						if(direction>=0)
							thatRoom=thisRoom.getRoomInDir(direction);
						else
							correction=null;
					}
					tickStatus=Tickable.STATUS_MISC+11;
					if((direction<0)||(thatRoom==null))
					{
						step=0;
						tickStatus=Tickable.STATUS_NOT;
						return true;
					}
					tickStatus=Tickable.STATUS_MISC+12;
				}
				else
				{
					Log.errOut("Patroller","'"+nxt+"' for "+ticking.name()+" ("+CMLib.map().getDescriptiveExtendedRoomID(CMLib.map().getStartRoom((Environmental)ticking))+") is utterly unknown!");
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}
			else
				correction=null;
			tickStatus=Tickable.STATUS_MISC+13;
			final Exit E=thisRoom.getExitInDir(direction);
			if(E==null)
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}

			tickStatus=Tickable.STATUS_MISC+14;
			final Set<MOB> mobsHere=CMLib.players().getPlayersHere(thisRoom);
			if(mobsHere.size()>0)
			{
				for(final MOB inhab : mobsHere)
				{
					if((!inhab.isMonster())
					&&(CMSecurity.isAllowed(inhab,thisRoom,CMSecurity.SecFlag.CMDMOBS)
					   ||CMSecurity.isAllowed(inhab,thisRoom,CMSecurity.SecFlag.CMDROOMS)))
					{
						tickStatus=Tickable.STATUS_NOT;
						return true;
					}
				}
			}

			tickStatus=Tickable.STATUS_MISC+15;
			if(ticking instanceof Item)
			{
				final Item I=(Item)ticking;
				if((ticking instanceof BoardableShip)
				&&(((BoardableShip)ticking).getShipItem() instanceof SailingShip)
				&&(thatRoom!=null))
				{
					final SailingShip ship = (SailingShip)((BoardableShip)ticking).getShipItem();
					if(ship.isAnchorDown())
						ship.setAnchorDown(false);
					ship.setCurrentCourse(new XVector<Integer>(Integer.valueOf(direction)));
					ship.tick(ship, Tickable.TICKID_AREA); // this should force a move
				}
				else
				{
					if((ticking instanceof Rideable)
					&&(thatRoom!=null)
					&&(riders!=null))
					{
						final Exit opExit=thatRoom.getReverseExit(direction);
						for(int i=0;i<riders.size();i++)
						{
							final Rider R=riders.get(i);
							if(R instanceof MOB)
							{
								tickStatus=Tickable.STATUS_MISC+16;
								final MOB mob=(MOB)R;
								mob.setRiding((Rideable)ticking);
								// overboard check
								if(mob.isMonster()
								&& mob.isSavable()
								&& (mob.location() != thisRoom)
								&& CMLib.flags().isInTheGame(mob,true))
									thisRoom.bringMobHere(mob,false);
	
								final CMMsg enterMsg=CMClass.getMsg(mob,thatRoom,E,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
								final CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null);
								try
								{
									rideCheckCt++;
									if(!E.okMessage(mob,enterMsg))
									{
										tickStatus=Tickable.STATUS_NOT;
										return true;
									}
									else
									if((opExit!=null)&&(!opExit.okMessage(mob,leaveMsg)))
									{
										tickStatus=Tickable.STATUS_NOT;
										return true;
									}
									else
									if(!enterMsg.target().okMessage(mob,enterMsg))
									{
										tickStatus=Tickable.STATUS_NOT;
										return true;
									}
									else
									if(!mob.okMessage(mob,enterMsg))
									{
										tickStatus=Tickable.STATUS_NOT;
										return true;
									}
								}
								finally
								{
									rideCheckCt--;
								}
							}
						}
					}
	
					tickStatus=Tickable.STATUS_MISC+17;
					thisRoom.showHappens(CMMsg.MSG_OK_ACTION,I,L("<S-NAME> goes @x1.",CMLib.directions().getDirectionName(direction)));
					tickStatus=Tickable.STATUS_MISC+18;
					if(thatRoom!=null)
						thatRoom.moveItemTo(I);
					tickStatus=Tickable.STATUS_MISC+19;
					if((I.owner()==thatRoom)&&(thatRoom!=null))
					{
						tickStatus=Tickable.STATUS_MISC+20;
						thatRoom.showHappens(CMMsg.MSG_OK_ACTION,I,L("<S-NAME> arrives from @x1.",CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(direction))));
						tickStatus=Tickable.STATUS_MISC+21;
						if(riders!=null)
						{
							for(int i=0;i<riders.size();i++)
							{
								final Rider R=riders.get(i);
								if(CMLib.map().roomLocation(R)!=thatRoom)
								{
									if((((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_SIT)
									&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_TABLE)
									&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_ENTERIN)
									&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_SLEEP)
									&&(((Rideable)ticking).rideBasis()!=Rideable.RIDEABLE_LADDER))
									{
										if((R instanceof MOB)
										&&(CMLib.flags().isInTheGame((MOB)R,true)))
										{
											tickStatus=Tickable.STATUS_MISC+30;
											thatRoom.bringMobHere((MOB)R,true);
											((MOB)R).setRiding((Rideable)ticking);
											tickStatus=Tickable.STATUS_MISC+31;
											CMLib.commands().postLook((MOB)R,true);
											thatRoom.show((MOB)R,thatRoom,E,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,null);
											tickStatus=Tickable.STATUS_MISC+32;
										}
										else
										if(R instanceof Item)
										{
											tickStatus=Tickable.STATUS_MISC+33;
											thatRoom.moveItemTo((Item)R);
											tickStatus=Tickable.STATUS_MISC+34;
										}
									}
									else
										R.setRiding(null);
								}
								tickStatus=Tickable.STATUS_MISC+35;
							}
						}
						tickStatus=Tickable.STATUS_MISC+36;
					}
				}
				tickStatus=Tickable.STATUS_MISC+37;
				if(I.owner()==destinationRoomForThisStep)
					step++;
				else
					tickDown=0;
			}
			else
			if(ticking instanceof MOB)
			{
				tickStatus=Tickable.STATUS_MISC+22;
				// ridden things dont wander!
				final MOB mob=(MOB)ticking;

				// handle doors!
				if(E.hasADoor()&&(!E.isOpen()))
				{
					if((E.hasALock())&&(E.isLocked()))
					{
						CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
						if(mob.location().okMessage(mob,msg))
						{
							msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> unlock(s) <T-NAMESELF>."));
							CMLib.utensils().roomAffectFully(msg,thisRoom,direction);
						}
					}
					CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(mob.location().okMessage(mob,msg))
					{
						msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,L("<S-NAME> @x1(s) <T-NAMESELF>.",E.openWord()));
						CMLib.utensils().roomAffectFully(msg,thisRoom,direction);
					}
				}
				if(!E.isOpen())
				{
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}

				tickStatus=Tickable.STATUS_MISC+23;
				final Ability A=mob.fetchAbility("Thief_Sneak");
				if(A!=null)
				{
					final Vector<String> V=new Vector<String>();
					V.add(CMLib.directions().getDirectionName(direction));
					if(A.proficiency()<50)
					{
						A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
						final Ability A2=mob.fetchAbility("Thief_Hide");
						if(A2!=null)
							A2.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
					}
					final CharState oldState=(CharState)mob.curState().copyOf();
					A.invoke(mob,V,null,false,0);
					mob.curState().setMana(oldState.getMana());
					mob.curState().setMovement(oldState.getMovement());
				}
				else
				{
					try
					{
						rideCheckCt++;
						CMLib.tracking().walk(mob,direction,false,false);
					}
					finally
					{
						rideCheckCt--;
					}
				}

				tickStatus=Tickable.STATUS_MISC+24;
				if(mob.location()==destinationRoomForThisStep)
					step++;
				else
					tickDown=0;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
