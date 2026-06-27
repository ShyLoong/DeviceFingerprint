# DeviceFingerprint

一个 Android 设备指纹采集与风险检测演示应用。通过采集多维度设备特征生成唯一设备指纹，并提供跨设备相似度对比与风险环境检测能力。

## 功能特性

| 模块 | 说明 |
|------|------|
| **多维特征采集** | 采集 50+ 设备特征，覆盖硬件、系统、网络、应用四个维度 |
| **SimHash 指纹** | 基于加权 SimHash 算法生成 64-bit 设备指纹 |
| **设备相似度对比** | 支持本地历史设备对比 + JSON 导入外部设备对比 |
| **风险环境检测** | 检测 Root / Xposed / Frida / 模拟器 / 代理 / VPN / 虚拟定位等风险 |
| **中英双语** | 支持简体中文 / English 切换 |
| **主题换色** | 支持蓝 / 紫 / 绿 / 红 / 橙 五种主题色 |

## 采集维度

- **硬件维度**：品牌、型号、主板、CPU、内存、存储、屏幕分辨率/DPI/刷新率、传感器、摄像头、电池
- **系统维度**：Android 版本、安全补丁、语言、时区、Android ID、Pseudo ID、Root 状态、模拟器检测、USB 调试
- **网络维度**：IP 地址、WiFi SSID/BSSID、运营商、网络类型、代理、VPN
- **应用维度**：包名、版本、签名哈希、安装时间

## 相似度阈值

| 相似度 | 判定 | 建议动作 |
|--------|------|----------|
| 90-100% | 同一设备 | 允许 |
| 75-90%  | 可能相同 | 行为检查 |
| 60-75%  | 不确定 | 多因素认证 |
| 40-60%  | 新设备 | 验证 + 监控 |
| 0-40%   | 高风险 | 拦截/审核 |

## 技术架构

```
com.device.fingerprint/
├── collector/          # 信息采集器
│   ├── HardwareCollector.java   # 硬件信息
│   ├── SystemCollector.java     # 系统信息 + Root/模拟器检测
│   ├── NetworkCollector.java    # 网络信息
│   ├── ApplicationCollector.java # 应用信息
│   └── RiskDetector.java        # 风险环境检测
├── generator/
│   └── FingerprintGenerator.java # SimHash 指纹生成
├── similarity/
│   └── SimilarityCalculator.java # 多维度相似度计算
├── model/
│   ├── DeviceInfo.java          # 设备信息数据模型
│   ├── ComparisonResult.java    # 对比结果模型
│   └── FieldDiff.java           # 字段差异模型
├── ui/                  # Activity 界面
│   ├── MainActivity.java        # 主界面
│   ├── DeviceDetailActivity.java # 详情页
│   ├── CompareActivity.java    # 对比页
│   ├── HistoryActivity.java    # 历史记录
│   ├── SettingsActivity.java   # 设置页
│   └── AboutActivity.java      # 关于页
├── ui/adapter/          # RecyclerView 适配器
├── utils/               # 工具类
│   ├── StorageManager.java      # 本地存储 (SharedPreferences + Gson)
│   ├── SettingsManager.java     # 设置管理 (语言/主题)
│   └── PermissionHelper.java    # 权限管理
└── res/                 # 布局与资源
```

## 构建要求

- **Android Studio**: 2023.1.1 (Hedgehog) 或更高
- **compileSdk**: 34
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34
- **Java**: 1.8

## 依赖库

- `androidx.appcompat:appcompat:1.4.2`
- `com.google.android.material:material:1.6.1`
- `androidx.recyclerview:recyclerview:1.2.1`
- `androidx.cardview:cardview:1.0.0`
- `com.google.code.gson:gson:2.10.1`

## 权限说明

| 权限 | 用途 | 级别 |
|------|------|------|
| `READ_PHONE_STATE` | 获取 IMEI / 设备标识符 | 危险权限 |
| `ACCESS_FINE_LOCATION` | 获取基站 / WiFi 信息 | 危险权限 |
| `ACCESS_COARSE_LOCATION` | 获取网络位置信息 | 危险权限 |
| `ACCESS_WIFI_STATE` | 获取 WiFi 状态 | 普通权限 |
| `ACCESS_NETWORK_STATE` | 获取网络状态 | 普通权限 |
| `INTERNET` | 网络检测 | 普通权限 |
| `BLUETOOTH` | 蓝牙信息 | 普通权限 |

> 所有设备信息**仅存储在本地**，不会上传到任何服务器。本应用仅供演示和教育用途。

## 构建方式

### 方式一：Android Studio

1. 打开项目根目录
2. 等待 Gradle 同步完成
3. 点击 **Run → Run 'app'** 或按 `Shift+F10`

### 方式二：命令行 Gradle

```bash
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

### 方式三：手动构建脚本

```bash
bash build_apk.sh
```

> 需要提前配置 `ANDROID_SDK` 环境变量，并确保 `build-tools/34.0.0` 已安装。

## 快速上手

1. 首次启动时授予所需权限（可选，跳过会限制部分功能）
2. 点击 **"Collect Device Info"** 采集设备信息
3. 查看概览卡片：设备名称、指纹、风险评分、特征数量
4. 点击 **Details** 查看完整设备信息（可复制 JSON）
5. 点击 **Compare** 对比两台设备（支持本地历史 / JSON 导入）
6. 点击 **History** 查看历史采集记录
7. 在 **Settings** 中切换语言和主题颜色

## 截图

> 主界面、详情页、对比页、历史记录、设置页等界面截图待补充。

## 注意事项

- Android 10+ 对 `IMEI` 访问有严格限制，会显示 "Restricted"
- `OAID` 需要接入 MSA 移动安全联盟 SDK，本演示版本未接入
- 部分特征需要 Root 权限才能获取更深层信息，本应用不请求 Root 权限
- 在生产环境使用时，请确保遵守当地隐私法规（如 PIPL、GDPR），并获取用户同意

## 开源协议

[MIT License](LICENSE)

---

**DeviceFingerprint Demo** — 2024
