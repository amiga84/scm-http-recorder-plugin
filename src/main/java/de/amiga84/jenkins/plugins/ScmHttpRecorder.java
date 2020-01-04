package de.amiga84.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import de.amiga84.jenkins.plugins.scmHttpRecorder.constants.ServerStage;
import de.amiga84.jenkins.plugins.scmHttpRecorder.model.CommitInfo;
import de.amiga84.jenkins.plugins.scmHttpRecorder.model.HttpMessage;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class ScmHttpRecorder extends Recorder implements SimpleBuildStep, Serializable {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(ScmHttpRecorder.class.getName());

  @Nonnull
  private String url;
  private ServerStage serverStage;
  private boolean restart;

  @DataBoundConstructor
  public ScmHttpRecorder(String url) {
    LOGGER.info("ScmHttpRecorder created");
    this.url = url;
  }

  @Override
  public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
    PrintStream logger = listener.getLogger();
    if (run instanceof MatrixRun) {
      logger.print("Matrix Projects not supported.");
    } else if (run != null) {

      int buildNumber = 0;
      String projectName = run.getParent().getName();
      try {
        buildNumber = Integer.parseInt(run.getId());
      } catch (NumberFormatException | NullPointerException e) {
        LOGGER.log(Level.WARNING, "Could not read Build Number");
      }
      List<CommitInfo> commitInfos = getCommitInfos(RunScmChangeExtractor.getChanges(run));
      HttpMessage message = new HttpMessage();
      message.setCommitInfos(commitInfos);
      message.setBuildNumber(buildNumber);
      message.setProjectName(projectName);
      message.setRestartServer(restart);
      message.setServerStage(serverStage);
      message.setTimestamp(System.currentTimeMillis());
      message.setBuildNumber(buildNumber);

      sendMessageAsJSON(message, logger);

    } else {
      throw new IllegalArgumentException("Unsupported run type " + run.getClass().getName());
    }
  }

  private List<CommitInfo> getCommitInfos(List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets) {
    List<CommitInfo> commits = new ArrayList<>();
    if (changeSets != null && !changeSets.isEmpty()) {
      for (ChangeLogSet<? extends ChangeLogSet.Entry> cls : changeSets) {
        for (ChangeLogSet.Entry entry : cls) {
          CommitInfo commitInfo = new CommitInfo();
          commitInfo.setAuthor(entry.getAuthor().toString());
          commitInfo.setCommitId(entry.getCommitId());
          commitInfo.setMessage(entry.getMsgEscaped());
          commitInfo.setTimestamp(entry.getTimestamp());
          commits.add(commitInfo);
        }
      }
    }
    return commits;
  }

  private void sendMessageAsJSON(HttpMessage message, PrintStream logger) {
    try {
      String json = JSONObject.fromObject(message).toString(2);
      logger.println("Request:");
      logger.println(json);
      Content content = Request.Post(url).bodyString(json, ContentType.APPLICATION_JSON).execute().returnContent();
      String response = content.asString();
      logger.println("Response:");
      logger.println(response);
    } catch (HttpResponseException e) {
      logger.print(e.getMessage());
      LOGGER.warning(e.getMessage());
    } catch (IOException e) {
      logger.print("Send failed." + e.getMessage());
      LOGGER.warning(e.getMessage());
    }
  }

  @Override
  public final BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  public String getUrl() {
    return url;
  }

  @DataBoundSetter
  public void setUrl(String url) {
    this.url = url;
  }

  public ServerStage getServerStage() {
    return serverStage;
  }

  @DataBoundSetter
  public void setServerStage(ServerStage serverStage) {
    this.serverStage = serverStage;
  }

  public boolean isRestart() {
    return restart;
  }

  @DataBoundSetter
  public void setRestart(boolean restart) {
    this.restart = restart;
  }

  @Symbol("scmHttpRecorder")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    public ListBoxModel doFillServerStageItems() {
      return ServerStage.getFillItems();
    }

    public FormValidation doURL(@QueryParameter String url) {
      if (url.length() == 0) {
        return FormValidation.error("URL can't be empty");
      }
      try {
        new URL(url);
      } catch (MalformedURLException e) {
        return FormValidation.error("URL can't be empty");
      }
      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Send SCM History as JSON to a URL";
    }

  }

}
