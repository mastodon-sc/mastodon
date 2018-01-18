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

		tagSetStructure.set( tss );
		// TODO: undo-record TagSetStructure change

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

}
