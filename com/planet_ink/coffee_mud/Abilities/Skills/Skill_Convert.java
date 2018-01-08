package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback.Type;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
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
public class Skill_Convert extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Convert";
	}

	private final static String	localizedName	= CMLib.lang().L("Convert");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CONVERT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EVANGELISM;
	}

	protected static PairVector<MOB, Long>	convertStack	= new PairVector<MOB, Long>();

	@Override
	public int overrideMana()
	{
		return 50;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	protected String	priorFaith	= "";

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
			if(text().length()>0)
				mob.tell(L("You start to have doubts about @x1.",text()));
			mob.setWorshipCharID(priorFaith);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((text().length()>0)&&(affected instanceof MOB)&&(!text().equals(((MOB)affected).getWorshipCharID())))
			((MOB)affected).setWorshipCharID(text());
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> oldCommands = new XVector<String>(commands);
		if(commands.size()==0)
		{
			mob.tell(L("You must specify either a deity to convert yourself to, or a player to convert to your religion."));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,null,L("I am unable to convert."),false,false);
			return false;
		}

		MOB target=mob;
		Deity D=CMLib.map().getDeity(CMParms.combine(commands,0));
		if(D==null)
		{
			D=mob.getMyDeity();
			target=getTarget(mob,commands,givenTarget,false,true);
			if(target==null)
			{
				mob.tell(L("You've also never heard of a deity called '@x1'.",CMParms.combine(commands,0)));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("I've never heard of '@x1'.",CMParms.combine(commands,0)),false,false);
				return false;
			}
			if(D==null)
			{
				mob.tell(L("A faithless one cannot convert @x1.",target.name(mob)));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("I am faithless, and can not convert you."),false,false);
				return false;
			}
		}
		if((CMLib.flags().isAnimalIntelligence(target))
		||((target.isMonster())&&(target.phyStats().level()>mob.phyStats().level())))
		{
			mob.tell(L("You can't convert @x1.",target.name(mob)));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,target,L("I can not convert you."),false,false);
			return false;
		}
		if(target.getMyDeity()==D)
		{
			mob.tell(L("@x1 already worships @x2.",target.name(mob),D.name()));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,target,L("You already worship @x1.",D.Name()),false,false);
			return false;
		}
		if(!auto)
		{
			convertStack.clear();
			if(convertStack.containsFirst(target))
			{
				final Long L=convertStack.getSecond(convertStack.indexOfFirst(target));
				if((System.currentTimeMillis()-L.longValue())>CMProps.getMillisPerMudHour()*5)
					convertStack.removeElementFirst(target);
			}
			if(convertStack.containsFirst(target))
			{
				mob.tell(L("@x1 must wait to be converted again.",target.name(mob)));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("You must wait to be converted again."),false,false);
				return false;
			}
		}

		final boolean success=proficiencyCheck(mob,0,auto);
		boolean targetMadeSave=(givenTarget == target) || CMLib.dice().roll(1,100,0)>(target.charStats().getSave(CharStats.STAT_FAITH));
		//if(CMSecurity.isASysOp(mob))
		//	targetMadeSave=false;
		if((!target.isMonster())
		&&(success)
		&&(targetMadeSave)
		&&(target.getMyDeity()!=null))
		{
			mob.tell(L("@x1 is worshipping @x2.  @x3 must REBUKE @x4 first.",target.name(mob),target.getMyDeity().name(),target.charStats().HeShe(),target.getMyDeity().charStats().himher()));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,target,L("You already worship @x1.",target.getMyDeity().Name()),false,false);
			return false;
		}
		if((success)
		&&(targetMadeSave)
		&&(!target.isMonster())
		&&(target!=mob))
		{
			if(givenTarget == target)
				targetMadeSave=!success;
			else
			{
				try
				{
					final Session tsess = target.session();
					final Deity tD=D;
					final MOB t=target;
					final Ability A=this;
					tsess.prompt(new InputCallback(InputCallback.Type.CONFIRM, "N", 30000L) {
						
						final Session session = tsess;
						final Deity D=tD;
						final MOB target=t;
						final Ability me=A;

						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\r@x1 is trying to convert you to the worship of @x2.  Is this what you want (N/y)?",mob.name(target),D.name()));
						}
	
						@Override
						public void timedOut()
						{
						}
	
						@Override
						public void callBack()
						{
							if(this.confirmed)
								me.invoke(mob, oldCommands, target, false, asLevel);
							else
							{
								if(CMLib.flags().isInTheGame(mob, true))
									mob.location().show(mob,target,CMMsg.MSG_SPEAK,L("<S-YOUPOSS> attempt to convert <T-NAME> to the worship of @x1 is rejected.",D.name()));
							}
						}
					});
					return false;
				}
				catch(final Exception e)
				{
					return false;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((success)
		&&((!targetMadeSave)||(target==mob)))
		{
			Room dRoom=D.location();
			if(dRoom==mob.location())
				dRoom=null;
			if(target.getMyDeity()!=null)
			{
				final Ability A=target.fetchEffect(ID());
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
				}
				final CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_REBUKE,null);
				if((mob.location().okMessage(mob,msg2))&&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
				{
					mob.location().send(target,msg2);
					if(dRoom!=null)
						dRoom.send(target,msg2);
				}
			}
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?L("<T-NAME> <T-IS-ARE> converted!"):L("<S-NAME> convert(s) <T-NAMESELF> to the worship of @x1.",D.name()));
			final CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_SERVE,null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg2))
			&&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(target,msg2);
				if(dRoom!=null)
					dRoom.send(target,msg2);
				convertStack.addElement(target,Long.valueOf(System.currentTimeMillis()));
				if(mob!=target)
				{
					if(target.isMonster())
						CMLib.leveler().postExperience(mob,null,null,1,false);
					else
						CMLib.leveler().postExperience(mob,null,null,200,false);
				}
				if(target.isMonster())
				{
					beneficialAffect(mob,target,asLevel,(int)(TimeManager.MILI_HOUR/CMProps.getTickMillis()));
					final Skill_Convert A=(Skill_Convert)target.fetchEffect(ID());
					if(A!=null)
						A.priorFaith=target.getWorshipCharID();
				}
			}
		}
		else
		{
			if((target.isMonster())&&(target.fetchEffect("Prayer_ReligiousDoubt")==null))
			{
				final Ability A=CMClass.getAbility("Prayer_ReligiousDoubt");
				if(A!=null)
					A.invoke(mob,target,true,asLevel);
			}
			else
				beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to convert <T-NAMESELF>, but <S-IS-ARE> unconvincing."));
		}

		// return whether it worked
		return success;
	}

	@Override
	public void makeLongLasting()
	{
		tickDown=(int)(CMProps.getTicksPerMinute()*60*24*7);
	}
}
