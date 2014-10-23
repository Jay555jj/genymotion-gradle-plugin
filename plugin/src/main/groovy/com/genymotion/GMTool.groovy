package main.groovy.com.genymotion

import org.codehaus.groovy.runtime.NullObject

/**
 * Created by eyal on 10/09/14.
 */
class GMTool {

    public static GenymotionConfig GENYMOTION_CONFIG = null

    private static final String GENYTOOL =  "gmtool"
    private static final String VERBOSE =   "--verbose"
    private static final String TIMEOUT =   "--timeout"
    private static final String USERNAME =  "--username"
    private static final String PASSWORD =  "--password"
    private static final String NAME =      "--name"

    //root actions
    private static final String HELP =    "help"
    private static final String VERSION = "version"

    // license actions
    private static final String LICENSE =   "license"
    private static final String INFO =      "info"
    private static final String REGISTER =  "register"
    private static final String COUNT =     "count"
    private static final String VERIFY =    "verify"
    private static final String VALIDITY =  "validity"

    // config actions
    private static final String CONFIG =      "config"
    private static final String PRINT =       "print"
    private static final String RESET =       "reset"
    private static final String CLEARCACHE =  "clearcache"

    //admin actions
    private static final String ADMIN =        "admin"
    private static final String LIST =         "list"
    private static final String TEMPLATES =    "templates"
    private static final String CREATE =       "create"
    private static final String EDIT =         "edit"
    private static final String DELETE =       "delete"
    private static final String CLONE =        "clone"
    private static final String DETAILS =      "details"
    private static final String START =        "start"
    private static final String RESTART =      "restart"
    private static final String STOP =         "stop"
    private static final String STOPALL =      "stopall"
    private static final String FACTORYRESET = "factoryreset"
    private static final String LOGZIP =       "logzip"
    private static final String FULL =         "--full"

    //device actions
    private static final String DEVICE =        "device"
    private static final String PUSH =          "push"
    private static final String PULL =          "pull"
    private static final String INSTALL =       "install"
    private static final String FLASH =         "flash"
    private static final String LOGCAT =        "logcat"
    private static final String ADBDISCONNECT = "adbdisconnect"
    private static final String ADBCONNECT =    "adbconnect"
    private static final String STARTAUTO =     "--start"
    private static final String ALL =           "--all"

    //code returned by gmtool or command line
    public static final int RETURN_NO_ERROR               = 0
    public static final int RETURN_NO_SUCH_ACTION         = 1
    public static final int RETURN_BAD_PARAM_VALUE        = 2
    public static final int RETURN_COMMAND_FAILED         = 3
    public static final int RETURN_VMENGINE_ERROR         = 4
    public static final int RETURN_DEVICE_NOT_FOUND       = 5
    public static final int RETURN_CANT_LOGIN             = 6
    public static final int RETURN_CANT_REGISTER_LICENSE  = 7
    public static final int RETURN_CANT_ACTIVATE_LICENSE  = 8
    public static final int RETURN_NO_ACTIVATED_LICENSE   = 9
    public static final int RETURN_INVALID_LICENSE        = 10
    public static final int RETURN_MISSING_ARGUMENTS      = 11
    public static final int RETURN_VM_NOT_STOPPED         = 12
    public static final int RETURN_LICENSE_REQUIRED       = 13
    public static final int RETURN_COMMAND_NOT_FOUND_UNIX = 127

    static def usage(){
        return cmd([GENYTOOL, "-h"]){line, count ->
        }
    }

    /*
    CONFIG
     */

    static def setLicense(String license, String username="", String password=""){
        return cmd([GENYTOOL, LICENSE, REGISTER, license, "-u="+username, "-p="+password]){line, count ->
        }
    }

    static def resetConfig(){
        return cmd([GENYTOOL, CONFIG, RESET]){line, count ->
        }
    }

