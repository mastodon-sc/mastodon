package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.SpotGaussFilteredIntensityFeature.Spec;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class SpotGaussFilteredIntensityFeatureSerializer implements FeatureSerializer< SpotGaussFilteredIntensityFeature, Spot >
{

	@Override
	public Spec getFeatureSpec()
	{
		return SpotGaussFilteredIntensityFeature.SPEC;
	}

	@Override
	public void serialize( final SpotGaussFilteredIntensityFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final int nSources = feature.means.size();
		oos.writeInt( nSources );
		for ( int i = 0; i < nSources; i++ )
		{
			new DoublePropertyMapSerializer<>( feature.means.get( i ) ).writePropertyMap( idmap, oos );
			new DoublePropertyMapSerializer<>( feature.stds.get( i ) ).writePropertyMap( idmap, oos );
		}
	}

	@Override
	public SpotGaussFilteredIntensityFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final int nSources = ois.readInt();
		final List< DoublePropertyMap< Spot > > means = new ArrayList<>( nSources );
		final List< DoublePropertyMap< Spot > > stds = new ArrayList<>( nSources );
		for ( int i = 0; i < nSources; i++ )
		{
			final DoublePropertyMap< Spot > meanMap = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( meanMap ).readPropertyMap( idmap, ois );
			means.add( meanMap );

			final DoublePropertyMap< Spot > stdMap = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( stdMap ).readPropertyMap( idmap, ois );
			stds.add( stdMap );
		}
		return new SpotGaussFilteredIntensityFeature( means, stds );
	}
}
