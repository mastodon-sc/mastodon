package org.mastodon.revised.ui.grouping;

/**
 * Listens to a {@link GroupHandle} and is notified when it changes group
 * membership. (See {@link GroupHandle#groupChangeListeners()}.)
 *
 * @author Tobias Pietzsch
 */
public interface GroupChangeListener
{
	public void groupChanged();
}
