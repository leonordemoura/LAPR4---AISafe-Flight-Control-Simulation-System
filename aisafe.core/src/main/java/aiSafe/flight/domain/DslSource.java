package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;

import java.io.Serializable;
import java.util.Objects;

@jakarta.persistence.Embeddable
public class DslSource implements ValueObject, Serializable {

    @Lob
    @Column(name = "dsl_content", nullable = false)
    private String content;

    protected DslSource() {
        // for ORM
    }

    public DslSource(final String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("DSL content is required.");
        }
        this.content = content.trim();
    }

    public String content() {
        return content;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DslSource that)) return false;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
