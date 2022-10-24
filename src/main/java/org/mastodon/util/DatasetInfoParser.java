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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import bdv.spimdata.SpimDataMinimal;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.XmlKeys;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.XmlIoTimePoints;

/**
 * Parses the XML file of a BDV dataset and return information extracted from
 * the metadata part (saved in the XML) of the first setup without trying to
 * access the pixels.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class DatasetInfoParser
{

	public static final DatasetInfoParser dummyInfo;
	static
	{
		dummyInfo = new DatasetInfoParser( "default", 100, 100, 100, 1., 1., 1., 10 );
	}

	public final int width;

	public final int height;

	public final int depth;

	public final double dx;

	public final double dy;

	public final double dz;

	public final int nTimePoints;

	public final String xmlFilename;

	private DatasetInfoParser(
			final String xmlFilename,
			final int width,
			final int height,
			final int depth,
			final double dx,
			final double dy,
			final double dz,
			final int nTimePoints )
	{
		this.xmlFilename = xmlFilename;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.nTimePoints = nTimePoints;
	}

	public SpimDataMinimal toDummySpimData()
	{
		return DummySpimData.tryCreate( width, height, depth, dx, dy, dz, nTimePoints );
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append( xmlFilename + ":" );
		str.append( "\n - image size: x = " + width + ", y = " + height + ", z = " + depth );
		str.append( "\n - voxel size: x = " + dx + ", y = " + dy + ", z = " + dz );
		str.append( "\n - n time-points: " + nTimePoints );
		return str.toString();
	}

	public static DatasetInfoParser inspect( final String xmlFilename )
	{
		final SAXBuilder sax = new SAXBuilder();
		try
		{
			// Open XML.
			final Document doc = sax.build( xmlFilename );
			final Element root = doc.getRootElement();
			// View setup dim and pixel size.
			final Element vsel = root
					.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
					.getChild( XmlKeys.VIEWSETUPS_TAG )
					.getChild( XmlKeys.VIEWSETUP_TAG );
			final int[] imSize = XmlHelpers.getIntArray( vsel, XmlKeys.VIEWSETUP_SIZE_TAG );
			final Element vxel = vsel.getChild( XmlKeys.VIEWSETUP_VOXELSIZE_TAG );
			final double[] voxelSize = XmlHelpers.getDoubleArray( vxel, XmlKeys.VOXELDIMENSIONS_SIZE_TAG );
			// Time-points.
			final Element timePointsElement = root
					.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
					.getChild( XmlKeys.TIMEPOINTS_TAG );
			final XmlIoTimePoints xmlIoTimePoints = new XmlIoTimePoints();
			final TimePoints timePoints = xmlIoTimePoints.fromXml( timePointsElement );
			final int nTimePoints = timePoints == null ? 10 : timePoints.size();

			return new DatasetInfoParser( xmlFilename, imSize[ 0 ], imSize[ 1 ], imSize[ 2 ], voxelSize[ 0 ], voxelSize[ 1 ], voxelSize[ 2 ], nTimePoints );
		}
		catch ( final Exception e )
		{
			return dummyInfo;
		}
	}
}
