package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class StoneMaze extends StdMaze
{
	public StoneMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=1;
	}
	public Environmental newInstance()
	{
		return new StoneMaze();
	}
	public String getChildLocaleID(){return "StoneRoom";}
}
