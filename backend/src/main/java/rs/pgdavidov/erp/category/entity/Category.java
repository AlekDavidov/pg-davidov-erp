package rs.pgdavidov.erp.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rs.pgdavidov.erp.common.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;

    @Column(nullable = false)
    private boolean active = true;
}