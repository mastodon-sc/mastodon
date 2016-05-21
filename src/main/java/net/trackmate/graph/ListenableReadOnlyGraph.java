package net.trackmate.graph;

public interface ListenableReadOnlyGraph< V extends Vertex< E >, E extends Edge< V > > extends ReadOnlyGraph< V, E >
{
	public boolean addGraphListener( GraphListener< V, E > listener );

	public boolean removeGraphListener( GraphListener< V, E > listener );

	public boolean addGraphChangeListener( GraphChangeListener listener );

	public boolean removeGraphChangeListener( GraphChangeListener listener );
}
