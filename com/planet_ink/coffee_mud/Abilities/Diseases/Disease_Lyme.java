package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Lyme extends Disease
{
	public String ID() { return "Disease_Lyme"; }
	public String name(){ return "Lyme Disease";}
	public String displayText(){ return "(Lyme Disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Lyme();}

	protected int DISEASE_TICKS(){return new Long(9*MudHost.TICKS_PER_MUDDAY).intValue();}
	protected int DISEASE_DELAY(){return new Long(MudHost.TICKS_PER_MUDDAY).intValue();}
	protected String DISEASE_DONE(){return "Your lyme disease goes away.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) lyme disease!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_DAMAGE;}
	int days=0;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(days>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(mob.fetchAbility(msg.tool().ID())==msg.tool())
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+25)))
		{
			mob.tell("Your headaches make you forget "+msg.tool().name()+"!");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if(getTickDownRemaining()==1)
		{
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=mob;
			Ability A=null;
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Fever");
			else
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Amnesia");
			else
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Arthritis");
			else
				A=CMClass.getAbility("Disease_Fever");
			if(A!=null) A.invoke(diseaser,mob,true);
			A=mob.fetchEffect(A.ID());
			if(A!=null) A.makeLongLasting();
		}
		else
		if((--diseaseTick)<=0)
		{
			days++;
			diseaseTick=DISEASE_DELAY();
			if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_DISEASE))
			{
				unInvoke();
				return false;
			}
			return true;
		}
		return true;
	}
}
