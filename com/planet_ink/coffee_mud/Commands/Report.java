package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Report extends BaseAbleLister
{
	public Report(){}

	private String[] access={"REPORT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			StringBuffer buf=new StringBuffer(
							    "say \"I have "+mob.curState().getHitPoints()
							   +"/"+mob.maxState().getHitPoints()+" hit points, "
							   +mob.curState().getMana()+"/"+mob.maxState().getMana()
							   +" mana, "+mob.curState().getMovement()
							   +"/"+mob.maxState().getMovement()+" move");
			if(mob.getExpNeededLevel()<Integer.MAX_VALUE)
			   buf.append(", and need "+mob.getExpNeededLevel()+" to level");
			buf.append(".\"");
			Command C=CMClass.getCommand("Say");
			if(C!=null) C.execute(mob,Util.parse(buf.toString()));
		}
		else
		{
			int level=parseOutLevel(commands);
			String s=Util.combine(commands,1).toUpperCase();
			if("SPELLS".startsWith(s))
				CommonMsgs.say(mob,null,("^NMy spells:^? "+getAbilities(mob,Ability.SPELL,-1,false,level)),false,false);
			else
			if("SKILLS".startsWith(s))
			{
				Vector V=new Vector();
				V.addElement(new Integer(Ability.THIEF_SKILL));
				V.addElement(new Integer(Ability.SKILL));
				V.addElement(new Integer(Ability.COMMON_SKILL));
				CommonMsgs.say(mob,null,("^NMy skills:^? "+getAbilities(mob,V,-1,false,level)),false,false);
			}
			else
			if("PRAYERS".startsWith(s))
				CommonMsgs.say(mob,null,("^NMy prayers:^? "+getAbilities(mob,Ability.PRAYER,-1,false,level)),false,false);
			else
			if("CHANTS".startsWith(s))
				CommonMsgs.say(mob,null,("^NMy chants:^? "+getAbilities(mob,Ability.CHANT,-1,false,level)),false,false);
			else
			if("SONGS".startsWith(s))
				CommonMsgs.say(mob,null,("^NMy songs:^? "+getAbilities(mob,Ability.SONG,-1,false,level)),false,false);
			else
				mob.tell("'"+s+"' is unknown.  Try SPELLS, SKILLS, PRAYERS, CHANTS, or SONGS.");
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
