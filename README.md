[![](https://jitpack.io/v/TTTUUUIII/androidblekit.svg)](https://jitpack.io/#TTTUUUIII/androidblekit)

# Introduce

In order to make the Android Bluetooth interface easier to use, this project encapsulates these interfaces.

# Quick start

settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven(url = "https://jitpack.io")
    }
}
```

build.gradle.kts

```kotlin
dependencies {
    implementation("com.github.TTTUUUIII:androidblekit:2.3.4-alpha")
    ...
}
```

**Initialize library**

```kotlin
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val options = BleKitOptions()
        options.uuidsForNotification = listOf(
            uuidForNotification1, 
            uuidForNotification2, 
            ...
        )
        options.uuidForWritable = uuidForWritable
        options.uuidForAdvertise = uuidForAdvertise
        BleEnvironment.initialize(this, options)
    }
}
```

**As a bluetooth central**

```kotlin
class BleCentralFragmentViewModel: BaseViewModel() {
    private val bleCentral = BleCentral(object: BleCentralCallback() {
        override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
            /*Message received*/
        }

        override fun onError(error: Int, address: String?) {
            super.onError(error, address)
            /*Some error happened*/
        }

        override fun onConnectionStateChanged(state: Int, address: String) {
            /*Connection state chanaged*/
        }
    })

    /**
     * Send message
     */
    private fun writeBytes(bytes: ByteArray) {
        if (!bleCentral.writeBytes(remoteAddressState.value, bytes)) {
            toast(R.string.str_send_failure)
        }
    }
    
    /**
     * connect to device
     */
    fun connect(device: BluetoothDevice) = bleCentral.connect(device)

    /**
     * disconnect from device
     */
    fun disconnect() = bleCentral.disconnect(remoteAddressState.value)
}
```

**As a bluetooth peripheral**

```kotlin

class BlePeripheralFragmentViewModel: BaseViewModel() {

    private val blePeripheral = BlePeripheral(object: BlePeripheralCallback(){
        override fun onMessage(address: String, characteristic: UUID, bytes: ByteArray, offset: Int) {
            /*Message received.*/
        }

        override fun onError(error: Int) {
            /*Some error happened.*/
        }

        override fun onConnectionStateChanged(state: Int, address: String) {
            /*Connection state changed.*/
        }
    })

    /**
     * Send message
     * @param bytes ByteArray
     */
    private fun writeBytes(bytes: ByteArray) {
        if (!blePeripheral.writeBytes(bytes)) {
            toast(R.string.str_send_failure)
        }
    }

    fun setBlePeripheralEnable(enable: Boolean) {
        if (enable) {
            blePeripheral.startup() /*Waiting for central to connect.*/
        } else {
            blePeripheral.shutdown() /*Shutdown the peripheral.*/
        }
    }
}
```