package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CurseItem extends Prayer
{
	public Prayer_CurseItem()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Curse Item";
		displayText="(Cursed Item)";
		holyQuality=Prayer.HOLY_EVIL;
		quality=Ability.MALICIOUS;


		baseEnvStats().setLevel(24);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CurseItem();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_EVIL);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			affectableStats.setArmor(affectableStats.armor()+10+mob.envStats().level());
		}
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if((affected instanceof Item)&&(((Item)affected).myOwner()!=null)&&(((Item)affected).myOwner() instanceof MOB))
				((MOB)((Item)affected).myOwner()).tell("The curse on "+((Item)affected).name()+" is lifted.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		mob.tell("The curse is lifted.");
		super.unInvoke();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;

		Item item=(Item)affected;

		MOB mob=affect.source();
		if(!affect.amITarget(item))
			return true;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_GET:
			if(!item.amWearingAt(Item.INVENTORY))
			{
				if(item.amWearingAt(Item.WIELD)||item.amWearingAt(Item.HELD))
				{
					mob.tell("You can't seem to let go of "+item.name()+".");
					return false;
				}
				mob.tell("You can't seem to remove "+item.name()+".");
				return false;
			}
			break;
		case Affect.TYP_DROP:
			mob.tell("You can't seem to get rid of "+item.name()+".");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true);
		Item target=null;
		if(mobTarget!=null)
		{
			Vector goodPossibilities=new Vector();
			Vector possibilities=new Vector();
			for(int i=0;i<mobTarget.inventorySize();i++)
			{
				Item item=mobTarget.fetchInventory(i);
				if((item!=null)
				   &&(item.location()==null))
				{
					if(item.amWearingAt(Item.INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					target=(Item)goodPossibilities.elementAt(Dice.roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					target=(Item)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1));
			}
		}
		
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands);
		
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> is cursed!":"<S-NAME> curse(s) <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType,null);
			if((mob.location().okAffect(msg))&&((mobTarget==null)||(mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					int a=0;
					while(a<target.numAffects())
					{
						Ability A=target.fetchAffect(a);
						if(A!=null)
						{
							int b=target.numAffects();
							if(A instanceof Prayer_BlessItem)
								A.unInvoke();
							if(A instanceof Prayer_Bless)
								A.unInvoke();
							if(b==target.numAffects())
								a++;
						}
						else
							a++;
					}
					target.recoverEnvStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
