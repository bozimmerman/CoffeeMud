package com.planet_ink.coffee_mud.interfaces;

public interface I3Interface
{
	public void i3who(MOB mob, String mudName);
	public void i3tell(MOB mob, String tellName, String mudName, String message);
	public void i3channel(MOB mob, String channelName, String message);
	public void i3locate(MOB mob, String mobName);
	public void giveI3MudList(MOB mob);
	public void giveIMC2MudList(MOB mob);
	public void registerIMC2(Object O);
	public boolean isI3channel(String channelName);
	public boolean isIMC2channel(String channelName);
	public boolean i3online();
	public boolean imc2online();
	public void i3chanwho(MOB mob, String channel, String mudName);
	public void giveI3ChannelsList(MOB mob);
	public void giveIMC2ChannelsList(MOB mob);
	public void i3channelAdd(MOB mob, String channel);
	public void i3channelListen(MOB mob, String channel);
	public void i3channelSilence(MOB mob, String channel);
	public void i3channelRemove(MOB mob, String channel);
	public void i3mudInfo(MOB mob, String parms);
	public void imc2mudInfo(MOB mob, String parms);
}
