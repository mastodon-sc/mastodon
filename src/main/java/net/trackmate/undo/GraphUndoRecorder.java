package net.trackmate.undo;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.features.Feature;
import net.trackmate.graph.features.FeatureChangeListener;
import net.trackmate.graph.features.Features;
import net.trackmate.undo.attributes.Attribute;
import net.trackmate.undo.attributes.AttributeChangeListener;
import net.trackmate.undo.attributes.Attributes;

/**
 * TODO: javadoc
 * TODO: figure out, when mappings can be removed from UndoIdBimaps.
 * TODO: move to package model.undo (?)
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GraphUndoRecorder<
			V extends Vertex< E >,
			E extends Edge< V >,
			L extends GraphUndoableEditList< V, E > >
		implements GraphListener< V, E >, UndoPointMarker
{
	private static final int defaultCapacity = 1000;

	protected boolean recording;

	protected final L edits;

	public static < V extends Vertex< E >, E extends Edge< V > >
		GraphUndoRecorder< V, E, GraphUndoableEditList< V, E > > create(
				final ListenableGraph< V, E > graph,
				final Features< V > vertexFeatures,
				final Features< E > edgeFeatures,
				final Attributes< V > vertexAttributes,
				final Attributes< E > edgeAttributes,
				final GraphIdBimap< V, E > idmap,
				final GraphUndoSerializer< V, E > serializer )
	{
		final UndoIdBimap< V > vertexUndoIdBimap = new UndoIdBimap<>( idmap.vertexIdBimap() );
		final UndoIdBimap< E > edgeUndoIdBimap = new UndoIdBimap<>( idmap.edgeIdBimap() );
		final GraphUndoableEditList< V, E > edits = new GraphUndoableEditList<>(
				defaultCapacity,
				graph,
				vertexFeatures,
				edgeFeatures,
				vertexAttributes,
				edgeAttributes,
				serializer,
				vertexUndoIdBimap,
				edgeUndoIdBimap );
		return new GraphUndoRecorder<>(
				graph,
				vertexFeatures,
				edgeFeatures,
				vertexAttributes,
				edgeAttributes,
				edits );
	}

	public GraphUndoRecorder(
			final ListenableGraph< V, E > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final Attributes< V > vertexAttributes,
			final Attributes< E > edgeAttributes,
			final L edits )
	{
		recording = true;
		this.edits = edits;
		graph.addGraphListener( this );
		vertexFeatures.addFeatureChangeListener( beforeVertexFeatureChange );
		edgeFeatures.addFeatureChangeListener( beforeEdgeFeatureChange );
		vertexAttributes.addAttributeChangeListener( beforeVertexAttributeChange );
		edgeAttributes.addAttributeChangeListener( beforeEdgeAttributeChange );
	}

	@Override
	public void setUndoPoint()
	{
		edits.setUndoPoint();
	}

	public void undo()
	{
//		System.out.println( "UndoRecorder.undo()" );
		recording = false;
		edits.undo();
		recording = true;
	}

	public void redo()
	{
//		System.out.println( "UndoRecorder.redo()" );
		recording = false;
		edits.redo();
		recording = true;
	}

	@Override
	public void graphRebuilt()
	{
		System.out.println( "UndoRecorder.graphRebuilt()" );
		System.out.println( "TODO!!!!" );
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.vertexAdded()" );
			edits.recordAddVertex( vertex );
		}
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.vertexRemoved()" );
			edits.recordRemoveVertex( vertex );
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.edgeAdded()" );
			edits.recordAddEdge( edge );
		}
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.edgeRemoved()" );
			edits.recordRemoveEdge( edge );
		}
	}

	private final FeatureChangeListener< V > beforeVertexFeatureChange = new FeatureChangeListener< V >()
	{
		@Override
		public void beforeFeatureChange( final Feature< ?, V, ? > feature, final V vertex )
		{
			if ( recording )
				edits.recordSetVertexFeature( feature, vertex );
		}
	};

	private final FeatureChangeListener< E > beforeEdgeFeatureChange = new FeatureChangeListener< E >()
	{
		@Override
		public void beforeFeatureChange( final Feature< ?, E, ? > feature, final E edge )
		{
			if ( recording )
				edits.recordSetEdgeFeature( feature, edge );
		}
	};

	private final AttributeChangeListener< V > beforeVertexAttributeChange = new AttributeChangeListener< V >()
	{
		@Override
		public void beforeAttributeChange( final Attribute< V > attribute, final V vertex )
		{
			if ( recording )
				edits.recordSetVertexAttribute( attribute, vertex );
		}
	};

	private final AttributeChangeListener< E > beforeEdgeAttributeChange = new AttributeChangeListener< E >()
	{
		@Override
		public void beforeAttributeChange( final Attribute< E > attribute, final E edge )
		{
			if ( recording )
				edits.recordSetEdgeAttribute( attribute, edge );
		}
	};
}
