/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.importer.simi;

import static org.mastodon.mamut.io.importer.simi.SimiImporter.ExpectedNumTokens.atleast;
import static org.mastodon.mamut.io.importer.simi.SimiImporter.ExpectedNumTokens.exactly;
import static org.mastodon.mamut.io.importer.simi.SimiImporter.LineType.EOF;
import static org.mastodon.mamut.io.importer.simi.SimiImporter.LineType.HEADER;
import static org.mastodon.mamut.io.importer.simi.SimiImporter.LineType.NORMAL;
import static org.mastodon.mamut.io.importer.simi.SimiImporter.LineType.SEPARATOR;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Import SIMI*BIOCELL lineages.
 * <p>
 * Draws heavily from https://github.com/nelas/simi.py by Bruno Vellutini.
 *
 * @author Tobias Pietzsch
 */
public class SimiImporter
{
	/**
	 * Import a SIMI*BIOCELL {@code .sdb} file into a model.
	 *
	 * @param sbdFilename
	 *            name of the {@code .sdb} file to read.
	 * @param timepointIdFunction
	 *            maps frames from {@code .sdb} file to timepoints (indices) in
	 *            the model.
	 * @param labelFunction
	 *            maps names from {@code .sdb} file to spot labels.
	 * @param positionFunction
	 *            maps frame and coordinates from {@code .sdb} file to spot
	 *            coordinates.
	 * @param radius
	 *            radius for all created spots.
	 * @param interpolateMissingSpots
	 *            whether gaps (missing frames) in the {@code .sdb} file should
	 *            be filled by interpolated spots.
	 * @param model
	 *            the {@link Model} to update with the read tracks.
	 *
	 * @throws IOException
	 *             when the specified {@code .sdb} file cannot be read.
	 * @throws ParseException
	 *             when errors occur in parsing.
	 */
	public static void read(
			final String sbdFilename,
			final IntUnaryOperator timepointIdFunction,
			final LabelFunction labelFunction,
			final BiFunction< Integer, double[], double[] > positionFunction,
			final double radius,
			final boolean interpolateMissingSpots,
			final Model model )
			throws IOException, ParseException
	{
		final Simi simi = readSDB( new File( sbdFilename ) );
		new Builder( model, simi, timepointIdFunction, labelFunction, positionFunction, radius,
				interpolateMissingSpots );
	}

	/**
	 * Function to generate Spot labels from various names appearing in the simi file.
	 */
	public interface LabelFunction
	{
		/**
		 * Function to generate Spot labels from various names appearing in the
		 * simi file.
		 *
		 * @param generic_name
		 *            the cell generic name.
		 * @param generation_name
		 *            the cell generation name.
		 * @param name
		 *            the cell name.
		 * @return a spot name. Can be <code>null</code>.
		 */
		String apply( final String generic_name, final String generation_name, final String name );
	}

	/*
	 * Utilities for parsing the SDB file
	 */

	enum LineType
	{
		NORMAL, HEADER, SEPARATOR, EOF
	}

	static final class Line
	{
		private final LineType type;

		private final String[] tokens;

		private final int lineNumber;

		private final String raw;

		public Line( final String line, final int lineNumber )
		{
			raw = line;
			tokens = line.split( "\\s+" );
			this.lineNumber = lineNumber;
			if ( tokens.length == 1 && tokens[ 0 ].equals( "SIMI*BIOCELL" ) )
				type = HEADER;
			else if ( tokens.length == 1 && tokens[ 0 ].equals( "---" ) )
				type = SEPARATOR;
			else
				type = NORMAL;
		}

		public Line()
		{
			raw = "EOF";
			tokens = new String[ 0 ];
			type = EOF;
			lineNumber = -1;
		}

		@Override
		public String toString()
		{
			return raw;
		}

		public LineType type()
		{
			return type;
		}

		public int size()
		{
			return tokens.length;
		}

		public String get( final int i )
		{
			return ( i > 0 && i < tokens.length ) ? tokens[ i ] : null;
		}

