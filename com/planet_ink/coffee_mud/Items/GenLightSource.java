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

		name="a generic lightable thing";
		displayText="a generic lightable thing sits here.";
		description="";
		isReadable=false;
		destroyedWhenBurnedOut=true;
		setMaterial(EnvResource.RESOURCE_OAK);
		setDuration(150);
	}

	public Environmental newInstance()
	{
		return new GenLightSource();
	}
	public boolean isGeneric(){return true;}

	public void setDuration(int duration){readableText=""+duration;}
	public int getDuration(){return Util.s_int(readableText);}
	public boolean destroyedWhenBurnedOut(){return destroyedWhenBurnedOut;}
	public boolean goesOutInTheRain(){return goesOutInTheRain;}
	public boolean isLit(){return lit;}
	public void light(boolean isLit){lit=isLit;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(LightSource.isAnOkAffect(this,affect))
		{
		case 0: return false;
		case 1: return super.okAffect(myHost,affect);
		default: return true;
		}
	}


	public void affect(Environmental myHost, Affect affect)
	{
		LightSource.lightAffect(this,affect);
		super.affect(myHost,affect);
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DROP:
			case Affect.TYP_THROW:
			case Affect.TYP_GET:
				if(affect.source()!=null)
				{
					affect.source().recoverEnvStats();
					if(affect.source().location()!=null)
						affect.source().location().recoverRoomStats();
					if((affect.tool()!=null)
					&&(affect.tool()!=affect.source().location())
					&&(affect.tool() instanceof Room))
						((Room)affect.tool()).recoverRoomStats();
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
	{ return Generic.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenLightSource)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
