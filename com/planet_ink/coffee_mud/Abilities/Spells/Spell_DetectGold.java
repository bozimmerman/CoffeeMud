package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectGold extends Spell
{
	Room lastRoom=null;
	public Spell_DetectGold()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Gold";
		displayText="(Detect Gold)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DetectGold();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		lastRoom=null;
		super.unInvoke();
		mob.tell(mob,null,"Your senses are no longer as golden.");
	}
	public String metalCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null) return "";
		if(I.location()==container)
		{
			if(((I.ID().equalsIgnoreCase("StdCoins"))
			||((I.ID().equalsIgnoreCase("GenCoins"))&&((I.material()==Item.METAL)||(I.material()==Item.MITHRIL)))
			&&(Sense.canBeSeenBy(I,mob))))
				msg.append(I.name()+" glows golden.\n\r");
		}
		else
		if((I.location()!=null)&&(I.location().location()==container))
			if(msg.toString().indexOf(I.location().name()+" contains some sort of gold.")<0)
				msg.append(I.location().name()+" contains some sort of gold.\n\r");
		return msg.toString();
	}
	public String metalHere(MOB mob, Environmental E, Item container)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((Room)E).numItems();i++)
			{
				Item I=((Room)E).fetchItem(i);
				metalCheck(mob,I,container,msg);
			}
		}
		else
		if((E instanceof Item)&&(Sense.canBeSeenBy(E,mob)))
		{
			metalCheck(mob,(Item)E,container,msg);
			msg.append(metalHere(mob,((Item)E).myOwner(),(Item)E));
		}
		else
		if((E instanceof MOB)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				if(!I.amWearingAt(Item.INVENTORY))
					metalCheck(mob,I,container,msg);
			}
			if(((MOB)E).getMoney()>0)
				msg.append(E.name()+" is carrying some gold.");
		}
		return msg.toString();
	}
	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<=Directions.NUM_DIRECTIONS;d++)
		{
			Room R=null;
			Exit E=null;
			if(d<Directions.NUM_DIRECTIONS)
			{
				R=mob.location().doors()[d];
				E=mob.location().exits()[d];
			}
			else
			{
				R=mob.location();
				E=CMClass.getExit("StdExit");
			}
			if((R!=null)&&(E!=null))
			{
				boolean metalFound=false;
				if(metalHere(mob,R,null).length()>0)
					metalFound=true;
				else
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=mob)&&(metalHere(mob,M,null).length()>0))
					{ metalFound=true; break;}
				}
				
				if(metalFound)
				{
					if(last.length()>0)
						dirs+=", "+last;
					if(d>=Directions.NUM_DIRECTIONS)
						last="here";
					else
						last=Directions.getFromDirectionName(d);
				}
			}
		}

		if((dirs.length()!=0)||(last.length()!=0))
		{
			if(dirs.length()==0)
				mob.tell("You sense golden emanations coming from "+last+".");
			else
				mob.tell("You sense golden emanations coming from "+dirs.substring(2)+", and "+last+".");
		}
	}
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}
	

	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(affect.target()!=null)
		   &&(affect.amISource((MOB)affected))
		   &&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			if((affect.tool()!=null)&&(affect.tool().ID().equals(ID())))
			{
				String msg=metalHere((MOB)affected,affect.target(),null);
				if(msg.length()>0)
					((MOB)affected).tell(msg);
			}
			else
			{
				FullMsg msg=new FullMsg(affect.source(),affect.target(),this,affect.MSG_EXAMINESOMETHING,affect.NO_EFFECT,affect.NO_EFFECT,null);
				affect.addTrailerMsg(msg);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting golden things.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> gain(s) golden senses!":"<S-NAME> chant(s) for golden senses!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> open(s) <S-HER-HER> golden eyes, but the spell fizzles.");

		return success;
	}
}