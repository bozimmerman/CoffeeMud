package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_ShieldBlock extends StdAbility
{
	public int hits=0;
	public String ID() { return "Fighter_ShieldBlock"; }
	public String name(){ return "Shield Block";}
	public String displayText(){return "";};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.SKILL; }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
		&&(mob.fetchFirstWornItem(Item.HELD) instanceof Shield)
		&&(msg.source().getVictim()==mob))
		{
			FullMsg msg2=new FullMsg(msg.source(),mob,mob.fetchFirstWornItem(Item.HELD),CMMsg.MSG_QUIETMOVEMENT,"<T-NAME> block(s) <S-YOUPOSS> attack with <O-NAME>!");
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