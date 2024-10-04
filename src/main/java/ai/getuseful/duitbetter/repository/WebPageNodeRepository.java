package ai.getuseful.duitbetter.repository;

import ai.getuseful.duitbetter.entities.WebPageNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WebPageNodeRepository extends PagingAndSortingRepository<WebPageNode, String> {

//    Page<WebPageNode> findByCleanedTextIsNotNullAndEmbeddingIsNull(Pageable pageable);
    WebPageNode findByUrl(String url);

    @Query(value = "MATCH (wp:WebPage) where wp.cleanedText contains '?' and " +
            "not exists ((wp)-[:HAS_QUESTION]->()) return wp as webpage SKIP $skip LIMIT $limit",
            countQuery = "MATCH (wp:WebPage) where wp.cleanedText contains '?' and not exists ((wp)-[:HAS_QUESTION]->()) return count(wp)")
    Page<WebPageNode> findWebPagesWithoutQuestionsAndAnswers(Pageable page);

    WebPageNode save(WebPageNode webPageNode);
}
