/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.plugins.selenium.HubHolder;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.RemoveSeleniumServer;
import hudson.plugins.selenium.callables.RunningRemoteSetterCallable;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.StopSeleniumServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import jenkins.security.MasterToSlaveCallable;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;

/**
 * @author Richard Lavoie
 * 
 */
public abstract class SeleniumJarRunner implements SeleniumProcess {

    public abstract SeleniumRunOptions initOptions(Computer c);

    public void start(Computer computer, TaskListener listener, String name) throws IOException, InterruptedException, ExecutionException {
        final FilePath seleniumJar = new FilePath(SeleniumProcessUtils.findStandAloneServerJar());
        final String nodeName = computer.getName();
        SeleniumRunOptions opts = initOptions(computer);

        if (opts != null) {
            final String masterName = PluginImpl.getMasterHostName();
            final PluginImpl p = PluginImpl.getPlugin();
            final EnvVars nodeVars = PluginImpl.getNodeVars(computer);
            String nodehost = nodeVars != null ? nodeVars.get("seleniumNodeIp") : null;
//            if( null == nodehost) {
//                //nodehost = computer.getHostName();
//            }
            listener.getLogger().println("host:"+computer.getHostName());
            if( null != nodehost) {
                opts.addOptionIfSet("-host", nodehost);
            }
            computer.getNode().getRootPath()
                    .act(new SeleniumCallable(seleniumJar,masterName, p.getPort(), nodeName, listener, name, opts));
        }
    }

    /**
     * @param computer
     * @param name
     */
    public void remove(Computer computer, String name) {
        stop(computer, name);
        try {
            computer.getNode().getRootPath().act(new RemoveSeleniumServer(name));
        } catch (Exception e) {

        }
    }

    /*
     * (non-Javadoc)
     * @see hudson.plugins.selenium.configuration.SeleniumRunner#stop(hudson.model.Computer)
     */
    public void stop(Computer computer, String name) {
        FilePath path = computer.getNode().getRootPath();
        if (path != null) {
            try {
                final String url = computer.getNode().getRootPath().act(new StopSeleniumServer(name));
                PluginImpl.getPlugin().getHubChannel().call(new MasterToSlaveCallable<Void, Exception>() {

                    /**
                     * 
                     */
                    private static final long serialVersionUID = -5805313572457450300L;
                    private String remoteUrl = url;

                    public Void call() throws Exception {
                        Registry registry = HubHolder.hub.getRegistry();
                        if (registry != null) {
                            Iterator<RemoteProxy> it = registry.getAllProxies().iterator();
                            while (it.hasNext()) {
                                RemoteProxy proxy = it.next();
                                if (remoteUrl.equals(proxy.getRemoteHost().toString())) {
                                    registry.removeIfPresent(proxy);
                                }
                            }
                        }
                        return null;
                    }

                });
            } catch (Exception e) {
                try {
                    computer.getNode().getRootPath().act(new RunningRemoteSetterCallable(name, SeleniumConstants.ERROR));
                } catch (Exception e1) {
                }
            }
        }

    }

}
