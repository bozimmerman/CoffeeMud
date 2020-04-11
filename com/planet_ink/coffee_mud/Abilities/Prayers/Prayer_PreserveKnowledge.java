package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.*;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Prayer_PreserveKnowledge extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_PreserveKnowledge";
	}

	private final static String localizedName = CMLib.lang().L("Preserve Knowledge");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	private final static String localizedStaticDisplay1 = CMLib.lang().L("(Preserve Knowledge)");
	private final static String localizedStaticDisplay2 = CMLib.lang().L("(Vulnerable Mind)");

	private volatile boolean vulnerableMind = false;
	private int maxXpToLose = 1100;

	@Override
	public String displayText()
	{
		if(vulnerableMind)
			return localizedStaticDisplay2;
		else
			return localizedStaticDisplay1;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public boolean canBeUninvoked()
	{
		if(!this.vulnerableMind)
			return super.canBeUninvoked();
		if(!super.canBeUninvoked())
			return false;
		if(this.tickDown<5)
			return super.canBeUninvoked();
		return false;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(this.vulnerableMind)
				mob.tell(L("Your traumatized mind fades."));
			else
				mob.tell(L("Your preservation of knowledge fades."));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				if(this.canBeUninvoked)
				{
					final Prayer_PreserveKnowledge saveA=this;
					this.canBeUninvoked=false;
					msg.addTrailerRunnable(new Runnable() {
						final Prayer_PreserveKnowledge A=saveA;
						@Override
						public void run()
						{
							A.canBeUninvoked=true;
						}
					});
				}
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
				||(msg.sourceMinor()==CMMsg.TYP_RPXPCHANGE))
			&&(!this.vulnerableMind)
			&&(msg.value()<-1))
			{
				if(this.maxXpToLose>=((-msg.value())/2))
				{
					this.maxXpToLose+=msg.value()/2;
					msg.setValue(msg.value()/2);
					if(this.maxXpToLose==0)
						unInvoke();
				}
				else
				{
					msg.setValue(msg.value()+this.maxXpToLose);
					this.maxXpToLose=0;
					this.vulnerableMind=true;
					final TimeClock C=CMLib.time().localClock(msg.source());
					if(C!=null)
						this.tickDown += C.getHoursInDay() * CMProps.getTicksPerMudHour();
					else
						this.tickDown += 24 * CMProps.getTicksPerMudHour();
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("Extreme experience loss leaves your mind traumatized!!"),-1,null,-1,null));
				}
			}
		}
		return true;
	}
	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already under the effects of this prayer."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?L("<T-NAME> attain(s) an aura of knowledge preservation."):L("^S<S-NAME> @x1 for preserved knowledge.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Prayer_PreserveKnowledge A= (Prayer_PreserveKnowledge)beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					A.maxXpToLose = 1100 + (100 * super.getXLEVELLevel(mob));
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for preserved knowledge, but go(es) unanswered.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
