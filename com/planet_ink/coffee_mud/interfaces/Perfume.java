package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Perfume
{
	public Vector getSmellEmotes(Perfume me);
	public String getSmellList();
	public void setSmellList(String list);
	public void wearIfAble(MOB mob, Perfume me);
}
