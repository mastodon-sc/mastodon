package net.trackmate.revised.trackscheme.context;

public interface ContextProvider< V >
{
	public String getContextProviderName();

	public void addContextListener( ContextListener< V > listener );

	public void removeContextListener( ContextListener< V > listener );
}