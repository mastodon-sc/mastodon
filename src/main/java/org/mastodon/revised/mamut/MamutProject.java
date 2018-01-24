package org.mastodon.revised.mamut;

import java.io.File;

public class MamutProject
{
	/**
	 * The project folder.
	 */
	private File projectFolder;

	private final File datasetXmlFile;

	static final String PROJECT_FILE_NAME = "project.xml";
	static final String RAW_MODEL_FILE_NAME = "model.raw";
	static final String RAW_TAGS_FILE_NAME = "tags.raw";

	public MamutProject(
			final File projectFolder,
			final File datasetXmlFile )
	{
		this.projectFolder = projectFolder;
		this.datasetXmlFile = datasetXmlFile;
	}

	/**
	 * Get the project folder.
	 *
	 * @return the project folder.
	 */
	public File getProjectFolder()
	{
		return projectFolder;
	}

	public void setProjectFolder( final File projectFolder )
	{
		this.projectFolder = projectFolder;
	}

	public File getDatasetXmlFile()
	{
		return datasetXmlFile;
	}

	public File getRawModelFile()
	{
		return new File( projectFolder, RAW_MODEL_FILE_NAME );
	}

	public File getRawTagsFile()
	{
		return new File( projectFolder, RAW_TAGS_FILE_NAME );
	}

	public File getProjectFile()
	{
		return new File( projectFolder, PROJECT_FILE_NAME );
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n"
				+ " - projectFolder: " + getProjectFolder() + "\n"
				+ " - projectFile: " + getProjectFile() + "\n"
				+ " - modelFile: " + getRawModelFile() + "\n"
				+ " - tagsFile: " + getRawTagsFile();
	}
}
