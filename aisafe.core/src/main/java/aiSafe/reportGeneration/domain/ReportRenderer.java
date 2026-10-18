package aiSafe.reportGeneration.domain;

/**
 * Interface responsible for rendering a fully-assembled {@link Report}
 * to a persistent file in the requested output directory.
 *
 * <p>Concrete implementations (adapters) decide the actual file format
 * (plain-text, PDF, HTML, …). The domain layer depends only on this interface,
 * keeping formatting concerns outside the domain model.</p>
 */
public interface ReportRenderer {

    /**
     * Renders the given report and writes it to {@code outputDirectory}.
     *
     * @param report          the fully-assembled report to render (never {@code null})
     * @param outputDirectory the filesystem path of the target directory (never {@code null})
     * @return the absolute path of the file that was written
     * @throws IllegalArgumentException if the file cannot be created or written
     */
    String render(Report report, String outputDirectory);
}
