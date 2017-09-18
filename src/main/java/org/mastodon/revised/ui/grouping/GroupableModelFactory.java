package org.mastodon.revised.ui.grouping;

public interface GroupableModelFactory< T >
{
	public T createBackingModel();

	public ForwardingModel< T > createForwardingModel();
}
