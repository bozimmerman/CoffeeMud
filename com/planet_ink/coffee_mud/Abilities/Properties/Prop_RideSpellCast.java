package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RideSpellCast extends Property
{
	public String ID() { return "Prop_RideSpellCast"; }
	public String name(){ return "Casting spells when ridden";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private Vector lastRiders=new Vector();
	private boolean processing=false;

	public Environmental newInstance(){	Prop_RideSpellCast BOB=new Prop_RideSpellCast(); BOB.setMiscText(text()); return BOB;}

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
			id="Casts "+id+" on the mounted.";
		return id;
	}

	public void addMeIfNeccessary(MOB E)
	{
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=E.fetchAffect(A.ID());
			if(EA==null)
			{
				A.invoke(E,E,true);
				EA=E.fetchAffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
		if(!lastRiders.contains(E))	lastRiders.addElement(E);
	}

	public void removeMyAffectsFromRider(MOB E)
	{
		Hashtable h=Prop_SpellAdder.getMySpellsH(this);
		int x=0;
		while(x<E.numAffects())
		{
			Ability thisAffect=E.fetchAffect(x);
			if(thisAffect!=null)
			{
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)&&(thisAffect.invoker()==E))
				{
					thisAffect.unInvoke();
					x=-1;
				}
			}
			x++;
		}
		while(lastRiders.contains(E))
			lastRiders.removeElement(E);
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB instanceof MOB)
		&&(affected instanceof Rideable))
		{
			Rideable RI=(Rideable)affected;
			MOB RR=(MOB)affectedMOB;
		
			if(lastRiders.contains(RR)&&(!RI.amRiding(RR)))
				removeMyAffectsFromRider(RR);
			else
			if((!lastRiders.contains(RR))&&(RI.amRiding(RR)))
				addMeIfNeccessary(RR);
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}