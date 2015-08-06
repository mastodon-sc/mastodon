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

	public SelectionModel( final Graph< V, E > graph )
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

	public boolean add( final V vertex )
	{
		return selectedVertices.add( vertex );
	}

	public boolean remove( final V vertex )
	{
		return selectedVertices.remove( vertex );
	}

	public void toggle( final V vertex )
	{
		if ( !selectedVertices.remove( vertex ) )
		{
			selectedVertices.add( vertex );
		}
	}

	public boolean add( final E edge )
	{
		return selectedEdges.add( edge );
	}

	public void toggle( final E edge )
	{
		if ( !selectedEdges.remove( edge ) )
		{
			selectedEdges.add( edge );
		}
	}

	public boolean addAllEdges( final Collection< E > edges )
	{
		return selectedEdges.addAll( edges );
	}

	public boolean addAllVertices( final Collection< V > vertices )
	{
		return selectedVertices.addAll( vertices );
	}

	public boolean remove( final E edge )
	{
		return selectedEdges.remove( edge );
	}

	public boolean clearSelection()
	{
		if ( selectedEdges.isEmpty() && selectedVertices.isEmpty() ) { return false; }
		selectedEdges.clear();
		selectedVertices.clear();
		return true;
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

	public static final < V extends Vertex< E >, E extends Edge< V > > SelectionModel< V, E > create( final Graph< V, E > graph )
	{
		return new SelectionModel< V, E >( graph );
	}
}
