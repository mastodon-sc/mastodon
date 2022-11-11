package org.mastodon.views.trackscheme.bdv;

import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;
import org.mastodon.views.bdv.SharedBigDataViewerData;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SharedBigDataViewerDataTest
{
	@Test
	public void testFromSpimDataXmlFileWindowsStyle() throws SpimDataException, IOException
	{
		ViewerOptions viewerOptions = new ViewerOptions();
		SharedBigDataViewerData sharedBigDataViewerData = SharedBigDataViewerData.fromSpimDataXmlFile( "./src/test/resources/org/mastodon/mamut/importer/trackmate/mamutproject/FakeTracksBDV.xml", viewerOptions, null );
		assertEquals( 50, sharedBigDataViewerData.getNumTimepoints() );
		assertEquals( 128, sharedBigDataViewerData.getSpimData().getSequenceDescription().getViewSetupsOrdered().get( 0 ).getSize().dimension( 0 ) );
		assertEquals( 128, sharedBigDataViewerData.getSources().get( 0 ).getSpimSource().getSource( 0, 0 ).dimension( 0 ) );
	}

	@Test
	public void testFromSpimDataXmlFileUnixAndMacOSStyle() throws SpimDataException, IOException
	{
		ViewerOptions viewerOptions = new ViewerOptions();
		SharedBigDataViewerData sharedBigDataViewerData = SharedBigDataViewerData.fromSpimDataXmlFile( ".\\src\\test\\resources\\org\\mastodon\\mamut\\importer\\trackmate\\mamutproject\\FakeTracksBDV.xml", viewerOptions, null );
		assertEquals( 50, sharedBigDataViewerData.getNumTimepoints() );
		assertEquals( 128, sharedBigDataViewerData.getSpimData().getSequenceDescription().getViewSetupsOrdered().get( 0 ).getSize().dimension( 0 ) );
		assertEquals( 128, sharedBigDataViewerData.getSources().get( 0 ).getSpimSource().getSource( 0, 0 ).dimension( 0 ) );
	}
}
