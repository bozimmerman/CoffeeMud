package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Score extends StdCommand
{
	public Score(){}

	private String[] access={"SCORE","SC"};
	public String[] getAccessWords(){return access;}


	public StringBuffer getAffects(MOB affected)
	{
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<affected.numEffects();a++)
		{
			Ability thisAffect=affected.fetchEffect(a);
			if((thisAffect!=null)&&(thisAffect.displayText().length()>0))
				msg.append("\n\r^S"+thisAffect.displayText());
		}
		if(msg.length()==0)
			msg.append("Nothing!");
		else
			msg.append("^?");
		return msg;
	}

	public StringBuffer getScore(MOB mob)
	{
		int adjustedAttack=mob.adjustedAttackBonus(null);
		int adjustedArmor=(-mob.adjustedArmor())+50;

		StringBuffer msg=new StringBuffer("");

		String levelStr=null;
		int classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
		if(classLevel>=mob.envStats().level())
			levelStr="level "+mob.envStats().level()+" "+mob.charStats().getCurrentClass().name();
		else
			levelStr=mob.charStats().getCurrentClass().name()+" "+classLevel+"/"+mob.envStats().level();
		msg.append("You are ^H"+mob.Name()+"^? the ^H"+levelStr+"^?.\n\r");
		if(classLevel<mob.envStats().level())
		{
			msg.append("You also have levels in: ");
			StringBuffer classList=new StringBuffer("");
			for(int c=0;c<mob.charStats().numClasses()-1;c++)
			{
				CharClass C=mob.charStats().getMyClass(c);
				if(C!=mob.charStats().getCurrentClass())
				{
					if(classList.length()>0)
						if(c==mob.charStats().numClasses()-2)
							classList.append(", and ");
						else
							classList.append(", ");
					classList.append(C.name()+" ("+mob.charStats().getClassLevel(C)+") ");
				}
			}
			msg.append(classList.toString()+".\n\r");
		}

		String genderName="neuter";
		if(mob.charStats().getStat(CharStats.GENDER)=='M') genderName="male";
		else
		if(mob.charStats().getStat(CharStats.GENDER)=='F') genderName="female";
		msg.append("You are a ^!"+genderName+" "+mob.charStats().getMyRace().name() + "^?");
		if(mob.getLeigeID().length()>0)
			msg.append(" who serves ^H"+mob.getLeigeID()+"^?");
		if(mob.getWorshipCharID().length()>0)
			msg.append(" worshipping ^H"+mob.getWorshipCharID()+"^?");
		msg.append(".\n\r");
		if((mob.getClanID()!=null)&&(mob.getClanID().length()>0))
		{
			Clan C=Clans.getClan(mob.getClanID());
			if(C!=null)
			{
				String role=Clans.getRoleName(C.getGovernment(),mob.getClanRole(),true,false);
				role=Util.startWithAorAn(role);
				msg.append("You are "+role+" of the ^H"+mob.getClanID()+"^? Clan.\n\r");
			}
		}
		msg.append("\n\rYour stats are: \n\r^!"+mob.charStats().getStats()+"^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? hit points, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? mana, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? movement.\n\r");
		if(mob.envStats().height()<0)
			msg.append("You are incorporeal, but still weigh "+mob.baseWeight()+" pounds.\n\r");
		else
			msg.append("You are "+mob.envStats().height()+" inches tall and weigh "+mob.baseWeight()+" pounds.\n\r");
		msg.append("You have ^!"+mob.envStats().weight()+"^?/^!"+mob.maxCarry()+"^? pounds of encumbrance.\n\r");
		msg.append("You have ^!"+mob.getPractices()+"^? practices, ^!"+mob.getTrains()+"^? training sessions, and ^H"+mob.getQuestPoint()+"^? quest points.\n\r");
		msg.append("You have scored ^!"+mob.getExperience()+"^? experience points, and have been online for ^!"+Math.round(Util.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
		if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)>0)
		&&(mob.baseEnvStats().level()>CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)))
			msg.append("You are immortal, and will not gain further levels through experience.\n\r");
		else
		if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
			msg.append("You will not gain further levels through experience.\n\r");
		else
			msg.append("You need ^!"+(mob.getExpNeededLevel())+"^? experience points to advance to the next level.\n\r");
		msg.append("Your alignment is      : ^H"+CommonStrings.alignmentStr(mob.getAlignment())+" ("+mob.getAlignment()+")^?.\n\r");
		msg.append("Your armored defense is: ^H"+CommonStrings.armorStr(adjustedArmor)+"^?.\n\r");
		msg.append("Your combat prowess is : ^H"+CommonStrings.fightingProwessStr(adjustedAttack)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");

		if(Sense.isBound(mob))
			msg.append("^!You are bound.^?\n\r");

		if(Sense.isFalling(mob))
			msg.append("^!You are falling!!!^?\n\r");
		else
		if(Sense.isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(Sense.isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(Sense.isSwimming(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(Sense.isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(Sense.isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");

		if(mob.riding()!=null)
			msg.append("^!You are "+mob.riding().stateString(mob)+" "+mob.riding().name()+".^?\n\r");

		if(Sense.isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(Sense.isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");
		if(Sense.isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");

		if(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)
			msg.append("^!You are fatigued.^?\n\r");
		if(mob.curState().getHunger()<1)
			msg.append("^!You are hungry.^?\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("^!You are thirsty.^?\n\r");
		msg.append("\n\r^!You are affected by:^? "+getAffects(mob)+"\n\r");

		return msg;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=getScore(mob);
		if(commands.size()==0)
		{
			commands.addElement(msg);
			return false;
		}
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
