package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class ScrollSpell extends StdScroll
{
	public String ID(){	return "ScrollSpell";}
	public ScrollSpell()
	{
		super();
		this.setUsesRemaining(2);
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_PAPER;
	}

	public Environmental newInstance()
	{
		return new ScrollSpell();
	}

	public void setMiscText(String newText)
	{
		miscText=newText;
		this.parseSpells(this,miscText);
	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity()," Charges: "+usesRemaining(),getSpells());
	}
}
