package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.Vector;
import com.planet_ink.coffee_mud.Items.SmallSack;

public class BagOfEndlessness extends BagOfHolding
{
	public String ID(){	return "BagOfEndlessness";}
	public BagOfEndlessness()
	{
		super();

		setName("a small sack");
		setDisplayText("a small black sack is crumpled up here.");
		setDescription("A nice silk sack to put your things in.");
		secretIdentity="The Bag of Endless Stuff";
		baseEnvStats().setLevel(1);
		capacity=Integer.MAX_VALUE-1000;

		baseGoldValue=10000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			Item newitem=(Item)msg.tool();
			if((newitem.container()==this)
			&&(newitem.owner() !=null))
			{
				Item neweritem=(Item)newitem.copyOf();
				Vector allStuff=new Vector();
				allStuff.addElement(neweritem);
				if(newitem instanceof Container)
				{
					Vector V=((Container)newitem).getContents();
					for(int v=0;v<V.size();v++)
					{
						Item I=(Item)((Item)V.elementAt(v)).copyOf();
						I.setContainer(neweritem);
						allStuff.addElement(I);
					}
				}
				neweritem.setContainer(this);
				for(int i=0;i<allStuff.size();i++)
				{
					neweritem=(Item)allStuff.elementAt(i);
					if(newitem.owner() instanceof MOB)
						((MOB)newitem.owner()).addInventory(neweritem);
					else
					if(newitem.owner() instanceof Room)
					{
						((Room)newitem.owner()).addItem(neweritem);
						neweritem.setDispossessionTime(dispossessionTime());
					}
					neweritem.recoverEnvStats();
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
}
