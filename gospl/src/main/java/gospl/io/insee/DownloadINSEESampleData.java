package gospl.io.insee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.xerces.impl.dv.xs.MonthDayDV;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;
import core.util.data.DataLocalRepository;
import gospl.GosplPopulation;
import gospl.distribution.GosplInputDataManager;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;

/**
 * Downloads INSEE samples from the INSEE website.
 * Give it an URL to a zip file delivered by the INSEE french national institute for statistics.
 * It will download it on your local repository (see {@link DataLocalRepository}), unzip it, 
 * and parse the dictionnary and data. 
 * 
 * @author Samuel Thiriot
 */
public class DownloadINSEESampleData {

	private Logger logger = LogManager.getLogger();

	private String encoding = null;
	
	private File zipFile = null;
	
	private Collection<DemographicAttribute<? extends IValue>> dictionnary = null;
	
	private File fileSample = null;
	
	private URL url;
	
	public DownloadINSEESampleData(URL url, String encoding) {
		
		downloadFromURL(url);
		
		this.encoding = encoding;
		this.url = url;
	}
	
	/**
	 * Sets the zipfile. 
	 * @param url
	 */
	private void downloadFromURL(URL url) {

		zipFile = DataLocalRepository.getRepository().getOrDownloadResource(url,  "downloaded.zip");
		
	}
	
	/**
	 * Detects the filename which is likely an INSEE MOD file,
	 * or null if none. 
	 * This method is still dumb and should evolve later.
	 * @param zipFile
	 * @return
	 */
	protected String detectNameOfDictionaryFile(ZipFile zipFile) {

		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
	    while (zipEntries.hasMoreElements()) {
	        String fileName = ((ZipEntry) zipEntries.nextElement()).getName();
	        if (fileName.startsWith("MOD_") && fileName.endsWith(".txt")) {
	        	logger.debug("found dictionnary file: {}", fileName);
	        	return fileName;
	        }
	    }
	    
    	logger.warn("no dictionnary file: found in the zip file");

	    return null;
	}
	
	/**
	 * Reads the dictionnary (if not done yet) 
	 * and returns its content 
	 * @return
	 */
	public Collection<DemographicAttribute<? extends IValue>> getDictionnary() {
		
		if (this.dictionnary != null)
			return this.dictionnary;
		
		try {
			
			ZipFile zipFile = new ZipFile(this.zipFile);
		    
			String dictionnaryFilename = detectNameOfDictionaryFile(zipFile);
			
			if (dictionnaryFilename == null)
				throw new RuntimeException("We where not able to detect the MOD dictionnary in this zip file");
			
			// retrieve the stream for this file
			ZipEntry entry = zipFile.getEntry(dictionnaryFilename);

	        // unzip this file
	        File modFile = new File(
	        		DataLocalRepository.getRepository().getDirectoryForUrl(url), 
	        		entry.getName());
	        FileOutputStream fos = new FileOutputStream(modFile);
            int len;
            InputStream	zis = zipFile.getInputStream(entry);
            byte[] buffer = new byte[1024];
            while ((len = zis.read(buffer)) > 0) {
       		fos.write(buffer, 0, len);
            }
            fos.close();
            
	    	logger.debug("reading dictionnary...");
	        this.dictionnary = ReadINSEEDictionaryUtils.readDictionnaryFromMODFile(modFile, encoding);
	        
		    
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error while reading MOD file from ZIP",e);
		} 
		
		return this.dictionnary;
		
	}

	/**
	 * Detects the filename which is likely an INSEE MOD file,
	 * or null if none. 
	 * This method is still dumb and should evolve later.
	 * @param zipFile
	 * @return
	 */
	protected String detectNameOfSampleFile(ZipFile zipFile) {

		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
	    while (zipEntries.hasMoreElements()) {
	        String fileName = ((ZipEntry) zipEntries.nextElement()).getName();
	        if (	(fileName.startsWith("FD_") && fileName.endsWith(".txt")) 
	        		|| fileName.endsWith(".dbf")
	        		) {
	        	logger.debug("found sample file: {}", fileName);
	        	return fileName;
	        }
	    }
	    
    	logger.warn("no sample file found in the zip file");

	    return null;
	}
	
	public File getSampleFile() {

		if (fileSample != null)
			return fileSample;
		
		try {
			
			ZipFile zipFile = new ZipFile(this.zipFile);
		    
			String sampleFilename = detectNameOfSampleFile(zipFile);
			
			if (sampleFilename == null)
				throw new RuntimeException("We where not able to detect the MOD dictionnary in this zip file");
			
			// retrieve the stream for this file
			ZipEntry entry = zipFile.getEntry(sampleFilename);

	        File sampleFile = new File(
	        		DataLocalRepository.getRepository().getDirectoryForUrl(url), 
	        		entry.getName());
	        if (sampleFile.exists()) {
	        	this.fileSample = sampleFile;
	        	return fileSample;
	        }
	        
	        // unzip this file
	        FileOutputStream fos = new FileOutputStream(sampleFile);
            int len;
            InputStream	zis = zipFile.getInputStream(entry);
            byte[] buffer = new byte[1024];
            while ((len = zis.read(buffer)) > 0) {
       		fos.write(buffer, 0, len);
            }
            fos.close();
            
	    	this.fileSample = sampleFile;
		    
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error while reading MOD file from ZIP",e);
		} 
		
		return this.fileSample;
		
	}
	
	public GosplPopulation getSamplePopulation(int maxToRead) {
		
		File sampleFile = getSampleFile();

		GosplSurveyFactory gsf = new GosplSurveyFactory();

		IGSSurvey survey;
		try {
			survey = gsf.getSurvey(sampleFile, GSSurveyType.Sample);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		
		GosplPopulation pop = null;
		
		Set<DemographicAttribute<? extends IValue>> updatedAttributes = new HashSet<>(getDictionnary());

		try {
			//Map<String,String> keepOnlyEqual = new HashMap<>();
			//keepOnlyEqual.put("DEPT", "75");
			//keepOnlyEqual.put("NAT13", "Marocains");
			
			
			pop = GosplInputDataManager.getSample(
					survey, 
					updatedAttributes, 
					maxToRead,
					Collections.emptyMap() // TODO parameters for that
					);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			throw new RuntimeException(e);
		}
		
		return pop;
		
	}
}
