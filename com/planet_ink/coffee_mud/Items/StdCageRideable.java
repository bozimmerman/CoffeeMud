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
		setName("a cage wagon");
		setDisplayText("a cage wagon sits here.");
		setDescription("It\\`s of solid wood construction with metal bracings.  The door has a key hole.");
		capacity=5000;
		setContainTypes(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED);
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=15;
		baseEnvStats().setWeight(1000);
		rideBasis=Rideable.RIDEABLE_WAGON;
		recoverEnvStats();
	}



	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this))&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			MOB mob=msg.source();
			if(Sense.canBeSeenBy(this,mob))
			{
				StringBuffer buf=new StringBuffer("");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
					buf.append(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()
							       +"\n\rUses  :"+usesRemaining()
							       +"\n\rHeight: "+baseEnvStats().height()
							       +"\n\rAbilty:"+baseEnvStats().ability()
							       +"\n\rLevel :"+baseEnvStats().level()
							       +"\n\rDeath : "+dispossessionTimeLeftString()
							       +"\n\r"+description()+"'\n\rKey  : "+keyName()+"\n\rMisc  :'"+text());
				else
					buf.append(description()+"\n\r");
				if((isOpen)&&((capacity>0)||(getContents().size()>0)))
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
					buf.append(CMLister.niceLister(mob,newItems,true));
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
					buf.append(CMLister.niceLister(mob,newItems,true));
				}
				mob.tell(buf.toString());
			}
			else
				mob.tell("You can't see that!");
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.executeMsg(this,msg);
			}

			for(int a=0;a<numEffects();a++)
			{
				Ability A=fetchEffect(a);
				if(A!=null)
					A.executeMsg(this,msg);
			}
			return;
		}
		super.executeMsg(myHost,msg);
	}
}
