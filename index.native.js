const {
    NativeModules,
    DeviceEventEmitter,
} = require('react-native');
const NativeModule = NativeModules.OeBt;

class OeBt {
    static MESH_ADDRESS_MIN = 0x8001;
    static MESH_ADDRESS_MAX = 0x8FFF;
    static GROUP_ADDRESS_MIN = 0x0000;
    static GROUP_ADDRESS_MAX = 0x7FFF;
    static GROUP_ADDRESS_MASK = 0x7FFF;
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
    static RELAY_TIMES_MAX = 16;
    static DELAY_MS_AFTER_UPDATE_MESH_COMPLETED = 1;
    static DELAY_MS_COMMAND = 320;
    static ALARM_CREATE = 0;
    static ALARM_REMOVE = 1;
    static ALARM_UPDATE = 2;
    static ALARM_ENABLE = 3;
    static ALARM_DISABLE = 4;
    static ALARM_ACTION_TURN_OFF = 0;
    static ALARM_ACTION_TURN_ON = 1;
    static ALARM_ACTION_SCENE = 2;
    static ALARM_TYPE_DAY = 0;
    static ALARM_TYPE_WEEK = 1;

    static passthroughMode = undefined; // send data on serial port to controll bluetooth node
    static lastBrightness = 2;

    static otaFileVersionOffset = 4;    // 把二进制固件作为一个字节数组看待的话，描述着版本号的第一个字节的数组地址
    static otaFileVersionLength = 2;    // 二进制固件中描述版本号用了几个字节

    static doInit() {
        NativeModule.doInit();
    }

    static doDestroy() {
        NativeModule.doDestroy();
    }

    static addListener(eventName, handler) {
        DeviceEventEmitter.addListener(eventName, handler);
    }

    static removeListener(eventName, handler) {
        DeviceEventEmitter.removeListener(eventName, handler);
    }

    static enableBluetooth() {
        NativeModule.enableBluetooth();
    }

    static enableSystemLocation() {
        NativeModule.enableSystemLocation();
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

    static autoConnect({
        userMeshName,
        userMeshPwd,
        otaMac
    }) {
        return NativeModule.autoConnect();
    }

    static autoRefreshNotify({
        repeatCount,
        Interval
    }) {}

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

    static sendData({
        meshAddress,
        value,
    }) {
        NativeModule.sendData(meshAddress, value);
    }

    static maxTo255(value, max) {
        return parseInt(value * 255 / max, 10);
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
    }) {
        for (let mode in this.passthroughMode) {
            if (this.passthroughMode[mode].includes(type)) {
                if (mode === 'sllc') {
                    let data = 'st00';
                    data += this.padHexString(this.maxTo255(brightness, this.BRIGHTNESS_MAX).toString(16));
                    data += this.padHexString(this.maxTo255(colorTemp, this.COLOR_TEMP_MAX).toString(16));
                    data += power === 1 ? 'op' : 'cl';
                    this.sendData({
                        meshAddress,
                        value: [].map.call(data, str => str.charCodeAt(0)),
                    });
                    break;
                }
            }
        }
    }

    static remind({
        meshAddress,
    }) {}

    static isOnline(status) {
        return (status & 0x03) !== this.NODE_STATUS_OFFLINE;
    }

    static isOn(status) {
        return (status & 0x03) === this.NODE_STATUS_ON;
    }

    static changePower({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
        immediate = false,
    }) {
        let changed = false;

        if (this.passthroughMode) {
            for (let mode in this.passthroughMode) {
                if (this.passthroughMode[mode].includes(type)) {
                    if (mode === 'sllc') {
                        let data = 'setpwr00';
                        data += value === 1 ? 'op' : 'cl';

                        this.sendData({
                            meshAddress,
                            value: [].map.call(data, str => str.charCodeAt(0)),
                        });

                        changed = true;
                    }

                    break;
                }
            }
        }

        if (!changed) {
            NativeModule.changePower(JSON.stringify({
                shortId: meshAddress,
                type,
                name,
                id: macAddress,
                dhmKey,
                groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            }), value);
        }
    }

