package aiSafe.reportGeneration;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.domain.MaxAltitude;
import aiSafe.aircontrolarea.domain.MinFuelAtArrival;
import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportFactory;
import aiSafe.reportGeneration.domain.ReportHeader;
import aiSafe.reportGeneration.domain.ReportPeriod;
import aiSafe.reportGeneration.domain.ReportSection;
import aiSafe.reportGeneration.domain.ReportTitle;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.usermanagement.domain.Email;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public final class ReportTestFixtures {

    private ReportTestFixtures() {
    }

    public static Report sampleMonthlyReport() {
        return Report.create(
                ReportTitle.valueOf("Monthly Statistics – 2026-06 – LIS"),
                ReportPeriod.valueOf(YearMonth.of(2026, 6)),
                ReportHeader.valueOf(LocalDateTime.of(2026, 5, 31, 10, 0), "operator@company.com"),
                ReportType.MONTHLY_STATISTICS,
                new AreaCode("LIS"),
                List.of(
                        new ReportSection("Executive Summary",
                                """
                                Reporting period : 2026-06
                                Air Control Area : LIS
                                Total flights    : 2
                                Total simulations: 1
                                Safety violations: 3
                                Validation rate  : 50.0%
                                """),
                        new ReportSection("Flight Operations Statistics",
                                """
                                Total flights  : 2
                                Regular flights: 1
                                Charter flights: 1
                                """),
                        new ReportSection("Safety Violations Summary",
                                """
                                Total safety violations: 3
                                Most frequent violation outcomes:
                                  - FAILED : 1 simulation(s)
                                """),
                        new ReportSection("Validation Rate",
                                """
                                Validated flight plans : 1 / 2
                                Validation rate        : 50.0%
                                Assessment             : REQUIRES ATTENTION
                                """),
                        new ReportSection("Statistical Charts",
                                """
                                Flight type distribution
                                  Regular                [####################--------------------]    1
                                  Charter                [####################--------------------]    1
                                Simulation validation outcomes
                                  SUCCEEDED              [----------------------------------------]    0
                                  FAILED                 [########################################]    1
                                  ABORTED                [----------------------------------------]    0
                                Validation rate overview: 50.0%
                                  Validated              [####################--------------------]   50
                                  Remaining              [####################--------------------]   50
                                Safety violations intensity (affected simulations)
                                  With violations        [########################################]    1
                                  Without violations     [----------------------------------------]    0
                                Total violation events: 3
                                """))
        );
    }

    public static Report minimalMonthlyReport() {
        return Report.create(
                ReportTitle.valueOf("Monthly Statistics – 2026-01 – LPPT"),
                ReportPeriod.valueOf(YearMonth.of(2026, 1)),
                ReportHeader.valueOf(LocalDateTime.of(2026, 1, 15, 9, 0), "fco@aisafe.local"),
                ReportType.MONTHLY_STATISTICS,
                new AreaCode("LPPT"),
                List.of(
                        new ReportSection("Executive Summary",
                                """
                                Total flights    : 0
                                Total simulations: 0
                                Safety violations: 0
                                Validation rate  : 0.0%
                                """),
                        new ReportSection("Flight Operations Statistics", "Total flights  : 0"),
                        new ReportSection("Safety Violations Summary", "Total safety violations: 0"),
                        new ReportSection("Validation Rate",
                                """
                                Validated flight plans : 0 / 0
                                Validation rate        : 0.0%
                                """),
                        new ReportSection("Statistical Charts",
                                """
                                Flight type distribution
                                Validation rate overview: 0.0%
                                Safety violations intensity (affected simulations)
                                  With violations        [----------------------------------------]    0
                                  Without violations     [----------------------------------------]    0
                                """))
        );
    }

    public static ReportFactory monthlyReportFactory() {
        return new ReportFactory(Map.of(
                ReportType.MONTHLY_STATISTICS,
                (period, areaCode) -> sampleMonthlyReport().sections()));
    }

    public static Email operatorEmail() {
        return new Email("operator@company.com");
    }

    public static AirControlArea sampleArea() {
        return new AirControlArea(
                new AreaCode("LIS"),
                new AreaName("Lisbon Control Area"),
                new MinFuelAtArrival(100.0),
                new MaxAltitude(14000),
                new GeoBoundary(List.of(
                        new Coordinate(0, 0),
                        new Coordinate(1, 0),
                        new Coordinate(1, 1),
                        new Coordinate(0, 1))));
    }
}
