package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Hippieness extends Chant
{
	public String ID() { return "Chant_Hippieness"; }
	public String name(){return "Hippieness";}
	public String displayText(){return "(Feeling Groovy)";}
	public int quality(){ return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_CURSE;}
	public Environmental newInstance(){ return new Chant_Hippieness();}
	private String oldClan="";

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.WISDOM,affectableStats.getStat(CharStats.WISDOM)-2);
		if(affectableStats.getStat(CharStats.WISDOM)<1)
			affectableStats.setStat(CharStats.WISDOM,1);
		if((oldClan.length()>0)&&(affected.getClanID().length()>0))
		   affected.setClanID("");
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(((MOB)affected).getClanID().length()>0)
		&&(oldClan.length()>0))
			((MOB)affected).setClanID("");
		
		if((msg.source()==affected)
		&&(msg.tool() instanceof Ability)
		&&((Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CRAFTING))
		   ||((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL)))
		{
			msg.source().tell("No, man... work is so bourgeois...");
			return false;
		}
		return super.okMessage(host,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			boolean mouthed=mob.fetchFirstWornItem(Item.ON_MOUTH)!=null;
			Room R=mob.location();
			if((!mouthed)&&(R!=null)&&(R.numItems()>0))
			{
				Item I=R.fetchItem(Dice.roll(1,R.numItems(),-1));
				if((I!=null)&&(I.fitsOn(Item.ON_MOUTH)))
					CommonMsgs.get(mob,I.container(),I,false);
			}
			
			if(mob.inventorySize()>0)
			{
				Item I=mob.fetchInventory(Dice.roll(1,mob.inventorySize(),-1));
				if(mouthed)
				{
					if((I!=null)&&(!I.amWearingAt(Item.INVENTORY))&&(!I.amWearingAt(Item.ON_MOUTH)))
						CommonMsgs.remove(mob,I,false);
				}
				else
				if((I!=null)&&(I instanceof Light)&&(I.fitsOn(Item.ON_MOUTH)))
				{
					if((I instanceof Container)
					&&(((Container)I).containTypes()==Container.CONTAIN_SMOKEABLES)
					&&(((Container)I).getContents().size()==0))
					{
						Item smoke=CMClass.getItem("GenResource");
						if(smoke!=null)
						{
							smoke.setName("some smoke");
							smoke.setDescription("Looks liefy and green.");
							smoke.setDisplayText("some smoke is sitting here.");
							smoke.setMaterial(EnvResource.RESOURCE_HEMP);
							smoke.baseEnvStats().setWeight(1);
							smoke.setBaseValue(25);
							smoke.recoverEnvStats();
							smoke.text();
							mob.addInventory(smoke);
							smoke.setContainer(I);
						}
					}
					mob.doCommand(Util.parse("WEAR \""+I.Name()+"\""));
				}
				else
				if((I!=null)&&(!I.amWearingAt(Item.INVENTORY))&&(!I.amWearingAt(Item.ON_MOUTH)))
					CommonMsgs.remove(mob,I,false);
			}
		}
		return true;
	}
	
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			if(oldClan.length()>0) mob.setClanID(oldClan);
			mob.tell("You don't feel quite so groovy.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(Sense.isAnimalIntelligence(target))
		{
			mob.tell(target.name()+" is not smart enough to be a hippy.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,(target.isMonster()?0:CMMsg.MASK_MALICIOUS)|affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,(target.isMonster()?0:CMMsg.MASK_MALICIOUS)|CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					oldClan=target.getClanID();
					target.setClanID("");
					CommonMsgs.say(target,null,"Far out...",false,false);
					maliciousAffect(mob,target,0,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
