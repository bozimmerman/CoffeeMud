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
	public int classificationCode(){return Ability.SKILL;}

	protected int DISEASE_TICKS(){return 34;}
	protected int DISEASE_DELAY(){return 5;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your infected wounds feel better.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> infected wounds.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> wince(s) in pain.";}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))	return false;
		if((affected==null)||(invoker==null)) return false;

		MOB mob=(MOB)affected;
		if(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
		{ unInvoke(); return false;}
		if(lastHP<mob.curState().getHitPoints())
			mob.curState().setHitPoints(mob.curState().getHitPoints()
							-((mob.curState().getHitPoints()-lastHP)/2));
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,DISEASE_AFFECT());
			int damage=1;
			ExternalPlay.postDamage(invoker,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_DISEASE,-1,null);
			return true;
		}
		lastHP=mob.curState().getHitPoints();
		return true;
	}
}