		public int getAsInt( final int i ) throws ParseException
		{
			try
			{
				return Integer.parseInt( tokens[ i ] );
			}
			catch ( final NumberFormatException e )
			{
				throw new ParseException( "Token is not an integer", lineNumber );
			}
		}

		public int getLineNumber()
		{
			return lineNumber;
		}
	}

	static final class ExpectedNumTokens
	{
		final int count;

		final boolean matchExactly;

		private ExpectedNumTokens( final int count, final boolean matchExactly )
		{
			this.count = count;
			this.matchExactly = matchExactly;
		}

		public boolean matches( final int n )
		{
			return matchExactly
					? n == count
					: n >= count;
		}

		static ExpectedNumTokens exactly( final int count )
		{
			return new ExpectedNumTokens( count, true );
		}

		static ExpectedNumTokens atleast( final int count )
		{
			return new ExpectedNumTokens( count, false );
		}
	}

	static final class Scanner implements Closeable
	{
		private final Stream< String > stream;

		private final Iterator< String > iter;

		private int lineNumber;

		public static final Line eof = new Line();

		public Scanner( final Path path ) throws IOException
		{
			stream = Files.lines( path );
			iter = stream.iterator();
			lineNumber = 0;
		}

		public boolean hasNext()
		{
			return iter.hasNext();
		}

		public Line next()
		{
			return hasNext() ? new Line( iter.next(), ++lineNumber ) : eof;
		}

		@Override
		public void close() throws IOException
		{
			stream.close();
		}

		public Line match( final LineType expectedType ) throws ParseException
		{
			return match( expectedType, atleast( 0 ), null );
		}

		public Line match( final LineType expectedType, final String error ) throws ParseException
		{
			return match( expectedType, atleast( 0 ), error );
		}

		public Line match( final LineType expectedType, final ExpectedNumTokens expectedNumTokens )
				throws ParseException
		{
			return match( expectedType, expectedNumTokens, null );
		}

		public Line match( final LineType expectedType, final ExpectedNumTokens expectedNumTokens, final String error )
				throws ParseException
		{
			final Line line = next();
			if ( !( line.type() == expectedType && expectedNumTokens.matches( line.size() ) ) )
			{
				String msg = error;
				if ( msg == null )
					msg = "expected " + expectedType + " line with "
							+ ( expectedNumTokens.matchExactly ? "" : "at least " )
							+ expectedNumTokens.count + " tokens.";
				throw new ParseException( msg, line.getLineNumber() );
			}
			return line;
		}
	}

	/*
	 * Representation of SDB data structures
	 */

	static final class SimiCellPoint
	{
		public final int t;

		public final double[] pos;

		public SimiCellPoint( final int t, final int x, final int y, final int z )
		{
			this.t = t;
			this.pos = new double[] { x, y, z };
		}
	}

	static final class SimiCell
	{
		public final String generic_name;

		public final String generation_name;

		public final String name;

		public final List< SimiCellPoint > points;

		public SimiCell left;

		public SimiCell right;

		public SimiCell( final String generic_name, final String generation_name, final String name )
		{
			this.generic_name = generic_name;
			this.generation_name = generation_name;
			this.name = name;
			points = new ArrayList<>();
			left = null;
			right = null;
		}

		public void add( final int t, final int x, final int y, final int z )
		{
			points.add( new SimiCellPoint( t, x, y, z ) );
		}
	}

	static final SimiCell NO_CHILD = new SimiCell( null, null, null );

	static class Simi
	{
		int version;

		SimiCell root = null;

		final ArrayDeque< SimiCell > stack = new ArrayDeque<>();

