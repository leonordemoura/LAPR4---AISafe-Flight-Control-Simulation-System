grammar FlightDSL;

flightPlan
    : flight EOF
    ;

flight
    : FLIGHT FLIGHT_DESIGNATOR LBRACE
        typeDecl
        leg+
      RBRACE
    ;

typeDecl
    : TYPE flightType
    ;

flightType
    : REGULAR #RegularType
    | CHARTER #CharterType
    ;

leg
    : LEG LBRACE
        departure
        arrival
        route
        segment+
        fuel
      RBRACE
    ;

departure
    : DEPARTURE airportCode date time
    ;

arrival
    : ARRIVAL airportCode date time
    ;

route
    : ROUTE LBRACE
        origin
        destination
      RBRACE
    ;

origin
    : ORIGIN airportCode
    ;

destination
    : DESTINATION airportCode
    ;

segment
    : SEGMENT LBRACE
        START coordinates
        END coordinates
        ALTITUDE NUMBER UNIT_M
        WIDTH NUMBER UNIT_KM
        WIND NUMBER DEG NUMBER windUnit
      RBRACE
    ;

fuel
    : FUEL NUMBER fuelUnit
    ;

fuelUnit
    : UNIT_KG
    | UNIT_L
    ;

windUnit
    : UNIT_MPS
    | UNIT_KMH
    | UNIT_KT
    ;

coordinates
    : LPAREN NUMBER COMMA NUMBER RPAREN
    ;

airportCode
    : AIRPORT_CODE
    ;

date
    : DATE
    ;

time
    : TIME
    ;

/* ---------------- LEXER ---------------- */

FLIGHT     : [fF][lL][iI][gG][hH][tT];
TYPE       : [tT][yY][pP][eE];
LEG        : [lL][eE][gG];
DEPARTURE  : [dD][eE][pP][aA][rR][tT][uU][rR][eE];
ARRIVAL    : [aA][rR][rR][iI][vV][aA][lL];
ROUTE      : [rR][oO][uU][tT][eE];
SEGMENT    : [sS][eE][gG][mM][eE][nN][tT];
FUEL       : [fF][uU][eE][lL];
ORIGIN     : [oO][rR][iI][gG][iI][nN];
DESTINATION: [dD][eE][sS][tT][iI][nN][aA][tT][iI][oO][nN];
ALTITUDE   : [aA][lL][tT][iI][tT][uU][dD][eE];
WIDTH      : [wW][iI][dD][tT][hH];
WIND       : [wW][iI][nN][dD];
START      : [sS][tT][aA][rR][tT];
END        : [eE][nN][dD];

REGULAR    : [rR][eE][gG][uU][lL][aA][rR];
CHARTER    : [cC][hH][aA][rR][tT][eE][rR];

UNIT_KG    : 'kg';
UNIT_L     : 'l';
UNIT_M     : 'm';
UNIT_KM    : 'km';
DEG        : 'deg';

UNIT_MPS   : 'mps';
UNIT_KMH   : 'kmh';
UNIT_KT    : 'kt';

LBRACE     : '{';
RBRACE     : '}';
LPAREN     : '(';
RPAREN     : ')';
COMMA      : ',';

fragment DIGIT : [0-9];

DATE
    : DIGIT DIGIT DIGIT DIGIT '-'
      DIGIT DIGIT '-'
      DIGIT DIGIT
    ;

TIME
    : DIGIT DIGIT ':'
      DIGIT DIGIT
    ;

AIRPORT_CODE
    : [A-Z][A-Z][A-Z]
    | [A-Z][A-Z][A-Z][A-Z]
    ;

FLIGHT_DESIGNATOR
    : [A-Z][A-Z][0-9][0-9]?[0-9]?[0-9]?[A-Z]?
    ;

NUMBER
    : '-'? [0-9]+ ('.' [0-9]+)?
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
