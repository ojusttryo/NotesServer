package ru.justtry.fileprocessing;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;

@Component
@DependsOn({"mongoMigration"})      // it should start after any changes been made in migrations
public class FileCleaner
{
    final static Logger logger = LogManager.getLogger(FileCleaner.class);

    @Autowired
    private Database database;

    @PostConstruct
    public void run()
    {
        Thread t = new Thread(() -> {
            while (true)
            {
                try
                {
                    logger.info("Removing old unused files...");
                    long time = Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond();
                    int count = database.deleteFilesOlderThan(time);
                    logger.info("Removed " + count + " files");
                }
                catch (Exception e)
                {
                    logger.error(e);
                }

                try
                {
                    Thread.sleep(5 * 60 * 1000);
                }
                catch (Exception e)
                {

                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
