package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Aids extends Disease
{
	public String ID() { return "Disease_Aids"; }
	public String name(){ return "Aids";}
	public String displayText(){ return "(Aids)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Aids();}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your aids goes into remission.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-IS-ARE> sick.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return true;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)-200);
	}

}
