package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_MassFreedom extends Prayer
{
	public Prayer_MassFreedom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Freedom";

		baseEnvStats().setLevel(20);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassFreedom();
	}

	public Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=new StdMOB();
		Vector offenders=new Vector();

		FullMsg msg=new FullMsg(newMOB,null,null,Affect.MOVE_SIT,"blah",Affect.MOVE_SIT,"blah",Affect.MOVE_SIT,"blah");
		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			newMOB.recoverEnvStats();
			A.affectEnvStats(newMOB,newMOB.envStats());
			if((!Sense.canMove(newMOB))
			   ||(!Sense.canPerformAction(newMOB))
			   ||(!A.okAffect(msg)))
			if((A.invoker()==null)
			   ||((A.invoker()!=null)
				  &&(A.invoker().envStats().level()<caster.envStats().level())))
					offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		boolean nothingDone=true;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) for freedom, and the area begins to fill with divine glory.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB target=mob.location().fetchInhabitant(i);
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
						if(old>target.numAffects())
							target.tell("You feel less constricted!");
					}
				}
			}
		}

		if(nothingDone)
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) for freedom, but nothing happens.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}


		// return whether it worked
		return success;
	}
}