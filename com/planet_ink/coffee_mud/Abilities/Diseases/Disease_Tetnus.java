package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Tetnus extends Disease
{
	public String ID() { return "Disease_Tetnus"; }
	public String name(){ return "Tetnus";}
	public String displayText(){ return "(Tetnus)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Tetnus();}

	protected int DISEASE_TICKS(){return new Long(Host.TICKS_PER_DAY*6).intValue();}
	protected int DISEASE_DELAY(){return new Long(Host.TICKS_PER_DAY).intValue();}
	protected String DISEASE_DONE(){return "Your tetnus clears up!";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) ill.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> <S-IS-ARE> getting slower...";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	protected int dexDown=1;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_NOISE,DISEASE_AFFECT());
			dexDown++;
			return true;
		}
		return true;
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(dexDown<0) return;
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-dexDown);
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
		{
			dexDown=-1;
			ExternalPlay.postDeath(invoker(),affected,null);
		}
	}
}