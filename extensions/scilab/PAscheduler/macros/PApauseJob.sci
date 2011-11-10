function PApauseJob(jobid)
    global ('PA_connected');
    if ~PAisConnected()
        error('A connection to the ProActive scheduler is not established, see PAconnect');
    end
    if or(type(jobid)==[1 5 8]) then
        jobid = string(jobid);
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);            
    env = jinvoke(solver,'getEnvironment');
    jinvoke(env,'pauseJob',jobid);
    jremove(solver,env,ScilabSolver);
endfunction