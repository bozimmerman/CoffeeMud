package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_WeaponCatch extends StdAbility
{
	public String ID() { return "Fighter_WeaponCatch"; }
	public String name(){ return "Weapon Catch";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("Skill_Disarm"))
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(mob,0,false))
		&&(mob.rangeToTarget()==0))
		{
			FullMsg msg2=new FullMsg(mob,msg.source(),this,CMMsg.MSG_NOISYMOVEMENT,"<T-NAME> disarm(s) <S-NAMESELF>, but <S-NAME> catch(es) the weapon!");
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}
