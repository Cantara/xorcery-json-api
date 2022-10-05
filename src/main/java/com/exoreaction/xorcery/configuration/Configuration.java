package com.exoreaction.xorcery.configuration;

import com.exoreaction.xorcery.json.JsonElement;
import com.exoreaction.xorcery.json.JsonMerger;
import com.exoreaction.xorcery.json.VariableResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author rickardoberg
 * @since 20/04/2022
 */
public final class Configuration
        implements JsonElement {
    private final ObjectNode json;

    Configuration(ObjectNode json) {
        this.json = json;
    }

    public ObjectNode json() {
        return json;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Configuration) obj;
        return Objects.equals(this.json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hash(json);
    }

    @Override
    public String toString() {
        return "Configuration[" +
                "json=" + json + ']';
    }

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public static Configuration empty() {
        return new Configuration(JsonNodeFactory.instance.objectNode());
    }

    public static final class Builder {
        private final ObjectNode builder;

        public Builder(ObjectNode builder) {
            this.builder = builder;
        }

            public static Builder load(File configFile)
                    throws IOException {
                Builder builder = new Builder();

                // Load system properties and environment variables
                builder.addSystemProperties("SYSTEM");
                builder.addEnvironmentVariables("ENV");

                // Load Xorcery defaults
                {
                    URL resource = Configuration.class.getClassLoader().getResource("META-INF/xorcery-defaults.yaml");
                    try (InputStream in = resource.openStream()) {
                        builder = builder.addYaml(in);
                        logger.info("Loaded " + resource);
                    }
                }

                // Load extensions
                Enumeration<URL> extensionConfigurationURLs = Configuration.class.getClassLoader().getResources("META-INF/xorcery.yaml");
                while (extensionConfigurationURLs.hasMoreElements()) {
                    URL resource = extensionConfigurationURLs.nextElement();
                    try (InputStream configurationStream = resource.openStream()) {
                        builder = builder.addYaml(configurationStream);
                        logger.info("Loaded " + resource);
                    }
                }

                // Load custom
                Enumeration<URL> configurationURLs = Configuration.class.getClassLoader().getResources("xorcery.yaml");
                while (configurationURLs.hasMoreElements()) {
                    URL resource = configurationURLs.nextElement();
                    try (InputStream configurationStream = resource.openStream()) {
                        builder = builder.addYaml(configurationStream);
                        logger.info("Loaded " + resource);
                    }
                }

                // Load user directory overrides
                File overridesYamlFile = new File(System.getProperty("user.dir"), "xorcery.yaml");
                if (overridesYamlFile.exists()) {
                    FileInputStream overridesYamlStream = new FileInputStream(overridesYamlFile);
                    builder = builder.addYaml(overridesYamlStream);
                    logger.info("Loaded " + overridesYamlFile);
                }

                // Load specified overrides
                if (configFile != null) {

                    if (configFile.getName().endsWith("yaml") || configFile.getName().endsWith("yml")) {
                        builder = builder.addYaml(new FileInputStream(configFile));
                        logger.info("Loaded " + configFile);
                    } else if (configFile.getName().endsWith("properties")) {
                        builder = builder.addProperties(new FileInputStream(configFile));
                        logger.info("Loaded " + configFile);
                    } else {
                        logger.warn("Unknown configuration filetype: " + configFile);
                    }
                }

                // Load user home overrides
                File userYamlFile = new File(System.getProperty("user.home"), "xorcery/xorcery.yaml");
                if (userYamlFile.exists()) {
                    FileInputStream userYamlStream = new FileInputStream(userYamlFile);
                    builder = builder.addYaml(userYamlStream);
                    logger.info("Loaded " + userYamlFile);
                }

                return builder;
            }

            public static Builder loadTest(File configFile)
                    throws IOException {
                Builder builder = load(configFile);

                // Load Xorcery defaults
                {
                    URL resource = Configuration.class.getClassLoader().getResource("META-INF/xorcery-defaults.yaml");
                    try (InputStream in = resource.openStream()) {
                        builder = builder.addYaml(in);
                        logger.info("Loaded " + resource);
                    }
                }
                {
                    URL resource = Configuration.class.getClassLoader().getResource("META-INF/xorcery-defaults-test.yaml");
                    try (InputStream in = resource.openStream()) {
                        builder = builder.addYaml(in);
                        logger.info("Loaded " + resource);
                    }
                }

                // Load extensions
                {
                    Enumeration<URL> extensionConfigurationURLs = Configuration.class.getClassLoader().getResources("META-INF/xorcery.yaml");
                    while (extensionConfigurationURLs.hasMoreElements()) {
                        URL resource = extensionConfigurationURLs.nextElement();
                        try (InputStream configurationStream = resource.openStream()) {
                            builder = builder.addYaml(configurationStream);
                            logger.info("Loaded " + resource);
                        }
                    }
                }
                {
                    Enumeration<URL> extensionConfigurationURLs = Configuration.class.getClassLoader().getResources("META-INF/xorcery-test.yaml");
                    while (extensionConfigurationURLs.hasMoreElements()) {
                        URL resource = extensionConfigurationURLs.nextElement();
                        try (InputStream configurationStream = resource.openStream()) {
                            builder = builder.addYaml(configurationStream);
                            logger.info("Loaded " + resource);
                        }
                    }
                }

                // Load custom
                {
                    Enumeration<URL> configurationURLs = Configuration.class.getClassLoader().getResources("xorcery.yaml");
                    while (configurationURLs.hasMoreElements()) {
                        URL resource = configurationURLs.nextElement();
                        try (InputStream configurationStream = resource.openStream()) {
                            builder = builder.addYaml(configurationStream);
                            logger.info("Loaded " + resource);
                        }
                    }
                }
                {
                    Enumeration<URL> configurationURLs = Configuration.class.getClassLoader().getResources("xorcery-test.yaml");
                    while (configurationURLs.hasMoreElements()) {
                        URL resource = configurationURLs.nextElement();
                        try (InputStream configurationStream = resource.openStream()) {
                            builder = builder.addYaml(configurationStream);
                            logger.info("Loaded " + resource);
                        }
                    }
                }

                // Load user directory overrides
                Configuration partialConfig = builder.build();
                StandardConfiguration standardConfiguration = new StandardConfiguration.Impl(partialConfig);
                builder = partialConfig.asBuilder();
                File overridesYamlFile = new File(standardConfiguration.getHome(), "xorcery-test.yaml");
                if (overridesYamlFile.exists()) {
                    FileInputStream overridesYamlStream = new FileInputStream(overridesYamlFile);
                    builder = builder.addYaml(overridesYamlStream);
                    logger.info("Loaded " + overridesYamlFile);
                }

                // Load specified overrides
                if (configFile != null) {

                    if (configFile.getName().endsWith("yaml") || configFile.getName().endsWith("yml")) {
                        builder = builder.addYaml(new FileInputStream(configFile));
                        logger.info("Loaded " + configFile);
                    } else if (configFile.getName().endsWith("properties")) {
                        builder = builder.addProperties(new FileInputStream(configFile));
                        logger.info("Loaded " + configFile);
                    } else {
                        logger.warn("Unknown configuration filetype: " + configFile);
                    }
                }

                // Load user overrides
                File userYamlFile = new File(System.getProperty("user.home"), "xorcery/xorcery-test.yaml");
                if (userYamlFile.exists()) {
                    FileInputStream userYamlStream = new FileInputStream(userYamlFile);
                    builder = builder.addYaml(userYamlStream);
                    logger.info("Loaded " + userYamlFile);
                }

                return builder;
            }

            public Builder() {
                this(JsonNodeFactory.instance.objectNode());
            }


            ObjectNode navigateToParentOfPropertyNameThenAdd(ObjectNode node, String name, JsonNode value) {
                int i = name.indexOf(".");
                if (i == -1) {
                    // name is the last element in path to navigate, navigation complete.
                    node.set(name, value);
                    return node;
                }
                String childElement = name.substring(0, i);
                String remainingName = name.substring(i + 1);
                JsonNode child = node.get(childElement);
                ObjectNode childObjectNode;
                if (child == null || child.isNull()) {
                    // non-existent json-node, need to create child object node
                    childObjectNode = node.putObject(childElement);
                } else {
                    // existing json-node, ensure that it can be navigated through
                    if (!child.isObject()) {
                        throw new RuntimeException("Attempted to navigate through json key that already exists, but is not a json-object that support navigation.");
                    }
                    childObjectNode = (ObjectNode) child;
                }
                // TODO support arrays, for now we only support object navigation
                return navigateToParentOfPropertyNameThenAdd(childObjectNode, remainingName, value);
            }

            public Builder add(String name, JsonNode value) {
                navigateToParentOfPropertyNameThenAdd(builder, name, value);
                return this;
            }

            public Builder add(String name, String value) {
                return add(name, builder.textNode(value));
            }

            public Builder add(String name, long value) {
                return add(name, builder.numberNode(value));
            }

            public Builder addYaml(InputStream yamlStream) throws IOException {
                try (yamlStream) {
                    ObjectNode yaml = (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(yamlStream);
                    new JsonMerger().merge(builder, yaml);
                    return this;
                }
            }

            public Builder addYaml(String yamlString) throws IOException {
                ObjectNode yaml = (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(yamlString);
                new JsonMerger().merge(builder, yaml);
                return this;
            }

            private Builder addProperties(InputStream propertiesStream) throws IOException {
                try (propertiesStream) {
                    ObjectNode properties = (ObjectNode) new ObjectMapper(new JavaPropsFactory()).readTree(propertiesStream);
                    new JsonMerger().merge(builder, properties);
                    return this;
                }
            }

            public Builder addProperties(String propertiesString) throws IOException {
                ObjectNode properties = (ObjectNode) new ObjectMapper(new JavaPropsFactory()).readTree(propertiesString);
                new JsonMerger().merge(builder, properties);
                return this;
            }

            public Builder addSystemProperties(String nodeName) {
                ObjectNode system = builder.objectNode();
                for (Map.Entry<Object, Object> systemProperty : System.getProperties().entrySet()) {
                    system.set(systemProperty.getKey().toString().replace('.', '_'), builder.textNode(systemProperty.getValue().toString()));
                }
                builder.set(nodeName, system);
                return this;
            }

            public Builder addEnvironmentVariables(String nodeName) {
                ObjectNode env = builder.objectNode();
                System.getenv().forEach((key, value) -> env.set(key.replace('.', '_'), env.textNode(value)));
                builder.set(nodeName, env);
                return this;
            }

            public Configuration build() {
                // Resolve any references
                return new Configuration(new VariableResolver().apply(builder, builder));
            }

        public ObjectNode builder() {
            return builder;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Builder) obj;
            return Objects.equals(this.builder, that.builder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(builder);
        }

        @Override
        public String toString() {
            return "Builder[" +
                    "builder=" + builder + ']';
        }

        }

    public Configuration getConfiguration(String name) {
        return getJson(name)
                .map(ObjectNode.class::cast).map(Configuration::new)
                .orElseGet(() -> new Configuration(JsonNodeFactory.instance.objectNode()));
    }

    public List<Configuration> getConfigurations(String name) {
        return getJson(name)
                .map(ArrayNode.class::cast)
                .map(a -> JsonElement.getValuesAs(a, Configuration::new))
                .orElseGet(Collections::emptyList);
    }

    public Builder asBuilder() {
        return new Builder(json);
    }
}

