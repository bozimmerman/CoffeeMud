package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Convert extends StdAbility
{
	public String ID() { return "Skill_Convert"; }
	public String name(){ return "Convert";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CONVERT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Convert();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("You must specify either a deity to convert yourself to, or a player to convert to your religeon.");
			return false;
		}

		MOB target=mob;
		Deity D=CMMap.getDeity(Util.combine(commands,0));
		if(D==null)
		{
			D=mob.getMyDeity();
			target=getTarget(mob,commands,givenTarget);
			if(target==null)
			{
				mob.tell("You've also never heard of a deity called '"+Util.combine(commands,0)+"'.");
				return false;
			}
			if(D==null)
			{
				mob.tell("A faithless one cannot convert "+target.displayName()+".");
				return false;
			}
		}
		if(target.isMonster())
		{
			mob.tell("You can't convert "+target.displayName()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(target!=mob)
			{
				if(target.getMyDeity()!=null)
				{
					mob.tell(target.displayName()+" is worshiping "+target.getMyDeity().displayName()+".  "+target.charStats().HeShe()+" must REBUKE "+target.getMyDeity().charStats().himher()+" first.");
					return false;
				}
				if(target.getMyDeity()==D)
				{
					mob.tell(target.displayName()+" already worships "+D.displayName()+".");
					return false;
				}
				try
				{
					if(!target.session().confirm(mob.displayName()+" is trying to convert you to the worship of "+D.displayName()+".  Is this what you want (N/y)?","N"))
					{
						mob.location().show(mob,target,Affect.MSG_SPEAK,"<S-YOUPOSS> attempt to convert <T-NAME> to the worship of "+D.displayName()+" is rejected.");
						return false;
					}
				}
				catch(Exception e)
				{
					return false;
				}
			}
			Room dRoom=D.location();
			if(dRoom==mob.location()) dRoom=null;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,auto?"<S-NAME> <S-IS-ARE> converted!":"<S-NAME> convert(s) <T-NAMESELF> to the worship of "+D.displayName()+".");
			FullMsg msg2=new FullMsg(target,D,this,Affect.MSG_SERVE,null);
			if((mob.location().okAffect(mob,msg))
			   &&(mob.location().okAffect(mob,msg2))
			   &&((dRoom==null)||(dRoom.okAffect(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(target,msg2);
				if(dRoom!=null)
					dRoom.send(target,msg2);
				if(mob!=target)
				{
					mob.tell("You gain 200 experience points.");
					mob.charStats().getCurrentClass().gainExperience(mob,null,null,200);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to convert <T-NAMESELF>, but <S-IS-ARE> unconvincing.");

		// return whether it worked
		return success;
	}
}
