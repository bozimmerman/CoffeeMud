package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassFreedom extends Prayer
{
	public String ID() { return "Prayer_MassFreedom"; }
	public String name(){ return "Mass Freedom";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_MassFreedom();}

	public Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
		Vector offenders=new Vector();

		FullMsg msg=new FullMsg(newMOB,null,null,Affect.MSG_SIT,null);
		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			if(A!=null)
			{
				try
				{
					newMOB.recoverEnvStats();
					A.affectEnvStats(newMOB,newMOB.envStats());
					if((!Sense.aliveAwakeMobile(newMOB,true))
					   ||(Util.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okAffect(newMOB,msg)))
					if((A.invoker()==null)
					   ||((A.invoker()!=null)
						  &&(A.invoker().envStats().level()<=caster.envStats().level()+1)))
							offenders.addElement(A);
				}
				catch(Exception e)
				{}
			}
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		boolean nothingDone=true;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"A feeling of freedom flows through the air":"^S<S-NAME> "+prayWord(mob)+" for freedom, and the area begins to fill with divine glory.^?");
			Room room=mob.location();
			if((room!=null)&&(room.okAffect(mob,msg)))
			{
				room.send(mob,msg);
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);
					if(target==null) break;

					Vector offensiveAffects=returnOffensiveAffects(mob,target);

					if(offensiveAffects.size()>0)
					{
						// it worked, so build a copy of this ability,
						// and add it to the affects list of the
						// affected MOB.  Then tell everyone else
						// what happened.
						int old=target.numAffects();
						for(int a=offensiveAffects.size()-1;a>=0;a--)
							((Ability)offensiveAffects.elementAt(a)).unInvoke();
						nothingDone=false;
						if((old>target.numAffects())&&(target.location()!=null))
							target.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> seem(s) less constricted.");
					}
				}
			}
		}
		else
			this.beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for freedom, but nothing happens.");

		// return whether it worked
		return success;
	}
}