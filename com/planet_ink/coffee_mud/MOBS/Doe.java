package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Doe extends Deer
{

	public Doe()
	{
		super();
		Username="a doe";
		setDescription("A nervous, but beautifully graceful creation.");
		setDisplayText("A doe looks up as you happen along.");
	}
	public Environmental newInstance()
	{
		return new Buck();
	}
}