package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Yawning extends Disease
{
	public String ID() { return "Disease_Yawning"; }
	public String name(){ return "Contageous Yawning";}
	public String displayText(){ return "(Yawning)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Yawning();}

	protected int DISEASE_TICKS(){return 30;}
	protected int DISEASE_DELAY(){return 3;}
	protected String DISEASE_DONE(){return "You stop yawning.";}
	protected String DISEASE_START(){return "^G<S-NAME> feel(s) really tired.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> stretch(es) and yawn(s).";}
	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((getTickDownRemaining()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_DISEASE)))
		{
			mob.delAffect(this);
			Ability A=CMClass.getAbility("Disease_Yawning");
			A.invoke(diseaser,mob,true);
		}
		else
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			Affect msg=new FullMsg(mob,null,this,Affect.MSG_NOISE,DISEASE_AFFECT());
			if((mob.location()!=null)&&(mob.location().okAffect(mob,msg)))
				mob.location().send(mob,msg);
			catchIt(mob);
			return true;
		}
		return true;
	}
}
