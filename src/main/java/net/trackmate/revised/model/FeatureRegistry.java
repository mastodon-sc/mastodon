package net.trackmate.revised.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Assign unique IDs to features.
 *
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class FeatureRegistry
{
	private static int idGenerator = 0;

	private static Map< String, Integer > vertexFeatureKeyIds = new HashMap<>();

	private static Map< String, VertexFeature< ?, ?, ? > > vertexFeatures = new HashMap<>();

	public static class DuplicateKeyException extends RuntimeException
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

	protected static synchronized void registerVertexFeature( final VertexFeature< ?, ?, ? > feature ) throws DuplicateKeyException
	{
		final String key = feature.getKey();
		if ( vertexFeatures.containsKey( key ) )
			throw new DuplicateKeyException( String.format( "vertex feature key \"%s\" already exists", key ) );
		vertexFeatures.put( key, feature );
	}

	/**
	 * @param key
	 * @return unique ID assigned to feature.
	 */
	public static synchronized int getUniqueVertexFeatureId( final String key )
	{
		final Integer id = vertexFeatureKeyIds.get( key );
		if ( id != null )
			return id;

		final int newId = idGenerator++;
		vertexFeatureKeyIds.put( key, newId );
		return newId;
	}

	private FeatureRegistry()
	{};
}
