package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
public class UnderSaltWater extends UnderWater
{
	public UnderSaltWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Environmental newInstance()
	{
		return new UnderSaltWater();
	}
	public int liquidType(){return Drink.LIQUID_SALT_WATER;}
}
