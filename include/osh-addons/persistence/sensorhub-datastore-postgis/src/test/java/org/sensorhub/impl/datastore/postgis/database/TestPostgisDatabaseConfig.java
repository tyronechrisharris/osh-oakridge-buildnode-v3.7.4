package org.sensorhub.impl.datastore.postgis.database;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class TestPostgisDatabaseConfig
{
    @Test
    public void passwordFileTakesPrecedence() throws Exception
    {
        var secret = Files.createTempFile("postgis-password-", ".txt");
        try
        {
            Files.writeString(secret, "from-file\nignored\n", StandardCharsets.UTF_8);
            var config = new PostgisObsSystemDatabaseConfig();
            config.password = "from-config";
            config.passwordFile = secret.toString();

            assertEquals("from-file", config.resolvePassword());
        }
        finally
        {
            Files.deleteIfExists(secret);
        }
    }

    @Test(expected = java.io.IOException.class)
    public void missingPasswordFailsClosed() throws Exception
    {
        new PostgisObsSystemDatabaseConfig().resolvePassword();
    }
}
