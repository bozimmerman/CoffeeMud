package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenMob extends StdMOB
{
	public String ID(){return "GenMob";}
	public GenMob()
	{
		super();
		Username="a generic mob";
		setDescription("");
		setDisplayText("A generic mob stands here.");

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GenMob();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		miscText=Util.compressString(Generic.getPropertiesStr(this,false));
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		if((newText!=null)&&(newText.length()>0))
			Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
		recoverCharStats();
		baseState().setHitPoints((10*baseEnvStats().level())+Dice.roll(baseEnvStats().level(),baseEnvStats().ability(),1));
		baseState().setMana(baseCharStats().getCurrentClass().getLevelMana(this));
		baseState().setMovement(baseCharStats().getCurrentClass().getLevelMove(this));
		recoverMaxState();
		resetToMaxState();
		if(getWimpHitPoint()>0) setWimpHitPoint((int)Math.round(Util.mul(curState().getHitPoints(),.10)));
	}
	public String getStat(String code)
	{ return Generic.getGenMobStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenMobStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENMOBCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenMob)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
