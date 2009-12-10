package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdFood extends StdItem implements Food
{
	public String ID(){	return "StdFood";}
	protected int amountOfNourishment=500;
	protected int nourishmentPerBite=0;
	protected long decayTime=0;

	public StdFood()
	{
		super();
		setName("a bit of food");
		baseEnvStats.setWeight(2);
		setDisplayText("a bit of food is here.");
		setDescription("Looks like some mystery meat");
		baseGoldValue=5;
		material=RawMaterial.RESOURCE_MEAT;
		recoverEnvStats();
	}



	public int nourishment()
	{
		return amountOfNourishment;
	}
	public void setNourishment(int amount)
	{
		amountOfNourishment=amount;
	}

    public int bite()
    {
        return nourishmentPerBite;
    }
    public void setBite(int amount)
    {
        nourishmentPerBite=amount;
    }

	public long decayTime(){return decayTime;}
	public void setDecayTime(long time){decayTime=time;}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((!CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS))
				||(mob.isMine(this))
				||(!CMLib.flags().isGettable(this)))
				{
	                int amountEaten=nourishmentPerBite;
	                if((amountEaten<1)||(amountEaten>amountOfNourishment))
	                    amountEaten=amountOfNourishment;
	                msg.setValue((amountEaten<amountOfNourishment)?amountEaten:0);
					return true;
				}
				mob.tell("You don't have that.");
				return false;
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				boolean hungry=mob.curState().getHunger()<=0;
			    if((!hungry)
			    &&(mob.curState().getHunger()>=mob.maxState().maxHunger(mob.baseWeight()))
				&&(CMLib.dice().roll(1,100,0)==1)
				&&(!CMLib.flags().isGolem(msg.source()))
				&&(msg.source().fetchEffect("Disease_Obesity")==null)
				&&(!CMSecurity.isDisabled("AUTODISEASE")))
				{
				    Ability A=CMClass.getAbility("Disease_Obesity");
				    if(A!=null){A.invoke(mob,mob,true,0);}
				}
			    int amountEaten=nourishmentPerBite;
			    if((amountEaten<1)||(amountEaten>amountOfNourishment))
			        amountEaten=amountOfNourishment;
			    amountOfNourishment-=amountEaten;
				boolean full=!mob.curState().adjHunger(amountEaten,mob.maxState().maxHunger(mob.baseWeight()));
				if((hungry)&&(mob.curState().getHunger()>0))
					mob.tell("You are no longer hungry.");
				else
				if(full)
					mob.tell("You are full.");
				if(amountOfNourishment<=0)
    				this.destroy();
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				break;
			default:
				break;
			}
		}
	}
}
