package org.mastodon.feature.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface for classes that can de/serialize a feature.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <F>
 *            the type of the feature to serialize.
 * @param <O>
 *            the class of the object the feature is defined for.
 */
public interface FeatureSerializer< F extends Feature< O >, O > extends SciJavaPlugin
{

	/**
	 * Returns the feature specifications of the feature this
	 * {@link FeatureSerializer} can de/serialize.
	 *
	 * @return the feature specifications of the feature this
	 *         {@link FeatureSerializer} can de/serialize.
	 */
	public FeatureSpec< F, O > getFeatureSpec();

	/**
	 * Serializes the feature to the specified output stream.
	 *
	 * @param feature
	 *            the feature to serialize.
	 * @param idmap
	 *            the {@link ObjectToFileIdMap}.
	 * @param oos
	 *            the output stream.
	 * @throws IOException
	 *             if an I/O error occurs while writing the feature file.
	 */
	public void serialize( F feature, ObjectToFileIdMap< O > idmap, ObjectOutputStream oos ) throws IOException;

	/**
	 * Deserializes a feature from the specified input stream.
	 *
	 * @param idmap
	 *            the {@link FileIdToObjectMap}.
	 * @param pool
	 *            the {@link RefCollection} used to create property maps inside
	 *            the feature.
	 * @param ois
	 *            the input stream.
	 * @return a new feature instance.
	 * @throws IOException
	 *             if an I/O error occurs while reading the feature file.
	 * @throws ClassNotFoundException
	 *             if the class of the feature or the class of its target cannot
	 *             be found.
	 */
	public F deserialize( final FileIdToObjectMap< O > idmap, final RefCollection< O > pool, ObjectInputStream ois ) throws IOException, ClassNotFoundException;

}
