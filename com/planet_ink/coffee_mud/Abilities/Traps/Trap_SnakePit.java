package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_SnakePit extends Trap_RoomPit
{
	public String ID() { return "Trap_SnakePit"; }
	public String name(){ return "snake pit";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "some caged snakes";}
	public Environmental newInstance(){	return new Trap_SnakePit();}

	private Vector monsters=null;

	protected Item getCagedAnimal(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if(I instanceof CagedAnimal)
			{
				MOB M=((CagedAnimal)I).unCageMe();
				if((M!=null)&&(M.baseCharStats().getMyRace().racialCategory().equalsIgnoreCase("Serpent")))
					return I;
			}
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getCagedAnimal(mob);
		StringBuffer buf=new StringBuffer("<SNAKES>");
		int num=0;
		while((I!=null)&&((++num)<6))
		{
			I.destroy();
			buf.append(((CagedAnimal)I).cageText());
			I=getCagedAnimal(mob);
		}
		buf.append("</SNAKES>");
		setMiscText(buf.toString());
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down some caged snakes first.");
			return false;
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)&&(monsters!=null))
			{
				for(int i=0;i<monsters.size();i++)
				{
					MOB M=(MOB)monsters.elementAt(i);
					if(M.amDead()||(!M.isInCombat()))
						M.destroy();
				}
				monsters=null;
			}
		}
		return super.tick(ticking,tickID);
	}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=Dice.roll(trapLevel(),6,1);
			MUDFight.postDamage(invoker(),target,this,damage,CMMsg.MSG_OK_VISUAL,-1,null);
		}
		Vector snakes=new Vector();
		String t=text();
		int x=t.indexOf("</MOBITEM><MOBITEM>");
		while(x>=0)
		{
			snakes.addElement(t.substring(0,x+10));
			t=t.substring(x+10);
			x=t.indexOf("</MOBITEM><MOBITEM>");
		}
		if(t.length()>0) snakes.addElement(t);
		if(snakes.size()>0)
			monsters=new Vector();
		for(int i=0;i<snakes.size();i++)
		{
			t=(String)snakes.elementAt(i);
			Item I=CMClass.getItem("GenCaged");
			((CagedAnimal)I).setCageText(t);
			MOB monster=((CagedAnimal)I).unCageMe();
			if(monster!=null)
			{
				monsters.addElement(monster);
				monster.baseEnvStats().setRejuv(0);
				monster.bringToLife(target.location(),true);
				monster.setVictim(target);
				if(target.getVictim()==null)
					target.setVictim(monster);
			}
		}
		CommonMsgs.look(target,true);
	}
}
