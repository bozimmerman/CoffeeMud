package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Plague extends Disease
{
	public String ID() { return "Disease_Plague"; }
	public String name(){ return "The Plague";}
	public String displayText(){ return "(Plague)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Plague();}

	protected int DISEASE_TICKS(){return 48;}
	protected int DISEASE_DELAY(){return 4;}
	protected String DISEASE_DONE(){return "The sores on your face clear up.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) seriously ill!^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> watch(es) <S-HIS-HER> body erupt with a fresh batch of painful oozing sores!";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,DISEASE_AFFECT());
			int dmg=mob.envStats().level()/2;
			if(dmg<1) dmg=1;
			ExternalPlay.postDamage(invoker,mob,this,dmg,Affect.MASK_GENERAL|Affect.TYP_DISEASE,-1,null);
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,3);
		affectableStats.setStat(CharStats.DEXTERITY,3);
	}
}
