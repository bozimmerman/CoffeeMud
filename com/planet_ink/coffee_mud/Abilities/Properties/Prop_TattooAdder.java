package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_TattooAdder extends Property
{
	int tattooCode=-1;
	public Prop_TattooAdder()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="A Tattoo";
	}
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
			Ability A=affect.source().fetchAbility("Prop_Tattoo");
			if(A==null)
			{
				A=CMClass.getAbility("Prop_Tattoo");
				affect.source().addAbility(A);
			}
			if(A.text().indexOf(";"+text().toUpperCase()+";")>=0)
				affect.source().tell("You already have the "+text()+" tattoo.");
			else
				affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,affected.name()+" gives <S-NAME> the "+text()+" tattoo.");
			if(A.text().length()==0)
				A.setMiscText(";");
			A.setMiscText(A.text()+text().toUpperCase()+";");
		}
		super.affect(affect);
	}
}