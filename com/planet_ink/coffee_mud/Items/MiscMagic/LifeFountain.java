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
		setName("a fountain");
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		setDisplayText("a fountain of life flows here.");
		setDescription("The fountain is coming magically from the ground.  The water looks pure and clean.");
		baseGoldValue=10;
		isGettable=false;
		material=EnvResource.RESOURCE_FRESHWATER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new LifeFountain();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.othersMessage()==null)
				&&(msg.sourceMessage()==null))
					return true;
				break;
			case CMMsg.TYP_FILL:
				mob.tell("You can't fill the fountain of life.");
				return false;
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					Long time=(Long)lastDrinks.get(msg.source());
					if((time==null)||(time.longValue()<(System.currentTimeMillis()-16000)))
					{
						Ability A=CMClass.getAbility("Prayer_CureLight");
						if(A!=null) A.invoke(msg.source(),msg.source(),true);
						A=CMClass.getAbility("Prayer_RemovePoison");
						if(A!=null) A.invoke(msg.source(),msg.source(),true);
						A=CMClass.getAbility("Prayer_CureDisease");
						if(A!=null) A.invoke(msg.source(),msg.source(),true);
						time=new Long(System.currentTimeMillis());
						lastDrinks.put(msg.source(),time);
					}
				}
				else
				{
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
					super.executeMsg(myHost,msg);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

}
