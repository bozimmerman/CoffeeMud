package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Gangrene extends Disease
{
	public String ID() { return "Disease_Gangrene"; }
	public String name(){ return "Gangrene";}
	public String displayText(){ return "(Gangrene)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Gangrene();}

	protected int DISEASE_TICKS(){return 100*Host.TICKS_PER_DAY;}
	protected int DISEASE_DELAY(){return 5;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your gangrous wounds feel better.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> gangrous wounds.^?";}
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
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,DISEASE_AFFECT());
			int damage=1;
			ExternalPlay.postDamage(diseaser,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_DISEASE,-1,null);
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
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null) return;
		int days=100-(getTickDownRemaining()/Host.TICKS_PER_DAY);
		if(days>0)
		{
			affectableState.setHitPoints(affectableState.getHitPoints()-(days*(affectableState.getHitPoints()/10)));
			if(affectableState.getHitPoints()<=0)
			{
				MOB diseaser=invoker;
				if(diseaser==null) diseaser=affected;
				ExternalPlay.postDeath(diseaser,affected,null);
			}
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)-4);
		if(affectableStats.getStat(CharStats.CHARISMA)<0)
		affectableStats.setStat(CharStats.CHARISMA,0);
	}
}
