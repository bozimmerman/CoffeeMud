package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearResister extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;

	public Prop_WearResister()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resistance due to wearing";
	}

	public Environmental newInstance()
	{
		Prop_WearResister BOB=new Prop_WearResister();
		BOB.setMiscText(text());
		return BOB;
	}

	public String accountForYourself()
	{
		String id="The wearer gains resistances: "+text();
		return id;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(affectedMOB!=null)
		{
			if(affectedMOB instanceof Item)
			{
				myItem=(Item)affectedMOB;
				if((myItem.myOwner()!=null)
				&&(myItem.myOwner() instanceof MOB))
					lastMOB=(MOB)myItem.myOwner();
				else
					lastMOB=null;
			}
			else
				lastMOB=null;
		}
		else
			lastMOB=null;
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(lastMOB==null) return true;
		if(myItem==null) return true;
		if(myItem.amWearingAt(Item.INVENTORY)) return true;
		MOB mob=lastMOB;
		if((affect.amITarget(mob))&&(!affect.wasModified())&&(mob.location()!=null))
		{
			if(!Prop_HaveResister.isOk(affect,this,mob))
				return false;
			Prop_HaveResister.resistAffect(affect,mob,this);
		}
		return true;
	}

}