package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearSpellCast extends Property
{
	public String ID() { return "Prop_WearSpellCast"; }
	public String name(){ return "Casting spells when worn";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private Item myItem=null;
	private MOB lastMOB=null;
	private boolean processing=false;

	public Environmental newInstance(){	Prop_WearSpellCast BOB=new Prop_WearSpellCast(); BOB.setMiscText(text()); return BOB;}

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
			id="Casts "+id+" on the wearer.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
		Vector V=Prop_SpellAdder.getMySpellsV(this);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchAffect(A.ID());
			if(EA==null)
			{
				A.invoke(newMOB,newMOB,true);
				EA=newMOB.fetchAffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
		lastMOB=newMOB;
	}

	public void removeMyAffectsFromLastMob()
	{
		Hashtable h=Prop_SpellAdder.getMySpellsH(this);
		int x=0;
		while(x<lastMOB.numAffects())
		{
			Ability thisAffect=lastMOB.fetchAffect(x);
			if(thisAffect!=null)
			{
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)&&(thisAffect.invoker()==lastMOB))
				{
					thisAffect.unInvoke();
					x=-1;
				}
			}
			x++;
		}
		lastMOB=null;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((affectedMOB!=null)&&(affectedMOB instanceof Item))
		{
			myItem=(Item)affectedMOB;

			if((lastMOB!=null)
			&&(lastMOB.location()!=null)
			&&((myItem.owner()!=lastMOB)||(myItem.amWearingAt(Item.INVENTORY))))
				removeMyAffectsFromLastMob();

			if((lastMOB==null)
			&&(!myItem.amWearingAt(Item.INVENTORY))
			&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)
			&&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary((MOB)myItem.owner());
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}