package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.Set;

import com.planet_ink.coffee_mud.core.collections.Pair;

/**
 * The Followable interface defines methods to both the follower, and
 * the thing being followed, since it is assumed that following is a
 * transitive property.
 * 
 * @author Bo Zimmerman
 *
 * @param <T> an extension of Followable, showing that Followables follow/are followed by Followables
 */
public interface Followable<T extends Followable<T>> extends Physical
{
	/**
	 * Adds/Alters a follower to this Followable, of the appropriate type, while
	 * also setting the order/rank they hold in the line.  Orders don't have
	 * to be unique.
	 * This does NOT call setFollowing on the follower, so that would also need
	 * to be done, and, in fact, can be done instead if you like.
	 * @see Followable#setFollowing(Followable)
	 * @param follower the new follower to follow
	 * @param order the relative position of the new follower in the formation
	 */
	public void addFollower(T follower, int order);
	
	/**
	 * Removes the given follower from this followable.
	 * @param follower the follower to remove
	 */
	public void delFollower(T follower);
	
	/**
	 * Returns the total number of Direct followers
	 * @see Followable#totalFollowers()
	 * @return the total number of Direct followers
	 */
	public int numFollowers();
	
	/**
	 * Returns the relative order/rank of the specific follower
	 * in the formation.
	 * @param thisOne the specific followable
	 * @return the relative order/rank of the specific follower
	 */
	public int fetchFollowerOrder(T thisOne);
	
	/**
	 * Returns whether the given followable is following this
	 * followable.
	 * @param thisOne the followable follower to look for.
	 * @return true if thisOne is following, false otherwise
	 */
	public boolean isFollowedBy(T thisOne);
	
	/**
	 * Returns an enumeration of the followers of this Followable,
	 * along with their relative rank/position in the formation.
	 * @return  an enumeration of the followers of this Followable
	 */
	public Enumeration<Pair<T,Short>> followers();
	
	/**
	 * Returns the followable follower at the give index in this collection
	 * @param index the index (0-based) of the followable follower
	 * @return the followable follower at the give index in this collection
	 */
	public T fetchFollower(int index);
	
	/**
	 * Returns the followable follower that best matches the given search
	 * string name.
	 * @param named the search string name
	 * @return the followable follower that best matches
	 */
	public T fetchFollower(String named);
	
	/**
	 * Returns the total number of followable followers following this followable
	 * by counting not only the followable followers of this followable, but also
	 * the followable followers of the followable followers of this followable,
	 * recursively.
	 * @see Followable#numFollowers()
	 * @return the total number of followers
	 */
	public int totalFollowers();
	
	/**
	 * Returns the followable that this followable follower is following, or null.
	 * @return the followable that this followable follower is following, or null.
	 */
	public T amFollowing();
	
	/**
	 * If this followable follower is following a followable that is also following
	 * a followable, then this will recursively determine the First followable and
	 * return it.
	 * @return the first followable
	 */
	public T amUltimatelyFollowing();
	
	/**
	 * Alters who this followable follower is following.  This also calls addFollower
	 * above.
	 * @see Followable#addFollower(Followable, int)
	 * @param mob the follower following
	 */
	public void setFollowing(T mob);
	
	/**
	 * Fills the given set with the Complete set of all followable followers by
	 * counting not only this followable, but all the recursive followable
	 * followers of this followable, and all the followables followed by this
	 * followable and all their followable followers.
	 * @param list requires list to populate with followables  
	 * @return the same list, filled
	 */
	public Set<T> getGroupMembers(Set<T> list);
	
	/**
	 * Fills the given set with the Complete set of all followable followers by
	 * counting not only this followable, but all the recursive followable
	 * followers of this followable, and all the followables followed by this
	 * followable and all their followable followers.  It also includes all their
	 * rideables, and their rideables riders, and those riders rideables.
	 * @param list requires list to populate with followables  and rideables
	 * @return the same list, filled
	 */
	public Set<? extends Rider> getGroupMembersAndRideables(Set<? extends Rider> list);
}
