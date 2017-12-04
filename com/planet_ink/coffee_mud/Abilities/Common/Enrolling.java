package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2003-2017 Bo Zimmerman

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

public class Enrolling extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Enrolling";
	}

	private final static String localizedName = CMLib.lang().L("Enrolling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ENROLLING","ENROLL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_EDUCATIONLORE;
	}

	protected Physical enrollingM=null;
	protected CharClass enrolledInC=null;
	protected boolean messedUp=false;
	public Enrolling()
	{
		super();
		displayText=L("You are enrolling...");
		verb=L("enrolling");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((enrollingM==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((enrollingM instanceof MOB)&&(!mob.location().isInhabitant((MOB)enrollingM)))
			{
				messedUp=true;
				unInvoke();
			}
			if((enrollingM instanceof Item)&&(!mob.location().isContent((Item)enrollingM)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((enrollingM!=null)&&(!aborted))
				{
					MOB M=null;
					if(enrollingM instanceof MOB)
						M=(MOB)enrollingM;
					else
					if((enrollingM!=null)&&(enrollingM instanceof CagedAnimal))
						M=((CagedAnimal)enrollingM).unCageMe();
					if((messedUp)||(M==null))
						commonTell(mob,L("You've failed to enroll @x1!",enrollingM.name()));
					else
					{
						if(M.charStats().getCurrentClass() != CMClass.getCharClass("StdCharClass"))
							commonTell(mob,L("@x1 is already enrolled.",enrollingM.name()));
						else
						{
							mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to enroll @x1 in @x2.",M.name(),enrolledInC.name()));
							Ability A=M.fetchEffect("Prop_Adjuster");
							String txt="";
							if(A!=null)
								txt=A.text();
							else
							{
								A=CMClass.getAbility("Prop_Adjuster");
								A.setSavable(true);
								M.addNonUninvokableEffect(A);
							}
							txt += " class="+enrolledInC.ID()+" clstart="+M.basePhyStats().level();
							txt = txt.trim();
							A.setMiscText(txt);
							M.recoverCharStats();
							M.recoverMaxState();
							M.recoverPhyStats();
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("enrolling");
		enrollingM=null;
		enrolledInC=null;
		if(commands.size()<2)
		{
			commonTell(mob,L("Enroll whom in what class?"));
			return false;
		}
		String s1=commands.get(commands.size()-1);
		enrolledInC=CMClass.getCharClass(s1);
		if(enrolledInC==null)
			enrolledInC=CMClass.findCharClass(s1);
		if(enrolledInC==null)
		{
			commonTell(mob,L("You don't know of a class called @x1.",s1));
			return false;
		}
		else
			commands.remove(commands.size()-1);
		
		if((!(mob.charStats().getCharClasses().contains(enrolledInC)))
		&&((mob.playerStats()==null)||(mob.playerStats().getActiveTitle().toLowerCase().indexOf(enrolledInC.name().toLowerCase())<0)))
		{
			commonTell(mob,L("You need to either be a @x1, or an honorary @x1 to enroll anyone in that.",enrolledInC.name()));
			return false;
		}
			
		
		
		final String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		enrollingM=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",str));
				return false;
			}
			if(!M.isMonster())
			{
				commonTell(mob,L("You can't enroll @x1.",M.name(mob)));
				return false;
			}
			if(M.charStats().getCurrentClass()!=CMClass.getCharClass("StdCharClass"))
			{
				commonTell(mob,L("@x1 already has a career in @x2.",M.name(),M.charStats().getCurrentClass().name()));
				return false;
			}
			if((!mob.getGroupMembers(new HashSet<MOB>()).contains(M))
			&&(CMLib.flags().canMove(M))
			&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,L("@x1 doesn't seem willing to cooperate.",M.name(mob)));
				return false;
			}
			enrollingM=M;
		}
		else
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,0,auto);
		final int duration=getDuration(35,mob,enrollingM.phyStats().level(),10);
		verb=L("enrolling @x1 into a @x2 career",M.name(),enrolledInC.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) enrolling @x1 into a @x2 career.",M.name(),enrolledInC.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
