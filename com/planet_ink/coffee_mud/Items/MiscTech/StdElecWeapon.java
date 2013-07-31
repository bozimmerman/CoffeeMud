package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;


/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class StdElecWeapon extends StdElecItem implements Weapon
{
	public String ID(){    return "StdElecWeapon";}
	protected int     weaponType=Weapon.TYPE_SHOOT;
	protected int     weaponClassification=Weapon.CLASS_RANGED;
	protected boolean useExtendedMissString=false;
	protected int     minRange=0;
	protected int     maxRange=10;

	public StdElecWeapon()
	{
		super();

		setName("a tech gun");
		setDisplayText("a tech gun sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=0;
		wornLogicalAnd=false;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		activated=true;
		baseGoldValue=15;
		minRange=0;
		maxRange=10;
		material=RawMaterial.RESOURCE_STEEL;
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
		recoverPhyStats();
	}


	public int weaponType(){return weaponType;}
	public int weaponClassification(){return weaponClassification;}
	public void setWeaponType(int newType){weaponType=newType;}
	public void setWeaponClassification(int newClassification){weaponClassification=newClassification;}

	public String secretIdentity()
	{
		return super.secretIdentity()+"\n\rAttack: "+phyStats().attackAdjustment()+", Damage: "+phyStats().damage();
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(amWearingAt(Wearable.WORN_WIELD) && activated())
		{
			if(phyStats().attackAdjustment()!=0)
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+phyStats().attackAdjustment());
			if(phyStats().damage()!=0)
				affectableStats.setDamage(affectableStats.damage()+phyStats().damage());
		}
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
				{
					msg.source().tell(name()+" is currently "+(activated()?"activated":"deactivated")
							+" and is at "+Math.round(CMath.div(powerRemaining(),powerCapacity())*100.0)+"% power.");
				}
				break;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> activate(s) <T-NAME>.");
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> deactivate(s) <T-NAME>.");
				this.activate(false);
				break;
			}
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((owner() instanceof MOB) && msg.amISource((MOB)owner()) && (!amWearingAt(Item.IN_INVENTORY)))
		{
			MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WEAPONATTACK:
				if(msg.tool() ==this)
				{
					if((powerRemaining()<=0)||(!activated))
					{
						String msgStr;
						if(weaponClassification()==Weapon.CLASS_RANGED)
							msgStr="<S-YOUPOSS> <T-NAMENOART> goes *click* and does not fire.";
						else
						if(activated())
							msgStr="<S-YOUPOSS> <T-NAMENOART> seems to be out of power.";
						else
							msgStr="<S-YOUPOSS> <T-NAMENOART> seems to be turned off.";
						CMMsg msg2=CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,msgStr);
						if((mob.location()!=null)&&(mob.location().okMessage(myHost, msg2)))
							mob.location().send(mob, msg2);
						return false;
					}
				}
				break;
			case CMMsg.TYP_DAMAGE: // remember 50% miss rate
				if(msg.tool() ==this)
				{
					double successFactor=0.5;
					final Manufacturer m=getFinalManufacturer();
					successFactor=m.getReliabilityPct()*successFactor;
					long powerConsumed=Math.round(phyStats().damage()*m.getEfficiencyPct());
					if(powerRemaining()>=powerConsumed)
					{
						setPowerRemaining(powerRemaining()-powerConsumed);
						if(msg.value()>0)
							msg.setValue((int)Math.round(successFactor*msg.value()));
	System.out.println("dam: "+msg.value());
					}
					else
					{
						setPowerRemaining(0);
					}
				}
				break;
			}
		}
		return true;
	}

	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	public String missString()
	{
		return CMLib.combat().standardMissString(weaponType,weaponClassification,name(),useExtendedMissString);
	}
	public String hitString(int damageAmount)
	{
		return CMLib.combat().standardHitString(weaponType, weaponClassification,damageAmount,name());
	}
	public int minRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMINRANGE))
			return 0;
		return minRange;
	}
	public int maxRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		return maxRange;
	}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	
	public boolean subjectToWearAndTear() { return false; }
	
	public void recoverOwner()
	{
		final ItemPossessor myOwner=owner;
		if(myOwner instanceof MOB)
		{
			((MOB)myOwner).recoverCharStats();
			((MOB)myOwner).recoverMaxState();
			((MOB)myOwner).recoverPhyStats();
		}
		else
		if(myOwner!=null)
			myOwner.recoverPhyStats();
	}
}
