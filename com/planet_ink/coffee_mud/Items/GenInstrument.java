package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenInstrument extends GenItem implements MusicalInstrument
{
	public String ID(){	return "GenInstrument";}
	public GenInstrument()
	{
		super();
		name="a generic musical instrument";
		baseEnvStats.setWeight(12);
		displayText="a generic musical instrument sits here.";
		description="";
		baseGoldValue=15;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_OAK);
	}
	public Environmental newInstance()
	{
		return new GenInstrument();
	}
	public boolean isReadable(){return false;}
	public int instrumentType(){return Util.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

}
