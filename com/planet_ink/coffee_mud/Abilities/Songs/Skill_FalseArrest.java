package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_FalseArrest extends StdAbility
{
	public String ID() { return "Skill_FalseArrest"; }
	public String name(){ return "False Arrest";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"ARREST"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Skill_FalseArrest();}
	protected int overrideMana(){return 50;}

	public Behavior getArrest(Area A)
	{
		if(A==null) return null;
		Vector V=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()==0) return null;
		return (Behavior)V.firstElement();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob==target)
		{
			mob.tell("Arrest whom?!");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		Behavior B=null;
		Area A=null;
		if(mob.location()!=null)
		{
			B=getArrest(mob.location().getArea());
			if((B==null)||(!B.modifyBehavior(mob.location().getArea(),target,new Integer(6))))
				B=null;
		}

		if(B==null)
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			A=(Area)e.nextElement();
			if(Sense.canAccess(mob,A))
			{
				B=getArrest(A);
				if((B!=null)&&(B.modifyBehavior(A,target,new Integer(6))))
					break;
			}
			B=null;
			A=null;
		}

		if(B==null)
		{
			mob.tell(target.name()+" is not wanted for anything, anywhere.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(!success)
		{
			beneficialWordsFizzle(mob,target,"<S-NAME> frown(s) at <T-NAMESELF>, but lose(s) the nerve.");
			return false;
		}
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT,"<S-NAME> frown(s) at <T-NAMESELF>.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			Vector V=new Vector();
			V.addElement(new Integer(1));
			V.addElement(mob);
			if(!B.modifyBehavior(A,target,V))
			{
				mob.tell("You are not able to arrest "+target.name()+" at this time.");
				return false;
			}
		}
		return success;
	}

}