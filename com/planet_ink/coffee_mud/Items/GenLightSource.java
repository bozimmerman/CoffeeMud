package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_THROW)
		&&(msg.source()!=null))
		{
			msg.source().recoverEnvStats();
			if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
			{
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
				Room R=CoffeeUtensils.roomLocation(msg.target());
				if((R!=null)&&(R!=msg.source().location()))
					R.recoverRoomStats();
			}
		}
		else
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				if(msg.source()!=null)
				{
					msg.source().recoverEnvStats();
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					{
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
						Room R=CoffeeUtensils.roomLocation(msg.tool());
						if((R!=null)&&(R!=msg.source().location()))
							R.recoverRoomStats();
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
