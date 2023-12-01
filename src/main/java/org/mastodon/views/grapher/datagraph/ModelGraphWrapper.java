/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.datagraph;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;

public class ModelGraphWrapper< V extends Vertex< E > & HasLabel, E extends Edge< V > >
{
	final GraphIdBimap< V, E > idmap;

	ModelGraphWrapper( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
	}

	ModelVertexWrapper createVertexWrapper( final DataVertex vertex )
	{
		return new ModelVertexWrapper( idmap.vertexIdBimap().createRef(), vertex );
	}

	ModelEdgeWrapper createEdgeWrapper( final DataEdge edge )
	{
		return new ModelEdgeWrapper( idmap.edgeIdBimap().createRef(), edge );
	}

	class ModelVertexWrapper
	{
		private final V ref;

		private final DataVertex vertex;

		public ModelVertexWrapper( final V ref, final DataVertex vertex )
		{
			this.ref = ref;
			this.vertex = vertex;
		}

		public String getLabel()
		{
			return getModelVertex().getLabel();
		}

		public void setLabel( final String label )
		{
			getModelVertex().setLabel( label );
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

		public ModelEdgeWrapper( final E ref, final DataEdge edge )
		{
			this.ref = ref;
		}

		public E getReusableRef()
		{
			return ref;
		}
	}
}
