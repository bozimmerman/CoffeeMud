package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;

public class MithrilNote extends StdCoins
{
	public MithrilNote()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a mithril note";
		displayText="a small note has been left here";
		description="It's a note worth 100 gold coins!";
		myUses=Integer.MAX_VALUE;
		material=Item.CLOTH;
		baseEnvStats.setWeight(0);
		baseEnvStats.setAbility(100);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new MithrilNote();
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
