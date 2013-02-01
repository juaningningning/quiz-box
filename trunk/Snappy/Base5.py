# -------------------------------------------------------------------
# QuizBox Base Version 5f
# 12/08/2012
# Ted Meyers
# -------------------------------------------------------------------
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# -------------------------------------------------------------------
# 2/1/13 ver 5f -   added quiet mode (only a mode on base unit)
#                   fixed baud rate display
# -------------------------------------------------------------------
#
from synapse.switchboard import *
from synapse.platforms import *

VER = "5f"
HEX = "0123456789ABCDEF"
NET_ID = 3
CHANNEL_ID = 4
NODE_NAME_ID = 202
BAUD_RATE_ID = 203
START_MODE_ID = 204
MODE_TEST  = 'T'
MODE_READY = 'R'
MODE_LOCK  = 'L'
MODE_DEMO  = 'D'
MODE_POWER = 'P'
MODE_QUIET = 'Q'
DEFAULT_BAUD_RATE = 57600

curMode=MODE_READY
refreshSeconds = 2
refreshTimer = refreshSeconds
nodeName = 0
nodeNameX = 0
textFormat = False

# ------------------------------------------
# Public functions
# ------------------------------------------
def getNodeNameX():
    return nodeNameX

def setNodeName(name): 
    """Call this at least once, and specify the Node's name""" 
    global nodeName
    nodeName = name 
    saveNvParam(NODE_NAME_ID, nodeName)
    _updateNodeNameX()

def getNodeName():
    return nodeName
     
def setModeQuiet():
    global curMode
    curMode=MODE_QUIET

def sendResetTimeOut():
    mcastRpc(1, 3, 'rt')

def sendClear():
    global select
    mcastRpc(1, 3, 'cs')
     
def sendModePower():
    global curMode
    curMode=MODE_POWER
    mcastRpc(1, 3, 'mp')
    
def sendModeLock():
    global curMode
    curMode=MODE_LOCK
    mcastRpc(1, 3, 'sl')
    
def sendModeLockClear():
    global curMode
    curMode=MODE_LOCK
    mcastRpc(1, 3, 'ml')
   
def sendModeReady():
    global curMode
    curMode=MODE_READY
    mcastRpc(1, 3, 'sr')
   
def sendModeReadyClear():
    global curMode
    curMode=MODE_READY
    mcastRpc(1, 3, 'mr')

def sendModeTest():
    global curMode
    curMode=MODE_TEST
    mcastRpc(1, 3, 'st')

def sendModeTestClear():
    global curMode
    curMode=MODE_TEST
    mcastRpc(1, 3, 'mt')
     
def sendModeDemo():
    global curMode
    curMode=MODE_DEMO
    mcastRpc(1, 3, 'sd')
     
def sendModeDemoClear():
    global curMode
    curMode=MODE_DEMO
    mcastRpc(1, 3, 'md')
    
def sendLQRequestTo(addr):
    rpc(addr,'sq')
    
def sendLQRequest():
    mcastRpc(1, 3, 'sq')
   
def sendUpdateDisplay(a,b,c,p):
    mcastRpc(1, 3, 'sled', a, b, c, p) 
    
def sendStatusRequestTo(addr):
    rpc(addr, 'gstat')
    
def sendStatusRequest():
    mcastRpc(1, 3, 'gstat')

def getStatus():
    return curMode

# ------------------------------------------
# Network functions
# ------------------------------------------
# Remote Status
def rstat(base, data):
    addr=rpcSourceAddr()
    addr=_translateNode(addr)
    base=_translateNode(base)
    print "(%S:", addr, ", ", base, "; ", data, ')'
    
# Remote LQ Status
def rs(data, lq):
    addr=rpcSourceAddr()
    if data==0: data="00"
    if curMode!=MODE_QUIET: print "(+", addr, data, lq, ')',

# Button selected Return requested
def br(data, lq):
    addr=rpcSourceAddr()
    rpc(addr, 'us', data)
    if data==0: data="00"
    print "(#", addr, data, lq, ')',
    
