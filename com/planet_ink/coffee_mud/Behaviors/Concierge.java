package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2008-2014 Bo Zimmerman

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
	@Override public String ID(){return "Concierge";}
	@Override protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;}

	protected PairVector<Object,Double> rates=new PairVector<Object,Double>();
	protected List<Environmental> ratesVec=null;
	protected TriadVector<MOB,Environmental,Double> destinations=new TriadVector<MOB,Environmental,Double>();
	protected PairVector<MOB,String> thingsToSay=new PairVector<MOB,String>();
	protected double basePrice=0.0;
	protected String talkerName="";
	protected MOB fakeTalker=null;
	protected Room startRoom=null;
	protected boolean areaOnly=false;
	protected String greeting="Need directions? Just name the place!";
	protected String mountStr="";
	protected int maxRange = 100;
	
	private final TrackingLibrary.TrackingFlags trackingFlags = new TrackingLibrary.TrackingFlags().plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
	
	protected TrackingLibrary.TrackingFlags getTrackingFlags()
	{
		return trackingFlags;
	}

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
			return L("Yep, I can help you find @x1, but you'll need to give me @x2 first.",getDestinationName(destination),moneyName);
		else
		if(observer instanceof Container)
			return L("Yep, I can help you find @x1, but you'll need to put @x2 into @x3 first.",getDestinationName(destination),moneyName,observer.name());
		else
			return L("Yep, I can help you find @x1, but you'll need to drop @x2 first.",getDestinationName(destination),moneyName);
	}
	
	protected final MOB getTalker(Environmental o, Room room)
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

	protected void resetDefaults()
	{
		basePrice=0.0;
		talkerName="";
		fakeTalker=null;
		startRoom=null;
		areaOnly=false;
		greeting="Need directions? Just name the place!";
		mountStr="";
		maxRange = 100;
		rates.clear();
		ratesVec=null;
		destinations.clear();
		thingsToSay.clear();
	}
	
	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
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
		for(int v=0;v<V.size();v++)
		{
			s=V.get(v);
			x=s.indexOf('=');
			if(x>0)
			{
				String numStr=s.substring(x+1).trim();
				if(numStr.startsWith("\"")&&(numStr.endsWith("\"")))
					numStr=numStr.substring(1,numStr.length()-1).trim();
				s=s.substring(0,x).trim().toUpperCase();
				if(s.equals("AREAONLY"))
				{
					areaOnly=CMath.s_bool(numStr);
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

	protected double getPrice(Environmental E)
	{
		if(E==null) 
			return basePrice;
		if(rates.size()==0) 
			return basePrice;
		final int rateIndex=rates.indexOfFirst(E);
		if(rateIndex<0) 
			return basePrice;
		return rates.get(rateIndex).second.doubleValue();
	}

	protected final List<Room> getRoomsInRange(final Room centerRoom, final List<Room> roomsInRange)
	{
		if(roomsInRange != null)
			return roomsInRange;
		TrackingLibrary.TrackingFlags flags;
		if(areaOnly)
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY);
		else
			flags = new TrackingLibrary.TrackingFlags();
		return
			CMLib.tracking().getRadiantRooms(centerRoom,flags,maxRange);
	}
	
	protected Environmental findDestination(final Environmental observer, final MOB mob, final Room centerRoom, final String where)
	{
		PairVector<String,Double> stringsToDo=null;
		Environmental E=null;
		List<Room> roomsInRange=null;
		if(rates.size()==0)
			E=CMLib.map().findArea(where);
		else
		if(rates.size()>0)
		{
			for(int r=rates.size()-1;r>=0;r--)
				if(rates.get(r).first instanceof String)
				{
					final String place=(String)rates.get(r).first;
					if((observer!=null)&&(centerRoom!=null))
					{
						if(stringsToDo==null) stringsToDo=new PairVector<String,Double>();
						stringsToDo.addElement(place,rates.get(r).second);
					}
					rates.removeElementAt(r);
				}
			if((stringsToDo!=null)&&(observer!=null))
			{
				Room R=null;
				String place=null;
				for(int r=0;r<stringsToDo.size();r++)
				{
					roomsInRange=getRoomsInRange(centerRoom,roomsInRange);
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
				ratesVec=new ArrayList<Environmental>();
				for(Pair<Object,Double> p : rates)
					if(p.first instanceof Environmental)
						ratesVec.add((Environmental)p.first);
			}
			E=CMLib.english().fetchEnvironmental(ratesVec,where,true);
			if(E==null)
				E=CMLib.english().fetchEnvironmental(ratesVec,where,false);
			if(E==null)
				E=CMLib.map().findArea(where);
		}
		if(E==null)
		{
			roomsInRange=getRoomsInRange(centerRoom,roomsInRange);
			E=CMLib.map().findFirstRoom(new IteratorEnumeration<Room>(roomsInRange.iterator()), mob, where, false, 5);
		}
		return E;
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
			final Environmental destination=destinations.get(destIndex).second;
			final Double paid=destinations.get(destinations.indexOfFirst(source)).third;
			final double owed=getPrice(destination)-paid.doubleValue();
			if(owed<=0.0)
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
			if((msg.amITarget(observer))
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
			synchronized(thingsToSay)
			{
				while(thingsToSay.size()>0)
				{
					final MOB source=thingsToSay.get(0).first;
					final MOB observer=getTalker((Environmental)ticking,source.location());
					final String msg=thingsToSay.get(0).second;
					thingsToSay.removeElementAt(0);
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
				final Environmental destination=destinations.elementAt(destIndex).second;
				final Double paid=destinations.elementAt(destIndex).third;
				double owed=getPrice(destination)-paid.doubleValue();
				owed-=((Coins)possibleCoins).getTotalValue();
				if(owed>0.0)
				{
					Triad<MOB,Environmental,Double> t=destinations.get(destIndex);
					t.third=Double.valueOf(owed);
					CMLib.commands().postSay(conciergeM,source,L("Ok, you still owe @x1.",CMLib.beanCounter().nameCurrencyLong(conciergeM,owed)),true,false);
					return;
				}
				else
				if(owed<0.0)
				{
					final double change=-owed;
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
						addToMsg.addTrailerMsg(newMsg);
					}
					else
						CMLib.commands().postSay(conciergeM,source,L("Gee, thanks. :)"),true,false);
				}
				((Coins)possibleCoins).destroy();
				this.giveMerchandise(source, destination, conciergeM, source.location());
				destinations.removeElementFirst(source);
			}
			else
			if(!CMLib.flags().canBeSeenBy(source,conciergeM))
				CMLib.commands().postSay(conciergeM,null,L("Wha?  Where did this come from?  Cool!"),true,false);
		}
	}

	protected String getDestinationName(Environmental destination)
	{
		return (destination instanceof Room)?destination.displayText():destination.name();
	}
	
	protected void giveMerchandise(MOB whoM, Environmental destination, Environmental observer, Room room)
	{
		MOB fromM=getTalker(observer,room);
		String name=destination.Name();
		if(destination instanceof Room) 
			name=CMLib.map().getExtendedRoomID((Room)destination);
		final Vector<Room> set=new Vector<Room>();
		CMLib.tracking().getRadiantRooms(fromM.location(),set,getTrackingFlags(),null,maxRange,null);
		String trailStr=CMLib.tracking().getTrailToDescription(fromM.location(),set,name,false,false,maxRange,null,1);
		thingsToSay.addElement(whoM,L("Yes, the way to @x1 from here is: @x2",getDestinationName(destination),trailStr));
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
				if(msg.amITarget(observer))
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
					final String say=CMStrings.getSayFromMessage(msg.sourceMessage());
					if((say!=null)&&(say.length()>0))
					{
						final Environmental E=findDestination(observer,msg.source(),room,say);
						if(E==null)
							synchronized(thingsToSay)
							{
								thingsToSay.addElement(msg.source(),L("I'm sorry, I don't know where '@x1' is.",say));
								return;
							}
						final int index=destinations.indexOfFirst(msg.source());
						final Double paid=(index>=0)?destinations.get(index).third:Double.valueOf(0.0);
						destinations.removeElementFirst(msg.source());
						final double rate=getPrice(E);
						if(rate<=0.0)
							giveMerchandise(msg.source(), E, observer, room);
						else
						{
							destinations.addElement(msg.source(),E,paid);
							final String moneyName=CMLib.beanCounter().nameCurrencyLong(getTalker(observer,room),rate);
							thingsToSay.addElement(msg.source(),this.getGiveMoneyMessage(observer,E,moneyName));
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
