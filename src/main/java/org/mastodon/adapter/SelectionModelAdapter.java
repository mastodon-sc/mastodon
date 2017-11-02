package org.mastodon.adapter;

import java.util.Collection;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;

/**
 * Adapts a {@code SelectionModel<V, E>} as a {@code SelectionModel<WV, WE>}.
 * The mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type this wrapped {@link SelectionModel}.
 * @param <WE>
 *            edge type this wrapped {@link SelectionModel}.
 */
public class SelectionModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements SelectionModel< WV, WE >
{
	private final SelectionModel< V, E > selection;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	private final ForwardedListeners< SelectionListener > listeners;

	public SelectionModelAdapter(
			final SelectionModel< V, E > selection,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.selection = selection;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
		this.listeners = new ForwardedListeners.SynchronizedList<>( selection.listeners() );
	}

	@Override
	public boolean isSelected( final WV vertex )
	{
		return selection.isSelected( vertexMap.getLeft( vertex ) );
	}

	@Override
	public boolean isSelected( final WE edge )
	{
		return selection.isSelected( edgeMap.getLeft( edge ) );
	}

	@Override
	public void setSelected( final WV vertex, final boolean selected )
	{
		selection.setSelected( vertexMap.getLeft( vertex ), selected );
	}

	@Override
	public void setSelected( final WE edge, final boolean selected )
	{
		selection.setSelected( edgeMap.getLeft( edge ), selected );
	}

	@Override
	public void toggle( final WV vertex )
	{
		selection.toggle( vertexMap.getLeft( vertex ) );
	}

	@Override
	public void toggle( final WE edge )
	{
		selection.toggle( edgeMap.getLeft( edge ) );
	}

	@Override
	public boolean setEdgesSelected( final Collection< WE > edges, final boolean selected )
	{
		return selection.setEdgesSelected( new CollectionAdapterReverse<>( edges, edgeMap ), selected );
	}

	@Override
	public boolean setVerticesSelected( final Collection< WV > vertices, final boolean selected )
	{
		return selection.setVerticesSelected( new CollectionAdapterReverse<>( vertices, vertexMap ), selected );
	}

	@Override
	public boolean clearSelection()
	{
		return selection.clearSelection();
	}

	@Override
	public RefSet< WE > getSelectedEdges()
	{
		return new RefSetAdapter<>( selection.getSelectedEdges(), edgeMap );
	}

	@Override
	public RefSet< WV > getSelectedVertices()
	{
		return new RefSetAdapter<>( selection.getSelectedVertices(), vertexMap );
	}

	@Override
	public boolean isEmpty()
	{
		return selection.isEmpty();
	}

	@Override
	public ForwardedListeners< SelectionListener > listeners()
	{
		return listeners;
	}

	@Override
	public void resumeListeners()
	{
		selection.resumeListeners();
	}

	@Override
	public void pauseListeners()
	{
		selection.pauseListeners();
	}
}
