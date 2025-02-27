package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Prop_NarrowLedge";
	}

	@Override
	public String name()
	{
		return "The Narrow Ledge";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_EXITS;
	}

	protected int check=16;
	protected String name="the narrow ledge";
	protected String targetRoom = null;
	protected List<MOB> mobsToKill=new Vector<MOB>();

	@Override
	public String accountForYourself()
	{
		return "Very narrow";
	}

	@Override
	public void setMiscText(final String newText)
	{
		mobsToKill=new Vector<MOB>();
		super.setMiscText(newText);
		check=CMParms.getParmInt(newText,"check",16);
		name=CMParms.getParmStr(newText,"name","the narrow ledge");
		targetRoom=CMParms.getParmStr(newText, "room", null);

	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_SPELL_AFFECT)
		{
			synchronized(mobsToKill)
			{
				CMLib.threads().deleteTick(this,Tickable.TICKID_SPELL_AFFECT);
				final List<MOB> V=new XVector<MOB>(mobsToKill);
				mobsToKill.clear();
				for(int v=0;v<V.size();v++)
				{
					final MOB mob=V.get(v);
					final Room oldR=mob.location();
					if(oldR!=null)
					{
						if((affected instanceof Room)&&(oldR!=affected))
							continue;

						if((affected instanceof Room)
						&&((((Room)affected).domainType()==Room.DOMAIN_INDOORS_AIR)
						   ||(((Room)affected).domainType()==Room.DOMAIN_OUTDOORS_AIR))
						&&(((Room)affected).getRoomInDir(Directions.DOWN)!=null)
						&&(((Room)affected).getExitInDir(Directions.DOWN)!=null)
						&&(((Room)affected).getExitInDir(Directions.DOWN).isOpen()))
							oldR.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> fall(s) off @x1!!",name));
						else
						{
							if(mob.isInCombat())
							{
								CMLib.commands().postFlee(mob,("NOWHERE"));
								mob.makePeace(false);
							}
							if(targetRoom != null)
							{
								final Room R=CMLib.map().getRoom(targetRoom);
								if(R!=null)
								{
									final CMMsg enterMsg=CMClass.getMsg(mob,R,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> fall(s) in."));
									final CMMsg leaveMsg=CMClass.getMsg(mob,oldR,CMMsg.MSG_LEAVE,L("<S-NAME> fall(s) off @x1 into @x2!!",name,R.displayText(mob)));
									oldR.okMessage(mob,leaveMsg);
									R.okMessage(mob,enterMsg);
									oldR.send(mob,leaveMsg);
									((Room)enterMsg.target()).bringMobHere(mob,false);
									((Room)enterMsg.target()).send(mob,enterMsg);
									mob.tell(L("\n\r\n\r"));
									CMLib.commands().postLook(mob,true);
									continue;
								}
							}
							oldR.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> fall(s) off @x1 to <S-HIS-HER> death!!",name));
							if(!CMSecurity.isAllowed(mob,oldR,CMSecurity.SecFlag.IMMORT))
								oldR.show(mob,null,CMMsg.MSG_DEATH,null);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected))
		&&(!CMLib.flags().isFalling(msg.source())))
		{
			final MOB mob=msg.source();
			if((!CMLib.flags().isInFlight(mob))
			&&(CMLib.dice().roll(1,check,-mob.charStats().getStat(CharStats.STAT_DEXTERITY))>0))
			{
				synchronized(mobsToKill)
				{
					if(!mobsToKill.contains(mob))
					{
						mobsToKill.add(mob);
						final Ability falling=CMClass.getAbility("Falling");
						falling.setMiscText("NORMAL");
						falling.setAffectedOne(affected);
						falling.invoke(null,null,mob,true,0);
						CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,1);
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		// always disable flying restrictions!
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}
}
