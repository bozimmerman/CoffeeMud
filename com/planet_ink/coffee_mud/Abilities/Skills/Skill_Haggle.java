package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Haggle extends StdAbility
{
	public String ID() { return "Skill_Haggle"; }
	public String name(){ return "Haggle";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"HAGGLE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Haggle();}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+10);
	}
	
	private static MOB shopkeeper(Room here, MOB mob)
	{
		for(int i=0;i<here.numInhabitants();i++)
		{
			MOB thisMOB=here.fetchInhabitant(i);
			if((thisMOB!=null)
			&&(CoffeeUtensils.getShopKeeper(thisMOB)!=null)
			&&(Sense.canBeSeenBy(thisMOB,mob)))
				return thisMOB;
		}
		return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String cmd="";
		if(commands.size()>0)
			cmd=((String)commands.firstElement()).toUpperCase();
	
		if((commands.size()<2)||((!cmd.equals("BUY")&&(!cmd.equals("SELL")))))
		{
			mob.tell("You must specify BUY, SELL, an item, and possibly a ShopKeeper (unless it is implied).");
			return false;
		}

		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell(Util.capitalize(cmd)+" what to whom?");
				return false;
			}
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(CoffeeUtensils.getShopKeeper(shopkeeper)!=null)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' buying anything.");
				return false;
			}
		}
		else
		if(commands.size()<2)
		{
			mob.tell(Util.capitalize(cmd)+" what?");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,shopkeeper,this,Affect.MSG_SPEAK,auto?"":"<S-NAME> haggle(s) with <T-NAMESELF>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				mob.addAffect(this);
				mob.recoverCharStats();
				try{
					ExternalPlay.doCommand(mob,commands);
				}catch(Exception e){Log.errOut("Skill_Haggle",e);}
				mob.delAffect(this);
				mob.recoverCharStats();
			}
		}
		else
			beneficialWordsFizzle(mob,shopkeeper,"<S-NAME> haggle(s) with <T-NAMESELF>, but <S-IS-ARE> unconvincing.");

		// return whether it worked
		return success;
	}
}
