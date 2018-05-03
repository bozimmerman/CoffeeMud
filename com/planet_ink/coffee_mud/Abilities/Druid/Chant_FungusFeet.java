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

public class Chant_FungusFeet extends Chant implements DiseaseAffect
{
	@Override
	public String ID()
	{
		return "Chant_FungusFeet";
	}

	private final static String localizedName = CMLib.lang().L("Fungus Feet");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Fungus Feet)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 4;
	}

	@Override
	public boolean isMalicious()
	{
		return true;
	}
	int plagueDown=8;
	double drawups=1.0;

	@Override
	public String getHealthConditionDesc()
	{
		return "A rotting foot condition.";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if((--plagueDown)<=0)
		{
			final MOB mob=(MOB)affected;
			plagueDown=10;
			if(invoker==null)
				invoker=mob;
			drawups+=.1;
			if(drawups>=3.1)
			{
				if((mob.location()!=null)&&(CMLib.flags().isInTheGame(mob,false)))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOU-POSS> feet rot off!"));
					final Ability A=CMClass.getAbility("Amputation");
					if(A!=null)
					{
						int x=100;
						while(((--x)>0)&&A.invoke(mob,CMParms.parse("foot"),mob,true,0)){/*do nothing */}
						mob.recoverCharStats();
						mob.recoverPhyStats();
						mob.recoverMaxState();
					}
					unInvoke();
				}
			}
			else
			{
				final MOB invoker=(invoker()!=null) ? invoker() : mob;
				CMLib.combat().postDamage(invoker,mob,this,1,CMMsg.TYP_DISEASE,-1,L("<T-NAME> feel(s) the fungus between <T-HIS-HER> toes eating <T-HIS-HER> feet away!"));
			}
		}
		return true;
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null)
			return;
		affectableState.setMovement((int)Math.round(CMath.div(affectableState.getMovement(),drawups+(0.1*super.getX1Level(invoker())))));
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
		{
			if((mob.location()!=null)&&(!mob.amDead())&&(mob.getWearPositions(Wearable.WORN_FEET)>0))
			{
				spreadImmunity(mob);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("The fungus on <S-YOUPOSS> feet dies and falls off."));
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(((MOB)target).charStats().getBodyPart(Race.BODY_FOOT)==0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.charStats().getBodyPart(Race.BODY_FOOT)==0)
		{
			mob.tell(L("@x1 has no feet!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":L("^S<S-NAME> chant(s) at <T-YOUPOSS> feet!^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_DISEASE,null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					invoker=mob;
					maliciousAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("A fungus sprouts up between <S-YOUPOSS> toes!"));
				}
				else
					spreadImmunity(target);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-YOUPOSS> feet, but nothing happens."));

		// return whether it worked
		return success;
	}
}
