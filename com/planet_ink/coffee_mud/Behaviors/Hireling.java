package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Hireling extends StdBehavior
{
	public String ID(){return "Hireling";}
	public Behavior newInstance(){	return new Hireling();}

	private Hashtable partials=new Hashtable();
	private String workingFor="";
	private long onTheJobUntil=0;

	private int price()
	{
		int price=100;
		if(getParms().indexOf(";")>=0)
			price=Util.s_int(getParms().substring(0,getParms().indexOf(";")));
		return price;
	}

	private int minutes()
	{
		int mins=30;
		if(getParms().indexOf(";")>=0)
			mins=Util.s_int(getParms().substring(getParms().indexOf(";")+1));
		return mins;
	}

	public void allDone(MOB observer)
	{
		workingFor="";
		onTheJobUntil=0;
		ExternalPlay.follow(observer,null,false);
		observer.setFollowing(null);
		int direction=-1;
		for(int d=0;d<4;d++)
			if(observer.location().getExitInDir(d)!=null)
				if(observer.location().getExitInDir(d).isOpen())
				{
					direction=d;
					break;
				}
				else
					direction=d;
		if(direction>=0)
			ExternalPlay.move(observer,direction,false,false);
		if(observer.getStartRoom()!=null)
			observer.getStartRoom().bringMobHere(observer,false);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return true;
		if(onTheJobUntil==0) return true;
		MOB observer=(MOB)ticking;
		if(System.currentTimeMillis()>onTheJobUntil)
		{
			Integer I=(Integer)partials.get(workingFor);
			partials.remove(workingFor);
			ExternalPlay.standIfNecessary(observer);
			if(!canActAtAll(observer))
			{
				workingFor="";
				onTheJobUntil=0;
				observer.setFollowing(null);
				if(observer.getStartRoom()!=null)
					observer.getStartRoom().bringMobHere(observer,false);
				return true;
			}
			MOB talkTo=null;
			if((workingFor.length()>0)&&(observer.location()!=null))
			{
				talkTo=observer.location().fetchInhabitant(workingFor);
				if(talkTo!=null)
					observer.setFollowing(talkTo);
			}
			int additional=0;
			if(I!=null)
				additional+=(int)Math.round(Util.mul(Util.div(minutes(),price()),I.intValue()));
			if(additional<=0)
			{
				if(talkTo!=null)
					ExternalPlay.quickSay(observer,talkTo,"Your time is up.  Goodbye!",true,false);
				allDone(observer);
			}
			else
			{
				if(talkTo!=null)
					ExternalPlay.quickSay(observer,talkTo,"Your base time is up, but you've paid for "+additional+" more minutes, so I'll hang around.",true,false);
				onTheJobUntil+=(additional*IQCalendar.MILI_MINUTE);
			}
		}
		else
		if((workingFor.length()>0)
		   &&(observer.location()!=null)
		   &&(observer.amFollowing()==null)
		   &&(canFreelyBehaveNormal(observer)))
		{
			MOB talkTo=observer.location().fetchInhabitant(workingFor);
			if(talkTo!=null)
			{
				ExternalPlay.follow(observer,talkTo,false);
				observer.setFollowing(talkTo);
			}
		}
		return true;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canActAtAll(affecting)) return;

		MOB observer=(MOB)affecting;
		if((affect.sourceMinor()==Affect.TYP_QUIT)
		&&(affect.amISource(observer)||affect.amISource(observer.amFollowing())))
		   allDone(observer);
		else
		if((affect.sourceMinor()==Affect.TYP_SPEAK)
		&&(!affect.amISource(observer))
		&&(!affect.source().isMonster()))
		{
			if(((affect.sourceMessage().toUpperCase().indexOf(" HIRE")>0)
				||(affect.sourceMessage().toUpperCase().indexOf("'HIRE")>0))
			&&(onTheJobUntil==0))
				ExternalPlay.quickSay(observer,null,"I'm for hire.  Just give me "+price()+" and I'll work for you.",false,false);
			else
			if(((affect.sourceMessage().toUpperCase().indexOf(" FIRED")>0))
			&&(affect.amITarget(observer))
			&&(onTheJobUntil!=0))
			{
				ExternalPlay.quickSay(observer,affect.source(),"Suit yourself.  Goodbye.",false,false);
				allDone(observer);
			}
		}
		else
		if(affect.amITarget(observer)
		   &&(!affect.amISource(observer))
		   &&(affect.targetMinor()==Affect.TYP_GIVE)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Coins))
		{
			int given=((Coins)affect.tool()).numberOfCoins();
			if(partials.get(affect.source().Name())!=null)
			{
				given+=((Integer)partials.get(affect.source().Name())).intValue();
				partials.remove(affect.source().Name());
			}
			if(given<price())
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						ExternalPlay.quickSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						ExternalPlay.quickSay(observer,source,"Sorry, I'm on the job right now.  Give me "+(price()-given)+" more later on and I'll work.",true,false);
				}
				else
					ExternalPlay.quickSay(observer,source,"My price is "+price()+".  Give me "+(price()-given)+" more and I'll work.",true,false);
				partials.put(affect.source().Name(),new Integer(given));
			}
			else
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						ExternalPlay.quickSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						ExternalPlay.quickSay(observer,source,"Sorry, I'm on the job right now.  Give me 1 more coin later on and I'll work.",true,false);
					partials.put(affect.source().Name(),new Integer(given));
				}
				else
				{
					if(given>price())
						partials.put(affect.source().Name(),new Integer(given-price()));
					StringBuffer skills=new StringBuffer("");
					for(int a=0;a<observer.numAbilities();a++)
					{
						Ability A=observer.fetchAbility(a);
						if(A.profficiency()==0)
							A.setProfficiency(50+observer.envStats().level()-CMAble.lowestQualifyingLevel(A.ID()));
						skills.append(", "+A.name());
					}
					workingFor=source.Name();
					onTheJobUntil=System.currentTimeMillis();
					onTheJobUntil+=(minutes()*IQCalendar.MILI_MINUTE);
					ExternalPlay.follow(observer,source,false);
					observer.setFollowing(source);
					ExternalPlay.quickSay(observer,source,"Ok.  You've got me for at least "+minutes()+" minutes.  My skills include: "+skills.substring(2)+".  I'll follow you.  Just ORDER me to do what you want.",true,false);
				}
			}
		}
	}
}