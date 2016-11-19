package hudson.plugins.selenium.actions;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.CreateStreamTaskListenerCallable;
import hudson.plugins.selenium.callables.GetConfigurations;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;

public class ServiceManagementAction implements Action {

    private Computer computer;

    public ServiceManagementAction(Computer c) {
        computer = c;
    }

    public String getIconFileName() {
        return "/plugin/selenium/24x24/selenium.png";
    }

    public String getDisplayName() {
        return "Selenium node Management";
    }

    public String getUrlName() {
        return "selenium";
    }

    public HttpResponse doRestart(@QueryParameter String conf) throws IOException, ServletException {
        doStop(conf);
        doStart(conf);
        return HttpResponses.forwardToPreviousPage();
    }

    public HttpResponse doStop(@QueryParameter String conf) throws IOException, ServletException {
        PluginImpl.getPlugin().getConfiguration(conf).stop(computer);
        return HttpResponses.forwardToPreviousPage();
    }

    public HttpResponse doStart(@QueryParameter String conf) throws IOException, ServletException {
        try {
            final CreateStreamTaskListenerCallable callable = new CreateStreamTaskListenerCallable();
            final StreamTaskListener stdout = computer.getNode().getRootPath().act(callable); //StreamTaskListener.fromStdout();//callable.call();
            stdout.getLogger().println("doStart :"+computer.getName());
            PluginImpl.startSeleniumNode(computer, stdout, conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HttpResponses.forwardToPreviousPage();
    }

    public Computer getComputer() {
        return computer;
    }

    public Map<String, SeleniumRunOptions> getConfigurations() {
        try {
            return computer.getNode().getRootPath().getChannel().call(new GetConfigurations());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public List<SeleniumGlobalConfiguration> getMatchingConfigurations() {
        return PluginImpl.getPlugin().getGlobalConfigurationForComputer(computer);
    }

}
