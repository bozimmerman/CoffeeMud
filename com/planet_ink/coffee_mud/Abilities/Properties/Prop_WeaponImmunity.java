package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WeaponImmunity extends Property
{
	public String ID() { return "Prop_WeaponImmunity"; }
	public String name(){ return "Weapon Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_WeaponImmunity BOB=new Prop_WeaponImmunity();	BOB.setMiscText(text());return BOB;}

	public String accountForYourself()
	{
		String id="Weapon Immunities for the wearer: "+text();
		return id;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			MOB M=null;
			if(affected instanceof MOB)
				M=(MOB)affected;
			else
			if((affected instanceof Item)
			&&(!((Item)affected).amWearingAt(Item.INVENTORY))
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB))
				M=(MOB)((Item)affected).owner();
			if(M==null) return true;
			if(!msg.amITarget(M)) return true;
			if(msg.tool()!=null)
			{
				String text=text().toUpperCase();
				boolean immune=text.indexOf("+ALL")>=0;
				if(msg.tool() instanceof Weapon)
				{
					int x=text.indexOf(Weapon.typeDescription[((Weapon)msg.tool()).weaponType()]);
					if(x>0)
					{
						if((text.charAt(x-1)=='-')&&(immune))
							immune=false;
						else
						if(text.charAt(x-1)!='-')
							immune=true;
					}
					if(Sense.isABonusItems(msg.tool()))
					{
						x=text.indexOf("MAGIC");
						if(x>0)
						{
							if((text.charAt(x-1)=='-')&&(immune))
								immune=false;
							else
							if(text.charAt(x-1)!='-')
								immune=true;
						}
					}
					x=text.indexOf("LEVEL");
					if(x>0)
					{
						String lvl=text.substring(x+5);
						if(lvl.indexOf(" ")>=0)
							lvl=lvl.substring(lvl.indexOf(" "));
						if((text.charAt(x-1)=='-')&&(immune))
						{
							if(msg.tool().envStats().level()>=Util.s_int(lvl))
								immune=false;
						}
						else
						if(text.charAt(x-1)!='-')
						{
							if(msg.tool().envStats().level()<Util.s_int(lvl))
								immune=true;
						}
					}
					x=text.indexOf(EnvResource.RESOURCE_DESCS[((Weapon)msg.tool()).material()&EnvResource.RESOURCE_MASK]);
					if(x>0)
					{
						if((text.charAt(x-1)=='-')&&(immune))
							immune=false;
						else
						if(text.charAt(x-1)!='-')
							immune=true;
					}
				}
				else
				if(msg.tool() instanceof Ability)
				{
					int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES;
					switch(classType)
					{
					case Ability.SPELL:
					case Ability.PRAYER:
					case Ability.CHANT:
					case Ability.SONG:
						{
							int x=text.indexOf("MAGIC");
							if(x>0)
							{
								if((text.charAt(x-1)=='-')&&(immune))
									immune=false;
								else
								if(text.charAt(x-1)!='-')
									immune=true;
							}
						}
						break;
					default:
						break;
					}
				}
				if(immune) msg.setValue(0);
			}

		}
		return true;
	}
}