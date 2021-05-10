package ru.justtry.fileprocessing;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;

@Component
@Slf4j
@RequiredArgsConstructor
@DependsOn({"mongoMigration"})      // it should start after any changes been made in migrations
public class FileCleaner
{
    private final Database database;

    @Value("${files.cleaner.enabled:false}")
    private boolean enabled;

    @PostConstruct
    public void run()
    {
        if (!enabled)
            return;

        Thread t = new Thread(() -> {
            while (true)
            {
                try
                {
                    log.info("Removing old unused files...");
                    long time = Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond();
                    int count = database.deleteFilesOlderThan(time);
                    log.info("Removed " + count + " files");
                }
                catch (Exception e)
                {
                    log.error(e.toString());
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
