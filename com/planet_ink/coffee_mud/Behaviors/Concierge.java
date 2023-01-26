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
   Copyright 2006-2023 Bo Zimmerman

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

	protected static enum TrackWords
	{
		PORTAL(null,null),
		MOBILE(null,null),
		CLAN(null,null),
		AREAONLY(null,TrackingFlag.AREAONLY),
		NOCLIMB(TrackingFlag.NOCLIMB,null),
		NOCRAWL(TrackingFlag.NOCRAWL,null),
		NOWATER(TrackingFlag.NOWATER,null),
		INDOOROK(TrackingFlag.OUTDOORONLY,null/*--TrackingFlag.OUTDOORONLY*/),
		NOAIR(TrackingFlag.NOAIR,null),
		NOLOCKS(TrackingFlag.UNLOCKEDONLY,null),
		NOHOMES(TrackingFlag.NOHOMES,null),
		NOINDOOR(TrackingFlag.OUTDOORONLY,null/*--TrackingFlag.OUTDOORONLY*/),
		MAXRANGE(null,null),
		GREETING(null,null),
		ENTERMSG(null,null),
		TALKERNAME(null,null),
		PERROOM(null,null)
		;
		public TrackingFlag tf;
		public TrackingFlag rf;
		private TrackWords(final TrackingFlag tf, final TrackingFlag rf)
		{
			this.tf=tf;
			this.rf=rf;
		}
	}

	protected PairVector<Object, Double>		rates		= new PairVector<Object, Double>();
	protected List<Room>						ratesVec	= null;
	protected PairVector<MOB, String>			thingsToSay	= new PairVector<MOB, String>();

	protected QuadVector<MOB, Room, Double, TrackingFlags>	destinations= new QuadVector<MOB, Room, Double, TrackingFlags>();

	protected static final String defaultGreeting = "Need directions? Just name the place and I'll name the price! Append words like noswim, noclimb, nofly, nolocks, noprivate, and nocrawl to narrow the focus.";

	protected final static TrackingLibrary.TrackingFlags defaultTrackingFlags	= CMLib.tracking().newFlags()
																				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
																				.plus(TrackingLibrary.TrackingFlag.NOHOMES)
																				.plus(TrackingLibrary.TrackingFlag.PASSABLE)
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
	protected boolean	mobile			= false;
	protected int		maxRange		= 100;
	protected String	clanName		= null;
	protected boolean	goHomeFlag		= false;

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

	protected String getGiveMoneyMessage(final MOB mob, final Environmental observer, final Environmental destination, final String moneyName)
	{
		if(observer instanceof MOB)
			return L("I can help you find @x1, but you'll need to give me @x2 first.",getDestinationName(mob,destination),moneyName);
		else
		if(observer instanceof Container)
			return L("I can help you find @x1, but you'll need to put @x2 into @x3 first.",getDestinationName(mob,destination),moneyName,observer.name());
		else
			return L("I can help you find @x1, but you'll need to drop @x2 first.",getDestinationName(mob,destination),moneyName);
	}

	protected MOB getTalker(final Environmental o, final Room room)
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
			final Environmental E=CMLib.english().fetchEnvironmental(((Rideable)o).riders(),talkerName,true);
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

	protected Environmental getReceiver(final Environmental o, final Room room)
	{
		return o;
	}

	protected void resetDefaults()
	{
		basePrice=0.0;
		talkerName="";
		clanName=null;
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
		mobile=false;
	}

	protected void resetFlags()
	{
		trackingFlags	= CMLib.tracking().newFlags().plus(defaultTrackingFlags);
		roomRadiusFlags = CMLib.tracking().newFlags().plus(defaultRoomRadiusFlags);
	}

	@Override
	public void setParms(final String newParm)
	{
		super.setParms(newParm);
		resetDefaults();
		if(CMath.isInteger(newParm) || CMath.isDouble(newParm))
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
				final boolean isTrue=numStr.toLowerCase().startsWith("t");
				final TrackWords tw = (TrackWords)CMath.s_valueOf(TrackWords.class, s);
				if(tw == null)
				{
					if(CMath.isNumber(numStr))
					{
						price=CMath.s_double(numStr);
						continue;
					}
				}
				else
				switch(tw)
				{
				case PORTAL:
					portal=isTrue;
					continue;
				case MOBILE:
					mobile=isTrue;
					continue;
				case CLAN:
					clanName=numStr;
					continue;
				case MAXRANGE:
					maxRange=CMath.s_int(numStr);
					continue;
				case GREETING:
					greeting=numStr;
					continue;
				case ENTERMSG:
					mountStr=numStr;
					continue;
				case TALKERNAME:
					talkerName=numStr;
					continue;
				case PERROOM:
					perRoomPrice=CMath.s_double(numStr);
					continue;
				default:
					if(tw.tf != null)
					{
						if(isTrue)
						{
							this.trackingFlags.add(tw.tf);
							this.roomRadiusFlags.remove(tw.tf);
						}
					}
					else
					if(tw.rf != null)
					{
						if(isTrue)
							this.roomRadiusFlags.add(tw.rf);
					}
					else
						Log.errOut("Broken Concierge flag: "+tw);
					break;
				}
			}
			else
			if(CMath.isNumber(s))
			{
				price=CMath.s_double(s);
				continue;
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
				final List<Room> trail=CMLib.tracking().findTrailToRoom(centerRoom, destR, trackingFlags, maxRange);
				if(trail != null)
					price = price + (perRoomPrice * trail.size());
			}
			return price;
		}
		return rates.get(rateIndex).second.doubleValue();
	}

	protected final List<Room> getRoomsInRange(final Room centerRoom, final List<Room> roomsInRange, final TrackingFlags roomRadiusFlags)
	{
		if(roomsInRange != null)
			return roomsInRange;
		return CMLib.tracking().getRadiantRooms(centerRoom,roomRadiusFlags,maxRange);
	}

	protected Room findNearestAreaRoom(final Area A, final List<Room> roomsInRange)
	{
		for(final Room R : roomsInRange)
		{
			if(A.inMyMetroArea(R.getArea()))
				return R;
		}
		return A.getRandomMetroRoom();
	}

	protected boolean isClanRoom(final Places P)
	{
		if(clanName == null)
			return false;
		LegalBehavior B=null;
		if(P instanceof Area)
		{
			final Area A=(Area)P;
			final PrivateProperty rec = CMLib.law().getPropertyRecord(A);
			if((rec != null)
			&&(clanName.equalsIgnoreCase(rec.getOwnerName())))
				return true;
			B=CMLib.law().getLegalBehavior(A);
		}
		else
		if(P instanceof Room)
		{
			final Room R=(Room)P;
			final PrivateProperty rec = CMLib.law().getPropertyRecord(R);
			if((rec != null)
			&&(clanName.equalsIgnoreCase(rec.getOwnerName())))
				return true;
			B=CMLib.law().getLegalBehavior(R);
		}
		if(B==null)
			return false;
		return (B.rulingOrganization().equalsIgnoreCase(clanName));
	}

	protected boolean isAllowedPlace(final Places A)
	{
		if(A==null)
			return false;
		if(clanName == null)
			return true;
		return this.isClanRoom(A);
	}

	protected Room findDestination(final Environmental observer, final MOB mob, final Room centerRoom, final String where, final TrackingFlags roomRadiusFlags)
	{
		PairVector<String,Double> stringsToDo=null;
		Room roomR=null;
		List<Room> roomsInRange=null;
		if(rates.size()==0)
		{
			final Area A=CMLib.map().findArea(where);
			if(isAllowedPlace(A))
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
				for(final Pair<Object,Double> p : rates)
				{
					if(p.first instanceof Area)
						ratesVec.add(((Area)p.first).getRandomMetroRoom());
					else
					if(p.first instanceof Room)
						ratesVec.add((Room)p.first);
				}
			}
			if((clanName != null) && (where.equalsIgnoreCase("home")))
			{
				final Clan C=CMLib.clans().getClan(clanName);
				if((C!=null)&&(C.getRecall()!=null)&&(C.getRecall().length()>0))
					roomR=CMLib.map().getRoom(C.getRecall());
			}
			if(roomR==null)
				roomR=(Room)CMLib.english().fetchEnvironmental(ratesVec,where,true);
			if(roomR==null)
				roomR=(Room)CMLib.english().fetchEnvironmental(ratesVec,where,false);
			if((roomR != null)
			&&(!isAllowedPlace(roomR)))
				roomR=null;

			if(roomR==null)
			{
				final Area A=CMLib.map().findArea(where);
				if(isAllowedPlace(A))
				{
					roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
					roomR=findNearestAreaRoom(A,roomsInRange);
				}
			}
		}
		if((roomR==null)
		&&(clanName != null)
		&& (where.equalsIgnoreCase("home")))
		{
			final Clan C=CMLib.clans().getClan(clanName);
			if((C!=null)&&(C.getRecall()!=null)&&(C.getRecall().length()>0))
				roomR=CMLib.map().getRoom(C.getRecall());
		}
		if(roomR==null)
		{
			roomsInRange=getRoomsInRange(centerRoom,roomsInRange,roomRadiusFlags);
			roomR=CMLib.map().findFirstRoom(new IteratorEnumeration<Room>(roomsInRange.iterator()), mob, where, false, 5);
			if((roomR != null)
			&&(!isAllowedPlace(roomR)))
				roomR=null;
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
			if(!CMLib.beanCounter().isCurrencyMatch(((Coins)possibleCoins).getCurrency(),CMLib.beanCounter().getCurrency(conceirgeM)))
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
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		final MOB source=msg.source();
		if(startRoom==null)
			startRoom=source.location();
		if(host instanceof MOB)
		{
			if((!canFreelyBehaveNormal(host))||(CMLib.flags().isTracking((MOB)host)))
				return true;
		}
		final Environmental observer=host;
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
	public boolean tick(final Tickable ticking, final int tickID)
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
		if(this.mobile
		&& goHomeFlag
		&& (startRoom != null)
		&& (ticking instanceof MOB)
		&& !CMLib.flags().isTracking((MOB)ticking))
		{
			final MOB mob=(MOB)ticking;
			goHomeFlag=false;
			while(mob.numFollowers()>0)
				mob.delFollower(mob.fetchFollower(0));
			CMLib.commands().postSay(mob, L("OK, here you are -- good luck!"));
			CMLib.tracking().wanderAway(mob, false, true);
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
				final Quad<MOB,Room,Double,TrackingFlags> destT=destinations.get(destIndex);
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

	protected String getDestinationName(final MOB mob, final Environmental destination)
	{
		return (destination instanceof Room)?((Room)destination).displayText(mob):destination.name();
	}

	protected void giveMerchandise(final MOB whoM, Room destination, final Environmental observer,
								   final Room room, final TrackingFlags trackingFlags)
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
				final Item I=CMClass.getItem("GenPortal");
				I.setName(L("A portal to @x1",getDestinationName(whoM,destination)));
				I.setDisplayText(L("A portal to @x1 swirls here",getDestinationName(whoM,destination)));
				I.setReadableText(CMLib.map().getExtendedRoomID(destination));
				R.addItem(I, Expire.Monster_EQ);
				final Behavior B=CMClass.getBehavior("Decay");
				B.setParms("minticks=8 maxticks=12");
				I.addBehavior(B);
				thingsToSay.addElement(whoM,L("Enter this portal to @x1.",getDestinationName(whoM,destination)));
			}
		}
		else
		if(this.mobile && (observer instanceof MOB))
		{
			final MOB fromM=(MOB)observer;
			String name=CMLib.map().getExtendedRoomID(destination);
			if(name.length()==0)
				name=destination.displayText();
			final Ability tracker = CMClass.getAbility("Skill_Track");
			final List<String> cmds = new ArrayList<String>();
			cmds.add(name);
			cmds.add("RADIUS="+maxRange);
			for(final TrackingFlag flag : trackingFlags)
				cmds.add("FLAG="+flag.name());
			if(!tracker.invoke(fromM, cmds, destination, true, maxRange))
				thingsToSay.addElement(whoM,L("Sorry, I can't get there from here."));
			else
			{
				goHomeFlag=true;
				CMLib.commands().postFollow(whoM, fromM, false);
				thingsToSay.addElement(whoM,L("OK! Off we go!"));
			}
		}
		else
		{
			final MOB fromM=getTalker(observer,room);
			String name=CMLib.map().getExtendedRoomID(destination);
			if(name.length()==0)
				name=destination.displayText();
			final List<Room> set=new ArrayList<Room>();
			CMLib.tracking().getRadiantRooms(fromM.location(),set,trackingFlags,null,maxRange,null);
			String trailStr;
			if(CMLib.tracking().canValidTrail(fromM.location(), set, name, maxRange, null, 1))
				trailStr=CMLib.tracking().getTrailToDescription(fromM.location(),set,name,null,maxRange,null,1);
			else
			{
				//set.clear();
				final TrackingFlags noAirFlags = trackingFlags.copyOf();
				noAirFlags.add(TrackingFlag.NOAIR);
				CMLib.tracking().getRadiantRooms(fromM.location(),set,noAirFlags,null,maxRange,null);
				trailStr=CMLib.tracking().getTrailToDescription(fromM.location(),set,name,null,maxRange,null,1);
			}
			thingsToSay.addElement(whoM,L("The way to @x1 from here is: @x2",getDestinationName(whoM,destination),trailStr));
		}
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if(affecting instanceof MOB)
		{
			if((!canFreelyBehaveNormal(affecting))||(CMLib.flags().isTracking((MOB)affecting)))
				return;
		}

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
						final List<String> parsedSay = CMParms.parse(say);
						boolean didLoop=true;
						final TrackingFlags myRoomRadiusFlags=CMLib.tracking().newFlags();
						myRoomRadiusFlags.addAll(roomRadiusFlags);
						boolean didAnything=false;
						while(didLoop && (parsedSay.size()>0))
						{
							didLoop=false;
							final String s=parsedSay.get(parsedSay.size()-1);
							if(s.equalsIgnoreCase("noclimb"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOCLIMB);
							else
							if(s.equalsIgnoreCase("nodoors"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.OPENONLY);
							else
							if(s.equalsIgnoreCase("noswim"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOWATER);
							else
							if(s.equalsIgnoreCase("noprivate"))
								didLoop=myRoomRadiusFlags.add(TrackingFlag.NOPRIVATEPROPERTY);
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
								if(clanName != null)
									thingsToSay.addElement(msg.source(),L("I'm sorry, I don't know where '@x1' is amongst places controlled by @x2.",say,clanName));
								else
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
							thingsToSay.addElement(msg.source(),getGiveMoneyMessage(msg.source(),observer,roomR,moneyName));
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
