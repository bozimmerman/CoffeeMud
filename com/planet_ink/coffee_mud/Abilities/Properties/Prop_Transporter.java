package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Transporter extends Property
{
	public String ID() { return "Prop_Transporter"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	private Hashtable zapList=new Hashtable();
	private Room room=null;
	int tattooCode=-1;
	public Environmental newInstance(){	return new Prop_Transporter();}

	public String accountForYourself()
	{ return "Zap them elsewhere";	}

	public int tattooCode()
	{
		if(tattooCode>=0) return tattooCode;
		if(affected==null) return -1;
		if(affected instanceof Drink)
			tattooCode= Affect.TYP_DRINK;
		else
		if(affected instanceof Food)
			tattooCode= Affect.TYP_EAT;
		else
		if(affected instanceof MOB)
			tattooCode= Affect.TYP_SPEAK;
		else
		if(affected instanceof Weapon)
			tattooCode= Affect.TYP_WEAPONATTACK;
		else
		if(affected instanceof Armor)
			tattooCode= Affect.TYP_WEAR;
		else
		if(affected instanceof Item)
			tattooCode= Affect.TYP_GET;
		else
		if(affected instanceof Room)
			tattooCode= Affect.TYP_ENTER;
		else
		if(affected instanceof Area)
			tattooCode= Affect.TYP_ENTER;
		else
		if(affected instanceof Exit)
			tattooCode= Affect.TYP_ENTER;
		return tattooCode;
	}
	
	public void affect(Affect affect)
	{
		if((tattooCode()>=0)
		   &&((affect.targetMinor()==tattooCode())||(affect.sourceMinor()==tattooCode()))
		   &&(affect.amITarget(affected)||(affect.tool()==affected))
		   &&(text().length()>0))
		{
			Room otherRoom=CMMap.getRoom(text());
			if(otherRoom==null)
				affect.source().tell("You are whisked nowhere at all, since '"+text()+"' is nowhere to be found.");
			else
				otherRoom.bringMobHere(affect.source(),true);
		}
		super.affect(affect);
	}
}
