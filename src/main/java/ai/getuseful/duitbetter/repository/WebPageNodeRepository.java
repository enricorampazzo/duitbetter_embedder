package ai.getuseful.duitbetter.repository;

import ai.getuseful.duitbetter.entities.WebPageNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WebPageNodeRepository extends PagingAndSortingRepository<WebPageNode, String> {

//    Page<WebPageNode> findByCleanedTextIsNotNullAndEmbeddingIsNull(Pageable pageable);
    WebPageNode findByUrl(String url);

    WebPageNode save(WebPageNode webPageWithoutEmbedding);
}
