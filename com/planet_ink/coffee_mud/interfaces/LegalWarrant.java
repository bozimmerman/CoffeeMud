package com.planet_ink.coffee_mud.interfaces;

public interface LegalWarrant
{
	public void setArrestingOfficer(MOB mob);
	public MOB criminal();
	public MOB victim();
	public MOB witness();
	public MOB arrestingOfficer();
	public Room jail();
	public Room releaseRoom();
	public String crime();
	public int actionCode();
	public int jailTime();
	public int state();
	public int offenses();
	public long lastOffense();
	public long travelAttemptTime();
	public String warnMsg();
	public void setCriminal(MOB mob);
	public void setVictim(MOB mob);
	public void setWitness(MOB mob);
	public void setJail(Room R);
	public void setReleaseRoom(Room R);
	public void setCrime(String crime);
	public void setActionCode(int code);
	public void setJailTime(int time);
	public void setState(int state);
	public void setOffenses(int num);
	public void setLastOffense(long last);
	public void setTravelAttemptTime(long time);
	public void setWarnMsg(String msg);
}
