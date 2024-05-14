package combit.hu.porphyr.config.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Szerepkörökhöz tartozó engedélyek nyilvántartása <br />
 * <br />
 * Mezők: <br />
 * {@code - id:} &#9;&#9; Egyedi azonosító <br />
 * {@code - name:} &#9;&#9; Engedély egyedi neve <br />
 * {@code - description:} &#9; Engedély leírása (egyedi) <br />
 * <br />
 *
 * @see RoleEntity
 */

@Entity
@Table(name = "permits")
@Data
@NoArgsConstructor
public class PermitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    private @NonNull String name;

    @Column
    private @NonNull String description;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany()
    @JoinTable(
        name = "roles_permits",
        joinColumns = {@JoinColumn(name = "permit_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Override
    public String toString() {
        return "Permit [id=" + id + ", name=" + name + ", description=" + description + "]";
    }

    public PermitEntity(final @NonNull String permitName, final @NonNull String description ) {
        this.name = permitName;
        this.description = description;
    }
}
