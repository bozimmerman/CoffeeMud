package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearResister extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;
	private CharStats adjCharStats=null;

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

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		Prop_HaveResister.setAdjustments(this,adjCharStats);
	}
	public boolean isBorrowed(Environmental toMe)
	{
		if(toMe instanceof MOB)
			return true;
		return borrowed;
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		ensureStarted();
		if(affectedMOB!=null)
		{
			if(affectedMOB instanceof Item)
			{
				myItem=(Item)affectedMOB;
				if((!myItem.amWearingAt(Item.INVENTORY))
				   &&(myItem.myOwner() instanceof MOB))
				{
					if((lastMOB!=null)&&(myItem.myOwner()!=lastMOB))
					{	Prop_HaveResister.removeMyAffectFromLastMob(this,lastMOB); lastMOB=null;}
					if(myItem.myOwner() !=null)
					{
						lastMOB=(MOB)myItem.myOwner();
						if(!lastMOB.isMine(this))
							Prop_HaveResister.addMe(lastMOB,this);
					}
				}
			}
			else
			if(affectedMOB instanceof MOB)
			{
				if((!myItem.amWearingAt(Item.INVENTORY))
				   &&(myItem.myOwner() instanceof MOB)
				   &&(myItem.myOwner()==affectedMOB))
				{
					if((lastMOB!=null)&&(affectedMOB!=lastMOB))
					{	Prop_HaveResister.removeMyAffectFromLastMob(this,lastMOB); lastMOB=null;}
					lastMOB=(MOB)affectedMOB;
				}
				else
				if((affectedMOB!=null)&&(affectedMOB!=myItem.myOwner()))
				{
					Prop_HaveResister.removeMyAffectFromLastMob(this,(MOB)affectedMOB);
				}
			}
		}
		super.affectEnvStats(affectedMOB,affectableStats);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affectedMOB!=null)
		   &&(lastMOB==affectedMOB))
			Prop_HaveResister.adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
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