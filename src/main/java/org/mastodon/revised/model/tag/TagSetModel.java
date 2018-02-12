package org.mastodon.revised.model.tag;

import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.labels.LabelSet;
import org.mastodon.labels.LabelSets;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.undo.Recorder;
import org.mastodon.undo.UndoableEdit;
import org.mastodon.util.Listeners;

/**
 * Assigns tags to vertices and edges of a graph, according to a {@link TagSetStructure}.
 *
 * @param <V>
 *            the type of the vertices in the graph.
 * @param <E>
 *            the type of the edges in the graph.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class TagSetModel< V extends Vertex< E >, E extends Edge< V > >
{
	private final ReadOnlyGraph< V, E > graph;

	private final TagSetStructure tagSetStructure;

	private final LabelSets< V, Integer > vertexIdLabelSets;

	private final LabelSets< E, Integer > edgeIdLabelSets;

	private final ObjTags< V > vertexTags;

	private final ObjTags< E > edgeTags;

	private final Listeners.List< TagSetModelListener > listeners;

	private Recorder< SetTagSetStructureUndoableEdit > editRecorder;

	public TagSetModel( final ReadOnlyGraph< V, E > graph )
	{
		this( graph, RefCollections.tryGetRefPool( graph.vertices() ), RefCollections.tryGetRefPool( graph.edges() ) );
	}

	public TagSetModel( final ReadOnlyGraph< V, E > graph, final RefPool< V > vertexPool, final RefPool< E > edgePool )
	{
		this.graph = graph;
		this.tagSetStructure = new TagSetStructure();
		vertexIdLabelSets = new LabelSets<>( vertexPool );
		edgeIdLabelSets = new LabelSets<>( edgePool );
		vertexTags = new ObjTags<>( vertexIdLabelSets, tagSetStructure );
		edgeTags = new ObjTags<>( edgeIdLabelSets, tagSetStructure );
		listeners = new Listeners.SynchronizedList<>();
	}

	public TagSetStructure getTagSetStructure()
	{
		return tagSetStructure;
	}

	public void setTagSetStructure( final TagSetStructure tss )
	{
		// find tags that have been removed from TagSetStructure
		final Set< Integer > removedIds = tagSetStructure.getTagSets().stream().flatMap( ts -> ts.getTags().stream() ).map( Tag::id ).collect( Collectors.toSet() );
		final Set< Integer > newIds = tss.getTagSets().stream().flatMap( ts -> ts.getTags().stream() ).map( Tag::id ).collect( Collectors.toSet() );
		removedIds.removeAll( newIds );

		// remove those tags from all vertices ...
		final LabelSet< V, Integer > vref = vertexIdLabelSets.createRef();
		for ( final Integer id : removedIds )
		{
			final RefSet< V > labeledWithId = RefCollections.createRefSet( graph.vertices() );
			labeledWithId.addAll( vertexIdLabelSets.getLabeledWith( id ) );
			labeledWithId.forEach( v -> vertexIdLabelSets.getLabels( v, vref ).remove( id ) );
		}
		vertexIdLabelSets.releaseRef( vref );

		// ... and edges
		final LabelSet< E, Integer > eref = edgeIdLabelSets.createRef();
		for ( final Integer id : removedIds )
		{
			final RefSet< E > labeledWithId = RefCollections.createRefSet( graph.edges() );
			labeledWithId.addAll( edgeIdLabelSets.getLabeledWith( id ) );
			labeledWithId.forEach( e -> edgeIdLabelSets.getLabels( e, eref ).remove( id ) );
		}
		edgeIdLabelSets.releaseRef( eref );

		// TODO: undo-record TagSetStructure change
		if ( editRecorder != null )
		{
			editRecorder.record( new SetTagSetStructureUndoableEdit( this, tagSetStructure, tss ) );
		}

		tagSetStructure.set( tss );
		listeners.list.forEach( TagSetModelListener::tagSetStructureChanged );

		vertexTags.update( tagSetStructure );
		edgeTags.update( tagSetStructure );
	}

	public ObjTags< V > getVertexTags()
	{
		return vertexTags;
	}

	public ObjTags< E > getEdgeTags()
	{
		return edgeTags;
	}

	public void setUndoRecorder( final Recorder< SetTagSetStructureUndoableEdit > editRecorder )
	{
		this.editRecorder = editRecorder;
	}

	public interface TagSetModelListener
	{
		void tagSetStructureChanged();
	}

	public Listeners< TagSetModelListener > listeners()
	{
		return listeners;
	}

	/**
	 * Internals. Can be derived for implementing de/serialisation and undo/redo
	 */
	public static class SerialisationAccess< V extends Vertex< E >, E extends Edge< V > >
	{
		private final TagSetModel< V, E > tagSetModel;

		protected SerialisationAccess( final TagSetModel< V, E > tagSetModel )
		{
			this.tagSetModel = tagSetModel;
		}

		protected LabelSets< V, Integer > getVertexIdLabelSets()
		{
			return tagSetModel.vertexIdLabelSets;
		}

		protected LabelSets< E, Integer > getEdgeIdLabelSets()
		{
			return tagSetModel.edgeIdLabelSets;
		}

		protected void updateObjTags()
		{
			tagSetModel.vertexTags.update( tagSetModel.tagSetStructure );
			tagSetModel.edgeTags.update( tagSetModel.tagSetStructure );
		}
	}

	public static class SetTagSetStructureUndoableEdit implements UndoableEdit
	{
		private final TagSetModel< ?, ? > tagSetModel;

		private final TagSetStructure oldTss;

		private final TagSetStructure newTss;

		SetTagSetStructureUndoableEdit( final TagSetModel<?,?> tagSetModel, final TagSetStructure oldTss,  final TagSetStructure newTss )
		{
			this.tagSetModel = tagSetModel;
			this.oldTss = new TagSetStructure();
			this.oldTss.set( oldTss );
			this.newTss = new TagSetStructure();
			this.newTss.set( newTss );
		}

		@Override
		public void undo()
		{
			tagSetModel.getTagSetStructure().set( oldTss );
			tagSetModel.vertexTags.update( tagSetModel.tagSetStructure );
			tagSetModel.edgeTags.update( tagSetModel.tagSetStructure );
			tagSetModel.listeners.list.forEach( TagSetModelListener::tagSetStructureChanged );
		}

		@Override
		public void redo()
		{
			tagSetModel.getTagSetStructure().set( newTss );
			tagSetModel.vertexTags.update( tagSetModel.tagSetStructure );
			tagSetModel.edgeTags.update( tagSetModel.tagSetStructure );
			tagSetModel.listeners.list.forEach( TagSetModelListener::tagSetStructureChanged );
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		for ( final TagSet tagSet : tagSetStructure.getTagSets() )
		{
			str.append( "\n" + tagSet.getName() + ":" );
			for ( final Tag tag : tagSet.getTags() )
			{
				str.append( "\n  - " + tag.label() + ":" );
				str.append( "\n    V: " );
				for ( final V v : getVertexTags().getTaggedWith( tag ) )
					str.append( v + "," );
				str.append( "\n    E: " );
				for ( final E e : getEdgeTags().getTaggedWith( tag ) )
					str.append( e + "," );
			}
		}
		return str.toString();
	}
}
