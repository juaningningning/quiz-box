# -------------------------------------------------------------------
# QuizBox Base Version 5a
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
    hexaddr = HEX[a0/16] + HEX[a0%16] + HEX[a1/16] + HEX[a1%16]
    hexaddr = hexaddr + HEX[a2/16] + HEX[a2%16]    
    nodeNameX = nodeName + "_" + imageName() + "_" + hexaddr    

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

@setHook(HOOK_STARTUP)
def _startupEvent():
    global nodeName
    
    #Setup Serial
    initUart(1, 57600)    # <= put your desired baud rate here!  (1 == 115200)!!!
    flowControl(1, False) # <= set flow control to True or False as needed
    stdinMode(0, False)   # Line mode, echo
    crossConnect(DS_UART1, DS_STDIO)
    nodeName = loadNvParam(NODE_NAME_ID)
    _updateNodeNameX()
    
@setHook (HOOK_STDIN)
def _processInput(data):
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