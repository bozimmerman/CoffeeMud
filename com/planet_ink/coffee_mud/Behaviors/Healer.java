package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Healer extends ActiveTicker
{
	private static Vector healingVector=new Vector();

	public Healer()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
		if(healingVector.size()==0)
		{
			healingVector.addElement(CMClass.getAbility("Prayer_CureBlindness"));
			healingVector.addElement(CMClass.getAbility("Prayer_CureDisease"));
			healingVector.addElement(CMClass.getAbility("Prayer_CureLight"));
			healingVector.addElement(CMClass.getAbility("Prayer_CureSerious"));
			healingVector.addElement(CMClass.getAbility("Prayer_Bless"));
			healingVector.addElement(CMClass.getAbility("Prayer_Sanctuary"));
		}
	}

	public Behavior newInstance()
	{
		return new Healer();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom==null) return;

			double aChance=Util.div(mob.curState().getMana(),mob.maxState().getMana());
			if((Math.random()>aChance)||(mob.curState().getMana()<50))
				return;

			MOB target=thisRoom.fetchInhabitant(Dice.roll(1,thisRoom.numInhabitants(),-1));
			int x=0;
			while(((target==null)||(target==mob)||(target.isMonster()))&&((++x)<10))
				target=thisRoom.fetchInhabitant(Dice.roll(1,thisRoom.numInhabitants(),-1));

			Ability tryThisOne=(Ability)healingVector.elementAt(Dice.roll(1,healingVector.size(),0)-1);
			Ability thisOne=mob.fetchAbility(tryThisOne.ID());
			if(thisOne==null)
			{
				thisOne=(Ability)tryThisOne.copyOf();
				thisOne.setBorrowed(mob,true);
				mob.addAbility(thisOne);
			}
			thisOne.setProfficiency(100);
			Vector V=new Vector();
			if((target!=null)&&(target!=mob)&&(!target.isMonster()))
				V.addElement(target.name());
			thisOne.invoke(mob,V,target,false);
		}
	}
}