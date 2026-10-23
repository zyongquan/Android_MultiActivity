// 实验三「多 Activity」应用模块
// 1 个 MainActivity + 4 个二级 Activity：
//   任务1 正向传参 / 任务2 返回数据 / 任务3 相机拍照 / 任务4 秒表计时
// 采用 Java 编写，依赖 appcompat（提供 AppCompatActivity）与 core（提供 FileProvider）。
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.zhouyongquan2024180750"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.zhouyongquan2024180750"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // 打开视图绑定：布局中的 id 会自动生成类型安全的 binding 类，
    // 代码里就不需要再写 findViewById()。
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
}
