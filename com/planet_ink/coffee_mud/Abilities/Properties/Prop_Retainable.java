package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_Retainable extends Property
{
	public String ID() { return "Prop_Retainable"; }
	public String name(){ return "Ability to set Price/Retainability of a pet.";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	private Room lastRoom=null;
	
	protected long period=0;
	protected int periodic=0;
	protected int price=0;
	protected long last=0;
	
	public String accountForYourself()
	{ return "Retainable";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		int x=text.indexOf(";");
		if(x<0)
		{
			price=Util.s_int(text);
			last=0;
			periodic=0;
		}
		else
		{
			price=Util.s_int(text.substring(0,x));
			text=text.substring(x+1);
			x=text.indexOf(";");
			if(x<0)
			{
				periodic=Util.s_int(text);
				last=0;
			}
			else
			{
				periodic=Util.s_int(text.substring(0,x));
				last=Util.s_long(text.substring(x+1));
			}
		}
	}
	
	
	public void quit(MOB mob, String msg)
	{
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				if(periodic>0)
				{
					if(last==0) 
					{
						last=System.currentTimeMillis();
						miscText=price+";"+periodic+";"+last;
					}
					if(period<=0)
						period=((long)periodic)*((long)CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY))*MudHost.TICK_TIME;
					if((System.currentTimeMillis()>(last+period))&&(Sense.isInTheGame(mob)))
					{
						last=System.currentTimeMillis();
						miscText=price+";"+periodic+";"+last;
						LandTitle t=CoffeeUtensils.getLandTitle(mob.location());
						String owner="";
						if(mob.amFollowing()!=null)
						{
							owner=mob.amFollowing().Name();
							if((t!=null)
							&&(t.landOwner().length()>0)
							&&(!t.landOwner().equalsIgnoreCase(mob.amFollowing().Name()))
							&&(!t.landOwner().equalsIgnoreCase(mob.amFollowing().getClanID())))
							{
								CommonMsgs.say(mob,null,"Hey, I'm not a crook!",false,false);
								mob.setFollowing(null);
								MUDTracker.wanderAway(mob,true,false);
								mob.destroy();
								return false;
							}
						}
						else
						if((t!=null)&&(t.landOwner().length()>0))
							owner=t.landOwner();
						
						if(owner.length()==0)
						{
							CommonMsgs.say(mob,null,"Argh! I quit!",false,false);
							mob.setFollowing(null);
							MUDTracker.wanderAway(mob,true,false);
							mob.destroy();
							return false;
						}
						else
						{
							Vector V=CMClass.DBEngine().DBReadAllPlayerData(owner);
							boolean paid=false;
							for(int v=0;v<V.size();v++)
							{
								Vector D=(Vector)V.elementAt(v);
								String last=(String)D.elementAt(3);
								if(last.startsWith("COINS;"))
								{
									Item I=CMClass.getItem("StdCoins");
									CoffeeMaker.setPropertiesStr(I,last.substring(6),true);
									I.recoverEnvStats();
									I.text();
									if(((Coins)I).numberOfCoins()>=price)
									{
										((Coins)I).setNumberOfCoins(((Coins)I).numberOfCoins()-price);
										CommonMsgs.say(mob,null,"Payday!",false,false);
										CMClass.DBEngine().DBDeleteData(owner,(String)D.elementAt(1),(String)D.elementAt(2));
										CMClass.DBEngine().DBCreateData(owner,(String)D.elementAt(1),""+I+Math.random(),"COINS;"+CoffeeMaker.getPropertiesStr(I,true));
										paid=true;
										break;
									}
								}
							}
							if(!paid)
							{
								CommonMsgs.say(mob,null,"I don't work for free!  I quit!",false,false);
								mob.setFollowing(null);
								MUDTracker.wanderAway(mob,true,false);
								mob.destroy();
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				if(mob.amFollowing()!=null)
				{
					Room room=mob.location();
					if((room!=lastRoom)
					&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),room))
					&&(room.isInhabitant(mob)))
					{
						lastRoom=room;
						mob.baseEnvStats().setRejuv(0);
						mob.setStartRoom(room);
					}
					if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
					||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
						mob.setFollowing(null);
				}
			}
		}
	}
}
