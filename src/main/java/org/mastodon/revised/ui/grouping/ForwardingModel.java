package org.mastodon.revised.ui.grouping;

public interface ForwardingModel< T >
{
	public void linkTo( final T model, final boolean copyCurrentStateToNewModel );

	public T asT();
}
