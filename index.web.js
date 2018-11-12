class OeBt {
    static MESH_ADDRESS_MIN = 0x8001;
    static MESH_ADDRESS_MAX = 0x8FFF;
    static HUE_MIN = 0;
    static HUE_MAX = 360;
    static SATURATION_MIN = 0;
    static SATURATION_MAX = 100;
    static BRIGHTNESS_MIN = 2;
    static BRIGHTNESS_MAX = 100;
    static COLOR_TEMP_MIN = 1;
    static COLOR_TEMP_MAX = 100;    // actually is 255 max after Util.CoolWarm(temp / 100.0D, lastBrightness / 100.0D) in JAVA, where temp / 100.0D is warmRatio, lastBrightness / 100.0D is brightnessRatio
    static NODE_STATUS_OFF = 0;
    static NODE_STATUS_ON = 1;
    static NODE_STATUS_OFFLINE = 2;
    static DELAY_MS_AFTER_UPDATE_MESH_COMPLETED = 1;

    static passthroughMode = undefined; // send data on serial port to controll bluetooth node
    static lastBrightness = 2;

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

    static getTypeFromUuid = uuid => parseInt(uuid.slice(4, 8), 16);

    static configNode({
        node,
        cfg,
        isToClaim,
    }) {}

    static getTotalOfGroupIndex({
        meshAddress,
    }) {}

    static setNodeGroupAddr({
        meshAddress,
        groupIndex,
        groupAddress,
    }) {}
}

module.exports = OeBt;
