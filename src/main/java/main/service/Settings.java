package main.service;

import java.util.HashMap;
import main.model.GlobalSettings;
import main.repository.GlobalSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class Settings {

  private final GlobalSettingsRepository repository;
  private final HashMap<String, Boolean> settings;

  public Settings(GlobalSettingsRepository repository) {
    settings = new HashMap<>();
    this.repository = repository;
    repository.findAll().forEach(s -> settings.put(s.getCode(), s.isValue()));
  }


  public boolean getSetting(String settingName) {
    return settings.getOrDefault(settingName, false);
  }

  public void update(HashMap<String, Boolean> request) {
    request.forEach((key, value) -> {
      GlobalSettings setting = repository.findByCode(key);
      if (value != null) {
        if (setting != null) {
          settings.put(key, value);
          setting.setValue(value);
          repository.save(setting);
        }
      }
    });
  }

  public HashMap<String, Boolean> getSettings() {
    return settings;
  }
}