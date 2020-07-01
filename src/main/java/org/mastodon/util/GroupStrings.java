package org.mastodon.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class used to group a collection of strings by their common first
 * words.
 * <p>
 * For instance, the 3 strings:
 *
 * <pre>
 * -"Save Project"
 * -"Import TGMM tracks"
 * -"Import Simi BioCell tracks"
 * </pre>
 *
 * will be grouped in 2 groups:
 *
 * <pre>
 * -"Save Project"
 * -"Import"
 * 	+"TGMM tracks"
 * 	+"Simi BioCell tracks"
 * </pre>
 *
 * Comparisons of common words are case insensitive.
 *
 * @author Jean-Yves Tinevez
 */
public class GroupStrings
{

	private final List< String > names;

	public GroupStrings()
	{
		this.names = new ArrayList<>();
	}

	public void add( final String string )
	{
		names.add( string );
	}

	public Collection< Group > group()
	{
		final List< Group > sgs = new ArrayList<>();
		for ( final String name : names )
			sgs.add( new Group( name ) );

		/*
		 * Fuse.
		 */
		final List< Group > fused = fuseCollection( sgs );

		/*
		 * Split.
		 */
		final List< Group > split = new ArrayList<>();
		for ( final Group sg : fused )
			split.addAll( sg.split() );

		return split;
	}

	private static List< Group > fuseCollection( final Collection< Group > c )
	{
		if ( c.size() <= 1 )
			return new ArrayList<>( c );

		final Deque< Group > champions = new ArrayDeque<>( c );
		final List< Group > fused = new ArrayList<>();
		while ( !champions.isEmpty() )
		{

			// First champion.
			Group champion = champions.pop();

			// Match the champion against all other challengers.
			final Set< Group > challengers = new HashSet<>( champions );
			challengers.remove( champion );

			for ( final Group challenger : challengers )
			{
				final Group fusion = champion.fuse( challenger );
				if ( null == fusion )
				{
					// No fusion possible.
				}
				else
				{
					// Fusion is possible. The challenger won't become a
					// champion.
					champions.remove( challenger );
					// The fusion becomes the champion.
					champion = fusion;
				}
			}

			fused.add( champion );
		}
		return fused;
	}

	/**
	 * Represent a group of strings, grouped by their common first words.
	 */
	public static class Group
	{

		/**
		 * Collection of strings in this group.
		 */
		public final Set< String > strings;

		/**
		 * Common first words of this group, in order.
		 */
		public final List< String > words;

		private Group()
		{
			this.words = new ArrayList<>();
			this.strings = new HashSet<>();
		}

		private Group( final String string )
		{
			this();
			strings.add( string );
			words.addAll( Arrays.asList( string.split( "\\s+" ) ) );
		}

		/**
		 * Tries to split a single group into several ones when by trying to
		 * increase the number of common words in the split groups.
		 *
		 * @return a collection of groups.
		 */
		private Collection< Group > split()
		{
			if ( strings.size() < 3 )
				return Collections.singleton( this );

			// Recreate each singleton group.
			final List< Group > singletons = new ArrayList<>();
			for ( final String string : strings )
			{
				final Group singleton = new Group();
				singleton.strings.add( string );
				// But we remove the common words of this group to check if we
				// can regroup them.
				final String[] tokens = string.split( "\\s+" );
				for ( int i = this.words.size(); i < tokens.length; i++ )
					singleton.words.add( tokens[ i ] );

				singletons.add( singleton );
			}
			final List< Group > split = fuseCollection( singletons );

			// If it was not interesting to split, don't.
			if ( split.size() == singletons.size() )
				return Collections.singleton( this );

			// Now we need to re-add the common words we removed.
			for ( final Group s : split )
				s.words.addAll( 0, this.words );

			return split;
		}

		/**
		 * Fuses this subgroup with the specified one. Fusion is made by
		 * checking whether the first words of the string composition are
		 * common. Returns <code>null</code> if the two subgroups have no words
		 * in common amd fusion is impossible.
		 *
		 * @param other
		 *            the subgroup to fuse with.
		 * @return a fused subgroup, or <code>null</code> if fusion is
		 *         impossible.
		 */
		private Group fuse( final Group other )
		{
			final int nWords = Math.min( this.words.size(), other.words.size() );
			if ( nWords == 0 )
				return null;

			int nCommonWords = 0;
			for ( int i = 0; i < nWords; i++ )
			{
				if ( this.words.get( i ).equalsIgnoreCase( other.words.get( i ) ) )
					nCommonWords = i + 1;
				else
					break;
			}
			if ( nCommonWords == 0 )
				return null;

			final Group fused = new Group();
			fused.strings.addAll( this.strings );
			fused.strings.addAll( other.strings );
			for ( int i = 0; i < nCommonWords; i++ )
				fused.words.add( this.words.get( i ) );

			return fused;
		}

		/**
		 * Returns the part of the specified string left after the common words
		 * in this group has been removed.
		 *
		 * @param string
		 *            the string to determine the suffix of.
		 * @return the suffix.
		 * @throws IllegalArgumentException
		 *             if the specified string does not belong in this group.
		 */
		public String suffix( final String string )
		{
			if ( !strings.contains( string ) )
				throw new IllegalArgumentException( "Specified string '" + string + "' does not belong in this group." );

			// Deal with singleton groups.
			if ( strings.size() == 1 )
				return string;

			String out = string;
			for ( final String word : words )
			{
				out = out.substring( word.length() );
				out = out.trim();
			}
			return out;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder( super.toString() );
			str.append( "\n - Strings: " + strings );
			str.append( "\n - Common words: " + words );
			return str.toString();
		}

	}
}
