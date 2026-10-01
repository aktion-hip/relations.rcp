# Vendored libraries

Every library this bundle needs is committed under `libs/` and listed on
`Bundle-ClassPath`; nothing is imported from the target platform except the `javax.*`,
OSGi and `org.elbe.relations.services` packages named in `Import-Package`. Updating a
library means replacing its jar by hand and editing `META-INF/MANIFEST.MF` and
`build.properties` accordingly.

All jars come from Maven Central (`https://repo1.maven.org/maven2/`) unless the source
column says otherwise.

| Jar | Maven coordinates | Source |
|---|---|---|
| annotations-13.0.jar | org.jetbrains:annotations:13.0 | Maven Central |
| bcpkix-jdk18on-1.78.1.jar | org.bouncycastle:bcpkix-jdk18on:1.78.1 | Maven Central |
| bcprov-jdk18on-1.78.1.jar | org.bouncycastle:bcprov-jdk18on:1.78.1 | Maven Central |
| checker-qual-3.43.0.jar | org.checkerframework:checker-qual:3.43.0 | Maven Central |
| error_prone_annotations-2.28.0.jar | com.google.errorprone:error_prone_annotations:2.28.0 | Maven Central |
| failureaccess-1.0.2.jar | com.google.guava:failureaccess:1.0.2 | Maven Central |
| guava-33.3.1-jre.jar | com.google.guava:guava:33.3.1-jre | Maven Central |
| j2objc-annotations-3.0.0.jar | com.google.j2objc:j2objc-annotations:3.0.0 | Maven Central |
| jakarta.json-api-2.1.3.jar | jakarta.json:jakarta.json-api:2.1.3 | Maven Central |
| java-multibase-v1.1.1.jar | com.github.multiformats:java-multibase:v1.1.1 | JitPack (`https://jitpack.io`) |
| jsr305-3.0.2.jar | com.google.code.findbugs:jsr305:3.0.2 | Maven Central |
| jvm-libp2p-1.3.7-RELEASE.jar | io.libp2p:jvm-libp2p:1.3.7-RELEASE | Cloudsmith (`https://dl.cloudsmith.io/public/libp2p/jvm-libp2p/maven/`) |
| kotlin-stdlib-1.6.21.jar | org.jetbrains.kotlin:kotlin-stdlib:1.6.21 | Maven Central |
| kotlin-stdlib-common-1.6.21.jar | org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21 | Maven Central |
| kotlin-stdlib-jdk7-1.6.21.jar | org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.21 | Maven Central |
| kotlin-stdlib-jdk8-1.6.21.jar | org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21 | Maven Central |
| kotlinx-coroutines-core-1.6.4.jar | org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4 | Maven Central |
| kotlinx-coroutines-core-jvm-1.6.4.jar | org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4 | Maven Central |
| listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar | com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava | Maven Central |
| netty-buffer-4.2.10.Final.jar | io.netty:netty-buffer:4.2.10.Final | Maven Central |
| netty-codec-base-4.2.10.Final.jar | io.netty:netty-codec-base:4.2.10.Final | Maven Central |
| netty-codec-compression-4.2.10.Final.jar | io.netty:netty-codec-compression:4.2.10.Final | Maven Central |
| netty-codec-http-4.2.10.Final.jar | io.netty:netty-codec-http:4.2.10.Final | Maven Central |
| netty-codec-protobuf-4.2.10.Final.jar | io.netty:netty-codec-protobuf:4.2.10.Final | Maven Central |
| netty-common-4.2.10.Final.jar | io.netty:netty-common:4.2.10.Final | Maven Central |
| netty-handler-4.2.10.Final.jar | io.netty:netty-handler:4.2.10.Final | Maven Central |
| netty-resolver-4.2.10.Final.jar | io.netty:netty-resolver:4.2.10.Final | Maven Central |
| netty-transport-4.2.10.Final.jar | io.netty:netty-transport:4.2.10.Final | Maven Central |
| netty-transport-classes-epoll-4.2.10.Final.jar | io.netty:netty-transport-classes-epoll:4.2.10.Final | Maven Central |
| netty-transport-native-unix-common-4.2.10.Final.jar | io.netty:netty-transport-native-unix-common:4.2.10.Final | Maven Central |
| noise-java-22.1.0.jar | tech.pegasys:noise-java:22.1.0 | Consensys (`https://artifacts.consensys.net/public/maven/maven/`) |
| parsson-1.1.6.jar | org.eclipse.parsson:parsson:1.1.6 | Maven Central |
| protobuf-java-3.25.5.jar | com.google.protobuf:protobuf-java:3.25.5 | Maven Central |
| protobuf-javanano-3.0.0-alpha-5.jar | com.google.protobuf.nano:protobuf-javanano:3.0.0-alpha-5 | Maven Central |
| slf4j-api-2.0.9.jar | org.slf4j:slf4j-api:2.0.9 | Maven Central |

## Notes

- `jakarta.json-api` and `parsson` are on the classpath rather than taken from the target
  platform because Parsson registers its `JsonProvider` only through the OSGi ServiceLoader
  Mediator. `ExportSender` therefore constructs `org.eclipse.parsson.JsonProviderImpl`
  directly and never calls `JsonProvider.provider()`.
