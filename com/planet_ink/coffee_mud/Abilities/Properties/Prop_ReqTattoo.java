package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqTattoo extends Property
{
	public String ID() { return "Prop_ReqTattoo"; }
	public String name(){ return "Tattoo Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_ReqTattoo newOne=new Prop_ReqTattoo();	newOne.setMiscText(text());	return newOne;}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		int x=text().toUpperCase().indexOf("ALL");
		Vector V=Prop_Tattoo.getTattoos(mob);
		if(V.size()==0) V.addElement("NONE");
		for(int v=0;v<V.size();v++)
		{
			String tattoo=(String)V.elementAt(v);
			int y=text().toUpperCase().indexOf(tattoo);
			if(((x>0)
				&&(text().charAt(x-1)=='-')
				&&((y<=0)
				   ||((y>0)&&(text().charAt(y-1)!='+'))))
			 ||((y>0)&&(text().charAt(y-1)=='-')))
				return false;
		}
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(affect.target()!=null)
		&&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			if(((affect.target() instanceof Room)&&(affect.targetMinor()==Affect.TYP_ENTER))
			||((affect.target() instanceof Item)&&(affect.targetMinor()==Affect.TYP_GET)))
			{
				Hashtable H=new Hashtable();
				if(text().toUpperCase().indexOf("NOFOL")>=0)
					H.put(affect.source(),affect.source());
				else
				{
					affect.source().getGroupMembers(H);
					for(Enumeration e=H.elements();e.hasMoreElements();)
						((MOB)e.nextElement()).getRideBuddies(H);
				}
				for(Enumeration e=H.elements();e.hasMoreElements();)
					if(passesMuster((MOB)e.nextElement()))
						return super.okAffect(myHost,affect);
				if(affect.target() instanceof Room)
					affect.source().tell("You have not been granted authorization to go that way.");
				else
				if(affect.source().location()!=null)
					affect.source().location().show(affect.source(),null,affected,Affect.MSG_OK_ACTION,"<O-NAME> flashes and flys out of <S-HIS-HER> hands!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}