    static changeBrightness({
        meshAddress,
        hue = 0,
        saturation = 0,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        let changed = false;

        if (this.passthroughMode) {
            for (let mode in this.passthroughMode) {
                if (this.passthroughMode[mode].includes(type)) {
                    if (mode === 'sllc') {
                        let data = 'setbri00';
                        data += this.padHexString(this.maxTo255(value, this.BRIGHTNESS_MAX).toString(16));
                        this.sendData({
                            meshAddress,
                            value: [].map.call(data, str => str.charCodeAt(0)),
                        });

                        changed = true;
                    }

                    break;
                }
            }
        }

        if (!changed) {
            this.lastBrightness = value;

            NativeModule.changeBrightness(JSON.stringify({
                shortId: meshAddress,
                type,
                name,
                id: macAddress,
                dhmKey,
                groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            }), hue, saturation, value);
        }
    }

    static changeColorTemp({
        meshAddress,
        value,
        dhmKey,
        type,
        name,
        macAddress,
    }) {
        let changed = false;

        if (this.passthroughMode) {
            for (let mode in this.passthroughMode) {
                if (this.passthroughMode[mode].includes(type)) {
                    if (mode === 'sllc') {
                        let data = 'settmp00';
                        data += this.padHexString(this.maxTo255(value, this.COLOR_TEMP_MAX).toString(16));
                        this.sendData({
                            meshAddress,
                            value: [].map.call(data, str => str.charCodeAt(0)),
                        });

                        changed = true;
                    }

                    break;
                }
            }
        }

        if (!changed) {
            NativeModule.changeColorTemp(JSON.stringify({
                shortId: meshAddress,
                type,
                name,
                id: macAddress,
                dhmKey,
                groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            }), value, this.lastBrightness);
        }
    }

    static changeColor({
        meshAddress,
        hue,
        saturation,
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
        }), hue,
            saturation,
            value);
    }

    static padHexString(string) {
        if (string.length === 1) {
            return '0' + string;
        } else {
            return string;
        }
    }

    static changeScene({
        meshAddress,
        scene,
        hue = 0,
        saturation = 0,
        value,
        colorIds = [1, 2, 3, 4, 5],
        type,
        immediate = false,
    }) {}

    static getTypeFromUuid = uuid => parseInt(uuid.slice(4, 8), 16);

    static configNode({
        node,
        cfg,
        isToClaim,
    }) {
        return NativeModule.configNode(JSON.stringify({
            shortId: node.meshAddress,
            type: node.type,
            name: node.name,
            id: node.macAddress,
            dhmKey: node.dhmKey,
            groups: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }), isToClaim);
    }

    static getTotalOfGroupIndex({
        meshAddress,
    }) {}

    static setNodeGroupAddr({
        meshAddress,
        groupIndex,
        groupAddress,
    }) {}

    static setTime({
        meshAddress,
        year,
        month,
        day,
        hour,
        minute,
        second = 0,
    }) {}

    static getTime({
        meshAddress,
        relayTimes,
    }) {}

    static setAlarm({
        meshAddress,
        crud,
        alarmId,
        status,
        action,
        type,
        month = 1,
        dayOrweek,
        hour,
        minute,
        second = 0,
        sceneId = 0,
    }) {}

    static getAlarm({
        meshAddress,
        relayTimes,
        alarmId,
    }) {}

    static getFirmwareVersion({
        meshAddress = 0xFFFF,
        relayTimes = 7,
        immediate = false,
    }) {}

    static getOtaState({
        meshAddress = 0x0000,
        relayTimes = 7,
        immediate = false,
    }) {}

    static setOtaMode({
        meshAddress = 0x0000,
        relayTimes = 7,     // 转发次数
        otaMode = 'gatt',   // OTA 模式， gatt 为单灯升级， mesh 为单灯升级后由单灯自动通过 mesh 网络发送新固件给其它灯
        type = 0xFB00,      // 设备类型（gatt OTA 模式请忽略此字段）
        immediate = false,
    }) {}

    static stopMeshOta({
        meshAddress = 0xFFFF,
        immediate = false,
    }) {}

    static startOta({
        firmware,
    }) {}

    static isValidFirmware(firmware) {
        return firmware[0] === 0x0E &&
            (firmware[1] & 0xFF) === 0x80 &&
            firmware.length > 6;
    }
}

module.exports = OeBt;
