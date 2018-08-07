# ForgeWurst

Wurst Client for Minecraft Forge.

This repository only contains the source code and is not intended for end users. Go to https://forge.wurstclient.net/ for ready-to-use downloads and installation instructions.

## How To Use This Code In Eclipse

1. Clone this repository.

2. Go into the `ForgeWurst MC 1.10.2` folder and run `gradlew setupDecompWorkspace.bat`, followed by `gradlew eclipse.bat`.

3. Repeat step 2 for all of the remaining versions (currently just 1.12.2). Please always use the .bat files, as some versions will not work if you run the Gradle commands directly.

3. In Eclipse, go to `Import... > Existing Projects into Workspace`. Select the folder where you saved this repository as the root directory.

4. Select all of the projects that Eclipse has found (currently just two), then click <kbd>Finish</kbd>. At this point `ForgeWurst MC 1.12.2` is already working, but `ForgeWurst MC 1.10.2` still shows some errors. That's because we have a shared source folder in the 1.12.2 project that the other versions (currently just 1.10.2) also need access to.

5. Right-click on the `ForgeWurst MC 1.10.2` project and select `New > Folder`. Click on <kbd>Advanced >></kbd> and select `Link to alternate location (Linked Folder)`.

6. Click on <kbd>Browse...</kbd> and navigate to `ForgeWurst\ForgeWurst MC 1.12.2\src\shared\java`. Change the "Folder name:" section from "java" to "shared-src", then click <kbd>Finish</kbd>.

7. The `shared-src` folder should now show up in Eclipse, under the `ForgeWurst MC 1.10.2` project. Right-click on this folder and select `Build Path > Use as Source Folder`.

8. Both projects should now be working without any errors. In the future, you will need to repeat steps 5-7 for all projects except the one that hosts the shared source folder.

## Issues & Pull Requests

Issues are disabled. Please [contact me](https://www.wurstclient.net/contact/) if you need help.

Pull Requests are welcome, but I might not always notice them.

## Licensing & Legal Stuff

This code is licensed under the GNU General Public License v3. **You can only use this code in open-source clients that you release under the same license! Using it in closed-source/proprietary clients is not allowed!**

See [LICENSE.txt](LICENSE.txt) for details, as well as a simplified summary on what you can and cannot do with this code.
