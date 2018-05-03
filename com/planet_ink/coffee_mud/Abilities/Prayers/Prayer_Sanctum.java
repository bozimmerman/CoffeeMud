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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prayer_Sanctum extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Sanctum";
	}

	private final static String localizedName = CMLib.lang().L("Sanctum");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Sanctum)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_WARDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	protected boolean inRoom(MOB mob, Room R)
	{
		if(!CMLib.law().doesAnyoneHavePrivilegesHere(mob, text(), R))
		{
			mob.tell(L("You feel your muscles unwilling to cooperate."));
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		final Room R=(Room)affected;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==R)
		&&(!msg.source().Name().equals(text()))
		&&(msg.source().getClanRole(text())==null)
		&&((msg.source().amFollowing()==null)
			||((!msg.source().amFollowing().Name().equals(text()))
				&&(msg.source().amFollowing().getClanRole(text())==null)))
		&&(!CMLib.law().doesHavePriviledgesHere(msg.source(),R)))
		{
			msg.source().tell(L("You feel your muscles unwilling to cooperate."));
			return false;
		}
		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					final MOB mob=(MOB)affected;
					if((CMLib.flags().isAliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().indexOf(';')>0)
						{
							final List<String> V=CMParms.parseSemicolons(text(),true);
							t=V.get(CMLib.dice().roll(1,V.size(),-1));
						}
						CMLib.commands().postSay(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().indexOf(';')>0)
					{
						final List<String> V=CMParms.parseSemicolons(text(),true);
						t=V.get(CMLib.dice().roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				final MOB victim=msg.source().getVictim();
				if(victim!=null)
					victim.makePeace(true);
				msg.source().makePeace(true);
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=mob.location();
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already a sanctum."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 to make this place a sanctum.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());

				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
				{
					final String landOwnerName=CMLib.law().getPropertyOwnerName((Room)target);
					if(CMLib.clans().getClan(landOwnerName)!=null)
					{
						setMiscText(landOwnerName);
						beneficialAffect(mob,target,asLevel,0);
					}
					else
					{
						target.addNonUninvokableEffect((Ability)this.copyOf());
						CMLib.database().DBUpdateRoom((Room)target);
					}
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 to make this place a sanctum, but <S-IS-ARE> not answered.",prayForWord(mob)));

		return success;
	}
}
