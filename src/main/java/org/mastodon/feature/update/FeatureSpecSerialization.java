package org.mastodon.feature.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;

/**
 * Only used to de/serialize a {@link GraphUpdateStack}.
 *
 * @author Jean-Yves Tinevez
 *
 */
class FeatureSpecSerialization
{

	static void serialize(
			final FeatureSpec< ?, ? > featureSpec,
			final ObjectOutputStream oos )
			throws IOException
	{
		oos.writeUTF( featureSpec.getKey() );
		oos.writeUTF( featureSpec.getInfo() );
		oos.writeObject( featureSpec.getFeatureClass() );
		oos.writeObject( featureSpec.getTargetClass() );
		oos.writeObject( featureSpec.getMultiplicity() );
		oos.writeInt( featureSpec.getProjectionSpecs().size() );
		for ( final FeatureProjectionSpec projectionSpec : featureSpec.getProjectionSpecs() )
			serializeFeatureProjectionSpec( projectionSpec, oos );
	}

	private static void serializeFeatureProjectionSpec(
			final FeatureProjectionSpec projectionSpec,
			final ObjectOutputStream oos )
			throws IOException
	{
		oos.writeUTF( projectionSpec.projectionName );
		oos.writeObject( projectionSpec.projectionDimension );
	}

	static FeatureSpec< ?, ? > deserialize(
			final ObjectInputStream ois )
			throws IOException, ClassNotFoundException
	{
		final String key = ois.readUTF();
		final String info = ois.readUTF();
		final Class< ? > featureClass = ( Class< ? > ) ois.readObject();
		final Class< ? > targetClass = ( Class< ? > ) ois.readObject();
		final Multiplicity multiplicity = ( Multiplicity ) ois.readObject();
		final int nProjectionSpecs = ois.readInt();
		final FeatureProjectionSpec[] projectionSpecs = new FeatureProjectionSpec[ nProjectionSpecs ];
		for ( int i = 0; i < nProjectionSpecs; i++ )
			projectionSpecs[ i ] = deserializeFeatureProjectionSpec( ois );

		@SuppressWarnings( { "rawtypes", "unchecked" } )
		final FeatureSpec< ?, ? > fs = new FeatureSpec(
				key,
				info,
				featureClass,
				targetClass,
				multiplicity,
				projectionSpecs )
		{};
		return fs;
	}

	private static FeatureProjectionSpec deserializeFeatureProjectionSpec(
			final ObjectInputStream ois )
			throws ClassNotFoundException, IOException
	{
		final String projectionName = ois.readUTF();
		final Dimension projectionDimension = ( Dimension ) ois.readObject();
		return new FeatureProjectionSpec( projectionName, projectionDimension );
	}
}
