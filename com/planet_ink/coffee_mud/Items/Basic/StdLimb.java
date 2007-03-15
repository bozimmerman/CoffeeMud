package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

public class StdLimb extends StdItem
{
	public String ID(){	return "StdLimb";}
	protected int partnum=-1;
	protected long wearplace=-1;
	
	public StdLimb()
	{
		super();
		setName("a false limb");
		baseEnvStats.setWeight(1);
		setDisplayText("a false limb is here.");
		setDescription("Looks like a false limb.");
		properWornBitmap=Item.WORN_HELD|Item.WORN_FLOATING_NEARBY;
		wornLogicalAnd=false;
		baseGoldValue=10;
		material=RawMaterial.RESOURCE_MEAT;
		recoverEnvStats();
	}

	public void setName(String name)
	{
		super.setName(name);
		wearplace=-1;
		partnum=-1;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_REMOVE)
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS)))
		{
			
		}
		return true;
	}
	
	public void setSecretIdentity(String id)
	{
		super.setSecretIdentity(id);
		wearplace=-1;
		partnum=-1;
	}
	
	protected int partNum()
	{
		if(partnum>=0) return partnum;
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if((name().toUpperCase().endsWith(Race.BODYPARTSTR[i]))
			||(rawSecretIdentity().toUpperCase().endsWith(Race.BODYPARTSTR[i])))
			{
				partnum=i;
				break;
			}
		return partnum;
	}
	
	private long wearPlace()
	{ 
		if(wearplace>=0) return wearplace;
		int num=partNum(); 
		if(num<0) 
			wearplace=0;
		else
			wearplace=Race.BODY_WEARVECTOR[num];
		return wearplace;
	}
	
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		
		if((!amWearingAt(Item.IN_INVENTORY))
		&&(!amWearingAt(Item.WORN_HELD))
		&&(!amWearingAt(Item.WORN_FLOATING_NEARBY))
		&&(!amWearingAt(Item.WORN_WIELD)))
		{
			if(affected.charStats().getBodyPart(partNum())<affected.charStats().getMyRace().bodyMask()[partNum()])
				affectableStats.alterBodypart(partNum(),envStats().ability());
			else
				setRawWornCode(0);
		}
	}
	
	public boolean canWear(MOB mob, long where)
	{
		if(where==Item.WORN_HELD) 
			return super.canWear(mob,where);
		if(where==Item.WORN_FLOATING_NEARBY)
			return false;
		if(partNum()<0) return false;
		if((where!=0)&&(where!=wearPlace()))
			return false;
		int numRacialTotal=mob.charStats().getMyRace().bodyMask()[partNum()];
		int numWorkingParts=mob.charStats().getBodyPart(partNum());
		// now add in other NON-FUNCTIONAL limb things worn
		// FUNCTIONAL limbs are already included in numWorkingParts
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I instanceof StdLimb)
			&&(((StdLimb)I).partNum()==partNum())
			&&(I.amWearingAt(wearPlace()))
			&&(I.container()==null))
				numWorkingParts++;
		}
		if(numWorkingParts>=numRacialTotal)
			return false;
		return true;
	}
	
	public boolean fitsOn(long wornCode)
	{
		if(wornCode==Item.WORN_HELD) 
			return super.fitsOn(wornCode);
		if(wornCode==Item.WORN_FLOATING_NEARBY)
			return false;
		if(wornCode<=0)	return true;
		return wearPlace()==wornCode;
	}
	
	protected boolean canWearComplete(MOB mob)
	{
		if(partNum()<0)
		{
			mob.tell("This limb looks malformed.");
			return false;
		}
		if(!canWear(mob,0))
		{
			mob.tell("You don't have any empty sockets to wear "+name()+" on.");
			return false;
		}
		return true;
	}
}
