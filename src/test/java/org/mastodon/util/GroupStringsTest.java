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
package org.mastodon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mastodon.util.GroupStrings.Group;

public class GroupStringsTest
{

	@Test
	public void testSingle()
	{
		final List< String > names = new ArrayList<>();
		names.add( "Select to Child" );
		final GroupStrings gs = new GroupStrings();
		for ( final String string : names )
			gs.add( string );
		final Collection< Group > groups = gs.group();
		assertEquals( "Did not group into the right numbers of groups.", 1, groups.size() );
	}

	@Test
	public void testGroup()
	{
		final String s0 = "Import all the TGMM tracks";
		final String s1 = "Import all the Simi BioCell tracks";
		final String s2 = "Import all the MaMuT project";

		final List< String > names = new ArrayList<>();
		names.add( s0 );
		names.add( s1 );
		names.add( s2 );
		final GroupStrings gs = new GroupStrings();
		for ( final String string : names )
			gs.add( string );
		final Collection< Group > groups = gs.group();
		assertEquals( "Did not group into the right numbers of groups.", 1, groups.size() );

		final Group group = groups.iterator().next();
		final String suffix0 = group.suffix( s0 );
		assertTrue( "Unexpected suffix.", "TGMM tracks".equals( suffix0 ) );
	}

	@Test
	public void testSplit()
	{
		final List< String > names = new ArrayList<>();
		names.add( "Select to Child" );
		names.add( "Select to Parent" );
		names.add( "Select to Left" );
		names.add( "Select to Right" );
		names.add( "Select Whole Track" );
		names.add( "Select Track Downward" );
		names.add( "Select Track Upward" );
		final GroupStrings gs = new GroupStrings();
		for ( final String string : names )
			gs.add( string );
		final Collection< Group > groups = gs.group();
		assertEquals( "Did not group into the right numbers of groups.", 3, groups.size() );
	}

	@Test
	public void testGlobal()
	{
		final List< String > names = new ArrayList<>();
		names.add( "New Project" );
		names.add( "Load Project" );
		names.add( "Save Project" );
		names.add( "Import TGMM tracks" );
		names.add( "Import Simi BioCell tracks" );
		names.add( "Import MaMuT project" );
		names.add( "Export MaMuT project" );
		names.add( "New Bdv" );
		names.add( "New Trackscheme" );
		names.add( "Preferences..." );
		names.add( "Settings Toolbar" );
		names.add( "Undo" );
		names.add( "Redo" );
		names.add( "Delete Selection" );
		names.add( "Select Whole Track" );
		names.add( "Select Track Downward" );
		names.add( "Select Track Upward" );
		names.add( "Load Bdv Settings" );
		names.add( "Save Bdv Settings" );
		names.add( "Brightness & Color" );
		names.add( "Visibility & Grouping" );
		names.add( "Navigate to Child" );
		names.add( "Navigate to Parent" );
		names.add( "Navigate to Left" );
		names.add( "Navigate to Right" );
		names.add( "Select to Child" );
		names.add( "Select to Parent" );
		names.add( "Select to Left" );
		names.add( "Select to Right" );
		names.add( "Toggle Focused Vertex Selection" );
		names.add( "Edit Vertex Label" );

		final GroupStrings gs = new GroupStrings();
		for ( final String string : names )
			gs.add( string );

		final Collection< Group > groups = gs.group();
		//		for ( final Group group : groups )
		//			System.out.println( group ); // DEBUG

		assertEquals( "Did not group into the right numbers of groups.", 18, groups.size() );
	}
}
