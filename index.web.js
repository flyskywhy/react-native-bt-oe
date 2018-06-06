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

    static doInit() {}

    static doDestroy() {}

    static addListener(eventName, handler) {}

    static removeListener(eventName, handler) {}

    static notModeAutoConnectMesh() {
        return true;
    }

    static setNetworkPassPhrase({
        passPhrase
    }) {}

    // 自动重连
    static autoConnect({
        userMeshName,
        userMeshPwd,
        otaMac
    }) {}

    // 自动刷新 Notify
    static autoRefreshNotify({
        repeatCount,
        Interval
    }) {}

    static idleMode({
        disconnect
    }) {}

    static startScan({
        meshName,
        outOfMeshName,
        timeoutSeconds,
        isSingleNode,
    }) {}

    static isPassthrough({
        type,
    }) {
        return false;
    }

    static changeBriTmpPwr({
        meshAddress,
        brightness,
        colorTemp,
        power,
        dhmKey,
        type,
        name,
        macAddress,
    }) {}

    static changePower({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {}

    static changeBrightness({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {}

    static changeColorTemp({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {}

    static changeColor({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {}

    static configNode({
        node,
        cfg,
        isToClaim,
    }) {}
}

module.exports = OeBt;
