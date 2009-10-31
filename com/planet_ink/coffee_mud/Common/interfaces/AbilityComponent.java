package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMath;

/**
 * Descriptor class for ability components.
 * These are managed by by the Ability library.
 * @author Bo Zimmerman
 *
 */
public interface AbilityComponent extends CMCommon
{
	/**
	 * 
	 * @return
	 */
	public CompConnector getConnector();
	
	/**
	 * 
	 * @param connector
	 */
	public void setConnector(CompConnector connector);
	
	/**
	 * 
	 * @return
	 */
	public CompLocation getLocation();
	
	/**
	 * 
	 * @param location
	 */
	public void setLocation(CompLocation location);
	
	/**
	 * 
	 * @return
	 */
	public boolean isConsumed();
	
	/**
	 * 
	 * @param isConsumed
	 */
	public void setConsumed(boolean isConsumed);
	
	/**
	 * 
	 * @return
	 */
	public int getAmount();
	
	/**
	 * 
	 * @param amount
	 */
	public void setAmount(int amount);
	
	/**
	 * 
	 * @return
	 */
	public CompType getType();
	
	/**
	 * 
	 * @param type
	 * @param typeObj
	 */
	public void setType(CompType type, Object typeObj);
	
	/**
	 * 
	 * @return
	 */
	public long getLongType();
	
	/**
	 * 
	 * @return
	 */
	public String getStringType();
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vector getCompiledMask();

	/**
	 * 
	 * @return
	 */
	public String getMaskStr();

	/**
	 * 
	 * @param maskStr
	 */
	public void setMask(String maskStr); 
	
	/**
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompConnector { AND, OR };
	
	/**
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompLocation { INVENTORY, HELD, WORN };
	
	/**
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompType { RESOURCE, MATERIAL, STRING };
}
