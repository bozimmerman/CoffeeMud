package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_ContractHit extends ThiefSkill
{
	public String ID() { return "Thief_ContractHit"; }
	public String name(){ return "Contract Hit";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CONTRACTHIT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_ContractHit();}
	public String displayText(){return "";}
	private boolean done=false;
	private boolean readyToHit=false;
	private boolean hitting=false;
	private Vector hitmen=new Vector();

	public void affect(Environmental myHost, Affect msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
			if(msg.amISource((MOB)affected)&&(msg.sourceMinor()==Affect.TYP_DEATH))
			{
				done=true;
				unInvoke();
			}
		super.affect(myHost,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(invoker()!=null))
		{
			if(super.tickDown==1)
			{
				makeLongLasting();
				readyToHit=true;
			}
			MOB mob=(MOB)affected;
			if(readyToHit&&(!hitting)
			&&(mob.location()!=null)
			&&(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))
			{
				hitting=true;
				int num=Dice.roll(1,3,3);
				int level=mob.envStats().level();
				if(level>invoker.envStats().level()) level=invoker.envStats().level();
				CharClass C=CMClass.getCharClass("StdCharClass");
				if(C==null) C=mob.charStats().getCurrentClass();
				for(int i=0;i<num;i++)
				{
					MOB M=(MOB)CMClass.getMOB("Assassin");
					M.baseEnvStats().setLevel(level);
					M.recoverEnvStats();
					M.baseEnvStats().setArmor(C.getLevelArmor(M));
					M.baseEnvStats().setAttackAdjustment(C.getLevelAttack(M));
					M.baseEnvStats().setDamage(C.getLevelDamage(M));
					M.baseEnvStats().setRejuv(0);
					M.baseState().setMana(C.getLevelMana(M));
					M.baseState().setMovement(C.getLevelMana(M));
					M.baseState().setHitPoints((10*level)+Dice.roll(level,baseEnvStats().ability(),1));
					Behavior B=CMClass.getBehavior("Thiefness");
					B.setParms("Assassin");
					M.addBehavior(B);
					M.recoverEnvStats();
					M.recoverCharStats();
					M.recoverMaxState();
					M.text();
					hitmen.addElement(M);
					M.bringToLife(mob.location(),true);
					M.setVictim(mob);
					mob.setVictim(M);
					Ability A=M.fetchAbility("Thief_Hide");
					if(A!=null) A.invoke(M,M,true);
					A=M.fetchAbility("Thief_BackStab");
					if(A!=null) A.invoke(M,mob,false);
				}
			}
			else
			if(hitting)
			{
				boolean anyLeft=false;
				for(int i=0;i<hitmen.size();i++)
				{
					MOB M=(MOB)hitmen.elementAt(i);
					if((!M.amDead())&&(M.location()!=null)&&(Sense.aliveAwakeMobile(M,true)))
					{
						anyLeft=true;
						
						if(((M.getVictim()!=mob)||(!M.location().isInhabitant(mob)))
						&&(M.fetchAffect("Thief_Assassinate")==null))
						{
							M.setVictim(null);
							Ability A=M.fetchAbility("Thief_Assassinate");
							A.invoke(M,mob,false);
						}
					}
				}
				if(!anyLeft)
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		MOB M=invoker();
		super.unInvoke();
		if(done)
		{
			if(M.location()!=null)
			{
				if(M.location().show(M,null,Affect.MSG_OK_VISUAL,"Someone steps out of the shadows and whispers something to <S-NAME>."))
					M.tell("'It is done.'");
			}
		}
		for(int i=0;i<hitmen.size();i++)
		{
			M=(MOB)hitmen.elementAt(i);
			M.destroy();
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Who would you like to put a hit out on?");
			return false;
		}
		if(mob.location()==null) return false;
		if(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("You need to be on the streets to put out a hit.");
			return false;
		}
		Vector V=new Vector();
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			MOB M=R.fetchInhabitant(Util.combine(commands,0));
			if(M!=null)
				V.addElement(M);
		}
		MOB target=null;
		if(V.size()>0)
			target=(MOB)V.elementAt(Dice.roll(1,V.size(),-1));
		if(target==null)
		{
			mob.tell("You've never heard of '"+target.name()+"'.");
			return false;
		}
		if(target==mob)
		{
			mob.tell("You cannot hit yourself!");
			return false;
		}
		if(!mob.mayIFight(target))
		{
			mob.tell("You are not allowed to put out a hit on "+target.name()+".");
			return false;
		}
		
		int level=target.envStats().level();
		if(level>mob.envStats().level()) level=mob.envStats().level();
		int goldRequired=100*level;
		if(mob.getMoney()<goldRequired)
		{
			mob.tell("You'll need at least "+goldRequired+" gold to put a hit out on "+target.name()+".");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(levelDiff,auto);

		FullMsg msg=new FullMsg(mob,target,this,Affect.MASK_GENERAL|Affect.MSG_THIEF_ACT,Affect.MSG_THIEF_ACT,Affect.MSG_THIEF_ACT,"<S-NAME> whisper(s) to a dark figure stepping out of the shadows.  The person nods and slips away.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			
			mob.setMoney(mob.getMoney()-goldRequired);
			if(success)
				maliciousAffect(mob,target,target.envStats().level()+10,0);
		}
		return success;
	}

}