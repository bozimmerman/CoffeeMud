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
		setName("a generic musical instrument");
		baseEnvStats.setWeight(12);
		setDisplayText("a generic musical instrument sits here.");
		setDescription("");
		baseGoldValue=15;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_OAK);
	}
	public Environmental newInstance()
	{
		return new GenInstrument();
	}
	public void recoverEnvStats(){Sense.setReadable(this,false); super.recoverEnvStats();}
	public int instrumentType(){return Util.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg)) return false;
		if(amWearingAt(Item.WIELD)
		   &&(msg.source()==owner())
		   &&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(msg.source().location()!=null)
		   &&((msg.tool()==null)
			  ||(msg.tool()==this)
			  ||(!(msg.tool() instanceof Weapon))
			  ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			msg.source().location().show(msg.source(),null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) <O-NAME>.");
			return false;
		}

		return true;
	}
}
