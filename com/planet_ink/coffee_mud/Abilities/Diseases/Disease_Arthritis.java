package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Arthritis extends Disease
{
	public String ID() { return "Disease_Arthritis"; }
	public String name(){ return "Arthritis";}
	public String displayText(){ return "(Arthritis)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Arthritis();}
	public int classificationCode(){return Ability.SKILL;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your arthritis clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-IS-ARE> in pain.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-3);
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}

}
