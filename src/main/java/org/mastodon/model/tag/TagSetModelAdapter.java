package org.mastodon.model.tag;

import org.mastodon.adapter.ForwardedListeners;
import org.mastodon.adapter.ListenersAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.properties.PropertyChangeListener;
import org.scijava.listeners.Listeners;

/**
 * Adapts a {@code TagSetModel<V, E>} as a {@code TagSetModel<WV, WE>}. The
 * mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type of this wrapped {@link TagSetModel}.
 * @param <WE>
 *            edge type of this wrapped {@link TagSetModel}.
 *
 * @author Tobias Pietzsch
 */
public class TagSetModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > > implements TagSetModel< WV, WE >
{
	private final TagSetModel< V, E > tagSetModel;

	private final ObjTags< WV > vertexTags;

	private final ObjTags< WE > edgeTags;

	private final ForwardedListeners< TagSetModelListener > listeners;

	private final ForwardedListeners< PropertyChangeListener< WV > > vertexTagChangeListeners;

	private final ForwardedListeners< PropertyChangeListener< WE > > edgeTagChangeListeners;

	public TagSetModelAdapter(
			final TagSetModel< V, E > tagSetModel,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.tagSetModel = tagSetModel;
		vertexTags = new ObjTagsAdapter<>( tagSetModel.getVertexTags(), vertexMap );
		edgeTags = new ObjTagsAdapter<>( tagSetModel.getEdgeTags(), edgeMap );
		listeners = new ForwardedListeners.List<>( tagSetModel.listeners() );
		final Listeners< PropertyChangeListener< WV > > vertexTagChangeListenersAdapter =
				new ListenersAdapter.List<>( tagSetModel.vertexTagChangeListeners(), l -> v -> {
					final WV ref = vertexMap.reusableRightRef();
					l.propertyChanged( vertexMap.getRight( v, ref ) );
					vertexMap.releaseRef( ref );
				} );
		final Listeners< PropertyChangeListener< WE > > edgeTagChangeListenersAdapter =
				new ListenersAdapter.List<>( tagSetModel.edgeTagChangeListeners(), l -> e -> {
					final WE ref = edgeMap.reusableRightRef();
					l.propertyChanged( edgeMap.getRight( e, ref ) );
					edgeMap.releaseRef( ref );
				} );
		vertexTagChangeListeners = new ForwardedListeners.List<>( vertexTagChangeListenersAdapter );
		edgeTagChangeListeners = new ForwardedListeners.List<>( edgeTagChangeListenersAdapter );
	}

	@Override
	public ObjTags< WV > getVertexTags()
	{
		return vertexTags;
	}

	@Override
	public ObjTags< WE > getEdgeTags()
	{
		return edgeTags;
	}

	@Override
	public TagSetStructure getTagSetStructure()
	{
		return tagSetModel.getTagSetStructure();
	}

	@Override
	public void setTagSetStructure( final TagSetStructure tss )
	{
		tagSetModel.setTagSetStructure( tss );
	}

	@Override
	public Listeners< TagSetModelListener > listeners()
	{
		return listeners;
	}

	@Override
	public Listeners< PropertyChangeListener< WV > > vertexTagChangeListeners()
	{
		return vertexTagChangeListeners;
	}

	@Override
	public Listeners< PropertyChangeListener< WE > > edgeTagChangeListeners()
	{
		return edgeTagChangeListeners;
	}

	@Override
	public void pauseListeners()
	{
		tagSetModel.pauseListeners();
	}

	@Override
	public void resumeListeners()
	{
		tagSetModel.resumeListeners();
	}

	@Override
	public void clear()
	{
		tagSetModel.clear();
	}
}
