package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Thief_Mark extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Mark";
	}

	private final static String localizedName = CMLib.lang().L("Mark");

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
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"MARK"});
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
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;
	}

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	protected int code=0;
	
	public int ticks=0;
	public MOB mark=null;

	@Override
	public String displayText()
	{
		if(mark!=null)
			return "(Marked: "+mark.name()+", "+ticks+" ticks)";
		return "";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		
		final MOB mark=this.mark;
		if(mark != null)
		{
			if(msg.amISource(mark)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
				this.mark=null;
				ticks=0;
				setMiscText("");
			}
			else
			if((msg.target()==mark)
			&&(msg.source()==invoker)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(mark,msg.source())))
			{
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
											  CMMsg.MSG_OK_VISUAL,L("\n\r^x@x1 is your mark.^?^.\n\r",mark.name(msg.source())),
											  CMMsg.NO_EFFECT,null,
											  CMMsg.NO_EFFECT,null));
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected!=null)&&(affected instanceof MOB)&&(((MOB)affected).getVictim()==mark))
		{
			final int xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+((ticks+xlvl)/20));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((ticks+xlvl)/2));
		}
	}

	@Override
	public boolean tick(Tickable me, int tickID)
	{
		if((text().length()==0)
		||((affected==null)||(!(affected instanceof MOB))))
		   return super.tick(me,tickID);
		final MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mark==null)
			{
				final int x=text().indexOf('/');
				if(x<0)
					return super.tick(me,tickID);
				final MOB M=mob.location().fetchInhabitant(text().substring(0,x));
				if(M!=null)
				{
					mark=M;
					ticks=CMath.s_int(text().substring(x+1));
				}
				else
				{
					mark=null;
					ticks=0;
					setMiscText("");
				}
			}
			else
			if(mob.location().isInhabitant(mark)
			&&(CMLib.flags().canBeSeenBy(mark,mob))
			&&(!CMLib.flags().canBeSeenBy(mob,mark)))
			{
				ticks++;
				setMiscText(mark.Name()+"/"+ticks);
			}
			else
			if(mark.amDestroyed())
			{
				mark=null;
				ticks=0;
				setMiscText("");
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Who would you like to mark?"));
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
		if((A!=null)&&(((Thief_Mark)A).mark==target))
		{
			mob.delEffect(A);
			mob.tell(L("You remove your mark from @x1",target.name(mob)));
			return true;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(!success)
			return beneficialVisualFizzle(mob,target,L("<S-NAME> lose(s) <S-HIS-HER> concentration on <T-NAMESELF>."));
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> mark(s) <T-NAMESELF>."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
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
			((Thief_Mark)A).mark=target;
			((Thief_Mark)A).ticks=0;
			A.setMiscText(target.Name()+"/0");
			mob.tell(L("You may use the mark skill again to unmark them."));
		}
		return success;
	}

}
