package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Surrender extends ThiefSkill
{
	public Thief_Surrender()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Surrender";
		displayText="(Ability to surrender)";
		miscText="";

		triggerStrings.addElement("SURRENDER");

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(3);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Surrender();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Vector theList=new Vector();
		int gold=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB vic=mob.location().fetchInhabitant(i);
			if((vic!=null)&&(vic!=mob)&&(vic.isInCombat())&&(vic.getVictim()==mob))
			{
				gold+=vic.envStats().level()*10;
				theList.addElement(vic);
			}
		}
		if((!mob.isInCombat())||(theList.size()==0))
		{
			mob.tell("There's noone to surrender to!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success||(mob.getMoney()<gold))
		{
			StringBuffer enemiesList=new StringBuffer("");
			for(int v=0;v<theList.size();v++)
			{
				MOB vic=(MOB)theList.elementAt(v);
				if(v==0)
					enemiesList.append(vic.name());
				else
				if(v==theList.size()-1)
					enemiesList.append(", and "+vic.name());
				else
					enemiesList.append(", "+vic.name());
			}
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_NOISYMOVEMENT,"<S-NAME> surrender(s) to "+enemiesList.toString()+", paying "+gold+" gold.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.setMoney(mob.getMoney()-gold);
				mob.makePeace();
				for(int v=0;v<theList.size();v++)
				{
					MOB vic=(MOB)theList.elementAt(v);
					vic.setMoney(vic.getMoney()+(10*vic.envStats().level()));
					vic.makePeace();
				}
			}
			else
				success=false;
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to surrender and fail(s).");
		return success;
	}
}