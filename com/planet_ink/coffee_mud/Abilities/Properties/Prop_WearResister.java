package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearResister extends Property
{
	public String ID() { return "Prop_WearResister"; }
	public String name(){ return "Resistance due to worn";}
	public boolean bubbleAffect(){return true;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	private CharStats adjCharStats=null;

	public Environmental newInstance(){	Prop_WearResister BOB=new Prop_WearResister();	BOB.setMiscText(text()); return BOB;}

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
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		if((affected !=null)
		&&(affected instanceof Item)
		&&(!((Item)affected).amWearingAt(Item.INVENTORY)))
			Prop_HaveResister.adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected !=null)
		&&(affected instanceof Item)
		&&(!((Item)affected).amWearingAt(Item.INVENTORY))
		&&(((Item)affected).owner() instanceof MOB))
		{
			MOB mob=(MOB)((Item)affected).owner();
			if((msg.amITarget(mob))&&(!msg.wasModified())&&(mob.location()!=null))
			{
				if(!Prop_HaveResister.isOk(msg,this,mob))
					return false;
				Prop_HaveResister.resistAffect(msg,mob,this);
			}
		}
		return true;
	}

}