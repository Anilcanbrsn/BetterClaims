package me.RonanCraft.Pueblos.resources.files;

import me.RonanCraft.Pueblos.Pueblos;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class FileLanguage {
    private final YamlConfiguration config = new YamlConfiguration();

    public String getString(String path) {
        if (config.isString(path))
            return config.getString(path);
        return "Seems like the path " + path + " didn't load correctly!";
    }

    @SuppressWarnings("all")
    public List<String> getStringList(String path) {
        if (config.isList(path))
            return config.getStringList(path);
        return Arrays.asList(getString(path));
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    @SuppressWarnings("all")
    public void load() {
        generateDefaults();
        String fileName = "lang" + File.separator + FileOther.FILETYPE.CONFIG.getString("Language-File");
        File file = new File(getPl().getDataFolder(), fileName);
        if (!file.exists()) {
            fileName = "lang" + File.separator + defaultLangs[0]; //Default to english
            file = new File(getPl().getDataFolder(), fileName);
        }
        try {
            config.load(file);
            InputStream in = getPl().getResource(fileName);
            if (in == null)
                in = getPl().getResource(fileName.replace(File.separator, "/"));
            if (in != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in)));
                config.options().copyDefaults(true);
                in.close();
            }
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final String[] defaultLangs = {
            "en.yml"
            /*"fr.yml", //French
            "ja.yml", //Japanese
            "ru.yml", //Russian
            "chs.yml", //Chinese Simplified
            "cht.yml", //Chinese
            "du.yml", //Dutch
            "es.yml", //Spanish
            "cs.yml", //Czech
            "pl.yml", //Polish
            "it.yml"  //Italian*/
    };

    private void generateDefaults() {
        //Generate all language files
        for (String yaml : defaultLangs) {
            generateDefaultConfig(yaml, yaml); //Generate its own defaults
            if (!yaml.equals(defaultLangs[0]))
                generateDefaultConfig(yaml, defaultLangs[0]); //Generate the english defaults (incase)
        }
    }

    private void generateDefaultConfig(String fName, String fNameDef /*Name of file to generate defaults*/) {
        String fileName = "lang" + File.separator + fName;
        File file = new File(getPl().getDataFolder(), fileName);
        if (!file.exists())
            getPl().saveResource(fileName, false);
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            String fileNameDef = "lang" + File.separator + fNameDef;
            InputStream in = getPl().getResource(fileNameDef);
            if (in == null)
                in = getPl().getResource(fileNameDef.replace(File.separator, "/"));
            if (in != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in)));
                config.options().copyDefaults(true);
                in.close();
            }
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Pueblos getPl() {
        return Pueblos.getInstance();
    }
}
