package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenAmmunition extends StdItem implements Ammunition
{
	public String ID(){	return "GenAmmunition";}
	protected String	readableText="";
	public GenAmmunition()
	{
		super();

		setName("a batch of arrows");
		setDisplayText("a generic batch of arrows sits here.");
		setUsesRemaining(100);
		setAmmunitionType("arrows");
		setDescription("");
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenAmmunition();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text)
	{
		if(Sense.isReadable(this)) Sense.setReadable(this,false);
		readableText=text;
	}
	public String ammunitionType(){return readableText;}
	public void setAmmunitionType(String text){readableText=text;}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return super.okMessage(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			mob.tell("You can't hold "+name()+".");
			return false;
		case CMMsg.TYP_WEAR:
			mob.tell("You can't wear "+name()+".");
			return false;
		case CMMsg.TYP_WIELD:
			mob.tell("You can't wield "+name()+" as a weapon.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}