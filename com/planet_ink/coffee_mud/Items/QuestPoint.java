package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class QuestPoint extends StdItem
{
	public String ID(){	return "QuestPoint";}
	public QuestPoint()
	{
		super();
		setName("a quest point");
		setDisplayText("A shiny blue coin has been left here.");
		myContainer=null;
		setDescription("A shiny blue coin with magical script around the edges.");
		myUses=Integer.MAX_VALUE;
		myWornCode=0;
		material=0;
		baseEnvStats.setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new QuestPoint();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
			{
				setContainer(null);
				destroy();
				if(!mob.isMine(this))
					mob.setQuestPoint(mob.getQuestPoint()+1);
				unWear();
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				return;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
