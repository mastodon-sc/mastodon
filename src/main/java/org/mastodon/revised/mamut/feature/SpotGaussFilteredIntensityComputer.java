package org.mastodon.revised.mamut.feature;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.feature.DoubleArrayFeature;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.scijava.plugin.Plugin;

import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotFeatureComputer.class, name = "Spot gaussian-filtered intensity" )
public class SpotGaussFilteredIntensityComputer implements SpotFeatureComputer
{

	public static final String KEY = "Spot gaussian-filtered intensity";

	/** Convert from min radius to sigma by dividing radius by: */
	private static final double SIGMA_FACTOR = 2.;

	private static final String HELP_STRING = "<html>"
			+ "Computes the average intensity and its standard deviation inside spots "
			+ "over all sources of the dataset. "
			+ "<p>"
			+ "The average is calculated by a weighted mean over the pixels of the spot, "
			+ "weighted by a gaussian centered in the spot and with a sigma value equal "
			+ "to the minimal radius of the ellipsoid divided by " + SIGMA_FACTOR + "."
			+ "</html>";

	private SharedBigDataViewerData bdvData;

	private boolean[] processSource;

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public void setSharedBigDataViewerData( final SharedBigDataViewerData bdvData )
	{
		this.bdvData = bdvData;
		this.processSource = new boolean[ bdvData.getSources().size() ];
		Arrays.fill( processSource, true );
	}

