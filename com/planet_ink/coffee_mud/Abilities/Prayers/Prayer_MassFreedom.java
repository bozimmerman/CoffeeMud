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

		FullMsg msg=new FullMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				try
				{
					newMOB.recoverEnvStats();
					A.affectEnvStats(newMOB,newMOB.envStats());
					int clas=A.classificationCode()&Ability.ALL_CODES;
					if((!Sense.aliveAwakeMobile(newMOB,true))
					   ||(Util.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okMessage(newMOB,msg)))
					if((A.invoker()==null)
					||((clas!=Ability.SPELL)&&(clas!=Ability.CHANT)&&(clas!=Ability.PRAYER)&&(clas!=Ability.SONG))
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

		boolean success=profficiencyCheck(mob,0,auto);
		boolean nothingDone=true;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"A feeling of freedom flows through the air":"^S<S-NAME> "+prayWord(mob)+" for freedom, and the area begins to fill with divine glory.^?");
			Room room=mob.location();
			if((room!=null)&&(room.okMessage(mob,msg)))
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
						int old=target.numEffects();
						for(int a=offensiveAffects.size()-1;a>=0;a--)
							((Ability)offensiveAffects.elementAt(a)).unInvoke();
						nothingDone=false;
						if((old>target.numEffects())&&(target.location()!=null))
							target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) less constricted.");
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