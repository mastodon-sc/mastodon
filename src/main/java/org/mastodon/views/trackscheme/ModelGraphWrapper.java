/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.views.trackscheme.wrap.TrackSchemeProperties;

public class ModelGraphWrapper< V extends Vertex< E >, E extends Edge< V > >
{
	final GraphIdBimap< V, E > idmap;

	final TrackSchemeProperties< V, E > modelGraphProperties;

	ModelGraphWrapper(
			final GraphIdBimap< V, E > idmap,
			final TrackSchemeProperties< V, E > modelGraphProperties )
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

		public int getFirstTimepoint()
		{
			return modelGraphProperties.getFirstTimePoint( getModelVertex() );
		}

		public String getRootLabel()
		{
			return modelGraphProperties.getFirstLabel( getModelVertex() );
		}
	}

	class ModelEdgeWrapper
	{
		private final E ref;

		//		private final TrackSchemeEdge edge;

		public ModelEdgeWrapper( final E ref, final TrackSchemeEdge edge )
		{
			this.ref = ref;
			//			this.edge = edge;
		}

		public E getReusableRef()
		{
			return ref;
		}

		//		private E getModelEdge()
		//		{
		//			return idmap.getEdge( edge.getModelEdgeId(), ref );
		//		}
	}
}
