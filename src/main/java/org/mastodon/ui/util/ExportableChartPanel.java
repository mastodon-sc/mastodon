/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.util;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import com.itextpdf.text.DocumentException;

public class ExportableChartPanel extends ChartPanel
{

	private static final long serialVersionUID = 1L;

	private static File currentDir;

	/*
	 * CONSTRUCTORS
	 */

	public ExportableChartPanel( final JFreeChart chart )
	{
		super( chart );
	}

	public ExportableChartPanel(
			final JFreeChart chart,
			final boolean properties,
			final boolean save,
			final boolean print,
			final boolean zoom,
			final boolean tooltips )
	{
		super( chart, properties, save, print, zoom, tooltips );
	}

	public ExportableChartPanel( final JFreeChart chart, final int width, final int height,
			final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth,
			final int maximumDrawHeight, final boolean useBuffer, final boolean properties,
			final boolean save, final boolean print, final boolean zoom, final boolean tooltips )
	{
		super( chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight,
				useBuffer, properties, save, print, zoom, tooltips );
	}

	public ExportableChartPanel( final JFreeChart chart, final int width, final int height,
			final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth,
			final int maximumDrawHeight, final boolean useBuffer, final boolean properties,
			final boolean copy, final boolean save, final boolean print, final boolean zoom,
			final boolean tooltips )
	{
		super( chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight,
				useBuffer, properties, copy, save, print, zoom, tooltips );
	}

	/*
	 * METHODS
	 */

	@Override
	protected JPopupMenu createPopupMenu( final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom )
	{
		final JPopupMenu menu = super.createPopupMenu( properties, copy, false, print, zoom );

		menu.addSeparator();

		final JMenuItem exportToFile = new JMenuItem( "Export plot to file" );
		exportToFile.addActionListener( e -> doSaveAs() );
		menu.add( exportToFile );

		return menu;
	}

	/**
	 * Opens a file chooser and gives the user an opportunity to save the chart
	 * in PNG, PDF or SVG format.
	 */
	@Override
	public void doSaveAs()
	{
		if ( null == currentDir )
			currentDir = getDefaultDirectoryForSaveAs();

		final File file;
		if ( FileChooser.isMac() )
		{
			Container dialogParent = getParent();
			while ( !( dialogParent instanceof Frame ) )
				dialogParent = dialogParent.getParent();

			final Frame frame = ( Frame ) dialogParent;
			final FileDialog dialog = new FileDialog( frame, "Export chart to PNG, PDF or SVG", FileDialog.SAVE );
			final FilenameFilter filter = ( dir, name ) -> name.endsWith( ".png" ) || name.endsWith( ".pdf" ) || name.endsWith( ".svg" );
			dialog.setFilenameFilter( filter );
			dialog.setDirectory( currentDir == null ? null : currentDir.getAbsolutePath() );
			dialog.setFile( getChart().getTitle().getText().replaceAll( "\\.+$", "" ) + ".pdf" );
			dialog.setVisible( true );
			final String selectedFile = dialog.getFile();
			if ( null == selectedFile )
				return;

			file = new File( dialog.getDirectory(), selectedFile );
			currentDir = new File( dialog.getDirectory() );
		}
		else
		{
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle( "Export chart to PNG, PDF or SVG" );
			fileChooser.setCurrentDirectory( currentDir );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter("PNG Image File", "png" ) );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter("Portable Document File (PDF)", "pdf" ) );
			fileChooser.addChoosableFileFilter( new FileNameExtensionFilter( "Scalable Vector Graphics (SVG)", "svg" ) );
			fileChooser.setSelectedFile( new File( currentDir, getChart().getTitle().getText().replaceAll( "\\.+$", "" ) + ".pdf" ) );
			final int option = fileChooser.showSaveDialog( this );
			if ( option != JFileChooser.APPROVE_OPTION )
				return;

			file = fileChooser.getSelectedFile();
			currentDir = fileChooser.getCurrentDirectory();
		}
		try
		{
			if ( file.getPath().endsWith( ".png" ) )
				ChartUtils.saveChartAsPNG( file, getChart(), getWidth(), getHeight() );
			else if ( file.getPath().endsWith( ".pdf" ) )
				ChartExporter.exportChartAsPDF( file, getChart(), getWidth(), getHeight() );
			else if ( file.getPath().endsWith( ".svg" ) )
				ChartExporter.exportChartAsSVG( file, getChart(), getWidth(), getHeight() );
			else
				JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( this ), "Invalid file extension.\n"
						+ "Please choose a filename with one of the 3 supported extension: .png, .pdf or .svg." );
		}
		catch ( final IOException | DocumentException e )
		{
			e.printStackTrace();
		}
	}
}
