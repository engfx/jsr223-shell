package jsr223.shell;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class AnsDeployShellHandler extends ShellHandler {
	
	public AnsDeployShellHandler(Shell shell) {
		super(shell);
	}
	
	/*
	 * @Override public CommandResult run(String command, ScriptContext
	 * scriptContext) throws IOException { if (scriptContext instanceof
	 * AnsDepScriptContext) { return this.run(command,
	 * (AnsDepScriptContext)scriptContext); } else { return this.run(command,
	 * scriptContext); }
	 * 
	 * }
	 */
	public CommandResult run(String command, ScriptContext scriptContext) throws IOException {
		 Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
	        CommandLine commandLine;
	        File temporaryFile = null;
	        if (bindings.get(KEY_LANGUAGE) != null && bindings.get(KEY_LANGUAGE).toString().startsWith(".")) {
	            String fileExtension = bindings.get(KEY_LANGUAGE).toString();
	            temporaryFile = commandAsTemporaryFile(command, fileExtension, (String) bindings.get(KEY_CHARSET_COMMAND));
	            commandLine = shell.createByFile(temporaryFile);
	        } else {
	            commandLine = shell.createByCommand(command);
	        }
	        Map<String, String> variables = new HashMap<String, String>(System.getenv());
	        Map<String, String> bindingVariables = build(bindings);
	        variables.putAll(bindingVariables);
	        
	         CommandResult result = execute(commandLine, variables, scriptContext);
	        
	        if (temporaryFile != null) {
	            temporaryFile.delete();
	        }
	        return result;
	    }

	 
	    private CommandResult execute(CommandLine commandLine, Map<String, String> environment, ScriptContext context) throws IOException {
	        DefaultExecutor executor = new DefaultExecutor();
	        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
	        executor.setWatchdog(watchdog);
	        executor.setExitValues(null);
	        
	        String charSet = (String)context.getAttribute("ResponseCharSet"); 
	        charSet = charSet != null ? charSet : "utf-8";
	        OutputStream outOS, errorOS;
	        if (context instanceof AnsDepScriptContext) {
	        	AnsDepScriptContext ansDepContext = (AnsDepScriptContext) context;
	        	outOS = new AnsDepInputStreamWriterOutputStream(ansDepContext.getDestinations(), charSet);
	        	errorOS = new AnsDepInputStreamWriterOutputStream(ansDepContext.getErrorDestinations(), charSet);
	        } else {
		        outOS = new AnsDepInputStreamWriterOutputStream(context.getWriter(), charSet);
		        errorOS = new AnsDepInputStreamWriterOutputStream(context.getErrorWriter(), charSet);
	        }
            PumpStreamHandler streamHandler = new PumpStreamHandler(outOS, errorOS);
            executor.setStreamHandler(streamHandler);
            
	        int exitValue = executor.execute(commandLine, environment);
	        watchdog.destroyProcess();
	        return new CommandResult().setExitValue(exitValue).setErrorMessage("任务运行失败！");
	    } 
	 
	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

}
