/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider;
import org.ow2.proactive.virtualizing.virtualbox.api.IConsole;


/**
 * Create, show and remove jobs output
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobsOutputController {

    /** title of the job output view */
    public static final String PREFIX_JOB_OUTPUT_TITLE = "Job #";

    /** Wait for logs message */
    public static final String INITIAL_MESSAGE = "Please wait while downloading logs..." +
        System.getProperty("line.separator");

    protected LogForwardingService lfs;
    
    
    // The shared instance
    private static JobsOutputController instance = null;
    private Map<String, JobOutputAppender> appenders = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private JobsOutputController() {
        appenders = new HashMap<String, JobOutputAppender>();
        lfs=new LogForwardingService(SocketBasedForwardingProvider.class.getName());
        try {
            lfs.initialize();
        } catch (LogForwardingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public synchronized static JobsOutputController getInstance() {
        if (instance == null) {
            instance = new JobsOutputController();
        }
        return instance;
    }

    public synchronized static void clearInstance() {
        if (instance != null) {
            instance.removeAllJobOutput();
            instance = null;
        }
    }

    /**
     * If the JobOutput for specified JobId is available, return it, else return null.
     * 
     * @param jobId valid Id for a running job
     * @return the output of the specified job, or null
     * @see JobsOutputController#createJobOutput(JobId)
     */
    public JobOutput getJobOutput(String jobId) {
        JobOutputAppender joa = appenders.get(jobId);
        if (joa == null) {
            return null;
        } else {
            return joa.getJobOutput();
        }
    }

    /**
     * Create an output for a job identified by the given jobId
     *
     * @param jobId
     *            the jobId
     * @param showOutput if true, show the output view after the logs have been obtained
     * @throws LogForwardingException 
     * @throws PermissionException 
     * @throws UnknownJobException 
     * @throws NotConnectedException 
     * @throws IOException 
     * @throws SchedulerException
     */
    public void createJobOutput(Scheduler s,String jobId) throws NotConnectedException, UnknownJobException, PermissionException, LogForwardingException, IOException { 
 
            PipedInputStream snk = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(snk);
                JobOutputAppender joa = new JobOutputAppender(new JobOutput(PREFIX_JOB_OUTPUT_TITLE + jobId,
                    snk,pos));
                joa.setLayout(Log4JTaskLogs.getTaskLogLayout());
                Logger log = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);
                log.setAdditivity(false);
                log.setLevel(Level.ALL);
                log.removeAllAppenders();
                log.addAppender(joa);
                appenders.put(jobId, joa);
                s.listenJobLogs(jobId, lfs.getAppenderProvider());
              
           
        }
    

    /**
     * To remove an output for a job identified by the given jobId
     *
     * @param jobId
     *            the jobId
     */
    public void removeJobOutput(JobId jobId) {
        JobOutputAppender joa = appenders.get(jobId);
        if (joa != null) {
            joa.close();
        }
        appenders.remove(jobId);
    }

    /**
     * Remove all output ! This method clear the console.
     */
    public void removeAllJobOutput() {
        // do NOT call removeJobOutput(JobId), 
        // you are going to throw a ConcurrentModificationException
        for (Entry<String, JobOutputAppender> out : appenders.entrySet()) {
            out.getValue().close();
        }
        appenders.clear();
    }
}
