package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class MagicFreeRoom extends StdRoom
{
	public MagicFreeRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_STONE;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new MagicFreeRoom();
	}

	public boolean okAffect(Affect affect)
	{
		if((affect.targetCode()==affect.STRIKE_MAGIC)
		||(affect.sourceCode()==affect.SOUND_MAGIC)
		||(affect.othersCode()==affect.SOUND_MAGIC)
		||(affect.targetCode()==affect.SOUND_MAGIC))
		{
			show(affect.source(),null,Affect.VISUAL_WNOISE,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}


}
