package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Hireling extends StdBehavior
{
	public String ID(){return "Hireling";}

	protected Hashtable partials=new Hashtable();
	protected String workingFor="";
	protected long onTheJobUntil=0;
	protected double price=100.0;
	protected int minutes=30;
	protected String zapperMask=null;

	public String accountForYourself()
	{ 
		return "availability for hiring";
	}

	public void setPrice(String s)
	{
		price=100.0;
		if(CMath.isNumber(s))
		{
			if(CMath.isDouble(s))
				price=CMath.s_double(s);
			else
				price=CMath.s_long(s);
		}
	}
	
	public void setMinutes(String s)
	{
		minutes=30;
		if(CMath.isNumber(s))
		{
			if(CMath.isDouble(s))
				minutes=(int)Math.round(CMath.s_double(s));
			else
				minutes=(int)CMath.s_long(s);
		}
	}
	
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		int dex=newParms.indexOf(';');
		zapperMask=null;
		if(dex>=0)
		{
			int dex2=newParms.indexOf(';',dex+1);
			if(dex2>dex)
			{
				setPrice(newParms.substring(0,dex));
				setMinutes(newParms.substring(dex+1,dex2));
				String s=getParms().substring(dex2+1);
				if(s.trim().length()>0)
					zapperMask=s.trim();
			}
			else
			{
				setPrice(newParms.substring(0,dex));
				setMinutes(newParms.substring(dex));
			}
		}
		else
		{
			setPrice(newParms);
			setMinutes("30");
		}
	}

	protected double price() { return price;}

	protected int minutes() { return minutes;}

	protected String zapper() { return zapperMask;}

	protected double gamehours()
	{
		double d=CMath.div((minutes() * 60L * 1000L),CMProps.getMillisPerMudHour());
		long d2=Math.round(d*10.0);
		return CMath.div(d2,10.0);
	}

	public void allDone(MOB observer)
	{
		workingFor="";
		onTheJobUntil=0;
		CMLib.commands().postFollow(observer,null,false);
		observer.setFollowing(null);
		int direction=-1;
		for(int d=0;d<Directions.DIRECTIONS_BASE().length;d++)
			if(observer.location().getExitInDir(Directions.DIRECTIONS_BASE()[d])!=null)
			{
				if(observer.location().getExitInDir(Directions.DIRECTIONS_BASE()[d]).isOpen())
				{
					direction=Directions.DIRECTIONS_BASE()[d];
					break;
				}
				direction=Directions.DIRECTIONS_BASE()[d];
			}
		if(direction>=0)
			CMLib.tracking().walk(observer,direction,false,false);
		if(observer.getStartRoom()!=null)
			observer.getStartRoom().bringMobHere(observer,false);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB) return true;
		if(onTheJobUntil==0) return true;
		MOB observer=(MOB)ticking;
		if(System.currentTimeMillis()>onTheJobUntil)
		{
			Double D=(Double)partials.get(workingFor);
			partials.remove(workingFor);
			CMLib.commands().postStand(observer,true);
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
			if(D!=null)
				additional+=(int)Math.round(CMath.mul(CMath.div(minutes(),price()),D.doubleValue()));
			if(additional<=0)
			{
				if(talkTo!=null)
					CMLib.commands().postSay(observer,talkTo,"Your time is up.  Goodbye!",true,false);
				allDone(observer);
			}
			else
			{
				if(talkTo!=null)
					CMLib.commands().postSay(observer,talkTo,"Your base time is up, but you've paid for "+additional+" more minutes, so I'll hang around.",true,false);
				onTheJobUntil+=(additional*TimeManager.MILI_MINUTE);
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
				CMLib.commands().postFollow(observer,talkTo,false);
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
			&&(msg.tool() instanceof Coins))
			{
				if(!CMLib.masking().maskCheck(zapper(),source,false))
				{
					CMLib.commands().postSay(observer,null,"I wouldn't work for the likes of you.",false,false);
					return false;
				}
				if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(observer)))
				{
					CMLib.commands().postSay(observer,null,"I'm sorry, I only deal in "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(observer))+".",false,false);
					return false;
				}
			}

			if((observer.soulMate()==null)
			&&(msg.amISource(observer))
			&&((msg.targetMinor()==CMMsg.TYP_GIVE)||(msg.targetMinor()==CMMsg.TYP_DROP))
			&&((msg.target() instanceof Coins)||(msg.tool() instanceof Coins)))
			{
				if((msg.target() instanceof MOB)
				&&(!CMSecurity.isAllowed(((MOB)msg.target()),source.location(),CMSecurity.SecFlag.CMDROOMS)))
					CMLib.commands().postSay(observer,null,"I don't think so.",false,false);
				return false;
			}
		}
		return true;
	}

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
			final String upperSrcMsg=msg.sourceMessage() == null ? "" : msg.sourceMessage().toUpperCase(); 
			if(((upperSrcMsg.indexOf(" HIRE")>0)
				||(upperSrcMsg.indexOf("'HIRE")>0)
				||(upperSrcMsg.indexOf("WORK")>0)
				||(upperSrcMsg.indexOf("AVAILABLE")>0))
			&&(onTheJobUntil==0))
				CMLib.commands().postSay(observer,null,"I'm for hire.  Just give me "+CMLib.beanCounter().nameCurrencyShort(observer,price())+" and I'll work for you for "+gamehours()+" \"hours\".",false,false);
			else
			if(((upperSrcMsg.indexOf(" FIRED")>0))
			&&((workingFor!=null)&&(msg.source().Name().equals(workingFor)))
			&&(msg.amITarget(observer))
			&&(onTheJobUntil!=0))
			{
				CMLib.commands().postSay(observer,msg.source(),"Suit yourself.  Goodbye.",false,false);
				allDone(observer);
			}
			else
			if(((upperSrcMsg.indexOf(" SKILLS") > 0)))
			{
				StringBuffer skills = new StringBuffer("");
				for(final Enumeration<Ability> a=observer.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(A!=null)
					{
						if(A.proficiency() == 0)
							A.setProficiency(50 + observer.phyStats().level() - CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
						skills.append(", " + A.name());
					}
				}
				if(skills.length()>2)
					CMLib.commands().postSay(observer, source, "My skills include: " + skills.substring(2) + ".",false,false);
			}
		}
		else
		if(msg.amITarget(observer)
		&&(!msg.amISource(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Coins))
		{
			double given=((Coins)msg.tool()).getTotalValue();
			if(partials.get(msg.source().Name())!=null)
			{
				given+=((Double)partials.get(msg.source().Name())).doubleValue();
				partials.remove(msg.source().Name());
			}
			if(given<price())
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						CMLib.commands().postSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						CMLib.commands().postSay(observer,source,"Sorry, I'm on the job right now.  Give me "+CMLib.beanCounter().nameCurrencyShort(observer,(price()-given))+" more later on and I'll work for "+gamehours()+" \"hours\".",true,false);
				}
				else
					CMLib.commands().postSay(observer,source,"My price is "+CMLib.beanCounter().nameCurrencyShort(observer,price())+".  Give me "+CMLib.beanCounter().nameCurrencyShort(observer,(price()-given))+" more and I'll work for you for "+gamehours()+" \"hours\".",true,false);
				partials.put(msg.source().Name(),Double.valueOf(given));
			}
			else
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						CMLib.commands().postSay(observer,source,"I'm still working for you.  I'll put that towards an extension though.",true,false);
					else
						CMLib.commands().postSay(observer,source,"Sorry, I'm on the job right now.  Give me 1 more coin later on and I'll work.",true,false);
					partials.put(msg.source().Name(),Double.valueOf(given));
				}
				else
				{
					if(given>price())
						partials.put(msg.source().Name(),Double.valueOf(given-price()));
					StringBuffer skills=new StringBuffer("");
					for(Enumeration<Ability> a=observer.allAbilities();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						if(A!=null)
						{
							if(A.proficiency()==0)
								A.setProficiency(50+observer.phyStats().level()-CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
							skills.append(", "+A.name());
						}
					}
					workingFor=source.Name();
					onTheJobUntil=System.currentTimeMillis();
					onTheJobUntil+=(minutes()*TimeManager.MILI_MINUTE);
					CMLib.commands().postFollow(observer,source,false);
					observer.setFollowing(source);
					CMLib.commands().postSay(observer,source,"Ok.  You've got me for at least "+gamehours()+" \"hours\".  My skills include: "+skills.substring(2)+".  I'll follow you.  Just ORDER me to do what you want.",true,false);
				}
			}
		}
	}
}
