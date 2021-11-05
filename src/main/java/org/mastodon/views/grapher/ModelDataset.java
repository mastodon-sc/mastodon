package org.mastodon.views.grapher;

import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.xy.XYDataset;
import org.mastodon.feature.FeatureProjection;

public class ModelDataset< O > extends AbstractDataset implements XYDataset
{

	private static final long serialVersionUID = 1L;

	private final List< O > items;

	private final List< FeatureProjection< O > > yProjections;

	private final FeatureProjection< O > xProjection;

	public ModelDataset(
			final List< O > items,
			final FeatureProjection< O > xProjection,
			final List< FeatureProjection< O > > yProjections )
	{
		this.items = items;
		this.xProjection = xProjection;
		this.yProjections = yProjections;
	}

	@Override
	public int getSeriesCount()
	{
		return yProjections.size();
	}

	@Override
	public String getSeriesKey( final int series )
	{
		return yProjections.get( series ).getKey().toString();
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public int indexOf( final Comparable seriesKey )
	{
		for ( int i = 0; i < yProjections.size(); i++ )
		{
			if ( getSeriesKey( i ).equals( seriesKey ) )
				return i;
		}
		return -1;
	}

	@Override
	public DomainOrder getDomainOrder()
	{
		return DomainOrder.NONE;
	}

	@Override
	public int getItemCount( final int series )
	{
		return items.size();
	}

	@Override
	public Number getX( final int series, final int item )
	{
		return getXValue( series, item );
	}

	@Override
	public double getXValue( final int series, final int item )
	{
		return xProjection.value( items.get( item ) );
	}

	@Override
	public Number getY( final int series, final int item )
	{
		return getYValue( series, item );
	}

	@Override
	public double getYValue( final int series, final int item )
	{
		return yProjections.get( series ).value( items.get( item ) );
	}
}
