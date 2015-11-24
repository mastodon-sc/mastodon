package net.trackmate.revised.ui.selection;

import gnu.trove.set.TIntSet;

/**
 * Interface for classes that declare belonging to certain emitting navigation
 * groups. These classes fire navigation events that will be received by
 * {@link NavigationGroupReceiver} belonging to one of the group that this
 * emitter belongs to.
 *
 * @author Jean-Yves Tinevez
 *
 */
public interface NavigationGroupEmitter
{
	/**
	 * Returns the set of group ids this emitter class belongs to.
	 * {@link NavigationGroupReceiver}s that declare belonging to the groups
	 * returned by this method will receive navigation events.
	 *
	 * @return a set of group ids. The set can be empty, be cannot be
	 *         <code>null</code>.
	 */
	public TIntSet getGroups();
}
