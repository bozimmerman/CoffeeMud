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
	private int dex=-1;
	private int dex2=-1;

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		dex=newParms.indexOf(";");
		if(dex>=0)
			dex2=newParms.indexOf(";",dex+1);
		else
			dex2=-1;
	}

	private int price()
	{
		int price=100;
		if(dex>=0)
			price=Util.s_int(getParms().substring(0,dex));
		return price;
	}

	private int minutes()
	{
		int mins=30;
		if(dex>=0)
		{
			if(dex2>dex) 
				mins = Util.s_int(getParms().substring(dex+1,dex2));
			else
				mins = Util.s_int(getParms().substring(dex + 1));
		}
		return mins;
	}

	private String zapper()
	{
		if(dex>=0)
		{
			if (dex2 > dex) 
				return getParms().substring(dex2+1);
		}
		return "";
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
		if(tickID!=Host.TICK_MOB) return true;
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

	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		MOB source=msg.source();
		if(affecting instanceof MOB)
		{
			MOB observer=(MOB)affecting;

			if(msg.amITarget(observer)
			&&(!msg.amISource(observer))
			&&(msg.targetMinor() == CMMsg.TYP_GIVE)
			&&(msg.tool() != null)
			&&(!SaucerSupport.zapperCheck(zapper(),source))
			&&(msg.tool()instanceof Coins))
			{
				ExternalPlay.quickSay(observer,null,"I wouldn't work for the likes of you.",false,false);
				return false;
			}
			
			if((observer.soulMate()==null)
			&&(msg.amISource(observer))
			&&((msg.targetMinor()==CMMsg.TYP_GIVE)||(msg.targetMinor()==CMMsg.TYP_DROP))
			&&((msg.target() instanceof Coins)||(msg.tool() instanceof Coins)))
			{
				if((msg.target() instanceof MOB)
				&&(!((MOB)msg.target()).isASysOp(source.location())))
					ExternalPlay.quickSay(observer,null,"I don't think so.",false,false);
				return false;
			}
		}
		return true;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canActAtAll(affecting)) return;
		if(!(affecting instanceof MOB)) return;

		MOB observer=(MOB)affecting;
		if((msg.sourceMinor()==CMMsg.TYP_QUIT)
		&&(msg.amISource(observer)||msg.amISource(observer.amFollowing())))
		   allDone(observer);
		else
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(!msg.amISource(observer))
		&&(!msg.source().isMonster()))
		{
			if(((msg.sourceMessage().toUpperCase().indexOf(" HIRE")>0)
				||(msg.sourceMessage().toUpperCase().indexOf("'HIRE")>0))
			&&(onTheJobUntil==0))
				ExternalPlay.quickSay(observer,null,"I'm for hire.  Just give me "+price()+" and I'll work for you.",false,false);
			else
			if(((msg.sourceMessage().toUpperCase().indexOf(" FIRED")>0))
			&&((workingFor!=null)&&(msg.source().Name().equals(workingFor)))
			&&(msg.amITarget(observer))
			&&(onTheJobUntil!=0))
			{
				ExternalPlay.quickSay(observer,msg.source(),"Suit yourself.  Goodbye.",false,false);
				allDone(observer);
			}
			else
			if(((msg.sourceMessage().toUpperCase().indexOf(" SKILLS") > 0))) 
			{
				StringBuffer skills = new StringBuffer("");
				for (int a = 0; a < observer.numAbilities(); a++) 
				{
					Ability A = observer.fetchAbility(a);
					if(A.profficiency() == 0)
						A.setProfficiency(50 + observer.envStats().level() - CMAble.lowestQualifyingLevel(A.ID()));
					skills.append(", " + A.name());
				}
				if(skills.length()>2)
					ExternalPlay.quickSay(observer, source, "My skills include: " + skills.substring(2) + ".",false,false);
			}
		}
		else
		if(msg.amITarget(observer)
		   &&(!msg.amISource(observer))
		   &&(msg.targetMinor()==CMMsg.TYP_GIVE)
		   &&(msg.tool()!=null)
		   &&(msg.tool() instanceof Coins))
		{
			int given=((Coins)msg.tool()).numberOfCoins();
			if(partials.get(msg.source().Name())!=null)
			{
				given+=((Integer)partials.get(msg.source().Name())).intValue();
				partials.remove(msg.source().Name());
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
				partials.put(msg.source().Name(),new Integer(given));
			}
			else
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						ExternalPlay.quickSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						ExternalPlay.quickSay(observer,source,"Sorry, I'm on the job right now.  Give me 1 more coin later on and I'll work.",true,false);
					partials.put(msg.source().Name(),new Integer(given));
				}
				else
				{
					if(given>price())
						partials.put(msg.source().Name(),new Integer(given-price()));
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