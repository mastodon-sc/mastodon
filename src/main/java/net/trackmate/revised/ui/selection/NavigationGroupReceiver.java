package net.trackmate.revised.ui.selection;

/**
 * Interface for classes that declare belonging to a certain receiving
 * navigation group. If a class belongs to a receiving navigation group, it can
 * respond to navigation events fired from classes that declare belonging to
 * emitting navigation groups with the same id.
 *
 * @author Jean-Yves Tinevez
 *
 */
public interface NavigationGroupReceiver
{
	/**
	 * Returns <code>true</code> when this class belongs to the specified group.
	 * The class may then <b>receive</b> navigation events from the same
	 * emitting group.
	 *
	 * @param group
	 *            the group.
	 * @return <code>true</code> if this class belongs to the group.
	 */
	public boolean isInGroup( int group );
}