	@Override
	public DoubleArrayFeature< Spot > compute( final Model model )
	{
		if ( null == bdvData )
			return null;

		// Calculation are made on resolution level 0.
		final int level = 0;
		// Covariance holder.
		final double[][] cov = new double[ 3 ][ 3 ];
		// Affine transform holder.
		final AffineTransform3D transform = new AffineTransform3D();
		// Physical calibration holder.
		final double[] calibration = new double[ 3 ];
		// Half-kernel holder.
		final double[][] kernels = new double[ 3 ][];
		// Half-kernel size holder.
		final int[] halfkernelsizes = new int[ 3 ];
		// Spot center position holder in image coords.
		final double[] pos = new double[ 3 ];
		// Spot center holder in image coords.
		final RealPoint center = RealPoint.wrap( pos );
		// Spot center position holder in integer image coords.
		final long[] p = new long[ 3 ];

		// Holder for property map.
		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		int nSourcesToProcess = 0;
		for ( final boolean pSource : processSource )
			if ( pSource )
				nSourcesToProcess++;
		final List< DoublePropertyMap< Spot > > pms = new ArrayList<>( nSourcesToProcess * 2 );
		final List< String > names = new ArrayList<>( nSourcesToProcess * 2 );

		final SpatioTemporalIndex< Spot > index = model.getSpatioTemporalIndex();
		final int numTimepoints = bdvData.getNumTimepoints();
		final int nSources = sources.size();
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			if ( !processSource[ iSource ] )
				continue;

			final PoolCollectionWrapper< Spot > vertices = model.getGraph().vertices();
			final DoublePropertyMap< Spot > pmMean = new DoublePropertyMap<>( vertices, Double.NaN );
			pms.add( pmMean );
			final String nameMean = "Average ch " + iSource;
			names.add( nameMean );
			final DoublePropertyMap< Spot > pmStd = new DoublePropertyMap<>( vertices, Double.NaN );
			pms.add( pmStd );
			final String nameStd = "Std ch " + iSource;
			names.add( nameStd );

			final Source< ? > source = sources.get( iSource ).getSpimSource();
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{
				final SpatialIndex< Spot > spatialIndex = index.getSpatialIndex( timepoint );
				source.getSourceTransform( timepoint, level, transform );
				for ( int d = 0; d < calibration.length; d++ )
					calibration[ d ] = Affine3DHelpers.extractScale( transform, d );

				@SuppressWarnings( "unchecked" )
				final RandomAccessibleInterval< RealType< ? > > rai = ( RandomAccessibleInterval< RealType< ? > > ) source.getSource( timepoint, level );
				final RandomAccess< RealType< ? > > ra = rai.randomAccess( rai );

				for ( final Spot spot : spatialIndex )
				{
					// Spot location in pixel units.
					transform.applyInverse( center, spot );
					for ( int d = 0; d < pos.length; d++ )
						p[ d ] = Math.round( pos[ d ] );

					// Compute kernels.
					final double minRadius = minRadius( spot, cov );
					final double sigma = minRadius / SIGMA_FACTOR; // um
					for ( int d = 0; d < 3; d++ )
					{
						final double s = sigma / calibration[ d ];
						halfkernelsizes[ d ] = Math.max( 2, ( int ) ( SIGMA_FACTOR * s + 0.5 ) + 1 );
						kernels[ d ] = halfkernel( s, pos[ d ] - p[ d ], halfkernelsizes[ d ] );
					}

					// Unsubtle loops.
					final long minX = Math.max( rai.min( 0 ), p[ 0 ] - halfkernelsizes[ 0 ] + 1 );
					final long maxX = Math.min( rai.max( 0 ), p[ 0 ] + halfkernelsizes[ 0 ] - 1 );
					final long minY = Math.max( rai.min( 1 ), p[ 1 ] - halfkernelsizes[ 1 ] + 1 );
					final long maxY = Math.min( rai.max( 1 ), p[ 1 ] + halfkernelsizes[ 1 ] - 1 );
					final long minZ = Math.max( rai.min( 2 ), p[ 2 ] - halfkernelsizes[ 2 ] + 1 );
					final long maxZ = Math.min( rai.max( 2 ), p[ 2 ] + halfkernelsizes[ 2 ] - 1 );

					/*
					 * Compute running mean & std.
					 * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Weighted_incremental_algorithm
					 */
					double weightedMean = 0.;
					double weightedSum = 0.;
					double S = 0.;

					for ( long z = minZ; z <= maxZ; z++ )
					{
						ra.setPosition( z, 2 );
						final int iz = ( int ) ( z - minZ );
						final double wz = kernels[ 2 ][ iz ];
						for ( long y = minY; y <= maxY; y++ )
						{
							ra.setPosition( y, 1 );
							final int iy = ( int ) ( y - minY );
							final double wy = kernels[ 1 ][ iy ];
							for ( long x = minX; x <= maxX; x++ )
							{
								ra.setPosition( x, 0 );
								final int ix = ( int ) ( x - minX );
								final double wx = kernels[ 0 ][ ix ];
								final double val = ra.get().getRealDouble();
								final double weight = wx * wy * wz;

								weightedSum += weight;
								final double oldWeightedMean = weightedMean;
								weightedMean = oldWeightedMean + ( weight / weightedSum ) * ( val - oldWeightedMean );
								S = S + weight * ( val - oldWeightedMean ) * ( val - weightedMean );
							}
						}
					}

					final double variance = S / weightedSum;
					pmMean.set( spot, weightedMean );
					pmStd.set( spot, Math.sqrt( variance ) );
				}
			}
		}
		return new DoubleArrayFeature<>( KEY, Spot.class, pms, names );
	}

	private static final double[] halfkernel( final double sigma, final double offset, final int size )
	{
		final double two_sq_sigma = 2 * sigma * sigma;
		final double[] kernel = new double[ 2 * size - 1 ];

		for ( int i = 0; i < kernel.length; ++i )
		{
			final double x = i - size + 1 - offset;
			kernel[ i ] = Math.exp( -( x * x ) / two_sq_sigma );
		}

		double sum = 0.;
		for ( int i = 0; i < kernel.length; i++ )
			sum += kernel[ i ];

		for ( int i = 0; i < kernel.length; ++i )
			kernel[ i ] /= sum;

		return kernel;
	}

	private static final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	private static final double minRadius( final Spot spot, final double[][] cov )
	{
		// Best radius is smallest radius of ellipse.
		spot.getCovariance( cov );
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double minEig = Double.POSITIVE_INFINITY;
		for ( int k = 0; k < eigVals.length; k++ )
			minEig = Math.min( minEig, eigVals[ k ] );
		final double radius = Math.sqrt( minEig );
		return radius;
	}

	@Override
	public DoubleArrayFeature< Spot > deserialize( final File file, final Model support, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ), 1024 * 1024 ) ))
		{
			// NUMBER OF ELEMENTS
			final int nSources = ois.readInt();
			final List< DoublePropertyMap< Spot > > propertyMaps = new ArrayList<>();
			final List< String > names = new ArrayList<>();
			for ( int i = 0; i < nSources; i++ )
			{
				// NAME OF ENTRIES
				final String name = ois.readUTF();
				names.add( name );
				// NUMBER OF ENTRIES and ENTRIES
				final PoolCollectionWrapper< Spot > vertices = support.getGraph().vertices();
				final DoublePropertyMap< Spot > pm = new DoublePropertyMap<>( vertices, Double.NaN, vertices.size() );
				@SuppressWarnings( "unchecked" )
				final FileIdToObjectMap< Spot > idToSpotMap = ( FileIdToObjectMap< Spot > ) fileIdToGraphMap.vertices();
				final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( pm );
				serializer.readPropertyMap( idToSpotMap, ois );
				propertyMaps.add( pm );
			}
			return new DoubleArrayFeature<>( KEY, Spot.class, propertyMaps, names );
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getHelpString()
	{
		return HELP_STRING;
	}

	@Override
	public JComponent getConfigPanel()
	{
		if ( null == bdvData )
			return null;

		final int nSources = bdvData.getSources().size();
		final JPanel configPanel = new JPanel();
		configPanel.setLayout( new GridBagLayout() );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 0;
		c.weightx = 1.;
		c.fill = GridBagConstraints.BOTH;

		final JLabel lbl = new JLabel( "Data to analyze:" );
		configPanel.add( lbl, c );
		c.gridy++;

		configPanel.add( new JSeparator(), c);
		c.gridy++;

		for ( int i = 0; i < nSources; i++ )
		{
			final int currentSource = i;
			final String str = "ch " + currentSource + ": " + bdvData.getSources().get( currentSource ).getSpimSource().getName();
			final JCheckBox processBox = new JCheckBox( str, processSource[ currentSource ] );
			processBox.addItemListener( ( e ) -> processSource[ currentSource ] = processBox.isSelected() );
			configPanel.add( processBox, c );
			c.gridy++;
		}

		return configPanel;

	}
}
