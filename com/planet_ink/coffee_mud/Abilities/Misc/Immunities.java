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
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		String text=text().toUpperCase();
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)
			||Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetMinor()==Affect.TYP_ACID)&&((text.indexOf("ACID")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_WATER)&&((text.indexOf("WATER")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_COLD)&&((text.indexOf("COLD")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_DEATH)&&((text.indexOf("DEATH")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_DISEASE)&&((text.indexOf("DISEASE")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_ELECTRIC)&&((text.indexOf("ELECTRIC")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_FIRE)&&((text.indexOf("FIRE")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_GAS)&&((text.indexOf("GAS")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_JUSTICE)&&((text.indexOf("JUSTICE")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_MIND)&&((text.indexOf("MIND")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_PARALYZE)&&((text.indexOf("PARALYZE")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_POISON)&&((text.indexOf("POISON")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_UNDEAD)&&((text.indexOf("UNDEAD")>=0)||(text.equals("ALL"))))
		&&((affect.targetMinor()==Affect.TYP_WEAPONATTACK)&&((text.indexOf("ACID")>=0)||(text.equals("ALL"))))
		&&(!mob.amDead()))
		{
			String immunityName="certain";
			if(affect.tool()!=null)
				immunityName=affect.tool().displayName();
			mob.location().show(mob,affect.source(),Affect.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+" attacks from <T-NAME>.");
			return false;
		}
		return true;
	}

}
