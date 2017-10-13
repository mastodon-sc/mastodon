package org.mastodon.revised.model.feature;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;

import org.mastodon.io.ObjectToFileIdMap;

/**
 * Interface for feature models, classes that manage a collection of features in
 * a model graph.
 *
 * @author Jean-Yves Tinevez
 */
public interface FeatureModel
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
	 * Appends the features of this model to the specified stream, using the
	 * specified mapping from object feature are defined for, to the file ids
	 * they are saved under.
	 * <p>
	 * Because the feature model can deal with features defined over several
	 * classes of objects, the mapping is specified as a map from object class
	 * to actual mapping.
	 * There must be a mapping for each class of target objects that has a
	 * feature in this model, in the specified map.
	 *
	 *
	 * @param oos
	 *            The stream to appends features to. Will not be closed.
	 * @param fileIdMaps
	 *            the mapping from object to file ids.
	 * @throws IOException
	 */
	public void writeRaw( ObjectOutputStream oos, Map< Class< ? >, ObjectToFileIdMap< ? > > fileIdMaps ) throws IOException;

}
