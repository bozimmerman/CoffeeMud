package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Slapstick extends BardSkill
{
	public String ID() { return "Skill_Slapstick"; }
	public String name(){ return "Slapstick";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SLAPSTICK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Slapstick();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			if(auto) str="<T-NAME> is drained of mana!";
			else
			switch(Dice.roll(1,10,0))
			{
			case 1:
				str="<S-NAME> stand(s) on <S-HIS-HER> head and stick(s) <S-HIS-HER> tounge out at <T-NAMESELF>.";
				break;
			case 2:
				str="<S-NAME> make(s) a silly face at <T-NAMESELF> and gyrate(s).";
				break;
			case 3:
				str="<S-NAME> do(es) the monkey dance with <T-NAMESELF>.";
				break;
			case 4:
				str="<S-NAME> trip(s) on <T-YOUPOSS> foot, fall(s) on <S-HIS-HER> back, and bounce(s) back up.";
				break;
			case 5:
				str="<S-NAME> smile(s) at <T-NAMESELF> as <S-HIS-HER> drawers drop.";
				break;
			case 6:
				str="<S-NAME> run(s) behind <T-NAMESELF>, throw(s) a pie in the air, and catch(es) it on <S-HIS-HER> face.";
				break;
			case 7:
				str="<S-NAME> feign(s) an inability to pull something from <S-HIS-HER> nose, looking to <T-NAMESELF> in distress.";
				break;
			case 8:
				str="<S-NAME> look(s) at <T-NAMESELF> as <S-HIS-HER> hands get into a silly fight with each other.";
				break;
			case 9:
				str="<S-NAME> turn(s) <S-HIS-HER> back to <T-NAMESELF>, tap(s) <S-HIM-HERSELF> on the shoulder with <T-YOUPOSS> hand, and then feign(s) ignorance about the source.";
				break;
			case 10:
				str="<S-NAME> do(es) a silly slapstick routine for <T-NAMESELF>.";
				break;
			}
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_SOUND|CMMsg.MASK_HANDS|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().adjMana(-mob.envStats().level(),target.maxState());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to something silly to <T-NAMESELF>, but fail(s).");

		return success;
	}

}
