package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Fighter_Breakout extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Breakout";
	}

	private final static String localizedName = CMLib.lang().L("Breakout");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Breaking your bonds)");

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
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_GRAPPLING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"BREAKOUT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(!CMLib.flags().isAliveAwakeMobile(mob,true))
			{
				unInvoke();
				return false;
			}
			final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING);
			if(V.size()==0)
			{
				unInvoke();
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HANDS,L("<S-NAME> struggle(s) and fight(s) against <S-HIS-HER> bonds."));
			strBonus=getXLEVELLevel(mob)+(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)*2);
			mob.recoverCharStats();
			try
			{
				for(int v=0;v<V.size();v++)
				{
					final Ability A=V.get(v);
					if(A.okMessage(mob,msg))
						A.executeMsg(mob,msg);
				}
			}
			finally
			{
				strBonus = 0;
				mob.recoverCharStats();
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)
		&&(!M.amDead())
		&&(super.canBeUninvoked()))
		{
			if(CMLib.flags().flaggedAffects(M,Ability.FLAG_BINDING).size()==0)
				M.tell(L("You break free of your bonds."));
			else
				M.tell(L("You stop trying to break free of your bonds."));
		}
	}

	protected volatile int strBonus = 0;

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats charStats)
	{
		charStats.setStat(CharStats.STAT_STRENGTH, charStats.getStat(CharStats.STAT_STRENGTH)+strBonus);
		charStats.setStat(CharStats.STAT_DEXTERITY, charStats.getStat(CharStats.STAT_DEXTERITY)+strBonus);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.fetchEffect(this.ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isAliveAwakeMobile(mob,true))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING).size()==0)
				return Ability.QUALITY_INDIFFERENT;
			return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already trying to break out of <S-HIS-HER> bonds."));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobile(mob,true))
		{
			failureTell(mob,target,auto,L("<T-NAME> <T-IS-ARE> not bound!"));
			return false;
		}
		if(CMLib.flags().flaggedAffects(mob,Ability.FLAG_BINDING).size()==0)
		{
			failureTell(mob,target,auto,L("<T-NAME> <T-IS-ARE> not bound by anything which can be broken out of."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DELICATE_HANDS_ACT,
				auto?L("<T-NAME> start(s) breaking out of <T-HIS-HER> bonds."):
					L("<S-NAME> attempt(s) to break out of <S-HIS-HER> bonds."));
		if(!success)
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to break out of <S-HIS-HER> bonds, but <S-IS-ARE> a failure."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
