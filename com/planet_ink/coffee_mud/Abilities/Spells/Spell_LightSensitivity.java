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
   Copyright 2002-2025 Bo Zimmerman

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
public class Spell_LightSensitivity extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_LightSensitivity";
	}

	private final static String	localizedName	= CMLib.lang().L("Light Sensitivity");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Light Sensitivity)");

	@Override
	public String displayText()
	{
		final Physical P=this.affected;
		if((P instanceof MOB) && (isLightBlind((MOB)P)))
			return localizedStaticDisplay;
		return "";
	}

	protected final static String cancelID="Spell_DarkSensitivity";

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB))
			return;
		final Room R=((MOB)affected).location();
		if(R==null)
			return;
		if(CMLib.flags().isInDark(R))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
		else
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-50);
			affectableStats.setArmor(affectableStats.armor()+50);
		}
	}

	protected boolean isLightBlind(final MOB M)
	{
		final Room R=M.location();
		if(R==null)
			return true;
		return !CMLib.flags().isInDark(R);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_EXAMINE:
				if(isLightBlind(msg.source())
				&& (!(msg.target() instanceof Room))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					msg.source().tell(L("You can't seem to make it out that well in this bright light."));
					return false;
				}
				break;
			case CMMsg.TYP_JUSTICE:
			{
				if(!msg.targetMajor(CMMsg.MASK_DELICATE))
					return true;
			}
				//$FALL-THROUGH$
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_CAST_SPELL:
			{
				if((msg.target()!=null)
				&&(msg.target()!=msg.source())
				&&(!(msg.target() instanceof Room))
				&&(isLightBlind(msg.source()))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					if(CMLib.dice().rollPercentage()>50)
					{
						msg.source().tell(msg.source(),msg.target(),null,L("You can't seem to make out <T-NAME> in this bright light."));
						return false;
					}
				}
			}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your light sensitivity returns to normal."));
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
			{
				if(CMLib.flags().isInDark(mob.location()))
					return Ability.QUALITY_INDIFFERENT;
				if(target instanceof MOB)
				{
					if(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0)
						return Ability.QUALITY_INDIFFERENT;
					if(!CMLib.flags().canSee((MOB)target))
						return Ability.QUALITY_INDIFFERENT;
				}
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!auto)&&(target.charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(L("@x1 has no eyes, and would not be affected.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final String autoStr=L("A flashing light blazes in the eyes of <T-NAME>!");
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?autoStr:L("^SYou invoke a sensitive light into <T-NAME>s eyes.^?"),verbalCastCode(mob,target,auto),auto?autoStr:L("^S<S-NAME> invoke(s) a sensitive light into your eyes.^?"),CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,auto?autoStr:L("^S<S-NAME> invokes a sensitive light into <T-NAME>s eyes.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(CMLib.flags().isInDark(mob.location()))
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) extremely sensitive to light."));
					else
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) sensitive to the light."));
					final Ability cancelA=target.fetchEffect(cancelID);
					if(cancelA!=null)
					{
						cancelA.unInvoke();
						if(target.fetchEffect(cancelA.ID())==null)
							return true;
					}
					if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
						success=maliciousAffect(mob,target,asLevel,0,-1)!=null;
					else
						success=beneficialAffect(mob,target,asLevel,0)!=null;
				}
			}
		}
		else
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			return maliciousFizzle(mob,target,L("<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fizzles."));
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
