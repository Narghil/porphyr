package combit.hu.porphyr.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "DEVELOPERS")
@Data
@NoArgsConstructor
public class DeveloperEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull String name;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "developerEntity")
    private List<ProjectDevelopersEntity> developers;

    public DeveloperEntity(final @NonNull String name) {
        this.name = name;
    }
}
