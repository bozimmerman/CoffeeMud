package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CommonSkill extends StdAbility
{
	public String ID() { return "CommonSkill"; }
	public String name(){ return "Common Skill";}
	private static final String[] triggerStrings = empty;
	public String[] triggerStrings(){return triggerStrings;}

	public int quality(){return Ability.INDIFFERENT;}
	protected String displayText="(Doing something productive)";
	public String displayText(){return displayText;}

	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}
	protected int practicesToPractice(){return 1;}

	protected Room activityRoom=null;
	protected boolean aborted=false;
	protected boolean helping=false;
	protected CommonSkill helpingAbility=null;
	protected int tickUp=0;
	protected String verb="working";
	public int usageType(){return USAGE_MOVEMENT;}

	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public int classificationCode()	{	return Ability.COMMON_SKILL; }

	private int yield=1;
	public int abilityCode(){return yield;}
	public void setAbilityCode(int newCode){yield=newCode;}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())||(mob.location()!=activityRoom)||(!Sense.aliveAwakeMobile(mob,true)))
			{aborted=true; unInvoke(); return false;}
			if(tickDown==4)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb+".");
			else
			if((tickUp%4)==0)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb+".");
			if((helping)
			&&(helpingAbility!=null)
			&&(helpingAbility.affected instanceof MOB)
			&&(((MOB)helpingAbility.affected).isMine(helpingAbility)))
				helpingAbility.tick(helpingAbility.affected,tickID);
		}
		tickUp++;
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				if(aborted)
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stop(s) "+verb+".");
				else
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> done "+verb+".");
				helping=false;
				helpingAbility=null;
			}
		}
		super.unInvoke();
	}

	protected void commonTell(MOB mob, Environmental target, Environmental tool, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			if(target!=null) str=Util.replaceAll(str,"<T-NAME>",target.name());
			if(tool!=null)  str=Util.replaceAll(str,"<O-NAME>",tool.name());
			CommonMsgs.say(mob,null,str,false,false);
		}
		else
			mob.tell(mob,target,tool,str);
	}

	protected void commonTell(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			CommonMsgs.say(mob,null,str,false,false);
		}
		else
			mob.tell(str);
	}

	protected void commonEmote(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_GENERAL,str);
		else
			mob.tell(mob,null,null,str);
	}

	protected int lookingFor(int material, Room fromHere)
	{
		Vector V=new Vector();
		V.addElement(new Integer(material));
		return lookingFor(V,fromHere);
	}

	protected int lookingFor(Vector materials, Room fromHere)
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=fromHere.getRoomInDir(d);
			Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				int material=room.myResource();
				if(materials.contains(new Integer(material&EnvResource.MATERIAL_MASK)))
				{possibilities.addElement(new Integer(d));}
			}
		}
		if(possibilities.size()==0)
			return -1;
		else
			return ((Integer)(possibilities.elementAt(Dice.roll(1,possibilities.size(),-1)))).intValue();
	}

	public Item getRequiredFire(MOB mob,int autoGenerate)
	{
		if(autoGenerate>0) return CMClass.getItem("StdItem");
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return null;
		}
		return fire;
	}

	public int[] usageCost(MOB mob)
	{
		if(mob==null)
		{
			int[] usage=new int[3];
			usage[0]=overrideMana();
			usage[1]=overrideMana();
			usage[2]=overrideMana();
			return usage;
		}
		if(usageType()==Ability.USAGE_NADA) return new int[3];

		int consumed=25;
		int diff=mob.envStats().level()-CMAble.qualifyingLevel(mob,this);
		if(diff>0)
		switch(diff)
		{
		case 1: consumed=20; break;
		case 2: consumed=15; break;
		case 3: consumed=10; break;
		default: consumed=5; break;
		}
		if(overrideMana()>=0) consumed=overrideMana();
		return buildCostArray(mob,consumed);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			commonEmote(mob,"<S-NAME> <S-IS-ARE> in combat!");
			return false;
		}
		if(!Sense.canBeSeenBy(mob.location(),mob))
		{
			commonTell(mob,"<S-NAME> can't see to do that!");
			return false;
		}
		if(Sense.isSitting(mob)||Sense.isSleeping(mob))
		{
			commonTell(mob,"You need to stand up!");
			return false;
		}
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
			}
		}
		isAnAutoEffect=false;

		// if you can't move, you can't do anything!
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		int[] consumed=usageCost(mob);
		if(mob.curState().getMana()<consumed[0])
		{
			if(mob.maxState().getMana()==consumed[0])
				mob.tell("You must be at full mana to do that.");
			else
				mob.tell("You don't have enough mana to do that.");
			return false;
		}
		mob.curState().adjMana(-consumed[0],mob.maxState());
		if(mob.curState().getMovement()<consumed[1])
		{
			if(mob.maxState().getMovement()==consumed[1])
				mob.tell("You must be at full movement to do that.");
			else
				mob.tell("You don't have enough movement to do that.  You are too tired.");
			return false;
		}
		mob.curState().adjMovement(-consumed[1],mob.maxState());
		if(mob.curState().getHitPoints()<consumed[2])
		{
			if(mob.maxState().getHitPoints()==consumed[2])
				mob.tell("You must be at full health to do that.");
			else
				mob.tell("You don't have enough hit points to do that.");
			return false;
		}
		mob.curState().adjHitPoints(-consumed[2],mob.maxState());
		activityRoom=mob.location();
		
		
		helpProfficiency(mob);

		return true;
	}
}