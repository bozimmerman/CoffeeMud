package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Fever extends Disease
{
	public String ID() { return "Disease_Fever"; }
	public String name(){ return "Fever";}
	public String displayText(){ return "(Fever)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Fever();}

	protected int DISEASE_TICKS(){return 15;}
	protected int DISEASE_DELAY(){return 3;}
	protected String DISEASE_DONE(){return "You head stops hurting.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a fever.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			MOB newvictim=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if(newvictim!=mob) mob.setVictim(newvictim);
		}
		else
		if(Sense.aliveAwakeMobile(mob,false)
		&&(Sense.canSee(mob))
		&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			switch(Dice.roll(1,10,0))
			{
			case 1: mob.tell("You think you just saw your mother swim by."); break;
			case 2: mob.tell("A pink elephant just attacked you!"); break;
			case 3: mob.tell("A horse just asked you a question."); break;
			case 4: mob.tell("Your hands look very green."); break;
			case 5: mob.tell("You think you just saw your father float by."); break;
			case 6: mob.tell("A large piece of bread swings at you and misses!"); break;
			case 7: mob.tell("Oh, the pretty colors!"); break;
			case 8: mob.tell("You think you just saw something, but aren't sure."); break;
			case 9: mob.tell("Hundreds of little rainbow bees buzz around your head."); break;
			case 10: mob.tell("Everything looks upside-down."); break;
			}
		}
		return super.tick(ticking,tickID);
	}

}