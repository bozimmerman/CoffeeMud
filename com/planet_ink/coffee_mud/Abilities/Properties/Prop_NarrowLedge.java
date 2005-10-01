package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Prop_NarrowLedge extends Property
{
	public String ID() { return "Prop_NarrowLedge"; }
	public String name(){ return "The Narrow Ledge";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_EXITS;}

	protected int check=16;
	protected String name="the narrow ledge";
	protected Vector mobsToKill=new Vector();

	public String accountForYourself()
	{ return "Very narrow";	}

	public void setMiscText(String newText)
	{
        mobsToKill=new Vector();
		super.setMiscText(newText);
		check=Util.getParmInt(newText,"check",16);
		name=Util.getParmStr(newText,"name","the narrow ledge");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_SPELL_AFFECT)
		{
			synchronized(mobsToKill)
			{
				CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_SPELL_AFFECT);
				Vector V=((Vector)mobsToKill.clone());
				mobsToKill.clear();
				for(int v=0;v<V.size();v++)
				{
					MOB mob=(MOB)V.elementAt(v);
					if(mob.location()!=null)
					{
						if((affected instanceof Room)&&(mob.location()!=affected))
							continue;

						if((affected instanceof Room)
						&&((((Room)affected).domainType()==Room.DOMAIN_INDOORS_AIR)
						   ||(((Room)affected).domainType()==Room.DOMAIN_OUTDOORS_AIR))
						&&(((Room)affected).getRoomInDir(Directions.DOWN)!=null)
						&&(((Room)affected).getExitInDir(Directions.DOWN)!=null)
						&&(((Room)affected).getExitInDir(Directions.DOWN).isOpen()))
							mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) off "+name+"!!");
						else
						{
							mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) off "+name+" to <S-HIS-HER> death!!");
							mob.location().show(mob,null,CMMsg.MSG_DEATH,null);
						}
					}
				}
			}
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected))
		&&(!Sense.isFalling(msg.source())))
		{
			MOB mob=msg.source();
			if((!Sense.isInFlight(mob))
			&&(Dice.roll(1,check,-mob.charStats().getStat(CharStats.DEXTERITY))>0))
			{
				synchronized(mobsToKill)
				{
					if(!mobsToKill.contains(mob))
					{
						mobsToKill.addElement(mob);
						Ability falling=CMClass.getAbility("Falling");
						falling.setProfficiency(0);
						falling.setAffectedOne(msg.target());
						falling.invoke(null,null,mob,true,0);
						CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_SPELL_AFFECT,1);
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		// always disable flying restrictions!
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}
}
