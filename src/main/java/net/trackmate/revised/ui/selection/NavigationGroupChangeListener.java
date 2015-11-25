package net.trackmate.revised.ui.selection;

/**
 * Interface for classes that are notified when a change is made in an
 * NavigationGroupHandler.
 */
public interface NavigationGroupChangeListener
{
	public void navigationGroupChanged( int groupId, boolean activated );
}
