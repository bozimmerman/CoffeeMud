package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class CombatAbilities extends StdBehavior
{
	public CombatAbilities()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Behavior newInstance()
	{
		return new CombatAbilities();
	}

	protected void newCharacter(MOB mob)
	{
		Vector oldAbilities=new Vector();
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)
				oldAbilities.addElement(A);
		}
		mob.charStats().getMyClass().startCharacter(mob,true,false);
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability newOne=mob.fetchAbility(a);
			if((newOne!=null)
			&&(!oldAbilities.contains(newOne))
			&&(newOne.qualifyingLevel(mob)>mob.baseEnvStats().level()))
			{
				mob.delAbility(newOne);
				mob.delAffect(newOne);
				a=a-1;
			}
		}
	}
	
	protected void getSomeMoreMageAbilities(MOB mob)
	{
		for(int a=0;a<((mob.baseEnvStats().level())+5);a++)
		{
			Ability addThis=null;
			int tries=0;
			while((addThis==null)&&((++tries)<10))
			{
				addThis=(Ability)CMClass.abilities.elementAt(Dice.roll(1,CMClass.abilities.size(),0)-1);
				if((addThis.qualifyingLevel(mob)<0)
				||(addThis.qualifyingLevel(mob)>=mob.baseEnvStats().level())
				||(((addThis.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)&&(!addThis.appropriateToMyAlignment(mob.getAlignment())))
				||(mob.fetchAbility(addThis.ID())!=null)
				||((addThis.quality()!=Ability.MALICIOUS)
				   &&(addThis.quality()!=Ability.BENEFICIAL_SELF)
				   &&(addThis.quality()!=Ability.BENEFICIAL_OTHERS)))
					addThis=null;
			}
			if(addThis!=null)
			{
				addThis=(Ability)addThis.newInstance();
				addThis.setBorrowed(mob,true);
				mob.addAbility(addThis);
				addThis.autoInvocation(mob);
			}
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return;
		MOB mob=(MOB)ticking;
		
		if(tickID!=Host.MOB_TICK) return;
		if(!canActAtAll(mob)) return;
		if(!mob.isInCombat()) return;
		MOB victim=mob.getVictim();
		if(victim==null) return;

		// insures we only try this once!
		for(int b=0;b<mob.numBehaviors();b++)
		{
			Behavior B=mob.fetchBehavior(b);
			if((B==null)||(B==this))
				break;
			else
			if(B instanceof CombatAbilities)
				return;
		}

		double aChance=Util.div(mob.curState().getMana(),mob.maxState().getMana());
		if((Math.random()>aChance)||(mob.curState().getMana()<50))
			return;

		int tries=0;
		Ability tryThisOne=null;

		while((tryThisOne==null)&&(tries<100)&&(mob.numAbilities()>0))
		{
			tryThisOne=mob.fetchAbility(Dice.roll(1,mob.numAbilities(),-1));
			if((tryThisOne!=null)&&(mob.fetchAffect(tryThisOne.ID())==null))
			{
				if(!((tryThisOne.quality()==Ability.MALICIOUS)
				||(tryThisOne.quality()==Ability.BENEFICIAL_SELF)
				||(tryThisOne.quality()==tryThisOne.BENEFICIAL_OTHERS)))
					tryThisOne=null;
			}
			else
				tryThisOne=null;
			tries++;
		}

		mob.curState().adjMana(5,mob.maxState());
		if(tryThisOne!=null)
		{
			if(tryThisOne.quality()!=Ability.MALICIOUS)
				victim=mob;

			tryThisOne.setProfficiency(Dice.roll(1,70,mob.baseEnvStats().level()));
			Vector V=new Vector();
			V.addElement(victim.name());
			tryThisOne.invoke(mob,V,victim,false);
		}
		else
		if((victim.location()!=null)
		&&(!victim.amDead())
		&&(Dice.rollPercentage()<25)
		&&(mob.fetchAbility("Skill_WandUse")!=null))
		{
			Item myWand=null;
			Item backupWand=null;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Wand)&&(!I.amWearingAt(Item.INVENTORY)))
					myWand=I;
				else
				if((I!=null)&&(I instanceof Wand))
					backupWand=I;
			}
			if((myWand==null)&&(backupWand!=null)&&(backupWand.canWear(mob)))
			{
				Vector V=new Vector();
				V.addElement("hold");
				V.addElement(backupWand.name());
				try{ExternalPlay.doCommand(mob,V);}catch(Exception e){Log.errOut("CombatAbilities",e);}
			}
			else
			if(myWand!=null)
			{
				Ability A=((Wand)myWand).getSpell();
				if((A!=null)
				&&((A.quality()==Ability.MALICIOUS)
				||(A.quality()==Ability.BENEFICIAL_SELF)
				||(A.quality()==Ability.BENEFICIAL_OTHERS)))
				{
					if(A.quality()==Ability.MALICIOUS)
						victim=mob;
					Vector V=new Vector();
					V.addElement("say");
					V.addElement(victim.name());
					V.addElement(((Wand)myWand).magicWord());
					try{ExternalPlay.doCommand(mob,V);}catch(Exception e){Log.errOut("CombatAbilities",e);}
				}
			}
		}
	}
}
