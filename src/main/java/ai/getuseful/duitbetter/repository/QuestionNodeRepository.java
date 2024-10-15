package ai.getuseful.duitbetter.repository;

import ai.getuseful.duitbetter.entities.QuestionNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionNodeRepository extends PagingAndSortingRepository<QuestionNode, UUID> {

    Page<QuestionNode> findByEmbeddingIsNull(Pageable page);
    QuestionNode findById(UUID id);
    List<QuestionNode> findDistinctByIdIn(List<UUID> uuids);

}
