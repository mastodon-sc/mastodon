package org.mastodon.model.branch;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
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

public class BranchGraphTagSetAdapter< 
	V extends Vertex< E >, 
	E extends Edge< V >, 
	BV extends Vertex< BE >,
	BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements TagSetModel< BV, BE >
{

	private final TagSetModel< V, E > tagsetModel;

	private final ObjTags< V > vertexTags;

	private final ObjTags< E > edgeTags;

	public BranchGraphTagSetAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final TagSetModel< V, E > tagsetModel )
	{
		super( branchGraph, graph, idmap );
		this.tagsetModel = tagsetModel;
		this.vertexTags = tagsetModel.getVertexTags();
		this.edgeTags = tagsetModel.getEdgeTags();
	}

	@Override
	public ObjTags< BV > getVertexTags()
	{
		// 1 to many correspondence.
		return new MyVertexTags();
	}

	@Override
	public ObjTags< BE > getEdgeTags()
	{
		// 1 to 1 correspondence.
		final RefBimap< E, BE > map = new BranchGraphEdgeBimap<>( branchGraph, graph );
		return new ObjTagsAdapter<>( tagsetModel.getEdgeTags(), map );
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

	private void set( final BV branchVertex, final Tag tag )
	{
		Iterator<V> vIter = branchGraph.vertexBranchIterator( branchVertex );
		Iterator<E> eIter = branchGraph.edgeBranchIterator( branchVertex );
		try
		{
			while ( vIter.hasNext() )
			{
				V v = vIter.next();
				vertexTags.set( v, tag );
			}

			while ( eIter.hasNext() )
			{
				E e = eIter.next();
				edgeTags.set( e, tag );
			}
		}
		finally
		{
			branchGraph.releaseIterator( vIter );
			branchGraph.releaseIterator( eIter );
		}
	}

	private class MyVertexTags implements ObjTags< BV >
	{

		@Override
		public ObjTagMap< BV, Tag > tags( final TagSet tagSet )
		{
			// Wrap to ensure many to 1 correspondence
			final ObjTagMap< V, Tag > vertexTagMap = vertexTags.tags( tagSet );
			final ObjTagMap< E, Tag > edgeTagMap = edgeTags.tags( tagSet );
			return new MyObjTagMap( vertexTagMap, edgeTagMap );
		}

		@Override
		public void set( final BV vertex, final Tag tag )
		{
			BranchGraphTagSetAdapter.this.set( vertex, tag );
		}

		@Override
		public Collection< BV > getTaggedWith( final Tag tag )
		{
			throw new UnsupportedOperationException( "getTaggedWith() is not supported for branch graphs." );
		}
	}

	private class MyObjTagMap implements ObjTagMap< BV, Tag >
	{

		private final ObjTagMap< E, Tag > edgeTagMap;

		private final ObjTagMap< V, Tag > vertexTagMap;

		public MyObjTagMap( final ObjTagMap< V, Tag > vertexTagMap, final ObjTagMap< E, Tag > edgeTagMap )
		{
			this.vertexTagMap = vertexTagMap;
			this.edgeTagMap = edgeTagMap;
		}

		@Override
		public void set( final BV edge, final Tag tag )
		{
			BranchGraphTagSetAdapter.this.set( edge, tag );
		}

		@Override
		public void remove( final BV branchVertex )
		{
			Iterator<V> vIter = branchGraph.vertexBranchIterator( branchVertex );
			Iterator<E> eIter = branchGraph.edgeBranchIterator( branchVertex );
			try
			{
				while ( vIter.hasNext() )
				{
					V v = vIter.next();
					vertexTagMap.remove( v );
				}

				while ( eIter.hasNext() )
				{
					E e = eIter.next();
					edgeTagMap.remove( e );
				}
			}
			finally
			{
				branchGraph.releaseIterator( vIter );
				branchGraph.releaseIterator( eIter );
			}
		}

		@Override
		public Tag get( final BV branchVertex )
		{
			final V vRef = graph.vertexRef();
			try
			{
				// NB: Return a tag only if all vertices and edges have the same
				// tag.
				V first = branchGraph.getFirstLinkedVertex( branchVertex, vRef );
				Tag tag = vertexTagMap.get( first );
				if(tag == null)
					return null;

				Iterator<V> vIter = branchGraph.vertexBranchIterator( branchVertex );
				Iterator<E> eIter = branchGraph.edgeBranchIterator( branchVertex );
				try
				{
					while( vIter.hasNext() ) {
						V v = vIter.next();
						if( ! tag.equals( vertexTagMap.get(v) ) )
							return null;
					}

					while( eIter.hasNext() ) {
						E e = eIter.next();
						if( ! tag.equals( edgeTagMap.get( e ) ) )
							return null;
					}
				}
				finally
				{
					branchGraph.releaseIterator( vIter );
					branchGraph.releaseIterator( eIter );
				}

				return tag;
			}
			finally
			{
				graph.releaseRef( vRef );
			}
		}

		@Override
		public Collection< BV > getTaggedWith( final Tag tag )
		{
			throw new UnsupportedOperationException( "getTaggedWith() is not supported for branch graphs." );
		}
	}
}
