package com.planet_ink.coffee_mud.interfaces;

public interface ShipComponent extends Item
{
	public final static int COMPONENT_MISC=0;
	public final static int COMPONENT_PANEL_ANY=1;
	public final static int COMPONENT_POWER=2;
	public final static int COMPONENT_COMPUTER=3;
	public final static int COMPONENT_ENGINE=4;
	public final static int COMPONENT_WEAPON=5;
	public final static int COMPONENT_SENSOR=6;
	public final static int COMPONENT_PANEL_WEAPON=6;
	public final static int COMPONENT_PANEL_ENGINE=7;
	public final static int COMPONENT_PANEL_SENSOR=8;
	public final static int COMPONENT_PANEL_POWER=9;
	public final static int COMPONENT_PANEL_COMPUTER=10;
	public final static String[] COMPONENT_DESC={
		"MISC","PANEL-ANY","POWER","COMPUTER","ENGINE","WEAPON","SENSOR",
		"PANEL-WEAPON","PANEL-ENGINE","PANEL-SENSOR","PANEL-POWER", "PANEL-COMPUTER"
	};
	
	
	public int componentType();
	public void setComponentType(int type);
	
	
}