    static def clearCache(){
        return cmd([GENYTOOL, CONFIG, CLEARCACHE]){line, count ->
        }
    }

/*
    static def logzip(String path="", String vdName=""){

        def command = [GENYTOOL, LOGZIP]

        if(vdName?.trim()){
            command.push("-n="+vdName)
        }

        if(path?.trim())
            command.push(path)

        return cmd([GENYTOOL, LOGZIP, "-n=", ]){line, count ->
        }
    }
*/

    static def config(){
        //TODO implement when gmtool is ready

    }



    /*
    ADMIN
     */

    static def getAllDevices(boolean verbose=false, boolean fill=true, boolean nameOnly=false){

        def devices = []

        cmd([GENYTOOL, ADMIN, LIST], verbose){line, count ->
            def device = parseList(count, line, nameOnly)
            if(device)
                devices.add(device)
        }

        if(fill && !nameOnly){
            devices.each(){
                it.fillFromDetails()
            }
        }

        devices
    }

    static def getRunningDevices(boolean verbose=false, boolean fill=true, boolean nameOnly=false){

        def devices = []

        cmd([GENYTOOL, ADMIN, LIST, "--running"], verbose){line, count ->
            def device = parseList(count, line, nameOnly)
            if(device)
                devices.add(device)
        }

        if(fill && !nameOnly){
            devices.each(){
                it.fillFromDetails()
            }
        }

        devices
    }

    static def getStoppedDevices(boolean verbose=false, boolean fill=true, boolean nameOnly=false){

        def devices = []

        cmd([GENYTOOL, ADMIN, LIST, "--off"], verbose){line, count ->
            def device = parseList(count, line, nameOnly)
            if(device)
                devices.add(device)
        }

        if(fill && !nameOnly){
            devices.each(){
                it.fillFromDetails()
            }
        }

        devices
    }

    static boolean isDeviceRunning(def device, boolean verbose=false) {
        isDeviceRunning(device.name, verbose)
    }

    static boolean isDeviceRunning(String name, boolean verbose=false) {
        def devices = getRunningDevices(verbose, false, true)
        devices.contains(name)
    }

    private static def parseList(int count, String line, boolean nameOnly) {

        def device

        String[] infos = line.split('\\|')

        String name = infos[2].trim()
        if (nameOnly) {
            device = name
        } else {
            device = new GenymotionVirtualDevice(name)
            device.ip = infos[1].trim()
            device.state = infos[0].trim()
        }
        device
    }


    static def isDeviceCreated(String name){

        if(!name?.trim())
            return false

        //we check if the VD name already exists
        boolean alreadyExists = false

        def devices = GMTool.getAllDevices(false, false)

        devices.each(){
            if(it.name.equals(name))
                alreadyExists = true
        }
        alreadyExists
    }

    static def getTemplatesNames(boolean verbose=false) {

        def templates = []

        def template = null

        cmd([GENYTOOL, ADMIN, TEMPLATES], verbose) { line, count ->

            //if empty line and template filled
            if (!line && template){
                templates.add(template)
                template = null
            }

            String[] info = line.split("\\:")
            switch (info[0].trim()){
                case "Name":
                    if(!template)
                        template = info[1].trim()
                    break
            }
        }
        if(template)
            templates.add(template)

        return templates
    }

    static def getTemplates(boolean verbose=false){

        def templates = []

        def template = new GenymotionTemplate()

        cmd([GENYTOOL, ADMIN, TEMPLATES, FULL], verbose) { line, count ->

            //if empty line and template filled
            if (!line && template.name){
                templates.add(template)
                template = new GenymotionTemplate()
            }

            String[] info = line.split("\\:")
            switch (info[0].trim()){
                case "Name":
                    if(!template.name)
                        template.name = info[1].trim()
                    break
                case "UUID":
                    template.uuid = info[1].trim()
                    break
                case "Description":
                    template.description = info[1].trim()
                    break
                case "Android Version":
                    template.androidVersion = info[1].trim()
                    break
                case "Genymotion Version":
                    template.genymotionVersion = info[1].trim()
                    break
                case "Screen Width":
                    template.width = info[1].trim().toInteger()
                    break
                case "Screen Height":
                    template.height = info[1].trim().toInteger()
                    break
                case "Screen Density":
                    template.density = info[1].trim()
                    break
                case "Screen DPI":
                    template.dpi = info[1].trim().toInteger()
                    break
                case "Nb CPU":
                    template.nbCpu = info[1].trim().toInteger()
                    break
                case "RAM":
                    template.ram = info[1].trim().toInteger()
                    break
                case "Internal Storage":
                    template.internalStorage = info[1].trim().toInteger()
                    break
                case "Telephony":
                    template.telephony = info[1].trim().toBoolean()
                    break
                case "Nav Bar Visible":
                    template.navbarVisible = info[1].trim().toBoolean()
                    break
                case "Virtual Keyboard":
                    template.virtualKeyboard = info[1].trim().toBoolean()
                    break
            }

        }
        if(template.name)
            templates.add(template)

        return templates
    }

