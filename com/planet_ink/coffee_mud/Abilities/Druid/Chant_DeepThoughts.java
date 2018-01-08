package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Chant_DeepThoughts extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_DeepThoughts";
	}

	private final static String localizedName = CMLib.lang().L("Deep Thoughts");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Thinking deep thoughts)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}
	protected long[] lastTime=new long[]{ 0 };

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> end(s) <S-HIS-HER> deep thinking."));
				else
					mob.tell(L("You stop thinking deeply."));
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.tool()!=this)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_EYES))))
			unInvoke();
		return;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		final MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!mob.isInCombat())
		{
			if((mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
			&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
			{
				unInvoke();
				return false;
			}
			if((System.currentTimeMillis()-lastTime[0])<60000)
				return true;
			if(!proficiencyCheck(null,0,false))
				return true;
			lastTime[0]=System.currentTimeMillis();
			final Room room=mob.location();
			final int myAlignment=mob.fetchFaction(CMLib.factions().AlignID());
			final int total=CMLib.factions().getTotal(CMLib.factions().AlignID());
			final int ratePct=(int)Math.round(CMath.mul(total,.01));
			if(CMLib.factions().getAlignPurity(myAlignment,Faction.Align.INDIFF)<99)
			{
				if(CMLib.factions().getAlignPurity(myAlignment,Faction.Align.EVIL)<CMLib.factions().getAlignPurity(myAlignment,Faction.Align.GOOD))
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), ratePct);
				else
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), -ratePct);
				switch(CMLib.dice().roll(1,10,0))
				{
				case 0: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> contemplate(s) the great depths.")); break;
				case 1: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> listen(s) to the wisdom of the stones.")); break;
				case 2: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> stare(s) at a single stone.")); break;
				case 3: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> mentally embrace(s) the earth.")); break;
				case 4: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> inhale(s) wisdom and dust.")); break;
				case 5: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> watch(es) the unchanging stones and feel(s) their history.")); break;
				case 6: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> become(s) one with the earth.")); break;
				case 7: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> feel(s) the inner warmth of the great deep.")); break;
				case 8: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> expunge(s) <S-HIS-HER> unnatural thoughts.")); break;
				case 9: room.show(mob,null,this,CMMsg.MSG_CONTEMPLATE,L("<S-NAME> find(s) clarity in the stones.")); break;
				}
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("You can't think deeply while in combat!"));
			return false;
		}
		if((mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("You must be in a cave for this chant to work."));
			return false;
		}

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),L("^S<S-NAME> grow(s) very still and begin(s) to think deep thoughts...^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_DeepThoughts B = (Chant_DeepThoughts)beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				if(B!=null)
					B.lastTime = this.lastTime;
				helpProficiency(mob, 0);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> grow(s) very still, but lose(s) concentration."));

		// return whether it worked
		return success;
	}
}
