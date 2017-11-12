package org.mastodon.revised.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.io.GraphSerializer;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.graph.ref.ListenableGraphImp;
import org.mastodon.io.properties.PropertyMapSerializers;
import org.mastodon.io.properties.RawPropertyIO;
import org.mastodon.pool.MappedElement;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.properties.PropertyMap;
import org.mastodon.spatial.VertexPositionChangeProvider;
import org.mastodon.spatial.VertexPositionListener;

public class AbstractModelGraph<
		G extends AbstractModelGraph< G, VP, EP, V, E, T >,
		VP extends AbstractSpotPool< V, E, T, G >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractSpot< V, E, VP, T, G >,
		E extends AbstractListenableEdge< E, V, EP, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
	implements VertexPositionChangeProvider< V >, GraphChangeNotifier
{
	protected final GraphIdBimap< V, E > idmap;

	/**
	 * Subclasses need to add any vertex {@link PropertyMap}s that they want to
	 * be serialized with the graph.
	 */
	protected final PropertyMapSerializers< V > vertexPropertySerializers;

	protected final ReentrantReadWriteLock lock;

	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		idmap = new GraphIdBimap<>( vertexPool, edgePool );
		vertexPropertySerializers = new PropertyMapSerializers<>();
		lock = new ReentrantReadWriteLock();
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
	 * @return the map collection that links file object ids to graph object
	 *         ids.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public FileIdToGraphMap< V, E > loadRaw(
			final File file,
			final GraphSerializer< V, E > serializer )
					throws IOException
	{
		final FileInputStream fis = new FileInputStream( file );
		final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( fis, 1024 * 1024 ) );
		final FileIdToGraphMap< V, E > fileIdToGraphMap = readRaw( ois, serializer );
		ois.close();
		return fileIdToGraphMap;
	}

	public FileIdToGraphMap< V, E > readRaw(
			final ObjectInputStream ois,
			final GraphSerializer< V, E > serializer )
					throws IOException
	{
		pauseListeners();
		clear();
		final FileIdToGraphMap< V, E > fileIdMap = RawGraphIO.read( this, idmap, serializer, ois );
		RawPropertyIO.readPropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, ois );
		// TODO: edge properties
//		RawFeatureIO.readFeatureMaps( fileIdMap.vertices(), vertexFeatures, ois );
//		RawFeatureIO.readFeatureMaps( fileIdMap.edges(), edgeFeatures, ois );
		resumeListeners();
		return fileIdMap;
	}


	/**
	 * Saves this model-graph to the specified raw file using the specified
	 * serializer.
	 *
	 * @param file
	 *            the raw file to save.
	 * @param serializer
	 *            the serializer used for writing individual vertices.
	 * @return the map collection that links graph object id to file object id.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public GraphToFileIdMap< V, E > saveRaw(
			final File file,
			final GraphSerializer< V, E > serializer )
					throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) );
		final GraphToFileIdMap< V, E > fileIdMap = RawGraphIO.write( this, idmap, serializer, oos );
		RawPropertyIO.writePropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, oos );
		// TODO: edge properties
//		RawFeatureIO.writeFeatureMaps( fileIdMap.vertices(), vertexFeatures, vertexFeaturesToSerialize, oos );
//		RawFeatureIO.writeFeatureMaps( fileIdMap.edges(), edgeFeatures, edgeFeaturesToSerialize, oos );
		oos.close();
		return fileIdMap;
	}

	public ReentrantReadWriteLock getLock()
	{
		return lock;
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
	public void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}

	/**
	 * Register a {@link VertexPositionListener} that will be notified when
	 * feature values are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	@Override
	public boolean addVertexPositionListener( final VertexPositionListener< V > listener )
	{
		return vertexPool.position.addPropertyChangeListener( wrap( listener ) );
	}

	/**
	 * Removes the specified {@link VertexPositionListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	@Override
	public boolean removeVertexPositionListener( final VertexPositionListener< V > listener )
	{
		return vertexPool.position.removePropertyChangeListener( wrap( listener ) );
	}

	private VertexPositionListenerWrapper< V > wrap( final VertexPositionListener< V > l )
	{
		return new VertexPositionListenerWrapper<>( l );
	}

	private static class VertexPositionListenerWrapper< V > implements PropertyChangeListener< V >
	{
		private final VertexPositionListener< V > l;

		VertexPositionListenerWrapper( final VertexPositionListener< V > l )
		{
			this.l = l;
		}

		@Override
		public void propertyChanged( final V vertex )
		{
			l.vertexPositionChanged( vertex );
		}

		@Override
		public int hashCode()
		{
			return l.hashCode();
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public boolean equals( final Object obj )
		{
			return ( obj != null )
					&& ( obj instanceof VertexPositionListenerWrapper )
					&& l.equals( ( ( VertexPositionListenerWrapper< V > ) obj ).l );
		}
	}
}
