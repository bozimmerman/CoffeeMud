package com.planet_ink.coffee_mud.interfaces;

public interface I3Interface
{
	public void i3who(MOB mob, String mudName);
	public void i3tell(MOB mob, String tellName, String mudName, String message);
	public void i3channel(MOB mob, String channelName, String message);
	public void i3locate(MOB mob, String mobName);
	public void giveMudList(MOB mob);
	public boolean isI3channel(String channelName);
	public boolean i3online();
	public void i3chanwho(MOB mob, String channel, String mudName);
	public void giveChannelsList(MOB mob);
}
