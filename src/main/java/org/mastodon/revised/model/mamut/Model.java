package org.mastodon.revised.model.mamut;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.properties.Property;
import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.DefaultFeatureModel;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureSerializer;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;
import org.mastodon.undo.GraphUndoRecorder;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.NullContextException;

import net.imglib2.RealLocalizable;

/**
 * A model built to manage a graph of {@link Spot}s and {@link Link}s.
 * <p>
 * Spots are the graph vertices. They are {@link RealLocalizable} 3D ellipsoids,
 * whose volume is specified through a covariance matrix. Graph edges are plain
 * links between spots.
 * <p>
 * On top of a graph structure for the spots, this model manages a
 * {@link SpatioTemporalIndex}, that can slice the model at some given
 * time-points.
 * <p>
 * The model graph is only exposed as a {@link ReadOnlyGraph}. All updates to
 * the model graph are done through {@link Model}. This includes vertex and edge
 * attribute changes (although this currently cannot be enforced through
 * {@link ReadOnlyGraph}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class Model extends AbstractModel< ModelGraph, Spot, Link > implements UndoPointMarker, Contextual
{
	/*
	 * SpatioTemporalIndex of model spots
	 */
	private final SpatioTemporalIndex< Spot > index;

	private final GraphUndoRecorder< Spot, Link > undoRecorder;

	private final FeatureModel< Model > featureModel;

	private final Context context;

	public Model()
	{
		super( new ModelGraph() );

		/*
		 * Instantiate context with required services.
		 */
		this.context = new Context( MamutFeatureComputerService.class );

		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.idmap().vertexIdBimap() );

		final int initialCapacity = 1024;

		final List< Property< Spot > > vertexUndoableProperties = new ArrayList<>();
		vertexUndoableProperties.add( modelGraph.getVertexPool().positionProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().covarianceProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().boundingSphereRadiusSquProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().labelProperty() );

		final List< Property< Link > > edgeUndoableProperties = new ArrayList<>();

		featureModel = new DefaultFeatureModel< Model >();

		undoRecorder = new GraphUndoRecorder<>(
				initialCapacity,
				modelGraph,
				modelGraph.idmap(),
				ModelSerializer.getInstance().getVertexSerializer(),
				ModelSerializer.getInstance().getEdgeSerializer(),
				vertexUndoableProperties,
				edgeUndoableProperties );
	}

	/**
	 * Clears this model and loads the model from the specified raw file.
	 * <p>
	 * Feature values will be loaded from the folder the specified file is in,
	 * using the {@link FeatureComputer}s visible at runtime.
	 *
	 * @param file
	 *            the raw file to load.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void loadRaw( final File file ) throws IOException
	{
		/*
		 * Read the model-graph.
		 */
		final FileIdToGraphMap< Spot, Link > fileIdToGraphMap = modelGraph.loadRaw( file, ModelSerializer.getInstance() );

		/*
		 * Read the feature values. We get the serializers from the MaMuT
		 * feature computers.
		 */
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Collection< FeatureComputer< ?, ?, Model > > featureComputers = featureComputerService.getFeatureComputers();
		final Map< String, FeatureSerializer< ?, ?, Model > > featureSerializers = new HashMap<>( featureComputers.size() );
		for ( final FeatureComputer featureComputer : featureComputers )
		{
			final PropertyMap pm = featureComputer.createPropertyMap( this );
			featureSerializers.put( featureComputer.getKey(), ( FeatureSerializer< ?, ?, Model > ) featureComputer.getSerializer( pm ) );
		}
		featureModel.loadRaw( file.getParentFile(), featureSerializers, fileIdToGraphMap );
	}

	/**
	 * Saves this model to the specified raw file.
	 * <p>
	 * Feature values will be saved in individual raw files in a subfolder of
	 * the folder the specified file is in.
	 *
	 * @param file
	 *            the raw file to save.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public void saveRaw( final File file ) throws IOException
	{
		// Serialize model graph.
		modelGraph.saveRaw( file, ModelSerializer.getInstance() );

		// Serialize feature model.
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Collection< FeatureComputer< ?, ?, Model > > featureComputers = featureComputerService.getFeatureComputers();
		final Map< String, FeatureSerializer< ?, ?, Model > > featureSerializers = new HashMap<>( featureComputers.size() );
		for ( final FeatureComputer< ?, ?, Model > featureComputer : featureComputers )
			featureSerializers.put( featureComputer.getKey(), featureComputer.getSerializer() );
		featureModel.saveRaw( file.getParentFile(), featureSerializers, this );
	}

	/**
	 * Exposes the spatio-temporal index of this model.
	 *
	 * @return the spatio-temporal index.
	 */
	public SpatioTemporalIndex< Spot > getSpatioTemporalIndex()
	{
		return index;
	}

	public void undo()
	{
		undoRecorder.undo();
		modelGraph.notifyGraphChanged();
	}

	public void redo()
	{
		undoRecorder.redo();
		modelGraph.notifyGraphChanged();
	}

	@Override
	public void setUndoPoint()
	{
		undoRecorder.setUndoPoint();
	}

	public FeatureModel< Model > getFeatureModel()
	{
		return featureModel;
	}

	// -- Contextual methods --

	@Override
	public Context context()
	{
		if ( context == null )
			throw new NullContextException();
		return context;
	}

	@Override
	public Context getContext()
	{
		return context;
	}
}
