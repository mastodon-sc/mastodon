package org.mastodon.ui.coloring;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefMaps;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.model.HasLabel;
import org.mastodon.views.trackscheme.util.AlphanumCompare;

public class TrackGraphColorGenerator< V extends Vertex< E > & HasLabel, E extends Edge< V > >
		implements GraphColorGenerator< V, E >, GraphChangeListener
{

	private final RefIntMap< V > cache;

	private final GlasbeyLut lut;

	private final InverseDepthFirstIterator< V, E > iterator;

	private final RefList< V > toUpdate;

	private final ReadOnlyGraph< V, E > graph;

	private boolean pause;

	public TrackGraphColorGenerator( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		this.cache = RefMaps.createRefIntMap( graph.vertices(), 0 );
		this.lut = GlasbeyLut.create();
		this.iterator = new InverseDepthFirstIterator<>( graph );
		this.toUpdate = RefCollections.createRefList( graph.vertices() );
		this.pause = false;
		graphChanged();
	}

	@Override
	public int color( final V v )
	{
		if ( cache.containsKey( v ) )
			return cache.get( v );

		/*
		 * Iterate until we find a spot with a color. The roots have already a
		 * color by then, so at worst we will have to iterate depth-first,
		 * upward, until we meet a root. Then we will return its color, but
		 * before, we will store this color in all the vertices we have
		 * encountered during iteration.
		 */
		iterator.reset( v );
		toUpdate.clear();
		toUpdate.add( v );
		while ( iterator.hasNext() )
		{
			final V n = iterator.next();
			if ( cache.containsKey( n ) )
			{
				final int c = cache.get( n );
				// Sets the color of the vertices we traversed.
				toUpdate.forEach( m -> cache.put( m, c ) );
				return c;
			}
			else
			{
				toUpdate.add( n );
			}
		}
		// Failed to find a root?!
		return 0;
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		return color( source );
	}

	@Override
	public void graphChanged()
	{
		if ( pause )
			return;

		cache.clear();
		lut.reset();
		final RefList< V > sortedRoots = RefCollections.createRefList( graph.vertices() );
		sortedRoots.addAll( RootFinder.getRoots( graph ) );
		sortedRoots.sort( ( v1, v2 ) -> AlphanumCompare.compare( v1.getLabel(), v2.getLabel() ) );
		sortedRoots.forEach( r -> cache.put( r, lut.next() ) );
	}

	/**
	 * If <code>true</code>, the track colors will not be regenerated when the
	 * graph changes. This is intended to save a possibly costly operation when
	 * this color generator is not used.
	 * 
	 * @param pause
	 *            whether to pause regeneration of colors or not.
	 */
	public void pauseListener( final boolean pause )
	{
		this.pause = pause;
	}
}
