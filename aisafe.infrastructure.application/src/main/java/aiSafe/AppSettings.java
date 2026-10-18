package aiSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AppSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettings.class);

    private static final String PROPERTIES_RESOURCE = "application.properties";
    private static final String REPOSITORY_FACTORY_KEY = "persistence.repositoryFactory";
    private static final String UI_MENU_LAYOUT_KEY = "ui.menu.layout";
    private static final String PERSISTENCE_UNIT_KEY = "persistence.persistenceUnit";
    private static final String SCHEMA_GENERATION_KEY = "jakarta.persistence.schema-generation.database.action";
    private static final String AIR_CONTROL_AREA_MAX_ALTITUDE_KEY = "aircontrolarea.maxAltitudeMeters";

    private final Properties applicationProperties = new Properties();

    public AppSettings() {
        loadProperties();
    }

    private void loadProperties() {
        try (var propertiesStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (propertiesStream == null) {
                throw new FileNotFoundException("property file '" + PROPERTIES_RESOURCE + "' not found in the classpath");
            }
            this.applicationProperties.load(propertiesStream);
            applyMissingDefaultProperties();
        } catch (final IOException exio) {
            setDefaultProperties();
            LOGGER.warn("Loading default properties", exio);
        }
    }

    private void applyMissingDefaultProperties() {
        this.applicationProperties.putIfAbsent(REPOSITORY_FACTORY_KEY, "aiSafe.persistence.impl.jpa.JpaRepositoryFactory");
        this.applicationProperties.putIfAbsent(UI_MENU_LAYOUT_KEY, "horizontal");
        this.applicationProperties.putIfAbsent(PERSISTENCE_UNIT_KEY, "AI_SAFE");
        this.applicationProperties.putIfAbsent(SCHEMA_GENERATION_KEY, "create");
        this.applicationProperties.putIfAbsent(AIR_CONTROL_AREA_MAX_ALTITUDE_KEY, "14000");
    }

    private void setDefaultProperties() {
        this.applicationProperties.setProperty(REPOSITORY_FACTORY_KEY, "aiSafe.persistence.impl.jpa.JpaRepositoryFactory");
        this.applicationProperties.setProperty(UI_MENU_LAYOUT_KEY, "horizontal");
        this.applicationProperties.setProperty(PERSISTENCE_UNIT_KEY, "AI_SAFE");
        this.applicationProperties.setProperty(SCHEMA_GENERATION_KEY, "create");
        this.applicationProperties.setProperty(AIR_CONTROL_AREA_MAX_ALTITUDE_KEY, "14000");
    }

    public Boolean isMenuLayoutHorizontal() {
        return "horizontal".equalsIgnoreCase(this.applicationProperties.getProperty(UI_MENU_LAYOUT_KEY));
    }

    public String getPersistenceUnitName() {
        return this.applicationProperties.getProperty(PERSISTENCE_UNIT_KEY);
    }

    public String getRepositoryFactory() {
        return this.applicationProperties.getProperty(REPOSITORY_FACTORY_KEY);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map getExtendedPersistenceProperties() {
        final Map ret = new HashMap();
        ret.put(SCHEMA_GENERATION_KEY, this.applicationProperties.getProperty(SCHEMA_GENERATION_KEY));
        return ret;
    }

    public String getProperty(final String prop) {
        return this.applicationProperties.getProperty(prop);
    }
}
