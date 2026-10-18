package aiSafe.weatherdata.importer;

import java.io.InputStream;
import java.util.List;

public interface WeatherDataImporter {
    List<WeatherDataImportRow> importFrom(InputStream source);
    String formatName();
}