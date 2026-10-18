package aiSafe.reportGeneration.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class ReportValueObjectsTest {

    @Test
    void reportSection_equalsAndHashCode() throws Exception {
        final ReportSection a = new ReportSection("Title", "body");
        final ReportSection b = new ReportSection("Title", "body");
        final ReportSection c = new ReportSection("Other", "body");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
        assertTrue(a.toString().contains("Title"));

        final ReportSection orm = newOrmInstance(ReportSection.class);
        setField(orm, "title", null);
        setField(orm, "content", "body");
        assertNotEquals(a, orm);
    }

    @Test
    void reportTitle_equalsAndOrmConstructor() throws Exception {
        final ReportTitle title = ReportTitle.valueOf("Monthly");
        assertEquals(title, ReportTitle.valueOf("Monthly"));
        assertNotEquals(title, ReportTitle.valueOf("Other"));
        assertTrue(title.title().equals("Monthly"));

        final ReportTitle orm = newOrmInstance(ReportTitle.class);
        assertNotNull(orm);
    }

    @Test
    void reportPeriod_equalsAndOrmConstructor() throws Exception {
        final YearMonth ym = YearMonth.of(2026, 3);
        final ReportPeriod period = ReportPeriod.valueOf(ym);
        assertEquals(period, ReportPeriod.valueOf(ym));
        assertEquals("2026-03", period.toString());
        assertNotEquals(period, ReportPeriod.valueOf(YearMonth.of(2026, 4)));

        assertNotNull(newOrmInstance(ReportPeriod.class));
    }

    @Test
    void reportHeader_equalsAndOrmConstructor() throws Exception {
        final LocalDateTime at = LocalDateTime.of(2026, 1, 1, 8, 0);
        final ReportHeader header = ReportHeader.valueOf(at, "user@test.com");
        assertEquals(header, ReportHeader.valueOf(at, "user@test.com"));
        assertNotEquals(header, ReportHeader.valueOf(at, "other@test.com"));

        final ReportHeader orm = newOrmInstance(ReportHeader.class);
        assertNotNull(orm);
    }

    @Test
    void report_aggregateIdentityMethods() {
        final Report report = Report.create(
                ReportTitle.valueOf("T"),
                ReportPeriod.valueOf(YearMonth.of(2026, 1)),
                ReportHeader.valueOf(LocalDateTime.now(), "u@test.com"),
                ReportType.MONTHLY_STATISTICS,
                new aiSafe.aircontrolarea.domain.AreaCode("LIS"),
                java.util.List.of(new ReportSection("S", "C")));

        assertNull(report.identity());
        assertEquals(0, report.hashCode());
        assertFalse(report.equals(Report.create(
                ReportTitle.valueOf("T"),
                ReportPeriod.valueOf(YearMonth.of(2026, 1)),
                ReportHeader.valueOf(LocalDateTime.now(), "u@test.com"),
                ReportType.MONTHLY_STATISTICS,
                new aiSafe.aircontrolarea.domain.AreaCode("LIS"),
                java.util.List.of(new ReportSection("S", "C")))));
        assertFalse(report.sameAs(null));
    }

    private static <T> T newOrmInstance(final Class<T> type) throws Exception {
        final Constructor<T> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private static void setField(final Object target, final String name, final Object value)
            throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
