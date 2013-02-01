# -------------------------------------------------------------------
# QuizBox Remote Version 5c
# 12/08/2012
# Ted Meyers
# -------------------------------------------------------------------
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# -------------------------------------------------------------------
# 2/1/13 ver 5c -   added version info
#                   added status request/response
# -------------------------------------------------------------------
#
from synapse.switchboard import *
from synapse.platforms import *

# Example base node addr:
#    "\x5D\x24\x50" # Address of the PCs USB-attached node
#    "\x00\x00\x01" # Portal address

VER = "5c"
HEX = "0123456789ABCDEF"
NET_ID = 3
CHANNEL_ID = 4
BASE_NODE_ADDR_ID = 201
NODE_NAME_ID = 202
START_MODE_ID = 204

MODE_STARTUP = 'S'
MODE_TEST  = 'T'
MODE_READY = 'R'
MODE_LOCK  = 'L'
MODE_DEMO  = 'D'
MODE_POWER = 'P'

NONE = 0
BUTTON_A_PIN = 30     # pin 18 := 30
BUTTON_B_PIN = 29     # pin 19 := 29
BUTTON_C_PIN = 28     # pin 20 := 28
DISPLAY_P_PIN =  8    # pin 13 :=  8
DISPLAY_A_PIN = 31    # pin 17 := 31
DISPLAY_B_PIN = 37    # pin 15 := 37
DISPLAY_C_PIN = 15    # pin 11 := 15

nodeName = NONE
nodeNameX = NONE
baseNodeAddr = NONE
baseNodeAddrX = NONE
curSelected = NONE
curMode = MODE_STARTUP
powerLEDon = False
isUpdateSelect = False

timeOutTicks = 0
timeOutTicksStart = 500     # 10 msec periods before radio times out
buttonTicks = 0
buttonTicksStart = 21       # 10 msec periods between allowed button changes
updateTicks = 0
updateTicksStart = 50       # 10 msec periods between sending status updates
startupTicks = 200          # 10 msec periods before startup time expires

# ------------------------------------------
# Network functions / Remote commands
# ------------------------------------------
    
# sendBackStatus
def gstat():
    addr = rpcSourceAddr()
    data = getStatusString()
    rpc(addr, 'rstat', baseNodeAddr, data)
   
# sendBackLQ
def sq():
    global timeOutTicks
    timeOutTicks = timeOutTicksStart
    addr = rpcSourceAddr()
    data = chr(getLq())
    rpc(addr, 'rq', baseNodeAddr, data)

# resetTimeout
def rt():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart

# showLEDs
def sled(a, b, c, p):
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        writePin(DISPLAY_A_PIN, a) 
        writePin(DISPLAY_B_PIN, b) 
        writePin(DISPLAY_C_PIN, c)
        writePin(DISPLAY_P_PIN, p)

# clearCurrentSelected
def cs():
    global curSelected
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        curSelected = NONE
        writePin(DISPLAY_A_PIN, False) 
        writePin(DISPLAY_B_PIN, False) 
        writePin(DISPLAY_C_PIN, False)

#updateSelected
def us(selected):
    global curSelected
    global timeOutTicks
    global isUpdateSelect
    
    if _checkAddr(rpcSourceAddr()):
        isUpdateSelect = True
        timeOutTicks = timeOutTicksStart
        curSelected = selected
        writePin(DISPLAY_A_PIN, (selected==BUTTON_A_PIN)) 
        writePin(DISPLAY_B_PIN, (selected==BUTTON_B_PIN)) 
        writePin(DISPLAY_C_PIN, (selected==BUTTON_C_PIN))

# setModePower (doesn't really make sense not to clear)
def mp():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        _setModeAndClearOnChange(MODE_POWER)
    
# setModeLock (without clear)
def sl():
    global curMode
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        curMode = MODE_LOCK
    
# setModeLock (with clear)
def ml():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        _setModeAndClear(MODE_LOCK)
    
# SetmodeReady (without a clear)
def sr():
    global curMode
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        curMode = MODE_READY

# setModeReady (with clear)
def mr():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        _setModeAndClearOnChange(MODE_READY)
    
# SetmodeTest (without a clear)
def st():
    global curMode
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        curMode = MODE_TEST

# setModeTest (with clear)
def mt():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        _setModeAndClearOnChange(MODE_TEST)
    
# SetmodeDemo (without a clear)
def sd():
    global curMode
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        curMode = MODE_DEMO

# setModeDemo (with clear)
def md():
    global timeOutTicks
    if _checkAddr(rpcSourceAddr()):
        timeOutTicks = timeOutTicksStart
        _setModeAndClearOnChange(MODE_DEMO)

# ------------------------------------------
# Setters & Getters (Nonvolatile Parameters)
# ------------------------------------------

