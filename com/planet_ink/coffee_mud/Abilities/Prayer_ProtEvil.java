package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_ProtEvil extends Prayer
{
	public Prayer_ProtEvil()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protection Evil";

		baseEnvStats().setLevel(4);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtEvil();
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		MOB mob=(MOB)affected;

		if(mob.getAlignment()<350)
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The protective aura around <S-NAME> "+TheFight.hitWord(-1,mob.envStats().level())+" <S-HIS-HER> skin!");
			TheFight.doDamage(mob,mob.envStats().level());
		}
		return super.tick(tickID);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if(affect.target()==invoker)
		{
			if((affect.targetCode()==Affect.STRIKE_MAGIC)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Prayer))
			{
				Prayer bob=(Prayer)affect.tool();
				if((bob.malicious)&&(!bob.isNeutral))
				{
					affect.source().location().show(invoker,null,Affect.VISUAL_WNOISE,"The holy field around <S-NAME> protect(s) <S-HIS-HER> from the evil magic attack of "+affect.source().name()+".");
					return false;
				}
			}

		}
		return true;
	}


	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob.isInCombat())
		{
			MOB victim=mob.getVictim();
			if(victim.getAlignment()<350)
				affectableStats.setArmor(affectableStats.armor()-mob.envStats().level()-10);
		}
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your protection from evil fades.");
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> call(s) upon the protection of <S-HIS-HER> god from evil.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> call(s) upon the protection of <S-HIS-HER> god, but there is no answer.");


		// return whether it worked
		return success;
	}
}
