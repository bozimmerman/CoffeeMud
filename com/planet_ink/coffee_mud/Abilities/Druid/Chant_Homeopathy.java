package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_Homeopathy extends Chant
{
	public String ID() { return "Chant_Homeopathy"; }
	public String name(){ return "Homeopathy";}
	public String displayText(){return "(Homeopathy)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"Something is happening to <T-NAME>!":"^S<S-NAME> chant(s) homeopathically to <T-NAME>^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability D=null;
				for(int t=0;t<target.numEffects();t++)
				{
					Ability A=target.fetchEffect(t);
					if((A!=null)&&(A instanceof DiseaseAffect))
						D=A;
				}
				int roll=Dice.rollPercentage();
				if((roll>66)||(D==null))
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> condition is unchanged.");
				else
				if(roll>33)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> glow(s) a bit.");
					D.unInvoke();
				}
				else
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"Something is definitely happening to <S-NAME>!");
					for(int i=0;i<1000;i++)
						if(!D.tick(target,MudHost.TICK_MOB))
							break;
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");

		return success;
	}
}