		/**
		 * Cells must be added in sequence as they appear in the {@code .sdb} file.
		 */
		SimiCell cell( final String generic_name, final String generation_name, final String name,
				final int cells_left_count, final int cells_right_count )
		{
			final SimiCell cell = new SimiCell( generic_name, generation_name, name );

			if ( root == null )
				root = cell;

			if ( cells_left_count == 0 )
				cell.left = NO_CHILD; // left child doesn't need to be filled
			if ( cells_right_count == 0 )
				cell.right = NO_CHILD; // right child doesn't need to be filled

			final boolean hasChildren = cell.left != NO_CHILD || cell.right != NO_CHILD;
			if ( hasChildren )
				stack.push( cell );
			else
			{
				SimiCell child = cell;
				SimiCell parent = stack.peek();
				while ( parent != null )
				{
					if ( parent.left == null )
						parent.left = child;
					else if ( parent.right == null )
						parent.right = child;

					if ( parent.left == null || parent.right == null )
						break; // parent still has unfilled children

					// otherwise, parent is complete
					stack.pop(); // pop it off the stack
					child = parent;
					parent = stack.peek();
				}
			}

			return cell;
		}
	}

	/**
	 * Parse {@code .sdb} file into a {@link Simi} object.
	 */
	static Simi readSDB( final File sdb ) throws IOException, ParseException
	{
		try (Scanner scanner = new Scanner( sdb.toPath() ))
		{

			final Simi simi = new Simi();

			scanner.match( HEADER, "expected SIMI*BIOCELL header." );
			simi.version = scanner.match( NORMAL, exactly( 1 ), "expected format version." ).getAsInt( 0 );

			scanner.match( SEPARATOR );

			final int free_3D_cells_count = scanner.match( NORMAL, exactly( 1 ) ).getAsInt( 0 );
			for ( int i = 0; i < free_3D_cells_count; ++i )
				scanner.match( NORMAL ); // discard for now

			scanner.match( SEPARATOR );

			Line line;
			line = scanner.match( NORMAL, exactly( 2 ) );
			final int start_cells_count = line.getAsInt( 0 );
			//			final int start_time = line.getAsInt( 1 );
			for ( int i = 0; i < start_cells_count; ++i )
				scanner.match( NORMAL ); // discard for now

			scanner.match( SEPARATOR );

			while ( scanner.hasNext() )
			{
				line = scanner.match( NORMAL, atleast( 4 ) );
				final int cells_left_count = line.getAsInt( 0 );
				final int cells_right_count = line.getAsInt( 1 );
				//				final int active_cell_left = line.getAsInt( 2 );
				//				final int active_cell_right = line.getAsInt( 3 );
				final String generic_name = line.get( 4 );

				line = scanner.match( NORMAL, atleast( 4 ) );
				//				final int generation_birth_time= line.getAsInt( 0 );
				//				final int generation_level = line.getAsInt( 1 );
				//				final String generation_wildtype = line.get( 2 );
				//				final String generation_color = line.get( 3 );
				final String generation_name = line.get( 4 );

				line = scanner.match( NORMAL, atleast( 5 ) );
				//				final int birth_frame = line.getAsInt( 0 );
				//				final int birth_level = line.getAsInt( 1 );
				//				final String wildtype = line.get( 2 );
				//				final String size = line.get( 3 );
				//				final String shape = line.get( 4 );
				//				final String color = line.get( 5 );
				final String name = line.get( 6 );

				line = scanner.match( NORMAL, atleast( 1 ) );
				final int coordinates_count = line.getAsInt( 0 );
				//				final String cell_comment = line.get( 1 );

				final SimiCell cell =
						simi.cell( generic_name, generation_name, name, cells_left_count, cells_right_count );

				for ( int i = 0; i < coordinates_count; ++i )
				{
					line = scanner.match( NORMAL, atleast( 7 ) );
					final int frame = line.getAsInt( 0 );
					final int x = line.getAsInt( 1 );
					final int y = line.getAsInt( 2 );
					final int level = line.getAsInt( 3 );
					//					final String _size = line.get( 4 );
					//					final String _shape = line.get( 5 );
					//					final String coord_comment = line.get( 6 );

					cell.add( frame, x, y, level );
				}

				scanner.match( SEPARATOR );
			}

			return simi;
		}
	}

	/*
	 * ModelImporter from Simi
	 */
	static final class Builder extends ModelImporter
	{
		private final IntUnaryOperator timepointIdFunction;

		private final LabelFunction labelFunction;

