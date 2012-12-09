# -------------------------------------------------------------------
# QuizBox Base Version 5c
# 12/08/2012
# Ted Meyers
# -------------------------------------------------------------------
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# -------------------------------------------------------------------
#
from synapse.switchboard import *
from synapse.platforms import *

HEX = "0123456789ABCDEF"
NODE_NAME_ID = 202
BAUD_RATE_ID = 203
MODE_TEST  = 'T'
MODE_READY = 'R'
MODE_LOCK  = 'L'
MODE_DEMO  = 'D'
MODE_POWER = 'P'
curMode=MODE_READY
refreshSeconds = 2
refreshTimer = refreshSeconds
nodeName = 0
nodeNameX = 0

def _updateNodeNameX():
    global nodeNameX
    addr = localAddr()
    a0 = ord(addr[0])
    a1 = ord(addr[1])
    a2 = ord(addr[2])
    ver = imageName() + ".v5c"
    hexaddr = HEX[a0/16] + HEX[a0%16] + HEX[a1/16] + HEX[a1%16]
    hexaddr = hexaddr + HEX[a2/16] + HEX[a2%16]    
    nodeNameX = nodeName + "_" + ver + "_" + hexaddr    

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
    
def sendLQRequest(addr):
    mcastRpc(1, 3, 'sq')
   
def sendUpdateDisplay(a,b,c,p):
    mcastRpc(1, 3, 'sd', a, b, c, p) 

def getStatus():
    return curMode

# Remote Status
def rs(data):
    addr=rpcSourceAddr()
    if data==0: data="00"
    print "(+", addr, data, ')',

# Button Selected
def bs(data):
    addr=rpcSourceAddr()
    if data==0: data="00"
    print "(#", addr, data, ')',

# Button selected Return requested
def br(data):
    addr=rpcSourceAddr()
    rpc(addr, 'us', data)
    if data==0: data="00"
    print "(#", addr, data, ')',
    
# Return link Quality
def rq(data):
    addr=rpcSourceAddr()
    print "(@", addr, data, ')',

def setupSerial():
    rate = loadNvParam(BAUD_RATE_ID)
    if rate==None: rate = 57600
    initUart(1, rate)     # baud rate (1 == 115200)!
    flowControl(1, False) # <= set flow control to True or False as needed
    stdinMode(0, False)   # Line mode, echo
    crossConnect(DS_UART1, DS_STDIO)
    
def setBaudCode(code):
    rate = 57600
    if code==0: rate=57600
    elif code==1: rate=1      # 115200
    elif code==2: rate=2400
    elif code==3: rate=38400
    elif code==4: rate=14400
    elif code==5: rate=57600
    elif code==9: rate=9600
    else: rate=57600
    saveNvParam(BAUD_RATE_ID, rate)
    
def setBaudRate(rate):
    saveNvParam(BAUD_RATE_ID, rate)
    
def getBaudRateX():
    val = "57600"
    rate = loadNvParam(BAUD_RATE_ID)
    if rate==None: val = "57600"
    elif rate==1: val = "115200"
    elif rate==-7936: val = "57600"
    else: val = str(rate)
    return val

def printHelp():
    print "Help: "
    print "   b - print current baud rate"
    print "   Bx - set baud rate (1=115200, 2=2400, 3=38400, 4=14400, 5=57600, 9=9600)"
    print "   Nname - set name to 'name'"
    print "   S - print status"
    print "   V - print version"
    print "   Q - send LQ request"
    print "   Uxxxx - send update request (x=T/F)"
    print "   C - send clear"
    print "   P,L,R,T,D - set mode (power, lock, ready, test, demo"
    print
    
@setHook(HOOK_STARTUP)
def _startupEvent():
    global nodeName
    
    setupSerial()
    nodeName = loadNvParam(NODE_NAME_ID)
    if nodeName==None: nodeName = "Base"
    _updateNodeNameX()
    
@setHook (HOOK_STDIN)
def _processInput(data):
    global nodeName
    global nodeNameX
    
    i = 0
    sz = len(data)
    while i<sz:
        d=data[i]
        if d=='C':
            sendClear()
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
        elif d=='H' or d=='h' or d=='?':
            printHelp()
        elif d=='Q':
            if sz>i+3:
                sendLQRequestTo(data[i+1:i+4])
                i=i+3
            else: sendLQRequest()
        elif d=='U':
            sz = len(data)
            if (i+4)<sz:
                a = data[i+1]=='T'
                b = data[i+2]=='T'
                c = data[i+3]=='T'
                p = data[i+4]=='T'
                sendUpdateDisplay(a,b,c,p)
                print "(=", a, b, c, p, ")",
                i = i+4
        elif d=='b':
            rate = loadNvParam(BAUD_RATE_ID)
            print "(^b:", getBaudRateX() + ")",
        elif d=='B':
            if (i+1)<sz: 
                setBaudCode(int(data[i+1]))
                print "(^b:", getBaudRateX() + ")",
                i = i+1
        elif d=='N':
            setNodeName(data[i+1:sz])
            i=sz;
            print "(^3:", nodeNameX, ")",
        elif d=='S':
            print "(!", curMode, chr(255-getLq()), ')',
        elif d=='V':
            print "(^2:", nodeNameX, ")",
        elif d=='Z':
            pass
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
