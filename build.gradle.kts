plugins {
    id("java")
}

group = "com.nothing"
version = "1.0-SNAPSHOT"

repositories {
    //阿里云
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/google")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/maven-central")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/jcenter")
    }
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    //lombok
    compileOnly("org.projectlombok:lombok:1.18.22")

    //javacv
    implementation("org.bytedeco:javacv-platform:1.5.10")
    implementation("org.bytedeco:javacv:1.5.10")

    implementation("ch.qos.logback:logback-classic:1.2.6")
}

tasks.test {
    useJUnitPlatform()
}