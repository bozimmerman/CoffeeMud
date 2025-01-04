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
   Copyright 2024-2025 Bo Zimmerman

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
public class Chant_CurseMood extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CurseMood";
	}

	private final static String localizedName = CMLib.lang().L("Curse Mood");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Cursed Mood)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_CURSING;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your mood curse is lifted."));

		super.unInvoke();

	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==affected)
		&&(msg.tool() instanceof Ability)
		&&(((msg.tool()).ID().equalsIgnoreCase("Mood")))
		&&(affected instanceof MOB)
		&&(super.proficiencyCheck((MOB)affected, 0, false)))
		{
			msg.source().tell(msg.source(),msg.target(),null,L("<S-YOUPOSS> curse prevents mood changes."));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String moodName="random";
		final Ability moodA = CMClass.getAbility("Mood");
		if(moodA != null)
		{
			if(commands.size()>1)
			{
				final String s = commands.get(commands.size()-1);
				if(s.equalsIgnoreCase("normal"))
				{
					commands.remove(commands.size()-1);
					moodName = s.toUpperCase().trim();
				}
				else
				{
					moodA.setMiscText(s.toUpperCase().trim());
					if((moodA.text().length()>0) )
					{
						commands.remove(commands.size()-1);
						moodName=moodA.text().toUpperCase().trim();
					}
				}
			}
			else
			{
				moodA.setMiscText("RANDOM");
				moodName = moodA.text();
			}
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> mood cursed."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Room R = target.location();
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) a moody curse at <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((R.okMessage(mob,msg))&&((R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				R.send(mob,msg2);
				if((msg.value()<=0) && (msg2.value()<=0))
				{
					final Ability A=CMClass.getAbility("Mood");
					if(A != null)
					{
						final Vector<String> V=new XVector<String>(moodName);
						A.invoke(target,V,target,true,0);
					}
					final long ticks = (CMLib.time().homeClock(mob).getHoursInDay()*CMProps.getTicksPerMudHour())
							+ (CMProps.getTicksPerMudHour()*adjustedLevel(mob,asLevel));
					maliciousAffect(mob,target,asLevel,(int)ticks, -1);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for a moody curse at <T-NAMESELF>, but fail(s)."));

		return success;
	}
}
