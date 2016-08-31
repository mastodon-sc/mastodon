package org.mastodon.revised.context;

public interface ContextListener< V >
{
	public void contextChanged( Context< V > context );
}

