package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
		properWornBitmap=Item.HELD|Item.FLOATING_NEARBY;
		wornLogicalAnd=false;
		baseGoldValue=10;
		material=EnvResource.RESOURCE_MEAT;
		recoverEnvStats();
	}

	public void setName(String name)
	{
		super.setName(name);
		wearplace=-1;
		partnum=-1;
	}
	
	public void setSecretIdentity(String id)
	{
		super.setSecretIdentity(id);
		wearplace=-1;
		partnum=-1;
	}
	
	private int partNum()
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
		
		if((!amWearingAt(Item.INVENTORY))
		&&(!amWearingAt(Item.HELD))
		&&(!amWearingAt(Item.FLOATING_NEARBY))
		&&(!amWearingAt(Item.WIELD)))
		{
			if(affected.charStats().getBodyPart(partNum())<affected.charStats().getMyRace().bodyMask()[partNum()])
				affectableStats.alterBodypart(partNum(),envStats().ability());
			else
				setRawWornCode(0);
		}
	}
	
	public boolean canWear(MOB mob, long where)
	{
		if(where==Item.HELD) 
			return super.canWear(mob,where);
		if((where==Item.FLOATING_NEARBY)
		||(envStats().ability()!=0))
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
			&&(I.envStats().ability()==0)
			&&(I.container()==null))
				numWorkingParts++;
		}
		if(numWorkingParts>=numRacialTotal)
			return false;
		return true;
	}
	
	public boolean fitsOn(long wornCode)
	{
		if(wornCode==Item.HELD) 
			return super.fitsOn(wornCode);
		if((wornCode==Item.FLOATING_NEARBY)
		||(envStats().ability()!=0))
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
		if((envStats().ability()!=0)
		||(!canWear(mob,0)))
		{
			mob.tell("You don't have any empty sockets to wear "+name()+" on.");
			return false;
		}
		return true;
	}
}
