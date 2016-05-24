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
import net.trackmate.graph.VertexFeature;
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

public class AbstractModelGraph<
		G extends AbstractModelGraph< G, VP, EP, V, E, T >,
		VP extends AbstractSpotPool< V, E, T, G >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractSpot3D< V, E, T, G >,
		E extends AbstractListenableEdge< E, V, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
	implements VertexPositionChangeProvider< V >
{
	protected final GraphIdBimap< V, E > idmap;

	private final ArrayList< AbstractSpotListener< V > > spotListeners;

	private final ArrayList< VertexPositionListener< V > > vertexPositionListeners;

	@SuppressWarnings( "unchecked" )
	public AbstractModelGraph( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		vertexPool.linkModelGraph( ( G ) this );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		spotListeners = new ArrayList<>();
		vertexPositionListeners = new ArrayList<>();
	}

	@SuppressWarnings( "unchecked" )
	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		vertexPool.linkModelGraph( ( G ) this );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		spotListeners = new ArrayList<>();
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

	/**
	 * Register a {@link AbstractSpotListener} that will be notified when
	 * feature values are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addAbstractSpotListener( final AbstractSpotListener< V > listener )
	{
		if ( ! spotListeners.contains( listener ) )
		{
			spotListeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified {@link AbstractSpotListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeAbstractSpotListener( final AbstractSpotListener< V > listener )
	{
		return spotListeners.remove( listener );
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
		for ( final AbstractSpotListener< V > l : spotListeners )
			l.beforePositionChange( vertex );
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