# Return link Quality
def rq(base, data):
    addr=rpcSourceAddr()
    lq = getLq()
    print "(@", addr, base, data, chr(lq), ')',
    if textFormat:
        addr=_translateNode(addr)
        base=_translateNode(base)
        print "(%Q:", addr, ',', base, ',', ord(data), ',', lq, ')'

# ------------------------------------------
# Setters & Getters (Nonvolatile Parameters)
# ------------------------------------------
def setStartupMode(mode):
    if mode==MODE_TEST or mode==MODE_READY or mode==MODE_LOCK or mode==MODE_DEMO or mode==MODE_POWER:
        saveNvParam(START_MODE_ID, mode)
    
def setBaudRate(rate):
    saveNvParam(BAUD_RATE_ID, rate)

def setBaudCode(code):
    rate = DEFAULT_BAUD_RATE
    if code==0: rate=DEFAULT_BAUD_RATE
    elif code==1: rate=1        # Special case for 115200
    elif code==2: rate=2400
    elif code==3: rate=38400
    elif code==4: rate=14400
    elif code==5: rate=57600    # This is the same as -7936 signed
    elif code==9: rate=9600
    else: rate=DEFAULT_BAUD_RATE
    saveNvParam(BAUD_RATE_ID, rate)
    
def getStartupMode():
    m = loadNvParam(START_MODE_ID)
    if m==None: m = MODE_READY
    return m
    
def getBaudRateX():
    val = str(DEFAULT_BAUD_RATE)
    rate = loadNvParam(BAUD_RATE_ID)
    if rate==None: rate = DEFAULT_BAUD_RATE # Use Default val
    
    if rate==1: val = "115200"            # Special Case
    elif rate==-7936: val = "57600"         # Special Case
    else: val = str(rate)
    return val

# ------------------------------------------
# Private functions
# ------------------------------------------
def _setupSerial():
    rate = loadNvParam(BAUD_RATE_ID)
    if rate==None: rate = DEFAULT_BAUD_RATE
    initUart(1, rate)     # baud rate (1 == 115200)!
    flowControl(1, False) # <= set flow control to True or False as needed
    stdinMode(0, False)   # Line mode, echo
    crossConnect(DS_UART1, DS_STDIO)

def _printHelp():
    print "(%H: "
    print "  b - print current baud rate"
    print "  Bx - set baud rate (1=115200, 2=2400, 3=38400, 4=14400, 5=57600, 9=9600)"
    print "  Nname - set name to 'name'"
    print "  v - print version"
    print "  s - print status"
    print "  S - send Status request"
    print "  q - send LQ request"
    print "  cx - change channel to x (on all connected devices)"
    print "  Uxxxx - send update request (x=T/F)"
    print "  C - send clear"
    print "  m - print startup mode"
    print "  Q - set Quiet mode (doesn't send mode updates or show updates)"
    print "  P,L,R,T,D - set mode (power, lock, ready, test, demo)"
    print "  Mx - set startup mode (x = P,L,R,T,D)"
    print "  Ax - set startup mode on remotes (x = P,L,R,T,D)"
    print "  F - Turn text format on;  f - turn text format off"
    print "  [Node Info: ", nodeNameX, " b: ", getBaudRateX(), " sm: ", getStartupMode(), \
        " nid: ", str(loadNvParam(NET_ID)), " ch: ", str(loadNvParam(CHANNEL_ID)), "]"
    print ")"

def _updateNodeNameX():
    global nodeNameX
    ver = imageName() + ".v" + VER
    hexaddr = _translateNode(localAddr())
    nodeNameX = nodeName + "_" + ver + "_" + hexaddr

def _translateNode(addr):
    a0 = ord(addr[0])
    a1 = ord(addr[1])
    a2 = ord(addr[2])
    hex = HEX[a0/16] + HEX[a0%16] + HEX[a1/16] + HEX[a1%16] + HEX[a2/16] + HEX[a2%16]
    return hex
    
@setHook(HOOK_STARTUP)
def _startupEvent():
    global nodeName
    global curMode
    
    curMode = getStartupMode()
    
    _setupSerial()
    nodeName = loadNvParam(NODE_NAME_ID)
    if nodeName==None: nodeName = "Base"
    _updateNodeNameX()
    
