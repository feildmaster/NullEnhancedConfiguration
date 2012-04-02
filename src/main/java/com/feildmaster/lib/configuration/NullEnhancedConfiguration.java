package com.feildmaster.lib.configuration;

import java.io.File;
import java.lang.reflect.Field;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.*;

/**
 * Enhances Configuration further by saving null values
 *
 * @author Feildmaster
 */
public class NullEnhancedConfiguration extends EnhancedConfiguration implements NullConfigurationSection {
    public NullEnhancedConfiguration(Plugin plugin) {
        this("config.yml", plugin);
    }

    public NullEnhancedConfiguration(String file, Plugin plugin) {
        super(file, plugin, false);

        reflectYaml();
    }

    public NullEnhancedConfiguration(File file, Plugin plugin) {
        super(file, plugin, false);

        reflectYaml();
    }

    @Override
    public void set(String path, Object value) {
        Validate.notEmpty(path, "Cannot set to an empty path");

        final char seperator = getRoot().options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(seperator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            this.map.put(key, value);
        } else {
            section.set(key, value);
        }
    }

    /**
     * Removes the specified path from the configuration.
     *
     * @param path The path to remove
     */
    @Override
    public void unset(String path) {
        final char seperator = getRoot().options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        NullConfigurationSection section = this;
        while ((i1 = path.indexOf(seperator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            NullConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            remove(key);
        } else {
            section.unset(key);
        }
    }

    private void remove(String key) {
        map.remove(key);
    }


    @Override
    public NullConfigurationSection getConfigurationSection(String path) {
        return (NullConfigurationSection) super.getConfigurationSection(path);
    }

    @Override
    public NullConfigurationSection createSection(String path) {
        Validate.notNull(path, "Path cannot be null");
        Validate.notEmpty(path, "Cannot create section at empty path");

        final char seperator = getRoot().options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        NullConfigurationSection section = this;
        while ((i1 = path.indexOf(seperator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            NullConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == this) {
                section = section.createLiteralSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            return createLiteralSection(key);
        }
        return section.createLiteralSection(key);
    }

    @Override
    public NullConfigurationSection createLiteralSection(String key) {
        NullConfigurationSection section = new NullEnhancedMemorySection(this, this, key);
        map.put(key, section);
        return section;
    }

    private void reflectYaml() {
        try {
            Class yamlClass = this.getClass();
            while(!yamlClass.equals(YamlConfiguration.class)) {
                yamlClass = yamlClass.getSuperclass();
            }

            // Set the representer
            Field representer = yamlClass.getDeclaredField("yamlRepresenter");
            representer.setAccessible(true);
            representer.set(this, new EnhancedRepresenter());
            // Get the options
            Field options = yamlClass.getDeclaredField("yamlOptions");
            options.setAccessible(true);
            // Set the yaml
            Field yaml = yamlClass.getDeclaredField("yaml");
            yaml.setAccessible(true);
            yaml.set(this, new Yaml(new YamlConstructor(), (EnhancedRepresenter) representer.get(this), (DumperOptions) options.get(this)));
        } catch (Exception ex) {
            getPlugin().getLogger().log(java.util.logging.Level.SEVERE, null, ex);
        }

        load();
    }
}
