/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.io.importer.tgmm;

import static mpicbg.spim.data.XmlHelpers.getDoubleArrayAttribute;
import static mpicbg.spim.data.XmlHelpers.getDoubleAttribute;
import static mpicbg.spim.data.XmlHelpers.getIntAttribute;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefMaps;
import org.mastodon.graph.Graph;
import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

import Jama.Matrix;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.TimePointsPattern;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

public class TgmmImporter extends ModelImporter
{
	/**
	 * Import a set of XML file generated by the TGMM algorithm in a model.
	 *
	 * @param tgmmFileNameFormat
	 *            a string that can be parsed by
	 *            {@link String#format(String, Object...)} to generate target
	 *            TGMM filenames. Example:
	 *            {@code /Volumes/Data/TGMM_TL0-528_xmls_curated/GMEMfinalResult_frame%04d.xml}
	 * @param timepointsToRead
	 *            the desired time-points to read.
	 * @param timepointToIndex
	 *            mapping between time-point and index in the file name.
	 * @param viewRegistrations
	 *            the {@link ViewRegistrations} to position the tracks in the
	 *            proper coordinate system.
	 * @param setupID
	 *            the setup ID of the desired transform in the
	 *            ViewRegistrations.
	 * @param nSigmas
	 *            the number of sigmas to convert a TGMM probability into
	 *            ellipsoids semi-axis lengths.
	 * @param model
	 *            the {@link Model} to update with the read tracks.
	 *
	 * @throws JDOMException
	 *             when errors occur in parsing.
	 * @throws IOException
	 *             when an I/O error prevents a document from being fully
	 *             parsed.
	 */
	public static void read(
			final String tgmmFileNameFormat,
			final TimePoints timepointsToRead,
			final Map< TimePoint, Integer > timepointToIndex,
			final ViewRegistrations viewRegistrations,
			final int setupID,
			final double nSigmas,
			final Model model )
			throws JDOMException, IOException
	{
		new TgmmImporter(
				tgmmFileNameFormat,
				timepointsToRead,
				timepointToIndex,
				viewRegistrations,
				setupID,
				nSigmas,
				null,
				model );
	}

	public static void read(
			final String tgmmFileNameFormat,
			final TimePoints timepointsToRead,
			final Map< TimePoint, Integer > timepointToIndex,
			final ViewRegistrations viewRegistrations,
			final int setupID,
			final double nSigmas,
			final double[][] useThisCovariance,
			final Model model )
			throws JDOMException, IOException
	{
		new TgmmImporter(
				tgmmFileNameFormat,
				timepointsToRead,
				timepointToIndex,
				viewRegistrations,
				setupID,
				nSigmas,
				useThisCovariance,
				model );
	}

	private TgmmImporter(
			final String tgmmFileNameFormat,
			final TimePoints timepointsToRead,
			final Map< TimePoint, Integer > timepointToIndex,
			final ViewRegistrations viewRegistrations,
			final int setupID,
			final double nSigmas,
			final double[][] useThisCovariance,
			final Model model )
			throws JDOMException, IOException
	{
		super( model );
		startImport();

		final Graph< Spot, Link > graph = model.getGraph();
		final Spot spot = graph.vertexRef();
		final Spot parent = graph.vertexRef();
		final Spot tmp = graph.vertexRef();
		final Link edge = graph.edgeRef();

		IntRefMap< Spot > idToSpot = RefMaps.createIntRefMap( graph.vertices(), -1, 2000 );
		IntRefMap< Spot > previousIdToSpot = RefMaps.createIntRefMap( graph.vertices(), -1, 2000 );

		for ( final TimePoint timepoint : timepointsToRead.getTimePointsOrdered() )
		{
			final int timepointId = timepoint.getId();
			final int timepointIndex = timepointToIndex.get( timepoint );
			final AffineTransform3D transform =
					viewRegistrations.getViewRegistration( timepointId, setupID ).getModel();
			final String tgmmFileName = String.format( tgmmFileNameFormat, timepointId );
			System.out.println( tgmmFileName );

			final SAXBuilder sax = new SAXBuilder();
			final Document doc = sax.build( tgmmFileName );
			final Element root = doc.getRootElement();

			final List< Element > gaussianMixtureModels = root.getChildren( "GaussianMixtureModel" );
			for ( final Element elem : gaussianMixtureModels )
			{
				try
				{
					final double nu = getDoubleAttribute( elem, "nu" );
					final double[] m = getDoubleArrayAttribute( elem, "m" );
					final double[] W = getDoubleArrayAttribute( elem, "W" );
					final int id = getIntAttribute( elem, "id" );
					//					final int lineage = getIntAttribute( elem, "lineage" );
					final int parentId = getIntAttribute( elem, "parent" );

					final double[][] S = ( useThisCovariance != null ) ? useThisCovariance
							: getCovariance( transform, nu / ( nSigmas * nSigmas ), W );
					graph.addVertex( spot ).init(
							timepointIndex,
							getPosition( transform, m ),
							S );
					idToSpot.put( id, spot, tmp );

					if ( ( parentId >= 0 ) && ( previousIdToSpot.get( parentId, parent ) != null ) )
						graph.addEdge( parent, spot, edge ).init();
				}
				catch ( final NumberFormatException e )
				{
					System.out.println( "- Ignoring " + elem + ": " + e.getMessage() );
				}
			}

			previousIdToSpot.clear();
			final IntRefMap< Spot > m = previousIdToSpot;
			previousIdToSpot = idToSpot;
			idToSpot = m;
		}

		graph.releaseRef( spot );
		graph.releaseRef( parent );
		graph.releaseRef( tmp );
		graph.releaseRef( edge );

		finishImport();
	}

