package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_EnlargeRoom extends Property
{
	public String ID() { return "Prop_EnlargeRoom"; }
	public String name(){ return "Change a rooms movement requirements";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance()
	{	Prop_EnlargeRoom newOne=new Prop_EnlargeRoom(); newOne.setMiscText(text());return newOne; }

	public String accountForYourself()
	{ return "Enlarged";	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(text().length()>0)
		{
			int weight=affectableStats.weight();
			switch(text().charAt(0))
			{
			case '+':
				affectableStats.setWeight(weight+Util.s_int(text().substring(1).trim()));
				break;
			case '-':
				affectableStats.setWeight(weight-Util.s_int(text().substring(1).trim()));
				break;
			case '*':
				affectableStats.setWeight(weight*Util.s_int(text().substring(1).trim()));
				break;
			case '/':
				affectableStats.setWeight(weight/Util.s_int(text().substring(1).trim()));
				break;
			default:
				affectableStats.setWeight(Util.s_int(text()));
				break;
			}
		}
	}
}
