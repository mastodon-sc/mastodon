package org.mastodon.graph.features;

import java.util.HashMap;
import java.util.Map;

import gnu.trove.map.TIntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;

/**
 * Assign unique IDs to features.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class FeatureRegistry
{
	private static int idGenerator = 0;

	private static final Map< String, Integer > featureKeyIds = new HashMap<>();

	private static final Map< String, Feature< ?, ?, ? > > features = new HashMap<>();

	private static final TIntObjectMap< Feature< ?, ?, ? > > featuresById = new TIntObjectArrayMap<>();

	public static final class DuplicateKeyException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public DuplicateKeyException()
		{
			super();
		}

		public DuplicateKeyException( final String message )
		{
			super( message );
		}
	}

	static synchronized void registerFeature( final Feature< ?, ?, ? > feature ) throws DuplicateKeyException
	{
		final String key = feature.getKey();
		if ( features.containsKey( key ) )
			throw new DuplicateKeyException( String.format( "feature key \"%s\" already exists", key ) );
		features.put( key, feature );
		featuresById.put( feature.getUniqueFeatureId(), feature );
	}

	/**
	 * @param key
	 * @return unique ID assigned to feature.
	 */
	public static synchronized int getUniqueFeatureId( final String key )
	{
		final Integer id = featureKeyIds.get( key );
		if ( id != null )
			return id;

		final int newId = idGenerator++;
		featureKeyIds.put( key, newId );
		return newId;
	}

	public static synchronized Feature< ?, ?, ? > getFeature( final String key )
	{
		return features.get( key );
	}

	public static synchronized Feature< ?, ?, ? > getFeature( final int uniqueId )
	{
		return featuresById.get( uniqueId );
	}

	private FeatureRegistry()
	{}
}
