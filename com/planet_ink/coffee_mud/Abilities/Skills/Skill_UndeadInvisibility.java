package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_UndeadInvisibility extends StdAbility
{
	public String ID() { return "Skill_UndeadInvisibility"; }
	public String name(){ return "Undead Invisibility";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			   &&(msg.source().charStats().getMyRace().racialCategory().equals("Undead"))
			   &&(msg.source().getVictim()!=target))
			{
				msg.source().tell("You don't see "+target.name());
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
					helpProfficiency((MOB)affected);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}