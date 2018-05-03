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
public class Thief_Con extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Con";
	}

	private final static String	localizedName	= CMLib.lang().L("Con");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CON" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DECEPTIVE;
	}

	protected MOB	lastChecked	= null;

	@Override
	public double castingTime(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSkillActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.Int.DEFABLETIME), 20.0));
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		if(commands!=null)
			commands=new XVector<String>(commands);
		if(!conCheck(mob,commands,givenTarget,auto,asLevel))
			return false;
		final Vector<String> V=new Vector<String>();
		V.addElement(commands.get(0));
		final MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null)
			return false;
		commands.remove(0);
		if(secondsElapsed>0)
		{
			if((secondsElapsed%4)==0)
				return mob.location().show(mob,target,CMMsg.MSG_SPEAK,L("^T<S-NAME> continue(s) conning <T-NAMESELF> to '@x1'.^?",CMParms.combine(commands,0)));
			return true;
		}
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> attempt(s) to con <T-NAMESELF> to '@x1'.^?",CMParms.combine(commands,0)));
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		else
			return false;
		return true;
	}

	public boolean conCheck(MOB mob, List<String> commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands!=null)
			commands= new XVector<String>(commands);
		if(commands.size()<1)
		{
			mob.tell(L("Con whom into doing what?"));
			return false;
		}
		final Vector<String> V=new Vector<String>();
		V.addElement(commands.get(0));
		final MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null)
			return false;

		commands.remove(0);

		if((!target.mayIFight(mob))
		||(!target.isMonster())
		||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
		{
			mob.tell(L("You can't con @x1.",target.name(mob)));
			return false;
		}

		if(target.isInCombat())
		{
			mob.tell(L("@x1 is too busy fighting right now.",target.name(mob)));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("You are too busy fighting right now."));
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell(L("Con @x1 into doing what?",target.charStats().himher()));
			return false;
		}

		if(commands.get(0).toUpperCase().startsWith("FOL"))
		{
			mob.tell(L("You can't con someone into following you."));
			return false;
		}

		CMObject O=CMLib.english().findCommand(target,commands);
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob))||(((Command)O).ID().equals("Sleep")))
			{
				mob.tell(L("You can't con someone into doing that."));
				return false;
			}
		}
		else
		{
			if(O instanceof Ability)
				O=CMLib.english().getToEvoke(target,commands);
			if(O instanceof Ability)
			{
				if(CMath.bset(((Ability)O).flags(),Ability.FLAG_NOORDERING))
				{
					mob.tell(L("You can't con @x1 to do that.",target.name(mob)));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands!=null)
			commands=new XVector<String>(commands);
		if(!conCheck(mob,commands,givenTarget,auto,asLevel))
			return false;
		final Vector<String> V=new Vector<String>();
		V.addElement(commands.get(0));
		final MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null)
			return false;
		commands.remove(0);

		final int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=((mob.phyStats().level()+(2*getXLEVELLevel(mob)))-target.phyStats().level())*10;
		if(levelDiff>0)
			levelDiff=0;
		final boolean success=proficiencyCheck(mob,(mob.charStats().getStat(CharStats.STAT_CHARISMA)*2)+levelDiff,auto);

		if(!success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> tr(ys) to con <T-NAMESELF> to '@x1', but <S-IS-ARE> unsuccessful.^?",CMParms.combine(commands,0)));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> con(s) <T-NAMESELF> to '@x1'.^?",CMParms.combine(commands,0)));
			mob.recoverPhyStats();
			final CMMsg omsg=CMClass.getMsg(mob,target,null,CMMsg.MSG_ORDER,null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob, omsg)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,omsg);
				if(omsg.sourceMinor()==CMMsg.TYP_ORDER)
					target.enqueCommand(commands,MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER,0);
			}
			target.recoverPhyStats();
		}
		if(target==lastChecked)
			setProficiency(oldProficiency);
		lastChecked=target;
		return success;
	}

}