		private final BiFunction< Integer, double[], double[] > positionFunction;

		private final double radius;

		private final boolean interpolate;

		private final ModelGraph graph;

		Builder(
				final Model model,
				final Simi simi,
				final IntUnaryOperator timepointIdFunction,
				final LabelFunction labelFunction,
				final BiFunction< Integer, double[], double[] > positionFunction,
				final double radius,
				final boolean interpolate )
		{
			super( model );

			this.timepointIdFunction = timepointIdFunction;
			this.labelFunction = labelFunction;
			this.positionFunction = positionFunction;
			this.radius = radius;
			this.interpolate = interpolate;
			this.graph = model.getGraph();

			startImport();
			add( simi.root, null );
			finishImport();
		}

		private void add( final SimiCell cell, final Spot parent )
		{
			final Spot vref1 = graph.vertexRef();
			final Spot vref2 = graph.vertexRef();
			final Link eref = graph.edgeRef();

			Spot spot = parent;
			Spot parent1 = parent;
			for ( final SimiCellPoint point : cell.points )
			{
				final String label = labelFunction.apply( cell.generic_name, cell.generation_name, cell.name );
				final double[] pos = positionFunction.apply( point.t, point.pos );
				final int tp = timepointIdFunction.applyAsInt( point.t );
				if ( parent != null && parent.getTimepoint() >= tp )
				{
					System.out.println( "skipping for " + label );
					continue;
				}
				spot = graph.addVertex( vref1 ).init( tp, pos, radius );
				spot.setLabel( label );
				if ( parent1 != null )
				{
					if ( interpolate )
					{
						final int pTp = parent1.getTimepoint();
						if ( tp - pTp > 1 )
						{
							// add intermediate spots
							final Spot vref3 = graph.vertexRef();
							final String plabel = parent1.getLabel();
							final double[] pPos = new double[ 3 ];
							parent1.localize( pPos );
							for ( int tp1 = pTp + 1; tp1 < tp; ++tp1 )
							{
								final double[] pos1 = new double[ 3 ];
								final double f = ( ( double ) ( tp1 - pTp ) ) / ( tp - pTp );
								for ( int d = 0; d < 3; ++d )
									pos1[ d ] = ( 1.0 - f ) * pPos[ d ] + f * pos[ d ];
								final Spot spot1 = graph.addVertex( vref3 ).init( tp1, pos1, radius );
								spot1.setLabel( "i_" + plabel );
								graph.addEdge( parent1, spot1, eref ).init();
								parent1 = vref2.refTo( spot1 );
							}
							graph.releaseRef( vref3 );
						}
					}
					graph.addEdge( parent1, spot, eref ).init();
				}

				parent1 = vref2.refTo( spot );
			}

			if ( cell.left != NO_CHILD )
				add( cell.left, spot );
			if ( cell.right != NO_CHILD )
				add( cell.right, spot );

			graph.releaseRef( vref1 );
			graph.releaseRef( vref2 );
			graph.releaseRef( eref );
		}
	}
}

/*
### Data v4.00 ####################################################

# Header: # # # # # # #
SIMI*BIOCELL
400
---
# # # # # # # # # # # #

SIMI*BIOCELL = magic ID string
400          = file version 4.00
---          = separator


# Cell: # # # # # # # #
<free 3D cells count>
<start frame> <end frame> <x> <y> <level> <comment>     <= *count
---
<start cells count> <start time>
<start generation>                                      <= *count
---
<cells left count> <"right> <active cell left> <"right> <gen.name1>
<gen.time of birth sec.> <g.level> <g.wildtype> <g.color> <g.name2>
<birth frm> <mitosis lvl> <wildtype> <size> <shape> <color> <name>
<coordinates count> <cell comment>
<frame> <x> <y> <level> <size> <shape> <coord.comment>  <= *count
---
# # # # # # # # # # # #

<start ...> values are not used yet (for later implementation)
<color> is a real hexadecimal RGB value (e.g. 00ff00 for green)
<size> and <shape> are internal values of BioCell
<coordinates> are real pixels

###################################################################
*/
