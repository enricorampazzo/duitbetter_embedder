package ai.getuseful.duitbetter.repository;

import ai.getuseful.duitbetter.entities.WebPageNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface WebPageNodeRepository extends PagingAndSortingRepository<WebPageNode, String> {


    WebPageNode findByUrl(String url);

    @Query(value = """
            MATCH (wp:WebPage) where wp.cleanedText contains '?' and
            not exists ((wp)-[:HAS_QUESTION]->()) and
            not wp.cleanedText contains "Blu can answer your questions Get 24/7 support from our online chatbot Blu."
            return wp SKIP $skip LIMIT $limit""",

            countQuery = """
              MATCH (wp:WebPage) where wp.cleanedText contains '?' and
              not wp.cleanedText contains "Blu can answer your questions Get 24/7 support from our online chatbot Blu."
              and not exists ((wp)-[:HAS_QUESTION]->()) return count(wp)
            """)
    Page<WebPageNode> findWebPagesWithoutQuestionsAndAnswers(Pageable page);

    WebPageNode save(WebPageNode webPageNode);
}
