package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Lepresy extends Disease
{
	public String ID() { return "Disease_Lepresy"; }
	public String name(){ return "Leprosy";}
	public String displayText(){ return "(Leprosy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Lepresy();}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 10;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your leprosy is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) pale!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}

	private static String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
			return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+str.substring(replace+8);
		return str;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.targetMessage()!=null))
		{
			if(msg.targetMessage().indexOf("<DAMAGE>")>=0)
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),replaceDamageTag(msg.targetMessage(),1,0),
						  msg.othersCode(),msg.othersMessage());
			else
			if((msg.tool()!=null)&&(msg.tool() instanceof Weapon))
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),"^F"+((Weapon)msg.tool()).hitString(1)+"^?",
						  msg.othersCode(),msg.othersMessage());
		}
		return super.okMessage(myHost,msg);
	}

}
