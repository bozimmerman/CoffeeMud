package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SensePlants extends Chant
{
	public String ID() { return "Chant_SensePlants"; }
	public String name(){return "Sense Plants";}
	public String displayText(){return "(Sensing Plants)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	Room lastRoom=null;
	protected String word(){return "plants";}
	public Environmental newInstance(){	return new Chant_SensePlants();	}
	private int[] myMats={EnvResource.MATERIAL_VEGETATION,
						  EnvResource.MATERIAL_WOODEN};
	protected int[] okMaterials(){	return myMats;}
	private int[] myRscs={EnvResource.RESOURCE_COTTON,
						  EnvResource.RESOURCE_HEMP};
	protected int[] okResources(){	return myRscs;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to "+word()+".");
	}
	public String itsHere(MOB mob, Room R)
	{
		if(R==null) return "";
		if((okMaterials()!=null)&&(okMaterials().length>0))
			for(int m=0;m<okMaterials().length;m++)
				if((R.myResource()&EnvResource.MATERIAL_MASK)==okMaterials()[m])
					return "You sense "+EnvResource.RESOURCE_DESCS[R.myResource()&EnvResource.RESOURCE_MASK].toLowerCase()+" here.";
		if((okResources()!=null)&&(okResources().length>0))
			for(int m=0;m<okResources().length;m++)
				if(R.myResource()==okResources()[m])
					return "You sense "+EnvResource.RESOURCE_DESCS[R.myResource()&EnvResource.RESOURCE_MASK].toLowerCase()+" here.";
		return "";
	}

	public void messageTo(MOB mob)
	{
		String here=itsHere(mob,mob.location());
		if(here.length()>0)
			mob.tell(here);
		else
		{
			String last="";
			String dirs="";
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R=mob.location().getRoomInDir(d);
				Exit E=mob.location().getExitInDir(d);
				if((R!=null)&&(E!=null)&&(itsHere(mob,R).length()>0))
				{
					if(last.length()>0)
						dirs+=", "+last;
					last=Directions.getFromDirectionName(d);
				}
			}
			if((dirs.length()==0)&&(last.length()>0))
				mob.tell("You sense "+word()+" to "+last+".");
			else
			if((dirs.length()>2)&&(last.length()>0))
				mob.tell("You sense "+word()+" to "+dirs.substring(2)+", and "+last+".");
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
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
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already sensing "+word()+".");
			return false;
		}
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) sensitivity to "+word()+"!":"^S<S-NAME> chant(s) and gain(s) sensitivity to "+word()+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		return success;
	}
}
