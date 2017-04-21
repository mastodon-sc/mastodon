package org.mastodon.revised.model.mamut;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleArrayAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.properties.Property;
import org.mastodon.revised.model.AbstractSpotPool;

public class SpotPool extends AbstractSpotPool< Spot, Link, ByteMappedElement, ModelGraph >
{
	public static class SpotLayout extends AbstractSpotLayout
	{
		public SpotLayout()
		{
			super( 3 );
		}

		final DoubleArrayField covariance = doubleArrayField( 6 );
		final DoubleField boundingSphereRadiusSqu = doubleField();
	}

	public static final SpotLayout layout = new SpotLayout();

	final DoubleArrayAttribute< Spot > covariance = new DoubleArrayAttribute<>( layout.covariance, this );

	final DoubleAttribute< Spot > boundingSphereRadiusSqu = new DoubleAttribute<>( layout.boundingSphereRadiusSqu, this );

	final ObjPropertyMap< Spot, String > label = new ObjPropertyMap<>( this );

	SpotPool( final int initialCapacity )
	{
		super( initialCapacity, layout, Spot.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
	}

	@Override
	protected Spot createEmptyRef()
	{
		return new Spot( this );
	}

	public final Property< Spot > covarianceProperty()
	{
		return covariance;
	}

	public final Property< Spot > boundingSphereRadiusSquProperty()
	{
		return boundingSphereRadiusSqu;
	}

	public final Property< Spot > positionProperty()
	{
		return position;
	}

	public final Property< Spot > labelProperty()
	{
		return label;
	}
}