def setBaseNodeAddr(address): 
    """Call this at least once, and specify the BASE node's address""" 
    global baseNodeAddr
    baseNodeAddr = address 
    saveNvParam(BASE_NODE_ADDR_ID, baseNodeAddr)
    _updateBaseNodeAddrX()

def setNodeName(name): 
    """Call this at least once, and specify the node's name""" 
    global nodeName
    nodeName = name 
    saveNvParam(NODE_NAME_ID, nodeName)
    _updateNodeNameX()

def setStartupMode(mode):
    if mode==MODE_TEST or mode==MODE_READY or mode==MODE_LOCK or mode==MODE_DEMO or mode==MODE_POWER:
        saveNvParam(START_MODE_ID, mode)

def getBaseNodeAddr():
    return baseNodeAddr

def getBaseNodeAddrX():
    return baseNodeAddrX

def getNodeName():
    return nodeName

def getNodeNameX():
    return nodeNameX

def getStartupMode():
    m = loadNvParam(START_MODE_ID)
    if m==None: m = MODE_DEMO
    return m

def getModeValue():
    """Get the current mode value"""
    return curMode
    
def getSelectValue():
    """Get the current selected value"""
    return curSelected
    
def getPowerLEDOnValue():
    """Get the power-save on flag"""
    return powerLEDon

def getStatusString():
    """Get the current status of this remote, as a string"""
    status = "m:" + str(curMode) + ", s:" + str(curSelected) + \
        ", p:" + str(powerLEDon) + ", q:" + str(getLq()) + \
        ", t:" + str(timeOutTicks) + ", n:" + str(loadNvParam(NET_ID)) + \
        ", c:" + str(loadNvParam(CHANNEL_ID)) + ", v:" + VER
    return status

def checkAddr(addr):
    return _checkAddr(addr)

# ------------------------------------------
# Private functions
# ------------------------------------------
def _checkAddr(addr):
    if addr==None: return False
    return baseNodeAddr[0]==addr[0] and baseNodeAddr[1]==addr[1] and baseNodeAddr[2]==addr[2]
  
def _setModeAndClear(mode):
    """Clear displayed pin and save the mode"""
    global curMode
    global curSelected
    curMode = mode
    curSelected = NONE
    writePin(DISPLAY_A_PIN, False) 
    writePin(DISPLAY_B_PIN, False) 
    writePin(DISPLAY_C_PIN, False)
  
def _setModeAndClearOnChange(mode):
    """If and only if the mode has changed, clear displayed pin and save the mode"""
    global curMode
    global curSelected
    if curMode!=mode:
        curMode = mode
        curSelected = NONE
        writePin(DISPLAY_A_PIN, False) 
        writePin(DISPLAY_B_PIN, False) 
        writePin(DISPLAY_C_PIN, False)

def _setupButton(pin):
    """Setup the button pins; input, pullup, and monitored"""
    setPinDir(pin, False)
    setPinPullup(pin, True)
    monitorPin(pin, True)
    
def _setupDisplay(pin):
    """Setup the display pins, and turn them on; output and power on"""
    setPinDir(pin, True)
    writePin(pin, True)

def _updateNodeNameX():
    global nodeNameX
    nodeNameX = nodeName + "_" + imageName() + "_" + _getHexAddr(localAddr())

def _updateBaseNodeAddrX():
    global baseNodeAddrX
    baseNodeAddrX = _getHexAddr(baseNodeAddr)

def _getHexAddr(addr):
    a0 = ord(addr[0])
    a1 = ord(addr[1])
    a2 = ord(addr[2])
    hexaddr = HEX[a0/16] + HEX[a0%16] + HEX[a1/16] + HEX[a1%16]
    hexaddr = hexaddr + HEX[a2/16] + HEX[a2%16]    
    return hexaddr
 
@setHook(HOOK_GPIN)
def _buttonEvent(pinNum, isSet):
    """Automatically called when a button changes state, do no call manually"""
    global buttonTicks
    global timeOutTicks
    global updateTicks
    global curSelected
    global isUpdateSelect
    
    if curMode==MODE_STARTUP:
        # Disallow button presses on startup
        return
    elif curMode==MODE_POWER:
        # Power savings mode (sleeping) -- do nothing!
        return
    elif curMode==MODE_LOCK:
        # Locked, so don't do anything
        return
    elif buttonTicks>0 or isSet:
        # If button change is too soon (buttonTicks) or 
        # button change is not pushed down (isSet).
        return
    elif pinNum!=curSelected:
        if curMode==MODE_READY:
            # Update display then send Button Status message
            writePin(DISPLAY_A_PIN, (pinNum==BUTTON_A_PIN)) 
            writePin(DISPLAY_B_PIN, (pinNum==BUTTON_B_PIN)) 
            writePin(DISPLAY_C_PIN, (pinNum==BUTTON_C_PIN))
            updateTicks = updateTicksStart
            # Send Button selected Return request message to base
            lq = chr(getLq())
            rpc(baseNodeAddr, 'br', pinNum, lq)
        elif curMode==MODE_TEST:
            # Display is updated by a message back from the base
            updateTicks = updateTicksStart
            # Send Button selected Return requested message to base
            lq  = chr(getLq())
            rpc(baseNodeAddr, 'br', pinNum, lq)
        elif curMode==MODE_DEMO:
            # Update display, no Button Status message is sent
            writePin(DISPLAY_A_PIN, (pinNum==BUTTON_A_PIN)) 
            writePin(DISPLAY_B_PIN, (pinNum==BUTTON_B_PIN)) 
            writePin(DISPLAY_C_PIN, (pinNum==BUTTON_C_PIN))
            timeOutTicks = timeOutTicksStart
        # Update button timeout and selected
        buttonTicks = buttonTicksStart
        curSelected = pinNum
        isUpdateSelect = False

