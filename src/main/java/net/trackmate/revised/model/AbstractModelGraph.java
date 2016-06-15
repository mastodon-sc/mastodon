package net.trackmate.revised.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.features.Feature;
import net.trackmate.graph.io.RawFeatureIO;
import net.trackmate.graph.io.RawGraphIO;
import net.trackmate.graph.io.RawGraphIO.FileIdToGraphMap;
import net.trackmate.graph.io.RawGraphIO.GraphToFileIdMap;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableEdgePool;
import net.trackmate.graph.ref.ListenableGraphImp;
import net.trackmate.pool.MappedElement;
import net.trackmate.spatial.VertexPositionChangeProvider;
import net.trackmate.spatial.VertexPositionListener;
import net.trackmate.undo.attributes.Attribute;
import net.trackmate.undo.attributes.AttributesImp;

public class AbstractModelGraph<
		G extends AbstractModelGraph< G, VP, EP, V, E, T >,
		VP extends AbstractSpotPool< V, E, T, G >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractSpot< V, E, T, G >,
		E extends AbstractListenableEdge< E, V, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
	implements VertexPositionChangeProvider< V >
{
	protected final GraphIdBimap< V, E > idmap;

	protected final AttributesImp< V > vertexAttributes;

	protected final AttributesImp< E > edgeAttributes;

	private final ArrayList< VertexPositionListener< V > > vertexPositionListeners;

	public final Attribute< V > VERTEX_POSITION;

	@SuppressWarnings( "unchecked" )
	public AbstractModelGraph( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		vertexPool.linkModelGraph( ( G ) this );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		vertexAttributes = new AttributesImp<>();
		edgeAttributes = new AttributesImp<>();
		VERTEX_POSITION = vertexAttributes.createAttribute( AbstractSpot.createPositionAttributeSerializer( vertexPool.numDimensions() ), "vertex position" );
		vertexPositionListeners = new ArrayList<>();
	}

	@SuppressWarnings( "unchecked" )
	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		vertexPool.linkModelGraph( ( G ) this );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		vertexAttributes = new AttributesImp<>();
		edgeAttributes = new AttributesImp<>();
		VERTEX_POSITION = vertexAttributes.createAttribute( AbstractSpot.createPositionAttributeSerializer( vertexPool.numDimensions() ), "vertex position" );
		vertexPositionListeners = new ArrayList<>();
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
		RawFeatureIO.readFeatureMaps( fileIdMap.vertices(), vertexFeatures, ois );
		RawFeatureIO.readFeatureMaps( fileIdMap.edges(), edgeFeatures, ois );
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
			final List< Feature< ?, V, ? > > vertexFeaturesToSerialize,
			final List< Feature< ?, E, ? > > edgeFeaturesToSerialize )
					throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( fos );
		final GraphToFileIdMap< V, E > fileIdMap = RawGraphIO.write( this, idmap, serializer, oos );
		RawFeatureIO.writeFeatureMaps( fileIdMap.vertices(), vertexFeatures, vertexFeaturesToSerialize, oos );
		RawFeatureIO.writeFeatureMaps( fileIdMap.edges(), edgeFeatures, edgeFeaturesToSerialize, oos );
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

	void notifyBeforeVertexPositionChange( final V vertex )
	{
		vertexAttributes.notifyBeforeAttributeChange( VERTEX_POSITION, vertex );
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
	public void pauseListeners()
	{
		super.pauseListeners();
	}

	@Override
	public void resumeListeners()
	{
		super.resumeListeners();
	}

	@Override
	protected void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}
}
