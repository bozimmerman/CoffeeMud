package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Roll extends StdAbility
{
	int regain=-1;

	public Fighter_Roll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Roll With Blows";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(16);

		addQualifyingClass("Fighter",16);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Roll();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

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
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(profficiencyCheck(-85+mob.charStats().getDexterity(),false)))
		{
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			regain=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),pctRecovery));
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
			affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> roll(s) with the hit."));
			helpProfficiency(mob);
			regain=-1;
		}
	}
}