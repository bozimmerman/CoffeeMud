package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import com.planet_ink.coffee_mud.system.*;

public class GoodyBag extends BagOfEndlessness implements ArchonOnly
{
	public String ID(){	return "GoodyBag";}
	boolean alreadyFilled=false;
	public GoodyBag()
	{
		super();
		setName("a little bag");
		setDisplayText("a small bag is sitting here.");
		setDescription("A nice little bag to put your things in.");
		secretIdentity="The Archon's Goody Bag";
		recoverEnvStats();
	}

	private void putInBag(Item I)
	{
		I.setContainer(this);
		if(owner() instanceof Room)
		{
			((Room)owner()).addItem(I);
			I.setDispossessionTime(dispossessionTime());
		}
		else
		if(owner() instanceof MOB)
			((MOB)owner()).addInventory(I);
		I.recoverEnvStats();
	}

	public void addMoney(int value)
	{
		Container I=(Container)CMClass.getItem("GenContainer");
		I.setCapacity(1);
		I.setContainTypes(Container.CONTAIN_COINS);
		putInBag(I);
		Coins money=MoneyUtils.makeNote(value,this.owner(),I);
		I.setName(money.Name()+" sleeve");
		I.setDisplayText(money.Name()+" sleeve has been left here.");
		I.recoverEnvStats();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(getContents().size()==0)
			{
				addMoney(100);
				addMoney(500);
				addMoney(1000);
				addMoney(5000);
				addMoney(10000);
				addMoney(100000);
				addMoney(1000000);
				addMoney(10000000);
				Item I=CMClass.getItem("GenSuperPill");
				I.setName("a training pill");
				I.setDisplayText("A small round pill has been left here.");
				((Pill)I).setSpellList("train+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a practice pill");
				I.setDisplayText("A tiny little pill has been left here.");
				((Pill)I).setSpellList("prac+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a quest point pill");
				I.setDisplayText("A questy little pill has been left here.");
				((Pill)I).setSpellList("ques+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 100 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+100");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 500 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+500");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 1000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+1000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 2000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+2000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 5000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+5000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a strength pill");
				I.setDisplayText("An strong little pill has been left here.");
				((Pill)I).setSpellList("str+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("an intelligence pill");
				I.setDisplayText("An smart little pill has been left here.");
				((Pill)I).setSpellList("int+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a wisdom pill");
				I.setDisplayText("A wise little pill has been left here.");
				((Pill)I).setSpellList("wis+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a dexterity pill");
				I.setDisplayText("A quick little pill has been left here.");
				((Pill)I).setSpellList("dex+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a constitution pill");
				I.setDisplayText("A nutricious little pill has been left here.");
				((Pill)I).setSpellList("con+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a charisma pill");
				I.setDisplayText("A pretty little pill has been left here.");
				((Pill)I).setSpellList("cha+1");
				putInBag(I);
			}
		}
		super.executeMsg(myHost,msg);
	}
}