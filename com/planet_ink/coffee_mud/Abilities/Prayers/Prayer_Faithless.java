package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Faithless extends Prayer
{
	public String ID() { return "Prayer_Faithless"; }
	public String name(){ return "Faithless";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_Faithless();}
	protected int overrideMana(){return 100;}
	private String godName="";

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.charStats().getCurrentClass().baseClass().equals("Cleric"))
		{
			mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}
		if(Sense.isAnimalIntelligence(target)||Sense.isGolem(target))
		{
			mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*25),auto);
		Deity D=null;
		if(target.getWorshipCharID().length()>0) D=CMMap.getDeity(target.getWorshipCharID());
		if((success)&&(D!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to lose faith!^?");
			FullMsg msg2=new FullMsg(target,D,this,CMMsg.MSG_REBUKE,"<S-NAME> LOSES FAITH!!!");
			FullMsg msg3=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg3))
			&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg3.value()<=0))
					mob.location().send(mob,msg2);
			}
		}
		else
			maliciousFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}