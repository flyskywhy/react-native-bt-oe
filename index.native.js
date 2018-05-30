const {
    NativeModules,
    DeviceEventEmitter,
} = require('react-native');
const NativeModule = NativeModules.OeBt;

class OeBt {
    static MESH_ADDRESS_MIN = 0x8001;
    static MESH_ADDRESS_MAX = 0x8FFF;
    static BRIGHTNESS_MIN = 5;
    static BRIGHTNESS_MAX = 255;
    static COLOR_TEMP_MIN = 1;
    static COLOR_TEMP_MAX = 255;
    static NODE_STATUS_OFF = 0;
    static NODE_STATUS_ON = 1;
    static NODE_STATUS_OFFLINE = 2;
    static DELAY_MS_AFTER_UPDATE_MESH_COMPLETED = 1;

    static doInit() {
        NativeModule.doInit();
    }

    // static doInit() {
    //     NativeModule.doInit();
    // }

    static doDestroy() {
        NativeModule.doDestroy();
    }

    static addListener(eventName, handler) {
        DeviceEventEmitter.addListener(eventName, handler);
    }

    static removeListener(eventName, handler) {
        DeviceEventEmitter.removeListener(eventName, handler);
    }

    static notModeAutoConnectMesh() {
        return NativeModule.notModeAutoConnectMesh();
    }

    static setNetworkPassPhrase({
        passPhrase
    }) {
        return NativeModule.setNetworkPassPhrase(JSON.stringify({
            name: '',   // 这个 name 除了不能为 undefined ，可以是任意值
            netKey: passPhrase,
        }));
    }

    // 自动重连
    static autoConnect({
        userMeshName,
        userMeshPwd,
        otaMac
    }) {
        return NativeModule.autoConnect();
    }

    // 自动刷新 Notify
    static autoRefreshNotify({
        repeatCount,
        Interval
    }) {
        return NativeModule.autoRefreshNotify(repeatCount, Interval);
    }

    static idleMode({
        disconnect
    }) {
        return NativeModule.idleMode(disconnect);
    }

    static startScan({
        meshName,
        outOfMeshName,
        timeoutSeconds,
        isSingleNode,
    }) {
        NativeModule.startScan(timeoutSeconds);
    }

    static changePower({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        NativeModule.changePower(JSON.stringify({
            shortId: meshAddress,
            type,
            name,
            id: macAddress,
            dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), value);
    }

    static changeBrightness({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        NativeModule.changeBrightness(JSON.stringify({
            shortId: meshAddress,
            type,
            name,
            id: macAddress,
            dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), value);
    }

    static changeColorTemp({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        NativeModule.changeColorTemp(JSON.stringify({
            shortId: meshAddress,
            type,
            name,
            id: macAddress,
            dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), value);
    }

    static changeColor({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        NativeModule.changeColor(JSON.stringify({
            shortId: meshAddress,
            type,
            name,
            id: macAddress,
            dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), value);
    }

    static configNode({
        node,
        cfg,
        isToClaim,
    }) {
            // console.warn({shortId: node.meshAddress,
            //                         type: node.type,
            //                         name: node.name,
            //                         id: node.macAddress,
            //                         dhmKey: node.dhmKey,})
        return NativeModule.configNode(JSON.stringify({
            shortId: node.meshAddress,
            type: node.type,
            name: node.name,
            id: node.macAddress,
            dhmKey: node.dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), isToClaim);
    }
}

module.exports = OeBt;
