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
		
		baseEnvStats().setAbility(11); // his only off-default

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
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=Util.compressString(Generic.getPropertiesStr(this,false));
		else
			miscText=Generic.getPropertiesStr(this,false).getBytes();
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		Generic.resetGenMOB(this,newText);
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
