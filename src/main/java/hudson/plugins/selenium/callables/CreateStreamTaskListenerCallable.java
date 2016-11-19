package hudson.plugins.selenium.callables;

import hudson.util.StreamTaskListener;
import jenkins.security.MasterToSlaveCallable;

public class CreateStreamTaskListenerCallable extends MasterToSlaveCallable<StreamTaskListener,Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = -5448989386458342771L;

    public StreamTaskListener call() throws Exception {
        return StreamTaskListener.fromStdout();
    }
}
