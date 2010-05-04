importClass(org.ow2.proactive.scheduler.util.console.SchedulerModel);

function exMode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	SchedulerModel.setExceptionMode(displayStack, displayOnDemand);
}

function help(){
	SchedulerModel.help();
}

function submit(xmlDescriptor){
    return SchedulerModel.submit(""+xmlDescriptor);
}

function pausejob(jobId){
    return SchedulerModel.pause(""+jobId);
}

function resumejob(jobId){
    return SchedulerModel.resume(""+jobId);
}

function killjob(jobId){
    return SchedulerModel.kill(""+jobId);
}

function removejob(jobId){
    SchedulerModel.remove(""+jobId);
}

function result(jobId){
    return SchedulerModel.result(""+jobId);
}

function tresult(jobId,taskName){
    return SchedulerModel.tresult(""+jobId,""+taskName);
}

function output(jobId){
    SchedulerModel.output(""+jobId);
}

function toutput(jobId,taskName){
    SchedulerModel.toutput(""+jobId,""+taskName);
}

function priority(jobId, priority){
    SchedulerModel.priority(""+jobId,""+priority);
}

function jobstate(jobId){
	return SchedulerModel.jobState(""+jobId);
}

function exec(commandFilePath){
	SchedulerModel.exec(""+commandFilePath);
}

function listjobs(){
	SchedulerModel.schedulerState();
}

function jmxinfo(){
	SchedulerModel.JMXinfo();
}

function test(){
	SchedulerModel.test();
}

function exit(){
	SchedulerModel.exit();
}

function start(){
    return SchedulerModel.start();
}

function stop(){
    return SchedulerModel.stop();
}

function pause(){
    return SchedulerModel.pause();
}

function freeze(){
    return SchedulerModel.freeze();
}

function resume(){
    return SchedulerModel.resume();
}

function shutdown(){
    return SchedulerModel.shutdown();
}

function kill(){
    return SchedulerModel.kill();
}

function linkrm(rmURL){
	return SchedulerModel.linkRM(""+rmURL);
}

function setLogsDir(logsDir){
	if (logsDir == undefined){
		logsDir = "";
	}
	SchedulerModel.setLogsDir(""+logsDir);
}

function viewlogs(nbLines){
	SchedulerModel.viewlogs(""+nbLines);
}

function viewDevlogs(nbLines){
	SchedulerModel.viewDevlogs(""+nbLines);
}

function changePolicy(newPolicyName){
	SchedulerModel.changePolicy(""+newPolicyName);
}

var scheduler = SchedulerModel.getSchedulerInterface();