package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_OpenPassword extends Property
{
	public String ID() { return "Prop_OpenPassword"; }
	public String name(){ return "Opening Password";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public Environmental newInstance()
	{	Prop_OpenPassword newOne=new Prop_OpenPassword(); newOne.setMiscText(text());return newOne; }

	public String accountForYourself()
	{ return "";	}

	public void affect(Environmental myHost, Affect affect)
	{
		if((affect.sourceMinor()==Affect.TYP_SPEAK)
		&&(affected!=null)
		&&((affect.sourceCode()&Affect.MASK_MAGIC)==0))
		{
			int start=affect.sourceMessage().indexOf("\'");
			int end=affect.sourceMessage().lastIndexOf("\'");
			if((start>0)&&(end>start))
			{
				String str=affect.sourceMessage().substring(start+1,end).trim();
				MOB mob=(MOB)affect.source();
				if(str.equalsIgnoreCase(text())
				&&(text().length()>0)
				&&(mob.location()!=null))
				{
					Room R=mob.location();
					if(affected instanceof Exit)
					{
						Exit E=(Exit)affected;
						if(!E.isOpen())
						{
							int dirCode=-1;
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								if(R.getExitInDir(d)==E)
								{ dirCode=d; break;}
							if(dirCode>=0)
							{
								FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_UNLOCK,null);
								ExternalPlay.roomAffectFully(msg,R,dirCode);
								msg=new FullMsg(mob,E,null,Affect.MSG_OPEN,"<T-NAME> opens.");
								ExternalPlay.roomAffectFully(msg,R,dirCode);
							}
						}
					}
					else
					if(affected instanceof Container)
					{
						FullMsg msg=new FullMsg(mob,affected,null,Affect.MSG_UNLOCK,null);
						affected.affect(mob,msg);
						msg=new FullMsg(mob,affected,null,Affect.MSG_OPEN,"<T-NAME> opens.");
						affected.affect(mob,msg);
					}
				}
			}
		}
		super.affect(myHost,affect);
	}
}
