package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CritStrike extends StdAbility
{
	private int oldDamage=0;

	public Fighter_CritStrike()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Critical Strike";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(9);

		addQualifyingClass("Fighter",9);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_CritStrike();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amISource(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(profficiencyCheck(-90+mob.charStats().getStrength(),false)))
		{
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			int bonus=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),pctRecovery));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+bonus,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			helpProfficiency(mob);
		}
		return true;
	}
	
}
