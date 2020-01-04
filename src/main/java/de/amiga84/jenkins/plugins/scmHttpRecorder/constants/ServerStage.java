package de.amiga84.jenkins.plugins.scmHttpRecorder.constants;

import hudson.util.ListBoxModel;

public enum ServerStage {

  DEV, TEST, WEB;

  public static ListBoxModel getFillItems() {
    ListBoxModel items = new ListBoxModel();
    for (ServerStage value : values()) {
      items.add(value.name());
    }
    return items;
  }

}
