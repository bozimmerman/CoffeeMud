package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_FrameMark extends ThiefSkill
{
	public String ID() { return "Thief_FrameMark"; }
	public String name(){ return "Frame Mark";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"FRAME"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_FrameMark();}
	protected int overrideMana(){return 50;}

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchAffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}
	public int getMarkTicks(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchAffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getMark(mob);
		if(target==null)
		{
			mob.tell("You need to have marked someone before you can frame him or her.");
			return false;
		}

		Behavior B=null;
		if(mob.location()!=null)
		{
			B=mob.location().fetchBehavior("Arrest");
			if(B==null)
				B=mob.location().getArea().fetchBehavior("Arrest");
		}
		if((B==null)
		||(!B.modifyBehavior(mob,null)))
		{
			mob.tell("You aren't wanted for anything here.");
			return false;
		}
		if(mob.getMoney()<(target.envStats().level()*1000))
		{
			mob.tell("You'll need at least "+(target.envStats().level()*1000)+" gold on hand to frame "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=(target.envStats().level()-mob.envStats().level()*15);
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(levelDiff,auto);

		mob.setMoney(mob.getMoney()-(target.envStats().level()*1000));

		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT,"<S-NAME> frame(s) <T-NAMESELF>.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			B.modifyBehavior(mob,target);
		}
		return success;
	}

}