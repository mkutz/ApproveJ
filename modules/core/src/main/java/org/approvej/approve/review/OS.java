package org.approvej.approve.review;

enum OS {
  WINDOWS,
  MAC,
  LINUX;

  public static OS current() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return WINDOWS;
    } else if (osName.contains("mac")) {
      return MAC;
    } else if (osName.contains("nix") || osName.contains("nux")) {
      return LINUX;
    } else {
      throw new UnsupportedOperationException("Unsupported operating system: %s".formatted(osName));
    }
  }
}
