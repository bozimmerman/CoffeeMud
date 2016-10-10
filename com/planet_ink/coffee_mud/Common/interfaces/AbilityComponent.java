package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.List;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.core.CMath;

/**
 * Descriptor class for ability components.
 * These are managed by by the Ability library.
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents#addAbilityComponent(String, java.util.Map)
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents#componentCheck(com.planet_ink.coffee_mud.MOBS.interfaces.MOB, List, boolean)
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents#getAbilityComponentMap()
 * @author Bo Zimmerman
 */
public interface AbilityComponent extends CMCommon
{
	/**
	 * Returns an enum describing how this component "connects" with the
	 * following component logically.  As in, is it required
	 * WITH the following component, or instead of?
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setConnector(CompConnector)
	 * @return a connector enum
	 */
	public CompConnector getConnector();

	/**
	 * Sets an enum describing how this component "connects" with the
	 * following component logically.  As in, is it required
	 * WITH the following component, or instead of?
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getConnector()
	 * @param connector a connector enum
	 */
	public void setConnector(CompConnector connector);

	/**
	 * Returns an enum value describing where an item must be to
	 * be considered a valid component.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setLocation(CompLocation)
	 * @return where an item must be to be a component
	 */
	public CompLocation getLocation();

	/**
	 * Sets an enum value describing where an item must be to
	 * be considered a valid component.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getLocation()
	 * @param location where an item must be to be a component
	 */
	public void setLocation(CompLocation location);

	/**
	 * Gets whether or not this component is consumed upon use
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setConsumed(boolean)
	 * @return true if consumed, false otherwise
	 */
	public boolean isConsumed();

	/**
	 * Sets whether or not this component is consumed upon use
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#isConsumed()
	 * @param isConsumed true if consumed, false otherwise
	 */
	public void setConsumed(boolean isConsumed);

	/**
	 * Returns the number of items matching this component which must be present.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setAmount(int)
	 * @return the number of items matching this component which must be present.
	 */
	public int getAmount();

	/**
	 * Sets the number of items matching this component which must be present.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getAmount()
	 * @param amount the number of items matching this component which must be present
	 */
	public void setAmount(int amount);

	/**
	 * Returns the item filter type for determining whether an item is
	 * a component.  This type can designate a resource, material, or
	 * an item name string.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setType(CompType, Object)
	 * @return a CompType enum
	 */
	public CompType getType();

	/**
	 * Sets the item filter type for determining whether an item is
	 * a component.  This type can designate a resource, material, or
	 * an item name string.  Also sent is either the resource mask,
	 * material mask, a string for a name filter, or a string with a
	 * long number in it.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getType()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getLongType()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getStringType()
	 * @param type the CompType enum
	 * @param typeObj either a Integer object or a String
	 */
	public void setType(CompType type, Object typeObj);

	/**
	 * For resource and material type item component filters, this will
	 * return the type comparison object as a long value.  This is typically
	 * a resource or material mask value.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setType(CompType, Object)
	 * @return a resource or material mask value
	 */
	public long getLongType();

	/**
	 * For resource and material type item component filters, this will
	 * return the type comparison object as a String value.  This is typically
	 * a item name filter.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setType(CompType, Object)
	 * @return a item name filter
	 */
	public String getStringType();

	/**
	 * Returns the compiled zapper mask to determine whether a given agent
	 * qualifies this item as a component.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getMaskStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setMask(String)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the compiled zapper mask
	 */
	public MaskingLibrary.CompiledZMask getCompiledMask();

	/**
	 * Returns the raw zapper mask to determine whether a given agent
	 * qualifies this item as a component.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getCompiledMask()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#setMask(String)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return the raw zapper mask
	 */
	public String getMaskStr();

	/**
	 * Sets the raw zapper mask to determine whether a given agent
	 * qualifies this item as a component.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getCompiledMask()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent#getMaskStr()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @param maskStr the raw zapper mask
	 */
	public void setMask(String maskStr);

	/**
	 * Connector descriptors for connecting component descriptions together
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompConnector { AND, OR }

	/**
	 * Where worn locations for determining where a component must be
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompLocation { INVENTORY, HELD, WORN, NEARBY, ONGROUND }

	/**
	 * An component type item filter for determining
	 * how to interpret the kind of item to compare
	 * @author Bo Zimmerman
	 *
	 */
	public enum CompType { RESOURCE, MATERIAL, STRING }
}
