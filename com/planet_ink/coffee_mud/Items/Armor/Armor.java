package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;

public class Armor extends StdItem
{
	public Armor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a shirt of armor";
		displayText="a thick armored shirt sits here.";
		description="Thick padded leather with strips of metal interwoven.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=150;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		Random randomizer = new Random(System.currentTimeMillis());
		int armorType = randomizer.nextInt() % 3;
		switch (armorType)
		{
			case 0:
				return new ChainMailArmor();
			case 1:
				return new FullPlate();
			case 2:
				return new LeatherArmor();
			default:
				return new Armor();
				
		}
	}
	
	public String materialDescription(int weaponType)
	{
		switch(material)
		{
		case CLOTH:
			return "CLOTH";
		case METAL:
			return "METAL";
		case LEATHER:
			return "LEATHER";
		case MITHRIL:
			return "MITRIL";
		case WOODEN:
			return "WOODEN";
		}
		return "";
	}
	
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			affectableStats.setArmor(affectableStats.armor()-envStats().armor()-(envStats().ability()*10));
	}
}
