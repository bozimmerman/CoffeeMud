package com.planet_ink.coffee_mud.Items.MiscMagic;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Ring_Ornamental;
import java.util.*;


public class Ring_Protection extends Ring_Ornamental implements MiscMagic
{
	public Ring_Protection()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		setIdentity();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Ring_Protection();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			affectableStats.setArmor(affectableStats.armor()-envStats().armor()-envStats().ability());
	}

	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		setIdentity();
	}

	private int correctTargetMinor()
	{
		switch(this.envStats().level())
		{
			case SILVER_RING:
				return Affect.TYP_COLD;
			case COPPER_RING:
				return Affect.TYP_ELECTRIC;
			case PLATINUM_RING:
				return Affect.TYP_GAS;
			case GOLD_RING_DIAMOND:
				return Affect.TYP_FIRE;
			case GOLD_RING:
				return Affect.TYP_ACID;
			case GOLD_RING_RUBY:
				return Affect.TYP_MIND;
			case BRONZE_RING:
				return Affect.TYP_PARALYZE;
			case GOLD_RING_OPAL:
				return Affect.TYP_CAST_SPELL;
			case GOLD_RING_TOPAZ:
				return -99;
			case GOLD_RING_SAPPHIRE:
				return Affect.TYP_JUSTICE;
			case MITHRIL_RING:
				return Affect.TYP_WEAPONATTACK;
			case GOLD_RING_PEARL:
				return Affect.TYP_WATER;
			case GOLD_RING_EMERALD:
				return -99;
			default:
				return -99;

		}
	}

	private boolean rollChance()
	{
		switch(this.envStats().level())
		{
			case GOLD_RING_OPAL:
				if(Math.random()>.15)
					return false;
				else
					return true;
			case GOLD_RING_SAPPHIRE:
				if(Math.random()>.15)
					return false;
				else
					return true;
			case MITHRIL_RING:
				if(Math.random()>.05)
					return false;
				else
					return true;
			default:
				return true;

		}
	}

	private void setIdentity()
	{
		baseEnvStats().setAbility(0);
		switch(this.envStats().level())
		{
			case SILVER_RING:
				secretIdentity="The ring of Seven Winters. (Protection from Cold)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_SILVER;
				break;
			case COPPER_RING:
				secretIdentity="The ring of Storms. (Protection from Electricity)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_COPPER;
				break;
			case PLATINUM_RING:
				secretIdentity="The ring of Sweet Air. (Protection from Gas)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_PLATINUM;
				break;
			case GOLD_RING_DIAMOND:
				secretIdentity="The ring of the Eternal Blaze. (Protection from Fire)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_GOLD;
				break;
			case BRONZE_RING:
				secretIdentity="The ring of the Bronze Shield. (Protection from Paralysis)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_BRONZE;
				break;
			case GOLD_RING:
				secretIdentity="The ring of Sweet Water. (Protection from Acid)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_GOLD;
				break;
			case GOLD_RING_RUBY:
				secretIdentity="The ring of Focus. (Protection from Mind Attacks)";
				baseGoldValue+=5000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_OPAL:
				secretIdentity="Mages Bane. (15% Resistance to Magic)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_TOPAZ:
				baseEnvStats().setAbility(60);
				secretIdentity="Zimmers Guard. (Ring of Protection +60)";
				baseGoldValue+=6000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_SAPPHIRE:
				secretIdentity="The ring of Justice. (15% Resistance to Criminal Behavior)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case MITHRIL_RING:
				secretIdentity="The ring of Fortitude. (5% Resistance to physical attacks)";
				baseGoldValue+=2000;
				material=EnvResource.RESOURCE_MITHRIL;
				break;
			case GOLD_RING_PEARL:
				secretIdentity="The ring of the Wave. (Protection from Water Attacks)";
				baseGoldValue+=1000;
				material=EnvResource.RESOURCE_PEARL;
				break;
			case GOLD_RING_EMERALD:
				baseEnvStats().setAbility(50);
				secretIdentity="Fox Guard. (Ring of Protection +50)";
				baseGoldValue+=5000;
				material=EnvResource.RESOURCE_GEM;
				break;
			default:
				double pct=Math.random();
				baseEnvStats().setAbility((int)Math.round(pct*49));
				baseGoldValue+=baseEnvStats().ability()*100;
				secretIdentity="A ring of protection + "+baseEnvStats().ability()+".";
				material=EnvResource.RESOURCE_STEEL;
				break;
		}
	}

	public void affect(Affect affect)
	{
		if((affect.target()==null)||(!(affect.target() instanceof MOB)))
			return ;

		MOB mob=(MOB)affect.target();
		if(mob!=this.owner()) return;

		if((affect.targetMinor()==correctTargetMinor())
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(!this.amWearingAt(Item.INVENTORY))
		&&(mob.isMine(this))
		&&(rollChance()))
			CommonStrings.resistanceMsgs(affect,affect.source(),mob);
		return ;
	}
}
