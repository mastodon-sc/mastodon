package org.mastodon.model.branch;

import java.util.Collection;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.ObjTagsAdapter;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.scijava.listeners.Listeners;

public class BranchGraphTagSetAdapter< V extends Vertex< E >, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		implements TagSetModel< BV, BE >
{

	private final BranchGraph< BV, BE, V, E > branchGraph;

	private final ReadOnlyGraph< V, E > graph;

	private final TagSetModel< V, E > tagsetModel;

	private final ObjTags< V > vertexTags;

	private final ObjTags< E > edgeTags;

	public BranchGraphTagSetAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final TagSetModel< V, E > tagsetModel )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
		this.tagsetModel = tagsetModel;
		this.vertexTags = tagsetModel.getVertexTags();
		this.edgeTags = tagsetModel.getEdgeTags();
	}

	@Override
	public ObjTags< BV > getVertexTags()
	{
		// 1 to 1 correspondence.
		final RefBimap< V, BV > map = new BranchGraphVertexBimap<>( branchGraph, graph );
		return new ObjTagsAdapter<>( tagsetModel.getVertexTags(), map );
	}

	@Override
	public ObjTags< BE > getEdgeTags()
	{
		// 1 to many correspondence.
		return new MyEdgeTags();
	}

	@Override
	public TagSetStructure getTagSetStructure()
	{
		return tagsetModel.getTagSetStructure();
	}

	@Override
	public void setTagSetStructure( final TagSetStructure tss )
	{
		tagsetModel.setTagSetStructure( tss );
	}

	@Override
	public Listeners< TagSetModelListener > listeners()
	{
		return tagsetModel.listeners();
	}

	@Override
	public void pauseListeners()
	{
		tagsetModel.pauseListeners();
	}

	@Override
	public void resumeListeners()
	{
		tagsetModel.resumeListeners();
	}

	@Override
	public void clear()
	{
		tagsetModel.clear();
	}

	private void set( final BE edge, final Tag tag )
	{
		final E eRef = graph.edgeRef();
		final V vRef = graph.vertexRef();
		final BE beRef = branchGraph.edgeRef();

		E e = branchGraph.getLinkedEdge( edge, eRef );
		edgeTags.set( e, tag );

		V target = e.getTarget( vRef );
		do
		{
			vertexTags.set( target, tag );
			/*
			 * The target vertex is still linked to the branch edge, so this
			 * means that it is still in the middle of the branch. This in turn
			 * means that it has only one outgoing edge.
			 */
			if ( target.outgoingEdges().isEmpty() )
				break;
			e = target.outgoingEdges().get( 0, eRef );
			edgeTags.set( e, tag );
			target = e.getTarget( vRef );
		}
		while ( edge.equals( branchGraph.getBranchEdge( target, beRef ) ) );

		branchGraph.releaseRef( beRef );
		graph.releaseRef( eRef );
		graph.releaseRef( vRef );
	}

	private class MyEdgeTags implements ObjTags< BE >
	{

		@Override
		public ObjTagMap< BE, Tag > tags( final TagSet tagSet )
		{
			// Wrap to ensure many to 1 correspondence
			final ObjTagMap< V, Tag > vertexTasMap = vertexTags.tags( tagSet );
			final ObjTagMap< E, Tag > edgeTagMap = edgeTags.tags( tagSet );
			return new MyObjTagMap( vertexTasMap, edgeTagMap );
		}

		@Override
		public void set( final BE edge, final Tag tag )
		{
			BranchGraphTagSetAdapter.this.set( edge, tag );
		}

		@Override
		public Collection< BE > getTaggedWith( final Tag tag )
		{
			throw new UnsupportedOperationException( "getTaggedWith() is not supported for branch graphs." );
		}
	}

	private class MyObjTagMap implements ObjTagMap< BE, Tag >
	{

		private final ObjTagMap< E, Tag > edgeTagMap;

		private final ObjTagMap< V, Tag > vertexTagMap;

		public MyObjTagMap( final ObjTagMap< V, Tag > vertexTagMap, final ObjTagMap< E, Tag > edgeTagMap )
		{
			this.vertexTagMap = vertexTagMap;
			this.edgeTagMap = edgeTagMap;
		}

		@Override
		public void set( final BE edge, final Tag tag )
		{
			BranchGraphTagSetAdapter.this.set( edge, tag );
		}

		@Override
		public void remove( final BE edge )
		{
			/*
			 * We need to untag all core vertices and edges mapped to this
			 * branch edge.
			 */
			final E eRef = graph.edgeRef();
			final V vRef = graph.vertexRef();
			final BE beRef = branchGraph.edgeRef();

			E e = branchGraph.getLinkedEdge( edge, eRef );
			edgeTagMap.remove( e );

			V target = e.getTarget( vRef );
			do
			{
				vertexTagMap.remove( target );
				if ( target.outgoingEdges().isEmpty() )
					break;
				e = target.outgoingEdges().get( 0, eRef );
				edgeTagMap.remove( e );
				target = e.getTarget( vRef );
			}
			while ( edge.equals( branchGraph.getBranchEdge( target, beRef ) ) );

			branchGraph.releaseRef( beRef );
			graph.releaseRef( eRef );
			graph.releaseRef( vRef );
		}

		@Override
		public Tag get( final BE edge )
		{
			/*
			 * We return a tag only if all core vertices and edges mapped to
			 * this branch edge are tagged with the same tag.
			 */
			final E eRef = graph.edgeRef();
			final V vRef = graph.vertexRef();
			final BE beRef = branchGraph.edgeRef();
			try
			{
				E e = branchGraph.getLinkedEdge( edge, eRef );
				final Tag tag = edgeTagMap.get( e );
				if ( tag == null )
					return null;

				V target = e.getTarget( vRef );
				do
				{
					if ( !tag.equals( vertexTagMap.get( target ) ) )
						return null;
					if ( target.outgoingEdges().isEmpty() )
						break;
					e = target.outgoingEdges().get( 0, eRef );
					if ( !tag.equals( edgeTagMap.get( e ) ) )
						return null;
					target = e.getTarget( vRef );
				}
				while ( edge.equals( branchGraph.getBranchEdge( target, beRef ) ) );
				return tag;
			}
			finally
			{
				branchGraph.releaseRef( beRef );
				graph.releaseRef( eRef );
				graph.releaseRef( vRef );
			}
		}

		@Override
		public Collection< BE > getTaggedWith( final Tag tag )
		{
			throw new UnsupportedOperationException( "getTaggedWith() is not supported for branch graphs." );
		}
	}
}
