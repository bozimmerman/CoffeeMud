package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_ClosedDayNight extends Property
{
	public String ID() { return "Prop_ClosedDayNight"; }
	public String name(){ return "Day/Night Visibility";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected boolean doneToday=false;
	protected int lastClosed=-1;
	protected boolean dayFlag=false;
	protected boolean sleepFlag=false;
	protected boolean sitFlag=false;
	protected boolean lockupFlag=false;
	protected int openTime=-1;
	protected int closeTime=-1;
	protected String Home=null;
	protected String shopMsg=null;
	protected Room exitRoom=null;

	public String accountForYourself()
	{ return "";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		Vector V=CMParms.parse(text);
		dayFlag=false;
		doneToday=false;
		lockupFlag=false;
		sleepFlag=false;
		sitFlag=false;
		openTime=-1;
		closeTime=-1;
		lastClosed=-1;
		Home=null;
		shopMsg=null;
		for(int v=0;v<V.size();v++)
		{
			String s=((String)V.elementAt(v)).toUpperCase();
			if(s.equals("DAY"))
				dayFlag=true;
			else
			if(s.equals("SLEEP"))
				sleepFlag=true;
			else
			if(s.equals("LOCKUP"))
				lockupFlag=true;
			else
			if(s.equals("SIT"))
				sitFlag=true;
			else
			if(s.startsWith("HOURS="))
			{
				s=s.substring(6);
				int x=s.indexOf("-");
				if(x>=0)
				{
					openTime=CMath.s_int(s.substring(0,x));
					closeTime=CMath.s_int(s.substring(x+1));
				}
			}
			else
			if(s.startsWith("HOME="))
				Home=((String)V.elementAt(v)).substring(5);
			else
			if(s.startsWith("SHOPMSG="))
				shopMsg=((String)V.elementAt(v)).substring(8);
		}
	}

	public void executeMsg(Environmental E, CMMsg msg)
	{
		super.executeMsg(E,msg);
		if(exitRoom!=null) return;
		if(msg.source().location()!=null)
			exitRoom=msg.source().location();
	}
	
	protected boolean closed(Environmental E)
	{
		boolean closed=false;
		Room R=CMLib.map().roomLocation(E);
		if(R==null) R=((exitRoom==null)?(Room)CMLib.map().rooms().nextElement():exitRoom);
		if(R==null) return false;
		if((openTime<0)&&(closeTime<0))
		{
			closed=(R.getArea().getTimeObj().getTODCode()==TimeClock.TIME_NIGHT);
			if(dayFlag) closed=!closed;
		}
		else
		{
			if(openTime<closeTime)
				closed=(R.getArea().getTimeObj().getTimeOfDay()<openTime)
					||(R.getArea().getTimeObj().getTimeOfDay()>closeTime);
			else
				closed=(R.getArea().getTimeObj().getTimeOfDay()>closeTime)
					&&(R.getArea().getTimeObj().getTimeOfDay()<openTime);
		}
		return closed;
	}

	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg))
			return false;

		if((affected!=null)
		&&(affected instanceof MOB)
		&&(closed(affected))
		&&(Home!=null)
		&&(!CMLib.flags().isSleeping(affected))
		&&((msg.targetMinor()==CMMsg.TYP_BUY)
		   ||(msg.targetMinor()==CMMsg.TYP_BID)
		   ||(msg.targetMinor()==CMMsg.TYP_SELL)
		   ||(msg.targetMinor()==CMMsg.TYP_VALUE)
		   ||(msg.targetMinor()==CMMsg.TYP_DEPOSIT)
		   ||(msg.targetMinor()==CMMsg.TYP_WITHDRAW)
		   ||(msg.targetMinor()==CMMsg.TYP_BORROW)
		   ||(msg.targetMinor()==CMMsg.TYP_VIEW)))
		{
			ShopKeeper sk=CMLib.coffeeShops().getShopKeeper(affected);
			if(sk!=null)
				CMLib.commands().postSay((MOB)affected,msg.source(),(shopMsg!=null)?shopMsg:"Sorry, I'm off right now.  Try me tomorrow.",false,false);
			return false;
		}
		return true;
	}

	protected Room getHomeRoom()
	{
		if(Home==null) return null;
		Room R=CMLib.map().getRoom(Home);
		if(R!=null) return R;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
        		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,25);
        		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
        		{
        			Room R2=CMLib.map().getRoom((Room)r.nextElement());
					if((R2.roomID().indexOf(Home)>=0)
					||CMLib.english().containsString(R2.name(),Home)
					||CMLib.english().containsString(R2.displayText(),Home)
					||CMLib.english().containsString(R2.description(),Home))
					{ R=R2; break;}
					if(R2.fetchInhabitant(Home)!=null)
					{ R=R2; break;}
				}
			}
			if(R!=null) return R;
			try
			{
		    	Vector rooms=CMLib.map().findRooms(CMLib.map().rooms(), mob, Home,false,10);
		    	if(rooms.size()>0) 
		    		R=(Room)rooms.elementAt(CMLib.dice().roll(1,rooms.size(),-1));
		    	else
		    	{
			    	Vector inhabs=CMLib.map().findInhabitants(CMLib.map().rooms(), mob, Home, 10);
			    	if(inhabs.size()>0) 
			    		R=CMLib.map().roomLocation((MOB)inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1)));
		    	}
		    }catch(NoSuchElementException e){}
		}
		return R;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(!((MOB)affected).amDead())
		&&((lastClosed<0)||(closed(affected)!=(lastClosed==1))))
		{
			MOB mob=(MOB)affected;
			if(closed(affected))
			{
				CMLib.commands().postStand(mob,true);
				if(!CMLib.flags().aliveAwakeMobile(mob,true)||(mob.isInCombat()))
					return true;

				if((mob.location()==mob.getStartRoom())
				&&(lockupFlag))
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit E=mob.location().getExitInDir(d);
						Room R2=mob.location().getRoomInDir(d);
						if((E!=null)&&(R2!=null)&&(E.hasADoor())&&(E.hasALock()))
						{
							CMMsg msg=null;
							if(E.isOpen())
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(R2.okMessage(mob,msg))
								{
									msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_CLOSE,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+E.closeWord()+"(s) <T-NAMESELF>.");
									CMLib.utensils().roomAffectFully(msg,mob.location(),d);
								}
							}
							if(!E.isLocked())
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(R2.okMessage(mob,msg))
								{
									msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
									CMLib.utensils().roomAffectFully(msg,mob.location(),d);
								}
							}
						}
					}

				if(Home!=null)
				{
					Room R=getHomeRoom();
					if((R!=null)&&(R!=mob.location()))
					{
						// still tracking...
						if(mob.fetchEffect("Skill_Track")!=null)
							return true;
						ShopKeeper sk=CMLib.coffeeShops().getShopKeeper(affected);
						if(sk!=null)
							CMLib.commands().postSay((MOB)affected,null,(shopMsg!=null)?shopMsg:"Sorry, I'm off right now.  Try me tomorrow.",false,false);
						Ability A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							A.setAbilityCode(1);
							A.invoke(mob,R,true,0);
						}
						return true;
					}
				}

				if(sleepFlag)
					mob.doCommand(CMParms.parse("SLEEP"),Command.METAFLAG_FORCED);
				else
				if(sitFlag)
					mob.doCommand(CMParms.parse("SIT"),Command.METAFLAG_FORCED);
				lastClosed=1;
			}
			else
			{
				CMLib.commands().postStand(mob,true);
				if(!CMLib.flags().aliveAwakeMobile(mob,true)||(mob.isInCombat()))
					return true;
				if(Home!=null)
				{
					if(mob.location()!=mob.getStartRoom())
					{
						// still tracking...
						if(mob.fetchEffect("Skill_Track")!=null)
							return true;
						Ability A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							A.setAbilityCode(1);
							A.invoke(mob,mob.getStartRoom(),true,0);
						}
						return true;
					}
				}
				lastClosed=0;

				if((mob.location()==mob.getStartRoom())
				&&(lockupFlag))
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit E=mob.location().getExitInDir(d);
						Room R2=mob.location().getRoomInDir(d);
						if((E!=null)&&(R2!=null)&&(E.hasADoor())&&(E.hasALock()))
						{
							CMMsg msg=null;
							if((E.isLocked())&&(!E.defaultsLocked()))
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(R2.okMessage(mob,msg))
								{
									msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
									CMLib.utensils().roomAffectFully(msg,mob.location(),d);
								}
							}
							if((!E.isOpen())&&(!E.defaultsClosed()))
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(R2.okMessage(mob,msg))
								{
									msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,"<S-NAME> "+E.openWord()+"(s) <T-NAMESELF>.");
									CMLib.utensils().roomAffectFully(msg,mob.location(),d);
								}
							}
						}
					}
			}
		}
		return true;
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected==null) return;
		if((affected instanceof MOB)
		||(affected instanceof Item))
		{
			if((closed(affected))
			&&(Home==null)
			&&(!sleepFlag)
			&&(!sitFlag)
			&&((!(affected instanceof MOB))||(!((MOB)affected).isInCombat())))
			{
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
			}
		}
		else
		if((affected instanceof Room)&&(closed(affected)))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
		else
		if(affected instanceof Exit)
		{
			if(closed(affected))
			{
				if(!doneToday)
				{
					doneToday=true;
					Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),false,e.defaultsClosed(),e.hasALock(),e.hasALock(),e.defaultsLocked());
				}
			}
			else
			{
				if(doneToday)
				{
					doneToday=false;
					Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),!e.defaultsClosed(),e.defaultsClosed(),e.hasALock(),e.defaultsLocked(),e.defaultsLocked());
				}
			}
		}

	}
}
