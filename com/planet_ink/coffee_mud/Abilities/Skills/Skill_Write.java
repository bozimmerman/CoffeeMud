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
	public int overrideMana(){return 0;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}

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
			if((target!=null)&&(Sense.isGettable(target)))
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
		if((item==null)
		||((item.material()!=EnvResource.RESOURCE_PAPER)
		   &&(item.material()!=EnvResource.RESOURCE_SILK)
		   &&(item.material()!=EnvResource.RESOURCE_HEMP))
		||((item!=null)&&(!Sense.isReadable(item))))
		{
			mob.tell("You can't write on that.");
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell("You can't write on a scroll.");
			return false;
		}

		if(Util.combine(commands,1).toUpperCase().startsWith("FILE="))
		{
			mob.tell("You can't write that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_WRITE,"<S-NAME> write(s) on <T-NAMESELF>.",CMMsg.MSG_WRITE,Util.combine(commands,1),CMMsg.MSG_WRITE,"<S-NAME> write(s) on <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<S-NAME> attempt(s) to write on <T-NAMESELF>, but mess(es) up.");
		return success;
	}

}