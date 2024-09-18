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
package org.mastodon.mamut.launcher;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.janelia.saalfeldlab.n5.bdv.N5ViewerCreator;
import org.janelia.saalfeldlab.n5.bdv.N5ViewerTreeCellRenderer;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ui.DatasetSelectorDialog;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.NgffSingleScaleAxesMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.v04.OmeNgffMetadata;
import org.mastodon.mamut.io.loader.N5UniverseImgLoader;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
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

class NewFromUrlPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

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

		final JLabel lblFetchFromURL = new JLabel( "Browse S3-compatible URL:" );
		final GridBagConstraints gbcLblFetchFromURL = new GridBagConstraints();
		gbcLblFetchFromURL.insets = new Insets( 5, 5, 5, 5 );
		gbcLblFetchFromURL.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblFetchFromURL.gridx = 0;
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
		btnBrowse.addActionListener( l -> {
			xmlFile = null;
			final ExecutorService exec = Executors.newFixedThreadPool( ij.Prefs.getThreads() );
			final DatasetSelectorDialog dialog = new DatasetSelectorDialog(
					new N5Importer.N5ViewerReaderFun(),
					new N5Importer.N5BasePathFun(),
					lastOpenedContainer,
					N5ViewerCreator.n5vGroupParsers,
					N5ViewerCreator.n5vParsers );

			dialog.setLoaderExecutor( exec );
			dialog.setContainerPathUpdateCallback( x -> lastOpenedContainer = x );
			dialog.setTreeRenderer( new N5ViewerTreeCellRenderer( false ) );

			dialog.run( selection -> {
				N5Metadata metadata = selection.metadata.get( 0 );
				if ( metadata instanceof OmeNgffMetadata )
				{
					final NgffSingleScaleAxesMetadata topLevelChildMetadata =
							( ( OmeNgffMetadata ) metadata ).multiscales[ 0 ].getChildrenMetadata()[ 0 ];
					final long[] dimensions = topLevelChildMetadata.getAttributes().getDimensions();
					final double[] scales = topLevelChildMetadata.getScale();
					final String[] axisLabels = topLevelChildMetadata.getAxisLabels();
					int x = 1;
					int y = 1;
					int z = 1;
					int c = 1;
					int t = 1;
					double sx = 1.0;
					double sy = 1.0;
					double sz = 1.0;
					String unit = ( ( OmeNgffMetadata ) metadata ).unit();
					if ( unit == null || unit.isEmpty() )
						unit = "pixel";
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
					final AffineTransform3D calib = ( ( OmeNgffMetadata ) metadata ).spatialTransform3d();
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
											.mapToObj( setup -> new ViewRegistration( tp, setup, calib ) ) )
									.collect( Collectors.toList() )
					);

					final JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle( "Save BigDataViewer XML File" );
					final FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter( "XML Files", "xml" );
					fileChooser.setFileFilter( xmlFilter );
					int userSelection = fileChooser.showSaveDialog( null );
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
						catch ( SpimDataException e )
						{
							e.printStackTrace();
						}

						if ( checkBDVFile( fileToSave ) )
						{
							xmlFile = fileToSave;
						}
					}

				}
			} );
		} );

		labelInfo = new JLabel( "" );
		final GridBagConstraints gbcLabelInfo = new GridBagConstraints();
		gbcLabelInfo.insets = new Insets( 5, 5, 5, 5 );
		gbcLabelInfo.fill = GridBagConstraints.BOTH;
		gbcLabelInfo.gridx = 0;
		gbcLabelInfo.gridy = row++;
		add( labelInfo, gbcLabelInfo );

		btnCreate = new JButton( buttonTitle );
		final GridBagConstraints gbcBtnCreate = new GridBagConstraints();
		gbcBtnCreate.anchor = GridBagConstraints.EAST;
		gbcBtnCreate.gridx = 0;
		gbcBtnCreate.gridy = row++;
		add( btnCreate, gbcBtnCreate );
	}

	boolean checkBDVFile( final File file )
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
}
