package net.trackmate.graph.listenable;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

public interface ListenableGraph< V extends Vertex< E >, E extends Edge< V > > extends Graph< V, E >
{
	public boolean addGraphListener( GraphListener< V, E > listener );

	public boolean removeGraphListener( GraphListener< V, E > listener );

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
