package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdDrink;
import java.util.*;

public class LifeFountain extends StdDrink implements MiscMagic
{
	public String ID(){	return "LifeFountain";}
	
	private Hashtable lastDrinks=new Hashtable();
											   
	public LifeFountain()
	{
		super();
		name="a fountain";
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		displayText="a fountain of life flows here.";
		description="The fountain is coming magically from the ground.  The water looks pure and clean.";
		baseGoldValue=10;
		isGettable=false;
		material=EnvResource.RESOURCE_FRESHWATER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new LifeFountain();
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_DRINK:
				if((affect.othersMessage()==null)
				&&(affect.sourceMessage()==null))
					return true;
				break;
			case Affect.TYP_FILL:
				mob.tell("You can't fill the fountain of life.");
				return false;
			default:
				break;
			}
		}
		return super.okAffect(affect);
	}
	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DRINK:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
				{
					Long time=(Long)lastDrinks.get(affect.source());
					if((time==null)||(time.longValue()<(System.currentTimeMillis()-16000)))
					{
						Ability A=CMClass.getAbility("Prayer_CureLight");
						if(A!=null) A.invoke(affect.source(),affect.source(),true);
						A=CMClass.getAbility("Prayer_RemovePoison");
						if(A!=null) A.invoke(affect.source(),affect.source(),true);
						A=CMClass.getAbility("Prayer_CureDisease");
						if(A!=null) A.invoke(affect.source(),affect.source(),true);
						time=new Long(System.currentTimeMillis());
						lastDrinks.put(affect.source(),time);
					}
				}
				else
				{
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
					super.affect(affect);
				}
				break;
			default:
				super.affect(affect);
				break;
			}
		}
		else
			super.affect(affect);
	}

}
