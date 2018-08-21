package org.mastodon.project;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MamutProject
{
	/**
	 * The project folder.
	 */
	private File projectRoot;

	private final File datasetXmlFile;

	static final String PROJECT_FILE_NAME = "project.xml";
	static final String RAW_MODEL_FILE_NAME = "model.raw";
	static final String RAW_TAGS_FILE_NAME = "tags.raw";

	public MamutProject(
			final File projectRoot,
			final File datasetXmlFile )
	{
		this.projectRoot = projectRoot;
		this.datasetXmlFile = datasetXmlFile;
	}

	/**
	 * Get the project folder or zip file.
	 *
	 * @return the project folder or zip file.
	 */
	public File getProjectRoot()
	{
		return projectRoot;
	}

	public void setProjectRoot( final File projectRoot )
	{
		this.projectRoot = projectRoot;
	}

	public File getDatasetXmlFile()
	{
		return datasetXmlFile;
	}

	public File getProjectFile()
	{
		return new File( projectRoot, PROJECT_FILE_NAME );
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n"
				+ " - projectRoot: " + getProjectRoot() + "\n"
				+ " - dataset: " + getDatasetXmlFile();
	}

	public ProjectReader openForReading()
	{
		return new ReadFromDirectory();
	}

	public ProjectWriter openForWriting()
	{
		return new WriteToDirectory();
	}

	public interface ProjectReader extends Closeable
	{
		InputStream getRawModelInputStream() throws FileNotFoundException;

		InputStream getRawTagsInputStream() throws FileNotFoundException;
	}

	public interface ProjectWriter extends Closeable
	{
		OutputStream getRawModelOutputStream() throws FileNotFoundException;

		OutputStream getRawTagsOutputStream() throws FileNotFoundException;
	}

	private class ReadFromDirectory implements ProjectReader
	{
		@Override
		public InputStream getRawModelInputStream() throws FileNotFoundException
		{
			return new FileInputStream( new File( projectRoot, RAW_MODEL_FILE_NAME ) );
		}

		@Override
		public InputStream getRawTagsInputStream() throws FileNotFoundException
		{
			return new FileInputStream( new File( projectRoot, RAW_TAGS_FILE_NAME ) );
		}

		@Override
		public void close()
		{
		}
	}

	private class WriteToDirectory implements ProjectWriter
	{
		@Override
		public OutputStream getRawModelOutputStream() throws FileNotFoundException
		{
			return new FileOutputStream( new File( projectRoot, RAW_MODEL_FILE_NAME ) );
		}

		@Override
		public OutputStream getRawTagsOutputStream() throws FileNotFoundException
		{
			return new FileOutputStream( new File( projectRoot, RAW_TAGS_FILE_NAME ) );
		}

		@Override
		public void close() throws IOException
		{
		}
	}
}
