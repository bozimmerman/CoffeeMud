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

public class Hireling extends StdBehavior
{
	@Override
	public String ID()
	{
		return "Hireling";
	}

	protected Hashtable<String,Double> partials=new Hashtable<String,Double>();
	protected String workingFor="";
	protected long onTheJobUntil=0;
	protected double price=100.0;
	protected int minutes=30;
	protected String zapperMask=null;

	@Override
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

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		final int dex=newParms.indexOf(';');
		zapperMask=null;
		if(dex>=0)
		{
			final int dex2=newParms.indexOf(';',dex+1);
			if(dex2>dex)
			{
				setPrice(newParms.substring(0,dex));
				setMinutes(newParms.substring(dex+1,dex2));
				final String s=getParms().substring(dex2+1);
				if(s.trim().length()>0)
					zapperMask=s.trim();
			}
			else
			{
				setPrice(newParms.substring(0,dex));
				setMinutes(newParms.substring(dex+1));
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
		final double d=CMath.div((minutes() * 60L * 1000L),CMProps.getMillisPerMudHour());
		final long d2=Math.round(d*10.0);
		return CMath.div(d2,10.0);
	}

	public void allDone(MOB observer)
	{
		workingFor="";
		onTheJobUntil=0;
		CMLib.commands().postFollow(observer,null,false);
		observer.setFollowing(null);
		int direction=-1;
		for(final int dir : Directions.CODES())
		{
			if(observer.location().getExitInDir(dir)!=null)
			{
				if(observer.location().getExitInDir(dir).isOpen())
				{
					direction=dir;
					break;
				}
				direction=dir;
			}
		}
		if(direction>=0)
			CMLib.tracking().walk(observer,direction,false,false);
		if(observer.getStartRoom()!=null)
			observer.getStartRoom().bringMobHere(observer,false);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(onTheJobUntil==0)
			return true;
		final MOB observer=(MOB)ticking;
		if(System.currentTimeMillis()>onTheJobUntil)
		{
			final Double D=partials.get(workingFor);
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
					CMLib.commands().postSay(observer,talkTo,L("Your time is up.  Goodbye!"),true,false);
				allDone(observer);
			}
			else
			{
				if(talkTo!=null)
					CMLib.commands().postSay(observer,talkTo,L("Your base time is up, but you've paid for @x1 more minutes, so I'll hang around.",""+additional),true,false);
				onTheJobUntil+=(additional*TimeManager.MILI_MINUTE);
			}
		}
		else
		if((workingFor.length()>0)
		   &&(observer.location()!=null)
		   &&(observer.amFollowing()==null)
		   &&(canFreelyBehaveNormal(observer)))
		{
			final MOB talkTo=observer.location().fetchInhabitant(workingFor);
			if(talkTo!=null)
			{
				CMLib.commands().postFollow(observer,talkTo,false);
				observer.setFollowing(talkTo);
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		final MOB source=msg.source();
		if(affecting instanceof MOB)
		{
			final MOB observer=(MOB)affecting;

			if(msg.amITarget(observer)
			&&(!msg.amISource(observer))
			&&(msg.targetMinor() == CMMsg.TYP_GIVE)
			&&(msg.tool() instanceof Coins))
			{
				if(!CMLib.masking().maskCheck(zapper(),source,false))
				{
					CMLib.commands().postSay(observer,null,L("I wouldn't work for the likes of you."),false,false);
					return false;
				}
				if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(observer)))
				{
					CMLib.commands().postSay(observer,null,L("I'm sorry, I only deal in @x1.",CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(observer))),false,false);
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
					CMLib.commands().postSay(observer,null,L("I don't think so."),false,false);
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		final MOB source=msg.source();
		if(!canActAtAll(affecting))
			return;
		if(!(affecting instanceof MOB))
			return;

		final MOB observer=(MOB)affecting;
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
				CMLib.commands().postSay(observer,null,L("I'm for hire.  Just give me @x1 and I'll work for you for @x2 \"hours\".",CMLib.beanCounter().nameCurrencyShort(observer,price()),""+gamehours()),false,false);
			else
			if(((upperSrcMsg.indexOf(" FIRED")>0))
			&&((workingFor!=null)&&(msg.source().Name().equals(workingFor)))
			&&(msg.amITarget(observer))
			&&(onTheJobUntil!=0))
			{
				CMLib.commands().postSay(observer,msg.source(),L("Suit yourself.  Goodbye."),false,false);
				allDone(observer);
			}
			else
			if(((upperSrcMsg.indexOf(" SKILLS") > 0)))
			{
				final StringBuffer skills = new StringBuffer("");
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
					CMLib.commands().postSay(observer, source, L("My skills include: @x1.",skills.substring(2)),false,false);
			}
		}
		else
		if(msg.amITarget(observer)
		&&(!msg.amISource(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool() instanceof Coins))
		{
			double given=((Coins)msg.tool()).getTotalValue();
			if(partials.get(msg.source().Name())!=null)
			{
				given+=partials.get(msg.source().Name()).doubleValue();
				partials.remove(msg.source().Name());
			}
			if(given<price())
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						CMLib.commands().postSay(observer,source,L("I'm still working for you.  I'll put that towards an extension though."),true,false);
					else
						CMLib.commands().postSay(observer,source,L("Sorry, I'm on the job right now.  Give me @x1 more later on and I'll work for @x2 \"hours\".",CMLib.beanCounter().nameCurrencyShort(observer,(price()-given)),""+gamehours()),true,false);
				}
				else
					CMLib.commands().postSay(observer,source,L("My price is @x1.  Give me @x2 more and I'll work for you for @x3 \"hours\".",CMLib.beanCounter().nameCurrencyShort(observer,price()),CMLib.beanCounter().nameCurrencyShort(observer,(price()-given)),""+gamehours()),true,false);
				partials.put(msg.source().Name(),Double.valueOf(given));
			}
			else
			{
				if(onTheJobUntil!=0)
				{
					if(workingFor.equals(source.Name()))
						CMLib.commands().postSay(observer,source,L("I'm still working for you.  I'll put that towards an extension though."),true,false);
					else
						CMLib.commands().postSay(observer,source,L("Sorry, I'm on the job right now.  Give me 1 more coin later on and I'll work."),true,false);
					partials.put(msg.source().Name(),Double.valueOf(given));
				}
				else
				{
					if(given>price())
						partials.put(msg.source().Name(),Double.valueOf(given-price()));
					final StringBuffer skills=new StringBuffer("");
					for(final Enumeration<Ability> a=observer.allAbilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
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
					CMLib.commands().postSay(observer,source,L("Ok.  You've got me for at least @x1 \"hours\".  My skills include: @x2.  I'll follow you.  Just ORDER me to do what you want.",""+gamehours(),skills.substring(2)),true,false);
				}
			}
		}
	}
}
