package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AnimalTaming extends CommonSkill
{
	public String ID() { return "AnimalTaming"; }
	public String name(){ return "Animal Taming";}
	private static final String[] triggerStrings = {"TAME","TAMING","ANIMALTAMING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Environmental taming=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public AnimalTaming()
	{
		super();
		displayText="You are taming...";
		verb="taming";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",1,ID(),false);
		}
	}
	public Environmental newInstance(){	return new AnimalTaming();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((taming==null)||(mob.location()==null))
				unInvoke();
			if((taming instanceof MOB)&&(!mob.location().isInhabitant((MOB)taming)))
				unInvoke();
			if((taming instanceof Item)&&(!mob.location().isContent((Item)taming)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((taming!=null)&&(!aborted))
				{
					MOB animal=null;
					if(taming instanceof MOB)
						animal=(MOB)taming;
					else
					if((taming!=null)&&(taming instanceof CagedAnimal))
						animal=((CagedAnimal)taming).unCageMe();
					if((messedUp)||(animal==null))
						commonTell(mob,"You've failed to tame "+taming.name()+"!");
					else
					{
						if(animal.numBehaviors()==0)
							commonTell(mob,taming.name()+" is already tame.");
						else
						{
							int amount=1;
							amount=amount*(abilityCode());
							if(amount>animal.numBehaviors())
								amount=animal.numBehaviors();
							String s="";
							if(amount>1)
								s="of "+amount+" of ";
							s+="of "+animal.charStats().himher()+" behaviors";
							mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to tame "+animal.name()+" "+s+".");
							for(int i=0;i<amount;i++)
							{
								if(animal.numBehaviors()==0) break;
								Behavior B=animal.fetchBehavior(Dice.roll(1,animal.numBehaviors(),-1));
								if(B!=null)	animal.delBehavior(B);
								animal.recoverCharStats();
								animal.recoverEnvStats();
								animal.recoverMaxState();
							}
							if(taming instanceof CagedAnimal)
							{
								animal.text();
								((CagedAnimal)taming).cageMe(animal);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="taming";
		taming=null;
		Item cage=null;
		String str=Util.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		taming=null;
		if(M!=null)
		{
			if(!Sense.canBeSeenBy(M,mob))
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			if((!M.isMonster())
			   ||(!Sense.isAnimalIntelligence(M)))
			{
				commonTell(mob,"You can't tame "+M.name()+".");
				return false;
			}
			if((Sense.canMove(M))&&(!Sense.isBound(M)))
			{
				commonTell(mob,M.name()+" doesn't seem willing to cooperate.");
				return false;
			}
			taming=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Item I=mob.location().fetchItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.removeElement(last);
				}
			}
			if(cage==null)
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			taming=mob.location().fetchItem(cage,Util.combine(commands,0));
			if((taming==null)||(!Sense.canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTell(mob,"You don't see any creatures in "+cage.name()+" called '"+Util.combine(commands,0)+"'.");
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		messedUp=!profficiencyCheck(-taming.envStats().level(),auto);
		int duration=35+taming.envStats().level()-mob.envStats().level();
		if(duration<10) duration=10;
		verb="taming "+M.name();
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) taming "+M.name()+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}