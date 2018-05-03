package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class Concierge extends StdBehavior
{
	@Override
	public String ID()
	{
		return "Concierge";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ITEMS | Behavior.CAN_MOBS | Behavior.CAN_ROOMS | Behavior.CAN_EXITS | Behavior.CAN_AREAS;
	}

	protected PairVector<Object, Double>		rates		= new PairVector<Object, Double>();
	protected List<Room>						ratesVec	= null;
	protected PairVector<MOB, String>			thingsToSay	= new PairVector<MOB, String>();
	
	protected QuadVector<MOB, Room, Double, TrackingFlags>	destinations= new QuadVector<MOB, Room, Double, TrackingFlags>();
	
	protected static final String defaultGreeting = "Need directions? Just name the place and I'll name the price! Append words like noswim, noclimb, nofly, nolocks and nocrawl to narrow the focus.";
	
	protected final static TrackingLibrary.TrackingFlags defaultTrackingFlags	= CMLib.tracking().newFlags()
																				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
																				.plus(TrackingLibrary.TrackingFlag.NOHOMES)
																				.plus(TrackingLibrary.TrackingFlag.NOHIDDENAREAS);
	protected final static TrackingLibrary.TrackingFlags defaultRoomRadiusFlags	= CMLib.tracking().newFlags();
	
	protected double	basePrice		= 0.0;
	protected double	perRoomPrice	= 0.0;
	protected String	talkerName		= "";
	protected MOB		fakeTalker		= null;
	protected Room		startRoom		= null;
	protected String	greeting		= defaultGreeting;
	protected String	mountStr		= "";
	protected boolean	portal			= false;
	protected int		maxRange		= 100;
	
	protected TrackingLibrary.TrackingFlags trackingFlags	= CMLib.tracking().newFlags().plus(defaultTrackingFlags);
	protected TrackingLibrary.TrackingFlags roomRadiusFlags = CMLib.tracking().newFlags().plus(defaultRoomRadiusFlags);
	
	@Override
	public String accountForYourself()
	{
		return "direction giving and selling";
	}
	
	protected boolean disableComingsAndGoings()
	{
		return false;
	}

	protected String getGiveMoneyMessage(Environmental observer, Environmental destination, String moneyName)
	{
		if(observer instanceof MOB)
			return L("I can help you find @x1, but you'll need to give me @x2 first.",getDestinationName(destination),moneyName);
		else
		if(observer instanceof Container)
			return L("I can help you find @x1, but you'll need to put @x2 into @x3 first.",getDestinationName(destination),moneyName,observer.name());
		else
			return L("I can help you find @x1, but you'll need to drop @x2 first.",getDestinationName(destination),moneyName);
	}
	
	protected MOB getTalker(Environmental o, Room room)
	{
		if(o==null)
			return null;
		if(startRoom==null)
			startRoom=room;
		if((talkerName==null)||(talkerName.length()==0))
		{
			if(o instanceof MOB)
				return (MOB)o;
			if(fakeTalker==null)
			{
				fakeTalker=CMClass.getFactoryMOB();
				fakeTalker.setName(o.name());
			}
		}
		else
		if(o instanceof Rideable)
		{
			Environmental E=CMLib.english().fetchEnvironmental(((Rideable)o).riders(),talkerName,true);
			if(E instanceof MOB)
				return (MOB)E;
		}
		if(fakeTalker==null)
		{
			fakeTalker=CMClass.getFactoryMOB();
			fakeTalker.setName(talkerName);
		}
		fakeTalker.setStartRoom(startRoom);
		if(room != null)
			fakeTalker.setLocation(room);
		else
			fakeTalker.setLocation(CMLib.map().roomLocation(o));
		return fakeTalker;
	}

	protected Environmental getReceiver(Environmental o, Room room)
	{
		return o;
	}

	protected void resetDefaults()
	{
		basePrice=0.0;
		talkerName="";
		fakeTalker=null;
		startRoom=null;
		greeting=defaultGreeting;
		mountStr="";
		maxRange = 100;
		perRoomPrice = 0.0;
		rates.clear();
		ratesVec=null;
		destinations.clear();
		thingsToSay.clear();
		portal=false;
	}
	
	protected void resetFlags()
	{
		trackingFlags	= CMLib.tracking().newFlags().plus(defaultTrackingFlags);
		roomRadiusFlags = CMLib.tracking().newFlags().plus(defaultRoomRadiusFlags);
	}
	
	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
		resetDefaults();
		if((CMath.isInteger(newParm))
		||(CMath.isDouble(newParm)))
		{
			basePrice=CMath.s_double(newParm);
			return;
		}
		final List<String> V=CMParms.parseSemicolons(newParm,true);
		String s=null;
		int x=0;
		double price=0;
		Room R=null;
		Area A=null;
		resetFlags();
		for(int v=0;v<V.size();v++)
		{
			s=V.get(v);
			x=s.indexOf('=');
			if(x>=0)
			{
				String numStr=s.substring(x+1).trim();
				if(numStr.startsWith("\"")&&(numStr.endsWith("\"")))
					numStr=numStr.substring(1,numStr.length()-1).trim();
				s=s.substring(0,x).trim().toUpperCase();
				boolean isTrue=numStr.toLowerCase().startsWith("t");
				if(s.equals("PORTAL"))
				{
					portal=isTrue;
					continue;
				}
				else
				if(s.equals("AREAONLY"))
				{
					if(isTrue)
						roomRadiusFlags.add(TrackingFlag.AREAONLY);
					continue;
				}
				else
				if(s.equals("NOCLIMB"))
				{
					if(isTrue)
						trackingFlags.add(TrackingFlag.NOCLIMB);
					continue;
				}
				else
				if(s.equals("NOWATER"))
				{
					if(isTrue)
						trackingFlags.add(TrackingFlag.NOWATER);
					continue;
				}
				else
				if(s.equals("INDOOROK"))
				{
					if(isTrue)
					{
						trackingFlags.remove(TrackingFlag.OUTDOORONLY);
						roomRadiusFlags.remove(TrackingFlag.OUTDOORONLY);
					}
					continue;
				}
				else
				if(s.equals("NOAIR"))
				{
					if(isTrue)
						trackingFlags.add(TrackingFlag.NOAIR);
					continue;
				}
				else
				if(s.equals("NOLOCKS"))
				{
					if(isTrue)
						trackingFlags.add(TrackingFlag.UNLOCKEDONLY);
					continue;
				}
				else
				if(s.equals("NOHOMES"))
				{
					if(isTrue)
						trackingFlags.add(TrackingFlag.NOHOMES);
					continue;
				}
				else
				if(s.equals("NOINDOOR"))
				{
					if(isTrue)
					{
						trackingFlags.add(TrackingFlag.OUTDOORONLY);
						roomRadiusFlags.add(TrackingFlag.OUTDOORONLY);
					}
					continue;
				}
				else
				if(s.equals("MAXRANGE"))
				{
					maxRange=CMath.s_int(numStr);
					continue;
				}
				else
				if(s.equals("GREETING"))
				{
					greeting=numStr;
					continue;
				}
				else
				if(s.equals("ENTERMSG"))
				{
					mountStr=numStr;
					continue;
				}
				else
				if(s.equals("TALKERNAME"))
				{
					talkerName=numStr;
					continue;
				}
				else
				if(s.equals("PERROOM"))
				{
					perRoomPrice=CMath.s_double(numStr);
					continue;
				}
				else
					price=CMath.s_double(numStr);
			}
			A=null;
			R=CMLib.map().getRoom(s);
			if(R==null) 
				A=CMLib.map().findArea(s);
			if(A!=null)
				rates.add(A,Double.valueOf(price));
			else
			if((R!=null)&&(!rates.containsFirst(R)))
				rates.add(R,Double.valueOf(price));
			else
				rates.add(s,Double.valueOf(price));
		}
		basePrice=price;
	}

	protected double getPrice(final Room centerRoom, final Room  destR)
	{
		if(destR==null) 
			return basePrice;
		final int rateIndex=rates.indexOfFirst(destR);
		if(rateIndex<0)
		{
			double price=basePrice;
			if(this.perRoomPrice > 0.0)
			{
				List<Room> trail=CMLib.tracking().findTrailToRoom(centerRoom, destR, trackingFlags, maxRange);
				if(trail != null)
					price = price + (perRoomPrice * trail.size());
			}
			return price;
		}
		return rates.get(rateIndex).second.doubleValue();
	}

	protected final List<Room> getRoomsInRange(final Room centerRoom, final List<Room> roomsInRange, TrackingFlags roomRadiusFlags)
	{
		if(roomsInRange != null)
			return roomsInRange;
		return CMLib.tracking().getRadiantRooms(centerRoom,roomRadiusFlags,maxRange);
	}
	
	protected Room findNearestAreaRoom(Area A, List<Room> roomsInRange)
	{
		for(Room R : roomsInRange)
		{
			if(A.inMyMetroArea(R.getArea()))
				return R;
		}
		return A.getRandomMetroRoom();
	}
	
	protected Room findDestination(final Environmental observer, final MOB mob, final Room centerRoom, final String where, TrackingFlags roomRadiusFlags)
	{
		PairVector<String,Double> stringsToDo=null;
		Room roomR=null;
		List<Room> roomsInRange=null;
		if(rates.size()==0)
		{
			Area A=CMLib.map().findArea(where);
			if(A!=null)
			{
				roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
				roomR=findNearestAreaRoom(A,roomsInRange);
			}
		}
		else
		if(rates.size()>0)
		{
			for(int r=rates.size()-1;r>=0;r--)
			{
				if(rates.get(r).first instanceof String)
				{
					final String place=(String)rates.get(r).first;
					if((observer!=null)&&(centerRoom!=null))
					{
						if(stringsToDo==null)
							stringsToDo=new PairVector<String,Double>();
						stringsToDo.addElement(place,rates.get(r).second);
					}
					rates.removeElementAt(r);
				}
			}
			if((stringsToDo!=null)&&(observer!=null))
			{
				Room R=null;
				String place=null;
				for(int r=0;r<stringsToDo.size();r++)
				{
					roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
					place=stringsToDo.get(r).first;
					R=(Room)CMLib.english().fetchEnvironmental(roomsInRange,place,false);
					if(R!=null) 
						rates.add(R,stringsToDo.get(r).second);
				}
				stringsToDo.clear();
				stringsToDo=null;
			}
			if(ratesVec==null)
			{
				ratesVec=new ArrayList<Room>();
				for(Pair<Object,Double> p : rates)
				{
					if(p.first instanceof Area)
						ratesVec.add(((Area)p.first).getRandomMetroRoom());
					else
					if(p.first instanceof Room)
						ratesVec.add((Room)p.first);
				}
			}
			roomR=(Room)CMLib.english().fetchEnvironmental(ratesVec,where,true);
			if(roomR==null)
				roomR=(Room)CMLib.english().fetchEnvironmental(ratesVec,where,false);
			if(roomR==null)
			{
				Area A=CMLib.map().findArea(where);
				if(A!=null)
				{
					roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
					roomR=findNearestAreaRoom(A,roomsInRange);
				}
			}
		}
		if(roomR==null)
		{
			roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
			roomR=CMLib.map().findFirstRoom(new IteratorEnumeration<Room>(roomsInRange.iterator()), mob, where, false, 5);
		}
		return roomR;
	}

	protected boolean mayGiveThisMoney(final MOB source, final MOB conceirgeM, final Room room, final Environmental possibleCoins)
	{
		if(possibleCoins!=null)
		{
			if(!(possibleCoins instanceof Coins))
			{
				if(room.isInhabitant(conceirgeM))
				{
					CMLib.commands().postSay(conceirgeM,source,L("I'm sorry, I can only accept money."),true,false);
					return false;
				}
				return true;
			}
			
			final int destIndex=destinations.indexOfFirst(source);
			if(destIndex<0)
			{
				CMLib.commands().postSay(conceirgeM,source,L("What's this for?  Please tell me where you'd like to go first."),true,false);
				return false;
			}
			else
			if(!((Coins)possibleCoins).getCurrency().equalsIgnoreCase(CMLib.beanCounter().getCurrency(conceirgeM)))
			{
				CMLib.commands().postSay(conceirgeM,source,L("I'm sorry, I don't accept that kind of currency."),true,false);
				return false;
			}
			final Double owed=destinations.get(destinations.indexOfFirst(source)).third;
			if(owed.doubleValue()<=0.0)
			{
				CMLib.commands().postSay(conceirgeM,source,L("Hey, you've already paid me!"),true,false);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		final MOB source=msg.source();
		if(startRoom==null)
			startRoom=source.location();
		if((!canFreelyBehaveNormal(affecting))&&(affecting instanceof MOB))
			return true;
		final Environmental observer=affecting;
		final Room room=source.location();
		if(source != observer)
		{
			if((msg.amITarget(getReceiver(observer,room)))
			&&(msg.targetMinor()==CMMsg.TYP_GIVE)
			&&(!(observer instanceof Container))
			&&(!(observer instanceof MOB)))
				return this.mayGiveThisMoney(source, getTalker(observer,room), room, msg.tool());
			else 
			if((msg.amITarget(observer))
			&&(msg.targetMinor()==CMMsg.TYP_PUT)
			&&(observer instanceof Container))
				return this.mayGiveThisMoney(source, getTalker(observer,room), room, msg.tool());
			else 
			if((msg.targetMinor()==CMMsg.TYP_DROP)
			&&(!(observer instanceof MOB)))
				return this.mayGiveThisMoney(source, getTalker(observer,room), room, msg.target());
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof Environmental)
		&&(tickID==Tickable.TICKID_MOB)
		&&(thingsToSay.size()>0)
		&&(canFreelyBehaveNormal(ticking))||(!(ticking instanceof MOB)))
		{
			final Room R=CMLib.map().roomLocation((Environmental)ticking);
			synchronized(thingsToSay)
			{
				while(thingsToSay.size()>0)
				{
					final MOB source=thingsToSay.get(0).first;
					final MOB observer=getTalker((Environmental)ticking,source.location());
					final String msg=thingsToSay.get(0).second;
					thingsToSay.removeElementAt(0);
					if((R!=null)&&(R.isHere(source)))
						CMLib.commands().postSay(observer,source,msg,true,false);
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	protected void executeMoneyDrop(final MOB source, final MOB conciergeM, final Environmental possibleCoins, final CMMsg addToMsg)
	{
		if(possibleCoins instanceof Coins)
		{
			final int destIndex=destinations.indexOfFirst(source);
			if(destIndex>=0)
			{
				Quad<MOB,Room,Double,TrackingFlags> destT=destinations.get(destIndex);
				final Room destR=destT.second;
				final Double owed=Double.valueOf(destT.third.doubleValue() - ((Coins)possibleCoins).getTotalValue());
				if(owed.doubleValue()>0.0)
				{
					destT.third=owed;
					CMLib.commands().postSay(conciergeM,source,L("Ok, you still owe @x1.",CMLib.beanCounter().nameCurrencyLong(conciergeM,owed.doubleValue())),true,false);
					return;
				}
				else
				if(owed.doubleValue()<0.0)
				{
					final double change=-owed.doubleValue();
					final Coins C=CMLib.beanCounter().makeBestCurrency(conciergeM,change);
					if((change>0.0)&&(C!=null))
					{
						// this message will actually end up triggering the hand-over.
						final CMMsg newMsg=CMClass.getMsg(conciergeM,source,C,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Heres your change.' to <T-NAMESELF>.^?"));
						C.setOwner(conciergeM);
						final long num=C.getNumberOfCoins();
						final String curr=C.getCurrency();
						final double denom=C.getDenomination();
						C.destroy();
						C.setNumberOfCoins(num);
						C.setCurrency(curr);
						C.setDenomination(denom);
						destT.third=Double.valueOf(0.0);
						addToMsg.addTrailerMsg(newMsg);
					}
					else
						CMLib.commands().postSay(conciergeM,source,L("Gee, thanks. :)"),true,false);
				}
				((Coins)possibleCoins).destroy();
				giveMerchandise(source, destR, conciergeM, source.location(), destT.fourth);
				destinations.removeElementFirst(source);
			}
			else
			if(!CMLib.flags().canBeSeenBy(source,conciergeM))
				CMLib.commands().postSay(conciergeM,null,L("Wha?  Where did this come from?  Cool!"),false,false);
		}
	}

	protected String getDestinationName(Environmental destination)
	{
		return (destination instanceof Room)?destination.displayText():destination.name();
	}
	
	protected void giveMerchandise(MOB whoM, Room destination, Environmental observer, Room room, TrackingFlags trackingFlags)
	{
		
		if(this.portal)
		{
			final Room R=whoM.location();
			final Ability spell=CMClass.getAbility("Spell_Portal");
			final CMMsg msg=CMClass.getMsg(whoM,R,spell,CMMsg.MSG_CAST_VERBAL_SPELL,L("A blinding, swirling portal appears here."));
			final CMMsg msg2=CMClass.getMsg(whoM,destination,spell,CMMsg.MSG_CAST_VERBAL_SPELL,L("A blinding, swirling portal appears here."));
			if((R.okMessage(whoM,msg))&&(destination.okMessage(whoM,msg2)))
			{
				R.send(whoM,msg);
				destination=(Room)msg2.target();
				destination.sendOthers(whoM,msg2);
				Item I=CMClass.getItem("GenPortal");
				I.setName(L("A portal to @x1",getDestinationName(destination)));
				I.setDisplayText(L("A portal to @x1 swirls here",getDestinationName(destination)));
				I.setReadableText(CMLib.map().getExtendedRoomID(destination));
				R.addItem(I, Expire.Monster_EQ);
				Behavior B=CMClass.getBehavior("Decay");
				B.setParms("minticks=8 maxticks=12");
				I.addBehavior(B);
				thingsToSay.addElement(whoM,L("Enter this portal to @x1.",getDestinationName(destination)));
			}
		}
		else
		{
			MOB fromM=getTalker(observer,room);
			String name=CMLib.map().getExtendedRoomID(destination);
			if(name.length()==0)
				name=destination.displayText();
			final List<Room> set=new ArrayList<Room>();
			CMLib.tracking().getRadiantRooms(fromM.location(),set,trackingFlags,null,maxRange,null);
			String trailStr;
			if(CMLib.tracking().canValidTrail(fromM.location(), set, name, maxRange, null, 1))
				trailStr=CMLib.tracking().getTrailToDescription(fromM.location(),set,name,false,false,maxRange,null,1);
			else
			{
				//set.clear();
				TrackingFlags noAirFlags = trackingFlags.copyOf();
				noAirFlags.add(TrackingFlag.NOAIR);
				CMLib.tracking().getRadiantRooms(fromM.location(),set,noAirFlags,null,maxRange,null);
				trailStr=CMLib.tracking().getTrailToDescription(fromM.location(),set,name,false,false,maxRange,null,1);
			}
			thingsToSay.addElement(whoM,L("The way to @x1 from here is: @x2",getDestinationName(destination),trailStr));
		}
	}
	
	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canFreelyBehaveNormal(affecting))&&(affecting instanceof MOB)) 
			return;

		final MOB source=msg.source();
		final Environmental observer=affecting;
		final Room room=source.location();
		if(source!=observer)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if(msg.amITarget(getReceiver(observer,room)))
					executeMoneyDrop(source,getTalker(observer,room),msg.tool(),msg);
				break;
			case CMMsg.TYP_DROP:
				if((!(observer instanceof Container))
				&&((!(observer instanceof Rideable))||(msg.source().riding()==observer))
				&&(!(observer instanceof MOB)))
					executeMoneyDrop(source,getTalker(observer,room),msg.target(),msg);
				break;
			case CMMsg.TYP_PUT:
				if((msg.amITarget(observer))
				&&((!(observer instanceof Rideable))||(msg.source().riding()==observer))
				&&(observer instanceof Container))
					executeMoneyDrop(source,getTalker(observer,room),msg.tool(),msg);
				break;
			case CMMsg.TYP_SPEAK:
				if((msg.source()==getTalker(observer,room))
				&&(msg.target() instanceof MOB)
				&&(msg.tool() instanceof Coins)
				&&(((Coins)msg.tool()).amDestroyed())
				&&(!msg.source().isMine(msg.tool()))
				&&(!((MOB)msg.target()).isMine(msg.tool())))
					CMLib.beanCounter().giveSomeoneMoney(msg.source(),(MOB)msg.target(),((Coins)msg.tool()).getTotalValue());
				else
				if((!msg.source().isMonster())
				&&((msg.target()==observer)||(source.location().numPCInhabitants()==1)||(!(observer instanceof MOB)))
				&&((!(observer instanceof Rideable))||(msg.source().riding()==observer))
				&&(msg.sourceMessage()!=null))
				{
					String say=CMStrings.getSayFromMessage(msg.sourceMessage());
					if((say!=null)&&(say.length()>0))
					{
						List<String> parsedSay = CMParms.parse(say);
						boolean didLoop=true;
						TrackingFlags myRoomRadiusFlags=CMLib.tracking().newFlags();
						myRoomRadiusFlags.addAll(roomRadiusFlags);
						boolean didAnything=false;
						while(didLoop && (parsedSay.size()>0))
						{
							didLoop=false;
							String s=parsedSay.get(parsedSay.size()-1);
							if(s.equalsIgnoreCase("noclimb"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOCLIMB);
							else
							if(s.equalsIgnoreCase("nodoors"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.OPENONLY);
							else
							if(s.equalsIgnoreCase("noswim"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOWATER);
							else
							if(s.equalsIgnoreCase("nofly"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOAIR);
							else
							if(s.equalsIgnoreCase("nolocks"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.UNLOCKEDONLY);
							else
							if(s.equalsIgnoreCase("nocrawl"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOCRAWL);
							if(didLoop)
								parsedSay.remove(parsedSay.size()-1);
							didAnything = didAnything || didLoop;
						}
						Room roomR;
						if(didAnything)
						{
							say=CMParms.combine(parsedSay);
							roomR=findDestination(observer,msg.source(),room,say,myRoomRadiusFlags);
							if((roomR==null)||(roomR.roomID().length()==0))
							{
								final Room roomR2=findDestination(observer,msg.source(),room,say,trackingFlags);
								if(roomR2!=null)
									roomR=roomR2;
							}
						}
						else
						{
							roomR=findDestination(observer,msg.source(),room,say,trackingFlags);
							if((roomR==null)||(roomR.roomID().length()==0))
							{
								final Room roomR2=findDestination(observer,msg.source(),room,say,myRoomRadiusFlags);
								if(roomR2!=null)
									roomR=roomR2;
							}
						}
						if(roomR==null)
						{
							synchronized(thingsToSay)
							{
								thingsToSay.addElement(msg.source(),L("I'm sorry, I don't know where '@x1' is.",say));
								return;
							}
						}
						final double rate=getPrice(room,roomR);
						final Double owed=Double.valueOf(rate);
						trackingFlags.addAll(myRoomRadiusFlags);
						destinations.removeElementFirst(msg.source());
						if(owed.doubleValue()<=0.0)
							giveMerchandise(msg.source(), roomR, observer, room, trackingFlags);
						else
						{
							destinations.addElement(msg.source(),roomR,owed, trackingFlags);
							final String moneyName=CMLib.beanCounter().nameCurrencyLong(getTalker(observer,room),rate);
							thingsToSay.addElement(msg.source(),this.getGiveMoneyMessage(observer,roomR,moneyName));
						}
					}
				}
				break;
			case CMMsg.TYP_LEAVE:
				if((msg.target()==room)
				&&(!disableComingsAndGoings())
				&&(destinations.containsFirst(msg.source())))
					destinations.removeElementFirst(msg.source());
				break;
			case CMMsg.TYP_ENTER:
				if((!msg.source().isMonster())
				&&((greeting!=null)&&(greeting.length()>0))
				&&(!disableComingsAndGoings())
				&&(!destinations.containsFirst(msg.source()))
				&&(msg.target()==CMLib.map().roomLocation(observer))
				&&(CMLib.flags().canBeSeenBy(msg.source(), getTalker(observer,room))))
					thingsToSay.addElement(msg.source(),greeting);
				//$FALL-THROUGH$
			case CMMsg.TYP_MOUNT:
				if((!msg.source().isMonster())
				&&(msg.target()==observer)
				&&((mountStr!=null)&&(mountStr.length()>0))
				&&(!destinations.containsFirst(msg.source()))
				&&(CMLib.flags().canBeSeenBy(msg.source(), getTalker(observer,room))))
					thingsToSay.addElement(msg.source(),mountStr);
			}
		}
	}
}
