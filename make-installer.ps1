$DIR='jre'
Remove-Item $DIR -Recurse -ErrorAction Ignore
jlink `
--no-header-files `
--no-man-pages `
--compress=2 `
--strip-debug `
--add-modules java.base,java.desktop,java.net.http,java.naming,jdk.crypto.ec `
--output jre
mvn clean package
jpackage `
--app-version "1.0" `
--input 'SwingUI\target\modules' `
--dest "release" `
--name "SwiftRift" `
--type "exe" `
--runtime-image 'jre' `
--main-jar 'swift-rift.jar' `
--main-class 'com.hawolt.LeagueClientUI' `
--win-shortcut `
--win-dir-chooser `
--icon 'resources\swift-rift.ico'
