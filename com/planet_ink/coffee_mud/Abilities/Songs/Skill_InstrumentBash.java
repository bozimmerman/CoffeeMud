package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_InstrumentBash extends StdAbility
{
	public String ID() { return "Skill_InstrumentBash"; }
	public String name(){ return "Instrument Bash";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"INSTRUMENTBASH","IBASH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_InstrumentBash();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Item instrument=Play.getInstrument(mob,-1,true);
		if(instrument==null) return false;

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" must stand up first!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str=auto?"<T-NAME> is bashed!":"^F<S-NAME> bash(es) <T-NAMESELF> with "+instrument.name()+"!^?";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon w=CMClass.getWeapon("ShieldWeapon");
				if((w!=null)&&(instrument!=null))
				{
					w.setName(instrument.name());
					w.setDisplayText(instrument.displayText());
					w.setDescription(instrument.description());
					w.baseEnvStats().setDamage(instrument.envStats().level()+5);
					ExternalPlay.postAttack(mob,target,w);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bash <T-NAMESELF> with "+instrument.name()+", but end(s) up looking silly.");

		return success;
	}

}
