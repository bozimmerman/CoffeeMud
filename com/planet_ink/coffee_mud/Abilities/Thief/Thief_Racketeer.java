package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Racketeer extends ThiefSkill
{
	public String ID() { return "Thief_Racketeer"; }
	public String name(){ return "Racketeer";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"RACKETEER"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Racketeer();}
	public Vector mobs=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Get protection money from whom?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		if(CoffeeUtensils.getShopKeeper(target)==null)
		{
			mob.tell("You can't get protection money from "+target.displayName()+".");
			return false;
		}
		Ability A=target.fetchAffect(ID());
		if(A!=null)
		{
			if(A.invoker()==mob)
				mob.tell(target.displayName()+" has already been extracted from today.");
			else
			{
				mob.tell(target.displayName()+" is already under "+A.invoker().displayName()+"'s protection.");
				A.invoker().tell("Word on the street is that "+mob.displayName()+" is trying to push into your business with "+target.displayName()+".");
			}
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if(((!target.mayIFight(mob))&&(levelDiff<10)))
		{
			mob.tell("You cannot rob from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int amount=Dice.roll(profficiency(),100,0);
		boolean success=profficiencyCheck(-(levelDiff),auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,(auto?Affect.MASK_GENERAL:0)|Affect.MSG_THIEF_ACT,"<S-NAME> extract(s) "+amount+" gold of protection money from <T-NAME>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,new Long(((Host.TIME_TICK_DELAY*Area.A_FULL_DAY)/Host.TICK_TIME)).intValue());
				Coins C=(Coins)CMClass.getItem("StdCoins");
				C.setNumberOfCoins(amount);
				C.recoverEnvStats();
				mob.location().addItemRefuse(C,Item.REFUSE_PLAYER_DROP);
				ExternalPlay.get(mob,null,C,true);
			}
		}
		else
			maliciousFizzle(mob,target,"<T-NAME> seem(s) unintimidated by <S-NAME>.");
		return success;
	}

}