@setHook(HOOK_10MS)
def _doEvery10ms(tick):
    """Automatically called every 10 msec, do not call manually"""
    global buttonTicks
    global timeOutTicks
    global updateTicks
    global startupTicks
    global curMode
    global curSelected
    
    if curMode==MODE_POWER:
        return
    
    # Decrement ticks
    if buttonTicks > 0: buttonTicks = buttonTicks - 1
    if timeOutTicks > 0: timeOutTicks = timeOutTicks - 1
    
    # Check if update needs to be sent
    if updateTicks>0:
        updateTicks = updateTicks - 1
    else:
        # Send Remote Status update
        lq = chr(getLq())
        rpc(baseNodeAddr, 'rs', curSelected, lq)
        updateTicks = updateTicksStart
        if curMode == MODE_READY:
            if not isUpdateSelect:
                curSelectedcurSelected = NONE
                writePin(DISPLAY_A_PIN, False)
                writePin(DISPLAY_B_PIN, False)
                writePin(DISPLAY_C_PIN, False)
                # Send Button selected Return request message to base
                rpc(baseNodeAddr, 'br', curSelected, lq)
    
    if curMode == MODE_STARTUP:
        if startupTicks <= 0:
            startupTicks = 0
            curMode = getStartupMode()
            _setModeAndClear(curMode)
        else:
            startupTicks = startupTicks - 1
 
@setHook(HOOK_1S) 
def _doEverySec(tick):
    """Automatically called once per second, do no call manually"""
    # This function handles power features
    global powerLEDon
    
    # If power saving mode is on
    if curMode==MODE_POWER:
        # If first time then turn everything off
        if powerLEDon:
            powerLEDon = False
            writePin(DISPLAY_P_PIN, False)
            writePin(DISPLAY_A_PIN, False) 
            writePin(DISPLAY_B_PIN, False) 
            writePin(DISPLAY_C_PIN, False)
    else:
        # Check time-out (no base found)
        if timeOutTicks>0:
            # Not a time-out, make sure power is on
            if not powerLEDon:
                powerLEDon = True
                writePin(DISPLAY_P_PIN, powerLEDon)
        else:
            # Timed-out, blink power LED, and clear if Demo or Test mode
            powerLEDon = not powerLEDon
            writePin(DISPLAY_P_PIN, powerLEDon)
            if curMode==MODE_DEMO | curMode==MODE_TEST:
                writePin(DISPLAY_A_PIN, False) 
                writePin(DISPLAY_B_PIN, False) 
                writePin(DISPLAY_C_PIN, False)
            else:
                writePin(DISPLAY_A_PIN, (curSelected==BUTTON_A_PIN)) 
                writePin(DISPLAY_B_PIN, (curSelected==BUTTON_B_PIN)) 
                writePin(DISPLAY_C_PIN, (curSelected==BUTTON_C_PIN))
   
@setHook(HOOK_STARTUP)
def _startupEvent():
    """System startup code, invoked automatically (do not call this manually)"""
    global curSelected
    global curMode
    global baseNodeAddr
    global nodeName
    
    curSelected = NONE
    curMode = MODE_STARTUP
    
    baseNodeAddr = loadNvParam(BASE_NODE_ADDR_ID)
    nodeName = loadNvParam(NODE_NAME_ID)
    if baseNodeAddr==None: baseNodeAddr = "xxx"
    if nodeName==None: nodeName = "r"
    _updateNodeNameX()
    _updateBaseNodeAddrX()

    # Set button polling rate - 3 is the highest rate
    setRate(3)

    _setupButton(BUTTON_A_PIN)
    _setupButton(BUTTON_B_PIN)
    _setupButton(BUTTON_C_PIN)

    _setupDisplay(DISPLAY_P_PIN)
    _setupDisplay(DISPLAY_A_PIN)
    _setupDisplay(DISPLAY_B_PIN)
    _setupDisplay(DISPLAY_C_PIN)