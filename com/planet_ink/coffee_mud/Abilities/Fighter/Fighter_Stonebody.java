package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Stonebody extends StdAbility
{
	int regain=-1;
	public String ID() { return "Fighter_Stonebody"; }
	public String name(){ return "Stone Body";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.BENEFICIAL_SELF;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Stonebody();}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean okAffect(Affect affect)
	{
		regain=-1;
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&((affect.targetCode()-Affect.MASK_HURT)>0)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(mob.rangeToTarget()==0)
		&&(profficiencyCheck(-85+mob.charStats().getStat(CharStats.CONSTITUTION),false)))
		{
			int regain=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),2.0));
			if(regain>(affect.targetCode()-Affect.MASK_HURT))
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			else
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-regain,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(regain>0))
		{
			helpProfficiency(mob);
			regain=-1;
		}
	}
}