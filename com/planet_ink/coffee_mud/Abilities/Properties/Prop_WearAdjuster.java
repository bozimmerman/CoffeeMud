package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearAdjuster extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;
	private CharStats adjCharStats=null;
	private CharState adjCharState=null;
	boolean gotClass=false;
	boolean gotRace=false;
	boolean gotSex=false;

	public Prop_WearAdjuster()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Adjustments to stats when worn";
	}

	public boolean isBorrowed(Environmental toMe)
	{
		if(toMe instanceof MOB)
			return true;
		return borrowed;
	}

	public String accountForYourself()
	{
		String id="Affects on the wearer: "+text();
		int x=id.toUpperCase().indexOf("ARM");
		for(StringBuffer ID=new StringBuffer(id);((x>0)&&(x<id.length()));x++)
			if(id.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				id=ID.toString();
				break;
			}
			else
			if(id.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				id=ID.toString();
				break;
			}
			else
			if(Character.isDigit(id.charAt(x)))
				break;
		return id;
	}

	public Environmental newInstance()
	{
		Prop_WearAdjuster BOB=new Prop_WearAdjuster();
		BOB.setMiscText(text());
		return BOB;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.adjCharStats=new DefaultCharStats();
		this.adjCharState=new DefaultCharState();
		int gotit=Prop_HaveAdjuster.setAdjustments(newText,baseEnvStats(),adjCharStats,adjCharState);
		gotClass=((gotit&1)==1);
		gotRace=((gotit&2)==2);
		gotSex=((gotit&4)==4);
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
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
					{	Prop_HaveAdjuster.removeMyAffectFromLastMob(this,lastMOB,adjCharState); lastMOB=null;}

					if(myItem.myOwner() !=null)
					{
						lastMOB=(MOB)myItem.myOwner();
						if(!lastMOB.isMine(this))
							Prop_HaveAdjuster.addMe(lastMOB,adjCharState,this);
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
					{	Prop_HaveAdjuster.removeMyAffectFromLastMob(this,lastMOB,adjCharState); lastMOB=null;}
					lastMOB=(MOB)affectedMOB;
					Prop_HaveAdjuster.envStuff(affectableStats,baseEnvStats());
				}
				else
				if((affectedMOB!=null)&&((affectedMOB!=myItem.myOwner())||(myItem.amWearingAt(Item.INVENTORY))))
				{
					Prop_HaveAdjuster.removeMyAffectFromLastMob(this,(MOB)affectedMOB,adjCharState);
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
			Prop_HaveAdjuster.adjCharStats(affectedStats,gotClass,gotRace,gotSex,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
		if((affectedMOB!=null)
		   &&(lastMOB==affectedMOB))
			Prop_HaveAdjuster.adjCharState(affectedState,adjCharState);
		super.affectCharState(affectedMOB,affectedState);
	}
}