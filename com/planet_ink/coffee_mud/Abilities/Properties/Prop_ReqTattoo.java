package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqTattoo extends Property
{
	public String ID() { return "Prop_ReqTattoo"; }
	public String name(){ return "Tattoo Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqTattoo newOne=new Prop_ReqTattoo();	newOne.setMiscText(text());	return newOne;}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			int x=text().toUpperCase().indexOf("ALL");
			Vector V=Prop_Tattoo.getTattoos(affect.source());
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
				{
					affect.source().tell("You have not been granted authorization to go that way.");
					return false;
				}
			}
		}
		return super.okAffect(affect);
	}
}