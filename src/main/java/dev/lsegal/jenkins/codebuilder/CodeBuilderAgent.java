package dev.lsegal.jenkins.codebuilder;

import java.io.IOException;
import java.util.Collections;

import javax.annotation.Nonnull;

import com.amazonaws.services.codebuild.model.ResourceNotFoundException;
import com.amazonaws.services.codebuild.model.StopBuildRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.ComputerLauncher;

class CodeBuilderAgent extends AbstractCloudSlave {
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuilderAgent.class);
  private static final long serialVersionUID = -6722929807051421839L;
  private final CodeBuilderCloud cloud;

  /**
   * Creates a new CodeBuilderAgent node that provisions a
   * {@link CodeBuilderComputer}.
   *
   * @param cloud    a {@link CodeBuilderCloud} object.
   * @param name     the name of the agent.
   * @param launcher a {@link hudson.slaves.ComputerLauncher} object.
   * @throws hudson.model.Descriptor$FormException if any.
   * @throws java.io.IOException                   if any.
   */
  public CodeBuilderAgent(@Nonnull CodeBuilderCloud cloud, @Nonnull String name, @Nonnull ComputerLauncher launcher)
      throws Descriptor.FormException, IOException {
    super(name, "AWS CodeBuild Agent", "/build", 1, Mode.NORMAL, cloud.getLabel(), launcher,
        new CloudRetentionStrategy(cloud.getAgentTimeout() / 60 + 1), Collections.emptyList());
    this.cloud = cloud;
  }

  /**
   * Get the cloud instance associated with this builder
   *
   * @return a {@link CodeBuilderCloud} object.
   */
  public CodeBuilderCloud getCloud() {
    return cloud;
  }

  /** {@inheritDoc} */
  @Override
  public AbstractCloudComputer<CodeBuilderAgent> createComputer() {
    return new CodeBuilderComputer(this);
  }

  /** {@inheritDoc} */
  @Override
  protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
    LOGGER.info("[CodeBuilder]: Terminating agent: " + getDisplayName());
    ((CodeBuilderComputer) getComputer()).gracefulShutdown();
  }
}
