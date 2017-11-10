package org.mastodon.revised.model.feature;

import java.io.File;
import java.io.IOException;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.properties.PropertyMap;

/**
 * Input/Output for a specific feature.
 * <p>
 *
 * @param <O>
 *            the type of the objects the feature is defined for.
 * @param <M>
 *            the type of the property map the feature relies on.
 * @param <AM>
 *            the type of the model the feature is calculated on and stored in.
 * @author Jean-Yves Tinevez
 */
public interface FeatureSerializer< O, M extends PropertyMap< O, ? >, AM >
{

	/**
	 * Serializes the specified feature to a raw file.
	 *
	 * @param feature
	 *            the feature to serialize.
	 * @param file
	 *            the file to write.
	 * @param support
	 *            the model the specified feature is defined on.
	 * @throws IOException
	 *             if an IO error occurs during serialization.
	 */
	public void serialize( Feature< O, M > feature, File file, AM support ) throws IOException;

	/**
	 * Deserializes a specific feature from the specified file.
	 * <p>
	 * The feature to deserialize must have been serialized with the same
	 * {@link FeatureSerializer}. Otherwise deserialization will cause an
	 * undefined behavior.
	 *
	 * @param file
	 *            the raw file to deserialize.
	 * @param support
	 *            the model the feature to deserialized is defined on. Must
	 *            itself be properly deserialized and unmodified since.
	 * @param fileIdToGraphMap the map collection that links file object ids to graph object
	 *            ids.
	 * @return the feature.
	 * @throws IOException
	 *             if an IO error occurs during deserialization.
	 */
	public Feature< O, M > deserialize( File file, AM support, FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException;
}
