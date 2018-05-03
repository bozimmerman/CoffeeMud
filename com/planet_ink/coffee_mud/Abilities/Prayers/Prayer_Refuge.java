package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_Refuge extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Refuge";
	}

	private final static String	localizedName	= CMLib.lang().L("Refuge");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_RESTORATION;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 25;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL | Ability.FLAG_TRANSPORTING;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SELL))
		{
			unInvoke();
			if(affected!=null)
				affected.delEffect(this);
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
	}

	public Room getRefuge(Item I)
	{
		Room R=CMLib.map().getRoom(text());
		if(R==null)
			R=CMLib.map().getRandomRoom();
		return R;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected instanceof Item)&&(text().length()>0))
		{
			final Item I=(Item)affected;
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.target()==I)
			&&((msg.source()==I.owner())||(I.owner() instanceof Room))
			&&(msg.sourceMessage()!=null)
			&&(CMLib.english().containsString(CMStrings.getSayFromMessage(msg.sourceMessage()).toUpperCase(), "REFUGE")))
			{
				final Room newRoom=this.getRefuge(I);
				if((newRoom!=null)&&(newRoom!=msg.source().location()))
				{
					final Set<MOB> h=properTargets(msg.source(),null,false);
					if(h==null)
						return;
					final Room thisRoom=msg.source().location();
					final Ability thisA=this;
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							for (final Object element : h)
							{
								final MOB follower=(MOB)element;
								final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,thisA,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> appears in a puff of smoke.@x1",CMLib.protocol().msp("appear.wav",10)));
								final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,thisA,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a puff of smoke."));
								if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
								{
									if(follower.isInCombat())
									{
										CMLib.commands().postFlee(follower,("NOWHERE"));
										follower.makePeace(true);
									}
									thisRoom.send(follower,leaveMsg);
									newRoom.bringMobHere(follower,false);
									newRoom.send(follower,enterMsg);
									follower.tell(L("\n\r\n\r"));
									CMLib.commands().postLook(follower,true);
								}
							}
						}
					});
					unInvoke();
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		final Room room = CMLib.map().roomLocation(mob);
		if((room == null)||(room.getArea()==null))
			return false;
		final String roomID=CMLib.map().getExtendedRoomID(room);
		if((CMath.bset(room.getArea().flags(),Area.FLAG_INSTANCE_CHILD))||(roomID.length()==0))
		{
			mob.tell(L("The magic in this place will not permit it to become a refuge."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) at <T-NAMESELF> and @x1.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glows slightly!"));
				mob.tell(L("@x1 will now await someone to 'SAYTO \"@x1\" Refuge' to it before teleporting you back here.",target.name(mob)));
				final Ability A=beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
				if(A!=null)
					A.setMiscText(roomID);
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, but fail(s) to properly pray."));
		// return whether it worked
		return success;
	}
}
