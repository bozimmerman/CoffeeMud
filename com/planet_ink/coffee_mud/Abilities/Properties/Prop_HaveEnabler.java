package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HaveEnabler extends Property
{
	public String ID() { return "Prop_HaveEnabler"; }
	public String name(){ return "Granting skills when owned";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private Item myItem=null;
	private MOB lastMOB=null;
	boolean processing=false;
	protected Vector spellV=null;
	public Vector getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=Prop_SpellAdder.getMySpellsV(this);
		return spellV;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
	}


	public String accountForYourself()
	{
		String id="";
		Vector V=getMySpellsV();
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
			id="Grants "+id+" to the owner.";
		return id;
	}

	public void addMeIfNeccessary(MOB newMOB)
	{
		Vector V=getMySpellsV();
		int proff=100;
		int x=text().indexOf("%");
		if(x>0)
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(text().charAt(x)))
					tot+=Util.s_int(""+text().charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			proff=tot;
		}
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			if(newMOB.fetchAbility(A.ID())==null)
			{
				String t=A.text();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					x=t.indexOf("/");
					if(x<0)
					{
						V2=Util.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=Util.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				A.setProfficiency(proff);
				newMOB.addAbility(A);
				A.setBorrowed(newMOB,true);
			}
		}
		lastMOB=newMOB;
	}

	public void removeMyAffectsFromLastMob()
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			lastMOB.delAbility(A);
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
			&&((myItem.owner()!=lastMOB)||(myItem.amDestroyed()))
			&&(lastMOB.location()!=null))
				removeMyAffectsFromLastMob();
			
			if((lastMOB==null)&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)&&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary((MOB)myItem.owner());
		}
		super.affectEnvStats(affectedMOB,affectableStats);
		processing=false;
	}
}