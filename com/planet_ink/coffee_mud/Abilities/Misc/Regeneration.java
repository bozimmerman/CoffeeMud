package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Regeneration extends StdAbility
{
	private static final int maxTickDown=3;
	private int regenTick=maxTickDown;

	public String ID() { return "Regeneration"; }
	public String name(){ return "Regeneration";}
	public String displayText(){ return "(Regeneration)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.BENEFICIAL_OTHERS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"REGENERATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Regeneration();}
	public int classificationCode(){return Ability.SKILL;}
	private int permanentDamage=0;


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((--regenTick)>0)
			return true;
		regenTick=maxTickDown;
		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob.location()==null) return true;
		if(mob.amDead()) return true;

		boolean doneAnything=false;
		doneAnything=doneAnything||mob.curState().adjHitPoints((int)Math.round(Util.div(mob.envStats().level(),2.0)),mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMana(mob.envStats().level()*2,mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMovement(mob.envStats().level()*3,mob.maxState());
		if(doneAnything)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> regenerate(s).");
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB M=(MOB)affected;
			if(msg.amISource(M)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
				permanentDamage=0;
				M.recoverMaxState();
			}
			else
			if((msg.amITarget(M))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(text().length()>0))
			{
				String text=text().toUpperCase();
				boolean hurts=false;
				if(msg.tool() instanceof Weapon)
				{
					int x=text.indexOf(Weapon.typeDescription[((Weapon)msg.tool()).weaponType()]);
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
						hurts=true;
					if(Sense.isABonusItems(msg.tool()))
					{
						x=text.indexOf("MAGIC");
						if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
							hurts=true;
					}
					x=text.indexOf("LEVEL");
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
					{
						String lvl=text.substring(x+5);
						if(lvl.indexOf(" ")>=0)
							lvl=lvl.substring(lvl.indexOf(" "));
						if(msg.tool().envStats().level()>=Util.s_int(lvl))
							hurts=true;
					}
					x=text.indexOf(EnvResource.RESOURCE_DESCS[((Weapon)msg.tool()).material()&EnvResource.RESOURCE_MASK]);
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
						hurts=true;
				}
				else
				if(msg.tool() instanceof Ability)
				{
					int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES;
					switch(classType)
					{
					case Ability.SPELL:
					case Ability.PRAYER:
					case Ability.CHANT:
					case Ability.SONG:
						{
							int x=text.indexOf("MAGIC");
							if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
								hurts=true;
						}
						break;
					default:
						break;
					}
				}
				if(hurts)
				{
					permanentDamage+=msg.value();
					M.recoverMaxState();
				}
			}

		}
		return true;
	}

	public void affectCharState(MOB mob, CharState state)
	{
		super.affectCharState(mob,state);
		state.setHitPoints(state.getHitPoints()-permanentDamage);
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You feel less regenerative.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			String str=auto?"":"<S-NAME> lay(s) regenerative magic upon <T-NAMESELF>.";
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,str);
			if(target.location().okMessage(target,msg))
			{
			    target.location().send(target,msg);
				success=beneficialAffect(mob,target,0);
			}
		}

        return success;

	}
}