package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Write extends StdAbility
{
	public String ID() { return "Skill_Write"; }
	public String name(){ return "Write";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"WRITE","WR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	protected int trainsRequired(){return 0;}
	protected int practicesRequired(){return 3;}
	public Environmental newInstance(){	return new Skill_Write();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.charStats().getStat(CharStats.INTELLIGENCE)<5)
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
		if(target==null)
		{
			target=mob.location().fetchItem(null,(String)commands.elementAt(0));
			if((target!=null)&&(target.isGettable()))
			{
				mob.tell("You don't have that.");
				return false;
			}
		}
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_WRITE,"<S-NAME> write(s) on <T-NAMESELF>.",Affect.MSG_WRITE,Util.combine(commands,1),Affect.MSG_WRITE,"<S-NAME> write(s) on <T-NAMESELF>.");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<S-NAME> attempt(s) to write on <T-NAMESELF>, but mess(es) up.");
		return success;
	}

}