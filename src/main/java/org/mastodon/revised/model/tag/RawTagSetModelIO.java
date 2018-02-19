package org.mastodon.revised.model.tag;

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
				LabelSetsSerializer.readPropertyMap( getVertexIdLabelSets(), intLabelSerializer, idmap.vertices(), ois );
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
				LabelSetsSerializer.writePropertyMap( getVertexIdLabelSets(), intLabelSerializer, idmap.vertices(), oos );
				LabelSetsSerializer.writePropertyMap( getEdgeIdLabelSets(), intLabelSerializer, idmap.edges(), oos );
			}
		}.write();
	}

	private static final LabelSetsSerializer.LabelSerializer< Integer > intLabelSerializer = new LabelSetsSerializer.LabelSerializer< Integer >()
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
