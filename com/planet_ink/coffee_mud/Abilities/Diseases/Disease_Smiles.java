package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Smiles extends Disease
{
	public String ID() { return "Disease_Smiles"; }
	public String name(){ return "Contageous Smiles";}
	public String displayText(){ return "(The Smiles)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Smiles();}

	protected int DISEASE_TICKS(){return 10;}
	protected int DISEASE_DELAY(){return 2;}
	protected String DISEASE_DONE(){return "You feel more serious.";}
	protected String DISEASE_START(){return "^G<S-NAME> start(s) smiling.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> smile(s) happily.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((getTickDownRemaining()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_DISEASE)))
		{
			mob.delAffect(this);
			Ability A=CMClass.getAbility("Disease_Giggles");
			A.invoke(invoker,mob,true);
		}
		else
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			Affect msg=new FullMsg(mob,null,this,Affect.MSG_QUIETMOVEMENT,DISEASE_AFFECT());
			if((mob.location()!=null)&&(mob.location().okAffect(mob,msg)))
				mob.location().send(mob,msg);
			catchIt(mob);
			return true;
		}
		return true;
	}
}
