package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DivineResistance extends Prayer
{
	public String ID() { return "Prayer_DivineResistance"; }
	public String name(){ return "Divine Resistance";}
	public String displayText(){ return "(Divine Resistance)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_DivineResistance();}
	private HashSet permProts=new HashSet();
	private int prots=4;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;

		affectableStats.setStat(CharStats.SAVE_WATER,100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,100);
		affectableStats.setStat(CharStats.SAVE_TRAPS,100);
		affectableStats.setStat(CharStats.SAVE_POISON,100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,100);
		affectableStats.setStat(CharStats.SAVE_MIND,100);
		affectableStats.setStat(CharStats.SAVE_MAGIC,100);
		affectableStats.setStat(CharStats.SAVE_JUSTICE,100);
		affectableStats.setStat(CharStats.SAVE_GENERAL,100);
		affectableStats.setStat(CharStats.SAVE_GAS,100);
		affectableStats.setStat(CharStats.SAVE_FIRE,100);
		affectableStats.setStat(CharStats.SAVE_ELECTRIC,100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,100);
		affectableStats.setStat(CharStats.SAVE_COLD,100);
		affectableStats.setStat(CharStats.SAVE_ACID,100);
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		
		if((msg.target()==affected)
		&&(affected instanceof MOB)
		&&((msg.tool()==null)||(!permProts.contains(msg.tool())))
		&&(prots>0)
		&&(msg.source().location()!=null))
		{
			boolean proceed=false;
			int sm=msg.sourceMinor();
			int tm=msg.targetMinor();
			for(int i=0;i<CharStats.affectTypeMap.length;i++)
				if((CharStats.affectTypeMap[i]>=0)
				&&((sm==CharStats.affectTypeMap[i])||(tm==CharStats.affectTypeMap[i])))
					proceed=true;
			if((msg.tool() instanceof Trap)||(proceed))
			{
				if(msg.tool()!=null)
					permProts.add(msg.tool());
				prots--;
				msg.source().location().show((MOB)msg.target(),msg.source(),this,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> divine protection glows!");
				if(prots==0)
					unInvoke();
			}
		}
		return super.okMessage(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> divine resistance fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<T-NAME> become(s) protected by divine resistance.":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to be protected by divine resistance.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				target.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to have divine resistance, but nothing happens.");


		// return whether it worked
		return success;
	}
}
