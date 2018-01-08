package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_FeignDeath extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FeignDeath";
	}

	private final static String localizedName = CMLib.lang().L("Feign Death");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Feign Death)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	public DeadBody Body=null;
	public Room deathRoom=null;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(canBeUninvoked())
			mob.tell(L("Your death is no longer feigned."));
		if((Body!=null)&&(deathRoom!=null)&&(deathRoom.isContent(Body)))
		{
			Body.destroy();
			deathRoom.recoverRoomStats();
		}
		super.unInvoke();
	}

	public void peaceAt(MOB mob)
	{
		final Room room=mob.location();
		if(room==null)
			return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				mob.tell(L("You are unable to attack in this semi-incorporeal form."));
				peaceAt(mob);
				return false;
			}
			else
			if((msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH)))
			{
				if(msg.sourceMajor(CMMsg.MASK_SOUND))
					mob.tell(L("You are unable to make sounds in this semi-incorporeal form."));
				else
					mob.tell(L("You are unable to do that in this semi-incorporeal form."));
				peaceAt(mob);
				return false;
			}
		}
		else
		if((msg.amITarget(mob))&&(!msg.amISource(mob))
		   &&(!msg.targetMajor(CMMsg.MASK_ALWAYS)))
		{
			msg.source().tell(L("@x1 doesn't seem to be here.",mob.name(msg.source())));
			return false;
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			return maliciousFizzle(mob,mob.location(),L("<S-NAME> point(s) to <T-NAMESELF> and yell(s), but nothing happens."));
		}

		CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) to <T-NAMESELF> and yell(s) for death!^?"));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			target.makePeace(true);
			peaceAt(target);
			deathRoom=mob.location();
			Body=(DeadBody)CMClass.getItem("Corpse");
			Body.setCharStats((CharStats)target.baseCharStats().copyOf());
			beneficialAffect(mob,target,asLevel,10);

			int tries=0;
			while((target.numFollowers()>0)&&((++tries)<1000))
			{
				final MOB follower=target.fetchFollower(0);
				if(follower!=null)
					follower.setFollowing(null);
			}
			final String msp=CMLib.protocol().msp("death"+CMLib.dice().roll(1,4,0)+".wav",50);
			msg=CMClass.getMsg(target,null,null,
					CMMsg.MSG_OK_VISUAL,L("^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r@x1",msp),
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_OK_VISUAL,L("^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r@x1",msp));
			if(deathRoom.okMessage(target,msg))
			{
				deathRoom.send(target,msg);
				Body.setName(L("the body of @x1",target.name()));
				Body.setDisplayText(L("the body of @x1 lies here.",target.name()));
				Body.basePhyStats().setWeight(target.phyStats().weight()+100);
				Body.setSecretIdentity("FAKE");
				deathRoom.addItem(Body,ItemPossessor.Expire.Monster_Body);
				Body.recoverPhyStats();
				deathRoom.recoverRoomStats();
			}
		}

		return success;
	}
}
