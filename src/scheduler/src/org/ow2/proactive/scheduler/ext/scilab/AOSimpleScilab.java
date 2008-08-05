/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.scilab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javasci.SciData;
import javasci.Scilab;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AOSimpleScilab implements Serializable {

    /**
     *
     */
    static String nl = System.getProperty("line.separator");
    private String inputScript = null;
    private String[] outputVars = null;
    private ArrayList<String> scriptLines = new ArrayList<String>();

    /** logger **/
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);
    protected static boolean debug = logger.isDebugEnabled();

    public AOSimpleScilab() {
    }

    /**
     * Constructor for the Simple task
     * @param inputScript  a pre-scilab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     */
    public AOSimpleScilab(String inputScript, ArrayList<String> scriptLines, String[] outputVars) {
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
        this.outputVars = outputVars;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                Scilab.Finish();
            }
        }));
    }

    public Object execute(TaskResult... results) throws Throwable {
        try {
            if (debug) {
                logger.info("Scilab Initialization...");
            }
            Scilab.init();
            if (debug) {
                logger.info("Initialization Complete!");
            }
        } catch (UnsatisfiedLinkError e) {
            StringWriter error_message = new StringWriter();
            PrintWriter pw = new PrintWriter(error_message);
            pw.println("Can't find the Scilab libraries in host " + java.net.InetAddress.getLocalHost());
            pw.println("PATH=" + System.getenv("PATH"));
            pw.println("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
            pw.println("java.library.path=" + System.getProperty("java.library.path"));

            UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
            ne.initCause(e);
            throw ne;
        }

        HashMap<String, List<SciData>> newEnv = new HashMap<String, List<SciData>>();

        for (TaskResult res : results) {
            if (!(res.value() instanceof List)) {
                throw new InvalidParameterException(res.value().getClass());
            }

            for (SciData in : (List<SciData>) res.value()) {
                if (newEnv.containsKey(in.getName())) {
                    List<SciData> ldata = newEnv.get(in.getName());
                    ldata.add(in);
                } else {
                    ArrayList<SciData> ldata = new ArrayList<SciData>();
                    ldata.add(in);
                    newEnv.put(in.getName(), ldata);
                }

                //Scilab.sendData(in);
            }
        }

        for (Map.Entry<String, List<SciData>> entry : newEnv.entrySet()) {
            List<SciData> ldata = entry.getValue();
            int i = 1;
            for (SciData in : ldata) {
                in.setName(in.getName() + i);
                i++;
                Scilab.sendData(in);
            }
        }
        executeScript();

        if (debug) {
            logger.info("Receiving outputs");
        }
        ArrayList<SciData> out = new ArrayList<SciData>();
        int i = 0;
        for (String var : outputVars) {
            if (debug) {
                logger.info("Receiving output :" + var);
            }
            out.add(Scilab.receiveDataByName(var));
        }

        return out;
    }

    /**
     * Terminates the Scilab engine
     * @return true for synchronous call
     */
    public boolean terminate() {
        Scilab.Finish();

        return true;
    }

    /**
     * Executes both input and main scripts on the engine
     * @throws Throwable
     */
    protected final void executeScript() throws Throwable {
        if (inputScript != null) {
            if (debug) {
                logger.info("Feeding input");
            }
            Scilab.Exec(inputScript);
        }

        String execScript = prepareScript();
        logger.info("Executing Script");
        File temp;
        BufferedWriter out;
        temp = File.createTempFile("scilab", ".sce");
        temp.deleteOnExit();
        out = new BufferedWriter(new FileWriter(temp));
        out.write(execScript);
        out.close();

        Scilab.Exec("exec(''" + temp.getAbsolutePath() + "'');");
        if (debug) {
            logger.info("Script Finished");
        }
    }

    /**
     * Appends all the script's lines as a single string
     * @return
     */
    private String prepareScript() {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += nl;
        }

        return script;
    }
}
