package net.trackmate.graph.listenable;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.ReadOnlyGraph;
import net.trackmate.graph.zzgraphinterfaces.Vertex;

public interface ListenableGraph< V extends Vertex< E >, E extends Edge< V > > extends ReadOnlyGraph< V, E >
{
	public boolean addGraphListener( GraphListener< V, E > listener );

	public boolean removeGraphListener( GraphListener< V, E > listener );

	public boolean addGraphChangeListener( GraphChangeListener listener );

	public boolean removeGraphChangeListener( GraphChangeListener listener );
}
