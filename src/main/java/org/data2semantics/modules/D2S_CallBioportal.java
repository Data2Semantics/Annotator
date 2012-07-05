package org.data2semantics.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.data2semantics.annotation.D2S_BioportalClient;
import org.data2semantics.util.D2S_Utils;
import org.data2semantics.util.D2S_Vocab;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author wibisono Class that will call the bioportal client, to provide
 *         annotations in xml format. Works on the snapshot that is already
 *         generated using D2S_CreateSnapshots.
 * 
 */
public class D2S_CallBioportal extends D2S_AbstractModule {

	private Logger log = LoggerFactory.getLogger(D2S_CallBioportal.class);

	String DEFAULT_SNAPSHOT_DIRECTORY = "results/snapshots";
	String DEFAULT_BIOPORTAL_DIRECTORY = "results/bioportal";
	String BIOPORTAL_DIRECTORY;
	String snapshotTimestamp = "";
	String timestamp;
	D2S_Vocab vocab;

	D2S_CallBioportal(Repository repo, URI graph, URI resource) {
		super(repo, graph, resource);

		SimpleDateFormat sdf = D2S_Utils.getSimpleDateFormat();
		timestamp = sdf.format(new Date());

		vocab = new D2S_Vocab(repo.getValueFactory());

		try {
			RepositoryConnection con = repo.getConnection();

			try {
				RepositoryResult<Statement> bioportalIterator = con
						.getStatements(resource,
								vocab.d2s("bioportalDirectory"), null, true);
				// Set bioportal_dir to the default value
				String bioportalDirectoryName = DEFAULT_BIOPORTAL_DIRECTORY;

				while (bioportalIterator.hasNext()) {
					Statement s = bioportalIterator.next();

					bioportalDirectoryName = s.getObject().stringValue();

					// We only need one bioportal_dir location
					break;
				}

				BIOPORTAL_DIRECTORY = bioportalDirectoryName + "/" + timestamp.replaceAll(":","-");

				log.info("Annotations will be stored in " + BIOPORTAL_DIRECTORY);

			} finally {
				con.close();
			}

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check if the bioportal directory exists, if not, create one
		File bioportal_directory = new File(BIOPORTAL_DIRECTORY);
		if (!bioportal_directory.exists()) {
			bioportal_directory.mkdirs();
			log.info("Created directory " + BIOPORTAL_DIRECTORY);
		}
	}


	public Repository start() {

		try {
			RepositoryConnection con = repo.getConnection();
			ValueFactory vf = repo.getValueFactory();

			try {
				RepositoryResult<Statement> documentIterator = con
						.getStatements(resource, vocab.d2s("resource"), null,
								true);

				while (documentIterator.hasNext()) {
					String cacheFileName = null;
					Statement docStatement = documentIterator.next();
					URI documentURI = (URI) docStatement.getObject();
					log.info("Going to annotate document "
							+ documentURI.stringValue());

					RepositoryResult<Statement> cacheIterator = con
							.getStatements(documentURI, vocab.d2s("hasCache"),
									null, true);

					Resource latestCacheResource = D2S_Utils.getLatest(con,
							cacheIterator, vocab.d2s("cacheTime"));

					if (latestCacheResource != null) {
						RepositoryResult<Statement> cacheLocationIterator = con
								.getStatements(latestCacheResource,
										vocab.d2s("cacheLocation"), null, true);


						while (cacheLocationIterator.hasNext()) {
							Statement cacheLocationStatement = cacheLocationIterator
									.next();

							cacheFileName = cacheLocationStatement.getObject()
									.stringValue();

							// We only need one cache file
							break;

						}


						log.info("Cache location found at " + cacheFileName);
						try {
							String annotationsFileName = process(documentURI,
									cacheFileName);
	
							BNode annotationBNode = vf.createBNode();
							Statement annotationStatement = vf.createStatement(
									documentURI, vocab.d2s("hasAnnotation"),
									annotationBNode);
							Statement annotationLocationStatement = vf.createStatement(
									annotationBNode, vocab.d2s("annotationLocation"),
									vf.createLiteral(annotationsFileName,
											XMLSchema.STRING));
							
							Statement annotationTimeStatement = vf.createStatement(
									annotationBNode, vocab.d2s("annotationTime"),
									vf.createLiteral(timestamp,
											XMLSchema.DATETIME));
							
							Statement annotationSourceStatement = vf.createStatement(annotationBNode, vocab.d2s("annotationSource"), latestCacheResource);
	
							con.add(annotationStatement);
							con.add(annotationLocationStatement);
							con.add(annotationTimeStatement);
							con.add(annotationSourceStatement);
	
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						log.warn("I can't annotate this file: No cached copy for "+ documentURI.stringValue());
					}
					
				}

			} finally {
				con.close();
			}

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return repo;

	}

	private String process(URI documentURI, String cacheFileName)
			throws FileNotFoundException, UnsupportedEncodingException,
			InterruptedException {

		String outputFilePath = BIOPORTAL_DIRECTORY + "/"
				+ URLEncoder.encode(documentURI.stringValue(), "utf-8")
				+ ".xml";
		;

		// IF this file already created, move on.
		if (new File(outputFilePath).exists()) {
			log.info("File " + outputFilePath + " already exists!");
			return outputFilePath;
		}

		File cacheFile = new File(cacheFileName);
		Scanner fileScanner = new Scanner(new FileInputStream(cacheFile));

		StringBuilder stringBuilder = new StringBuilder();
		D2S_BioportalClient client = new D2S_BioportalClient();

		try {
			log.info("Reading cache file " + cacheFileName);
			while (fileScanner.hasNextLine()) {
				stringBuilder.append(fileScanner.nextLine() + "\n");
			}
			log.info("Done");
			String textToAnnotate = Jsoup.clean(stringBuilder.toString(),
					Whitelist.none());
			log.info("Starting annotator. Number of characters in text to annotate: "+textToAnnotate.length());
			client.annotateToFile(textToAnnotate, "xml", new File(
					outputFilePath));
			log.info("Wrote annotations to " + outputFilePath);
		} finally {
			fileScanner.close();
		}

		return outputFilePath;

	}



}
