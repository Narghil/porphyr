package combit.hu.porphyr.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "DEVELOPERS")
@Data
public class DeveloperEntity {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private @Nullable Long id;
    @Column
    private @NonNull String name;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "developerEntity")
    @JsonBackReference
    private List<ProjectDevelopersEntity> developerProjects;

    public DeveloperEntity(){
        this.id = null;
        this.name = "";
    }

    public DeveloperEntity(final @NonNull String name) {
        this.id = null;
        this.name = name;
    }
}
