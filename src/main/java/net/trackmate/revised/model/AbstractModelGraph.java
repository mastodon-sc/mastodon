package net.trackmate.revised.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.io.RawFeatureIO;
import net.trackmate.graph.io.RawGraphIO;
import net.trackmate.graph.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.graph.io.RawGraphIO.GraphToFileIdMap;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableEdgePool;
import net.trackmate.graph.ref.AbstractListenableVertex;
import net.trackmate.graph.ref.AbstractListenableVertexPool;
import net.trackmate.graph.ref.ListenableGraphImp;
import net.trackmate.pool.MappedElement;

public class AbstractModelGraph<
		VP extends AbstractListenableVertexPool< V, E, T >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractListenableVertex< V, E, T >,
		E extends AbstractListenableEdge< E, V, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
{
	protected final GraphIdBimap< V, E > idmap;

	public AbstractModelGraph( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
	}

	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
	}

	/**
	 * Exposes the bidirectional map between vertices and their id, and between
	 * edges and their id.
	 *
	 * @return the bidirectional id map.
	 */
	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return idmap;
	}

	/**
	 * Clears this model and loads the model from the specified raw file using
	 * the specified serializer.
	 *
	 * @param file
	 *            the raw file to load.
	 * @param serializer
	 *            the serializer used for reading individual vertices.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public void loadRaw(
			final File file,
			final RawGraphIO.Serializer< V, E > serializer )
					throws IOException
	{
		final FileInputStream fis = new FileInputStream( file );
		final ObjectInputStream ois = new ObjectInputStream( fis );
		pauseListeners();
		clear();
		final FileIdToGraphMap< V, E > fileIdMap = RawGraphIO.read( this, idmap, serializer, ois );
		RawFeatureIO.readFeatureMaps( fileIdMap, features, ois );
		ois.close();
		resumeListeners();
	}

	/**
	 * Saves this model to the specified raw file using the specified
	 * serializer.
	 *
	 * @param file
	 *            the raw file to save.
	 * @param serializer
	 *            the serializer used for writing individual vertices.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public void saveRaw(
			final File file,
			final RawGraphIO.Serializer< V, E > serializer,
			final List< VertexFeature< ?, V, ? > > featuresToSerialize )
					throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( fos );
		final GraphToFileIdMap< V, E > fileIdMap = RawGraphIO.write( this, idmap, serializer, oos );
		RawFeatureIO.writeFeatureMaps( fileIdMap, features, featuresToSerialize, oos );
		oos.close();
	}

	@Override
	protected void clear()
	{
		super.clear();
	}

	@Override
	protected void pauseListeners()
	{
		super.pauseListeners();
	}

	@Override
	protected void resumeListeners()
	{
		super.resumeListeners();
	}

	@Override
	protected void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}
}
