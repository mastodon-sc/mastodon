package org.mastodon.revised.model.tag;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.mastodon.collection.RefObjectMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.ObjPropertyMapSerializer;

public class TagPropertyMapSerializer< O, T > extends ObjPropertyMapSerializer< O, T >
{

	private final TagPropertyMap< O, T > propertyMap;

	public TagPropertyMapSerializer( final TagPropertyMap< O, T > propertyMap )
	{
		super( propertyMap );
		this.propertyMap = propertyMap;
	}

	@Override
	public void readPropertyMap(
			final FileIdToObjectMap< O > idmap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		propertyMap.clear();
		super.readPropertyMap( idmap, ois );
		final RefObjectMap< O, T > pmap = propertyMap.getMap();
		for ( final O o : pmap.keySet() )
			propertyMap.store( o, pmap.get( o ) );
	}

	@Override
	public TagPropertyMap< O, T > getPropertyMap()
	{
		return propertyMap;
	}
}
