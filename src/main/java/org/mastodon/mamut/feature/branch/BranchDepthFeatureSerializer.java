package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Plugin( type = FeatureSerializer.class )
public class BranchDepthFeatureSerializer implements BranchFeatureSerializer< BranchDepthFeature, BranchSpot, Spot >
{

	@Override
	public BranchDepthFeature.Spec getFeatureSpec()
	{
		return BranchDepthFeature.SPEC;
	}

	@Override
	public BranchDepthFeature deserialize( final FileIdToObjectMap< Spot > idmap, final ObjectInputStream ois,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws ClassNotFoundException, IOException
	{
		// Read the map link -> val.
		final IntPropertyMap< Spot > linkMap = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( linkMap );
		propertyMapSerializer.readPropertyMap( idmap, ois );

		// Map to branch-link -> val.
		return new BranchDepthFeature( BranchFeatureSerializer.mapToBranchSpotMap( linkMap, branchGraph ) );
	}

	@Override
	public void serialize( final BranchDepthFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws IOException
	{
		final IntPropertyMap< Spot > linkMap = BranchFeatureSerializer.branchSpotMapToMap( feature.map, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( linkMap );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}

}
