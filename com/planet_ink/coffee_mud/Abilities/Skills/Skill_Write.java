package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Write extends StdAbility
{

	public Skill_Write()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Write";
		displayText="(in the mystical realm of magic)";
		miscText="";

		triggerStrings.addElement("WRITE");
		triggerStrings.addElement("WR");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Write();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.charStats().getIntelligence()<8)
		{
			mob.tell("You are too stupid to actually write anything.");
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell("What would you like to write on?");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}

		Item item=target;
		if((item==null)||((item!=null)&&(!item.isReadable())))
		{
			mob.tell("You can't write on that.");
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell("You can't write on a scroll.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_DELICATE_HANDS_ACT,"<S-NAME> write(s) on <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,target,null,Affect.MSG_WRITE,null,Affect.MSG_WRITE,Util.combine(commands,1),Affect.MSG_WRITE,null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
			}
		}
		else
			mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<S-NAME> attempt(s) to write on <T-NAMESELF>, but mess(es) up.");
		return success;
	}

}