package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Burning extends StdAbility
{
	public Burning()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Burning";
		displayText="(Burning)";
		miscText="";

		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=0;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Burning();
	}
	
	private boolean reversed(){return profficiency()==100;}
	public boolean tick(int tickID)
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
		if(!super.tick(tickID))
			return false;

		if(tickID!=Host.MOB_TICK)
			return true;

		if(affected==null)
			return false;

		if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
		{
			if(!ouch((MOB)((Item)affected).owner()))
				ExternalPlay.drop((MOB)((Item)affected).owner(),(Item)affected);
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
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected!=null)
		&&(affected instanceof Item)
		&&(affect.amITarget((Item)affected))
		&&(affect.targetMinor()==Affect.TYP_GET))
			return ouch(affect.source());
		return true;
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
