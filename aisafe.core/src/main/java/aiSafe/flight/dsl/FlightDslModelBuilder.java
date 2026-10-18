package aiSafe.flight.dsl;

import aiSafe.flight.domain.FlightType;
import aiSafe.flight.dsl.model.DslCoordinates;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.dsl.model.DslFuel;
import aiSafe.flight.dsl.model.DslLeg;
import aiSafe.flight.dsl.model.DslRoute;
import aiSafe.flight.dsl.model.DslSchedulePoint;
import aiSafe.flight.dsl.model.DslSegment;

import java.util.ArrayList;
import java.util.List;

final class FlightDslModelBuilder extends aiSafe.flight.dsl.FlightDSLBaseVisitor<Object> {

    @Override
    public DslFlightPlan visitFlightPlan(final aiSafe.flight.dsl.FlightDSLParser.FlightPlanContext ctx) {
        return visitFlight(ctx.flight());
    }

    @Override
    public DslFlightPlan visitFlight(final aiSafe.flight.dsl.FlightDSLParser.FlightContext ctx) {
        final String identifier = ctx.FLIGHT_DESIGNATOR().getText();
        final FlightType flightType = (FlightType) visit(ctx.typeDecl());
        final List<DslLeg> legs = new ArrayList<>();
        for (final aiSafe.flight.dsl.FlightDSLParser.LegContext legContext : ctx.leg()) {
            legs.add(visitLeg(legContext));
        }
        return new DslFlightPlan(identifier, flightType, legs, ctx.getStart().getLine());
    }

    @Override
    public FlightType visitTypeDecl(final aiSafe.flight.dsl.FlightDSLParser.TypeDeclContext ctx) {
        return (FlightType) visit(ctx.flightType());
    }

    @Override
    public FlightType visitRegularType(final aiSafe.flight.dsl.FlightDSLParser.RegularTypeContext ctx) {
        return FlightType.REGULAR;
    }

    @Override
    public FlightType visitCharterType(final aiSafe.flight.dsl.FlightDSLParser.CharterTypeContext ctx) {
        return FlightType.CHARTER;
    }

    @Override
    public DslLeg visitLeg(final aiSafe.flight.dsl.FlightDSLParser.LegContext ctx) {
        final List<DslSegment> segments = new ArrayList<>();
        for (final aiSafe.flight.dsl.FlightDSLParser.SegmentContext segmentContext : ctx.segment()) {
            segments.add(visitSegment(segmentContext));
        }
        return new DslLeg(
                visitDeparture(ctx.departure()),
                visitArrival(ctx.arrival()),
                visitRoute(ctx.route()),
                segments,
                visitFuel(ctx.fuel()),
                ctx.getStart().getLine());
    }

    @Override
    public DslSchedulePoint visitDeparture(final aiSafe.flight.dsl.FlightDSLParser.DepartureContext ctx) {
        return schedulePoint(ctx.airportCode(), ctx.date(), ctx.time(), ctx);
    }

    @Override
    public DslSchedulePoint visitArrival(final aiSafe.flight.dsl.FlightDSLParser.ArrivalContext ctx) {
        return schedulePoint(ctx.airportCode(), ctx.date(), ctx.time(), ctx);
    }

    @Override
    public DslRoute visitRoute(final aiSafe.flight.dsl.FlightDSLParser.RouteContext ctx) {
        return new DslRoute(
                ctx.origin().airportCode().AIRPORT_CODE().getText(),
                ctx.destination().airportCode().AIRPORT_CODE().getText(),
                ctx.getStart().getLine());
    }

    @Override
    public DslSegment visitSegment(final aiSafe.flight.dsl.FlightDSLParser.SegmentContext ctx) {
        final List<aiSafe.flight.dsl.FlightDSLParser.CoordinatesContext> coordinates = ctx.coordinates();
        final List<org.antlr.v4.runtime.tree.TerminalNode> numbers = ctx.NUMBER();

        final double altitude = Double.parseDouble(numbers.get(0).getText());
        final double width = Double.parseDouble(numbers.get(1).getText());
        final double windDirection = Double.parseDouble(numbers.get(2).getText());
        final double windSpeed = Double.parseDouble(numbers.get(3).getText());

        final String windUnit = visitWindUnit(ctx.windUnit());

        return new DslSegment(
                visitCoordinates(coordinates.get(0)),
                visitCoordinates(coordinates.get(1)),
                altitude,
                width,
                windDirection,
                windSpeed,
                windUnit,
                ctx.getStart().getLine());
    }

    @Override
    public String visitWindUnit(final aiSafe.flight.dsl.FlightDSLParser.WindUnitContext ctx) {
        if (ctx.UNIT_MPS() != null) return "mps";
        if (ctx.UNIT_KMH() != null) return "kmh";
        return "kt";
    }

    @Override
    public DslCoordinates visitCoordinates(final aiSafe.flight.dsl.FlightDSLParser.CoordinatesContext ctx) {
        final List<org.antlr.v4.runtime.tree.TerminalNode> numbers = ctx.NUMBER();
        return new DslCoordinates(
                Double.parseDouble(numbers.get(0).getText()),
                Double.parseDouble(numbers.get(1).getText()));
    }

    @Override
    public DslFuel visitFuel(final aiSafe.flight.dsl.FlightDSLParser.FuelContext ctx) {
        final String unit = ctx.fuelUnit().UNIT_KG() != null ? "kg" : "l";
        return new DslFuel(
                Double.parseDouble(ctx.NUMBER().getText()),
                unit,
                ctx.getStart().getLine());
    }

    private DslSchedulePoint schedulePoint(
            final aiSafe.flight.dsl.FlightDSLParser.AirportCodeContext airportCode,
            final aiSafe.flight.dsl.FlightDSLParser.DateContext date,
            final aiSafe.flight.dsl.FlightDSLParser.TimeContext time,
            final org.antlr.v4.runtime.ParserRuleContext ctx) {
        return new DslSchedulePoint(
                airportCode.AIRPORT_CODE().getText(),
                date.DATE().getText(),
                time.TIME().getText(),
                ctx.getStart().getLine());
    }
}
