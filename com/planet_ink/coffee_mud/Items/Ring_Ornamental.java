package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ring_Ornamental extends Ring
{
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

	public Ring_Ornamental()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);

		int ringType = Dice.roll(1,14,-1);

		this.envStats.setLevel(ringType);
		setItemDescription(this.envStats.level());
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Ring_Ornamental();
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case SILVER_RING:
				name="a silver ring";
				displayText="a silver ring is on the ground.";
				description="It is a fancy silver ring inscribed with shields.";
				baseGoldValue=5;
				material=EnvResource.RESOURCE_SILVER;
				break;
			case COPPER_RING:
				name="a copper ring";
				displayText="a copper ring is on the ground.";
				description="It is a fancy copper ring inscribed with runes.";
				baseGoldValue=1;
				material=EnvResource.RESOURCE_COPPER;
				break;
			case PLATINUM_RING:
				name="a platinum ring";
				displayText="a platinum ring is on the ground.";
				description="It is a fancy platinum ring inscribed with ornate symbols.";
				baseGoldValue=500;
				material=EnvResource.RESOURCE_PLATINUM;
				break;
			case GOLD_RING_DIAMOND:
				name="a diamond ring";
				displayText="a diamond ring is on the ground.";
				description="It is a fancy gold ring with a diamond inset.";
				baseGoldValue=1000;
				material=EnvResource.RESOURCE_DIAMOND;
				break;
			case GOLD_RING:
				name="a gold ring";
				displayText="a golden ring is on the ground.";
				description="It is a simple gold ";
				baseGoldValue=50;
				material=EnvResource.RESOURCE_GOLD;
				break;
			case GOLD_RING_RUBY:
				name="a ruby ring";
				displayText="a ruby ring is on the ground.";
				description="It is a fancy gold ring with a ruby inset.";
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_OPAL:
				name="a opal ring";
				displayText="an opal ring is on the ground.";
				description="It is a fancy gold ring with an opal inset.";
				baseGoldValue=75;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_TOPAZ:
				name="a diamond ring";
				displayText="a diamond ring is on the ground.";
				description="It is a fancy gold ring with a diamond inset.";
				baseGoldValue=65;
				material=EnvResource.RESOURCE_GEM;
				break;
			case GOLD_RING_SAPPHIRE:
				name="a sapphire ring";
				displayText="a sapphire ring is on the ground.";
				description="It is a fancy gold ring with a sapphire inset.";
				baseGoldValue=200;
				material=EnvResource.RESOURCE_GEM;
				break;
			case MITHRIL_RING:
				name="a mithril ring";
				displayText="a mithril ring is on the ground.";
				description="It is a fancy mithril ring.";
				baseGoldValue=20;
				material=EnvResource.RESOURCE_MITHRIL;
				break;
			case GOLD_RING_PEARL:
				name="a pearl ring";
				displayText="a pearl ring is on the ground.";
				description="It is a fancy gold ring with a pearl inset.";
				baseGoldValue=65;
				material=EnvResource.RESOURCE_PEARL;
				break;
			case GOLD_RING_EMERALD:
				name="a emerald ring";
				displayText="a emerald ring is on the ground.";
				description="It is a fancy gold ring with an emerald inset.";
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case BRONZE_RING:
				name="a bronze ring";
				displayText="a bronze ring is on the ground.";
				description="It is a simple broze ring.";
				baseGoldValue=2;
				material=EnvResource.RESOURCE_BRONZE;
				break;
			default:
				name="a metal ring";
				displayText="a simple steel ring is on the ground.";
				description="It is a simple steel ring.";
				material=EnvResource.RESOURCE_STEEL;
				break;
		}
	}
}