    static boolean isTemplateExists(String template, boolean verbose=false) {

        if(!template?.trim())
            return false

        def templates = getTemplatesNames(verbose)
        templates.contains(template)
    }

    static def createDevice(GenymotionVDLaunch device){
        return createDevice(device.template, device.name)
    }

    static def createDevice(GenymotionTemplate template){
        return createDevice(template.name, template.name)
    }

    static def createDevice(def template, def deviceName, def dpi="", def width="", def height="", def virtualKeyboard="", def navbarVisible="", def nbcpu="", def ram=""){

        def exitValue = noNull(){
            cmd([GENYTOOL, ADMIN, CREATE, template, deviceName,
                 '--dpi='+dpi, '--width='+width, '--height='+height, '--virtualkeyboard='+virtualKeyboard, '--navbar='+navbarVisible, '--nbcpu='+nbcpu, "--ram="+ram]){line, count ->
            }
        }

        if(exitValue == RETURN_NO_ERROR)
            return new GenymotionVirtualDevice(deviceName, dpi, width, height, virtualKeyboard, navbarVisible, nbcpu, ram)
        else
            return exitValue
    }

    static def editDevice(GenymotionVirtualDevice device){
        return editDevice(device.name, device.dpi, device.width, device.height, device.virtualKeyboard, device.navbarVisible, device.nbCpu, device.ram)
    }

    static def editDevice(def deviceName, def dpi="", def width="", def height="", def virtualKeyboard="", def navbarVisible="", def nbcpu="", def ram=""){

        return noNull(){
            return cmd([GENYTOOL, ADMIN, EDIT, deviceName,
                 '--dpi='+dpi, '--width='+width, '--height='+height, '--virtualkeyboard='+virtualKeyboard, '--navbar='+navbarVisible, '--nbcpu='+nbcpu, "--ram="+ram]){line, count ->
            }
        }
    }

