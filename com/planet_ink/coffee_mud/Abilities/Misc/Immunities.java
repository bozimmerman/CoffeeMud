package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_HaveResister;
import java.util.*;

public class Immunities extends StdAbility
{
	public String ID() { return "Immunities"; }
	public String name(){ return "Immunities";}
	private String displayText="";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int resistanceCode=0;
	public Environmental newInstance(){	return new Immunities();}


	public static Object[][] immunityTypes={
		{new Integer(CMMsg.TYP_ACID), new String("ACID")},
		{new Integer(CMMsg.TYP_WATER), new String("WATER")},
		{new Integer(CMMsg.TYP_COLD), new String("COLD")},
		{new Integer(CMMsg.TYP_DISEASE), new String("DISEASE")},
		{new Integer(CMMsg.TYP_ELECTRIC), new String("ELECTRIC")},
		{new Integer(CMMsg.TYP_FIRE), new String("FIRE")},
		{new Integer(CMMsg.TYP_GAS), new String("GAS")},
		{new Integer(CMMsg.TYP_JUSTICE), new String("JUSTICE")},
		{new Integer(CMMsg.TYP_MIND), new String("MIND")},
		{new Integer(CMMsg.TYP_PARALYZE), new String("PARALYZE")},
		{new Integer(CMMsg.TYP_POISON), new String("POISON")},
		{new Integer(CMMsg.TYP_UNDEAD), new String("UNDEAD")},
	};

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)||Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
		&&(!mob.amDead()))
		{
			for(int i=0;i<immunityTypes.length;i++)
				if((msg.targetMinor()==((Integer)immunityTypes[i][0]).intValue())
				&&((text().toUpperCase().indexOf((String)immunityTypes[i][1])>=0)||(text().toUpperCase().equals("ALL"))))
			{
				String immunityName="certain";
				if(msg.tool()!=null)
					immunityName=msg.tool().name();
				if(mob!=msg.source())
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+" attacks from <T-NAME>.");
				else
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+".");
				return false;
			}
		}
		return true;
	}
}

