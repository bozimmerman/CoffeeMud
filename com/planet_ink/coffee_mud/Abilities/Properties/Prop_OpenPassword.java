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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(affected!=null)
		&&((msg.sourceCode()&CMMsg.MASK_MAGIC)==0))
		{
			int start=msg.sourceMessage().indexOf("\'");
			int end=msg.sourceMessage().lastIndexOf("\'");
			if((start>0)&&(end>start))
			{
				String str=msg.sourceMessage().substring(start+1,end).trim();
				MOB mob=(MOB)msg.source();
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
								FullMsg msg2=new FullMsg(mob,E,null,CMMsg.MSG_UNLOCK,null);
								ExternalPlay.roomAffectFully(msg2,R,dirCode);
								msg2=new FullMsg(mob,E,null,CMMsg.MSG_OPEN,"<T-NAME> opens.");
								ExternalPlay.roomAffectFully(msg2,R,dirCode);
							}
						}
					}
					else
					if(affected instanceof Container)
					{
						FullMsg msg2=new FullMsg(mob,affected,null,CMMsg.MSG_UNLOCK,null);
						affected.executeMsg(mob,msg2);
						msg2=new FullMsg(mob,affected,null,CMMsg.MSG_OPEN,"<T-NAME> opens.");
						affected.executeMsg(mob,msg2);
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
}
