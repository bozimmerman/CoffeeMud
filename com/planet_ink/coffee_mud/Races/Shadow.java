package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Shadow extends Spirit
{
	public String ID(){	return "Shadow"; }
	public String name(){ return "Shadow"; }
	public long forbiddenWornBits(){return 0;}

	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((Sense.isInDark(affected))
		||((affected instanceof MOB)&&(((MOB)affected).location()!=null)&&(Sense.isInDark((((MOB)affected).location())))))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
}

