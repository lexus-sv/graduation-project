package main.model;

public enum ModerationStatus {
  NEW,
  ACCEPTED,
  DECLINED;

  public static ModerationStatus getEqualStatus(String status) {
    for (ModerationStatus ms : ModerationStatus.values()) {
      if (ms.toString().equalsIgnoreCase(status) || ms.toString().toLowerCase().contains(status)) {
        return ms;
      }
    }
    return null;
  }
}
