package net.trackmate.revised.context;

public interface ContextProvider< V >
{
	public String getContextProviderName();

	public boolean addContextListener( ContextListener< V > listener );

	public boolean removeContextListener( ContextListener< V > listener );
}
