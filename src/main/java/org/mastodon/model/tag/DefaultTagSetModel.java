/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.model.tag;

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
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.undo.Recorder;
import org.mastodon.undo.UndoableEdit;
import org.scijava.listeners.Listeners;

/**
 * Default implementation of {@link TagSetModel}.
 * <p>
 * Assigns tags to vertices and edges of a graph, according to a {@link TagSetStructure}.
 * <p>
 * Provides facilities for serialization and undo/redo.
 *
 * @param <V>
 * 		the type of the vertices in the graph.
 * @param <E>
 * 		the type of the edges in the graph.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class DefaultTagSetModel< V extends Vertex< E >, E extends Edge< V > > implements TagSetModel< V, E >
{
	private final ReadOnlyGraph< V, E > graph;

	private final TagSetStructure tagSetStructure;

	private final LabelSets< V, Integer > vertexIdLabelSets;

	private final LabelSets< E, Integer > edgeIdLabelSets;

	private final DefaultObjTags< V > vertexTags;

	private final DefaultObjTags< E > edgeTags;

	private final Listeners.List< TagSetModelListener > listeners;

	private Recorder< SetTagSetStructureUndoableEdit > editRecorder;

	private boolean emitEvents;

	public DefaultTagSetModel( final ReadOnlyGraph< V, E > graph )
	{
		this( graph, RefCollections.tryGetRefPool( graph.vertices() ), RefCollections.tryGetRefPool( graph.edges() ) );
	}

	public DefaultTagSetModel( final ReadOnlyGraph< V, E > graph, final RefPool< V > vertexPool,
			final RefPool< E > edgePool )
	{
		this.graph = graph;
		this.tagSetStructure = new TagSetStructure();
		vertexIdLabelSets = new LabelSets<>( vertexPool );
		edgeIdLabelSets = new LabelSets<>( edgePool );
		vertexTags = new DefaultObjTags<>( vertexIdLabelSets, tagSetStructure );
		edgeTags = new DefaultObjTags<>( edgeIdLabelSets, tagSetStructure );
		listeners = new Listeners.SynchronizedList<>();
		emitEvents = true;
	}

	@Override
	public TagSetStructure getTagSetStructure()
	{
		return tagSetStructure;
	}

	@Override
	public void setTagSetStructure( final TagSetStructure tss )
	{
		// find tags that have been removed from TagSetStructure
		final Set< Integer > removedIds = tagSetStructure.getTagSets().stream().flatMap( ts -> ts.getTags().stream() )
				.map( Tag::id ).collect( Collectors.toSet() );
		final Set< Integer > newIds = tss.getTagSets().stream().flatMap( ts -> ts.getTags().stream() ).map( Tag::id )
				.collect( Collectors.toSet() );
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

		if ( editRecorder != null && emitEvents )
		{
			editRecorder.record( new SetTagSetStructureUndoableEdit( this, tagSetStructure, tss ) );
		}

		tagSetStructure.set( tss );
		vertexTags.update( tagSetStructure );
		edgeTags.update( tagSetStructure );

		if ( emitEvents )
			listeners.list.forEach( TagSetModelListener::tagSetStructureChanged );
	}

	@Override
	public DefaultObjTags< V > getVertexTags()
	{
		return vertexTags;
	}

	@Override
	public DefaultObjTags< E > getEdgeTags()
	{
		return edgeTags;
	}

	public void setUndoRecorder( final Recorder< SetTagSetStructureUndoableEdit > editRecorder )
	{
		this.editRecorder = editRecorder;
	}

	@Override
	public Listeners< TagSetModelListener > listeners()
	{
		return listeners;
	}

	@Override
	public void pauseListeners()
	{
		emitEvents = false;
	}

	@Override
	public void resumeListeners()
	{
		emitEvents = true;
		if ( emitEvents )
			listeners.list.forEach( TagSetModelListener::tagSetStructureChanged );
	}

	@Override
	public void clear()
	{
		vertexIdLabelSets.clear();
		edgeIdLabelSets.clear();
	}

	/**
	 * Internals. Can be derived for implementing de/serialisation and
	 * undo/redo.
	 *
	 * @param <V>
	 * 		the type of the vertices in the graph.
	 * @param <E>
	 * 		the type of the edges in the graph.
	 */
	public static class SerialisationAccess< V extends Vertex< E >, E extends Edge< V > >
	{
		private final DefaultTagSetModel< V, E > tagSetModel;

		protected SerialisationAccess( final DefaultTagSetModel< V, E > tagSetModel )
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
		private final DefaultTagSetModel< ?, ? > tagSetModel;

		private final TagSetStructure oldTss;

		private final TagSetStructure newTss;

		SetTagSetStructureUndoableEdit( final DefaultTagSetModel< ?, ? > tagSetModel, final TagSetStructure oldTss,
				final TagSetStructure newTss )
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
