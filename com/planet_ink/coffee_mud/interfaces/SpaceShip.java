package com.planet_ink.coffee_mud.interfaces;

public interface SpaceShip extends SpaceObject
{
	public void dockHere(Room R);
	public void unDock(boolean toSpace);
}
