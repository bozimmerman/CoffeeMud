package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ring_Ornamental extends Ring
{
	public String ID(){	return "Ring_Ornamental";}
	public final static int GOLD_RING 					= 0;
	public final static int SILVER_RING					= 1;
	public final static int COPPER_RING		  			= 2;
	public final static int PLATINUM_RING				= 3;
	public final static int GOLD_RING_DIAMOND			= 4;
	public final static int GOLD_RING_RUBY				= 5;
	public final static int GOLD_RING_OPAL				= 6;
	public final static int GOLD_RING_TOPAZ				= 7;
	public final static int GOLD_RING_SAPPHIRE			= 8;
	public final static int MITHRIL_RING	 			= 9;
	public final static int GOLD_RING_PEARL				= 10;
	public final static int GOLD_RING_EMERALD			= 11;
	public final static int STEEL_RING					= 12;
	public final static int BRONZE_RING					= 13;

	private int lastLevel=-1;

	public Ring_Ornamental()
	{
		super();

		int ringType = Dice.roll(1,14,-1);
		this.baseEnvStats.setLevel(ringType);
		setItemDescription(this.baseEnvStats.level());
		lastLevel=ringType;
		recoverEnvStats();
	}


	public void recoverEnvStats()
	{
		if(lastLevel!=baseEnvStats().level())
		{
			setItemDescription(baseEnvStats().level());
			lastLevel=baseEnvStats().level();
		}
		super.recoverEnvStats();
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case SILVER_RING:
				setName("a silver ring");
				setDisplayText("a silver ring is on the ground.");
				setDescription("It is a fancy silver ring inscribed with shields.");
				baseGoldValue=5;
				material=EnvResource.RESOURCE_SILVER;
				break;
			case COPPER_RING:
				setName("a copper ring");
				setDisplayText("a copper ring is on the ground.");
				setDescription("It is a fancy copper ring inscribed with runes.");
				baseGoldValue=1;
				material=EnvResource.RESOURCE_COPPER;
				break;
			case PLATINUM_RING:
				setName("a platinum ring");
				setDisplayText("a platinum ring is on the ground.");
				setDescription("It is a fancy platinum ring inscribed with ornate symbols.");
				baseGoldValue=500;
				material=EnvResource.RESOURCE_PLATINUM;
				break;
			case GOLD_RING_DIAMOND:
				setName("a diamond ring");
				setDisplayText("a diamond ring is on the ground.");
				setDescription("It is a fancy gold ring with a diamond inset.");
				baseGoldValue=1000;
				material=EnvResource.RESOURCE_DIAMOND;
				break;
			case GOLD_RING:
				setName("a gold ring");
				setDisplayText("a golden ring is on the ground.");
				setDescription("It is a simple gold ");
				baseGoldValue=50;
				material=EnvResource.RESOURCE_GOLD;
				break;
			case GOLD_RING_RUBY:
				setName("a ruby ring");
				setDisplayText("a ruby ring is on the ground.");
				setDescription("It is a fancy gold ring with a ruby inset.");
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_OPAL:
				setName("a opal ring");
				setDisplayText("an opal ring is on the ground.");
				setDescription("It is a fancy gold ring with an opal inset.");
				baseGoldValue=75;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_TOPAZ:
				setName("a diamond ring");
				setDisplayText("a diamond ring is on the ground.");
				setDescription("It is a fancy gold ring with a diamond inset.");
				baseGoldValue=65;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_SAPPHIRE:
				setName("a sapphire ring");
				setDisplayText("a sapphire ring is on the ground.");
				setDescription("It is a fancy gold ring with a sapphire inset.");
				baseGoldValue=200;
				material=EnvResource.RESOURCE_GEM;
				break;
			case MITHRIL_RING:
				setName("a mithril ring");
				setDisplayText("a mithril ring is on the ground.");
				setDescription("It is a fancy mithril ring.");
				baseGoldValue=20;
				material=EnvResource.RESOURCE_MITHRIL;
				break;
			case GOLD_RING_PEARL:
				setName("a pearl ring");
				setDisplayText("a pearl ring is on the ground.");
				setDescription("It is a fancy gold ring with a pearl inset.");
				baseGoldValue=65;
				material=EnvResource.RESOURCE_PEARL;
				break;
			case GOLD_RING_EMERALD:
				setName("a emerald ring");
				setDisplayText("a emerald ring is on the ground.");
				setDescription("It is a fancy gold ring with an emerald inset.");
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case BRONZE_RING:
				setName("a bronze ring");
				setDisplayText("a bronze ring is on the ground.");
				setDescription("It is a simple broze ring.");
				baseGoldValue=2;
				material=EnvResource.RESOURCE_BRONZE;
				break;
			default:
				setName("a metal ring");
				setDisplayText("a simple steel ring is on the ground.");
				setDescription("It is a simple steel ring.");
				material=EnvResource.RESOURCE_STEEL;
				break;
		}
	}
}
