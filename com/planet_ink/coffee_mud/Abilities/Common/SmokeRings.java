package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SmokeRings extends CommonSkill
{
	public String ID() { return "SmokeRings"; }
	public String name(){ return "Smoke Rings";}
	public boolean isAutoInvoked(){return true;}
	private static boolean mapped=false;
	
	public SmokeRings()
	{
		super();
		displayText="";
		canBeUninvoked=false;
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new SmokeRings();}

	public void affect(Environmental affected, Affect msg)
	{
		if(((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		&&(msg.targetMinor()==Affect.TYP_QUIETMOVEMENT)
		&&(msg.target() instanceof Light)
		&&(msg.tool() instanceof Light)
		&&(msg.target()==msg.tool())
		&&(((Light)msg.target()).amWearingAt(Item.ON_MOUTH))
		&&(((Light)msg.target()).isLit())
		&&(profficiencyCheck(0,false)))
		{
			if(Dice.rollPercentage()==1) helpProfficiency((MOB)affected);
			String str="<S-NAME> blow(s) out a perfect smoke ring.";
			switch(Dice.roll(1,10,0))
			{
			case 1:
				str="<S-NAME> blow(s) out a perfect smoke ring.";
				break;
			case 2:
				str="<S-NAME> blow(s) out a swirling string of smoke.";
				break;
			case 3:
				str="<S-NAME> blow(s) out a huge smoke ring.";
				break;
			case 4:
				str="<S-NAME> blow(s) out a train of tiny smoke rings.";
				break;
			case 5:
				str="<S-NAME> blow(s) out a couple of tiny smoke rings.";
				break;
			case 6:
				str="<S-NAME> blow(s) out a nice round smoke ring.";
				break;
			case 7:
				str="<S-NAME> blow(s) out three big smoke rings.";
				break;
			case 8:
				str="<S-NAME> blow(s) out an ENORMOUS smoke ring.";
				break;
			case 9:
				str="<S-NAME> blow(s) out a swirl of tiny smoke rings.";
				break;
			case 10:
				str="<S-NAME> blow(s) out a smoke ring shaped like a galley.";
				break;
			}
			msg.addTrailerMsg(new FullMsg(msg.source(),null,msg.tool(),Affect.MSG_OK_VISUAL,str));
		}
		super.affect(affected,msg);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		return true;
	}
}