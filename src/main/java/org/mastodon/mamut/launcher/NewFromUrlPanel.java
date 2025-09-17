/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.launcher;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5URI;
import org.janelia.saalfeldlab.n5.bdv.N5ViewerCreator;
import org.janelia.saalfeldlab.n5.bdv.N5ViewerTreeCellRenderer;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.metadata.N5ViewerMultichannelMetadata;
import org.janelia.saalfeldlab.n5.ui.DatasetSelectorDialog;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.StorageFormat;
import org.janelia.saalfeldlab.n5.universe.metadata.MultiscaleMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5SingleScaleMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.axes.AxisUtils;
import org.janelia.saalfeldlab.n5.universe.metadata.axes.DefaultAxisMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.NgffSingleScaleAxesMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.OmeNgffMetadata;
import org.mastodon.mamut.io.loader.N5UniverseImgLoader;
import org.mastodon.mamut.io.loader.util.credentials.AWSCredentialsManager;
import org.mastodon.mamut.io.loader.util.credentials.AWSCredentialsTools;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import ij.IJ;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Pair;

class NewFromUrlPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final String DOCUMENTATION_STR = "Information on NGFF and remote imports";

	private static final String DOCUMENTATION_URL = "https://github.com/saalfeldlab/n5-ij?tab=readme-ov-file#open-hdf5n5zarrome-ngff";

	private static final String N5_DOCUMENTATION_LINK =
			"<html><a href='" + DOCUMENTATION_URL + "'>" + DOCUMENTATION_STR + "</html>";

	final JButton btnCreate;

	final JLabel labelInfo;

	File xmlFile;

	private String lastOpenedContainer = "";

	public NewFromUrlPanel( final String panelTitle, final String buttonTitle )
	{
		final GridBagLayout gblNewMastodonProjectPanel = new GridBagLayout();
		gblNewMastodonProjectPanel.columnWidths = new int[] { 0, 0 };
		gblNewMastodonProjectPanel.rowHeights = new int[] { 35, 70, 65, 0, 25, 45, 0, 0, 25, 0, 0, 0 };
		gblNewMastodonProjectPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblNewMastodonProjectPanel.rowWeights =
				new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gblNewMastodonProjectPanel );

		int row = 0;

		final JLabel lblNewMastodonProject = new JLabel( panelTitle );
		lblNewMastodonProject.setFont(
				lblNewMastodonProject.getFont().deriveFont( lblNewMastodonProject.getFont().getStyle() | Font.BOLD ) );
		final GridBagConstraints gbcLblNewMastodonProject = new GridBagConstraints();
		gbcLblNewMastodonProject.insets = new Insets( 5, 5, 5, 5 );
		gbcLblNewMastodonProject.gridx = 0;
		gbcLblNewMastodonProject.gridy = row++;
		add( lblNewMastodonProject, gbcLblNewMastodonProject );

		final JLabel lblFetchFromURL = new JLabel( "<html>Browse to local or remote OME-NGFF / Zarr / N5 / HDF5 file:</html>" );
		final GridBagConstraints gbcLblFetchFromURL = new GridBagConstraints();
		gbcLblFetchFromURL.insets = new Insets( 5, 5, 5, 5 );
		gbcLblFetchFromURL.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblFetchFromURL.gridx = 0;
		gbcLblFetchFromURL.gridwidth = 2;
		gbcLblFetchFromURL.fill = GridBagConstraints.HORIZONTAL;
		gbcLblFetchFromURL.gridy = row++;
		add( lblFetchFromURL, gbcLblFetchFromURL );

		final JButton btnBrowse = new JButton( "browse" );
		final GridBagConstraints gbcBtnBrowse = new GridBagConstraints();
		gbcBtnBrowse.insets = new Insets( 5, 5, 5, 5 );
		gbcBtnBrowse.anchor = GridBagConstraints.EAST;
		gbcBtnBrowse.gridx = 0;
		gbcBtnBrowse.gridy = row++;
		add( btnBrowse, gbcBtnBrowse );

		/*
		 * Wire listeners.
		 */
		btnBrowse.addActionListener( e -> openBrowserWindow() );

		labelInfo = new JLabel( "" );
		final GridBagConstraints gbcLabelInfo = new GridBagConstraints();
		gbcLabelInfo.insets = new Insets( 5, 5, 5, 5 );
		gbcLabelInfo.fill = GridBagConstraints.BOTH;
		gbcLabelInfo.gridx = 0;
		gbcLabelInfo.gridy = row++;
		add( labelInfo, gbcLabelInfo );

		btnCreate = new JButton( buttonTitle );
		btnCreate.setEnabled( false );
		final GridBagConstraints gbcBtnCreate = new GridBagConstraints();
		gbcBtnCreate.anchor = GridBagConstraints.EAST;
		gbcBtnCreate.gridx = 0;
		gbcBtnCreate.gridy = row++;
		add( btnCreate, gbcBtnCreate );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.insets = new Insets( 5, 5, 5, 5 );
		gbcSeparator.anchor = GridBagConstraints.SOUTH;
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = row++;
		gbcSeparator.weighty = 1.;
		gbcLblFetchFromURL.gridwidth = 2;
		gbcLblFetchFromURL.fill = GridBagConstraints.BOTH;
		add( new JSeparator(), gbcSeparator );

		final JLabel hyperlink = new JLabel( N5_DOCUMENTATION_LINK );
		hyperlink.setForeground( Color.BLUE.darker() );
		hyperlink.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		hyperlink.setToolTipText( DOCUMENTATION_URL );
		hyperlink.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( DOCUMENTATION_URL ) );
				}
				catch ( IOException | URISyntaxException e1 )
				{
					e1.printStackTrace();
				}
			}
		} );
		final GridBagConstraints gbcHyperlink = new GridBagConstraints();
		gbcHyperlink.insets = new Insets( 5, 5, 5, 5 );
		gbcHyperlink.anchor = GridBagConstraints.SOUTH;
		gbcHyperlink.gridx = 0;
		gbcHyperlink.gridy = row++;
		gbcHyperlink.gridwidth = 2;
		gbcHyperlink.fill = GridBagConstraints.HORIZONTAL;
		add( hyperlink, gbcHyperlink );
	}

	private void openBrowserWindow()
	{
		xmlFile = null;
		final ExecutorService exec = Executors.newFixedThreadPool( ij.Prefs.getThreads() );
		final Consumer< String > messenger = ( s ) -> SwingUtilities.invokeLater( () -> labelInfo.setText( "<html>" + s + "</html>" ) );
		final DatasetSelectorDialog dialog = new DatasetSelectorDialog(
				new N5ViewerReaderFun( messenger ),
				new N5Importer.N5BasePathFun(),
				lastOpenedContainer,
				N5ViewerCreator.n5vGroupParsers,
				N5ViewerCreator.n5vParsers );

		dialog.setLoaderExecutor( exec );
		dialog.setContainerPathUpdateCallback( x -> lastOpenedContainer = x );
		dialog.setTreeRenderer( new N5ViewerTreeCellRenderer( false ) );

		btnCreate.setEnabled( false );
		labelInfo.setText( "" );

		dialog.run( selection -> {

			// Disable UI when clicking ok.
			final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( SwingUtilities.getWindowAncestor( dialog.getJTree() ), null );
			disabler.disable();

			final N5Metadata metadata = selection.metadata.get( 0 );
			long[] dimensions = null;
			double[] scales = null;
			String[] axisLabels = null;
			String unit = null;
			AffineTransform3D calib = null;
			int x = 1;
			int y = 1;
			int z = 1;
			int c = 1;
			int t = 1;
			double sx = 1.0;
			double sy = 1.0;
			double sz = 1.0;
			if ( metadata instanceof OmeNgffMetadata )
			{
				final NgffSingleScaleAxesMetadata setup0Level0Metadata =
						( ( OmeNgffMetadata ) metadata ).multiscales[ 0 ].getChildrenMetadata()[ 0 ];
				dimensions = setup0Level0Metadata.getAttributes().getDimensions();
				scales = setup0Level0Metadata.getScale();
				axisLabels = setup0Level0Metadata.getAxisLabels();
				unit = setup0Level0Metadata.unit();
				if ( unit == null || unit.isEmpty() )
					unit = "pixel";
				calib = setup0Level0Metadata.spatialTransform3d();
			}
			else if ( metadata instanceof N5ViewerMultichannelMetadata )
			{
				c = ( ( N5ViewerMultichannelMetadata ) metadata ).getChildrenMetadata().length;
				final MultiscaleMetadata< ? > setup0Metadata =
						( ( N5ViewerMultichannelMetadata ) metadata ).getChildrenMetadata()[ 0 ];
				final N5SingleScaleMetadata setup0Level0Metadata = ( N5SingleScaleMetadata ) setup0Metadata.getChildrenMetadata()[ 0 ];
				dimensions = setup0Level0Metadata.getAttributes().getDimensions();
				scales = setup0Level0Metadata.getPixelResolution();
				final DefaultAxisMetadata axes = AxisUtils.defaultN5ViewerAxes( ( setup0Level0Metadata ) );
				axisLabels = axes.getAxisLabels();
				unit = setup0Level0Metadata.unit();
				if ( unit == null || unit.isEmpty() )
					unit = "pixel";
				calib = setup0Level0Metadata.spatialTransform3d();
			}
			else
			{
				messenger.accept( "The metadata is not supported: " + metadata.getName() );
				return;
			}
			final AffineTransform3D calibFinal = calib.copy();

			for ( int i = 0; i < axisLabels.length; i++ )
			{
				if ( axisLabels[ i ].toLowerCase().equals( "x" ) )
				{
					x = ( int ) dimensions[ i ];
					sx = scales[ i ];
				}
				else if ( axisLabels[ i ].toLowerCase().equals( "y" ) )
				{
					y = ( int ) dimensions[ i ];
					sy = scales[ i ];
				}
				else if ( axisLabels[ i ].toLowerCase().equals( "z" ) )
				{
					z = ( int ) dimensions[ i ];
					sz = scales[ i ];
				}
				else if ( axisLabels[ i ].toLowerCase().equals( "c" ) )
					c = ( int ) dimensions[ i ];
				else if ( axisLabels[ i ].toLowerCase().equals( "t" ) )
					t = ( int ) dimensions[ i ];
			}
			final Dimensions imageSize = new FinalDimensions( x, y, z );
			final TimePoints timepoints = new TimePoints(
					IntStream.range( 0, t ).mapToObj( TimePoint::new ).collect( Collectors.toList() ) );
			final Map< Integer, BasicViewSetup > setups = new HashMap<>();
			final VoxelDimensions voxelDimensions = new FinalVoxelDimensions( unit, sx, sy, sz );
			for ( int i = 0; i < c; i++ )
				setups.put( i, new BasicViewSetup( i, String.format( "channel %d", i ), imageSize, voxelDimensions ) );
			final BasicImgLoader imgLoader =
					new N5UniverseImgLoader( selection.n5.getURI().toString(), metadata.getPath(), null );
			final SequenceDescriptionMinimal sequenceDescription =
					new SequenceDescriptionMinimal( timepoints, setups, imgLoader, null );
			final ViewRegistrations viewRegistrations = new ViewRegistrations(
					IntStream.range( 0, t )
							.boxed()
							.flatMap( tp -> IntStream.range( 0, setups.size() )
									.mapToObj( setup -> new ViewRegistration( tp, setup, calibFinal ) ) )
							.collect( Collectors.toList() ) );

			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle( "Save BigDataViewer XML File" );
			final FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter( "XML Files", "xml" );
			fileChooser.setFileFilter( xmlFilter );
			final String currDir = IJ.getDirectory( "current" );
			if ( currDir != null )
				fileChooser.setCurrentDirectory( new File( currDir ) );
			final int userSelection = fileChooser.showSaveDialog( null );
			if ( userSelection == JFileChooser.APPROVE_OPTION )
			{
				File fileToSave = fileChooser.getSelectedFile();

				// Ensure the file has the .xml extension
				if ( !fileToSave.getAbsolutePath().endsWith( ".xml" ) )
				{
					fileToSave = new File( fileToSave.getAbsolutePath() + ".xml" );
				}

				final SpimDataMinimal spimData =
						new SpimDataMinimal( fileToSave.getParentFile(), sequenceDescription, viewRegistrations );

				try
				{
					new XmlIoSpimDataMinimal().save( spimData, fileToSave.getAbsolutePath() );
				}
				catch ( final SpimDataException e )
				{
					e.printStackTrace();
				}

				if ( checkBDVFile( fileToSave ) )
				{
					xmlFile = fileToSave;
					btnCreate.setEnabled( true );
				}
			}
		} );
		SwingUtilities.getWindowAncestor( dialog.getJTree() ).setLocationRelativeTo( this );
	}

	private boolean checkBDVFile( final File file )
	{
		try
		{
			final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( file.getAbsolutePath() );
			final String str = LauncherUtil.buildInfoString( spimData );
			labelInfo.setText( str );
			return true;
		}
		catch ( final SpimDataException | RuntimeException e )
		{
			labelInfo.setText( "<html>Invalid BDV xml file.<p>" + e.getMessage() + "</html>" );
			return false;
		}
	}

	private static class N5ViewerReaderFun implements Function< String, N5Reader >
	{

		private final Consumer< String > messenger;

		public N5ViewerReaderFun( final Consumer< String > messager )
		{
			this.messenger = messager;
		}

		@Override
		public N5Reader apply( final String n5UriOrPath )
		{
			if ( n5UriOrPath == null || n5UriOrPath.isEmpty() )
				return null;

			String rootPath = null;
			if ( n5UriOrPath.contains( "?" ) )
			{
				// need to strip off storage format for n5uri to correctly
				// remove query;
				final Pair< StorageFormat, URI > fmtUri = StorageFormat.parseUri( n5UriOrPath );
				final StorageFormat format = fmtUri.getA();

				final N5URI n5uri = new N5URI( URI.create( fmtUri.getB().toString() ) );
				// add the format prefix back if it was present
				rootPath = format == null ? n5uri.getContainerPath() : format.toString().toLowerCase() + ":" + n5uri.getContainerPath();
			}

			if ( rootPath == null )
				rootPath = upToLastExtension( n5UriOrPath );

			N5Factory factory = new N5Factory().cacheAttributes( true );
			try
			{
				return factory.openReader( rootPath );
			}
			catch ( final Exception e )
			{}
			// Use credentials
			if ( AWSCredentialsManager.getInstance().getCredentials() == null )
				AWSCredentialsManager.getInstance().setCredentials( AWSCredentialsTools.getBasicAWSCredentials() );
			factory = factory.s3UseCredentials( AWSCredentialsManager.getInstance().getCredentials() );
			try
			{
				return factory.openReader( rootPath );
			}
			catch ( final N5Exception e )
			{
				AWSCredentialsManager.getInstance().setCredentials( null );
				messenger.accept( e.getMessage() );
			}
			catch ( final Exception e )
			{
				messenger.accept( e.getMessage() );
			}
			return null;
		}
	}

	private static String upToLastExtension( final String path )
	{

		final int i = path.lastIndexOf( '.' );
		if ( i >= 0 )
		{
			final int j = path.substring( i ).indexOf( '/' );
			if ( j >= 0 )
				return path.substring( 0, i + j );
			else
				return path;
		}
		else
			return path;
	}
}