	private static double[][] getCovariance( final AffineTransform3D transform, final double nu, final double[] W )
	{
		final double[] wtmp = new double[ 9 ];
		LinAlgHelpers.scale( W, nu, wtmp );

		final Matrix precMat = new Matrix( wtmp, 3 );
		final Matrix covMat = precMat.inverse();
		final double[][] S = covMat.getArray();

		final double[][] T = new double[ 3 ][ 3 ];
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = transform.get( r, c );

		final double[][] TS = new double[ 3 ][ 3 ];
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		return S;
	}

	private static double[] getPosition( final AffineTransform3D transform, final double[] m )
	{
		final double[] pos = new double[ 3 ];
		transform.apply( m, pos );
		return pos;
	}

	public static Map< TimePoint, Integer > getTimepointToIndex( final AbstractSpimData< ? > spimData )
	{
		final Map< TimePoint, Integer > timepointToIndex = new HashMap<>();
		int i = 0;
		for ( final TimePoint tp : spimData.getSequenceDescription().getTimePoints().getTimePointsOrdered() )
			timepointToIndex.put( tp, i++ );
		return timepointToIndex;
	}

	public static void main( final String[] args ) throws ParseException, SpimDataException, JDOMException, IOException
	{
		//		final String tgmmFiles = "/Users/pietzsch/Downloads/data/TGMMruns_testRunToCheckOutput/XML_finalResult_lht/GMEMfinalResult_frame%04d.xml";
		//		final String bdvFile = "/Users/pietzsch/TGMM/data/tifs/datasethdf5.xml";
		//		final int setupID = 0;
		//		final String target = "/Users/pietzsch/TGMM/data/tifs/model_revised.raw";
		//		final TimePoints timepoints = new TimePointsPattern( "0-30" );

		final String tgmmFiles = "/Volumes/Data/TGMM_TL0-528_xmls_curated/GMEMfinalResult_frame%04d.xml";
		final String bdvFile = "/Volumes/Data/BDV_MVD_5v_final.xml";
		final int setupID = 1;
		final String target = "/Volumes/Data/mamutproject";
		final TimePoints timepoints = new TimePointsPattern( "1-528" );

		System.out.println( "Started reading TGMM files." );
		System.out.println( " - TGMM files:\t\t" + tgmmFiles );
		System.out.println( " - SPIM data file:\t" + bdvFile );
		System.out.println( " - Setup ID:\t\t" + setupID );
		System.out.println( " - Time-points to import:\t" + timepoints );
		System.out.println( " - Save to:\t\t" + target );

		final long start = System.currentTimeMillis();

		System.out.print( "\nReading view registrations." );
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( bdvFile );
		final Map< TimePoint, Integer > timepointToIndex = getTimepointToIndex( spimData );
		final ViewRegistrations viewRegistrations = spimData.getViewRegistrations();
		System.out.println( " Done." );

		System.out.println( "Reading the model." );
		final String timeUnits = "frame";
		final String spaceUnits;
		if ( spimData.getSequenceDescription().getViewSetupsOrdered().isEmpty() )
			spaceUnits = "pixel";
		else
			spaceUnits = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().unit();
		final Model model = new Model( spaceUnits, timeUnits );
		read( tgmmFiles, timepoints, timepointToIndex, viewRegistrations, setupID, 2, model );
		final long end = System.currentTimeMillis();
		System.out.println( "Done  in " + ( end - start ) / 1000d + " s." );

		System.out.println( "\nExporting to " + target );

		final MamutProject project = new MamutProject( new File( target ), new File( bdvFile ) );
		final MamutProject.ProjectWriter writer = project.openForWriting();
		model.saveRaw( writer );
		writer.close();
		final long end2 = System.currentTimeMillis();
		System.out.println( "Exporting done in " + ( end2 - end ) / 1000d + " s." );
	}
}
