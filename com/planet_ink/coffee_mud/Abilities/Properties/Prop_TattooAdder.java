package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_TattooAdder extends Property
{
	public String ID() { return "Prop_TattooAdder"; }
	public String name(){ return "A Tattoo";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS|Ability.CAN_EXITS;}
	int tattooCode=-1;
	public Environmental newInstance()
	{
		Prop_TattooAdder BOB=new Prop_TattooAdder();
		BOB.setMiscText(text());
		BOB.tattooCode=-1;
		return BOB;
	}

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
		if(affected instanceof MOB)
			tattooCode= CMMsg.TYP_DEATH;
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
		&&(msg.amITarget(affected)||((msg.tool()==affected)&&(tattooCode()!=CMMsg.TYP_DEATH)))
		&&(text().length()>0))
		{
			String tattooName=text();
			boolean tattooMinus=tattooName.startsWith("-");
			if(tattooMinus)
				tattooName=tattooName.substring(1);

			Ability A=msg.source().fetchAbility("Prop_Tattoo");
			if(A==null)
			{
				A=CMClass.getAbility("Prop_Tattoo");
				msg.source().addAbility(A);
			}
			int x=A.text().indexOf(";"+tattooName.toUpperCase()+";");
			if(x>=0)
			{
				if(tattooMinus)
				{
					msg.source().location().showHappens(CMMsg.MSG_OK_ACTION,affected.name()+" takes away the "+tattooName+" tattoo from <S-NAME>.");
					A.setMiscText(A.text().substring(0,x+1)+A.text().substring(x+2+tattooName.length()));
				}
			}
			else
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_ACTION,affected.name()+" gives <S-NAME> the "+tattooName+" tattoo.");
				if(A.text().length()==0)
					A.setMiscText(";");
				A.setMiscText(A.text()+tattooName.toUpperCase()+";");
			}
		}
		super.executeMsg(myHost,msg);
	}
}