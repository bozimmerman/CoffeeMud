package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Fragmentation extends StdAbility
{
	public String ID() { return "Fighter_Fragmentation"; }
	public String name(){ return "Fragmentation";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.target() instanceof Weapon)
		&&(msg.tool()==this))
			((Weapon)msg.target()).destroy();
		super.executeMsg(myHost,msg);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(msg.value()>0)
		&&(msg.target() instanceof MOB)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		{
			if(Dice.rollPercentage()<25) helpProfficiency((MOB)affected);
			msg.addTrailerMsg(new FullMsg((MOB)msg.target(),msg.tool(),this,CMMsg.MSG_OK_VISUAL,"<T-NAME> fragment(s) in <S-NAME>!"));
			msg.setValue(msg.value()+(2*(int)Math.round(Util.mul(msg.value(),Util.div(profficiency(),100.0)))));
		}

		return super.okMessage(myHost,msg);
	}

}