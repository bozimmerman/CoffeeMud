package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
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
   Copyright 2003-2020 Bo Zimmerman

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

	public static final long DOUBT_TIME=TimeManager.MILI_HOUR;

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

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if(text().length()>0)
			{
				mob.tell(L("You start to have doubts about @x1.",text()));
				if(mob.isMonster())
				{
					mob.baseCharStats().setWorshipCharID("");
					mob.charStats().setWorshipCharID("");
					final Room startRoom=mob.getStartRoom();
					final Area startArea=(startRoom==null)?null:startRoom.getArea();
					if(startArea!=null)
						Resources.removeResource("PIETY_"+startArea.Name().toUpperCase());
				}
			}
		}

		super.unInvoke();
	}


	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((text().length()>0)
		&&(affected instanceof MOB)
		&&(!text().equals(affectableStats.getWorshipCharID())))
			affectableStats.setWorshipCharID(text());
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
		Deity deityToConvertToM=CMLib.map().getDeity(CMParms.combine(commands,0));
		if(deityToConvertToM==null)
		{
			deityToConvertToM=mob.charStats().getMyDeity();
			if(deityToConvertToM==null)
				deityToConvertToM=mob.baseCharStats().getMyDeity();
			target=getTarget(mob,commands,givenTarget,false,true);
			if(target==null)
			{
				mob.tell(L("You've also never heard of a deity called '@x1'.",CMParms.combine(commands,0)));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("I've never heard of '@x1'.",CMParms.combine(commands,0)),false,false);
				return false;
			}
			if(deityToConvertToM==null)
			{
				mob.tell(L("A faithless one cannot convert @x1.",target.name(mob)));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("I am faithless, and can not convert you."),false,false);
				return false;
			}
		}
		if(CMLib.flags().isAnimalIntelligence(target))
		{
			mob.tell(L("You can't convert @x1.",target.name(mob)));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,target,L("I can not convert you."),false,false);
			return false;
		}
		if((target.baseCharStats().getMyDeity()==deityToConvertToM)
		||(target.charStats().getMyDeity()==deityToConvertToM))
		{
			mob.tell(L("@x1 already worships @x2.",target.name(mob),deityToConvertToM.name()));
			if(mob.isMonster())
				CMLib.commands().postSay(mob,target,L("You already worship @x1.",deityToConvertToM.Name()),false,false);
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

		int levelDiff=0;
		if((target.isMonster())&&(target.phyStats().level()>mob.phyStats().level()))
		{
			levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
			if(levelDiff<0)
				levelDiff=0;
		}

		final boolean success=proficiencyCheck(mob,-(levelDiff*15),auto);
		// when faith < 100, they have so much faith they can't be converted?
		// when faith >= 100, the have so much doubt they CAN be converted.
		boolean targetMadeSave=(givenTarget == target) || CMLib.dice().roll(1,100,0)>(target.charStats().getSave(CharStats.STAT_SAVE_DOUBT));
		//if(CMSecurity.isASysOp(mob))
		//	targetMadeSave=false;
		if((!target.isMonster())
		&&(success)
		&&(targetMadeSave))
		{
			if((target.charStats().deityName().length()>0)
			||(target.baseCharStats().getMyDeity()!=null))
			{
				final String requiredMask = CMParms.getParmStr(CMProps.getVar(Str.DEITYPOLICY), "REQUIREDMASK", "").trim();
				if((requiredMask.length()==0)
				||(CMLib.masking().maskCheck(requiredMask, mob, true)))
				{
					final boolean canConvertDeity=!CMParms.getParmBool(CMProps.getVar(Str.DEITYPOLICY), "NOCONVERT", false);
					if(!canConvertDeity)
					{
						mob.tell(L("You are not able to convert @x1.",target.name(mob)));
						return false;
					}
					final boolean canRebukeDeity=!CMParms.getParmBool(CMProps.getVar(Str.DEITYPOLICY), "NOREBUKE", false);
					if((!canRebukeDeity) // we must auto-rebuke
					&&(target.baseCharStats().getMyDeity()!=null))
					{
						CMMsg msg=null;
						final Deity deity=target.baseCharStats().getMyDeity();
						msg=CMClass.getMsg(target,deity,null,CMMsg.MSG_REBUKE,L("<S-NAME> rebuke(s) @x1.",deity.Name()));
						if(target.location().okMessage(mob,msg))
							target.location().send(mob,msg);
					}
				}
			}
			if(target.charStats().deityName().length()>0)
			{
				mob.tell(L("@x1 is worshipping @x2.  @x3 must REBUKE @x4 first.",
						target.name(mob),
						target.charStats().getWorshipCharID(),
						target.charStats().HeShe(),
						target.baseCharStats().getMyDeity().charStats().himher()));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("You already worship @x1.",target.charStats().getWorshipCharID()),false,false);
				return false;
			}
			else
			if(target.baseCharStats().getMyDeity()!=null)
			{
				mob.tell(L("@x1 is worshipping @x2.  @x3 must REBUKE @x4 first.",
						target.name(mob),
						L(" a deity"),
						target.charStats().HeShe(),
						L("him")));
				if(mob.isMonster())
					CMLib.commands().postSay(mob,target,L("You already worship a deity."),false,false);
				return false;
			}

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
					final Deity tD=deityToConvertToM;
					final MOB t=target;
					final Ability A=this;
					tsess.prompt(new InputCallback(InputCallback.Type.CONFIRM, "N", 30000L)
					{
						final Session	session	= tsess;
						final Deity		D		= tD;
						final MOB		target	= t;
						final Ability	me		= A;

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
			Room dRoom=deityToConvertToM.location();
			if(dRoom==mob.location())
				dRoom=null;
			final Deity rebukeDeityM=target.baseCharStats().getMyDeity() != null ? target.baseCharStats().getMyDeity() : target.charStats().getMyDeity();
			if(rebukeDeityM!=null)
			{
				final Ability A=target.fetchEffect(ID());
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
				}
				final CMMsg msg2=CMClass.getMsg(target,target.baseCharStats().getMyDeity(),this,CMMsg.MSG_REBUKE,null);
				if((mob.location().okMessage(mob,msg2))&&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
				{
					mob.location().send(target,msg2);
					if(dRoom!=null)
						dRoom.send(target,msg2);
				}
			}
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?L("<T-NAME> <T-IS-ARE> converted!"):L("<S-NAME> convert(s) <T-NAMESELF> to the worship of @x1.",deityToConvertToM.name()));
			final CMMsg msg2=CMClass.getMsg(target,deityToConvertToM,this,target.isMonster()?CMMsg.MSG_OK_ACTION:CMMsg.MSG_SERVE,null);
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
					final Room startRoom=target.getStartRoom();
					final Area startArea=(startRoom==null)?null:startRoom.getArea();
					if(startArea!=null)
						Resources.removeResource("PIETY_"+startArea.Name().toUpperCase());
				}

			}
		}
		else
		{
			if((target.isMonster())&&(target.fetchEffect("Prayer_ReligiousDoubt")==null))
			{
				final Ability A=CMClass.getAbility("Prayer_ReligiousDoubt");
				if(A!=null)
				{
					A.invoke(mob,target,true,asLevel);
					final Ability effA=target.fetchEffect("Prayer_ReligiousDoubt");
					if((effA!=null)
					&&(effA.canBeUninvoked())
					&&(effA.expirationDate()>0))
						effA.setExpirationDate((int)(DOUBT_TIME/CMProps.getTickMillis()));
				}
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
