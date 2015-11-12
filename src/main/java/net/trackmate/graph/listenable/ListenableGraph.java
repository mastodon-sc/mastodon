package net.trackmate.graph.listenable;

import net.trackmate.graph.Edge;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

// TODO: this should rather extend ReadOnlyGraph!
public interface ListenableGraph< V extends Vertex< E >, E extends Edge< V > > extends ReadOnlyGraph< V, E >
{
	public boolean addGraphListener( GraphListener< V, E > listener );

	public boolean removeGraphListener( GraphListener< V, E > listener );

	public boolean addGraphChangeListener( GraphChangeListener listener );

	public boolean removeGraphChangeListener( GraphChangeListener listener );

//	public Set< GraphListener< V, E > > getGraphListeners();
//
//	public void beginUpdate();
//
//	public void endUpdate();
//
//	public void pauseUpdate();
//
//	public void resumeUpdate();
}
