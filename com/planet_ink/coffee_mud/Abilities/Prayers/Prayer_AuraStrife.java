package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AuraStrife extends Prayer
{
	public String ID() { return "Prayer_AuraStrife"; }
	public String name(){ return "Aura of Strife";}
	public String displayText(){ return "(Aura of Strife)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public boolean autoInvocation(MOB mob){return true;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((invoker()!=null)&&(affected!=invoker()))
		{
			int levels=invoker().charStats().getClassLevel("Templar");
			if(levels<0) levels=invoker().envStats().level();
			affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)-(levels/5));
			if(affectableStats.getStat(CharStats.CHARISMA)<=0)
				affectableStats.setStat(CharStats.CHARISMA,1);
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(M!=null)&&(!M.amDead())&&(M.location()!=null))
			M.location().show(M,null,CMMsg.MSG_OK_VISUAL,"The aura of strife around <S-NAME> fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==MudHost.TICK_MOB)
		&&(invoker()!=null)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			HashSet invokerGroup=invoker().getGroupMembers(new HashSet());
			if(mob!=invoker())
			{
				if(mob.location()!=invoker().location())
					unInvoke();
				else
				{
					if(invokerGroup.contains(mob))
						unInvoke();
					else
					if(mob.isInCombat())
					{
						int levels=invoker().charStats().getClassLevel("Templar");
						if(levels<0) levels=invoker().envStats().level();
						if(Dice.rollPercentage()>=levels)
						{
							MOB newvictim=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
							if(newvictim!=mob) mob.setVictim(newvictim);
						}
					}
				}
			}
			else
			if(mob.location()!=null)
			for(int m=0;m<mob.location().numInhabitants();m++)
			{
				MOB M=mob.location().fetchInhabitant(m);
				if((M!=null)&&(M!=invoker())&&(!invokerGroup.contains(M)))
					beneficialAffect(invoker,M,Integer.MAX_VALUE-1000);
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			beneficialAffect(mob,target,0);
			target.recoverEnvStats();
			target.location().recoverRoomStats();
		}
		// return whether it worked
		return success;
	}

}
