package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantTrap extends Chant implements Trap
{
	public String ID() { return "Chant_PlantTrap"; }
	public String name(){ return "Plant Trap";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}
	public Environmental newInstance(){	return new Chant_PlantTrap();}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{beneficialAffect(mob,E,0); return (Trap)E.fetchEffect(ID());}

	public boolean disabled(){return false;}
	public boolean sprung(){return false;}
	public void disable(){unInvoke();}
	public void spring(MOB M)
	{
		doMyThing(M);
	}

	public static final String[] choices={"Chant_PlantChoke","Chant_PlantConstriction"};
	public void doMyThing(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) some agressive plants!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> <S-IS-ARE> assaulted by the plants!"))
			{
				Vector them=Util.makeVector(choices);
				if(invoker()!=null)
				for(int i=0;i<choices.length;i++)
					if(invoker().fetchAbility(choices[i])==null)
						them.removeElement(choices[i]);
				if(them.size()>0)
				{
					String s=(String)them.elementAt(Dice.roll(1,them.size(),-1));
					Ability A=CMClass.getAbility(s);
					A.invoke(target,target,true);
				}
			}
		}
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(affected)&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!msg.amISource(invoker))
		&&(msg.source().amFollowing()!=invoker))
			spring(msg.source());
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already trapped.");
			return false;
		}
		boolean any=false;
		for(int i=0;i<choices.length;i++)
			if(mob.fetchAbility(choices[i])!=null)
			{ any=true; break;}
		if(!any)
		{
			mob.tell("You must know plant choke or plant constriction for this chant to work.");
			return false;
		}
		
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		&&(!auto))
		{
			mob.tell("This chant does not work here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"This area seems to writh with malicious plants.":"^S<S-NAME> chant(s), stirring the plant life into maliciousness.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but the magic fades.");

		// return whether it worked
		return success;
	}
}
