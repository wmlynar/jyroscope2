package com.inovatica.orchestrator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.process_tree_killer.ProcessTree;

import com.github.jy2.Publisher;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.Subscribe;
import com.inovatica.orchestrator.internal.OrchestratorStartStop;
import com.inovatica.orchestrator.json.OrcherstratorStatus;
import com.inovatica.orchestrator.model.OrchestratorModel;
import com.inovatica.orchestrator.model.OrchestratorModelBuilder;

import go.jyroscope.ros.diagnostic_msgs.KeyValue;
import go.jyroscope.ros.rosgraph_msgs.Log;

public class Orchestrator implements OutputCallback {

    public static final org.apache.commons.logging.Log LOG = JyroscopeDi.getLog(Orchestrator.class);

    @Parameter("working_directory")
    public String workingDir = ".";

    @Parameter("launchfile_path")
    public String launchfilePath = ".";

    @Parameter("ros2_launchfile_path")
    public String ros2LaunchfilePath = ".";

    @Parameter("hz_launchfile_path")
    public String hzLaunchfilePath = ".";

    @Parameter("jy2_launchfile_path")
    public String jy2LaunchfilePath = ".";

    @Parameter("jar_path")
    public String jarPath = ".";

    @Parameter("bash_path")
    public String bashPath = ".";

    @Parameter("jar_params")
    public String jarParams = "";

    @Parameter("bash_params")
    public String bashParams = "";

    @Parameter("start_list")
    public List<?> startList = new ArrayList<>();

    @Parameter("java_opts")
    public String javaOpts = "";

    @Parameter("debug")
    public boolean debug = true;

    @Parameter("jmx")
    public boolean jmx = true;

    @Parameter("hostname")
    public String hostname = "localhost";

    @Parameter("heapDumpOnOutOfMemory")
    public boolean heapDumpOnOutOfMemomry = false;

    @Parameter("heapDumpPath")
    public String heapDumpPath = "";
    
    @Parameter("z_gc")
    public boolean zGc = false;
    
    @Parameter("java_memory_limit")
    public int javaMemoryLimit = 0;

    @Parameter("shenandoah_gc")
    public boolean shenandoahGc = false;

    @Parameter("concurrent_gc")
    public boolean concurrentGc = false;
    
    @Parameter("preallocate_gc")
    public boolean preallocateGc = false;

    @Parameter("kill_on_out_of_memory")
    public boolean killOnOutOfMemory = true;

    @Parameter("new_ratio")
    public int newRatio = 0;

    @Parameter("kill_with_sudo")
    public boolean killWithSudo = false;

    @Parameter("user")
    public String user = "";

    @Parameter("run_as_sudo_when_suffix")
    public boolean runAsSudoWhenSuffix = true;

    @Parameter("limit_memory_when_xmx")
    public boolean limitMemoryWhenXmx = true;
    
    @Publish("status")
    public Publisher<OrcherstratorStatus> statusPublisher;

    @Publish("output")
    public Publisher<Log> outputPublisher;

    @Parameter("suspend_debug")
    public boolean suspendDebug = false;

    @Parameter("remote_profiling")
    public boolean remoteProfiling = false;

    @Parameter("use_legacy_debug")
    public boolean useLegacyDebug = false;

    @Parameter("debug_start_port")
    public int debugStartPort = 4001;

    @Parameter("jmx_start_port")
    public int jmxStartPort = 9012;

    private OrchestratorStartStop startStop;
    private OrchestratorModelBuilder builder;

    @Init
    public void init() {
        builder = new OrchestratorModelBuilder();
        builder.setWorkingDir(workingDir);
        builder.setLaunchFileDir(launchfilePath);
        builder.setRos2LaunchFileDir(ros2LaunchfilePath);
        builder.setHzLaunchFileDir(hzLaunchfilePath);
        builder.setJy2LaunchFileDir(jy2LaunchfilePath);
        builder.setJarFileDir(jarPath);
        builder.setBashFileDir(bashPath);
        builder.setStringStartList(startList);
        builder.setJarParams(jarParams);
        builder.setJavaOpts(javaOpts);
        builder.setDebug(debug);
        builder.setJmx(jmx);
        builder.setBashParams(bashParams);
        builder.setHostname(hostname);
        builder.setHeapDumpOnOutOfMemomry(heapDumpOnOutOfMemomry);
        builder.setHeapDumpPath(heapDumpPath);
        builder.debugStartPort = debugStartPort;
        builder.jmxStartPort = jmxStartPort;
        builder.shenandoahGc = shenandoahGc;
        builder.concurrentGc = concurrentGc;
        builder.preallocateGc = preallocateGc;
        builder.killOnOutOfMemory = killOnOutOfMemory;
        OrchestratorModel model = builder.build();
        startStop = new OrchestratorStartStop(model, this);
        setStartStopAttributes();
        startStop.onStartup();
    }

    @Repeat(interval = 1000)
    public void publishOrchestratorStatus() throws IOException {
        OrcherstratorStatus status = startStop.getStatus();
        statusPublisher.publish(status);
    }

    @Subscribe("command")
    public synchronized void receive(KeyValue message) {
        LOG.info("Received key: " + message.key + " value: " + message.value);
        setStartStopAttributes();
        if ("start".equalsIgnoreCase(message.key)) {
            startStop.start(message.value);
        } else if ("stop".equalsIgnoreCase(message.key)) {
            ProcessTree.KILL_WITH_SUDO = killWithSudo;
            startStop.stop(message.value, false);
        } else if ("kill".equalsIgnoreCase(message.key)) {
            ProcessTree.KILL_WITH_SUDO = killWithSudo;
            startStop.stop(message.value, true);
        } else if ("scan".equalsIgnoreCase(message.key)) {
            builder.scanAndAddNewFiles(startStop.model);
        } else {
            LOG.error("Unrecognised command: " + message.key);
        }
    }

    @Override
    public synchronized void logOutput(boolean isError, String item, String text) {
        text = stripConsoleControlCodes(text);

        Log output = new Log();
        if (isError) {
            output.level= Log.ERROR;
        } else {
            output.level = Log.INFO;
        }
        output.name = item;
        output.msg = text;
        outputPublisher.publish(output);
    }

    private void setStartStopAttributes() {
        startStop.suspendDebug = suspendDebug;
        startStop.remoteProfiling = remoteProfiling;
        startStop.newRatio = newRatio;
        startStop.user = user;
        startStop.runAsSudoWhenSuffix = runAsSudoWhenSuffix;
        startStop.limitMemoryWhenXmx = limitMemoryWhenXmx;
        startStop.useLegacyDebug = useLegacyDebug;
        startStop.zGc = zGc;
        startStop.javaMemoryLimit = javaMemoryLimit;
    }

    private String stripConsoleControlCodes(String str) {
        return str.replaceAll("\u001B[\\[\\]][0-9]*[;mK]", "").replace("\u0007", "");

    }
}
