package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_StripItem extends ThiefSkill
{
	public String ID() { return "Thief_StripItem"; }
	public String name(){ return "Strip Item";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"STRIPITEM"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_StripItem();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Strip what off of whom?");
			return false;
		}
		String itemToSteal=(String)commands.elementAt(0);

		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.mayIFight(mob))&&(levelDiff<10))
		{
			mob.tell("You cannot strip anything off of "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Item stolen=target.fetchWornItem(itemToSteal);
		if((stolen==null)||(!Sense.canBeSeenBy(stolen,mob)))
		{
			mob.tell(target.name()+" doesn't seem to be wearing '"+itemToSteal+"'.");
			return false;
		}
		if(stolen.amWearingAt(Item.WIELD))
		{
			mob.tell(target.name()+" is wielding "+stolen.name()+"! Try disarm!");
			return false;
		}

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?1:2));
		boolean success=profficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to strip "+stolen.name()+" off <T-NAME>; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to strip "+stolen.name()+" off you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to strip "+stolen.name()+" off <T-NAME> and fails!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str=null;
			if(!auto) str="<S-NAME> strip(s) "+stolen.name()+" off <T-NAMESELF>.";

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT | ((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);

			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(((hisStr==null)||mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace();
				}
				msg=new FullMsg(target,stolen,null,CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
					target.location().send(mob,msg);
			}
		}
		return success;
	}
}
