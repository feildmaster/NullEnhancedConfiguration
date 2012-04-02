package com.feildmaster.lib.configuration;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

public class NullEnhancedMemorySection extends EnhancedMemorySection implements NullConfigurationSection {
    public NullEnhancedMemorySection(EnhancedConfiguration superParent, MemorySection parent, String path) {
        super(superParent, parent, path);
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
        Validate.notEmpty(path, "Cannot set to an empty path");

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
        this.map.remove(key);
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
            if (subSection == null) {
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
        NullConfigurationSection section = new NullEnhancedMemorySection(superParent, this, key);
        map.put(key, section);
        return section;
    }
}
