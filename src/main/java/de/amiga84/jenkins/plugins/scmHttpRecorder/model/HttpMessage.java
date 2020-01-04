package de.amiga84.jenkins.plugins.scmHttpRecorder.model;

import de.amiga84.jenkins.plugins.scmHttpRecorder.constants.ServerStage;
import java.util.ArrayList;
import java.util.List;

public class HttpMessage {

  private List<CommitInfo> commitInfos = new ArrayList<>();

  private long timestamp;

  private int buildNumber;

  private String projectName;

  private boolean restartServer;

  private ServerStage serverStage;

  public List<CommitInfo> getCommitInfos() {
    return commitInfos;
  }

  public void setCommitInfos(List<CommitInfo> commitInfos) {
    this.commitInfos = commitInfos;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public void setBuildNumber(int buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public boolean isRestartServer() {
    return restartServer;
  }

  public void setRestartServer(boolean restartServer) {
    this.restartServer = restartServer;
  }

  public ServerStage getServerStage() {
    return serverStage;
  }

  public void setServerStage(ServerStage serverStage) {
    this.serverStage = serverStage;
  }

}
