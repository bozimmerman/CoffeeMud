package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_UseSpellCast2 extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_UseSpellCast2"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_UseSpellCast2 BOB=new Prop_UseSpellCast2();	BOB.setMiscText(text()); return BOB;}

	public void addMeIfNeccessary(MOB sourceMOB, MOB newMOB)
	{
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchAffect(A.ID());
			if((EA==null)&&(Prop_SpellAdder.didHappen(100,this)))
				A.invoke(sourceMOB,newMOB,true);
		}
	}

	public String accountForYourself()
	{
		String id="";
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(V.size()==1)
				id+=A.name();
			else
			if(v==(V.size()-1))
				id+="and "+A.name();
			else
				id+=A.name()+", ";
		}
		if(V.size()>0)
			id="Casts "+id+" when used.";
		return id;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if(processing) return;
		processing=true;

		if(affected==null) return;
		Item myItem=(Item)affected;
		if(myItem.owner()==null) return;
		switch(affect.sourceMinor())
		{
		case Affect.TYP_DRINK:
			if((myItem instanceof Drink)
			&&(affect.amITarget(myItem)))
				addMeIfNeccessary(affect.source(),affect.source());
			break;
		case Affect.TYP_EAT:
			if((myItem instanceof Food)
			&&(affect.amITarget(myItem)))
				addMeIfNeccessary(affect.source(),affect.source());
			break;
		case Affect.TYP_GET:
			if((!(myItem instanceof Drink))
			  &&(!(myItem instanceof Food))
			  &&(affect.amITarget(myItem)))
				addMeIfNeccessary(affect.source(),affect.source());
			break;
		}
		processing=false;
	}
}