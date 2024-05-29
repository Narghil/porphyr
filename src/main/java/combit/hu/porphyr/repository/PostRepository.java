package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.PostEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<PostEntity, Long> {
    @Nullable
    PostEntity findAllById(final @NonNull Long id);

    @NonNull
    List<PostEntity> findAllByProjectTaskEntityOrderByCreatedDesc(final @NonNull ProjectTaskEntity projectTaskEntity);

    void saveAndFlush(final @NonNull PostEntity postEntity);
}
