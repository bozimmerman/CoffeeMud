package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdCageRideable extends StdRideable
{
	public String ID(){	return "StdCageRideable";}
	public StdCageRideable()
	{
		super();
		name="a cage wagon";
		displayText="a cage wagon sits here.";
		description="It\\`s of solid wood construction with metal bracings.  The door has a key hole.";
		capacity=5000;
		setContainTypes(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED);
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=15;
		baseEnvStats().setWeight(1000);
		rideBasis=Rideable.RIDEABLE_WAGON;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdCageRideable();
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		if((affect.amITarget(this))&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			MOB mob=affect.source();
			if(Sense.canBeSeenBy(this,mob))
			{
				StringBuffer buf=new StringBuffer("");
				if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
					buf.append(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight: "+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rDeath : "+dispossessionTimeLeftString()+"\n\r"+description()+"'\n\rKey  : "+keyName()+"\n\rMisc  :'"+text());
				else
					buf.append(description()+"\n\r");
				buf.append(name()+" contains:\n\r");
				Vector newItems=new Vector();
							
				if(mob.isMine(this))
				{
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item item=mob.fetchInventory(i);
						if((item!=null)&&(item.container()==this))
							newItems.addElement(item);
					}
					buf.append(ExternalPlay.niceLister(mob,newItems,true));
				}
				else
				{
					Room room=mob.location();
					if(room!=null)
					for(int i=0;i<room.numItems();i++)
					{
						Item item=room.fetchItem(i);
						if((item!=null)&&(item.container()==this))
							newItems.addElement(item);
					}
					buf.append(ExternalPlay.niceLister(mob,newItems,true));
				}
				mob.tell(buf.toString());
			}
			else
				mob.tell("You can't see that!");
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.affect(this,affect);
			}

			for(int a=0;a<numAffects();a++)
			{
				Ability A=fetchAffect(a);
				if(A!=null)
					A.affect(this,affect);
			}
			return;
		}
		super.affect(myHost,affect);
	}
}
