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
		setDuration(100);
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

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		return LightSource.isAnOkAffect(this,affect);
	}


	public void affect(Affect affect)
	{
		LightSource.lightAffect(this,affect);
		super.affect(affect);
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DROP:
			case Affect.TYP_GET:
				if(affect.source()!=null)
				{
					affect.source().recoverEnvStats();
					if(affect.source().location()!=null)
						affect.source().location().recoverRoomStats();
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
	
	public boolean tick(int tickID)
	{
		if(!LightSource.pleaseTickLightly(this,tickID))
			return false;
		return super.tick(tickID);
	}
}
