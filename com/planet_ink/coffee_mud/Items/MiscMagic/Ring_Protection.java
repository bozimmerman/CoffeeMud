package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.Ring_Ornamental;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Ring_Protection extends Ring_Ornamental implements MiscMagic
{
	public String ID(){	return "Ring_Protection";}
	private int lastLevel=-1;
	
	public Ring_Protection()
	{
		super();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			affectableStats.setArmor(affectableStats.armor()-envStats().armor()-envStats().ability());
	}

	public void recoverEnvStats()
	{
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		super.recoverEnvStats();
		if(lastLevel!=baseEnvStats().level())
		{ lastLevel=baseEnvStats().level(); setIdentity();}
	}

	private int correctTargetMinor()
	{
		switch(this.envStats().level())
		{
			case SILVER_RING:
				return CMMsg.TYP_COLD;
			case COPPER_RING:
				return CMMsg.TYP_ELECTRIC;
			case PLATINUM_RING:
				return CMMsg.TYP_GAS;
			case GOLD_RING_DIAMOND:
				return CMMsg.TYP_FIRE;
			case GOLD_RING:
				return CMMsg.TYP_ACID;
			case GOLD_RING_RUBY:
				return CMMsg.TYP_MIND;
			case BRONZE_RING:
				return CMMsg.TYP_PARALYZE;
			case GOLD_RING_OPAL:
				return CMMsg.TYP_CAST_SPELL;
			case GOLD_RING_TOPAZ:
				return -99;
			case GOLD_RING_SAPPHIRE:
				return CMMsg.TYP_JUSTICE;
			case MITHRIL_RING:
				return CMMsg.TYP_WEAPONATTACK;
			case GOLD_RING_PEARL:
				return CMMsg.TYP_WATER;
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
			    if(baseEnvStats().ability()==0)
					baseEnvStats().setAbility(50);
				secretIdentity="Fox Guard. (Ring of Protection +50)";
				baseGoldValue+=5000;
				material=EnvResource.RESOURCE_GEM;
				break;
			default:
				double pct=Math.random();
			    if(baseEnvStats().ability()==0)
					baseEnvStats().setAbility((int)Math.round(pct*49));
				baseGoldValue+=baseEnvStats().ability()*100;
				secretIdentity="A ring of protection + "+baseEnvStats().ability()+".";
				material=EnvResource.RESOURCE_STEEL;
				break;
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB)))
			return ;

		MOB mob=(MOB)msg.target();
		if(mob!=this.owner()) return;

		if((msg.targetMinor()==correctTargetMinor())
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(!this.amWearingAt(Item.INVENTORY))
		&&(mob.isMine(this))
		&&(rollChance()))
			CommonStrings.resistanceMsgs(msg,msg.source(),mob);
	}
}
