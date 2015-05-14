package net.trackmate.trackscheme;

import java.util.Collection;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefSet;

public class SelectionModel< V extends Vertex< E >, E extends Edge< V > >
{
	private final RefSet< V > selectedVertices;

	private final RefSet< E > selectedEdges;

	public SelectionModel( Graph< V, E > graph )
	{
		this.selectedVertices = CollectionUtils.createVertexSet( graph );
		this.selectedEdges = CollectionUtils.createEdgeSet( graph );
	}

	public RefSet< E > getSelectedEdges()
	{
		return selectedEdges;
	}

	public RefSet< V > getSelectedVertices()
	{
		return selectedVertices;
	}

	public void add( V vertex )
	{
		selectedVertices.add( vertex );
	}

	public void remove( V vertex )
	{
		selectedVertices.remove( vertex );
	}

	public void toggle( V vertex )
	{
		if ( !selectedVertices.remove( vertex ) )
		{
			selectedVertices.add( vertex );
		}
	}

	public void add( E edge )
	{
		selectedEdges.add( edge );
	}

	public void toggle( E edge )
	{
		if ( !selectedEdges.remove( edge ) )
		{
			selectedEdges.add( edge );
		}
	}

	public void addAllEdges( Collection< E > edges )
	{
		selectedEdges.addAll( edges );
	}

	public void addAllVertices( Collection< V > vertices )
	{
		selectedVertices.addAll( vertices );
	}

	public void remove( E edge )
	{
		selectedEdges.remove( edge );
	}

	public void clearSelection()
	{
		selectedEdges.clear();
		selectedVertices.clear();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( super.toString() );
		sb.append( "\nVertices: " + selectedVertices );
		sb.append( "\nEdges:    " + selectedEdges );
		return sb.toString();
	}

	/*
	 * STATIC ACCESSOR
	 */

	public static final < V extends Vertex< E >, E extends Edge< V > > SelectionModel< V, E > create( Graph< V, E > graph )
	{
		return new SelectionModel< V, E >( graph );
	}
}
