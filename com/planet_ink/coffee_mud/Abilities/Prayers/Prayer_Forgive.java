package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Forgive extends Prayer
{
	public String ID() { return "Prayer_Forgive"; }
	public String name(){return "Forgive";}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_Forgive();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Vector Bs=Sense.flaggedBehaviors(mob.location().getArea(),Behavior.FLAG_LEGALBEHAVIOR);
		Behavior B=null;
		if((Bs!=null)&&(Bs.size()>0)) B=(Behavior)Bs.firstElement();
		
		String name=Util.combine(commands,0);
		if(name.trim().length()==0)
		{
			mob.tell("Forgive whom?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to forgive "+name+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(B!=null)
				{
					Vector V=new Vector();
					V.addElement(new Integer(Law.MOD_FORGIVENAME));
					V.addElement(name);
					B.modifyBehavior(mob.location().getArea(),mob,V);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayForWord(mob)+" to forgive "+name+", but nothing happens.");


		// return whether it worked
		return success;
	}
}