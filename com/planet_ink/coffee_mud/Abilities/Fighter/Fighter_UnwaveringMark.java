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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_UnwaveringMark extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_UnwaveringMark";
	}

	private final static String localizedName = CMLib.lang().L("Unwavering Mark");

	@Override
	public String name()
	{
		return localizedName;
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

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings =I(new String[] {"UMARK"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COMBATLORE;
	}

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	protected int code=0;

	public MOB mark=null;

	@Override
	public String displayText()
	{
		if(mark!=null)
			return "(Marked: "+mark.name()+")";
		return "";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{

		final MOB mark=this.mark;
		if(mark != null)
		{
			if(msg.amISource(mark))
			{
				if(msg.sourceMinor() == CMMsg.TYP_DEATH)
				{
					this.mark=null;
					setMiscText("");
				}
				else
				if((msg.target() instanceof MOB)
				&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
				&&(msg.target() != affected)
				&&(msg.target() != invoker())
				&&(msg.trailerRunnables()==null)
				&&(CMLib.flags().isAliveAwakeMobileUnbound(invoker(), true))
				&&super.proficiencyCheck(invoker(), 0, false))
				{
					final MOB attackM = invoker() == null ? (MOB)affected : invoker();
					msg.addTrailerRunnable(new Runnable() {
						final MOB aM = attackM;
						final MOB tM = msg.source();
						@Override
						public void run()
						{
							if((aM==null)||(tM==null))
								return;
							CMLib.combat().postAttack(aM, tM, aM.fetchWieldedItem());
							final Ability A = aM.fetchAbility(ID());
							final int chance = ((A==null)?50:A.proficiency()/2) + (getXLEVELLevel(invoker()) * 5);
							if(CMLib.dice().rollPercentage()<chance)
							{
								final Room R = CMLib.map().roomLocation(aM);
								if(R!=null)
									R.show(tM, aM, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> switch(es) <S-HIS-HER> attention to <T-NAME>."));
								if(tM.mayIFight(aM))
									tM.setVictim(aM);
							}
						}
					});
				}
			}
			else
			if((msg.target()==mark)
			&&(msg.source()==invoker)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(mark,msg.source())))
			{
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
											  CMMsg.MSG_OK_VISUAL,L("\n\r^x@x1 is your unwavering mark.^?^.\n\r",mark.name(msg.source())),
											  CMMsg.NO_EFFECT,null,
											  CMMsg.NO_EFFECT,null));
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((text().length()==0)
		||((affected==null)||(!(affected instanceof MOB))))
			return super.tick(ticking,tickID);
		final MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mark==null)
			{
				final int x=text().indexOf('/');
				if(x<0)
					return super.tick(ticking,tickID);
				final MOB M=mob.location().fetchInhabitant(text().substring(0,x));
				if(M!=null)
					mark=M;
				else
				{
					mark=null;
					setMiscText("");
				}
			}
			else
			if(mark.amDestroyed())
			{
				mark=null;
				setMiscText("");
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Who do you need to mark?"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(target==mob)
		{
			mob.tell(L("You cannot mark yourself!"));
			return false;
		}
		Ability A=mob.fetchEffect(ID());
		if((A!=null)&&(((Fighter_UnwaveringMark)A).mark==target))
		{
			mob.delEffect(A);
			mob.tell(L("You remove your unwavering mark from @x1",target.name(mob)));
			return true;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISE,L("<S-NAME> challenge(s) <T-NAMESELF> with <S-HIS-HER> unwavering mark."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				A=mob.fetchEffect(ID());
				if(A==null)
				{
					A=(Ability)copyOf();
					mob.addEffect(A);
					A.makeNonUninvokable();
				}
				((Fighter_UnwaveringMark)A).mark=target;
				((Fighter_UnwaveringMark)A).invoker=mob;
				A.setMiscText(target.Name()+"/0");
				mob.tell(L("You may use this skill again to unmark them."));
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> challenge(s) <T-NAME>, but is ignored."));
		return success;
	}

}
