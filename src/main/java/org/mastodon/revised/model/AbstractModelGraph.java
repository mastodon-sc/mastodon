package org.mastodon.revised.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.graph.ref.ListenableGraphImp;
import org.mastodon.io.properties.PropertyMapSerializers;
import org.mastodon.io.properties.RawPropertyIO;
import org.mastodon.pool.MappedElement;
import org.mastodon.spatial.VertexPositionChangeProvider;
import org.mastodon.spatial.VertexPositionListener;
import org.mastodon.undo.attributes.Attribute;

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

	protected final ArrayList< Attribute< V > > vertexAttributes;

	protected final ArrayList< Attribute< E > > edgeAttributes;

	protected final PropertyMapSerializers< V > vertexPropertySerializers;

	private final ArrayList< VertexPositionListener< V > > vertexPositionListeners;

	public final Attribute< V > VERTEX_POSITION;

	@SuppressWarnings( "unchecked" )
	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		vertexPool.linkModelGraph( ( G ) this );
		idmap = new GraphIdBimap<>( vertexPool, edgePool );
		vertexAttributes = new ArrayList<>();
		edgeAttributes = new ArrayList<>();
		vertexPropertySerializers = new PropertyMapSerializers<>();
		vertexPositionListeners = new ArrayList<>();

		VERTEX_POSITION = new Attribute< V >( AbstractSpot.createPositionAttributeSerializer( vertexPool.numDimensions() ), "vertex position" );
		vertexAttributes.add( VERTEX_POSITION );
		vertexPool.position.addBeforePropertyChangeListener( v -> VERTEX_POSITION.notifyBeforeAttributeChange( v ) );
		vertexPool.position.addPropertyChangeListener( v -> notifyVertexPositionChanged( v ) );
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
		RawPropertyIO.readPropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, ois );
		// TODO: edge properties
//		RawFeatureIO.readFeatureMaps( fileIdMap.vertices(), vertexFeatures, ois );
//		RawFeatureIO.readFeatureMaps( fileIdMap.edges(), edgeFeatures, ois );
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
	 * @param vertexFeaturesToSerialize
	 *            the vertex features to serialize.
	 * @param edgeFeaturesToSerialize
	 *            the edge features to serialize.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public void saveRaw(
			final File file,
			final RawGraphIO.Serializer< V, E > serializer )
					throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( fos );
		final GraphToFileIdMap< V, E > fileIdMap = RawGraphIO.write( this, idmap, serializer, oos );
		RawPropertyIO.writePropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, oos );
		// TODO: edge properties
//		RawFeatureIO.writeFeatureMaps( fileIdMap.vertices(), vertexFeatures, vertexFeaturesToSerialize, oos );
//		RawFeatureIO.writeFeatureMaps( fileIdMap.edges(), edgeFeatures, edgeFeaturesToSerialize, oos );
		oos.close();
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
		if ( ! vertexPositionListeners.contains( listener ) )
		{
			vertexPositionListeners.add( listener );
			return true;
		}
		return false;
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
		return vertexPositionListeners.remove( listener );
	}

	void notifyVertexPositionChanged( final V vertex )
	{
		for ( final VertexPositionListener< V > l : vertexPositionListeners )
			l.vertexPositionChanged( vertex );
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
		for ( final Attribute< V > attribute : vertexAttributes )
			attribute.pauseListeners();
		for ( final Attribute< E > attribute : edgeAttributes )
			attribute.pauseListeners();
	}

	@Override
	protected void resumeListeners()
	{
		super.resumeListeners();
		for ( final Attribute< V > attribute : vertexAttributes )
			attribute.resumeListeners();
		for ( final Attribute< E > attribute : edgeAttributes )
			attribute.resumeListeners();
	}

	@Override
	public void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}
}
