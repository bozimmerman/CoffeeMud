package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLightSource extends GenItem implements Light
{
	public String ID(){	return "GenLightSource";}
	protected boolean lit=false;
	protected boolean destroyedWhenBurnedOut=true;
	protected boolean goesOutInTheRain=true;


	public GenLightSource()
	{
		super();

		setName("a generic lightable thing");
		setDisplayText("a generic lightable thing sits here.");
		setDescription("");
		destroyedWhenBurnedOut=true;
		setMaterial(EnvResource.RESOURCE_OAK);
		setDuration(200);
	}


	public boolean isGeneric(){return true;}

	public void setDuration(int duration){readableText=""+duration;}
	public int getDuration(){return Util.s_int(readableText);}
	public boolean destroyedWhenBurnedOut(){return destroyedWhenBurnedOut;}
	public void setDestroyedWhenBurntOut(boolean truefalse){destroyedWhenBurnedOut=truefalse;}
	public boolean goesOutInTheRain(){return goesOutInTheRain;}
	public boolean isLit(){return lit;}
	public void light(boolean isLit){lit=isLit;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(LightSource.isAnOkAffect(this,msg))
		{
		case 0: return false;
		case 1: return super.okMessage(myHost,msg);
		default: return true;
		}
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		LightSource.lightAffect(this,msg);
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_THROW:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				if(msg.source()!=null)
				{
					msg.source().recoverEnvStats();
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					{
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
						if((msg.tool()!=null)
						&&(msg.tool()!=msg.source().location())
						&&(msg.tool() instanceof Room))
							((Room)msg.tool()).recoverRoomStats();
					}
				}
				break;
			}
		}
	}
	public void recoverEnvStats()
	{
		LightSource.recoverMyEnvStats(this);
		super.recoverEnvStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!LightSource.pleaseTickLightly(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}
	public String getStat(String code)
	{ return CoffeeMaker.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ CoffeeMaker.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return CoffeeMaker.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenLightSource)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
