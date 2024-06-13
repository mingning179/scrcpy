plugins {
    id("java")
}

group = "com.nothing"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    //lombok
    compileOnly("org.projectlombok:lombok:1.18.22")

    //javacv
    implementation("org.bytedeco:javacv-platform:1.5.6")
}

tasks.test {
    useJUnitPlatform()
}