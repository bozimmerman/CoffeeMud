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
	private Calendar onTheJobUntil=null;
	
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
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return;
		if(onTheJobUntil==null) return;
		MOB observer=(MOB)ticking;
		if(onTheJobUntil.before(Calendar.getInstance()))
		{
			Integer I=(Integer)partials.get(workingFor);
			partials.remove(workingFor);
			if(!canFreelyBehaveNormal(observer))
			{
				workingFor="";
				onTheJobUntil=null;
				observer.setFollowing(null);
				return;
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
				workingFor="";
				onTheJobUntil=null;
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
					ExternalPlay.move(observer,direction,false);
				if(observer.getStartRoom()!=null)
					observer.getStartRoom().bringMobHere(observer,false);
			}
			else
			{
				if(talkTo!=null)
					ExternalPlay.quickSay(observer,talkTo,"Your base time is up, but you've paid for "+additional+" more minutes, so I'll hang around.",true,false);
				onTheJobUntil.add(Calendar.MINUTE,additional);
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
	}
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting)) return;
		
		MOB observer=(MOB)affecting;
		if((affect.sourceMinor()==Affect.TYP_SPEAK)
		&&(affect.sourceMessage().toUpperCase().indexOf("HIRE")>0)
		&&(onTheJobUntil==null))
			ExternalPlay.quickSay(observer,null,"I'm for hire.  Just give me "+price()+" and I'll work for you.",false,false);
		else
		if(affect.amITarget(observer)
		   &&(!affect.amISource(observer))
		   &&(affect.targetMinor()==Affect.TYP_GIVE)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Coins))
		{
			int given=((Coins)affect.tool()).numberOfCoins();
			if(partials.get(affect.source().name())!=null)
			{
				partials.remove(affect.source().name());
				given+=((Integer)partials.get(affect.source().name())).intValue();
			}
			if(given<price())
			{
				if(onTheJobUntil!=null)
				{
					if(workingFor.equals(source.name()))
						ExternalPlay.quickSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						ExternalPlay.quickSay(observer,source,"Sorry, I'm on the job right now.  Give me "+(price()-given)+" more later on and I'll work.",true,false);
				}
				else
					ExternalPlay.quickSay(observer,source,"My price is "+price()+".  Give me "+(price()-given)+" more and I'll work.",true,false);
				partials.put(affect.source().name(),new Integer(given));
			}
			else
			{
				if(onTheJobUntil!=null)
				{
					if(workingFor.equals(source.name()))
						ExternalPlay.quickSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						ExternalPlay.quickSay(observer,source,"Sorry, I'm on the job right now.  Give me 1 more coin later on and I'll work.",true,false);
					partials.put(affect.source().name(),new Integer(given));
				}
				else
				{
					StringBuffer skills=new StringBuffer("");
					for(int a=0;a<observer.numAbilities();a++)
					{
						Ability A=observer.fetchAbility(a);
						skills.append(", "+A.name());
					}
					workingFor=source.name();
					onTheJobUntil=Calendar.getInstance();
					onTheJobUntil.add(Calendar.MINUTE,minutes());
					ExternalPlay.follow(observer,source,false);
					observer.setFollowing(source);
					ExternalPlay.quickSay(observer,source,"Ok.  You've got me for "+minutes()+" minutes.  My skills include: "+skills.substring(3)+".  Just ORDER me to do what you want.",true,false);
				}
			}
		}
	}
}