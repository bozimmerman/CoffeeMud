package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_UseSpellCast extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_UseSpellCast"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_UseSpellCast BOB=new Prop_UseSpellCast();	BOB.setMiscText(text()); return BOB;}

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
		if(!(myItem.owner() instanceof MOB)) return;
		if(affect.amISource((MOB)myItem.owner()))
			switch(affect.sourceMinor())
			{
			case Affect.TYP_FILL:
				if((myItem instanceof Drink)
				&&(affect.amITarget(myItem)))
					addMeIfNeccessary(affect.source(),affect.source());
				break;
			case Affect.TYP_WEAR:
				if((myItem instanceof Armor)
				  &&(affect.amITarget(myItem)))
					addMeIfNeccessary(affect.source(),affect.source());
				break;
			case Affect.TYP_PUT:
				if((myItem instanceof Container)
				  &&(affect.amITarget(myItem)))
					addMeIfNeccessary(affect.source(),affect.source());
				break;
			case Affect.TYP_WIELD:
			case Affect.TYP_HOLD:
				if((!(myItem instanceof Drink))
				  &&(!(myItem instanceof Armor))
				  &&(!(myItem instanceof Container))
				  &&(affect.amITarget(myItem)))
					addMeIfNeccessary(affect.source(),affect.source());
				break;
			}
		processing=false;
	}
}