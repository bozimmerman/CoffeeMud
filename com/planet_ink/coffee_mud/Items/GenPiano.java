package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenPiano extends GenRideable implements MusicalInstrument
{
	public String ID(){	return "GenPiano";}
	public GenPiano()
	{
		super();
		name="a generic piano";
		displayText="a generic piano sits here.";
		description="";
		baseGoldValue=1015;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		baseEnvStats().setWeight(2000);
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=2;
		setMaterial(EnvResource.RESOURCE_OAK);
	}
	public Environmental newInstance()
	{
		return new GenPiano();
	}
	public boolean isReadable(){return false;}
	public int instrumentType(){return Util.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

}
