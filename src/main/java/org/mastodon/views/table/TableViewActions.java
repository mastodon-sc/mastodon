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
package org.mastodon.views.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import com.opencsv.CSVWriter;

public class TableViewActions
{

	public static final String EDIT_LABEL = "edit vertex label";

	public static final String TOGGLE_TAG = "toggle tag";

	public static final String EXPORT_TO_CSV = "export to csv";

	private static final String[] EDIT_LABEL_KEYS = new String[] { "F2" };

	private static final String[] TOGGLE_TAG_KEYS = new String[] { "SPACE" };

	private static final String[] EXPORT_TO_CSV_KEYS = new String[] { "not mapped" };

	private final RunnableAction editLabel;

	private final RunnableAction toggleTag;

	private final RunnableAction exportToCSV;

	private TableViewActions( final MyTableViewFrame tableView )
	{
		this.editLabel = new RunnableAction( EDIT_LABEL, tableView::editCurrentLabel );
		this.toggleTag = new RunnableAction( TOGGLE_TAG, tableView::toggleCurrentTag );
		this.exportToCSV = new RunnableAction( EXPORT_TO_CSV, () -> exportToCSV( tableView ) );
	}

	/**
	 * Create table-view actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param frame
	 *            Actions are targeted at this table view.
	 */
	public static void install( final Actions actions, final MyTableViewFrame frame )
	{
		final TableViewActions tva = new TableViewActions( frame );
		actions.namedAction( tva.editLabel, EDIT_LABEL_KEYS );
		actions.namedAction( tva.toggleTag, TOGGLE_TAG_KEYS );
		actions.namedAction( tva.exportToCSV, EXPORT_TO_CSV_KEYS );
	}

	public static String csvExportPath;

	public static final void exportToCSV( final MyTableViewFrame frame )
	{
		exportToCSV( frame.getCurrentlyDisplayedTable() );
	}

	public static void exportToCSV( final FeatureTagTablePanel< ? > table )
	{
		// Try to get at least one object.
		final Object obj = table.getObjectForViewRow( 0 );
		if ( obj == null )
			return;

		// Ask for filename.
		final String klass = obj.getClass().getSimpleName();
		final String filename = ( csvExportPath == null )
				? new File( System.getProperty( "user.home" ), "MastodonTable.csv" ).getAbsolutePath()
				: csvExportPath;
		final File file = FileChooser.chooseFile(
				table,
				filename,
				new ExtensionFileFilter( "csv" ),
				"Export " + klass + " table as CSV file",
				FileChooser.DialogType.SAVE );
		if ( file == null )
			return;
		csvExportPath = file.getAbsolutePath();

		final int p = csvExportPath.lastIndexOf( '.' );
		final String path = csvExportPath.substring( 0, p ) + "-" + klass + ".csv";
		try
		{
			TableViewActions.export( path, table, CSVWriter.DEFAULT_SEPARATOR );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog( table,
					"Could not save to file " + path + ":\n" + e.getMessage(),
					"Error exporting vertices to CSV",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}

	public static < O > void export( final String path, final FeatureTagTablePanel< O > table, final char separator )
			throws IOException
	{
		try (CSVWriter writer = new CSVWriter( new FileWriter( new File( path ) ),
				separator,
				CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END ))
		{
			/*
			 * Headers are complicated: The first 2 columns are label and id,
			 * and they are not in column group. All the rest is, and have an
			 * extra line for units.
			 */
			final GroupableTableHeader header = ( GroupableTableHeader ) table.getTable().getTableHeader();
			final int nCols = table.getTable().getColumnCount();
			final String[][] headerEntries = new String[ 3 ][ nCols ];
			headerEntries[ 0 ][ 0 ] = header.getColumnModel().getColumn( 0 ).getHeaderValue().toString();
			headerEntries[ 1 ][ 0 ] = "";
			headerEntries[ 2 ][ 0 ] = "";
			headerEntries[ 0 ][ 1 ] = header.getColumnModel().getColumn( 1 ).getHeaderValue().toString();
			headerEntries[ 1 ][ 1 ] = "";
			headerEntries[ 2 ][ 1 ] = "";

			if ( null != header.columnGroups )
			{
				int lcol = 2;
				for ( final ColumnGroup cg : header.columnGroups )
				{
					for ( final Object obj : cg.v )
					{
						final ColumnGroup cg2 = ( ColumnGroup ) obj;
						headerEntries[ 0 ][ lcol ] = cg.text;
						headerEntries[ 1 ][ lcol ] = cg2.text;
						headerEntries[ 2 ][ lcol ] =
								header.getColumnModel().getColumn( lcol ).getHeaderValue().toString();
						lcol++;
					}
				}
			}
			for ( int hr = 0; hr < 3; hr++ )
				writer.writeNext( headerEntries[ hr ] );

			/*
			 * Content.
			 */
			final String[] content = new String[ nCols ];
			final int nRows = table.getTable().getRowCount();
			final TableModel model = table.getTable().getModel();
			for ( int r = 0; r < nRows; r++ )
			{
				final int row = table.getTable().convertRowIndexToModel( r );
				for ( int col = 0; col < nCols; col++ )
				{
					final Object obj = model.getValueAt( row, col );
					if ( null == obj )
						content[ col ] = "";
					else if ( obj instanceof Integer )
						content[ col ] = Integer.toString( ( Integer ) obj );
					else if ( obj instanceof Double )
						content[ col ] = Double.toString( ( Double ) obj );
					else if ( obj instanceof Boolean )
						content[ col ] = ( ( Boolean ) obj ) ? "1" : "0";
					else
						content[ col ] = obj.toString();
				}
				writer.writeNext( content );
			}
		}
	}

	/*
	 * Command descriptions for all 'manually' added commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.TABLE );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( EDIT_LABEL, EDIT_LABEL_KEYS, "Edit the label of the current row." );
			descriptions.add( TOGGLE_TAG, TOGGLE_TAG_KEYS, "Toggle the tag at the current cell in the table." );
			descriptions.add( EXPORT_TO_CSV, EXPORT_TO_CSV_KEYS,
					"Export the content of the displayed table to a CSV file." );
		}
	}
}
