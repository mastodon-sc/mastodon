package org.mastodon.feature.update;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

/**
 * The content of these modifications can be obtained via the
 * {@link #vertices(UpdateLocality)}, {@link #edges(UpdateLocality)} methods.
 * For the first two methods, a flag specifies whether they return the objects
 * that were modified or added themselves ({@link UpdateLocality#SELF}) or the
 * direct neighbors of these objects ({@link UpdateLocality#NEIGHBOR}).
 * 
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class GraphUpdate< V extends Vertex< E >, E extends Edge< V > >
{

	public enum UpdateLocality
	{
		/**
		 * Flags object that were modified or removed directly.
		 */
		SELF,
		/**
		 * Flags direct neighbors of modified objects. E.g. the edge of a vertex
		 * that has been moved, the source vertex of an edge that has been
		 * deleted.
		 */
		NEIGHBOR;
	}

	private final RefSet< V > modifiedVerticesSelf;

	private final RefSet< E > modifiedEdgesSelf;

	private final RefSet< V > modifiedVerticesNeighbor;

	private final RefSet< E > modifiedEdgesNeighbor;

	public GraphUpdate( final ReadOnlyGraph< V, E > graph )
	{
		this.modifiedVerticesSelf = RefCollections.createRefSet( graph.vertices() );
		this.modifiedEdgesSelf = RefCollections.createRefSet( graph.edges() );
		this.modifiedVerticesNeighbor = RefCollections.createRefSet( graph.vertices() );
		this.modifiedEdgesNeighbor = RefCollections.createRefSet( graph.edges() );
	}

	void add( final V vertex )
	{
		modifiedVerticesSelf.add( vertex );
		modifiedVerticesNeighbor.remove( vertex );
	}

	void remove( final V vertex )
	{
		modifiedVerticesSelf.remove( vertex );
		modifiedVerticesNeighbor.remove( vertex );
	}

	void add( final E edge )
	{
		modifiedEdgesSelf.add( edge );
		modifiedEdgesNeighbor.remove( edge );
	}

	void remove( final E edge )
	{
		modifiedEdgesSelf.remove( edge );
		modifiedEdgesNeighbor.remove( edge );
	}

	void addAsNeighbor( final E edge )
	{
		modifiedEdgesNeighbor.add( edge );
	}

	void addAsNeighbor( final V vertex )
	{
		modifiedVerticesNeighbor.add( vertex );
	}

	void concatenate( final GraphUpdate< V, E > other )
	{
		modifiedVerticesSelf.addAll( other.modifiedVerticesSelf );
		modifiedVerticesNeighbor.addAll( other.modifiedVerticesNeighbor );
		modifiedVerticesNeighbor.removeAll( modifiedVerticesSelf );

		modifiedEdgesSelf.addAll( other.modifiedEdgesSelf );
		modifiedEdgesNeighbor.addAll( other.modifiedEdgesNeighbor );
		modifiedEdgesNeighbor.removeAll( modifiedEdgesSelf );
	}

	public RefSet< V > vertices( final UpdateLocality locality )
	{
		switch ( locality )
		{
		case NEIGHBOR:
			return modifiedVerticesNeighbor;
		case SELF:
			return modifiedVerticesSelf;
		default:
			throw new IllegalArgumentException( "Unknown locality: " + locality );
		}
	}

	public RefSet< E > edges( final UpdateLocality locality )
	{
		switch ( locality )
		{
		case NEIGHBOR:
			return modifiedEdgesNeighbor;
		case SELF:
			return modifiedEdgesSelf;
		default:
			throw new IllegalArgumentException( "Unknown locality: " + locality );
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() + ":" );
		for ( final UpdateLocality l : UpdateLocality.values() )
		{
			str.append( String.format( "\n - vertices %8s:" + vertices( l ), l ) );
			str.append( String.format( "\n - edges    %8s:" + edges( l ), l ) );
		}
		return str.toString();
	}
}
