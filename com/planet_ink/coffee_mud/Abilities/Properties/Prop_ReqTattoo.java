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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(!Sense.isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			||((msg.target() instanceof Item)&&(msg.targetMinor()==CMMsg.TYP_GET)))
			{
				Hashtable H=new Hashtable();
				if(text().toUpperCase().indexOf("NOFOL")>=0)
					H.put(msg.source(),msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					for(Enumeration e=H.elements();e.hasMoreElements();)
						((MOB)e.nextElement()).getRideBuddies(H);
				}
				for(Enumeration e=H.elements();e.hasMoreElements();)
					if(passesMuster((MOB)e.nextElement()))
						return super.okMessage(myHost,msg);
				if(msg.target() instanceof Room)
					msg.source().tell("You have not been granted authorization to go that way.");
				else
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,affected,CMMsg.MSG_OK_ACTION,"<O-NAME> flashes and flies out of <S-HIS-HER> hands!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}