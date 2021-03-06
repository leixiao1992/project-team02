package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.StopWordRemover;
import util.TypeConstants;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.answer.CandidateAnswerVariant;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class TripleAnnotator extends JCasAnnotator_ImplBase {
	

	public GoPubMedService service=null;
	public LinkedLifeDataServiceResponse.Result linkedLifeDataResult = null;
	
	
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
		// TODO Auto-generated method stub
		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		
	    FSIterator<TOP> iter = aJCas.getJFSIndexRepository().getAllIndexedFS
	    		(AtomicQueryConcept.type);
	    
		// iterate
		if (iter.hasNext()) {

			// get the Question type
			AtomicQueryConcept a = (AtomicQueryConcept) iter.next();

			String docText = a.getText();
			String text = docText;

			System.out.println(text);
			
			/***************************************/
			try {
				linkedLifeDataResult = service.findLinkedLifeDataEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int rank = 1;

			//System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());

			for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
				//System.out.println(" > " + entity.getEntity());
				if(entity==null) break;
		
				LinkedLifeDataServiceResponse.Relation relation = entity.getRelations().get(0);
				Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(),
						relation.getObj());
				TripleSearchResult triple_result = TypeFactory.createTripleSearchResult(aJCas, triple);
				triple_result.setRank(rank);
				triple_result.setScore(entity.getScore());
				triple_result.addToIndexes(aJCas);
				rank++;
			}

			/************************************************************************/
			Collection<TripleSearchResult> cs = TypeUtil.getRankedTripleSearchResults(aJCas);

			Collection<TripleSearchResult> result = TypeUtil.rankedSearchResultsByScore(
					JCasUtil.select(aJCas, TripleSearchResult.class), cs.size());

			System.err.println("triple result size(in consumer):" + result.size());

			Iterator<TripleSearchResult> it = result.iterator();
			rank = 1;
			while (it.hasNext()) {
				TripleSearchResult csr = (TripleSearchResult) it.next();
				csr.setRank(rank);
				rank++;
				
			}
		}

	}

}
