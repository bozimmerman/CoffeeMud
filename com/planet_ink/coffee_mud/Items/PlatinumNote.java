package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;

public class PlatinumNote extends StdCoins
{
	public PlatinumNote()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a platinum note";
		displayText="a small note has been left here";
		description="It's worth 10 gold coins!";
		myUses=Integer.MAX_VALUE;
		material=Item.METAL;
		baseEnvStats.setWeight(0);
		baseEnvStats.setAbility(10);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new PlatinumNote();
	}
	public String name()
	{
		return name;
	}
	public String displayText()
	{
		return displayText;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		goldValue=envStats().ability();
	}
}
