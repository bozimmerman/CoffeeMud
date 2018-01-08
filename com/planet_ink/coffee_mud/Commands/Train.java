package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class Train extends StdCommand
{
	public Train(){}

	private final String[] access=I(new String[]{"TRAIN","TR","TRA"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private static final int	TRAIN_HITPOINTS	= 101;
	private static final int	TRAIN_MANA		= 102;
	private static final int	TRAIN_MOVE		= 103;
	private static final int	TRAIN_GAIN		= 104;
	private static final int	TRAIN_PRACTICES	= 105;
	private static final int	TRAIN_CCLASS	= 106;
	
	public static List<String> getAllPossibleThingsToTrainFor()
	{
		final List<String> V=new Vector<String>();
		V.add("HIT POINTS");
		V.add("MANA");
		V.add("MOVEMENT");
		V.add("GAIN");
		V.add("PRACTICES");
		for(final int i: CharStats.CODES.BASECODES())
			V.add(CharStats.CODES.DESC(i));
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			if(CMLib.login().isAvailableCharClass(C))
				V.add(C.name().toUpperCase().trim());
		}
		return V;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		List<String> origCmds=new StringXVector(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You have @x1 training sessions. Enter HELP TRAIN for more information.",""+mob.getTrains()));
			return false;
		}
		commands.remove(0);
		String teacherName=null;
		if(commands.size()>1)
		{
			teacherName=commands.get(commands.size()-1);
			if(teacherName.length()>1)
				commands.remove(commands.size()-1);
			else
				teacherName=null;
		}

		final String abilityName=CMParms.combine(commands,0).toUpperCase();
		final StringBuffer thingsToTrainFor=new StringBuffer("");
		for(final int i: CharStats.CODES.BASECODES())
			thingsToTrainFor.append(CharStats.CODES.DESC(i)+", ");

		int trainsRequired=1;
		int abilityCode=mob.baseCharStats().getCode(abilityName);
		int curStat=-1;
		if((abilityCode>=0)&&(CharStats.CODES.isBASE(abilityCode)))
		{
			curStat=mob.baseCharStats().getRacialStat(mob, abilityCode);
			trainsRequired=CMLib.login().getTrainingCost(mob, abilityCode, false);
			if(trainsRequired<0)
				return false;
		}
		else
			abilityCode=-1;
		CharClass theClass=null;
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSTRAINING))&&(abilityCode<0))
		{
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				int classLevel=mob.charStats().getClassLevel(C);
				if(classLevel<0)
					classLevel=0;
				if((C.name().toUpperCase().startsWith(abilityName.toUpperCase()))
				||(C.name(classLevel).toUpperCase().startsWith(abilityName.toUpperCase())))
				{
					if((C.qualifiesForThisClass(mob,false))
					&&(CMLib.login().isAvailableCharClass(C)))
					{
						abilityCode=TRAIN_CCLASS;
						theClass=C;
					}
					break;
				}
				else
				if((C.qualifiesForThisClass(mob,true))
				&&(CMLib.login().isAvailableCharClass(C)))
					thingsToTrainFor.append(C.name()+", ");
			}
		}

		if(abilityCode<0)
		{
			if("HIT POINTS".startsWith(abilityName.toUpperCase()))
				abilityCode=TRAIN_HITPOINTS;
			else
			if("MANA".startsWith(abilityName.toUpperCase()))
				abilityCode=TRAIN_MANA;
			else
			if("MOVE".startsWith(abilityName.toUpperCase()))
				abilityCode=TRAIN_MOVE;
			else
			if("GAIN".startsWith(abilityName.toUpperCase()))
				abilityCode=TRAIN_GAIN;
			else
			if("PRACTICES".startsWith(abilityName.toUpperCase()))
				abilityCode=TRAIN_PRACTICES;
			else
			{
				if(abilityCode<0)
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("You can't train for '@x1'. Try @x2HIT POINTS, MANA, MOVE, GAIN, or PRACTICES.",abilityName,thingsToTrainFor.toString()));
					return false;
				}
			}
		}

		if(abilityCode==TRAIN_GAIN)
		{
			if(mob.getPractices()<7)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have enough practices to do that."));
				return false;
			}
		}
		else
		if(mob.getTrains()<=0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have enough training sessions to do that."));
			return false;
		}
		else
		if(mob.getTrains()<trainsRequired)
		{
			if(trainsRequired>1)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Training that ability further will require @x1 training points.",""+trainsRequired));
				return false;
			}
			else
			if(trainsRequired==1)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Training that ability further will require @x1 training points.",""+trainsRequired));
				return false;
			}
		}

		MOB teacher=null;
		if(teacherName!=null)
			teacher=mob.location().fetchInhabitant(teacherName);
		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}
		if((teacher==null)||(!CMLib.flags().canBeSeenBy(teacher,mob)))
		{
			mob.tell(teacher,null,null,L("You can't see <S-NAME>!"));
			return false;
		}
		if(!CMLib.flags().canBeSeenBy(mob,teacher))
		{
			mob.tell(teacher,null,null,L("<S-NAME> can't see you!"));
			return false;
		}
		if(teacher==mob)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You cannot train with yourself!"));
			return false;
		}
		if(teacher.isAttributeSet(MOB.Attrib.NOTEACH))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1 is refusing to teach right now.",teacher.name()));
			return false;
		}
		if(mob.isAttributeSet(MOB.Attrib.NOTEACH))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You are refusing training at this time."));
			return false;
		}
		if(CMLib.flags().isSleeping(mob)||CMLib.flags().isSitting(mob))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You need to stand up for your training."));
			return false;
		}
		if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
		{
			if(teacher.isMonster())
				CMLib.commands().postStand(teacher,true);
			if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("@x1 looks a bit too relaxed to train with you.",teacher.name()));
				return false;
			}
		}
		if(mob.isInCombat())
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Not while you are fighting!"));
			return false;
		}
		if(teacher.isInCombat())
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Your teacher seems busy right now."));
			return false;
		}

		if(abilityCode==TRAIN_CCLASS)
		{
			boolean canTeach=false;
			for(int c=0;c<teacher.charStats().numClasses();c++)
			{
				if(teacher.charStats().getMyClass(c).baseClass().equals(mob.charStats().getCurrentClass().baseClass()))
					canTeach=true;
			}
			if((!canTeach)
			&&(teacher.charStats().getClassLevel(theClass)<1))
			{
				if((!CMProps.getVar(CMProps.Str.MULTICLASS).startsWith("MULTI"))
				&&(!CMProps.getVar(CMProps.Str.MULTICLASS).endsWith("MULTI")))
				{
					final CharClass C=CMClass.getCharClass(mob.charStats().getCurrentClass().baseClass());
					final String baseClassName=(C!=null)?C.name():mob.charStats().getCurrentClass().baseClass();
					CMLib.commands().postCommandFail(mob,origCmds,L("You can only learn that from another @x1.",baseClassName));
				}
				else
				if(theClass!=null)
				{
					int classLevel=mob.charStats().getClassLevel(theClass);
					if(classLevel<0)
						classLevel=0;
					CMLib.commands().postCommandFail(mob,origCmds,L("You can only learn that from another @x1.",theClass.name(classLevel)));
				}
				return false;
			}
		}

		if(abilityCode<100)
		{
			final int teachStat=teacher.charStats().getStat(abilityCode);
			if(curStat>=teachStat)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You can only train with someone whose score is higher than yours."));
				return false;
			}
			curStat=mob.baseCharStats().getStat(abilityCode);
		}

		final CMMsg msg=CMClass.getMsg(teacher,mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> train(s) with <T-NAMESELF>."));
		if(!mob.location().okMessage(mob,msg))
			return false;
		mob.location().send(mob,msg);
		switch(abilityCode)
		{
		case CharStats.STAT_STRENGTH:
			mob.tell(L("You feel stronger!"));
			mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case CharStats.STAT_INTELLIGENCE:
			mob.tell(L("You feel smarter!"));
			mob.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case CharStats.STAT_DEXTERITY:
			mob.tell(L("You feel more dextrous!"));
			mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case CharStats.STAT_CONSTITUTION:
			mob.tell(L("You feel healthier!"));
			mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case CharStats.STAT_CHARISMA:
			mob.tell(L("You feel more charismatic!"));
			mob.baseCharStats().setStat(CharStats.STAT_CHARISMA,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case CharStats.STAT_WISDOM:
			mob.tell(L("You feel wiser!"));
			mob.baseCharStats().setStat(CharStats.STAT_WISDOM,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case TRAIN_HITPOINTS:
			mob.tell(L("You feel even healthier!"));
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+10);
			mob.maxState().setHitPoints(mob.maxState().getHitPoints()+10);
			mob.curState().setHitPoints(mob.curState().getHitPoints()+10);
			mob.setTrains(mob.getTrains()-1);
			break;
		case TRAIN_MANA:
			mob.tell(L("You feel more powerful!"));
			mob.baseState().setMana(mob.baseState().getMana()+20);
			mob.maxState().setMana(mob.maxState().getMana()+20);
			mob.curState().setMana(mob.curState().getMana()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case TRAIN_MOVE:
			mob.tell(L("You feel more rested!"));
			mob.baseState().setMovement(mob.baseState().getMovement()+20);
			mob.maxState().setMovement(mob.maxState().getMovement()+20);
			mob.curState().setMovement(mob.curState().getMovement()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case TRAIN_GAIN:
			mob.tell(L("You feel more trainable!"));
			mob.setTrains(mob.getTrains()+1);
			mob.setPractices(mob.getPractices()-7);
			break;
		case TRAIN_PRACTICES:
			mob.tell(L("You feel more educatable!"));
			mob.setTrains(mob.getTrains()-1);
			mob.setPractices(mob.getPractices()+5);
			break;
		default:
			if(CMParms.contains(CharStats.CODES.BASECODES(), abilityCode))
			{
				mob.tell(L("You feel more @x1!",CharStats.CODES.NAME(abilityCode)));
				mob.baseCharStats().setStat(abilityCode,curStat+1);
				mob.recoverCharStats();
				mob.setTrains(mob.getTrains()-trainsRequired);
			}
			break;
		case TRAIN_CCLASS:
			if(theClass!=null)
			{
				int classLevel=mob.charStats().getClassLevel(theClass);
				if(classLevel<0)
					classLevel=0;
				mob.tell(L("You have undergone @x1 training!",theClass.name(classLevel)));
				mob.setTrains(mob.getTrains()-1);
				mob.baseCharStats().getCurrentClass().endCharacter(mob);
				mob.baseCharStats().setCurrentClass(theClass);
				if((!mob.isMonster())&&(mob.soulMate()==null))
					CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_CLASSCHANGE);
				mob.recoverCharStats();
				mob.charStats().getCurrentClass().startCharacter(mob,false,true);
			}
			break;
		}
		return false;
	}
	
	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}
}
