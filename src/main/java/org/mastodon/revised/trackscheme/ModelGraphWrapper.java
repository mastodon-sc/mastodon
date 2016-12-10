package org.mastodon.revised.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.trackscheme.wrap.ModelGraphProperties;

public class ModelGraphWrapper< V extends Vertex< E >, E extends Edge< V > >
{
	final GraphIdBimap< V, E > idmap;

	final ModelGraphProperties< V, E > modelGraphProperties;

	ModelGraphWrapper(
			final GraphIdBimap< V, E > idmap,
			final ModelGraphProperties< V, E > modelGraphProperties )
	{
		this.idmap = idmap;
		this.modelGraphProperties = modelGraphProperties;
	}

	ModelVertexWrapper createVertexWrapper( final TrackSchemeVertex vertex )
	{
		return new ModelVertexWrapper( idmap.vertexIdBimap().createRef(), vertex );
	}

	ModelEdgeWrapper createEdgeWrapper( final TrackSchemeEdge edge )
	{
		return new ModelEdgeWrapper( idmap.edgeIdBimap().createRef(), edge );
	}

	class ModelVertexWrapper
	{
		private final V ref;

		private final TrackSchemeVertex vertex;

		public ModelVertexWrapper( final V ref, final TrackSchemeVertex vertex )
		{
			this.ref = ref;
			this.vertex = vertex;
		}

		public int getTimepoint()
		{
			return modelGraphProperties.getTimepoint( getModelVertex() );
		}

		public String getLabel()
		{
			return modelGraphProperties.getLabel( getModelVertex() );
		}

		public void setLabel( final String label )
		{
			modelGraphProperties.setLabel( getModelVertex(), label );
		}

		public V getReusableRef()
		{
			return ref;
		}

		private V getModelVertex()
		{
			return idmap.getVertex( vertex.getModelVertexId(), ref );
		}
	}

	class ModelEdgeWrapper
	{
		private final E ref;

		private final TrackSchemeEdge edge;

		public ModelEdgeWrapper( final E ref, final TrackSchemeEdge edge )
		{
			this.ref = ref;
			this.edge = edge;
		}

		public E getReusableRef()
		{
			return ref;
		}

		private E getModelEdge()
		{
			return idmap.getEdge( edge.getModelEdgeId(), ref );
		}
	}
}
