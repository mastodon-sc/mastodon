package org.mastodon.revised.model.feature;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.revised.model.AbstractModel;

/**
 * Interface for feature models, classes that manage a collection of features in
 * a model graph.
 *
 * @author Jean-Yves Tinevez
 * @param <AM>
 *            the type of the model over which the features stored in this
 *            feature model are defined.
 */
public interface FeatureModel< AM extends AbstractModel< ?, ?, ? > >
{

	public Set< Feature< ?, ? > > getFeatureSet( Class< ? > targetClass );

	/**
	 * Clears this feature model.
	 */
	public void clear();

	/**
	 * Registers the feature key and the feature projections provided by the
	 * specified feature.
	 *
	 * @param feature
	 *            the feature.
	 */
	public void declareFeature( final Feature< ?, ? > feature );

	/**
	 * Returns the feature with the specified key.
	 *
	 * @param key
	 *            the key of the feature to retrieve.
	 * @return the feature, or <code>null</code> if a feature with the specified
	 *         key is not registered in this model.
	 */
	public Feature< ?, ? > getFeature( String key );

	/**
	 * Saves the {@link Feature}s managed by this feature model as individual
	 * raw files.
	 *
	 * @param projectFolder
	 *            the feature will be saved in a sub-folder of this project
	 *            folder.
	 * @param serializers
	 *            the map of serializers to use to save features. If a
	 *            serializer is not found in the map for a feature with a
	 *            specific key, that feature is not saved.
	 * @param support
	 *            the model on which features are defined.
	 * @param graphToFileIdMap
	 *            the map collection that linkg graph object ids to file object
	 *            ids.
	 */
	public void saveRaw( File projectFolder, Map< String, FeatureSerializer< ?, ?, AM > > serializers, AM support, GraphToFileIdMap< ?, ? > graphToFileIdMap );

	/**
	 * Clears this feature model and loads the {@link Feature}s from files in a
	 * sub-folder of the specified project folder.
	 *
	 * @param projectFolder
	 *            the feature will be loaded from a sub-folder of this project
	 *            folder.
	 * @param serializers
	 *            the map of serializers to use to save features. If a
	 *            serializer is not found in the map for a feature with a
	 *            specific key, that feature is not loaded.
	 * @param support
	 *            the model on which features are defined.
	 * @param fileIdToGraphMap
	 *            the map collection that links file object ids to graph object
	 *            ids.
	 */
	public void loadRaw( File projectFolder, Map< String, FeatureSerializer< ?, ?, AM > > serializers, final AM support, FileIdToGraphMap< ?, ? > fileIdToGraphMap );


}
