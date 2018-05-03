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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_GustOfWind extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_GustOfWind";
	}

	private final static String localizedName = CMLib.lang().L("Gust of Wind");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Blown Down)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(4);
	}

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

	public boolean doneTicking=false;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> regain(s) <S-HIS-HER> feet."));
				CMLib.commands().postStand(mob,true);
			}
			else
				mob.tell(L("You regain your feet."));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room R=CMLib.map().roomLocation(givenTarget);
		if(R==null)
			R=mob.location();
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell(L("There doesn't appear to be anyone here worth blowing around."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(R.show(mob,null,this,verbalCastCode(mob,null,auto),auto?L("A horrendous wind gust blows through here."):L("^S<S-NAME> blow(s) at <S-HIS-HER> enemies.^?")))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("<T-NAME> get(s) blown back!"));
					if((R.okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						if((msg.value()<=0)&&(target.location()==R))
						{
							MOB victim=target.getVictim();
							if((victim!=null)&&(target.rangeToTarget()>=0))
								target.setRangeToTarget(target.rangeToTarget()+1+(adjustedLevel(mob,asLevel)/10));
							if(target.rangeToTarget()>target.location().maxRange())
								target.setRangeToTarget(target.location().maxRange());

							R.send(mob,msg);
							if((!CMLib.flags().isInFlight(target))
							&&(CMLib.dice().rollPercentage()>((target.charStats().getStat(CharStats.STAT_DEXTERITY)*2)+target.phyStats().level()-(adjustedLevel(mob,asLevel)/2)))
							&&(target.charStats().getBodyPart(Race.BODY_LEG)>0))
							{
								R.show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> fall(s) down!"));
								doneTicking=false;
								success=maliciousAffect(mob,target,asLevel,2,-1)!=null;
							}
							victim=target.getVictim();
							if(victim!=null)
								victim.setRangeToTarget(target.rangeToTarget());
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> blow(s), but find(s) <S-HE-SHE> is only full of hot air."));

		// return whether it worked
		return success;
	}
}
