package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Fighter_Roll extends StdAbility
{
	int oldHP=0;

	public Fighter_Roll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Roll With Blows";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(10);

		addQualifyingClass(new Fighter().ID(),10);
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
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				switch(affect.targetCode())
				{
				case Affect.STRIKE_HANDS:
					oldHP=mob.curState().getHitPoints();
					break;
				}
				break;

			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				switch(affect.targetCode())
				{
				case Affect.STRIKE_HANDS:
					if((oldHP>mob.curState().getHitPoints())
					&&(profficiencyCheck(0)))
					{
						int recovery=mob.curState().getHitPoints()-oldHP;
						double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
						recovery=(int)Math.round(Util.mul(recovery,pctRecovery));
						if(recovery>=0)
						{
							mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> roll(s) with the hit.");
							mob.curState().adjHitPoints(recovery,mob.maxState());
							helpProfficiency(mob);
						}
					}

					break;
				}
				break;
			}
		}
	}
}