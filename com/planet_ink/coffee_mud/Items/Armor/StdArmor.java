package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;

public class StdArmor extends StdItem implements Armor
{
	public StdArmor()
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
				return new StdArmor();

		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&((!this.amWearingAt(Item.HELD))||(this instanceof Shield)))
		{
			affectableStats.setArmor(affectableStats.armor()-envStats().armor());
			if(this.amWearingAt(Item.ON_TORSO))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*10));
			else
			if((this.amWearingAt(Item.ON_HEAD))||(this.amWearingAt(Item.HELD)))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*5));
			else
				affectableStats.setArmor(affectableStats.armor()-envStats().ability());
		}
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(envStats().ability()>0)
			id=name()+" +"+envStats().ability()+((id.length()>0)?"\n\r":"")+id;
		else
		if(envStats().ability()<0)
			id=name()+" "+envStats().ability()+((id.length()>0)?"\n\r":"")+id;
		return id+"\n\rProtection: "+envStats().armor();
	}
}