    static def deleteDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return deleteDevice(device.name, verbose)
    }

    static def deleteDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, DELETE, deviceName], verbose){line, count ->
        }
    }

    static def cloneDevice(GenymotionVirtualDevice device, def name, boolean verbose=false){
        return cloneDevice(device.name, name, verbose)
    }

    static def cloneDevice(def deviceName, def newName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, CLONE, deviceName, newName], verbose){line, count ->
        }
    }

    static def getDevice(String name, boolean verbose=false){

        if(name == null)
            return null

        def device = new GenymotionVirtualDevice(name)
        return getDevice(device, verbose)
    }

    static def getDevice(def device, boolean verbose=false){

        if(device == null)
            return null

        //we get the device details
        cmd([GENYTOOL, ADMIN, DETAILS, device.name], verbose){line, count ->

            String[] info = line.split("\\:")
            switch (info[0].trim()){
                case "Name":
                    device.name = info[1].trim()
                    break
                case "Android Version":
                    device.androidVersion = info[1].trim()
                    break
                case "Genymotion Version":
                    device.genymotionVersion = info[1].trim()
                    break
                case "Screen Width":
                    device.width = info[1].trim().toInteger()
                    break
                case "Screen Height":
                    device.height = info[1].trim().toInteger()
                    break
                case "Screen Density":
                    device.density = info[1].trim()
                    break
                case "Screen DPI":
                    device.dpi = info[1].trim().toInteger()
                    break
                case "Nb CPU":
                    device.nbCpu = info[1].trim().toInteger()
                    break
                case "RAM":
                    device.ram = info[1].trim().toInteger()
                    break
                case "Telephony":
                    device.telephony = info[1].trim().toBoolean()
                    break
                case "Nav Bar Visible":
                    device.navbarVisible = info[1].trim().toBoolean()
                    break
                case "Virtual Keyboard":
                    device.virtualKeyboard = info[1].trim().toBoolean()
                    break
                case "UUID":
                    device.uuid = info[1].trim()
                    break
                case "Path":
                    device.path = info[1].trim()
                    break
                case "State":
                    device.state = info[1].trim()
                    break
                case "IP":
                    device.ip = info[1].trim()
                    break
            }
        }
        device
    }

    static def startDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return startDevice(device.name, verbose)
    }

    static def startDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, START, deviceName], verbose) {line, count ->
        }
    }

    static def restartDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return restartDevice(device.name, verbose)
    }

    static def restartDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, RESTART, deviceName], verbose){line, count ->
        }
    }

    static def stopDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return stopDevice(device.name, verbose)
    }

    static def stopDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, STOP, deviceName], verbose){line, count ->
        }
    }

    static def stopAllDevices(boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, STOPALL], verbose){line, count ->
        }
    }

    static def resetDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return resetDevice(device.name, verbose)
    }

    static def resetDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, ADMIN, FACTORYRESET, deviceName], verbose){line, count ->
        }
    }

    static def startAutoDevice(def template, def deviceName, boolean verbose=false){
        def device = createDevice(template, deviceName, verbose)

        if(!device instanceof GenymotionVirtualDevice)
            return device

        def startExit = startDevice(device)

        if(startExit == RETURN_NO_ERROR)
            return device
        else
            return startExit
    }


    /*
    Device
     */

    static def pushToDevice(GenymotionVirtualDevice device, def files, boolean verbose=false){
        pushToDevice(device.name, files, verbose)
    }

    static def pushToDevice(def deviceName, def files, boolean verbose=false){

        if(!files)
            return false

        def exitValues = []

        if(files instanceof String)
            files = [files]

        files.each(){

            def command = [GENYTOOL, DEVICE, '-n='+deviceName, PUSH]
            if(files instanceof Map){
                command.push(it.key)
                command.push(it.value)
            }
            else
                command.push(it)

            int exitValue = cmd(command, verbose){line, count ->
            }
            exitValues.add(exitValue)
        }

        return exitValues
    }

    static def pullFromDevice(GenymotionVirtualDevice device, def files, boolean verbose=false){
        pullFromDevice(device.name, files, verbose)
    }

    static def pullFromDevice(String deviceName, String source, String destination, boolean verbose=false){
        pullFromDevice(deviceName, [(source):destination], verbose)
    }

    static def pullFromDevice(def deviceName, def files, boolean verbose=false){

        if(!files)
            return false

        def exitValues = []

        if(files instanceof String)
            files = [files]

        files.each(){

            def command = [GENYTOOL, DEVICE, '-n='+deviceName, PULL]
            if(files instanceof Map){
                command.push(it.key)
                command.push(it.value)
            }
            else
                command.push(it)

            int exitValue = cmd(command, verbose){line, count ->
            }
            exitValues.add(exitValue)
        }

        return exitValues
    }

    static def installToDevice(GenymotionVirtualDevice device, def apks, boolean verbose=false){
        installToDevice(device.name, apks, verbose)
    }

    static def installToDevice(def deviceName, def apks, boolean verbose=false){

        if(!apks)
            return false

        if(apks instanceof String){
            cmd([GENYTOOL, DEVICE, '-n='+deviceName, INSTALL, apks], verbose){line, count ->
            }

        } else if(apks instanceof List){
            def exitValues = []
            apks.each(){
                int exitValue = cmd([GENYTOOL, DEVICE, '-n='+deviceName, INSTALL, it], verbose){line, count ->
                }
                exitValues.add(exitValue)
            }
            return exitValues
        }
    }

    static def flashDevice(GenymotionVirtualDevice device, def zips, boolean verbose=false){
        return flashDevice(device.name, zips, verbose)
    }

    static def flashDevice(def deviceName, def zips, boolean verbose=false){

        if(!zips)
            return false

        if(zips instanceof String){
            return cmd([GENYTOOL, DEVICE, '-n='+deviceName, FLASH, zips], verbose){line, count ->
            }

        } else if(zips instanceof List){
            def exitValues = []
            zips.each(){
                int exitValue = cmd([GENYTOOL, DEVICE, '-n='+deviceName, FLASH, it], verbose){line, count ->
                }
                exitValues.add(exitValue)
            }
            return exitValues
        }
    }

    static def adbDisconnectDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return adbDisconnectDevice(device.name, verbose)
    }

    static def adbDisconnectDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, DEVICE, deviceName, ADBDISCONNECT], verbose){line, count ->
        }
    }

    static def adbConnectDevice(GenymotionVirtualDevice device, boolean verbose=false){
        return adbConnectDevice(device.name, verbose)
    }

    static def adbConnectDevice(def deviceName, boolean verbose=false){
        return cmd([GENYTOOL, DEVICE, deviceName, ADBCONNECT], verbose){line, count ->
        }
    }

    static def routeLogcat(GenymotionVirtualDevice device, path, boolean verbose=false){
        return routeLogcat(device.name, path, verbose)
    }

    static def routeLogcat(def deviceName, def path, boolean verbose=false){
        return cmd([GENYTOOL, DEVICE, deviceName, LOGCAT, path], verbose){line, count ->
        }
    }



    /*
    TOOLS
     */

    /**
     * Fire a command line and process the result.
     * This function runs a closure for each line returned by the prompt.
     * The closure contains the parameters:
     * - <b>line</b> (containing the line's text)
     * - <b>count</b> (index of the line)
     *
     * @param command the command line to execute. It can be a String or a table
     * @param verbose true if you want to print each line returned by the prompt
     * @param c the closure to implement after the call
     */
    static def cmd(def command, boolean verbose=true, Closure c){

        def toExec = command

        //we eventually insert the genymotion binary path
        if(GENYMOTION_CONFIG != null && GENYMOTION_CONFIG.genymotionPath != null){
            if(toExec instanceof String){
                toExec = GENYMOTION_CONFIG.genymotionPath + toExec
            } else {
                toExec = command.clone()
                toExec[0] = GENYMOTION_CONFIG.genymotionPath + toExec[0]
            }
        }

        if(verbose) {
            if(toExec[0].contains(GENYTOOL))
                toExec.addAll(1, [VERBOSE])

            println toExec
        }
        Process p = toExec.execute()
        StringBuffer error = new StringBuffer()
        StringBuffer out = new StringBuffer()
        p.consumeProcessOutput(out, error)

        p.waitForOrKill(GENYMOTION_CONFIG.processTimeout)

        if(verbose){
            println "out:" + out.toString()
        }

        out.eachLine {line, count ->
            c(line, count)
        }

        return handleExitValue(p.exitValue(), error)
    }

    static def handleExitValue(int exitValue, StringBuffer error) {
        if(exitValue == RETURN_NO_ERROR){
            //do nothing
        } else {
            println "error: "+error.toString()

            if(GENYMOTION_CONFIG.abortOnError){
                throw new GMToolException("GMTool command failed. Error code: $exitValue. Check the output to solve the problem")
            }
        }
        exitValue
    }
/**
     * Avoid null.toString returning "null"
     *
     * @param c the code to execute
     * @return the c's return
     */
    static def noNull(Closure c){
        //set null.toString to return ""
        String nullLabel = null.toString()
        NullObject.metaClass.toString = {return ''}

        def exit = c()

        //set as defaut
        NullObject.metaClass.toString = {return nullLabel}

        return exit
    }


}
