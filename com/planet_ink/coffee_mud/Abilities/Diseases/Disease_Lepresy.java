package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Lepresy extends Disease
{
	public String ID() { return "Disease_Lepresy"; }
	public String name(){ return "Lepresy";}
	public String displayText(){ return "(Lepresy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Lepresy();}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 10;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your lepresy is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) pale!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int spreadCode(){return 0;}

	private static String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
			return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+str.substring(replace+8);
		return str;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.targetMessage()!=null))
		{
			if(affect.targetMessage().indexOf("<DAMAGE>")>=0)
			affect.modify(affect.source(),
						  affect.target(),
						  affect.tool(),
						  affect.sourceCode(),affect.sourceMessage(),
						  affect.targetCode(),replaceDamageTag(affect.targetMessage(),1,0),
						  affect.othersCode(),affect.othersMessage());
			else
			if((affect.tool()!=null)&&(affect.tool() instanceof Weapon))
			affect.modify(affect.source(),
						  affect.target(),
						  affect.tool(),
						  affect.sourceCode(),affect.sourceMessage(),
						  affect.targetCode(),"^F"+((Weapon)affect.tool()).hitString(1)+"^?",
						  affect.othersCode(),affect.othersMessage());
		}
		return super.okAffect(myHost,affect);
	}

}
