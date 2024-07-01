package org.mastodon.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for exporting JComponents as PNG or SVG files.
 */
public class ExportUtils
{
	private ExportUtils()
	{
		// prevent instantiation
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final String PNG_EXTENSION = "png";

	public static final String SVG_EXTENSION = "svg";

	private static final int PRINT_RESOLUTION = 600;

	/**
	 * Export the given Component as PNG to the specified file.
	 * 
	 * @param file
	 *            the file to export to
	 * @param paintComponent
	 *            the component to export
	 */
	public static void exportPng( final File file, final Component paintComponent )
	{
		final int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
		final double scale = PRINT_RESOLUTION / ( double ) screenResolution;
		final BufferedImage image = new BufferedImage( ( int ) ( paintComponent.getWidth() * scale ),
				( int ) ( paintComponent.getHeight() * scale ), BufferedImage.TYPE_INT_RGB );
		final Graphics2D g = image.createGraphics();
		g.setTransform( AffineTransform.getScaleInstance( scale, scale ) );
		paintComponent.paint( g );
		try
		{
			ImageIO.write( image, PNG_EXTENSION, file );
		}
		catch ( final IOException e )
		{
			logger.error( "Could not export trackscheme as PNG to File: {}.", file.getAbsolutePath(), e );
		}
	}

	/**
	 * Export the given Component as SVG to the specified file.
	 * 
	 * @param file
	 *            the file to export to
	 * @param paintComponent
	 *            the component to export
	 */
	public static void exportSvg( final File file, final Component paintComponent )
	{
		final SVGGraphics2D g2 = new SVGGraphics2D( paintComponent.getWidth(), paintComponent.getHeight() );
		paintComponent.paint( g2 );
		try
		{
			SVGUtils.writeToSVG( file, g2.getSVGElement() );
		}
		catch ( final IOException e )
		{
			logger.error( "Could not export trackscheme as SVG to File: {}.", file.getAbsolutePath(), e );
		}
	}

	/**
	 * Open a file chooser dialog to choose a file to export to, then export the given JComponent to that file using the given export function.
	 * @param extension the file extension to use. Supported extensions are {@link #SVG_EXTENSION} and {@link #PNG_EXTENSION}.
	 * @param exportFunction the function to export the JComponent to a file
	 * @param name the name in the file chooser dialog
	 * @param parentComponent the parent component of the file chooser dialog
	 */
	public static void chooseFileAndExport( final String extension, final Consumer< File > exportFunction, final String name,
			final Container parentComponent )
	{
		final File chosenFile = FileChooser.chooseFile( parentComponent, name + "." + extension, new ExtensionFileFilter( extension ),
				"Save " + name + " to " + extension, FileChooser.DialogType.SAVE );
		if ( chosenFile != null )
		{
			exportFunction.accept( chosenFile );
			openFile( chosenFile );
		}
	}

	private static void openFile( final File chosenFile )
	{
		try
		{
			Desktop.getDesktop().open( chosenFile );
		}
		catch ( final IOException e )
		{
			logger.error( "Could not open file: {}", chosenFile.getAbsolutePath(), e );
		}
	}
}
