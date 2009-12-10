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
		check=CMParms.getParmInt(newText,"check",16);
		name=CMParms.getParmStr(newText,"name","the narrow ledge");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_SPELL_AFFECT)
		{
			synchronized(mobsToKill)
			{
				CMLib.threads().deleteTick(this,Tickable.TICKID_SPELL_AFFECT);
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
                            if(!CMSecurity.isAllowed(mob,mob.location(),"IMMORT"))
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
		&&(!CMLib.flags().isFalling(msg.source())))
		{
			MOB mob=msg.source();
			if((!CMLib.flags().isInFlight(mob))
			&&(CMLib.dice().roll(1,check,-mob.charStats().getStat(CharStats.STAT_DEXTERITY))>0))
			{
				synchronized(mobsToKill)
				{
					if(!mobsToKill.contains(mob))
					{
						mobsToKill.addElement(mob);
						Ability falling=CMClass.getAbility("Falling");
						falling.setProficiency(0);
						falling.setAffectedOne(msg.target());
						falling.invoke(null,null,mob,true,0);
						CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,1);
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
