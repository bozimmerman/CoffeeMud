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
			tattooCode= CMMsg.TYP_DRINK;
		else
		if(affected instanceof Food)
			tattooCode= CMMsg.TYP_EAT;
		else
		if(affected instanceof Rideable)
		{
			tattooCode= CMMsg.TYP_MOUNT;
			switch(((Rideable)affected).rideBasis())
			{
			case Rideable.RIDEABLE_ENTERIN:
				tattooCode= CMMsg.TYP_ENTER; break;
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
				tattooCode= CMMsg.TYP_SIT; break;
			case Rideable.RIDEABLE_SLEEP:
				tattooCode= CMMsg.TYP_SLEEP; break;
			}
		}
		else
		if(affected instanceof MOB)
			tattooCode= CMMsg.TYP_SPEAK;
		else
		if(affected instanceof Weapon)
			tattooCode= CMMsg.TYP_WEAPONATTACK;
		else
		if(affected instanceof Armor)
			tattooCode= CMMsg.TYP_WEAR;
		else
		if(affected instanceof Item)
			tattooCode= CMMsg.TYP_GET;
		else
		if(affected instanceof Room)
			tattooCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Area)
			tattooCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Exit)
			tattooCode= CMMsg.TYP_ENTER;
		return tattooCode;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((tattooCode()>=0)
		   &&((msg.targetMinor()==tattooCode())||(msg.sourceMinor()==tattooCode()))
		   &&(msg.amITarget(affected)||(msg.tool()==affected))
		   &&(text().length()>0))
		{
			Room otherRoom=CMMap.getRoom(text());
			if(otherRoom==null)
				msg.source().tell("You are whisked nowhere at all, since '"+text()+"' is nowhere to be found.");
			else
			{
				otherRoom.bringMobHere(msg.source(),true);
				ExternalPlay.look(msg.source(),null,true);
				if(affected instanceof Rideable)
					msg.addTrailerMsg(new FullMsg(msg.source(),affected,CMMsg.TYP_DISMOUNT,null));
			}

		}
		super.executeMsg(myHost,msg);
	}
}
