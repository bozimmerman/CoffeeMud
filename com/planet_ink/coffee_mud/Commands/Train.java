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
   Copyright 2004-2015 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Train extends StdCommand
{
	public Train(){}

	private final String[] access=I(new String[]{"TRAIN","TR","TRA"});
	@Override public String[] getAccessWords(){return access;}

	public static Vector getAllPossibleThingsToTrainFor()
	{
		final Vector V=new Vector();
		V.add("HIT POINTS");
		V.add("MANA");
		V.add("MOVEMENT");
		V.add("GAIN");
		V.add("PRACTICES");
		for(final int i: CharStats.CODES.BASECODES())
			V.add(CharStats.CODES.DESC(i));
		for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=(CharClass)c.nextElement();
			if((!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK))&&(C.availabilityCode()!=0))
				V.add(C.name().toUpperCase().trim());
		}
		return V;
	}


	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Vector origCmds=new XVector(commands);
		if(commands.size()<2)
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("You have @x1 training sessions. Enter HELP TRAIN for more information.",""+mob.getTrains()));
			return false;
		}
		commands.remove(0);
		String teacherName=null;
		if(commands.size()>1)
		{
			teacherName=(String)commands.lastElement();
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
			for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=(CharClass)c.nextElement();
				int classLevel=mob.charStats().getClassLevel(C);
				if(classLevel<0)
					classLevel=0;
				if((C.name().toUpperCase().startsWith(abilityName.toUpperCase()))
				||(C.name(classLevel).toUpperCase().startsWith(abilityName.toUpperCase())))
				{
					if((C.qualifiesForThisClass(mob,false))
					&&(!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK))
					&&(C.availabilityCode()!=0))
					{
						abilityCode=106;
						theClass=C;
					}
					break;
				}
				else
				if((C.qualifiesForThisClass(mob,true))
				&&(!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK)))
					thingsToTrainFor.append(C.name()+", ");
			}
		}

		if(abilityCode<0)
		{
			if("HIT POINTS".startsWith(abilityName.toUpperCase()))
				abilityCode=101;
			else
			if("MANA".startsWith(abilityName.toUpperCase()))
				abilityCode=102;
			else
			if("MOVE".startsWith(abilityName.toUpperCase()))
				abilityCode=103;
			else
			if("GAIN".startsWith(abilityName.toUpperCase()))
				abilityCode=104;
			else
			if("PRACTICES".startsWith(abilityName.toUpperCase()))
				abilityCode=105;
			else
			{
				if(abilityCode<0)
				{
					CMLib.commands().doCommandFail(mob,origCmds,L("You can't train for '@x1'. Try @x2HIT POINTS, MANA, MOVE, GAIN, or PRACTICES.",abilityName,thingsToTrainFor.toString()));
					return false;
				}
			}
		}

		if(abilityCode==104)
		{
			if(mob.getPractices()<7)
			{
				CMLib.commands().doCommandFail(mob,origCmds,L("You don't seem to have enough practices to do that."));
				return false;
			}
		}
		else
		if(mob.getTrains()<=0)
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("You don't seem to have enough training sessions to do that."));
			return false;
		}
		else
		if(mob.getTrains()<trainsRequired)
		{
			if(trainsRequired>1)
			{
				CMLib.commands().doCommandFail(mob,origCmds,L("Training that ability further will require @x1 training points.",""+trainsRequired));
				return false;
			}
			else
			if(trainsRequired==1)
			{
				CMLib.commands().doCommandFail(mob,origCmds,L("Training that ability further will require @x1 training points.",""+trainsRequired));
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
			mob.tell(teacher,null,null,L("<S-NAME> can't see you!"));
			return false;
		}
		if(!CMLib.flags().canBeSeenBy(mob,teacher))
		{
			mob.tell(teacher,null,null,L("<S-NAME> can't see you!"));
			return false;
		}
		if(teacher==mob)
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("You cannot train with yourself!"));
			return false;
		}
		if(teacher.isAttribute(MOB.Attrib.NOTEACH))
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("@x1 is refusing to teach right now.",teacher.name()));
			return false;
		}
		if(mob.isAttribute(MOB.Attrib.NOTEACH))
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("You are refusing training at this time."));
			return false;
		}
		if(CMLib.flags().isSleeping(mob)||CMLib.flags().isSitting(mob))
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("You need to stand up for your training."));
			return false;
		}
		if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
		{
			if(teacher.isMonster())
				CMLib.commands().postStand(teacher,true);
			if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
			{
				CMLib.commands().doCommandFail(mob,origCmds,L("@x1 looks a bit too relaxed to train with you.",teacher.name()));
				return false;
			}
		}
		if(mob.isInCombat())
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("Not while you are fighting!"));
			return false;
		}
		if(teacher.isInCombat())
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("Your teacher seems busy right now."));
			return false;
		}

		if(abilityCode==106)
		{
			boolean canTeach=false;
			for(int c=0;c<teacher.charStats().numClasses();c++)
				if(teacher.charStats().getMyClass(c).baseClass().equals(mob.charStats().getCurrentClass().baseClass()))
					canTeach=true;
			if((!canTeach)
			&&(teacher.charStats().getClassLevel(theClass)<1))
			{
				if((!CMProps.getVar(CMProps.Str.MULTICLASS).startsWith("MULTI"))
				&&(!CMProps.getVar(CMProps.Str.MULTICLASS).endsWith("MULTI")))
				{
					final CharClass C=CMClass.getCharClass(mob.charStats().getCurrentClass().baseClass());
					final String baseClassName=(C!=null)?C.name():mob.charStats().getCurrentClass().baseClass();
					CMLib.commands().doCommandFail(mob,origCmds,L("You can only learn that from another @x1.",baseClassName));
				}
				else
				if(theClass!=null)
				{
					int classLevel=mob.charStats().getClassLevel(theClass);
					if(classLevel<0)
						classLevel=0;
					CMLib.commands().doCommandFail(mob,origCmds,L("You can only learn that from another @x1.",theClass.name(classLevel)));
				}
				return false;
			}
		}

		if(abilityCode<100)
		{
			final int teachStat=teacher.charStats().getStat(abilityCode);
			if(curStat>=teachStat)
			{
				CMLib.commands().doCommandFail(mob,origCmds,L("You can only train with someone whose score is higher than yours."));
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
		case 0:
			mob.tell(L("You feel stronger!"));
			mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 1:
			mob.tell(L("You feel smarter!"));
			mob.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 2:
			mob.tell(L("You feel more dextrous!"));
			mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 3:
			mob.tell(L("You feel healthier!"));
			mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 4:
			mob.tell(L("You feel more charismatic!"));
			mob.baseCharStats().setStat(CharStats.STAT_CHARISMA,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 5:
			mob.tell(L("You feel wiser!"));
			mob.baseCharStats().setStat(CharStats.STAT_WISDOM,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 101:
			mob.tell(L("You feel even healthier!"));
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+10);
			mob.maxState().setHitPoints(mob.maxState().getHitPoints()+10);
			mob.curState().setHitPoints(mob.curState().getHitPoints()+10);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 102:
			mob.tell(L("You feel more powerful!"));
			mob.baseState().setMana(mob.baseState().getMana()+20);
			mob.maxState().setMana(mob.maxState().getMana()+20);
			mob.curState().setMana(mob.curState().getMana()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 103:
			mob.tell(L("You feel more rested!"));
			mob.baseState().setMovement(mob.baseState().getMovement()+20);
			mob.maxState().setMovement(mob.maxState().getMovement()+20);
			mob.curState().setMovement(mob.curState().getMovement()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 104:
			mob.tell(L("You feel more trainable!"));
			mob.setTrains(mob.getTrains()+1);
			mob.setPractices(mob.getPractices()-7);
			break;
		case 105:
			mob.tell(L("You feel more educatable!"));
			mob.setTrains(mob.getTrains()-1);
			mob.setPractices(mob.getPractices()+5);
			break;
		case 106:
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
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandActionCost(ID());}
	@Override public boolean canBeOrdered(){return false;}


}
