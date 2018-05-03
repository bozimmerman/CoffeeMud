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

public class Prayer_JoyousRapture extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_JoyousRapture";
	}

	private final static String localizedName = CMLib.lang().L("Joyous Rapture");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Joyous Rapture)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}
	
	private final static Map<String,String> moodMap = CMParms.parseEQParms(
			"FORMAL=HAPPY POLITE=HAPPY HAPPY=HAPPY SAD=NORMAL ANGRY=NORMAL RUDE=NORMAL "
			+ "MEAN=NORMAL GRUMPY=NORMAL EXCITED=HAPPY SCARED=HAPPY LONELY=HAPPY");
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your joyous rapture is lifted."));
		super.unInvoke();
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected != null)
		{
			Ability moodA=affected.fetchEffect("Mood");
			if(moodA!=null)
			{
				if(text().equals("NORMAL"))
					affected.delEffect(moodA);
				else
				if(!moodA.text().equals(text()))
					moodA.setMiscText(text());
			}
			else
			if(!text().equals("NORMAL"))
			{
				moodA=CMClass.findAbility("Mood");
				if((moodA!=null)&&(affected instanceof MOB))
				{
					final MOB mob=(MOB)affected;
					final Vector<String> V=new XVector<String>(text());
					moodA.invoke(mob,V,mob,true,0);
				}
			}
			if(affected instanceof MOB)
			{
				((MOB)affected).makePeace(true);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> <T-IS-ARE> in joyous rapture!"):L("^S<S-NAME> @x1 for <T-NAMESELF> to be in joyous rapture.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Ability A=beneficialAffect(mob,target,asLevel,0);
					success=A!=null;
					if(A!=null)
					{
						Ability moodA=target.fetchEffect("Mood");
						if(moodA==null)
							A.setMiscText("HAPPY");
						else
						{
							String newStr=moodMap.get(moodA.text());
							if(newStr==null)
								newStr="HAPPY";
							A.setMiscText(newStr);
						}
					}
					target.recoverPhyStats();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for <T-NAMESELF>, but nothing happens.",prayForWord(mob)));

		// return whether it worked
		return success;
	}
}
