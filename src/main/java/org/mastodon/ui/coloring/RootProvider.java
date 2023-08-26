package org.mastodon.ui.coloring;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.scijava.listeners.Listeners;

/**
 * A class that listens to a graph to provide the set if its roots.
 * <p>
 * The root set is updated by listening to graph changes. So to work, an
 * instance of this class <b>must</b> be first registered as a
 * {@link GraphListener} to the graph it will analyze, and then have its
 * {@link #graphRebuilt()} method called once.
 * <p>
 * Classes that use this root provider can register as a listener to it. They
 * will be notified when the set of roots have changed. Not all changes in the
 * graph result in a change in the root set.
 * 
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class RootProvider< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{

	public interface RootsListener
	{
		void rootsChanged();
	}

	private final RefSet< V > roots;

	private final Listeners.List< RootsListener > listeners;

	private final ReadOnlyGraph< V, E > graph;

	public RootProvider( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		this.roots = RefCollections.createRefSet( graph.vertices() );
		this.listeners = new Listeners.List<>();
	}

	public Listeners.List< RootsListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		listeners.list.forEach( l -> l.rootsChanged() );
	}

	@Override
	public void graphRebuilt()
	{
		roots.clear();
		roots.addAll( RootFinder.getRoots( graph ) );
		notifyListeners();
	}

	@Override
	public void vertexAdded( final V v )
	{
		roots.add( v );
		notifyListeners();
	}

	@Override
	public void vertexRemoved( final V v )
	{
		if ( roots.remove( v ) )
			notifyListeners();
	}

	@Override
	public void edgeAdded( final E e )
	{
		final V ref = graph.vertexRef();
		final V target = e.getTarget( ref );
		// Cannot be a root anymore
		if ( roots.remove( target ) )
			notifyListeners();
		graph.releaseRef( ref );
	}

	@Override
	public void edgeRemoved( final E e )
	{
		final V ref = graph.vertexRef();
		final V target = e.getTarget( ref );
		// Is it the last edge -> if yes it will become a root.
		if ( target.incomingEdges().size() == 1 )
		{
			roots.add( target );
			notifyListeners();
		}
		graph.releaseRef( ref );
	}

	public RefSet< V > get()
	{
		return roots;
	}
}
