package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Burning extends StdAbility
{
	public String ID() { return "Burning"; }
	public String name(){ return "Burning";}
	public String displayText(){ return "(Burning)";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Burning();}

	private boolean reversed(){return profficiency()==100;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickDown<2)&&(affected!=null))
		{
			if(affected instanceof Item)
			{
				Environmental E=((Item)affected).owner();
				if(E==null)
					((Item)affected).destroyThis();
				else
				if(E instanceof Room)
				{
					Room room=(Room)E;
					if((affected instanceof EnvResource)
					&&(room.isContent((Item)affected)))
					{
						for(int i=0;i<room.numItems();i++)
						{
							Item I=room.fetchItem(i);
							if(I.name().equals(affected.name())
							&&(I instanceof EnvResource)
							&&(I.material()==((Item)affected).material()))
							{
								int durationOfBurn=5;
								switch(I.material()&EnvResource.MATERIAL_MASK)
								{
								case EnvResource.MATERIAL_LEATHER:
									durationOfBurn=20+I.envStats().weight();
									break;
								case EnvResource.MATERIAL_CLOTH:
								case EnvResource.MATERIAL_PAPER:
									durationOfBurn=5+I.envStats().weight();
									break;
								case EnvResource.MATERIAL_WOODEN:
									durationOfBurn=40+(I.envStats().weight()*2);
									break;
								}
								Burning B=new Burning();
								B.setProfficiency(durationOfBurn);
								B.invoke(invoker,I,true);
								break;
							}
						}
					}
					((Item)affected).destroyThis();
					((Room)E).recoverRoomStats();
				}
				else
				if(E instanceof MOB)
				{
					((Item)affected).destroyThis();
					((MOB)E).location().recoverRoomStats();
				}
				return false;
			}
		}
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Host.MOB_TICK)
			return true;

		if(affected==null)
			return false;

		if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
		{
			Item I=(Item)affected;
			if(!ouch((MOB)I.owner()))
				ExternalPlay.drop((MOB)I.owner(),I,false);
			if(I.subjectToWearAndTear())
			{
				if((I.usesRemaining()<1000)
				&&(I.usesRemaining()>1))
					I.setUsesRemaining(I.usesRemaining()-1);
			}
		}

		// might want to add the ability for it to spread
		return true;
	}

	public boolean ouch(MOB mob)
	{
		if(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_FIRE)-50))
		{
			mob.tell("Ouch!!, "+affected.name()+" is on fire!");
			ExternalPlay.postDamage(invoker,mob,this,Dice.roll(1,5,5),Affect.NO_EFFECT,Weapon.TYPE_BURNING,null);
			return false;
		}
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected!=null)
		&&(affected instanceof Item)
		&&(affect.amITarget((Item)affected))
		&&(affect.targetMinor()==Affect.TYP_GET))
			return ouch(affect.source());
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(affect.tool()!=null)
		&&(affect.tool()==affected)
		&&(affect.target()!=null)
		&&(affect.target() instanceof Container)
		&&(affect.targetMinor()==Affect.TYP_PUT))
		{
			Item I=(Item)affected;
			Item C=(Container)affect.target();
			if((C instanceof Drink)
			   &&(((Drink)C).containsDrink()))
			{
				affect.addTrailerMsg(new FullMsg(invoker,null,Affect.MSG_OK_VISUAL,I.name()+" is extinguished."));
				I.delAffect(this);
			}
		}
		super.affect(myHost,affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto)
	{
		if(!auto) return false;
		if(target==null) return false;
		if(target.fetchAffect("Burning")==null)
		{
			if((mob!=null)&&(mob.location()!=null))
			{
				FullMsg msg=new FullMsg(mob,target,Affect.MASK_GENERAL|Affect.TYP_FIRE,null);
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
			}
			beneficialAffect(mob,target,profficiency());
			target.recoverEnvStats();
			if(target instanceof Item)
			{
				((Item)target).owner().recoverEnvStats();
				if(((Item)target).owner() instanceof Room)
					((Room)((Item)target).owner()).recoverRoomStats();
				else
				if(((Item)target).owner() instanceof MOB)
					((MOB)((Item)target).owner()).location().recoverRoomStats();
			}
		}
		return true;
	}
}
