# React Native Bluetooth OE

[![npm version](http://img.shields.io/npm/v/react-native-bt-oe.svg?style=flat-square)](https://npmjs.org/package/react-native-bt-oe "View this project on npm")
[![npm downloads](http://img.shields.io/npm/dm/react-native-bt-oe.svg?style=flat-square)](https://npmjs.org/package/react-native-bt-oe "View this project on npm")
[![npm licence](http://img.shields.io/npm/l/react-native-bt-oe.svg?style=flat-square)](https://npmjs.org/package/react-native-bt-oe "View this project on npm")
[![Platform](https://img.shields.io/badge/platform-android-989898.svg?style=flat-square)](https://npmjs.org/package/react-native-bt-oe "View this project on npm")

Component implementation for Bluetooth Mesh SDK of [OE](www.oecore.com) .

## Install
For RN >= 0.60
```shell
npm i --save react-native-bt-oe
```

For RN < 0.60
```shell
npm i --save react-native-bt-oe@1.0.x
```

For RN >= 0.60 , just in `android/settings.gradle`
```
include ':csrmeshlibrary'
project(':csrmeshlibrary').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bt-csr/android/libs')
include ':luckysdk'
project(':luckysdk').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bt-oe/android/libs')
```

For RN < 0.60, need files edited below:

In `android/app/build.gradle`
```
dependencies {
    implementation project(':react-native-bt-oe')
}
```

In `android/app/src/main/java/com/YourProject/MainApplication.java`
```
import com.oe.luckysdk.framework.OeBtPackage;
...
    new OeBtPackage(),
```

In `android/build.gradle`
```
buildscript {
    dependencies {
        // maybe need it
        classpath 'org.javafxports:jfxmobile-plugin:1.0.10-SNAPSHOT'
    }
```

In `android/settings.gradle`
```
include ':csrmeshlibrary'
project(':csrmeshlibrary').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bt-csr/android/libs')
include ':luckysdk'
project(':luckysdk').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bt-oe/android/libs')
include ':react-native-bt-oe'
project(':react-native-bt-oe').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bt-oe/android')
```

## Usage

```jsx
import React from 'react';
import { View } from 'react-native';
import meshModule from 'react-native-bt-oe';

export default class MeshModuleExample extends React.Component {
    constructor(props) {
        super(props);
        meshModule.passthroughMode = {
            oe: [
                10240,
                10337,
            ],
            sllc: [
                30848,
            ],
        };
    }

    componentDidMount() {
        meshModule.addListener('leScan', this.onLeScan);
        meshModule.doInit();
    }

    onLeScan = data => console.warn(data)

    render() {
        return (
            <View/>
        );
    }
}
```

## Donate
To support my work, please consider donate.

- ETH: 0xd02fa2738dcbba988904b5a9ef123f7a957dbb3e

- <img src="https://raw.githubusercontent.com/flyskywhy/flyskywhy/main/assets/alipay_weixin.png" width="500">
