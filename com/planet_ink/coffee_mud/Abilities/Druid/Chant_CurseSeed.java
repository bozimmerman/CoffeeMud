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
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_CurseSeed extends Chant implements DiseaseAffect
{
	@Override
	public String ID()
	{
		return "Chant_CurseSeed";
	}

	private final static String localizedName = CMLib.lang().L("Curse Seed");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Curse Seed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_CURSING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int difficultyLevel()
	{
		return 100;
	}

	@Override
	public boolean isMalicious()
	{
		return false;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Suffering the effects of " + name();
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
			mob.tell(L("Your cursed seed subsides."));
	}

	protected Set<Ability> done = new LimitedTreeSet<Ability>(TimeManager.MILI_YEAR, 100, false);

	protected void pollute(final MOB mob)
	{
		final Ability pregA=mob.fetchEffect("Pregnancy");
		if((pregA != null)
		&&(!done.contains(pregA)))
		{
			done.add(pregA);
			char gender;
			if((text().length()==0)
			||("MNF".indexOf(text().toUpperCase().charAt(0))<0))
				gender=(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
			else
				gender=text().toUpperCase().charAt(0);
			pregA.setStat("BABYGENDER", ""+gender);
		}
	}

	@Override
	public CMObject copyOf()
	{
		// assume an inheretance is going on right now
		final Chant_CurseSeed seedA = (Chant_CurseSeed)super.copyOf();
		if(seedA != null)
		{
			if(seedA.tickDown>1)
				seedA.tickDown=1;
			if((affected instanceof MOB)
			&&(this.miscText!=null))
			{
				final int x = miscText.indexOf('/');
				if(x>0)
				{
					int count = 0;
					if(x>0)
					{
						count=CMath.s_int(text().substring(x))-1;
						if(count>=0)
						{
							final MOB mob=(MOB)affected;
							char gender;
							if((text().length()==0)
							||("MNF".indexOf(text().toUpperCase().charAt(0))<0))
								gender=(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
							else
								gender=text().toUpperCase().charAt(0);
							seedA.setMiscText(gender+"/"+count);
							final TimeClock C = CMLib.time().homeClock(mob);
							if(C!=null)
							{
								final long ticks = (CMProps.getTicksPerMudHour()*C.getHoursInDay()*C.getDaysInYear())
										+(adjustedLevel(mob,0)*(CMProps.getTicksPerMudHour()*C.getHoursInDay()*C.getDaysInMonth()));
								if(seedA.tickDown<ticks)
									seedA.tickDown=(int)ticks;
							}
						}
					}
				}
			}
		}
		return seedA;
	}

	@Override
	public int spreadBitmap()
	{
		if((miscText!=null)
		&&(miscText.indexOf('/')>0)
		&&(CMath.s_int(miscText.substring(miscText.indexOf('/')+1))>0))
			return DiseaseAffect.SPREAD_INHERETED;
		return 0;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;

			if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_STD))
			&&((msg.amITarget(mob))||(msg.amISource(mob)))
			&&(msg.tool() instanceof Social)
			&&(msg.target() instanceof MOB)
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>")))
			{
				final Chant_CurseSeed me = this;
				msg.addTrailerRunnable(new Runnable() {
					final MOB M = mob;
					final MOB targetM=(mob==msg.source())?(MOB)msg.target():msg.source();
					final Chant_CurseSeed meA = me;
					@Override
					public void run()
					{
						meA.pollute(M);
						meA.pollute(targetM);
					}
				});
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto) && (auto||(target.fetchEffect("Pregnancy")==null));
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) genetically cursed!"));
				char gender;
				if((text().length()==0)||("MNF".indexOf(text().toUpperCase().charAt(0))<0))
					gender=(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
				else
					gender=text().toUpperCase().charAt(0);
				final Chant_CurseSeed seedA = (Chant_CurseSeed)target.fetchEffect(ID());
				if(seedA!=null)
				{
					if((seedA.text().length()>0)
					&&(seedA.text().toUpperCase().charAt(0)!='N')
					&&(seedA.text().toUpperCase().charAt(0)!=gender))
						seedA.setMiscText("N");
					if(seedA.invoker!=mob)
						seedA.setTickDown(seedA.tickDown*2);
				}
				else
				{
					final TimeClock C = CMLib.time().homeClock(mob);
					final long ticks = (CMProps.getTicksPerMudHour()*C.getHoursInDay()*C.getDaysInYear())
						+(adjustedLevel(mob,asLevel)*(CMProps.getTicksPerMudHour()*C.getHoursInDay()*C.getDaysInMonth()));
					final Ability A = beneficialAffect(mob,target,asLevel,(int)ticks);
					if(A!=null)
					{
						A.setMiscText(gender+"");
						if(super.getXLEVELLevel(mob)>0)
						{
							A.makeLongLasting();
							A.setMiscText(gender+"/"+super.getXLEVELLevel(mob));
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
