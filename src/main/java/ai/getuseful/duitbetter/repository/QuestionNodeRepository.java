package ai.getuseful.duitbetter.repository;

import ai.getuseful.duitbetter.entities.QuestionNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionNodeRepository extends PagingAndSortingRepository<QuestionNode, UUID> {

    Page<QuestionNode> findByEmbeddingIsNull(Pageable page);
    QuestionNode findById(UUID id);
    List<QuestionNode> findDistinctByIdIn(List<UUID> uuids);
@Query(value = "MATCH (q:Question) where q.textEmbedding3Small is null and size(q.text) >0 return q skip $skip limit $limit",
            countQuery = "MATCH (q:Question) where q.textEmbedding3Small is null and size(q.text) > 0 return count(q) ")
    Page<QuestionNode> findByTextEmbedding3SmallIsNull(Pageable page);


}
