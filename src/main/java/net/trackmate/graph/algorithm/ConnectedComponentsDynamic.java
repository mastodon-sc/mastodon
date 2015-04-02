package net.trackmate.graph.algorithm;

import java.util.Set;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.listenable.GraphChangeEvent;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;

public class ConnectedComponentsDynamic< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{
	private final Set< RefSet< V >> cc;

	public ConnectedComponentsDynamic( final Graph< V, E > graph )
	{
		new ListenableGraph< V, E >( graph ).addGraphListener( this );
		cc = new ConnectedComponents< V, E >( graph ).get();
	}

	@Override
	public void graphChanged( final GraphChangeEvent< V, E > event )
	{

	}

}