@setHook (HOOK_STDIN)
def _processInput(data):
    global textFormat
    
    i = 0
    sz = len(data)
    while i<sz:
        d=data[i]
        if d=='C':
            sendClear()
        elif d=='Q':
            setModeQuiet()
        elif d=='P':
            sendModePower()
        elif d=='L':
            sendModeLock()
        elif d=='l':
            sendModeLockClear()
        elif d=='R':
            sendModeReady()
        elif d=='r':
            sendModeReadyClear()
        elif d=='T':
            sendModeTest()
        elif d=='t':
            sendModeTestClear()
        elif d=='D':
            sendModeDemo()
        elif d=='d':
            sendModeDemoClear()
        elif d=='F':
            textFormat=True
            print "(%F:On)"
        elif d=='f':
            textFormat=False
        elif d=='H' or d=='h' or d=='?':
            _printHelp()
        elif d=='q':
            if sz>i+3:
                sendLQRequestTo(data[i+1:i+4])
                i=i+3
            else: sendLQRequest()
            if textFormat: print "(%q:Sent_LQ)"
        elif d=='U':
            sz = len(data)
            if (i+4)<sz:
                a = data[i+1]=='T'
                b = data[i+2]=='T'
                c = data[i+3]=='T'
                p = data[i+4]=='T'
                sendUpdateDisplay(a,b,c,p)
                print "(%U:", a, b, c, p, ")",
                if textFormat: print
                i = i+4
        elif d=='b':
            rate = loadNvParam(BAUD_RATE_ID)
            print "(%b:", getBaudRateX(), ")",
            if textFormat: print
        elif d=='B':
            if (i+1)<sz: 
                setBaudCode(int(data[i+1]))
                print "(%B:", getBaudRateX(), ")",
                if textFormat: print
                i = i+1
        elif d=='A':
            if (i+1)<sz:
                mcastRpc(1, 3, 'setStartupMode', data[i+1]);
                if textFormat: print "(%A:", data[i+1], ")"
                i = i+1        
        elif d=='c':
            if (i+2)<sz:
                ch = int(data[i+1:i+3])
                i = i+2
            elif (i+1)<sz:
                ch = int(data[i+1])
                i = i+1
            if textFormat: print "(%c:", ch, ")"
            mcastRpc(1, 3, 'saveNvParam', CHANNEL_ID, ch)
            saveNvParam(CHANNEL_ID, ch)
            mcastRpc(1, 3, 'reboot')
            reboot()
        elif d=='m':
            print "(%m:", getStartupMode(), ")",
            if textFormat: print
        elif d=='M':
            if (i+1)<sz:
                setStartupMode(data[i+1])
                if textFormat: print "(%M:", getStartupMode(), ")"
                i = i+1
        elif d=='N':
            setNodeName(data[i+1:sz])
            i=sz;
            if textFormat: print "(%N:", nodeNameX, ")"
        elif d=='s':
            lq = getLq()
            print "(!", curMode, chr(lq), ')',
            if textFormat: print "(%s:", curMode, ",", lq, ')'
        elif d=='S':
            if sz>i+3:
                sendStatusRequest(data[i+1:i+4])
                i=i+3
            if textFormat: print "(%S: Sending...)"
            sendStatusRequest()
        elif d=='v':
            nid = ", nid=" + str(loadNvParam(NET_ID))
            ch = ", ch=" + str(loadNvParam(CHANNEL_ID))
            print "(^", VER, ":", nodeNameX, nid, ch, ")",
            if textFormat: print
        elif d=='Z':
            if textFormat: print
        i=i+1
 
@setHook(HOOK_1S) 
def _doEverySec(tick):
    global refreshTimer
    refreshTimer = refreshTimer-1
    if refreshTimer<0:
        refreshTimer = refreshSeconds
        if curMode==MODE_TEST: mcastRpc(1, 3, 'st')
        elif curMode==MODE_READY: mcastRpc(1, 3, 'sr')
        elif curMode==MODE_LOCK: mcastRpc(1, 3, 'sl')
        elif curMode==MODE_DEMO: mcastRpc(1, 3, 'sd')
        elif curMode==MODE_POWER: mcastRpc(1, 3, 'mp')
        else: mcastRpc(1, 3, 'rt')
