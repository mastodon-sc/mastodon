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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.io.labels.LabelSetsSerializer;

public class RawTagSetModelIO
{
	public static < V extends Vertex< E >, E extends Edge< V > > void read(
			final DefaultTagSetModel< V, E > tagSetModel,
			final FileIdToGraphMap< V, E > idmap,
			final ObjectInputStream ois )
			throws IOException
	{
		new DefaultTagSetModel.SerialisationAccess< V, E >( tagSetModel )
		{
			void read() throws IOException
			{
				tagSetModel.getTagSetStructure().loadRaw( ois );
				LabelSetsSerializer.readPropertyMap( getVertexIdLabelSets(), intLabelSerializer, idmap.vertices(),
						ois );
				LabelSetsSerializer.readPropertyMap( getEdgeIdLabelSets(), intLabelSerializer, idmap.edges(), ois );
				updateObjTags();
			}
		}.read();
	}

	public static < V extends Vertex< E >, E extends Edge< V > > void write(
			final DefaultTagSetModel< V, E > tagSetModel,
			final GraphToFileIdMap< V, E > idmap,
			final ObjectOutputStream oos )
			throws IOException
	{
		new DefaultTagSetModel.SerialisationAccess< V, E >( tagSetModel )
		{
			void write() throws IOException
			{
				tagSetModel.getTagSetStructure().saveRaw( oos );
				LabelSetsSerializer.writePropertyMap( getVertexIdLabelSets(), intLabelSerializer, idmap.vertices(),
						oos );
				LabelSetsSerializer.writePropertyMap( getEdgeIdLabelSets(), intLabelSerializer, idmap.edges(), oos );
			}
		}.write();
	}

	private static final LabelSetsSerializer.LabelSerializer< Integer > intLabelSerializer =
			new LabelSetsSerializer.LabelSerializer< Integer >()
			{
				@Override
				public void writeLabel( final Integer label, final ObjectOutputStream oos ) throws IOException
				{
					oos.writeInt( label );
				}

				@Override
				public Integer readLabel( final ObjectInputStream ois ) throws IOException
				{
					return ois.readInt();
				}
			};
}
