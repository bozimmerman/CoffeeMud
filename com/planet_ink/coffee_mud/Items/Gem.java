package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gem extends StdItem
{
	public String ID(){	return "Gem";}
	public final static int QUARTZ 			= 0;
	public final static int AZURITE			= 1;
	public final static int BLOODSTONE		= 2;
	public final static int JADE			= 3;
	public final static int DIAMOND			= 4;
	public final static int RUBY			= 5;
	public final static int OPAL			= 6;
	public final static int TOPAZ			= 7;
	public final static int SAPPHIRE		= 8;
	public final static int ONYX	 		= 9;
	public final static int PEARL			= 10;
	public final static int EMERALD			= 11;
	public final static int AMETHYST		= 12;

	public Gem()
	{
		super();


		Random randomizer = new Random(System.currentTimeMillis());
		int ringType = Math.abs(randomizer.nextInt() % 12);

		this.envStats.setLevel(ringType);
		setItemDescription(this.envStats.level());
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Gem();
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case AZURITE:
				name="a piece of azurite";
				displayText="a piece of azurite lies here.";
				description="A piece of blue stone.";
				baseGoldValue=20;
				material=EnvResource.RESOURCE_GEM;
				break;
			case BLOODSTONE:
				name="a bloodstone";
				displayText="a bloodstone lies here.";
				description="It dark grey stone with flecks of red.";
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case JADE:
				name="a jade stone";
				displayText="a jade stone lies here.";
				description="A beutiful green stone.";
				baseGoldValue=200;
				material=EnvResource.RESOURCE_JADE;
				break;
			case DIAMOND:
				name="a diamond";
				displayText="a diamond lies here.";
				description="Finely cut and sparkling.";
				baseGoldValue=5000;
				material=EnvResource.RESOURCE_DIAMOND;
				break;
			case QUARTZ:
				name="a piece of quartz";
				displayText="a piece of quartz lies here.";
				description="It is a glasslike stone, gorgeous to the eye.";
				baseGoldValue=30;
				material=EnvResource.RESOURCE_CRYSTAL;
				break;
			case RUBY:
				name="a ruby";
				displayText="a ruby lies here.";
				description="A beautiful red ruby with a smooth surface.";
				baseGoldValue=5000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case OPAL:
				name="an opal";
				displayText="an opal lies here.";
				description="Pale blue and lovely.";
				baseGoldValue=2000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case TOPAZ:
				name="a piece of topaz";
				displayText="a piece of topaz lies here.";
				description="A yellow stone.";
				baseGoldValue=500;
				material=EnvResource.RESOURCE_GEM;
				break;
			case SAPPHIRE:
				name="a sapphire";
				displayText="a sapphire lies here.";
				description="Clear, blue, and very fancy.";
				baseGoldValue=1000;
				material=EnvResource.RESOURCE_GEM;
				break;
			case ONYX:
				name="an onyx stone";
				displayText="a onyx stone lies here.";
				description="A beautiful rich black stone.";
				baseGoldValue=100;
				material=EnvResource.RESOURCE_GEM;
				break;
			case PEARL:
				name="a pearl";
				displayText="a pearl lies here.";
				description="Perfectly round, pure and white.";
				baseGoldValue=300;
				material=EnvResource.RESOURCE_PEARL;
				break;
			case EMERALD:
				name="an emerald";
				displayText="an emerald lies here.";
				description="A beautiful clear green stone.";
				baseGoldValue=5000;
				material=EnvResource.RESOURCE_GEM;
				break;
			default:
				name="a hunk of metal";
				displayText="a hunk of steel ring is on the ground.";
				description="It is a simple steel ring.";
				material=EnvResource.RESOURCE_STEEL;
				break;
		}
	}
}
