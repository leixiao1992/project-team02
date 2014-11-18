package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.StopWordRemover;
import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {
	
GoPubMedService service=null;
	
	
	public void initialize(UimaContext aContext) throws ResourceInitializationException{
		super.initialize(aContext);

		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  }
	

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();


		// iterate
		if (iter.isValid()) {

			// get the Question type
			Question a = (Question) iter.get();

			String docText = a.getText();
			String text = docText.replace("?", "");

			System.out.println(text);
			
/************************************************************************/
/*			
			String queText=text;
			String stemmedQue = StanfordLemmatizer.stemText(queText);
			
			
			// remove stop words by StopWordRemover
			StopWordRemover stopWordRemover = StopWordRemover.getInstance();
			String StopRemovedQue = stopWordRemover.removeStopWords(stemmedQue);
			
			text=stemmedQue;
*/
			/************************************************************************/
			
	/*		GoPubMedService service = null;

			
			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	*/		
			/********************************************************************/
			
			PubMedSearchServiceResponse.Result pubmedResult = null;
			try {
				pubmedResult = service.findPubMedCitations(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
	          
	          int rank=1;
		    System.out.println(pubmedResult.getSize());
		    for(PubMedSearchServiceResponse.Document documents : pubmedResult.getDocuments()){
		    	// System.out.println(" >>>>>>>>>>>>>> " + documents.getPmid());
		    	String m="http://www.ncbi.nlm.nih.gov/pubmed/"+documents.getPmid();
				 Document doc=TypeFactory.createDocument(aJCas,m,"xxxx",rank,"cccc",documents.getPmid(),documents.getPmid());
				 rank++;
				 doc.addToIndexes();
			
		    }
			
			//System.out.println("***********");

			iter.moveToNext();

		}

	}
}
