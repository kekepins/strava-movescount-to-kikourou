package kikstrava.service;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigManagerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void prefersExternalConfigOverPackagedTemplate() throws Exception {
        Path root = temporaryFolder.newFolder().toPath();
        Path externalConfig = Files.createDirectories(root.resolve("conf"))
                .resolve("stravakik.conf");
        Path packagedConfig = Files.createDirectories(root.resolve("app/conf"))
                .resolve("stravakik.conf");
        Files.createFile(externalConfig);
        Files.createFile(packagedConfig);

        Path codeLocation = root.resolve("app/KikStrava.jar");
        Path differentWorkingDirectory = temporaryFolder.newFolder().toPath();

        assertEquals(externalConfig,
                ConfigManager.findConfigFile(differentWorkingDirectory, codeLocation).orElseThrow());
    }

    @Test
    public void findsConfigNextToPackagedApplication() throws Exception {
        Path root = temporaryFolder.newFolder().toPath();
        Path packagedConfig = Files.createDirectories(root.resolve("app/conf"))
                .resolve("stravakik.conf");
        Files.createFile(packagedConfig);

        assertEquals(packagedConfig,
                ConfigManager.findConfigFile(root, root.resolve("app/KikStrava.jar")).orElseThrow());
    }
}
