package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AnimalTraining extends CommonSkill
{
	public String ID() { return "AnimalTraining"; }
	public String name(){ return "Animal Training";}
	private static final String[] triggerStrings = {"ANIMALTRAINING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Environmental taming=null;
	private String skillto="";
	private Object skill=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public AnimalTraining()
	{
		super();
		displayText="You are taming...";
		verb="taming";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",1,ID(),false);
		}
	}
	public Environmental newInstance(){	return new AnimalTraining();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB))
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
					if((messedUp)||(animal==null)||(skill==null))
						commonTell(mob,"You've failed to train "+taming.name()+"!");
					else
					{
						if(animal.numBehaviors()==0)
							commonTell(mob,taming.name()+" is already tame.");
						else
						{
							String s=" to "+skillto;
							mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to train "+animal.name()+" "+s+".");
							if(skill instanceof Behavior)
								animal.addBehavior((Behavior)skill);
							else
							if(skill instanceof Ability)
							{
								((Ability)skill).setProfficiency(100);
								animal.addAbility((Ability)skill);
							}
							animal.recoverCharStats();
							animal.recoverEnvStats();
							animal.recoverMaxState();
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
		verb="training";
		taming=null;
		Item cage=null;
		String[] skills={"WANDER", //0
						 "HUNT", //1
						 "KILL", //2
						 "DOORGUARD" //3
		};
		String valid="Skills include:";
		for(int i=0;i<skills.length;i++)
			valid+=" "+skills[i];
		valid+=".";
		if(commands.size()<2)
		{
			commonTell(mob,"Train whom to do what? "+valid);
			return false;
		}
		skill=null;
		String what=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		for(int i=0;i<skills.length;i++)
		{
			if(skills[i].startsWith(what.toUpperCase()))
			{
				switch(i)
				{
				case 0:
					skill=CMClass.getBehavior("Mobile");
					break;
				case 1:
					skill=CMClass.getAbility("Hunt");
					break;
				case 2:
					skill=CMClass.getBehavior("Aggressive");
					break;
				case 3:
					skill=CMClass.getBehavior("DoorwayGuardian");
					break;
				}
				if(skill!=null)
					skillto=skills[i].toLowerCase();
			}
		}
		if(skill==null)
		{
			commonTell(mob,"Train whom to do what? "+valid);
			return false;
		}

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
			if((!M.isMonster())||(!Sense.isAnimalIntelligence(M)))
			{
				commonTell(mob,"You can't train "+M.name()+".");
				return false;
			}
			if((Sense.canMove(M))&&(!Sense.isBoundOrHeld(M)))
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

		if(((skill instanceof Behavior)&&(M.fetchBehavior(((Behavior)skill).ID())!=null))
		||(skill instanceof Ability)&&(M.fetchAbility(((Ability)skill).ID())!=null))
		{
			commonTell(mob,M.name()+" already knows how to do that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		messedUp=!profficiencyCheck(mob,-taming.envStats().level(),auto);
		int duration=35+taming.envStats().level()-mob.envStats().level();
		if(duration<10) duration=10;
		verb="training "+M.name();
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) training "+M.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}