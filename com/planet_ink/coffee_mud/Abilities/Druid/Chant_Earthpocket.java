package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Earthpocket extends Chant
{
	public String ID() { return "Chant_Earthpocket"; }
	public String name(){return "Earthpocket";}
	public String displayText(){return "(Earthpocket: "+(super.tickDown/CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY))+")";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	private Container pocket=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			mob.tell("Your earthpocket fades away, dumping its contents into your inventory!");
			Vector V=pocket.getContents();
			for(int v=0;v<V.size();v++)
			{
				((Item)V.elementAt(v)).setContainer(null);
				mob.giveItem((Item)V.elementAt(v));
			}
			pocket.destroy();
			pocket=null;
		}
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.source()!=affected)
		&&((msg.target()==pocket)||(msg.tool()==pocket))
		&&(Util.bset(msg.sourceCode(),CMMsg.MASK_HANDS)
		   ||Util.bset(msg.sourceCode(),CMMsg.MASK_MOVE)
		   ||Util.bset(msg.sourceCode(),CMMsg.MASK_DELICATE)
		   ||Util.bset(msg.sourceCode(),CMMsg.MASK_MOUTH)))
		{
			msg.source().tell("The dark pocket draws away from you, preventing your action.");
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		movePocket();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		movePocket();
		return true;
	}

	public void movePocket()
	{
		if((affected instanceof MOB)&&(pocket!=null))
		{
			if(pocket.owner() instanceof MOB)
				pocket.removeFromOwnerContainer();
			else
			if(pocket.owner() instanceof Room)
			{
				if(((MOB)affected).location()!=null)
				{
					if(((MOB)affected).location().domainType()==Room.DOMAIN_INDOORS_CAVE)
					{
						if(Util.bset(pocket.baseEnvStats().disposition(),EnvStats.IS_NOT_SEEN))
						{
							pocket.baseEnvStats().setDisposition(pocket.baseEnvStats().disposition()-EnvStats.IS_NOT_SEEN);
							pocket.recoverEnvStats();
						}
						((MOB)affected).location().bringItemHere(pocket,0);
					}
					else
					if(!Util.bset(pocket.baseEnvStats().disposition(),EnvStats.IS_NOT_SEEN))
					{
						pocket.baseEnvStats().setDisposition(pocket.baseEnvStats().disposition()|EnvStats.IS_NOT_SEEN);
						pocket.recoverEnvStats();
					}
				}
				else
				if(!Util.bset(pocket.baseEnvStats().disposition(),EnvStats.IS_NOT_SEEN))
				{
					pocket.baseEnvStats().setDisposition(pocket.baseEnvStats().disposition()|EnvStats.IS_NOT_SEEN);
					pocket.recoverEnvStats();
				}
			}
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("The earthpocket can only be summoned or seen in a cave.");
			return false;
		}

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already connected with an earthpocket.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) for a connection with a mystical dimension!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				pocket=(Container)CMClass.getItem("GenContainer");
				pocket.setCapacity(Integer.MAX_VALUE);
				pocket.baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
				pocket.baseEnvStats().setWeight(0);
				pocket.setMaterial(EnvResource.RESOURCE_NOTHING);
				pocket.setName("an earthpocket");
				pocket.setDisplayText("an empty pitch-black pocket is in the wall here.");
				pocket.setDescription("It looks like an endless black hole in the wall.  Very mystical.");
				pocket.recoverEnvStats();
				target.location().addItem(pocket);
				beneficialAffect(mob,target,(int)CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*5);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"A dark pocket of energy appears in a nearby wall.");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing more happens.");

		// return whether it worked
		return success;
	}
}
