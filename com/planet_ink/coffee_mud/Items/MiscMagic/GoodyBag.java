package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import com.planet_ink.coffee_mud.system.*;

public class GoodyBag extends BagOfEndlessness
{
	public String ID(){	return "GoodyBag";}
	boolean alreadyFilled=false;
	public GoodyBag()
	{
		super();
		secretIdentity="The Archon's Goody Bag";
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GoodyBag();
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
		Coins money=Money.makeNote(value,this.owner(),I);
		I.setName(money.name()+" sleeve");
		I.setDisplayText(money.name()+" sleeve has been left here.");
		I.recoverEnvStats();
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(getContents().size()==0)
			{
				putInBag(CMClass.getItem("QuestPoint"));
				addMoney(10);
				addMoney(100);
				addMoney(500);
				addMoney(1000);
				addMoney(5000);
				addMoney(10000);
				addMoney(100000);
				addMoney(1000000);
				addMoney(10000000);
				Money.makeNote(10,this.owner(),this);
				Money.makeNote(100,this.owner(),this);
				Money.makeNote(500,this.owner(),this);
				Money.makeNote(1000,this.owner(),this);
				Money.makeNote(5000,this.owner(),this);
				Money.makeNote(10000,this.owner(),this);
				Money.makeNote(100000,this.owner(),this);
				Money.makeNote(1000000,this.owner(),this);
				Money.makeNote(10000000,this.owner(),this);
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
		super.affect(myHost,affect);
	}
}