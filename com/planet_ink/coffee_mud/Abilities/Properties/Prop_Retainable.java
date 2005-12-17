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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
    protected long lastMoveIn=0;
	
	public String accountForYourself()
	{ return "Retainable";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		int x=text.indexOf(";");
		if(x<0)
		{
			price=CMath.s_int(text);
			last=0;
			periodic=0;
		}
		else
		{
			price=CMath.s_int(text.substring(0,x));
			text=text.substring(x+1);
			x=text.indexOf(";");
			if(x<0)
			{
				periodic=CMath.s_int(text);
				last=0;
			}
			else
			{
				periodic=CMath.s_int(text.substring(0,x));
				last=CMath.s_long(text.substring(x+1));
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
                if((lastMoveIn>0)&&((System.currentTimeMillis()-lastMoveIn)>4))
                {
                    lastMoveIn=0;
                    if(mob.location()==lastRoom)
                    {
                        if((mob.amFollowing()!=null)&&(mob.location().isInhabitant(mob.amFollowing())))
                            CMLib.commands().say(mob,mob.amFollowing(),"Is this my new permanent post?  If so, order me to NOFOLLOW and I'll stay here.",false,false);
                        else
                        if(mob.location().numPCInhabitants()>0)
                            CMLib.commands().say(mob,mob.amFollowing(),"I guess this is my new permanent posting?",false,false);
                    }
                }
				if(periodic>0)
				{
					if(last==0) 
					{
						last=System.currentTimeMillis();
						miscText=price+";"+periodic+";"+last;
					}
					if(period<=0)
						period=((long)periodic)*((long)CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY))*MudHost.TICK_TIME;
					if((System.currentTimeMillis()>(last+period))&&(CMLib.flags().isInTheGame(mob,false)))
					{
						last=System.currentTimeMillis();
						miscText=price+";"+periodic+";"+last;
						LandTitle t=CMLib.utensils().getLandTitle(mob.location());
						String owner="";
						if(mob.amFollowing()!=null)
						{
							owner=mob.amFollowing().Name();
							if((t!=null)
							&&(t.landOwner().length()>0)
							&&(!t.landOwner().equalsIgnoreCase(mob.amFollowing().Name()))
							&&(!t.landOwner().equalsIgnoreCase(mob.amFollowing().getClanID())))
							{
								CMLib.commands().say(mob,null,"Hey, I'm not a crook!",false,false);
								mob.setFollowing(null);
								CMLib.tracking().wanderAway(mob,true,false);
								mob.destroy();
								return false;
							}
						}
						else
						if((t!=null)&&(t.landOwner().length()>0))
							owner=t.landOwner();
						
						if(owner.length()==0)
						{
							CMLib.commands().say(mob,null,"Argh! I quit!",false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,true,false);
							mob.destroy();
							return false;
						}
						boolean paid=CMLib.beanCounter().modifyLocalBankGold(mob.location().getArea(), 
						        owner, 
						        CMLib.utensils().getFormattedDate(mob)+": Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(mob,price)+": Payroll: "+Name(),
						        CMLib.beanCounter().getCurrency(mob),
						        new Integer(-price).doubleValue());
						if(paid)
							CMLib.commands().say(mob,null,"Payday!",false,false);
						else
						{
							CMLib.commands().say(mob,null,"I don't work for free!  I quit!",false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,true,false);
							mob.destroy();
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
    public void tellSkills(MOB me, MOB toMe)
    {
        StringBuffer skills = new StringBuffer("");
        if(me instanceof ShopKeeper)
            skills.append(", selling "+ShopKeeper.SOLDCODES[((ShopKeeper)me).whatIsSold()].toLowerCase());
        for (int a = 0; a < me.numAbilities(); a++)
        {
            Ability A = me.fetchAbility(a);
            if(A.profficiency() == 0)
                A.setProfficiency(50 + me.envStats().level() - CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
            skills.append(", " + A.name());
        }
        if(me instanceof ShopKeeper)
            skills.append(".  Once I'm at my permanent post, you may give me appropriate items to sell at any time");
        if(skills.length()>2)
            CMLib.commands().say(me, toMe, "My skills include: " + skills.substring(2) + ".",false,false);
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
                if((msg.tool()==affected)
                &&(msg.targetMinor()==CMMsg.TYP_BUY))
                {
                    tellSkills(mob,msg.source());
                    if(periodic>0)
                        CMLib.commands().say(mob,msg.source(),"I accept your terms of employment, and I understand I will be paid "+CMLib.beanCounter().abbreviatedPrice(mob,new Integer(price).doubleValue())+" every "+period+" days.",false,false);
                    else
                        CMLib.commands().say(mob,msg.source(),"I accept your terms of employment.",false,false);
                    CMLib.commands().say(mob,msg.source(),"Please show me the way to my permanent post.",false,false);
                }
                else
				if(mob.amFollowing()!=null)
				{
					Room room=mob.location();
					if((room!=lastRoom)
					&&(CMLib.utensils().doesHavePriviledgesHere(mob.amFollowing(),room))
					&&(room.isInhabitant(mob)))
					{
						lastRoom=room;
						mob.baseEnvStats().setRejuv(0);
						mob.setStartRoom(room);
                        lastMoveIn=System.currentTimeMillis();
					}
					if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
					||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
						mob.setFollowing(null);
				}
			}
		}
	}
}
