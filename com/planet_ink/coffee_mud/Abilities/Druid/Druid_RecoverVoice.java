package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_RecoverVoice extends StdAbility
{
	public String ID() { return "Druid_RecoverVoice"; }
	public String name(){ return "Recover Voice";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	private static final String[] triggerStrings = {"VRECOVER","RECOVERVOICE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Druid_RecoverVoice();}
	public int classificationCode(){return Ability.SKILL;}


	public static Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				newMOB.recoverEnvStats();
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!Sense.canSpeak(newMOB))
				&&((A.invoker()==null)
				   ||((A.invoker()!=null)
					  &&(A.invoker().envStats().level()<=caster.envStats().level()+10))))
						offenders.addElement(A);
			}
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		Vector offensiveAffects=returnOffensiveAffects(mob,mob);
		if((!success)||(offensiveAffects.size()==0))
			mob.tell("You failed in your vocal meditation.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_GENERAL|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
			}
		}
		return success;
	}
}

