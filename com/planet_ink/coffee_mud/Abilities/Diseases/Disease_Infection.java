package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Infection extends Disease
{
	public String ID() { return "Disease_Infection"; }
	public String name(){ return "Infection";}
	public String displayText(){ return "(Infected Wounds)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Infection();}

	protected int DISEASE_TICKS(){return 34;}
	protected int DISEASE_DELAY(){return 5;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your infected wounds feel better.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> infected wounds.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> wince(s) in pain.";}
	public int abilityCode(){return 0;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
		{ unInvoke(); return false;}
		if(lastHP<mob.curState().getHitPoints())
			mob.curState().setHitPoints(mob.curState().getHitPoints()
							-((mob.curState().getHitPoints()-lastHP)/2));
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((getTickDownRemaining()==1)
		&&(!mob.amDead())
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_DISEASE))
		&&(Dice.rollPercentage()<25-mob.charStats().getStat(CharStats.CONSTITUTION)))
		{
			mob.delEffect(this);
			Ability A=CMClass.getAbility("Disease_Gangrene");
			A.invoke(diseaser,mob,true);
		}
		else
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,DISEASE_AFFECT());
			int damage=1;
			MUDFight.postDamage(diseaser,mob,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_DISEASE,-1,null);
			if(Dice.rollPercentage()==1)
			{
				Ability A=CMClass.getAbility("Disease_Fever");
				if(A!=null) A.invoke(diseaser,mob,true);
			}
			return true;
		}
		lastHP=mob.curState().getHitPoints();
		return true;
	}
}
