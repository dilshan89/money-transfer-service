plugins {
	java
	id ("io.freefair.lombok") version "8.0.1"
}

group = "com.boku"
version = "1.0.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.javalin:javalin:4.0.0")
	implementation("org.projectlombok:lombok:1.18.28")
	implementation("org.slf4j:slf4j-simple:2.0.5")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
	testImplementation("org.junit.vintage:junit-vintage-engine:5.9.2")
	testImplementation("org.mockito:mockito-inline:4.5.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
