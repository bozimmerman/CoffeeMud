package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_FightSpellCast extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_FightSpellCast"; }
	public String name(){ return "Casting spells when properly used during combat";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_FightSpellCast BOB=new Prop_FightSpellCast();	BOB.setMiscText(text()); return BOB;}

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
			id="Casts "+id+" during combat.";
		return id;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if(processing) return;
		processing=true;

		if(affected==null) return;

		Item myItem=(Item)affected;

		if((myItem!=null)
		&&(!myItem.amWearingAt(Item.INVENTORY))
		&&(myItem.owner()!=null)
		&&(myItem.owner() instanceof MOB)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB))
		{
			MOB mob=(MOB)myItem.owner();
			if((mob.isInCombat())
			&&(mob.location()!=null)
			&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&((affect.targetCode()-Affect.MASK_HURT)>0)
			&&(!mob.amDead()))
			{
				if((myItem instanceof Weapon)
				&&(affect.tool()==myItem)
				&&(myItem.amWearingAt(Item.WIELD))
				&&(affect.amISource(mob)))
					addMeIfNeccessary(affect.source(),(MOB)affect.target());
				else
				if((affect.amITarget(mob))
				&&(!myItem.amWearingAt(Item.WIELD))
				&&(!(myItem instanceof Weapon)))
					addMeIfNeccessary((MOB)affect.target(),(MOB)affect.target());
			}
		}
		processing=false;
	}
}