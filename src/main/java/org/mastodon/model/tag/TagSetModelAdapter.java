package org.mastodon.model.tag;

import org.mastodon.adapter.ForwardedListeners;
import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
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

	public TagSetModelAdapter(
			final TagSetModel< V, E > tagSetModel,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.tagSetModel = tagSetModel;
		vertexTags = new ObjTagsAdapter<>( tagSetModel.getVertexTags(), vertexMap );
		edgeTags = new ObjTagsAdapter<>( tagSetModel.getEdgeTags(), edgeMap );
		this.listeners = new ForwardedListeners.List<>( tagSetModel.listeners() );
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
