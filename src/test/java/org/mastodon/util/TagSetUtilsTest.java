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
package org.mastodon.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TagSetUtilsTest
{

	private final static String tagSetName = "testTagSet";

	private final static String tagLabel0 = "tag0";

	private final static String tagLabel1 = "tag1";

	private final static String tagLabel2 = "tag2";

	private final static Color tagColor0 = Color.red;

	private final static Color tagColor1 = Color.green;

	private final static Color tagColor2 = Color.blue;

	private final static Collection< Pair< String, Integer > > tagsAndColors = initTagsAndColors();

	private static Collection< Pair< String, Integer > > initTagsAndColors()
	{
		Collection< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		tagsAndColors.add( Pair.of( tagLabel0, tagColor0.getRGB() ) );
		tagsAndColors.add( Pair.of( tagLabel1, tagColor1.getRGB() ) );
		tagsAndColors.add( Pair.of( tagLabel2, tagColor2.getRGB() ) );
		return tagsAndColors;
	}

	@Test
	public void testAddNewTagSetToModel()
	{
		ExampleGraph1 exampleGraph1 = new ExampleGraph1();

		TagSetUtils.addNewTagSetToModel( exampleGraph1.getModel(), tagSetName, tagsAndColors );

		TagSetStructure.TagSet tagSet = exampleGraph1.getModel().getTagSetModel().getTagSetStructure().getTagSets().get( 0 );

		assertEquals( tagSetName, tagSet.getName() );
		assertEquals( tagsAndColors.size(), tagSet.getTags().size() );
		assertEquals( tagLabel0, tagSet.getTags().get( 0 ).label() );
		assertEquals( tagLabel1, tagSet.getTags().get( 1 ).label() );
		assertEquals( tagLabel2, tagSet.getTags().get( 2 ).label() );
		assertEquals( tagColor0.getRGB(), tagSet.getTags().get( 0 ).color() );
		assertEquals( tagColor1.getRGB(), tagSet.getTags().get( 1 ).color() );
		assertEquals( tagColor2.getRGB(), tagSet.getTags().get( 2 ).color() );
	}

	@Test
	public void tagSpotAndOutgoingEdges()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		Model model = exampleGraph2.getModel();

		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( exampleGraph2.getModel(), tagSetName, tagsAndColors );

		TagSetStructure.Tag tag0 = tagSet.getTags().get( 0 );
		TagSetUtils.tagSpotAndOutgoingEdges( exampleGraph2.getModel(), tagSet, tag0, exampleGraph2.spot0 );
		TagSetUtils.tagSpotAndOutgoingEdges( exampleGraph2.getModel(), tagSet, tag0, exampleGraph2.spot2 );

		Collection< Spot > taggedSpots = model.getTagSetModel().getVertexTags().getTaggedWith( tag0 );

		assertEquals( 2, taggedSpots.size() );
		assertTrue( taggedSpots.contains( exampleGraph2.spot0 ) );
		assertTrue( taggedSpots.contains( exampleGraph2.spot2 ) );
		// 3 links are tagged: spot0 -> spot1, spot2 -> spot3, spot2 -> spot11
		assertEquals( 3, model.getTagSetModel().getEdgeTags().getTaggedWith( tag0 ).size() );
	}

	@Test
	public void testGetTagSetNames()
	{
		ExampleGraph1 exampleGraph1 = new ExampleGraph1();
		String tagSetName1 = "TagSet1";
		String tagSetName2 = "TagSet2";
		String tagSetName3 = "TagSet2";
		Collection< Pair< String, Integer > > emptyTagsAndColors = Collections.emptyList();
		TagSetUtils.addNewTagSetToModel( exampleGraph1.getModel(), tagSetName1, emptyTagsAndColors );
		TagSetUtils.addNewTagSetToModel( exampleGraph1.getModel(), tagSetName2, emptyTagsAndColors );
		TagSetUtils.addNewTagSetToModel( exampleGraph1.getModel(), tagSetName3, emptyTagsAndColors );
		Collection< String > tagSetNames = TagSetUtils.getTagSetNames( exampleGraph1.getModel() );
		List< String > expected = Arrays.asList( tagSetName1, tagSetName2, tagSetName3 );
		assertEquals( expected, tagSetNames );
	}

	@Test
	public void testGetTagLabel()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		String tagSetName = "TagSet";
		Pair< String, Integer > tag0 = Pair.of( "Tag", 0 );
		Collection< Pair< String, Integer > > tagAndColor = Collections.singletonList( tag0 );
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( exampleGraph2.getModel(), tagSetName, tagAndColor );
		TagSetStructure.Tag tag = tagSet.getTags().get( 0 );
		TagSetUtils.tagBranch( exampleGraph2.getModel(), tagSet, tag, exampleGraph2.spot5 );
		assertEquals( tag.label(), TagSetUtils.getTagLabel( exampleGraph2.getModel(), exampleGraph2.branchSpotD, tagSet ) );
		assertNull( TagSetUtils.getTagLabel( null, exampleGraph2.branchSpotD, tagSet ) );
		assertNull( TagSetUtils.getTagLabel( exampleGraph2.getModel(), null, tagSet ) );
		assertNull( TagSetUtils.getTagLabel( exampleGraph2.getModel(), exampleGraph2.branchSpotD, null ) );
	}
}
